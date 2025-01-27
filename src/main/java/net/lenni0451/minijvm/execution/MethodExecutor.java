package net.lenni0451.minijvm.execution;

import net.lenni0451.minijvm.ExecutionContext;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.stack.StackElement;
import net.lenni0451.minijvm.utils.ExceptionUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

public interface MethodExecutor {

    MethodExecutor NOOP_VOID = (executionContext, currentClass, currentMethod, instance, arguments) -> ExecutionResult.voidResult();
    MethodExecutor STACK_DUMP = (executionContext, currentClass, currentMethod, instance, arguments) -> {
        System.out.println("Stack dump:");
        for (ExecutionContext.StackFrame stackFrame : executionContext.getStackFrames()) {
            System.out.println(" -> " + stackFrame);
        }
        return ExceptionUtils.newException(executionContext, Type.getType(InternalError.class), "Stack dump");
    };

    ExecutionResult execute(final ExecutionContext context, final ExecutorClass currentClass, final MethodNode currentMethod, final ExecutorObject instance, final StackElement[] arguments);

}
