package ninja.invisiblecode.foxfactory.util;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.reflections.Reflections;

public final class FoxUtil {

	public static <T> Supplier<Collection<Class<? extends T>>> loadAllSubtypes(Class<T> product) {
		List<Class<? extends T>> collect = new Reflections().getSubTypesOf(product).stream()
				.filter(type -> !type.isInterface() && !Modifier.isAbstract(type.getModifiers()))
				.collect(Collectors.toList());
		return () -> collect;
	}

	public static <T> Supplier<Collection<Class<? extends T>>> loadAllSubtypes(Class<T> product,
			ClassLoader... classloaders) {
		List<Class<? extends T>> collect = new Reflections((Object[]) classloaders).getSubTypesOf(product).stream()
				.filter(type -> !type.isInterface() && !Modifier.isAbstract(type.getModifiers()))
				.collect(Collectors.toList());
		return () -> collect;
	}

}
