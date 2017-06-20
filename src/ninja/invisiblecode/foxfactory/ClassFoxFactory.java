package ninja.invisiblecode.foxfactory;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import ninja.invisiblecode.commons.Status;
import ninja.invisiblecode.foxfactory.key.ClassArrayKey;
import ninja.invisiblecode.foxfactory.key.ClassKey;
import ninja.invisiblecode.foxfactory.key.DefaultClassArrayKey;
import ninja.invisiblecode.foxfactory.key.NamedClassArrayKey;
import ninja.invisiblecode.foxfactory.key.NamedClassKey;
import ninja.invisiblecode.foxfactory.key.NamedDefaultClassArrayKey;

/**
 * Fox Factory that creates associated products based on a class key.
 *
 * @param <Product>
 */

public final class ClassFoxFactory<Product> extends FoxFactory<Class<?>, Product> {

	private boolean											superclasses;
	private Map<Integer, ProductInfo<? extends Product>>	defaultProducts	= new HashMap<>();

	/**
	 * 
	 * @param product
	 *            the type of the product produced.
	 * @param source
	 *            A supplier providing a set of classes to scan for annotations.
	 *            For convenience you can use FoxFactoryUtil to get a Supplier
	 *            for commonly used criteria.
	 * @param superclasses
	 *            If there is no valid product for the provided key Class, will
	 *            try to produce a product using the key'ssuperclass.
	 */

	public ClassFoxFactory(final Class<Product> product, final Supplier<Collection<Class<? extends Product>>> source,
			boolean superclasses) {
		this(product, null, source, superclasses);
	}

	public ClassFoxFactory(final Class<Product> product, String name,
			final Supplier<Collection<Class<? extends Product>>> source, boolean superclasses) {
		super(product, name, source);
		this.superclasses = superclasses;
	}

	@Override
	protected <T extends Product> boolean hasAnnotations(Class<T> product) {
		if (product.isAnnotationPresent(ClassKey.class))
			return true;
		if (product.isAnnotationPresent(ClassArrayKey.class))
			return true;
		if (product.isAnnotationPresent(NamedClassKey.class))
			return true;
		if (product.isAnnotationPresent(NamedClassArrayKey.class))
			return true;
		if (product.isAnnotationPresent(DefaultClassArrayKey.class))
			return true;
		if (product.isAnnotationPresent(NamedDefaultClassArrayKey.class))
			return true;
		return false;
	}

	@Override
	protected <T extends Product> void parseAnnotations(Class<T> type, Constructor<?> cons) {
		{
			ClassKey[] keys = type.getAnnotationsByType(ClassKey.class);
			for (ClassKey key : keys)
				setProduct(key.value(), new ProductInfo<>(type, cons));
			ClassArrayKey[] akeys = type.getAnnotationsByType(ClassArrayKey.class);
			for (ClassArrayKey key : akeys) {
				try {
					setProduct(arrayType(key.value(), key.dimensions()), new ProductInfo<>(type, cons));
				}
				catch (ClassNotFoundException e) {
					status.update(Status.ERROR, "Failed to create type " + key.value().getName()
							+ "[]. Skipping product " + type.getSimpleName());
				}
			}
			DefaultClassArrayKey[] dkeys = type.getAnnotationsByType(DefaultClassArrayKey.class);
			for (DefaultClassArrayKey key : dkeys) {
				setDefaultArray(key.dimension(), new ProductInfo<>(type, cons));
			}
		}
		if (name != null) {
			NamedClassKey[] keys = type.getAnnotationsByType(NamedClassKey.class);
			for (NamedClassKey key : keys)
				if (key.name().equals(name))
					setProduct(key.value(), new ProductInfo<>(type, key.name(), cons));
			NamedClassArrayKey[] akeys = type.getAnnotationsByType(NamedClassArrayKey.class);
			for (NamedClassArrayKey key : akeys)
				try {
					setProduct(arrayType(key.value(), key.dimensions()), new ProductInfo<>(type, key.name(), cons));
				}
				catch (ClassNotFoundException e) {
					status.update(Status.ERROR, "Failed to create type " + key.value().getName()
							+ "[]. Skipping product " + type.getSimpleName());
				}
			NamedDefaultClassArrayKey[] dkeys = type.getAnnotationsByType(NamedDefaultClassArrayKey.class);
			for (NamedDefaultClassArrayKey key : dkeys) {
				setDefaultArray(key.dimension(), new ProductInfo<>(type, cons));
			}
		}
	}

	@Override
	protected ProductInfo<? extends Product> getProduct(Class<?> key) {
		if (!superclasses)
			return super.getProduct(key);
		if (key.isArray()) {
			Class<?> component = key;
			int dimension = 0;
			do {
				dimension++;
				key = key.getComponentType();
			} while (component.isArray());
			return getArray(component, dimension);
		}
		return getSimple(key);
	}

	private ProductInfo<? extends Product> getSimple(Class<?> key) {
		ProductInfo<? extends Product> info = super.getProduct(key);
		if (info != null)
			return info;
		while ((key = key.getSuperclass()) != null) {
			info = super.getProduct(key);
			if (info != null)
				return info;
		}
		return null;
	}

	private ProductInfo<? extends Product> getArray(Class<?> key, int dimension) {
		if (dimension < 1)
			throw new IllegalArgumentException("Arrays must have a positive dimension.");
		if (dimension > 255)
			throw new IllegalArgumentException("Arrays can have a maximum dimension of 255.");
		try {
			ProductInfo<? extends Product> info = super.getProduct(arrayType(key, dimension));
			if (info != null)
				return info;
			while ((key = key.getSuperclass()) != null) {
				info = super.getProduct(arrayType(key, dimension));
				if (info != null)
					return info;
			}
		}
		catch (ClassNotFoundException e) {
			status.update(Status.ERROR, "Type " + key.getName() + "[] not found.");
		}
		return null;
	}

	private Class<?> arrayType(Class<?> type, int dimension) throws ClassNotFoundException {
		StringBuilder array = new StringBuilder();
		for (int i = 0; i < dimension; i++)
			array.append('[');
		array.append(typeSignature(type));
		return Class.forName(array.toString());
	}

	private String typeSignature(Class<?> type) throws ClassNotFoundException {
		if (type.isPrimitive()) {
			if (type == boolean.class)
				return "Z";
			if (type == byte.class)
				return "B";
			if (type == char.class)
				return "C";
			if (type == short.class)
				return "S";
			if (type == int.class)
				return "I";
			if (type == long.class)
				return "J";
			if (type == float.class)
				return "F";
			if (type == double.class)
				return "D";
			throw new IllegalArgumentException(
					"Invalid array type. Cannot have an array of " + type.getSimpleName() + ".");
		} else
			return "L" + type.getName() + ";";
	}

	private <T extends Product> void setDefaultArray(int dimension, ProductInfo<T> product) {
		ProductInfo<? extends Product> old = defaultProducts.get(dimension);
		if (old == null) {
		} else if (old.name == null) {
			if (product.name == null)
				status.update(Status.WARNING,
						"Product [" + old.type.getSimpleName()
								+ "] already exists for default array. Overwriting with ["
								+ product.type.getSimpleName() + "].");
		} else {
			if (product.name == null)
				return;
			else
				status.update(Status.WARNING,
						"Product [" + old.type.getSimpleName() + "] already exists for " + product.name
								+ " default array of dimension " + dimension + ". Overwriting with ["
								+ product.type.getSimpleName() + "].");
		}
		defaultProducts.put(dimension, product);
	}

}
