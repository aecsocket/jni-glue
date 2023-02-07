package io.github.aecsocket.jniglue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates that a native {@code jmethodID} field will be created for this method, with the field name
 * {@code [Java class name]_[Java method name]}.
 * <b>NOTE:</b> name collisions are not handled yet!
 * <p>
 * Generated C++ code:
 * <code>
 *     jmethodID [Java class name]_[Java method name];
 * </code>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface JniCallback {}
