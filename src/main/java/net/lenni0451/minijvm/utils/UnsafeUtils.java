package net.lenni0451.minijvm.utils;

import net.lenni0451.minijvm.object.ExecutorClass;
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
    public static FieldNode getFieldByHashCode(final ExecutorClass executorClass, final int hashCode) {
        for (FieldNode field : executorClass.getClassNode().fields) {
            if (getFieldHashCode(field) == hashCode) return field;
        }
        return null;
    }

    public static int getFieldHashCode(final FieldNode fieldNode) {
        return Math.abs(fieldNode.name.hashCode());
    }

}
