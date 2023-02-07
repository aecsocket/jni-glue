package io.github.aecsocket.jniglue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds {@code #include} directives to the top of the generated native file.
 * If the include name is wrapped in {@code <angled brackets>}, the native name will also be wrapped in brackets.
 * If the include name is not wrapped, the native name will be wrapped in {@code "quotes"}.
 * <p>
 * Multiple lines can be included to specify multiple imports.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface JniInclude {
    /**
     * The imports to add, one per line.
     */
    String value();
}
