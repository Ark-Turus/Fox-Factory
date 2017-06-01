package ninja.invisiblecode.foxfactory;

import java.lang.reflect.Constructor;

import ninja.invisiblecode.commons.Status;

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

	private static <T> Constructor<?> getPolymorphicConstructor(Class<T> type, Class<?>[] consArgs) {
		Constructor<?> res = null;
		next: //
		for (Constructor<?> cons : type.getConstructors()) {
			Class<?>[] args = cons.getParameterTypes();
			if (args.length != consArgs.length)
				continue next;
			for (int i = 0; i < args.length; i++) {
				if (!consArgs[i].isAssignableFrom(args[i]))
					continue next;
			}
			if (res != null) {
				FoxFactory.status.update(Status.WARNING,
						"Multiple constructors for [" + type.getName() + "] satisfy polymorphic signature of public "
								+ signature(type, consArgs) + ". Skipping product.");
				return null;
			}
			res = cons;
		}
		if (res != null) {
			FoxFactory.status.update(Status.INFO, "Using constructor for [" + type.getName()
					+ "] with signature of public " + signature(type, res.getParameterTypes()) + ".");
		}
		return res;
	}

	private static String signature(Class<?> type, Class<?>[] args) {
		StringBuilder signature = new StringBuilder(type.getSimpleName());
		signature.append('(');
		if (args.length > 0) {
			signature.append(args[0].getSimpleName());
			for (int i = 1; i < args.length; i++) {
				signature.append(", ");
				signature.append(args[i].getSimpleName());
			}
		}
		signature.append(')');
		return signature.toString();
	}

}
