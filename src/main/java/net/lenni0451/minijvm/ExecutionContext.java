package net.lenni0451.minijvm;

import net.lenni0451.minijvm.object.ExecutorClass;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to manage the current state of the executor.<br>
 * This is the equivalent of the Thread in the JVM.
 */
public class ExecutionContext {

    private final ExecutionManager executionManager;
    private final List<StackFrame> stackFrames;

    public ExecutionContext(final ExecutionManager executionManager) {
        this.executionManager = executionManager;
        this.stackFrames = new ArrayList<>();
    }

    public ExecutionManager getExecutionManager() {
        return this.executionManager;
    }

    public StackFrame getCurrentStackFrame() {
        return this.stackFrames.get(this.stackFrames.size() - 1);
    }

    public StackFrame[] getStackFrames() {
        return this.stackFrames.toArray(new StackFrame[0]);
    }

    public StackFrame pushStackFrame(final ExecutorClass executorClass, final MethodNode methodNode, final int lineNumber) {
        StackFrame stackFrame = new StackFrame(executorClass, methodNode, lineNumber);
        this.stackFrames.add(stackFrame);
        return stackFrame;
    }

    public StackFrame popStackFrame() {
        return this.stackFrames.remove(this.stackFrames.size() - 1);
    }


    public static class StackFrame {
        private final ExecutorClass executorClass;
        private final MethodNode methodNode;
        private int lineNumber;

        public StackFrame(final ExecutorClass executorClass, final MethodNode methodNode, final int lineNumber) {
            this.executorClass = executorClass;
            this.methodNode = methodNode;
            this.lineNumber = lineNumber;
        }

        public ExecutorClass getExecutorClass() {
            return this.executorClass;
        }

        public MethodNode getMethodNode() {
            return this.methodNode;
        }

        public int getLineNumber() {
            return this.lineNumber;
        }

        public void setLineNumber(final int lineNumber) {
            this.lineNumber = lineNumber;
        }

        public boolean isNativeMethod() {
            return this.lineNumber == -2;
        }

        @Override
        public String toString() {
            String fileName = this.executorClass.getClassNode().sourceFile;
            String className = this.executorClass.getClassNode().name;
            String methodName = this.methodNode.name;
            return className + "." + methodName + this.methodNode.desc + (this.isNativeMethod() ? "(Native Method)" : (fileName != null && this.lineNumber >= 0 ? "(" + fileName + ":" + this.lineNumber + ")" : (fileName != null ? "(" + fileName + ")" : "(Unknown Source)")));
        }
    }

}
