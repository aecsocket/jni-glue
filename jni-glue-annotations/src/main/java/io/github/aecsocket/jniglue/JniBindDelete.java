package io.github.aecsocket.jniglue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <b>Requires class annotated with {@link JniTypeMapping}:</b> annotates that a method maps to a C++ {@code delete} call.
 * The method must have arguments {@code long _a}, with {@code _a} being the address of the object to delete.
 * <p>
 * Generated C++ code:
 * <code>
 *     delete ([JNI type name]*) _a;
 * </code>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface JniBindDelete {}
