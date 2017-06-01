package ninja.invisiblecode.foxfactory;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.function.Supplier;

import ninja.invisiblecode.commons.Status;
import ninja.invisiblecode.foxfactory.key.NamedShortKey;
import ninja.invisiblecode.foxfactory.key.NamedShortRangeKey;
import ninja.invisiblecode.foxfactory.key.ShortKey;
import ninja.invisiblecode.foxfactory.key.ShortRangeKey;

public final class ShortFoxFactory<Product> extends FoxFactory<Short, Product> {

	public ShortFoxFactory(final Class<Product> product, final Supplier<Collection<Class<? extends Product>>> source) {
		super(product, null, source);
	}

	public ShortFoxFactory(final Class<Product> product, String name,
			final Supplier<Collection<Class<? extends Product>>> source) {
		super(product, name, source);
	}

	@Override
	protected <T extends Product> boolean hasAnnotations(Class<T> product) {
		if (product.isAnnotationPresent(ShortKey.class))
			return true;
		if (product.isAnnotationPresent(ShortRangeKey.class))
			return true;
		if (product.isAnnotationPresent(NamedShortKey.class))
			return true;
		if (product.isAnnotationPresent(NamedShortRangeKey.class))
			return true;
		return false;
	}

	@Override
	protected <T extends Product> void parseAnnotations(Class<T> type, Constructor<?> cons) {
		{
			ShortKey[] keys = type.getAnnotationsByType(ShortKey.class);
			for (ShortKey key : keys)
				setProduct(key.value(), new ProductInfo<>(type, cons));
			ShortRangeKey[] rkeys = type.getAnnotationsByType(ShortRangeKey.class);
			for (ShortRangeKey key : rkeys) {
				if (key.min() >= key.max())
					status.update(Status.WARNING, "[" + type.getName()
							+ "] has invalid key range annotation. min must be less than max. Skipping Annotation.");
				else
					for (short i = key.min(); i <= key.max(); i++)
						setProduct(i, new ProductInfo<>(type, cons));
			}
		}
		if (name == null) {
			NamedShortKey[] keys = type.getAnnotationsByType(NamedShortKey.class);
			for (NamedShortKey key : keys)
				if (key.name().equals(name))
					setProduct(key.value(), new ProductInfo<>(type, key.name(), cons));
			NamedShortRangeKey[] rkeys = type.getAnnotationsByType(NamedShortRangeKey.class);
			for (NamedShortRangeKey key : rkeys) {
				if (key.name().equals(name))
					if (key.min() >= key.max())
						status.update(Status.WARNING, "[" + type.getName()
								+ "] has invalid key range annotation. min must be less than max. Skipping Annotation.");
					else
						for (short i = key.min(); i <= key.max(); i++)
							setProduct(i, new ProductInfo<>(type, key.name(), cons));
			}
		}
	}

}
