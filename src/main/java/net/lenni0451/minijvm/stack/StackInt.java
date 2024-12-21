package net.lenni0451.minijvm.stack;

public record StackInt(int value) implements StackElement {

    public StackInt(final boolean b) {
        this(b ? 1 : 0);
    }

    @Override
    public int size() {
        return 1;
    }

}
