package io.github.aecsocket.jniglue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <b>Requires class annotated with {@link JniTypeMapping}:</b> annotates that a method maps to a native binding call
 * on a native object with a specific memory address. A pointer to an object of type specified by {@link JniTypeMapping} will
 * be generated. The method must have the first argument be {@code long _a}, with {@code _a} being the address of the object.
 * <p>
 * Generated C++ code:
 * <code>
 *     auto* self = ([JNI type name]*) _a;
 *     // method body
 * </code>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface JniBindSelf {
    /**
     * The native code to include.
     */
    String value();
}
