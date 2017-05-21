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

public abstract class FoxFactory<Key, Product> {

	protected static final Status						status		= new Status.SystemStatus();

	protected Map<Key, ProductInfo<? extends Product>>	products	= new HashMap<>();

	protected <T extends Product> FoxFactory(Class<Product> product, Supplier<Collection<Class<? extends T>>> source) {
		if (!product.isAnnotationPresent(Fox.class))
			throw new MissingAnnotationException("FoxFactory requires");
		for (Class<? extends T> pro : source.get()) {
			if (Modifier.isInterface(pro.getModifiers()))
				continue;
			if (Modifier.isAbstract(pro.getModifiers()))
				continue;
			if (pro.isAnnotationPresent(NoFox.class))
				continue;
			if (!(pro.isAnnotationPresent(DefaultKey.class) || hasAnnotations(pro))) {
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
				setDefaultProduct(new ProductInfo(pro, cons));
			else
				parseAnnotations(new ProductInfo(pro, cons));
		}
	}

	protected abstract void parseAnnotations(ProductInfo<? extends Product> productInfo);

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

	protected void setDefaultProduct(ProductInfo<? extends Product> product) {
		if (products.containsKey(null))
			status.update(Status.WARNING, "Default product " + products.get(null).type.getSimpleName()
					+ " already exists. Overwriting with " + product.type.getSimpleName());
		products.put(null, product);
	}

	protected void setProduct(Key key, ProductInfo<? extends Product> product) {
		if (products.containsKey(key))
			status.update(Status.WARNING, "Product " + products.get(key).type.getSimpleName()
					+ " already exists for key " + key + ". Overwriting with " + product.type.getSimpleName());
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
