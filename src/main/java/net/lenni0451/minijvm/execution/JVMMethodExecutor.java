package net.lenni0451.minijvm.execution;

import net.lenni0451.commons.asm.Modifiers;
import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.context.ExecutionContext;
import net.lenni0451.minijvm.exception.ExecutorException;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.object.types.ArrayObject;
import net.lenni0451.minijvm.stack.*;
import net.lenni0451.minijvm.utils.ExceptionUtils;
import net.lenni0451.minijvm.utils.ExecutorStack;
import net.lenni0451.minijvm.utils.ExecutorTypeUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static net.lenni0451.commons.asm.Types.*;

public class JVMMethodExecutor implements MethodExecutor {

    @Override
    public ExecutionResult execute(ExecutionManager executionManager, ExecutionContext executionContext, ExecutorClass currentClass, MethodNode currentMethod, ExecutorObject instance, StackElement[] arguments) {
        boolean isStatic = Modifiers.has(currentMethod.access, Opcodes.ACC_STATIC);
        Map<Integer, StackElement> locals = new HashMap<>();
        {
            if (!isStatic) locals.put(0, new StackObject(instance));
            int currentIndex = isStatic ? 0 : 1;
            for (StackElement argument : arguments) {
                //TODO: Verification: Check if some method overwrites 2 wide locals
                locals.put(currentIndex, argument);
                currentIndex += argument.size();
            }
        }
        ExecutionContext.StackFrame stackFrame = executionContext.getCurrentStackFrame();
        ExecutorStack stack = new ExecutorStack(executionContext);
        AbstractInsnNode currentInstruction = currentMethod.instructions.getFirst();
        ExecutionResult result = null;
        while (true) {
            if (ExecutionManager.DEBUG) {
                System.out.println("  " + currentInstruction.getClass().getSimpleName() + " -> " + Arrays.stream(stack.getStack()).map(StackElement::toString).collect(Collectors.joining(", ")));
            }
            int opcode = currentInstruction.getOpcode();
            switch (opcode) {
                case Opcodes.NOP:
                    break;
                case Opcodes.ACONST_NULL:
                    stack.push(StackObject.NULL);
                    break;
                case Opcodes.ICONST_M1:
                case Opcodes.ICONST_0:
                case Opcodes.ICONST_1:
                case Opcodes.ICONST_2:
                case Opcodes.ICONST_3:
                case Opcodes.ICONST_4:
                case Opcodes.ICONST_5:
                    stack.push(new StackInt(opcode - Opcodes.ICONST_0));
                    break;
                case Opcodes.LCONST_0:
                case Opcodes.LCONST_1:
                    stack.push(new StackLong(opcode - Opcodes.LCONST_0));
                    break;
                case Opcodes.FCONST_0:
                case Opcodes.FCONST_1:
                case Opcodes.FCONST_2:
                    stack.push(new StackFloat(opcode - Opcodes.FCONST_0));
                    break;
                case Opcodes.DCONST_0:
                case Opcodes.DCONST_1:
                    stack.push(new StackDouble(opcode - Opcodes.DCONST_0));
                    break;
                case Opcodes.BIPUSH:
                case Opcodes.SIPUSH:
                    IntInsnNode intInsnNode1 = (IntInsnNode) currentInstruction;
                    stack.push(new StackInt(intInsnNode1.operand));
                    break;
                case Opcodes.LDC:
                    LdcInsnNode ldcInsnNode = (LdcInsnNode) currentInstruction;
                    stack.push(ExecutorTypeUtils.parse(executionManager, executionContext, ldcInsnNode.cst));
                    break;
                case Opcodes.ILOAD:
                case Opcodes.LLOAD:
                case Opcodes.FLOAD:
                case Opcodes.DLOAD:
                case Opcodes.ALOAD:
                    VarInsnNode varInsnNode = (VarInsnNode) currentInstruction;
                    StackElement value = locals.get(varInsnNode.var);
                    verifyType(executionContext, value, getTypeFromOpcode(opcode));
                    stack.push(value);
                    break;
                case Opcodes.IALOAD:
                case Opcodes.LALOAD:
                case Opcodes.FALOAD:
                case Opcodes.DALOAD:
                case Opcodes.AALOAD:
                case Opcodes.BALOAD:
                case Opcodes.CALOAD:
                case Opcodes.SALOAD:
                    StackInt index = stack.pop(StackInt.class);
                    StackObject array = stack.pop(StackObject.class);
                    StackElement[] arrayElements = ((ArrayObject) array.value()).getElements();
                    //TODO: Type checks
                    if (index.value() < 0 || index.value() >= arrayElements.length) {
                        result = ExceptionUtils.newException(executionManager, executionContext, "java/lang/ArrayIndexOutOfBoundsException", "Index: " + index.value() + ", Length: " + arrayElements.length);
                    } else {
                        value = arrayElements[index.value()];
                        if (opcode == Opcodes.BALOAD) value = new StackInt((byte) ((StackInt) value).value());
                        else if (opcode == Opcodes.CALOAD) value = new StackInt((char) ((StackInt) value).value());
                        else if (opcode == Opcodes.SALOAD) value = new StackInt((short) ((StackInt) value).value());
                        stack.push(value);
                    }
                    break;
                case Opcodes.ISTORE:
                case Opcodes.LSTORE:
                case Opcodes.FSTORE:
                case Opcodes.DSTORE:
                case Opcodes.ASTORE:
                    varInsnNode = (VarInsnNode) currentInstruction;
                    value = stack.popSized();
                    verifyType(executionContext, value, getTypeFromOpcode(opcode));
                    locals.put(varInsnNode.var, value);
                    break;
                case Opcodes.IASTORE:
                case Opcodes.LASTORE:
                case Opcodes.FASTORE:
                case Opcodes.DASTORE:
                case Opcodes.AASTORE:
                case Opcodes.BASTORE:
                case Opcodes.CASTORE:
                case Opcodes.SASTORE:
                    value = stack.popSized();
                    index = stack.pop(StackInt.class);
                    array = stack.pop(StackObject.class);
                    //TODO: Type checks
                    arrayElements = ((ArrayObject) array.value()).getElements();
                    if (index.value() < 0 || index.value() >= arrayElements.length) {
                        result = ExceptionUtils.newException(executionManager, executionContext, "java/lang/ArrayIndexOutOfBoundsException", "Index: " + index.value() + ", Length: " + arrayElements.length);
                    } else {
                        if (opcode == Opcodes.BASTORE) value = new StackInt((byte) ((StackInt) value).value());
                        else if (opcode == Opcodes.CASTORE) value = new StackInt((char) ((StackInt) value).value());
                        else if (opcode == Opcodes.SASTORE) value = new StackInt((short) ((StackInt) value).value());
                        ((ArrayObject) array.value()).getElements()[index.value()] = value;
                    }
                    break;
                case Opcodes.POP:
                    stack.pop();
                    break;
                case Opcodes.POP2:
                    stack.pop();
                    stack.pop();
                    break;
                case Opcodes.DUP:
                    stack.dup();
                    break;
                case Opcodes.DUP_X1:
                    stack.dupX1();
                    break;
                case Opcodes.DUP_X2:
                    stack.dupX2();
                    break;
                case Opcodes.DUP2:
                    stack.dup2();
                    break;
                case Opcodes.DUP2_X1:
                    stack.dup2X1();
                    break;
                case Opcodes.DUP2_X2:
                    stack.dup2X2();
                    break;
                case Opcodes.SWAP:
                    stack.swap();
                    break;
                case Opcodes.IADD:
                    StackInt int1 = stack.pop(StackInt.class);
                    StackInt int2 = stack.pop(StackInt.class);
                    stack.push(new StackInt(int2.value() + int1.value()));
                    break;
                case Opcodes.LADD:
                    StackLong long1 = stack.pop(StackLong.class);
                    StackLong long2 = stack.pop(StackLong.class);
                    stack.push(new StackLong(long2.value() + long1.value()));
                    break;
                case Opcodes.FADD:
                    StackFloat float1 = stack.pop(StackFloat.class);
                    StackFloat float2 = stack.pop(StackFloat.class);
                    stack.push(new StackFloat(float2.value() + float1.value()));
                    break;
                case Opcodes.DADD:
                    StackDouble double1 = stack.pop(StackDouble.class);
                    StackDouble double2 = stack.pop(StackDouble.class);
                    stack.push(new StackDouble(double2.value() + double1.value()));
                    break;
                case Opcodes.ISUB:
                    int1 = stack.pop(StackInt.class);
                    int2 = stack.pop(StackInt.class);
                    stack.push(new StackInt(int2.value() - int1.value()));
                    break;
                case Opcodes.LSUB:
                    long1 = stack.pop(StackLong.class);
                    long2 = stack.pop(StackLong.class);
                    stack.push(new StackLong(long2.value() - long1.value()));
                    break;
                case Opcodes.FSUB:
                    float1 = stack.pop(StackFloat.class);
                    float2 = stack.pop(StackFloat.class);
                    stack.push(new StackFloat(float2.value() - float1.value()));
                    break;
                case Opcodes.DSUB:
                    double1 = stack.pop(StackDouble.class);
                    double2 = stack.pop(StackDouble.class);
                    stack.push(new StackDouble(double2.value() - double1.value()));
                    break;
                case Opcodes.IMUL:
                    int1 = stack.pop(StackInt.class);
                    int2 = stack.pop(StackInt.class);
                    stack.push(new StackInt(int2.value() * int1.value()));
                    break;
                case Opcodes.LMUL:
                    long1 = stack.pop(StackLong.class);
                    long2 = stack.pop(StackLong.class);
                    stack.push(new StackLong(long2.value() * long1.value()));
                    break;
                case Opcodes.FMUL:
                    float1 = stack.pop(StackFloat.class);
                    float2 = stack.pop(StackFloat.class);
                    stack.push(new StackFloat(float2.value() * float1.value()));
                    break;
                case Opcodes.DMUL:
                    double1 = stack.pop(StackDouble.class);
                    double2 = stack.pop(StackDouble.class);
                    stack.push(new StackDouble(double2.value() * double1.value()));
                    break;
                case Opcodes.IDIV:
                    int1 = stack.pop(StackInt.class);
                    int2 = stack.pop(StackInt.class);
                    stack.push(new StackInt(int2.value() / int1.value()));
                    break;
                case Opcodes.LDIV:
                    long1 = stack.pop(StackLong.class);
                    long2 = stack.pop(StackLong.class);
                    stack.push(new StackLong(long2.value() / long1.value()));
                    break;
                case Opcodes.FDIV:
                    float1 = stack.pop(StackFloat.class);
                    float2 = stack.pop(StackFloat.class);
                    stack.push(new StackFloat(float2.value() / float1.value()));
                    break;
                case Opcodes.DDIV:
                    double1 = stack.pop(StackDouble.class);
                    double2 = stack.pop(StackDouble.class);
                    stack.push(new StackDouble(double2.value() / double1.value()));
                    break;
                case Opcodes.IREM:
                    int1 = stack.pop(StackInt.class);
                    int2 = stack.pop(StackInt.class);
                    stack.push(new StackInt(int2.value() % int1.value()));
                    break;
                case Opcodes.LREM:
                    long1 = stack.pop(StackLong.class);
                    long2 = stack.pop(StackLong.class);
                    stack.push(new StackLong(long2.value() % long1.value()));
                    break;
                case Opcodes.FREM:
                    float1 = stack.pop(StackFloat.class);
                    float2 = stack.pop(StackFloat.class);
                    stack.push(new StackFloat(float2.value() % float1.value()));
                    break;
                case Opcodes.DREM:
                    double1 = stack.pop(StackDouble.class);
                    double2 = stack.pop(StackDouble.class);
                    stack.push(new StackDouble(double2.value() % double1.value()));
                    break;
                case Opcodes.INEG:
                    int1 = stack.pop(StackInt.class);
                    stack.push(new StackInt(-int1.value()));
                    break;
                case Opcodes.LNEG:
                    long1 = stack.pop(StackLong.class);
                    stack.push(new StackLong(-long1.value()));
                    break;
                case Opcodes.FNEG:
                    float1 = stack.pop(StackFloat.class);
                    stack.push(new StackFloat(-float1.value()));
                    break;
                case Opcodes.DNEG:
                    double1 = stack.pop(StackDouble.class);
                    stack.push(new StackDouble(-double1.value()));
                    break;
                case Opcodes.ISHL:
                    int1 = stack.pop(StackInt.class);
                    int2 = stack.pop(StackInt.class);
                    stack.push(new StackInt(int2.value() << int1.value()));
                    break;
                case Opcodes.LSHL:
                    int1 = stack.pop(StackInt.class);
                    long1 = stack.pop(StackLong.class);
                    stack.push(new StackLong(long1.value() << int1.value()));
                    break;
                case Opcodes.ISHR:
                    int1 = stack.pop(StackInt.class);
                    int2 = stack.pop(StackInt.class);
                    stack.push(new StackInt(int2.value() >> int1.value()));
                    break;
                case Opcodes.LSHR:
                    int1 = stack.pop(StackInt.class);
                    long1 = stack.pop(StackLong.class);
                    stack.push(new StackLong(long1.value() >> int1.value()));
                    break;
                case Opcodes.IUSHR:
                    int1 = stack.pop(StackInt.class);
                    int2 = stack.pop(StackInt.class);
                    stack.push(new StackInt(int2.value() >>> int1.value()));
                    break;
                case Opcodes.LUSHR:
                    int1 = stack.pop(StackInt.class);
                    long1 = stack.pop(StackLong.class);
                    stack.push(new StackLong(long1.value() >>> int1.value()));
                    break;
                case Opcodes.IAND:
                    int1 = stack.pop(StackInt.class);
                    int2 = stack.pop(StackInt.class);
                    stack.push(new StackInt(int2.value() & int1.value()));
                    break;
                case Opcodes.LAND:
                    long1 = stack.pop(StackLong.class);
                    long2 = stack.pop(StackLong.class);
                    stack.push(new StackLong(long2.value() & long1.value()));
                    break;
                case Opcodes.IOR:
                    int1 = stack.pop(StackInt.class);
                    int2 = stack.pop(StackInt.class);
                    stack.push(new StackInt(int2.value() | int1.value()));
                    break;
                case Opcodes.LOR:
                    long1 = stack.pop(StackLong.class);
                    long2 = stack.pop(StackLong.class);
                    stack.push(new StackLong(long2.value() | long1.value()));
                    break;
                case Opcodes.IXOR:
                    int1 = stack.pop(StackInt.class);
                    int2 = stack.pop(StackInt.class);
                    stack.push(new StackInt(int2.value() ^ int1.value()));
                    break;
                case Opcodes.LXOR:
                    long1 = stack.pop(StackLong.class);
                    long2 = stack.pop(StackLong.class);
                    stack.push(new StackLong(long2.value() ^ long1.value()));
                    break;
                case Opcodes.IINC:
                    IincInsnNode iincInsnNode = (IincInsnNode) currentInstruction;
                    StackElement local = locals.get(iincInsnNode.var);
                    verifyType(executionContext, local, StackInt.class);
                    locals.put(iincInsnNode.var, new StackInt(((StackInt) local).value() + iincInsnNode.incr));
                    break;
                case Opcodes.I2L:
                    int1 = stack.pop(StackInt.class);
                    stack.push(new StackLong(int1.value()));
                    break;
                case Opcodes.I2F:
                    int1 = stack.pop(StackInt.class);
                    stack.push(new StackFloat(int1.value()));
                    break;
                case Opcodes.I2D:
                    int1 = stack.pop(StackInt.class);
                    stack.push(new StackDouble(int1.value()));
                    break;
                case Opcodes.L2I:
                    long1 = stack.pop(StackLong.class);
                    stack.push(new StackInt((int) long1.value()));
                    break;
                case Opcodes.L2F:
                    long1 = stack.pop(StackLong.class);
                    stack.push(new StackFloat(long1.value()));
                    break;
                case Opcodes.L2D:
                    long1 = stack.pop(StackLong.class);
                    stack.push(new StackDouble(long1.value()));
                    break;
                case Opcodes.F2I:
                    float1 = stack.pop(StackFloat.class);
                    stack.push(new StackInt((int) float1.value()));
                    break;
                case Opcodes.F2L:
                    float1 = stack.pop(StackFloat.class);
                    stack.push(new StackLong((long) float1.value()));
                    break;
                case Opcodes.F2D:
                    float1 = stack.pop(StackFloat.class);
                    stack.push(new StackDouble(float1.value()));
                    break;
                case Opcodes.D2I:
                    double1 = stack.pop(StackDouble.class);
                    stack.push(new StackInt((int) double1.value()));
                    break;
                case Opcodes.D2L:
                    double1 = stack.pop(StackDouble.class);
                    stack.push(new StackLong((long) double1.value()));
                    break;
                case Opcodes.D2F:
                    double1 = stack.pop(StackDouble.class);
                    stack.push(new StackFloat((float) double1.value()));
                    break;
                case Opcodes.I2B:
                    int1 = stack.pop(StackInt.class);
                    stack.push(new StackInt((byte) int1.value()));
                    break;
                case Opcodes.I2C:
                    int1 = stack.pop(StackInt.class);
                    stack.push(new StackInt((char) int1.value()));
                    break;
                case Opcodes.I2S:
                    int1 = stack.pop(StackInt.class);
                    stack.push(new StackInt((short) int1.value()));
                    break;
                case Opcodes.LCMP:
                    long1 = stack.pop(StackLong.class);
                    long2 = stack.pop(StackLong.class);
                    stack.push(new StackInt(Long.compare(long2.value(), long1.value())));
                    break;
                case Opcodes.FCMPL:
                    float1 = stack.pop(StackFloat.class);
                    float2 = stack.pop(StackFloat.class);
                    if (Float.isNaN(float1.value()) || Float.isNaN(float2.value())) stack.push(new StackInt(-1));
                    stack.push(new StackInt(Float.compare(float2.value(), float1.value())));
                    break;
                case Opcodes.FCMPG:
                    float1 = stack.pop(StackFloat.class);
                    float2 = stack.pop(StackFloat.class);
                    if (Float.isNaN(float1.value()) || Float.isNaN(float2.value())) stack.push(new StackInt(1));
                    stack.push(new StackInt(Float.compare(float2.value(), float1.value())));
                    break;
                case Opcodes.DCMPL:
                    double1 = stack.pop(StackDouble.class);
                    double2 = stack.pop(StackDouble.class);
                    if (Double.isNaN(double1.value()) || Double.isNaN(double2.value())) stack.push(new StackInt(-1));
                    stack.push(new StackInt(Double.compare(double2.value(), double1.value())));
                    break;
                case Opcodes.DCMPG:
                    double1 = stack.pop(StackDouble.class);
                    double2 = stack.pop(StackDouble.class);
                    if (Double.isNaN(double1.value()) || Double.isNaN(double2.value())) stack.push(new StackInt(1));
                    stack.push(new StackInt(Double.compare(double2.value(), double1.value())));
                    break;
                case Opcodes.IFEQ:
                    JumpInsnNode jumpInsnNode = (JumpInsnNode) currentInstruction;
                    int1 = stack.pop(StackInt.class);
                    if (int1.value() == 0) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IFNE:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    int1 = stack.pop(StackInt.class);
                    if (int1.value() != 0) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IFLT:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    int1 = stack.pop(StackInt.class);
                    if (int1.value() < 0) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IFGE:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    int1 = stack.pop(StackInt.class);
                    if (int1.value() >= 0) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IFGT:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    int1 = stack.pop(StackInt.class);
                    if (int1.value() > 0) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IFLE:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    int1 = stack.pop(StackInt.class);
                    if (int1.value() <= 0) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IF_ICMPEQ:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    int1 = stack.pop(StackInt.class);
                    int2 = stack.pop(StackInt.class);
                    if (int1.value() == int2.value()) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IF_ICMPNE:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    int1 = stack.pop(StackInt.class);
                    int2 = stack.pop(StackInt.class);
                    if (int1.value() != int2.value()) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IF_ICMPLT:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    int1 = stack.pop(StackInt.class);
                    int2 = stack.pop(StackInt.class);
                    if (int2.value() < int1.value()) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IF_ICMPGE:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    int1 = stack.pop(StackInt.class);
                    int2 = stack.pop(StackInt.class);
                    if (int2.value() >= int1.value()) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IF_ICMPGT:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    int1 = stack.pop(StackInt.class);
                    int2 = stack.pop(StackInt.class);
                    if (int2.value() > int1.value()) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IF_ICMPLE:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    int1 = stack.pop(StackInt.class);
                    int2 = stack.pop(StackInt.class);
                    if (int2.value() <= int1.value()) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IF_ACMPEQ:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    StackObject object1 = stack.pop(StackObject.class);
                    StackObject object2 = stack.pop(StackObject.class);
                    if (object1.value() == object2.value()) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IF_ACMPNE:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    object1 = stack.pop(StackObject.class);
                    object2 = stack.pop(StackObject.class);
                    if (object1.value() != object2.value()) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.GOTO:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    currentInstruction = jumpInsnNode.label;
                    break; //Jump
                case Opcodes.JSR:
                    throw new UnsupportedOperationException(currentInstruction.getClass().getSimpleName() + " " + opcode); //TODO
                case Opcodes.RET:
                    throw new UnsupportedOperationException(currentInstruction.getClass().getSimpleName() + " " + opcode); //TODO
                case Opcodes.TABLESWITCH:
                    TableSwitchInsnNode tableSwitchInsnNode = (TableSwitchInsnNode) currentInstruction;
                    int1 = stack.pop(StackInt.class);
                    if (int1.value() >= tableSwitchInsnNode.min && int1.value() <= tableSwitchInsnNode.max) {
                        currentInstruction = tableSwitchInsnNode.labels.get(int1.value() - tableSwitchInsnNode.min);
                    } else {
                        currentInstruction = tableSwitchInsnNode.dflt;
                    }
                    break; //Jump
                case Opcodes.LOOKUPSWITCH:
                    LookupSwitchInsnNode lookupSwitchInsnNode = (LookupSwitchInsnNode) currentInstruction;
                    int1 = stack.pop(StackInt.class);
                    int caseIndex = lookupSwitchInsnNode.keys.indexOf(int1.value());
                    if (caseIndex != -1) {
                        currentInstruction = lookupSwitchInsnNode.labels.get(caseIndex);
                    } else {
                        currentInstruction = lookupSwitchInsnNode.dflt;
                    }
                    break; //Jump
                case Opcodes.IRETURN:
                    result = ExecutionResult.returnValue(stack.pop(StackInt.class));
                    break;
                case Opcodes.LRETURN:
                    result = ExecutionResult.returnValue(stack.pop(StackLong.class));
                    break;
                case Opcodes.FRETURN:
                    result = ExecutionResult.returnValue(stack.pop(StackFloat.class));
                    break;
                case Opcodes.DRETURN:
                    result = ExecutionResult.returnValue(stack.pop(StackDouble.class));
                    break;
                case Opcodes.ARETURN:
                    result = ExecutionResult.returnValue(stack.pop(StackObject.class));
                    break;
                case Opcodes.RETURN:
                    result = ExecutionResult.voidResult();
                    break;
                case Opcodes.GETSTATIC: //TODO: Access checks for all fields and methods
                case Opcodes.PUTSTATIC:
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) currentInstruction;
                    ExecutorClass owner = executionManager.loadClass(executionContext, fieldInsnNode.owner);
                    ExecutorClass.ResolvedField fieldNode = owner.findField(fieldInsnNode.name, fieldInsnNode.desc);
                    if (fieldNode == null) {
                        result = ExceptionUtils.newException(executionManager, executionContext, "java/lang/NoSuchFieldError", fieldInsnNode.name);
                    } else {
                        if (opcode == Opcodes.GETSTATIC) {
                            stack.push(fieldNode.get());
                        } else {
                            value = stack.popSized();
                            verifyType(executionContext, value, ExecutorTypeUtils.typeToStackType(type(fieldNode.field())));
                            fieldNode.set(value);
                        }
                    }
                    break;
                case Opcodes.GETFIELD:
                    fieldInsnNode = (FieldInsnNode) currentInstruction;
                    StackObject object = stack.pop(StackObject.class);
                    if (object == StackObject.NULL) {
                        result = ExceptionUtils.newException(executionManager, executionContext, "java/lang/NullPointerException", "Tried to access field of null object");
                    } else {
                        fieldNode = object.value().getOwner().findField(fieldInsnNode.name, fieldInsnNode.desc);
                        if (fieldNode == null) {
                            result = ExceptionUtils.newException(executionManager, executionContext, "java/lang/NoSuchFieldError", fieldInsnNode.name);
                        } else {
                            stack.push(object.value().getField(fieldNode.field()));
                        }
                    }
                    break;
                case Opcodes.PUTFIELD:
                    fieldInsnNode = (FieldInsnNode) currentInstruction;
                    value = stack.popSized();
                    object = stack.pop(StackObject.class);
                    if (object == StackObject.NULL) {
                        result = ExceptionUtils.newException(executionManager, executionContext, "java/lang/NullPointerException", "Tried to access field of null object");
                    } else {
                        fieldNode = object.value().getOwner().findField(fieldInsnNode.name, fieldInsnNode.desc);
                        if (fieldNode == null) {
                            result = ExceptionUtils.newException(executionManager, executionContext, "java/lang/NoSuchFieldError", fieldInsnNode.name);
                        } else {
                            verifyType(executionContext, value, ExecutorTypeUtils.typeToStackType(type(fieldNode.field())));
                            object.value().setField(fieldNode.field(), value);
                        }
                    }
                    break;
                case Opcodes.INVOKEVIRTUAL:
                case Opcodes.INVOKESPECIAL:
                case Opcodes.INVOKEINTERFACE:
                    MethodInsnNode methodInsnNode = (MethodInsnNode) currentInstruction;
                    Type[] argumentTypes = argumentTypes(methodInsnNode);
                    List<StackElement> stackElements = new ArrayList<>(argumentTypes.length);
                    for (int i = argumentTypes.length - 1; i >= 0; i--) {
                        StackElement argumentType = stack.popSized();
                        verifyType(executionContext, argumentType, ExecutorTypeUtils.typeToStackType(argumentTypes[i]));
                        stackElements.add(0, argumentType);
                    }
                    StackElement ownerElement = stack.pop(StackObject.class);
                    if (ownerElement == StackObject.NULL) {
                        result = ExceptionUtils.newException(executionManager, executionContext, "java/lang/NullPointerException", "Tried to invoke method on null object");
                    } else {
                        ExecutorObject ownerObject = ((StackObject) ownerElement).value();
                        //TODO: Interface checks
                        ExecutorClass.ResolvedMethod methodNode;
                        if (opcode == Opcodes.INVOKESPECIAL) {
                            ExecutorClass ownerClass = executionManager.loadClass(executionContext, methodInsnNode.owner);
                            methodNode = ownerClass.findMethod(methodInsnNode.name, methodInsnNode.desc);
                        } else {
                            methodNode = ownerObject.getOwner().findMethod(methodInsnNode.name, methodInsnNode.desc);
                        }
                        if (methodNode == null) {
                            result = ExceptionUtils.newException(executionManager, executionContext, "java/lang/NoSuchMethodError", methodInsnNode.name);
                        } else if (Modifiers.has(methodNode.method().access, Opcodes.ACC_STATIC)) {
                            result = ExceptionUtils.newException(executionManager, executionContext, "java/lang/IncompatibleClassChangeError", "Expecting non-static method " + methodInsnNode.owner + "." + methodInsnNode.name + methodInsnNode.desc);
                        } else {
                            ExecutionResult invokeResult = Executor.execute(executionManager, executionContext, methodNode.owner(), methodNode.method(), ownerObject, stackElements.toArray(new StackElement[0]));
                            if (invokeResult.hasReturnValue()) {
                                verifyType(executionContext, invokeResult.getReturnValue(), ExecutorTypeUtils.typeToStackType(returnType(methodNode.method())));
                                stack.push(invokeResult.getReturnValue());
                            } else if (invokeResult.hasException()) {
                                result = invokeResult;
                            }
                        }
                    }
                    break;
                case Opcodes.INVOKESTATIC:
                    methodInsnNode = (MethodInsnNode) currentInstruction;
                    argumentTypes = argumentTypes(methodInsnNode);
                    stackElements = new ArrayList<>(argumentTypes.length);
                    for (int i = argumentTypes.length - 1; i >= 0; i--) {
                        StackElement argumentType = stack.popSized();
                        verifyType(executionContext, argumentType, ExecutorTypeUtils.typeToStackType(argumentTypes[i]));
                        stackElements.add(0, argumentType);
                    }
                    ExecutorClass ownerClass = executionManager.loadClass(executionContext, methodInsnNode.owner);
                    ExecutorClass.ResolvedMethod methodNode = ownerClass.findMethod(methodInsnNode.name, methodInsnNode.desc);
                    if (methodNode == null) {
                        result = ExceptionUtils.newException(executionManager, executionContext, "java/lang/NoSuchMethodError", methodInsnNode.name);
                    } else if (!Modifiers.has(methodNode.method().access, Opcodes.ACC_STATIC)) {
                        result = ExceptionUtils.newException(executionManager, executionContext, "java/lang/IncompatibleClassChangeError", "Expecting static method " + methodInsnNode.owner + "." + methodInsnNode.name + methodInsnNode.desc);
                    } else {
                        ExecutionResult invokeResult = Executor.execute(executionManager, executionContext, methodNode.owner(), methodNode.method(), null, stackElements.toArray(new StackElement[0]));
                        if (invokeResult.hasReturnValue()) {
                            verifyType(executionContext, invokeResult.getReturnValue(), ExecutorTypeUtils.typeToStackType(returnType(methodNode.method())));
                            stack.push(invokeResult.getReturnValue());
                        } else if (invokeResult.hasException()) {
                            result = invokeResult;
                        }
                    }
                    break;
                case Opcodes.INVOKEDYNAMIC:
                    throw new UnsupportedOperationException(currentInstruction.getClass().getSimpleName() + " " + opcode); //TODO
                case Opcodes.NEW:
                    TypeInsnNode typeInsnNode = (TypeInsnNode) currentInstruction;
                    ExecutorClass newClass = executionManager.loadClass(executionContext, typeInsnNode.desc);
                    ExecutorObject newObject = executionManager.instantiate(executionContext, newClass);
                    stack.push(new StackObject(newObject));
                    break;
                case Opcodes.NEWARRAY:
                    IntInsnNode intInsnNode = (IntInsnNode) currentInstruction;
                    int length = stack.pop(StackInt.class).value();
                    switch (intInsnNode.operand) {
                        case Opcodes.T_BOOLEAN -> stack.push(ExecutorTypeUtils.newArray(executionManager, executionContext, type(boolean[].class), length, () -> new StackInt(0)));
                        case Opcodes.T_BYTE -> stack.push(ExecutorTypeUtils.newArray(executionManager, executionContext, type(byte[].class), length, () -> new StackInt(0)));
                        case Opcodes.T_CHAR -> stack.push(ExecutorTypeUtils.newArray(executionManager, executionContext, type(char[].class), length, () -> new StackInt(0)));
                        case Opcodes.T_SHORT -> stack.push(ExecutorTypeUtils.newArray(executionManager, executionContext, type(short[].class), length, () -> new StackInt(0)));
                        case Opcodes.T_INT -> stack.push(ExecutorTypeUtils.newArray(executionManager, executionContext, type(int[].class), length, () -> new StackInt(0)));
                        case Opcodes.T_LONG -> stack.push(ExecutorTypeUtils.newArray(executionManager, executionContext, type(long[].class), length, () -> new StackLong(0)));
                        case Opcodes.T_FLOAT -> stack.push(ExecutorTypeUtils.newArray(executionManager, executionContext, type(float[].class), length, () -> new StackFloat(0)));
                        case Opcodes.T_DOUBLE -> stack.push(ExecutorTypeUtils.newArray(executionManager, executionContext, type(double[].class), length, () -> new StackDouble(0)));
                        default -> throw new ExecutorException(executionContext, "Unknown array type: " + intInsnNode.operand);
                    }
                    break;
                case Opcodes.ANEWARRAY:
                    typeInsnNode = (TypeInsnNode) currentInstruction;
                    length = stack.pop(StackInt.class).value();
                    newClass = executionManager.loadClass(executionContext, "[L" + typeInsnNode.desc + ";");
                    stack.push(new StackObject(executionManager.instantiateArray(executionContext, newClass, length, () -> StackObject.NULL)));
                    break;
                case Opcodes.ARRAYLENGTH:
                    array = stack.pop(StackObject.class);
                    if (!(array.value() instanceof ArrayObject)) {
                        throw new ExecutorException(executionContext, "Expected array but got " + array.getClass().getSimpleName());
                    }
                    stack.push(new StackInt(((ArrayObject) array.value()).getElements().length));
                    break;
                case Opcodes.ATHROW:
                    object = stack.pop(StackObject.class);
                    if (object == StackObject.NULL) {
                        result = ExceptionUtils.newException(executionManager, executionContext, "java/lang/NullPointerException");
                    } else if (!object.value().getOwner().isInstance("java/lang/Throwable")) {
                        throw new ExecutorException(executionContext, "Expected throwable but got " + object.value().getOwner().getClassNode().name);
                    } else {
                        result = ExecutionResult.exception(object.value());
                    }
                    break;
                case Opcodes.CHECKCAST:
                    //TODO: Type checks?
                    break;
                case Opcodes.INSTANCEOF:
                    typeInsnNode = (TypeInsnNode) currentInstruction;
                    object = stack.pop(StackObject.class);
                    if (object == StackObject.NULL) {
                        stack.push(new StackInt(0));
                    } else {
                        boolean isInstance = object.value().getOwner().isInstance(typeInsnNode.desc);
                        stack.push(new StackInt(isInstance ? 1 : 0));
                    }
                    break;
                case Opcodes.MONITORENTER:
                    throw new UnsupportedOperationException(currentInstruction.getClass().getSimpleName() + " " + opcode); //TODO
                case Opcodes.MONITOREXIT:
                    throw new UnsupportedOperationException(currentInstruction.getClass().getSimpleName() + " " + opcode); //TODO
                case Opcodes.MULTIANEWARRAY:
                    throw new UnsupportedOperationException(currentInstruction.getClass().getSimpleName() + " " + opcode); //TODO
                case Opcodes.IFNULL:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    object = stack.pop(StackObject.class);
                    if (object == StackObject.NULL) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IFNONNULL:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    object = stack.pop(StackObject.class);
                    if (object != StackObject.NULL) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case -1:
                    if (currentInstruction instanceof LineNumberNode) {
                        stackFrame.setLineNumber(((LineNumberNode) currentInstruction).line);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unknown opcode: " + opcode);
            }

            if (result != null) {
                if (result.hasException()) {
                    TryCatchBlockNode matchingTryCatchBlock = getMatchingTryCatchBlock(executionManager, executionContext, currentMethod, currentInstruction, result.getException().getOwner());
                    if (matchingTryCatchBlock == null) {
                        //If no try catch block was found, throw the exception to the caller
                        break;
                    } else {
                        //A try catch block was found, jump to the handler, clear the stack and push the exception
                        currentInstruction = matchingTryCatchBlock.handler; //Jump
                        stack.clear();
                        stack.push(new StackObject(result.getException()));
                    }
                } else {
                    break;
                }
            }

            currentInstruction = currentInstruction.getNext();
        }
        return result;
    }

