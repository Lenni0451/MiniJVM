package net.lenni0451.minijvm.stack;

public interface StackElement {

    int size();

    default StackElement normalize() {
        return this;
    }

    default boolean isNull() {
        return false;
    }

}
