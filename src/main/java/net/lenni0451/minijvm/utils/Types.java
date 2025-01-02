package net.lenni0451.minijvm.utils;

import org.objectweb.asm.Type;

public class Types extends net.lenni0451.commons.asm.Types {

    public static final Type CLASS = Type.getType(Class.class);
    public static final Type OBJECT = Type.getType(Object.class);
    public static final Type STRING = Type.getType(String.class);
    public static final Type BYTE_ARRAY = Type.getType(byte[].class);
    public static final Type CHAR_ARRAY = Type.getType(char[].class);
    public static final Type THROWABLE = Type.getType(Throwable.class);
    public static final Type ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION = Type.getType(ArrayIndexOutOfBoundsException.class);
    public static final Type NO_SUCH_FIELD_ERROR = Type.getType(NoSuchFieldError.class);
    public static final Type NULL_POINTER_EXCEPTION = Type.getType(NullPointerException.class);
    public static final Type NO_SUCH_METHOD_ERROR = Type.getType(NoSuchMethodError.class);
    public static final Type INCOMPATIBLE_CLASS_CHANGE_ERROR = Type.getType(IncompatibleClassChangeError.class);
    public static final Type CLASS_NOT_FOUND_EXCEPTION = Type.getType(ClassNotFoundException.class);
    public static final Type CLONE_NOT_SUPPORTED_EXCEPTION = Type.getType(CloneNotSupportedException.class);
    public static final Type INTERNAL_ERROR = Type.getType(InternalError.class);
    public static final Type ILLEGAL_ARGUMENT_EXCEPTION = Type.getType(IllegalArgumentException.class);
    public static final Type NEGATIVE_ARRAY_SIZE_EXCEPTION = Type.getType(NegativeArraySizeException.class);
    public static final Type CLASS_CAST_EXCEPTION = Type.getType(ClassCastException.class);

    public static Type asArray(final Type type, final int dimensions) {
        if (dimensions < 0) throw new IllegalArgumentException("Dimensions must be greater or equal to 0");
        return Type.getType("[".repeat(dimensions) + type.getDescriptor());
    }

    public static Type arrayType(final Type type) {
        if (type.getSort() != Type.ARRAY) throw new IllegalArgumentException("Type is not an array");
        if (type.getDimensions() == 1) return type.getElementType();
        else return Type.getType(type.getDescriptor().substring(1));
    }

}
