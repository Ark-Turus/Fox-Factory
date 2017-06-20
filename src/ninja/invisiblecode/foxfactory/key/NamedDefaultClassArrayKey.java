
package ninja.invisiblecode.foxfactory.key;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(NamedDefaultClassArrayKeys.class)
public @interface NamedDefaultClassArrayKey {
	int dimension() default 1;

	String name() default "";
}
