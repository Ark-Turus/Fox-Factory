package ninja.invisiblecode.foxfactory;

import java.util.Collection;
import java.util.function.Supplier;

import ninja.invisiblecode.foxfactory.key.ClassKey;

/**
 * Fox Factory that creates associated products based on a class key.
 *
 * @param <Product>
 */

public final class ClassFoxFactory<Product> extends FoxFactory<Class<?>, Product> {

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
	 * @return
	 */
	private ClassFoxFactory(final Class<Product> product, final Supplier<Collection<Class<? extends Product>>> source) {
		super(product, source);
	}

	@Override
	protected <T extends Product> boolean hasAnnotations(Class<T> product) {
		if (product.isAnnotationPresent(ClassKey.class))
			return true;
		return false;
	}

	@Override
	protected void parseAnnotations(ProductInfo<? extends Product> product) {
		ClassKey[] keys = product.type.getAnnotationsByType(ClassKey.class);
		for (ClassKey key : keys)
			setProduct(key.value(), product);
	}

}
