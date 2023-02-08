package io.github.aecsocket.jniglue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates that a native {@code jclass} field will be created for this class, with the field name
 * {@code jni_[Java class name]}, which allows using {@link JniCallback}.
 * <b>NOTE:</b> name collisions are not handled yet!
 * <p>
 * Generated C++ code:
 * <code>
 *     jclass jni_[Java class name];
 * </code>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface JniReferenced {}
