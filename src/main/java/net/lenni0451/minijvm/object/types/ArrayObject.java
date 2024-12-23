package net.lenni0451.minijvm.object.types;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.context.ExecutionContext;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.stack.StackElement;
import org.objectweb.asm.Type;

import static net.lenni0451.commons.asm.Types.type;

public class ArrayObject extends ExecutorObject {

    private final StackElement[] elements;

    public ArrayObject(final ExecutionManager executionManager, final ExecutionContext executionContext, final ExecutorClass arrayType, final StackElement[] elements) {
        super(executionManager, executionContext, arrayType);
        this.elements = elements;
    }

    public Type getElementType() {
        return type(this.getOwner().getClassNode().name).getElementType();
    }

    public StackElement[] getElements() {
        return this.elements;
    }

    @Override
    public String toString() {
        return "ArrayObject{" + this.getOwner().getClassNode().name + "=" + this.elements.length + "}";
    }

}
