package ninja.invisiblecode.foxfactory;

import java.util.Collection;
import java.util.function.Supplier;

import ninja.invisiblecode.commons.Status;
import ninja.invisiblecode.foxfactory.key.IntKey;
import ninja.invisiblecode.foxfactory.key.IntRangeKey;

public final class IntEnumFoxFactory<EnumType extends Enum<EnumType>, Product>
		extends EnumFoxFactory<EnumType, Product> {

	public IntEnumFoxFactory(final Class<EnumType> keyType, final Class<Product> product,
			final Supplier<Collection<Class<? extends Product>>> source) {
		super(keyType, product, source);
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
		for (IntKey key : keys) {
			EnumType e = getEnum(key.value(), product.type);
			if (e != null)
				setProduct(e, product);
		}
		IntRangeKey[] rkeys = product.type.getAnnotationsByType(IntRangeKey.class);
		for (IntRangeKey key : rkeys) {
			if (key.min() >= key.max())
				status.update(Status.WARNING, "[" + product.type.getName()
						+ "] has invalid key range annotation. min must be less than max. Skipping Annotation.");
			else
				for (int i = key.min(); i <= key.max(); i++) {
					EnumType e = getEnum(i, product.type);
					if (e != null)
						setProduct(e, product);
				}
		}
	}

	protected EnumType getEnum(int value, Class<? extends Product> product) {
		try {
			return type.getEnumConstants()[value];
		}
		catch (ArrayIndexOutOfBoundsException e) {
			status.update(Status.ERROR, "[" + product.getName() + "] has invalid annotation. No value "
					+ type.getSimpleName() + ".values()[" + value + "] found. Skipping Annotation.");
			return null;
		}
	}

}
