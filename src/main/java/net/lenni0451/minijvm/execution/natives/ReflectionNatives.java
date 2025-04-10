package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionContext;
import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.stack.StackObject;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

public class ReflectionNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("jdk/internal/reflect/Reflection.getCallerClass()Ljava/lang/Class;", (executionContext, currentClass, currentMethod, instance, arguments) -> {
            ExecutionContext.StackFrame[] stackFrames = executionContext.getStackFrames();
            ExecutionContext.StackFrame firstFrame = stackFrames[stackFrames.length - 1]; //TODO: What to do if no class was found?
            for (int i = stackFrames.length - 1; i >= 0; i--) {
                ExecutionContext.StackFrame stackFrame = stackFrames[i];
                //TODO: What are the actual checks?
                if (stackFrame.isNativeMethod()) continue;
                if (stackFrame.getExecutorClass().getClassNode().name.startsWith("java/lang/reflect/")) continue;
                if (stackFrame.getExecutorClass().getClassNode().name.startsWith("java/lang/invoke/")) continue;
                firstFrame = stackFrame;
                break;
            }
            return returnValue(new StackObject(executionContext.getExecutionManager().instantiateClass(executionContext, firstFrame.getExecutorClass())));
        });
    }

}
