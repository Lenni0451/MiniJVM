package net.lenni0451.minijvm.object.types;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.context.ExecutionContext;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;

public class ClassObject extends ExecutorObject {

    private final ExecutorClass classType;

    public ClassObject(final ExecutionManager executionManager, final ExecutionContext executionContext, final ExecutorClass classType) {
        super(executionManager, executionContext, executionManager.loadClass(executionContext, "java/lang/Class"));
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
