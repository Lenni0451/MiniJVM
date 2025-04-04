package net.lenni0451.minijvm.utils;

import net.lenni0451.commons.asm.ASMUtils;
import net.lenni0451.minijvm.ExecutionContext;
import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.execution.Executor;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.object.types.ArrayObject;
import net.lenni0451.minijvm.stack.*;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

import javax.annotation.Nullable;
import java.util.function.Supplier;

import static net.lenni0451.minijvm.utils.Types.*;

public class ExecutorTypeUtils {

    public static StackElement parse(final ExecutionContext context, final Object jvmObject) {
        if (jvmObject == null) return StackObject.NULL;
        ExecutionManager manager = context.getExecutionManager();
        if (jvmObject instanceof Integer i) {
            return new StackInt(i);
        } else if (jvmObject instanceof Long l) {
            return new StackLong(l);
        } else if (jvmObject instanceof Float f) {
            return new StackFloat(f);
        } else if (jvmObject instanceof Double d) {
            return new StackDouble(d);
        } else if (jvmObject instanceof String s) {
            ExecutorClass stringClass = manager.loadClass(context, STRING);
            ExecutorObject stringObject = manager.instantiate(context, stringClass);
            if (s.isEmpty()) {
                //TODO: Find out what to actually do with empty strings
                ExecutorClass.ResolvedField valueField = stringClass.findField(context, "value", "[B");
                stringObject.setField(valueField.field(), new StackObject(manager.instantiateArray(context, manager.loadClass(context, BYTE_ARRAY), new StackElement[0])));
            } else {
                char[] value = s.toCharArray();
                StackElement[] valueArray = new StackElement[value.length];
                for (int i = 0; i < value.length; i++) valueArray[i] = new StackInt(value[i]);
                ExecutorClass arrayClass = manager.loadClass(context, CHAR_ARRAY);
                ExecutorObject arrayObject = manager.instantiateArray(context, arrayClass, valueArray);
                Executor.execute(context, stringClass, ASMUtils.getMethod(stringClass.getClassNode(), "<init>", "([C)V"), stringObject, new StackObject(arrayObject));
            }
            return new StackObject(stringObject);
        } else if (jvmObject instanceof Type t) {
            ExecutorClass typeClass = manager.loadClass(context, t);
            return new StackObject(manager.instantiateClass(context, typeClass));
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
            return StackInt.ZERO;
        } else if (type.equals(StackLong.class)) {
            return StackLong.ZERO;
        } else if (type.equals(StackFloat.class)) {
            return StackFloat.ZERO;
        } else if (type.equals(StackDouble.class)) {
            return StackDouble.ZERO;
        } else if (type.equals(StackObject.class)) {
            return StackObject.NULL;
        } else {
            throw new UnsupportedOperationException("Unsupported field type: " + type.getName());
        }
    }

    public static String fromExecutorString(final ExecutionContext context, final ExecutorObject executorObject) {
        if (!executorObject.getClazz().getClassNode().name.equals("java/lang/String")) {
            throw new IllegalArgumentException("The given executor object is not a string object");
        }
        ExecutorClass.ResolvedMethod toCharArray = executorObject.getClazz().findMethod(context, "toCharArray", "()[C");
        StackObject valueArray = (StackObject) Executor.execute(context, toCharArray.owner(), toCharArray.method(), executorObject).getReturnValue();
        StringBuilder builder = new StringBuilder();
        for (StackElement element : ((ArrayObject) valueArray.value()).getElements()) {
            builder.append((char) ((StackInt) element).value());
        }
        return builder.toString();
    }

    public static StackObject newArray(final ExecutionContext context, final Type type, final int length, @Nullable final Supplier<StackElement> supplier) {
        StackElement[] elements = new StackElement[length];
        if (supplier != null) {
            for (int i = 0; i < length; i++) elements[i] = supplier.get();
        }
        return newArray(context, type, elements);
    }

    public static StackObject newArray(final ExecutionContext context, final Type type, final StackElement[] elements) {
        ExecutorClass arrayClass = context.getExecutionManager().loadClass(context, type);
        ExecutorObject arrayObject = context.getExecutionManager().instantiateArray(context, arrayClass, elements);
        return new StackObject(arrayObject);
    }

}
