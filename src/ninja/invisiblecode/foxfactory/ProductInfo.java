package ninja.invisiblecode.foxfactory;

import java.lang.reflect.Constructor;

public class ProductInfo<Product> {

	public final Class<? extends Product>	type;
	public final Constructor<?>				constructor;
	public final String						name;

	public ProductInfo(Class<? extends Product> type, String name, Constructor<?> constructor) {
		this.type = type;
		this.name = name;
		this.constructor = constructor;
	}

	public ProductInfo(Class<? extends Product> type, Constructor<?> constructor) {
		this(type, null, constructor);
	}

}
