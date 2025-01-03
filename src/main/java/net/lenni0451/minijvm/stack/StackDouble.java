package net.lenni0451.minijvm.stack;

public record StackDouble(double value) implements StackElement {

    public static final StackDouble ZERO = new StackDouble(0);
    public static final StackDouble ONE = new StackDouble(1);

    @Override
    public int size() {
        return 2;
    }

}
