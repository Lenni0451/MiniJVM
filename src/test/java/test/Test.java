package test;

import net.lenni0451.commons.asm.io.ClassIO;
import net.lenni0451.commons.asm.provider.LoaderClassProvider;
import net.lenni0451.commons.asm.provider.MapClassProvider;
import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.context.ExecutionContext;
import net.lenni0451.minijvm.exception.ExecutorException;
import net.lenni0451.minijvm.execution.Executor;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.stack.StackElement;
import net.lenni0451.minijvm.stack.StackObject;
import net.lenni0451.minijvm.utils.ExecutorTypeUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.HashMap;
import java.util.Map;

public class Test {

    public static void main(String[] args) {
        try {
            ClassNode node = new ClassNode();
            node.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "Test", null, "java/lang/Object", null);
            MethodNode methodNode = new MethodNode(Opcodes.ACC_STATIC, "test", "()Ljava/lang/String;", null, null);
            methodNode.instructions.add(new TypeInsnNode(Opcodes.NEW, "java/lang/StringBuilder"));
            methodNode.instructions.add(new InsnNode(Opcodes.DUP));
            methodNode.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false));
            methodNode.instructions.add(new LdcInsnNode("Hello "));
            methodNode.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));
            methodNode.instructions.add(new LdcInsnNode("World!"));
            methodNode.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));
            methodNode.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false));
            methodNode.instructions.add(new InsnNode(Opcodes.ARETURN));
            node.methods.add(methodNode);

            Map<String, byte[]> classes = new HashMap<>();
            classes.put("Test", ClassIO.toStacklessBytes(node));

            ExecutionManager manager = new ExecutionManager(new MapClassProvider(classes, MapClassProvider.NameFormat.SLASH).then(new LoaderClassProvider()));
            ExecutionContext context = new ExecutionContext();
            ExecutorClass clazz = manager.loadClass(context, Type.getObjectType("Test"));
            StackElement returnValue = Executor.execute(manager, context, clazz, methodNode, null).getReturnValue();
            System.out.println(returnValue);
            System.out.println(ExecutorTypeUtils.fromExecutorString(manager, context, ((StackObject) returnValue).value()));
        } catch (ExecutorException e) {
            e.printStackTrace();
            for (ExecutionContext.StackFrame stackFrame : e.getStackFrames()) {
                System.err.println(stackFrame);
            }
        }
    }

}
