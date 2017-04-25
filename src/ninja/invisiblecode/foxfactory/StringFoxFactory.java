package ninja.invisiblecode.foxfactory;

import java.util.Collection;
import java.util.function.Supplier;

import ninja.invisiblecode.foxfactory.key.StringKey;

public final class StringFoxFactory<Product> extends FoxFactory<String, Product> {

	public StringFoxFactory(final Class<Product> product, final Supplier<Collection<Class<? extends Product>>> source) {
		super(product, source);
	}

	@Override
	protected <T extends Product> boolean hasAnnotations(Class<T> product) {
		if (product.isAnnotationPresent(StringKey.class))
			return true;
		return false;
	}

	@Override
	protected void parseAnnotations(ProductInfo<? extends Product> product) {
		StringKey[] keys = product.type.getAnnotationsByType(StringKey.class);
		for (StringKey key : keys)
			setProduct(key.value(), product);
	}

}
