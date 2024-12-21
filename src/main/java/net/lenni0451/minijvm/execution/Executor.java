package net.lenni0451.minijvm.execution;

import net.lenni0451.commons.asm.Modifiers;
import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.context.ExecutionContext;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.stack.StackElement;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

public class Executor {

    private static final boolean DEBUG = true;

    public static ExecutionResult execute(final ExecutionManager executionManager, final ExecutionContext executionContext, final ExecutorClass currentClass, final MethodNode currentMethod, final ExecutorObject instance, final StackElement[] arguments) {
        if (DEBUG) {
            System.out.println("Invoking method: " + currentClass.getClassNode().name + " " + currentMethod.name + currentMethod.desc);
        }
        boolean isStatic = Modifiers.has(currentMethod.access, Opcodes.ACC_STATIC);
        if (isStatic && instance != null) {
            throw new IllegalStateException("Tried to execute a static method with an instance");
        } else if (!isStatic && instance == null) {
            throw new IllegalStateException("Tried to execute an instance method without an instance");
        }

        executionContext.pushStackFrame(currentClass, currentMethod, -2);
        MethodExecutor methodExecutor = executionManager.getMethodExecutor(executionContext, currentClass.getClassNode().name, currentMethod);
        ExecutionResult result = methodExecutor.execute(executionManager, executionContext, currentClass, currentMethod, instance, arguments);
        executionContext.popStackFrame();
        if (DEBUG) {
            System.out.println("----- Finished " + currentClass.getClassNode().name + " " + currentMethod.name + currentMethod.desc + " execution with result " + result + " -----");
        }
        return result;
    }

}
