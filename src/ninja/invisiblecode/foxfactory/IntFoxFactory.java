package ninja.invisiblecode.foxfactory;

import java.util.Collection;
import java.util.function.Supplier;

import ninja.invisiblecode.commons.Status;
import ninja.invisiblecode.foxfactory.key.IntKey;
import ninja.invisiblecode.foxfactory.key.IntRangeKey;

public final class IntFoxFactory<Product> extends FoxFactory<Integer, Product> {

	public IntFoxFactory(final Class<Product> type, final Supplier<Collection<Class<? extends Product>>> source) {
		super(type, source);
	}

	@Override
	protected <T extends Product> boolean hasAnnotations(Class<T> product) {
		if (product.isAnnotationPresent(IntKey.class))
			return true;
		if (product.isAnnotationPresent(IntRangeKey.class))
			return true;
		return false;
	}

	@Override
	protected void parseAnnotations(ProductInfo<? extends Product> product) {
		IntKey[] keys = product.type.getAnnotationsByType(IntKey.class);
		for (IntKey key : keys)
			setProduct(key.value(), product);
		IntRangeKey[] rkeys = product.type.getAnnotationsByType(IntRangeKey.class);
		for (IntRangeKey key : rkeys) {
			if (key.min() >= key.max())
				status.update(Status.WARNING, "[" + product.type.getName()
						+ "] has invalid key range annotation. min must be less than max. Skipping Annotation.");
			else
				for (int i = key.min(); i <= key.max(); i++)
					setProduct(i, product);
		}
	}

}
