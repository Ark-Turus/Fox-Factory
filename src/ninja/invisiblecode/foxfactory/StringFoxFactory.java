package ninja.invisiblecode.foxfactory;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.function.Supplier;

import ninja.invisiblecode.foxfactory.key.NamedStringKey;
import ninja.invisiblecode.foxfactory.key.StringKey;

public final class StringFoxFactory<Product> extends FoxFactory<String, Product> {

	public StringFoxFactory(final Class<Product> product, final Supplier<Collection<Class<? extends Product>>> source) {
		super(product, null, source);
	}

	public StringFoxFactory(final Class<Product> product, String name,
			final Supplier<Collection<Class<? extends Product>>> source) {
		super(product, name, source);
	}

	@Override
	protected <T extends Product> boolean hasAnnotations(Class<T> product) {
		if (product.isAnnotationPresent(StringKey.class))
			return true;
		if (product.isAnnotationPresent(NamedStringKey.class))
			return true;
		return false;
	}

	@Override
	protected <T extends Product> void parseAnnotations(Class<T> type, Constructor<?> cons) {
		{
			StringKey[] keys = type.getAnnotationsByType(StringKey.class);
			for (StringKey key : keys)
				setProduct(key.value(), new ProductInfo<>(type, cons));
		}
		if (name != null) {
			NamedStringKey[] keys = type.getAnnotationsByType(NamedStringKey.class);
			for (NamedStringKey key : keys)
				if (key.name().equals(name))
					setProduct(key.value(), new ProductInfo<>(type, key.name(), cons));
		}
	}

}
