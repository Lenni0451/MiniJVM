package net.lenni0451.minijvm.execution.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.context.ExecutionContext;
import net.lenni0451.minijvm.execution.ExecutionResult;
import net.lenni0451.minijvm.execution.MethodExecutor;
import net.lenni0451.minijvm.object.ClassClass;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.stack.StackElement;
import net.lenni0451.minijvm.stack.StackObject;
import org.objectweb.asm.tree.MethodNode;

import java.util.function.Consumer;

import static net.lenni0451.minijvm.execution.ExecutionResult.returnValue;

public class ReflectionNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerMethodExecutor("jdk/internal/reflect/Reflection.getCallerClass()Ljava/lang/Class;", new MethodExecutor() {
            @Override
            public ExecutionResult execute(ExecutionManager executionManager, ExecutionContext executionContext, ExecutorClass currentClass, MethodNode currentMethod, ExecutorObject instance, StackElement[] arguments) {
                ExecutionContext.StackFrame[] stackFrames = executionContext.getStackFrames();
                for (int i = stackFrames.length - 1; i >= 0; i--) {
                    ExecutionContext.StackFrame stackFrame = stackFrames[i];
                    if (stackFrame.isNativeMethod()) continue;
                    if (stackFrame.getExecutorClass().getClassNode().name.startsWith("java/lang/reflect/")) continue;
                    if (stackFrame.getExecutorClass().getClassNode().name.startsWith("java/lang/invoke/")) continue;
                    ClassClass classClass = executionManager.loadClassClass(executionContext, stackFrame.getExecutorClass().getClassNode().name);
                    return returnValue(new StackObject(executionManager.instantiate(executionContext, classClass)));
                }
                //TODO: What to do if no class was found?
                ClassClass classClass = executionManager.loadClassClass(executionContext, stackFrames[stackFrames.length - 1].getExecutorClass().getClassNode().name);
                return returnValue(new StackObject(executionManager.instantiate(executionContext, classClass)));
            }
        });
    }

}