    private static Class<? extends StackElement> getTypeFromOpcode(final int opcode) {
        switch (opcode) {
            case Opcodes.ILOAD:
            case Opcodes.ISTORE:
                return StackInt.class;
            case Opcodes.LLOAD:
            case Opcodes.LSTORE:
                return StackLong.class;
            case Opcodes.FLOAD:
            case Opcodes.FSTORE:
                return StackFloat.class;
            case Opcodes.DLOAD:
            case Opcodes.DSTORE:
                return StackDouble.class;
            case Opcodes.ALOAD:
            case Opcodes.ASTORE:
                return StackObject.class;
        }
        throw new IllegalStateException("Unknown opcode: " + opcode);
    }

    private static Class<? extends StackElement> getArrayTypeFromOpcode(final int opcode) {
        switch (opcode) {
            case Opcodes.IALOAD:
            case Opcodes.IASTORE:
            case Opcodes.BALOAD:
            case Opcodes.BASTORE:
            case Opcodes.CALOAD:
            case Opcodes.CASTORE:
            case Opcodes.SALOAD:
            case Opcodes.SASTORE:
                return StackInt.class;
            case Opcodes.LALOAD:
            case Opcodes.LASTORE:
                return StackLong.class;
            case Opcodes.FALOAD:
            case Opcodes.FASTORE:
                return StackFloat.class;
            case Opcodes.DALOAD:
            case Opcodes.DASTORE:
                return StackDouble.class;
            case Opcodes.AALOAD:
            case Opcodes.AASTORE:
                return StackObject.class;
        }
        throw new IllegalStateException("Unknown opcode: " + opcode);
    }

