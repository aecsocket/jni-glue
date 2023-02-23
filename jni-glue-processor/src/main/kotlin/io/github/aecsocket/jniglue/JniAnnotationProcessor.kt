package io.github.aecsocket.jniglue

import java.io.Writer
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror
import javax.tools.StandardLocation
import kotlin.reflect.KClass

private fun Writer.writeLine(str: String = "") = write(str + '\n')

private fun <T> List<List<T>>.flatJoin(separator: List<T> = emptyList()): List<T> {
    val res = mutableListOf<T>()
    forEachIndexed { idx, child ->
        res += child
        if (idx < size - 1)
            res += separator
    }
    return res
}

private val newline = listOf("")

// https://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/design.html
private fun mangleMethodName(name: String) = name
    .replace("_", "_1")
    .replace(";", "_2")
    .replace("[", "_3")

private fun mangleType(type: TypeMirror) = when (type.toString()) {
    "void" -> "V"
    "boolean" -> "Z"
    "char" -> "C"
    "byte" -> "B"
    "short" -> "S"
    "int" -> "I"
    "float" -> "F"
    "long" -> "J"
    "double" -> "D"
    else -> "L${type.toString().replace('.', '/')};"
}

private fun mangleMethod(className: String, methodName: String) =
    "Java_${className.replace('.', '_')}_${mangleMethodName(methodName)}"

private fun TypeMirror.isVoid() = toString() == "void"

private fun TypeMirror.isString() = toString() == "java.lang.String"

private fun TypeMirror.cType() = when (toString()) {
    "void" -> "void"
    "boolean", "byte", "char", "short", "int", "float", "long", "double" -> "j$this"
    "boolean[]" -> "jbooleanArray"
    "byte[]" -> "jbyteArray"
    "char[]" -> "jcharArray"
    "short[]" -> "jshortArray"
    "int[]" -> "jintArray"
    "long[]" -> "jlongArray"
    "float[]" -> "jfloatArray"
    "double[]" -> "jdoubleArray"
    "java.lang.String" -> "jstring"
    else -> "jobject"
}

private fun TypeMirror.jniCallMethod() = when (toString()) {
    "void" -> "Void"
    "boolean" -> "Boolean"
    "char" -> "Char"
    "byte" -> "Byte"
    "short" -> "Short"
    "int" -> "Int"
    "float" -> "Float"
    "long" -> "Long"
    "double" -> "Double"
    else -> "Object"
}

private fun String.classPath() = replace('.', '/')

private fun Element.enclosingPackage(): PackageElement {
    val enclosing = enclosingElement
    return if (enclosing is PackageElement) enclosing else enclosing.enclosingPackage()
}

@SupportedAnnotationTypes("io.github.aecsocket.jniglue.*")
class JniAnnotationProcessor : AbstractProcessor() {
    data class MethodBinding(
        val name: String,
        val params: List<String>,
        val returns: String,
        val body: List<String>
    )

    data class CallbackBinding(
        val methodName: String,
        val isStatic: Boolean,
        val isConstructor: Boolean,
        val params: List<VariableElement>,
        val returns: TypeMirror
    )

    data class CallbackClass(
        val fullClassName: String,
        val simpleClassName: String
    ) {
        val methods = ArrayList<CallbackBinding>()
    }

    class ClassModel(val priority: Int) {
        val includes = ArrayList<List<String>>()
        val callbackClasses = ArrayList<CallbackClass>()
        val headers = ArrayList<List<String>>()
        val onLoad = ArrayList<List<String>>()
        val methodBindings = ArrayList<MethodBinding>()
    }

    class JniModel {
        var jniVersion: String? = null
        val originElements = HashSet<Element>()
        val classes = ArrayList<ClassModel>()
    }

