package ninja.invisiblecode.foxfactory;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.function.Supplier;

import ninja.invisiblecode.commons.Status;
import ninja.invisiblecode.foxfactory.key.LongKey;
import ninja.invisiblecode.foxfactory.key.LongRangeKey;
import ninja.invisiblecode.foxfactory.key.NamedLongKey;
import ninja.invisiblecode.foxfactory.key.NamedLongRangeKey;

public final class LongFoxFactory<Product> extends FoxFactory<Long, Product> {

	public LongFoxFactory(final Class<Product> product, final Supplier<Collection<Class<? extends Product>>> source) {
		super(product, null, source);
	}

	public LongFoxFactory(final Class<Product> product, String name,
			final Supplier<Collection<Class<? extends Product>>> source) {
		super(product, name, source);
	}

	@Override
	protected <T extends Product> boolean hasAnnotations(Class<T> product) {
		if (product.isAnnotationPresent(LongKey.class))
			return true;
		if (product.isAnnotationPresent(LongRangeKey.class))
			return true;
		if (product.isAnnotationPresent(NamedLongKey.class))
			return true;
		if (product.isAnnotationPresent(NamedLongRangeKey.class))
			return true;
		return false;
	}

	@Override
	protected <T extends Product> void parseAnnotations(Class<T> type, Constructor<?> cons) {
		{
			LongKey[] keys = type.getAnnotationsByType(LongKey.class);
			for (LongKey key : keys)
				setProduct(key.value(), new ProductInfo<>(type, cons));
			LongRangeKey[] rkeys = type.getAnnotationsByType(LongRangeKey.class);
			for (LongRangeKey key : rkeys) {
				if (key.min() >= key.max())
					status.update(Status.WARNING, "[" + type.getName()
							+ "] has invalid key range annotation. min must be less than max. Skipping Annotation.");
				else
					for (long i = key.min(); i <= key.max(); i++)
						setProduct(i, new ProductInfo<>(type, cons));
			}
		}
		if (name != null) {
			NamedLongKey[] keys = type.getAnnotationsByType(NamedLongKey.class);
			for (NamedLongKey key : keys)
				if (key.name().equals(name))
					setProduct(key.value(), new ProductInfo<>(type, key.name(), cons));
			NamedLongRangeKey[] rkeys = type.getAnnotationsByType(NamedLongRangeKey.class);
			for (NamedLongRangeKey key : rkeys) {
				if (key.name().equals(name))
					if (key.min() >= key.max())
						status.update(Status.WARNING, "[" + type.getName()
								+ "] has invalid key range annotation. min must be less than max. Skipping Annotation.");
					else
						for (long i = key.min(); i <= key.max(); i++)
							setProduct(i, new ProductInfo<>(type, key.name(), cons));
			}
		}
	}

}
