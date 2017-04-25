package ninja.invisiblecode.foxfactory;

import java.util.Collection;
import java.util.function.Supplier;

import ninja.invisiblecode.commons.Status;
import ninja.invisiblecode.foxfactory.key.LongKey;
import ninja.invisiblecode.foxfactory.key.LongRangeKey;

public final class LongFoxFactory<Product> extends FoxFactory<Long, Product> {

	public LongFoxFactory(final Class<Product> product, final Supplier<Collection<Class<? extends Product>>> source) {
		super(product, source);
	}

	@Override
	protected <T extends Product> boolean hasAnnotations(Class<T> product) {
		if (product.isAnnotationPresent(LongKey.class))
			return true;
		if (product.isAnnotationPresent(LongRangeKey.class))
			return true;
		return false;
	}

	@Override
	protected void parseAnnotations(ProductInfo<? extends Product> product) {
		LongKey[] keys = product.type.getAnnotationsByType(LongKey.class);
		for (LongKey key : keys)
			setProduct(key.value(), product);
		LongRangeKey[] rkeys = product.type.getAnnotationsByType(LongRangeKey.class);
		for (LongRangeKey key : rkeys) {
			if (key.min() >= key.max())
				status.update(Status.WARNING, "[" + product.type.getName()
						+ "] has invalid key range annotation. min must be less than max. Skipping Annotation.");
			else
				for (long i = key.min(); i <= key.max(); i++)
					setProduct(i, product);
		}
	}

}
