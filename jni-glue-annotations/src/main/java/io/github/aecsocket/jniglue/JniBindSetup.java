package io.github.aecsocket.jniglue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates that a method maps to the internal native setup function call.
 * The main "environment initializer" class should typically hold a static method with this annotation, so the
 * bindings can be set up.
 * <p>
 * Generated C++ code:
 * <code>
 *     JNIInit(env);
 * </code>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface JniBindSetup {}
