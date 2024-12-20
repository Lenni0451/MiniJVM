package net.lenni0451.minijvm.object;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.context.ExecutionContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

public class ClassClass extends ExecutorClass {

    private static ClassNode buildClass(final Type type) {
        ClassNode classNode = new ClassNode();
        classNode.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, type.getInternalName(), null, "java/lang/Class", null);
        return classNode;
    }

    public ClassClass(final ExecutionManager executionManager, final ExecutionContext executionContext, final Type type) {
        super(executionManager, executionContext, buildClass(type));
    }

    @Override
    public String toString() {
        return "ClassClass{" + this.getClassNode().name + "}";
    }

}
