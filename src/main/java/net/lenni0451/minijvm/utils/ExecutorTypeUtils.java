package net.lenni0451.minijvm.utils;

import net.lenni0451.commons.asm.ASMUtils;
import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.Executor;
import net.lenni0451.minijvm.context.ExecutionContext;
import net.lenni0451.minijvm.object.ArrayObject;
import net.lenni0451.minijvm.object.ClassClass;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.stack.*;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ExecutorTypeUtils {

    public static StackElement parse(final ExecutionManager executionManager, final ExecutionContext executionContext, final Object jvmObject) {
        if (jvmObject == null) return StackObject.NULL;
        if (jvmObject instanceof Integer i) {
            return new StackInt(i);
        } else if (jvmObject instanceof Long l) {
            return new StackLong(l);
        } else if (jvmObject instanceof Float f) {
            return new StackFloat(f);
        } else if (jvmObject instanceof Double d) {
            return new StackDouble(d);
        } else if (jvmObject instanceof String s) {
            ExecutorClass stringClass = executionManager.loadClass(executionContext, "java/lang/String");
            ExecutorObject stringObject = executionManager.instantiate(executionContext, stringClass);
            if (s.isEmpty()) {
                //TODO: Find out what to actually do with empty strings
                ExecutorClass.ResolvedField valueField = stringClass.findField("value", "[B");
                stringObject.setField(valueField.field(), new StackObject(executionManager.instantiateArray(executionContext, executionManager.loadClass(executionContext, "[B"), new StackElement[0])));
            } else {
                char[] value = s.toCharArray();
                StackElement[] valueArray = new StackElement[value.length];
                for (int i = 0; i < value.length; i++) valueArray[i] = new StackInt(value[i]);
                ExecutorClass arrayClass = executionManager.loadClass(executionContext, "[C");
                ExecutorObject arrayObject = executionManager.instantiateArray(executionContext, arrayClass, valueArray);
                Executor.execute(executionManager, executionContext, stringClass, ASMUtils.getMethod(stringClass.getClassNode(), "<init>", "([C)V"), stringObject, new StackElement[]{new StackObject(arrayObject)});
            }
            return new StackObject(stringObject);
        } else if (jvmObject instanceof Type t) {
            ClassClass classClass = executionManager.loadClassClass(executionContext, t.getInternalName());
            ExecutorObject executorObject = executionManager.instantiate(executionContext, classClass);
            return new StackObject(executorObject);
        } else if (jvmObject instanceof Handle) {
            //TODO: Convert to executor object
            throw new UnsupportedOperationException("Unsupported field value type: " + jvmObject.getClass().getName());
        } else if (jvmObject instanceof ConstantDynamic) {
            //TODO: Convert to executor object
            throw new UnsupportedOperationException("Unsupported field value type: " + jvmObject.getClass().getName());
        } else {
            throw new UnsupportedOperationException("Unsupported field value type: " + jvmObject.getClass().getName());
        }
    }

    public static Class<? extends StackElement> typeToStackType(final Type type) {
        if (type.equals(Type.BOOLEAN_TYPE)) return StackInt.class;
        if (type.equals(Type.BYTE_TYPE)) return StackInt.class;
        if (type.equals(Type.CHAR_TYPE)) return StackInt.class;
        if (type.equals(Type.SHORT_TYPE)) return StackInt.class;
        if (type.equals(Type.INT_TYPE)) return StackInt.class;
        if (type.equals(Type.LONG_TYPE)) return StackLong.class;
        if (type.equals(Type.FLOAT_TYPE)) return StackFloat.class;
        if (type.equals(Type.DOUBLE_TYPE)) return StackDouble.class;
        return StackObject.class;
    }

    public static StackElement getFieldDefault(final Class<? extends StackElement> type) {
        if (type.equals(StackInt.class)) {
            return new StackInt(0);
        } else if (type.equals(StackLong.class)) {
            return new StackLong(0);
        } else if (type.equals(StackFloat.class)) {
            return new StackFloat(0);
        } else if (type.equals(StackDouble.class)) {
            return new StackDouble(0);
        } else if (type.equals(StackObject.class)) {
            return StackObject.NULL;
        } else {
            throw new UnsupportedOperationException("Unsupported field type: " + type.getName());
        }
    }

    public static String fromExecutorString(final ExecutionManager executionManager, final ExecutionContext executionContext, final ExecutorObject executorObject) {
        if (!executorObject.getOwner().getClassNode().name.equals("java/lang/String")) {
            throw new IllegalArgumentException("The given executor object is not a string object");
        }
        ExecutorClass.ResolvedMethod toCharArray = executorObject.getOwner().findMethod("toCharArray", "()[C");
        StackObject valueArray = (StackObject) Executor.execute(executionManager, executionContext, toCharArray.owner(), toCharArray.method(), executorObject, new StackElement[0]);
        StringBuilder builder = new StringBuilder();
        for (StackElement element : ((ArrayObject) valueArray.value()).getElements()) {
            builder.append((char) ((StackInt) element).value());
        }
        return builder.toString();
    }

    public static StackObject newArray(final ExecutionManager executionManager, final ExecutionContext executionContext, final Type type, final int length, @Nullable final Supplier<StackElement> supplier) {
        StackElement[] elements = new StackElement[length];
        if (supplier != null) {
            for (int i = 0; i < length; i++) elements[i] = supplier.get();
        }
        return newArray(executionManager, executionContext, type, elements);
    }

    public static StackObject newArray(final ExecutionManager executionManager, final ExecutionContext executionContext, final Type type, final StackElement[] elements) {
        ExecutorClass arrayClass = executionManager.loadClass(executionContext, type.getDescriptor());
        ExecutorObject arrayObject = executionManager.instantiateArray(executionContext, arrayClass, elements);
        return new StackObject(arrayObject);
    }

}
