package net.lenni0451.minijvm.natives;

import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.context.ExecutionContext;
import net.lenni0451.minijvm.object.ClassClass;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.stack.StackElement;
import net.lenni0451.minijvm.stack.StackObject;
import org.objectweb.asm.tree.MethodNode;

import java.util.function.Consumer;

public class ReflectionNatives implements Consumer<ExecutionManager> {

    @Override
    public void accept(ExecutionManager manager) {
        manager.registerNativeExecutor("jdk/internal/reflect/Reflection.getCallerClass()Ljava/lang/Class;", new NativeExecutor() {
            @Override
            public StackElement execute(ExecutionManager executionManager, ExecutionContext executionContext, ExecutorClass currentClass, MethodNode currentMethod, ExecutorObject instance, StackElement[] arguments) {
                ExecutionContext.StackFrame[] stackFrames = executionContext.getStackFrames();
                for (int i = stackFrames.length - 1; i >= 0; i--) {
                    ExecutionContext.StackFrame stackFrame = stackFrames[i];
                    if (stackFrame.isNativeMethod()) continue;
                    if (stackFrame.getExecutorClass().getClassNode().name.startsWith("java/lang/reflect/")) continue;
                    if (stackFrame.getExecutorClass().getClassNode().name.startsWith("java/lang/invoke/")) continue;
                    ClassClass classClass = executionManager.loadClassClass(executionContext, stackFrame.getExecutorClass().getClassNode().name);
                    return new StackObject(executionManager.instantiate(executionContext, classClass));
                }
                //TODO: What to do if no class was found?
                ClassClass classClass = executionManager.loadClassClass(executionContext, stackFrames[stackFrames.length - 1].getExecutorClass().getClassNode().name);
                return new StackObject(executionManager.instantiate(executionContext, classClass));
            }
        });
    }

}
