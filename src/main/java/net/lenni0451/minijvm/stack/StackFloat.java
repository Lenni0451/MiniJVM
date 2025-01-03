package net.lenni0451.minijvm.stack;

public record StackFloat(float value) implements StackElement {

    public static final StackFloat ZERO = new StackFloat(0);
    public static final StackFloat ONE = new StackFloat(1);
    public static final StackFloat TWO = new StackFloat(2);

    @Override
    public int size() {
        return 1;
    }

}
