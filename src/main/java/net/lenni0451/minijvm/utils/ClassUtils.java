package net.lenni0451.minijvm.utils;

import net.lenni0451.minijvm.context.ExecutionContext;
import net.lenni0451.minijvm.exception.ExecutorException;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.object.types.ClassObject;
import net.lenni0451.minijvm.stack.StackObject;

import java.util.Map;
import java.util.Set;

public class ClassUtils {

    public static final Set<String> PRIMITIVE_CLASSES = Set.of("void", "boolean", "byte", "short", "char", "int", "long", "float", "double");
    public static final Map<String, String> PRIMITIVE_DESCRIPTOR_TO_CLASS = Map.of("V", "void", "Z", "boolean", "B", "byte", "S", "short", "C", "char", "I", "int", "J", "long", "F", "float", "D", "double");

    public static ExecutorClass getClassFromClassInstance(final ExecutionContext executionContext, final StackObject stackObject) {
        ExecutorObject executorObject = stackObject.value();
        if (!(executorObject instanceof ClassObject classObject)) {
            throw new ExecutorException(executionContext, "Expected a ClassObject but got " + executorObject.getClass().getSimpleName() + " - " + executorObject);
        }
        return classObject.getClassType();
    }

}