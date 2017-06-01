package ninja.invisiblecode.foxfactory;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.function.Supplier;

import ninja.invisiblecode.commons.Status;
import ninja.invisiblecode.foxfactory.key.IntKey;
import ninja.invisiblecode.foxfactory.key.IntRangeKey;
import ninja.invisiblecode.foxfactory.key.NamedIntKey;
import ninja.invisiblecode.foxfactory.key.NamedIntRangeKey;

public final class IntFoxFactory<Product> extends FoxFactory<Integer, Product> {

	public IntFoxFactory(final Class<Product> type, final Supplier<Collection<Class<? extends Product>>> source) {
		super(type, null, source);
	}

	public IntFoxFactory(final Class<Product> type, String name,
			final Supplier<Collection<Class<? extends Product>>> source) {
		super(type, name, source);
	}

	@Override
	protected <T extends Product> boolean hasAnnotations(Class<T> product) {
		if (product.isAnnotationPresent(IntKey.class))
			return true;
		if (product.isAnnotationPresent(IntRangeKey.class))
			return true;
		if (product.isAnnotationPresent(NamedIntKey.class))
			return true;
		if (product.isAnnotationPresent(NamedIntRangeKey.class))
			return true;
		return false;
	}

	@Override
	protected <T extends Product> void parseAnnotations(Class<T> type, Constructor<?> cons) {
		{
			IntKey[] keys = type.getAnnotationsByType(IntKey.class);
			for (IntKey key : keys)
				setProduct(key.value(), new ProductInfo<>(type, cons));
			IntRangeKey[] rkeys = type.getAnnotationsByType(IntRangeKey.class);
			for (IntRangeKey key : rkeys) {
				if (key.min() >= key.max())
					status.update(Status.WARNING, "[" + type.getName()
							+ "] has invalid key range annotation. min must be less than max. Skipping Annotation.");
				else
					for (int i = key.min(); i <= key.max(); i++)
						setProduct(i, new ProductInfo<>(type, cons));
			}
		}
		if (name != null) {
			NamedIntKey[] keys = type.getAnnotationsByType(NamedIntKey.class);
			for (NamedIntKey key : keys)
				if (key.name().equals(name))
					setProduct(key.value(), new ProductInfo<>(type, key.name(), cons));
			NamedIntRangeKey[] rkeys = type.getAnnotationsByType(NamedIntRangeKey.class);
			for (NamedIntRangeKey key : rkeys) {
				if (key.name().equals(name))
					if (key.min() >= key.max())
						status.update(Status.WARNING, "[" + type.getName()
								+ "] has invalid key range annotation. min must be less than max. Skipping Annotation.");
					else
						for (int i = key.min(); i <= key.max(); i++)
							setProduct(i, new ProductInfo<>(type, key.name(), cons));
			}
		}
	}

}
