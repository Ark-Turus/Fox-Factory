package ninja.invisiblecode.foxfactory;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.function.Supplier;

import ninja.invisiblecode.commons.Status;
import ninja.invisiblecode.foxfactory.key.IntKey;
import ninja.invisiblecode.foxfactory.key.IntRangeKey;
import ninja.invisiblecode.foxfactory.key.NamedIntKey;
import ninja.invisiblecode.foxfactory.key.NamedIntRangeKey;
import ninja.invisiblecode.foxfactory.key.NamedStringKey;
import ninja.invisiblecode.foxfactory.key.StringKey;

public class EnumFoxFactory<EnumType extends Enum<EnumType>, Product> extends FoxFactory<EnumType, Product> {

	protected final Class<EnumType> type;

	public EnumFoxFactory(Class<EnumType> type, Class<Product> product,
			Supplier<Collection<Class<? extends Product>>> source) {
		super(product, null, source);
		this.type = type;
	}

	public EnumFoxFactory(Class<EnumType> type, Class<Product> product, String name,
			Supplier<Collection<Class<? extends Product>>> source) {
		super(product, name, source);
		this.type = type;
	}

	@Override
	protected <T extends Product> boolean hasAnnotations(Class<T> product) {
		if (product.isAnnotationPresent(IntKey.class))
			return true;
		if (product.isAnnotationPresent(IntRangeKey.class))
			return true;
		if (product.isAnnotationPresent(StringKey.class))
			return true;
		if (product.isAnnotationPresent(NamedIntKey.class))
			return true;
		if (product.isAnnotationPresent(NamedIntRangeKey.class))
			return true;
		if (product.isAnnotationPresent(NamedStringKey.class))
			return true;
		return false;
	}

	@Override
	protected <T extends Product> void parseAnnotations(Class<T> type, Constructor<?> cons) {
		{
			IntKey[] keys = type.getAnnotationsByType(IntKey.class);
			for (IntKey key : keys) {
				EnumType e = getEnum(key.value(), type);
				if (e != null)
					setProduct(e, new ProductInfo<>(type, cons));
			}
			StringKey[] skeys = type.getAnnotationsByType(StringKey.class);
			for (StringKey key : skeys) {
				EnumType e = getEnum(key.value(), type);
				if (e != null)
					setProduct(e, new ProductInfo<>(type, cons));
			}
			IntRangeKey[] rkeys = type.getAnnotationsByType(IntRangeKey.class);
			for (IntRangeKey key : rkeys) {
				if (key.min() >= key.max())
					status.update(Status.WARNING, "[" + type.getName()
							+ "] has invalid key range annotation. min must be less than max. Skipping Annotation.");
				else
					for (int i = key.min(); i <= key.max(); i++) {
						EnumType e = getEnum(i, type);
						if (e != null)
							setProduct(e, new ProductInfo<>(type, cons));
					}
			}
		}
		{
			NamedIntKey[] keys = type.getAnnotationsByType(NamedIntKey.class);
			for (NamedIntKey key : keys) {
				EnumType e = getEnum(key.value(), type);
				if (e != null)
					setProduct(e, new ProductInfo<>(type, key.name(), cons));
			}
			NamedStringKey[] skeys = type.getAnnotationsByType(NamedStringKey.class);
			for (NamedStringKey key : skeys) {
				EnumType e = getEnum(key.value(), type);
				if (e != null)
					setProduct(e, new ProductInfo<>(type, key.name(), cons));
			}
			NamedIntRangeKey[] rkeys = type.getAnnotationsByType(NamedIntRangeKey.class);
			for (NamedIntRangeKey key : rkeys) {
				if (key.min() >= key.max())
					status.update(Status.WARNING, "[" + type.getName()
							+ "] has invalid key range annotation. min must be less than max. Skipping Annotation.");
				else
					for (int i = key.min(); i <= key.max(); i++) {
						EnumType e = getEnum(i, type);
						if (e != null)
							setProduct(e, new ProductInfo<>(type, key.name(), cons));
					}
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