    val models = HashMap<String, JniModel>()
    val errors = ArrayList<String>()

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (roundEnv.processingOver()) {
            if (errors.isNotEmpty()) {
                throw IllegalStateException(
                    "JNI annotation processing failed:\n" +
                    errors.joinToString("\n") { " - $it" }
                )
            }

            models.forEach { (path, model) ->
                val outFile = processingEnv.filer.createResource(
                    StandardLocation.SOURCE_OUTPUT,
                    "",
                    "$path.h",
                    *model.originElements.toTypedArray()
                )

                val sections = ArrayList<List<String>>()

                val classModels = model.classes.sortedBy { it.priority }

                // includes
                val includes = LinkedHashSet(listOf("<jni.h>") +
                    classModels
                        .flatMap { it.includes }
                        .flatten()
                )
                sections += includes.map { include ->
                    if (include.startsWith("<") && include.endsWith(">"))
                        "#include $include"
                    else
                        """#include "$include""""
                }

                // type + callback bindings
                val bindingFields = ArrayList<String>()
                val bindingFunctions = ArrayList<String>()
                val bindingSetup = ArrayList<String>()

                classModels.flatMap { it.callbackClasses }.forEach { callbackClass ->
                    val classFieldName = "jni_${callbackClass.simpleClassName}"
                    bindingFields += "jclass $classFieldName;"
                    bindingSetup += """
                        $classFieldName = env->FindClass("${callbackClass.fullClassName.classPath()}");
                        if (env->ExceptionCheck()) return JNI_ERR;
                    """.trimIndent().lines()

                    callbackClass.methods.forEach { method ->
                        val baseMethodName =
                            if (method.isConstructor) method.methodName
                            else method.methodName.removePrefix("_")
                        val baseName = "${callbackClass.simpleClassName}_$baseMethodName"
                        val methodFieldName = "jni_$baseName"
                        bindingFields += "jmethodID $methodFieldName;"

                        val functionName = "JNI_$baseName"
                        val params = listOfNotNull(
                            "JNIEnv* env",
                            if (method.isStatic) null else "jobject _obj"
                        ) + method.params.map { "${it.asType().cType()} ${it.simpleName}" }
                        val args = method.params.map { it.simpleName }

                        bindingFunctions += listOf(
                            "${method.returns.cType()} $functionName",
                            "  (${params.joinToString()}) {"
                        )
                        bindingFunctions += "    " + if (method.isConstructor) {
                            val newArgs = listOf(classFieldName, methodFieldName) + args
                            "return env->NewObject(${newArgs.joinToString()});"
                        } else {
                            val line = if (method.isStatic) {
                                val newArgs = listOf(
                                    classFieldName,
                                    methodFieldName
                                ) + args
                                "env->CallStatic${method.returns.jniCallMethod()}Method(${newArgs.joinToString()});"
                            } else {
                                val newArgs = listOf(
                                    "_obj",
                                    methodFieldName
                                ) + args
                                "env->Call${method.returns.jniCallMethod()}Method(${newArgs.joinToString()});"
                            }

                            if (method.returns.isVoid()) {
                                line
                            } else {
                                if (method.returns.isString()) "return (jstring) $line"
                                else "return $line"
                            }
                        }
                        bindingFunctions += "}"

                        val sigName = if (method.isConstructor) "<init>" else method.methodName
                        val sigReturn = if (method.isConstructor) "V" else mangleType(method.returns)
                        val signature = "(${method.params.joinToString("") { mangleType(it.asType()) }})$sigReturn"

                        bindingSetup += """
                            $methodFieldName = env->Get${if (method.isStatic && !method.isConstructor) "Static" else ""}MethodID($classFieldName, "$sigName", "$signature");
                            if (env->ExceptionCheck()) return JNI_ERR;
                        """.trimIndent().lines()
                    }
                }

                sections += bindingFields
                sections += bindingFunctions

                // headers
                sections += classModels.flatMap { it.headers }.flatJoin(newline)

                // onLoad
                val onLoad = bindingSetup + classModels.flatMap { it.onLoad }.flatten()
                val jniVersion = model.jniVersion ?: "JNI_VERSION_1_6"
                sections += ("""
                    JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
                        JNIEnv* env;
                        if (vm->GetEnv(reinterpret_cast<void**>(&env), $jniVersion) != JNI_OK) {
                            return JNI_ERR;
                        }
                    
                """.trimIndent() + onLoad.joinToString("\n") { "    $it" } + """
                    
                        return $jniVersion;
                    }
                """.trimIndent()).lines()

                // method bindings
                sections += listOf("""extern "C" {""") + classModels.flatMap { it.methodBindings }
                    .map { binding ->
                        listOf(
                            "JNIEXPORT ${binding.returns} JNICALL ${binding.name}",
                            "  (${binding.params.joinToString(", ")}) {"
                        ) + binding.body.map { "    $it" } + "}"
                    }
                    .flatJoin(newline) + "}"

                outFile.openWriter().use { writer ->
                    writer.writeLine("// This file was auto-generated by JniGlue - do not edit!")
                    sections.flatJoin(newline).forEach { writer.writeLine(it) }
                }
            }
        } else {
            roundEnv.getElementsAnnotatedWith(JniNative::class.java).forEach { classElement ->
                val jniNative = classElement.getAnnotation(JniNative::class.java)
                val jniPriority = classElement.getAnnotation(JniPriority::class.java)?.value ?: NativePriority.NORMAL
                val jniModel = models.computeIfAbsent(jniNative.value) { JniModel() }
                classElement.getAnnotation(JniVersion::class.java)?.let { jniVersion ->
                    if (jniModel.jniVersion == null)
                        jniModel.jniVersion = jniVersion.value
                    else
                        error("Duplicate JniVersion definition")
                }

                val classModel = ClassModel(jniPriority).also { jniModel.classes += it }
                jniModel.originElements += classElement

                val packageName = classElement.enclosingPackage().qualifiedName.toString()
                val simpleClassName = classElement.simpleName.toString()
                val fullClassName = "$packageName.$simpleClassName"

                classElement.getAnnotation(JniInclude::class.java)?.let { jniInclude ->
                    classModel.includes += jniInclude.value.lines()
                }

                classElement.getAnnotation(JniHeader::class.java)?.let { jniHeader ->
                    classModel.headers += jniHeader.value.lines()
                }

                classElement.getAnnotation(JniOnLoad::class.java)?.let { jniInit ->
                    classModel.onLoad += jniInit.value.lines()
                }

                val jniTypeMapping: JniTypeMapping? = classElement.getAnnotation(JniTypeMapping::class.java)

                val callbackClass = classElement.getAnnotation(JniReferenced::class.java)?.let {
                    CallbackClass(fullClassName, simpleClassName).also {
                        classModel.callbackClasses += it
                    }
                }

                fun bindMethod(element: ExecutableElement, body: List<String>) {
                    jniModel.originElements += element

                    classModel.methodBindings += MethodBinding(
                        mangleMethod(fullClassName, element.simpleName.toString()),
                        listOf(
                            "JNIEnv* env",
                            if (element.modifiers.contains(Modifier.STATIC)) "jclass cls" else "jobject obj",
                        ) + element.parameters.map { "${it.asType().cType()} ${it.simpleName}" },
                        element.returnType.cType(),
                        body
                    )
                }

                classElement.enclosedElements.forEach { childElement ->
                    when (childElement) {
                        is ExecutableElement -> {
                            val methodName = childElement.simpleName.toString()

                            fun error(methodAnno: KClass<*>, message: String) {
                                errors += "Method $fullClassName.$methodName is annotated with ${methodAnno.simpleName}, but $message"
                            }

                            fun requireClassAnnotation(methodAnno: KClass<*>, classAnno: KClass<*>) =
                                error(methodAnno, "class is not annotated with ${classAnno.simpleName}")

                            fun requireTypeMapping(methodAnno: KClass<*>) =
                                requireClassAnnotation(methodAnno, JniTypeMapping::class)

                            fun requireReferenced(methodAnno: KClass<*>) =
                                requireClassAnnotation(methodAnno, JniReferenced::class)

                            childElement.getAnnotation(JniBind::class.java)?.let { jniBind ->
                                bindMethod(childElement, jniBind.value.lines())
                            }

                            childElement.getAnnotation(JniBindSelf::class.java)?.let { jniBindSelf ->
                                val selfType = jniTypeMapping?.value ?: run {
                                    requireTypeMapping(JniBindSelf::class)
                                    return@let
                                }

                                val params = childElement.parameters
                                if (
                                    params.isEmpty()
                                    || params[0].simpleName.toString() != "_a"
                                    || params[0].asType().toString() != "long"
                                ) {
                                    error(JniBindSelf::class, "requires 1st argument `long _a` = `address`")
                                    return@let
                                }

                                bindMethod(childElement, listOf(
                                    "auto* self = ($selfType*) _a;"
                                ) + jniBindSelf.value.lines())
                            }

                            childElement.getAnnotation(JniBindDelete::class.java)?.let {
                                val selfType = jniTypeMapping?.value ?: run {
                                    requireTypeMapping(JniBindDelete::class)
                                    return@let
                                }

                                val params = childElement.parameters
                                if (
                                    params.size != 1
                                    || params[0].simpleName.toString() != "_a"
                                    || params[0].asType().toString() != "long"
                                ) {
                                    error(JniBindDelete::class, "requires one argument `long _a` = `address`")
                                    return@let
                                }

                                bindMethod(childElement, listOf("delete ($selfType*) _a;"))
                            }

                            childElement.getAnnotation(JniCallback::class.java)?.let {
                                if (callbackClass == null) {
                                    requireReferenced(JniCallback::class)
                                    return@let
                                }

                                callbackClass.methods += if (childElement.simpleName.toString() == "<init>") {
                                    CallbackBinding(
                                        "_init",
                                        isStatic = true,
                                        isConstructor = true,
                                        childElement.parameters,
                                        classElement.asType()
                                    )
                                } else {
                                    CallbackBinding(
                                        methodName,
                                        childElement.modifiers.contains(Modifier.STATIC),
                                        isConstructor = false,
                                        childElement.parameters,
                                        childElement.returnType
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        return true
    }
}
