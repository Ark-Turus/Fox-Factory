package ninja.invisiblecode.foxfactory;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.function.Supplier;

import ninja.invisiblecode.commons.Status;
import ninja.invisiblecode.foxfactory.key.ByteKey;
import ninja.invisiblecode.foxfactory.key.ByteRangeKey;
import ninja.invisiblecode.foxfactory.key.NamedByteKey;
import ninja.invisiblecode.foxfactory.key.NamedByteRangeKey;

public final class ByteFoxFactory<Product> extends FoxFactory<Byte, Product> {

	public ByteFoxFactory(final Class<Product> product, final Supplier<Collection<Class<? extends Product>>> source) {
		super(product, null, source);
	}

	public ByteFoxFactory(final Class<Product> product, String name,
			final Supplier<Collection<Class<? extends Product>>> source) {
		super(product, name, source);
	}

	@Override
	protected <T extends Product> boolean hasAnnotations(Class<T> product) {
		if (product.isAnnotationPresent(ByteKey.class))
			return true;
		if (product.isAnnotationPresent(ByteRangeKey.class))
			return true;
		if (product.isAnnotationPresent(NamedByteKey.class))
			return true;
		if (product.isAnnotationPresent(NamedByteRangeKey.class))
			return true;
		return false;
	}

	@Override
	protected <T extends Product> void parseAnnotations(Class<T> type, Constructor<?> cons) {
		{
			ByteKey[] keys = type.getAnnotationsByType(ByteKey.class);
			for (ByteKey key : keys)
				setProduct(key.value(), new ProductInfo<>(type, cons));
			ByteRangeKey[] rkeys = type.getAnnotationsByType(ByteRangeKey.class);
			for (ByteRangeKey key : rkeys) {
				if (key.min() >= key.max())
					status.update(Status.WARNING, "[" + type.getName()
							+ "] has invalid key range annotation. min must be less than max. Skipping Annotation.");
				else
					for (byte i = key.min(); i <= key.max(); i++)
						setProduct(i, new ProductInfo<>(type, cons));
			}
		}
		if (name == null) {
			NamedByteKey[] keys = type.getAnnotationsByType(NamedByteKey.class);
			for (NamedByteKey key : keys)
				if (key.name().equals(name))
					setProduct(key.value(), new ProductInfo<>(type, key.name(), cons));
			NamedByteRangeKey[] rkeys = type.getAnnotationsByType(NamedByteRangeKey.class);
			for (NamedByteRangeKey key : rkeys) {
				if (key.name().equals(name))
					if (key.min() >= key.max())
						status.update(Status.WARNING, "[" + type.getName()
								+ "] has invalid key range annotation. min must be less than max. Skipping Annotation.");
					else
						for (byte i = key.min(); i <= key.max(); i++)
							setProduct(i, new ProductInfo<>(type, key.name(), cons));
			}
		}
	}

}
