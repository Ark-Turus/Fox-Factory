package ninja.invisiblecode.foxfactory;

import java.util.Collection;
import java.util.function.Supplier;

import ninja.invisiblecode.commons.Status;
import ninja.invisiblecode.foxfactory.key.StringKey;

public final class StringEnumFoxFactory<EnumType extends Enum<EnumType>, Product>
		extends EnumFoxFactory<EnumType, Product> {

	public StringEnumFoxFactory(final Class<EnumType> type, final Class<Product> product,
			final Supplier<Collection<Class<? extends Product>>> source) {
		super(type, product, source);
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
		for (StringKey key : keys) {
			EnumType e = getEnum(key.value(), product.type);
			if (e != null)
				setProduct(e, product);
		}
	}

	protected EnumType getEnum(final String key, final Class<? extends Product> product) {
		try {
			return Enum.valueOf(type, key);
		}
		catch (IllegalArgumentException e) {
			status.update(Status.ERROR, "[" + product.getName() + "] has invalid annotation. No value "
					+ type.getSimpleName() + "." + key + " found. Skipping Annotation.");
			return null;
		}
	}

}
