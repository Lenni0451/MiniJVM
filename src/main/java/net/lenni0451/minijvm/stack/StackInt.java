package net.lenni0451.minijvm.stack;

public record StackInt(int value) implements StackElement {

    public static final StackInt MINUS1 = new StackInt(-1);
    public static final StackInt ZERO = new StackInt(0);
    public static final StackInt ONE = new StackInt(1);
    public static final StackInt TWO = new StackInt(2);
    public static final StackInt THREE = new StackInt(3);
    public static final StackInt FOUR = new StackInt(4);
    public static final StackInt FIVE = new StackInt(5);

    public StackInt(final boolean b) {
        this(b ? 1 : 0);
    }

    public boolean booleanValue() {
        return this.value != 0;
    }

    @Override
    public int size() {
        return 1;
    }

}
