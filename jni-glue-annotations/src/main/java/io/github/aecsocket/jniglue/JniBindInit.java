package io.github.aecsocket.jniglue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates that a method maps to the internal C++ setup function call.
 * A "loader" or "init" class should typically hold a method with this annotation, so the
 * bindings can be initialized.
 * <p>
 * Generated C++ code:
 * <code>
 *     JNIInit(env);
 * </code>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface JniBindInit {}
