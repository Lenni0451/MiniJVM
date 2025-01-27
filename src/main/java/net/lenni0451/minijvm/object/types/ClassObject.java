package net.lenni0451.minijvm.object.types;

import net.lenni0451.minijvm.ExecutionContext;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.utils.Types;

public class ClassObject extends ExecutorObject {

    private final ExecutorClass classType;

    public ClassObject(final ExecutionContext context, final ExecutorClass classType) {
        super(context, context.getExecutionManager().loadClass(context, Types.CLASS));
        this.classType = classType;
    }

    public ExecutorClass getClassType() {
        return this.classType;
    }

    @Override
    public String toString() {
        return "ClassObject{" + this.classType.getClassNode().name + "}";
    }

}
