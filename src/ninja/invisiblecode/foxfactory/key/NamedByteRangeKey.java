
package ninja.invisiblecode.foxfactory.key;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(NamedByteRangeKeys.class)
public @interface NamedByteRangeKey {
	byte min();

	byte max();

	String name() default "";
}
