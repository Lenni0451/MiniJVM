package net.lenni0451.minijvm.context;

public class ContextKey<T> {

    public static <T> ContextKey<T> named(final String name) {
        return new ContextKey<>(name);
    }


    private final String name;

    private ContextKey(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

}
