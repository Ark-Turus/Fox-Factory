package ninja.invisiblecode.foxfactory;

import java.util.Collection;
import java.util.function.Supplier;

import ninja.invisiblecode.commons.Status;
import ninja.invisiblecode.foxfactory.key.ShortKey;
import ninja.invisiblecode.foxfactory.key.ShortRangeKey;

public final class ShortFoxFactory<Product> extends FoxFactory<Short, Product> {

	public ShortFoxFactory(final Class<Product> product, final Supplier<Collection<Class<? extends Product>>> source) {
		super(product, source);
	}

	@Override
	protected <T extends Product> boolean hasAnnotations(Class<T> product) {
		if (product.isAnnotationPresent(ShortKey.class))
			return true;
		if (product.isAnnotationPresent(ShortRangeKey.class))
			return true;
		return false;
	}

	@Override
	protected void parseAnnotations(ProductInfo<? extends Product> product) {
		ShortKey[] keys = product.type.getAnnotationsByType(ShortKey.class);
		for (ShortKey key : keys)
			setProduct(key.value(), product);
		ShortRangeKey[] rkeys = product.type.getAnnotationsByType(ShortRangeKey.class);
		for (ShortRangeKey key : rkeys) {
			if (key.min() >= key.max())
				status.update(Status.WARNING, "[" + product.type.getName()
						+ "] has invalid key range annotation. min must be less than max. Skipping Annotation.");
			else
				for (short i = key.min(); i <= key.max(); i++)
					setProduct(i, product);
		}
	}

}
