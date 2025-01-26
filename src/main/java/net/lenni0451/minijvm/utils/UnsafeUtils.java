package net.lenni0451.minijvm.utils;

import net.lenni0451.minijvm.object.ExecutorClass;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

import javax.annotation.Nullable;

public class UnsafeUtils {

    @Nullable
    public static FieldNode getFieldByName(final ExecutorClass executorClass, final String name) {
        for (FieldNode field : executorClass.getClassNode().fields) {
            if (field.name.equals(name)) return field;
        }
        return null;
    }

    @Nullable
    public static FieldNode getFieldByHashCode(final ExecutorClass executorClass, final long hashCode) {
        for (FieldNode field : executorClass.getClassNode().fields) {
            if (getFieldHashCode(field) == hashCode) return field;
        }
        return null;
    }

    public static long getFieldHashCode(final FieldNode fieldNode) {
        return Math.abs(fieldNode.name.hashCode());
    }

    public static int arrayIndexScale(final Type type) {
        return switch (type.getSort()) {
            case Type.BOOLEAN -> 1;
            case Type.BYTE -> Byte.BYTES;
            case Type.SHORT -> Short.BYTES;
            case Type.CHAR -> Character.BYTES;
            case Type.INT -> Integer.BYTES;
            case Type.LONG -> Long.BYTES;
            case Type.FLOAT -> Float.BYTES;
            case Type.DOUBLE -> Double.BYTES;
            default -> 4; //Address size
        };
    }

}