    private static void verifyType(final ExecutionContext executionContext, final StackElement element, final Class<? extends StackElement> expectedType) {
        if (element == null) {
            throw new ExecutorException(executionContext, "Tried to load empty " + element.getClass().getSimpleName() + " value from stack");
        }
        if (!expectedType.isInstance(element)) {
            throw new ExecutorException(executionContext, "Expected " + expectedType.getName() + " but got " + element.getClass().getSimpleName());
        }
    }

    @Nullable
    private static TryCatchBlockNode getMatchingTryCatchBlock(final ExecutionManager executionManager, final ExecutionContext executionContext, final MethodNode method, final AbstractInsnNode currentInstruction, final ExecutorClass exceptionClass) {
        int index = method.instructions.indexOf(currentInstruction);
        for (TryCatchBlockNode tryCatchBlock : method.tryCatchBlocks) {
            int start = method.instructions.indexOf(tryCatchBlock.start);
            int end = method.instructions.indexOf(tryCatchBlock.end);
            if (start < index && end > index) {
                if (tryCatchBlock.type == null) {
                    return tryCatchBlock;
                } else {
                    ExecutorClass catchClass = executionManager.loadClass(executionContext, tryCatchBlock.type);
                    if (exceptionClass.isInstance(catchClass.getClassNode().name)) {
                        return tryCatchBlock;
                    }
                }
            }
        }
        return null;
    }

}
