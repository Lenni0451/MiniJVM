package net.lenni0451.minijvm.execution;

import net.lenni0451.minijvm.context.ExecutionContext;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.stack.StackElement;
import org.objectweb.asm.tree.MethodNode;

public interface MethodExecutor {

    MethodExecutor NOOP_VOID = (executionContext, currentClass, currentMethod, instance, arguments) -> ExecutionResult.voidResult();


    ExecutionResult execute(final ExecutionContext executionContext, final ExecutorClass currentClass, final MethodNode currentMethod, final ExecutorObject instance, final StackElement[] arguments);

}
