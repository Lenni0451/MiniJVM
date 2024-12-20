package net.lenni0451.minijvm.stack;

public record StackInt(int value) implements StackElement {

    @Override
    public int size() {
        return 1;
    }

}
