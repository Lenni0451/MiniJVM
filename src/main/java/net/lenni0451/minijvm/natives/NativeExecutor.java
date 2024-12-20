package net.lenni0451.minijvm.natives;

import net.lenni0451.minijvm.ExecutionContext;
import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.stack.StackElement;
import org.objectweb.asm.tree.MethodNode;

public interface NativeExecutor {

    NativeExecutor NOOP_VOID = (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> null;


    StackElement execute(final ExecutionManager executionManager, final ExecutionContext executionContext, final ExecutorClass currentClass, final MethodNode currentMethod, final ExecutorObject instance, final StackElement[] arguments);

}
