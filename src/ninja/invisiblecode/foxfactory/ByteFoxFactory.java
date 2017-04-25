package ninja.invisiblecode.foxfactory;

import java.util.Collection;
import java.util.function.Supplier;

import ninja.invisiblecode.commons.Status;
import ninja.invisiblecode.foxfactory.key.ByteKey;
import ninja.invisiblecode.foxfactory.key.ByteRangeKey;

public final class ByteFoxFactory<Product> extends FoxFactory<Byte, Product> {

	public ByteFoxFactory(final Class<Product> product, final Supplier<Collection<Class<? extends Product>>> source) {
		super(product, source);
	}

	@Override
	protected <T extends Product> boolean hasAnnotations(Class<T> product) {
		if (product.isAnnotationPresent(ByteKey.class))
			return true;
		if (product.isAnnotationPresent(ByteRangeKey.class))
			return true;
		return false;
	}

	@Override
	protected void parseAnnotations(ProductInfo<? extends Product> product) {
		ByteKey[] keys = product.type.getAnnotationsByType(ByteKey.class);
		for (ByteKey key : keys)
			setProduct(key.value(), product);
		ByteRangeKey[] rkeys = product.type.getAnnotationsByType(ByteRangeKey.class);
		for (ByteRangeKey key : rkeys) {
			if (key.min() >= key.max())
				status.update(Status.WARNING, "[" + product.type.getName()
						+ "] has invalid key range annotation. min must be less than max. Skipping Annotation.");
			else
				for (byte i = key.min(); i <= key.max(); i++)
					setProduct(i, product);
		}
	}

}
