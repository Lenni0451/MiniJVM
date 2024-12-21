package net.lenni0451.minijvm.execution;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.ExecutionResult;
import net.lenni0451.minijvm.context.ExecutionContext;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.stack.StackElement;
import org.objectweb.asm.tree.MethodNode;

public interface MethodExecutor {

    MethodExecutor NOOP_VOID = (executionManager, executionContext, currentClass, currentMethod, instance, arguments) -> ExecutionResult.voidResult();


    ExecutionResult execute(final ExecutionManager executionManager, final ExecutionContext executionContext, final ExecutorClass currentClass, final MethodNode currentMethod, final ExecutorObject instance, final StackElement[] arguments);

}
