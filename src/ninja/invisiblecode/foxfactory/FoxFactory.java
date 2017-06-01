package ninja.invisiblecode.foxfactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import ninja.invisiblecode.commons.Status;
import ninja.invisiblecode.foxfactory.annotations.Fox;
import ninja.invisiblecode.foxfactory.annotations.NoFox;
import ninja.invisiblecode.foxfactory.exceptions.MissingAnnotationException;
import ninja.invisiblecode.foxfactory.key.DefaultKey;
import ninja.invisiblecode.foxfactory.key.NamedDefaultKey;

public abstract class FoxFactory<Key, Product> {

	protected static final Status						status		= new Status.SystemStatus();

	protected Map<Key, ProductInfo<? extends Product>>	products	= new HashMap<>();
	protected String									name;

	protected <T extends Product> FoxFactory(Class<Product> product, String name,
			Supplier<Collection<Class<? extends T>>> source) {
		this.name = name;
		if (!product.isAnnotationPresent(Fox.class))
			throw new MissingAnnotationException("FoxFactory requires @Fox annotation on product.");
		for (Class<? extends T> pro : source.get()) {
			if (Modifier.isInterface(pro.getModifiers()))
				continue;
			if (Modifier.isAbstract(pro.getModifiers()))
				continue;
			if (pro.isAnnotationPresent(NoFox.class))
				continue;
			if (!(pro.isAnnotationPresent(DefaultKey.class) || pro.isAnnotationPresent(NamedDefaultKey.class)
					|| hasAnnotations(pro))) {
				status.update(Status.WARNING,
						"[" + pro.getSimpleName() + "] missing key annotation. Use @NoFox to silence this warning.");
				continue;
			}
			Constructor<?> cons = findConstructor(pro, product);
			if (cons == null) {
				status.update(Status.WARNING, "Skipping [" + pro.getSimpleName()
						+ "]. No suitable constructor found. Use the @NoFox to silence this warning.");
				continue;
			}
			if (pro.isAnnotationPresent(DefaultKey.class))
				setProduct(null, new ProductInfo<>(pro, null, cons));
			if (name != null) {
				NamedDefaultKey[] keys = pro.getAnnotationsByType(NamedDefaultKey.class);
				for (NamedDefaultKey key : keys)
					if (key.name().equals(name))
						setProduct(null, new ProductInfo<>(pro, key.name(), cons));
			}
			parseAnnotations(pro, cons);
		}
	}

	protected abstract <T extends Product> void parseAnnotations(Class<T> pro, Constructor<?> cons);

	@SuppressWarnings("unchecked")
	private final <T extends Product> Constructor<T> findConstructor(Class<T> pro, Class<Product> product) {
		Constructor<T> cons = null;
		Class<?>[] argTypes = product.getAnnotation(Fox.class).value();
		for (Constructor<?> c : pro.getDeclaredConstructors())
			if (checkConstructor(c, argTypes))
				if (cons == null)
					cons = (Constructor<T>) c;
				else {
					status.update(Status.WARNING,
							"Skipping [" + pro.getSimpleName() + "]. Ambiguous Constructor " + cons + " and " + c);
					return null;
				}
		return cons;
	}

	/**
	 * Returns whether this constructor satisfies the specified parameter
	 * requirements set by the @Fox annotation. Potential classes should only
	 * have one valid public constructor that satisfies this requirement.
	 * 
	 * @param cons
	 *            the potential constructor.
	 * @param argTypes
	 *            the types of the constructor parameters.
	 * @return Whether the constructor can accept the argTypes.
	 */
	protected boolean checkConstructor(Constructor<?> cons, Class<?>[] argTypes) {
		Class<?>[] types = cons.getParameterTypes();
		if (types.length != argTypes.length)
			return false;
		for (int i = 0; i < types.length; i++)
			if (!argTypes[i].isAssignableFrom(types[i]))
				return false;
		return true;
	}

	/**
	 * Tests whether the potential product has the required annotation for this
	 * factory.
	 * 
	 * @param product
	 *            the potential product
	 * @return true if the product has the proper annotations.
	 */
	protected abstract <T extends Product> boolean hasAnnotations(Class<T> product);

	protected <T extends Product> void setProduct(Key key, ProductInfo<T> product) {
		if (products.containsKey(key)) {
			ProductInfo<? extends Product> old = products.get(key);
			if (old.name == null) {
				if (product.name == null)
					status.update(Status.WARNING,
							"Product [" + old.type.getSimpleName() + "] already exists for "
									+ (key != null ? key : "default") + ". Overwriting with ["
									+ product.type.getSimpleName() + "].");
			} else {
				if (product.name == null)
					return;
				else
					status.update(Status.WARNING,
							"Product [" + old.type.getSimpleName() + "] already exists for " + product.name + "."
									+ (key != null ? key : "default") + ". Overwriting with ["
									+ product.type.getSimpleName() + "].");
			}
		}
		products.put(key, product);
	}

	protected ProductInfo<? extends Product> getProduct(Key key) {
		ProductInfo<? extends Product> info = products.get(key);
		return info != null ? info : products.get(null);
	}

	@SuppressWarnings("unchecked")
	public Product produce(Key key, Object... args) {
		ProductInfo<? extends Product> info = getProduct(key);
		if (info == null)
			return null;
		try {
			return (Product) info.constructor.newInstance(args);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException("Product constructor inaccessible. Check security permissions.", e);
		}
		catch (InstantiationException e) {
			throw new RuntimeException("Product constructor failed. Has class file changed?", e);
		}
		catch (IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

}
