package net.lenni0451.minijvm.execution;

import net.lenni0451.commons.asm.Modifiers;
import net.lenni0451.minijvm.ExecutionContext;
import net.lenni0451.minijvm.ExecutionManager;
import net.lenni0451.minijvm.exception.ExecutorException;
import net.lenni0451.minijvm.object.ExecutorClass;
import net.lenni0451.minijvm.object.ExecutorObject;
import net.lenni0451.minijvm.object.types.ArrayObject;
import net.lenni0451.minijvm.stack.*;
import net.lenni0451.minijvm.utils.ExceptionUtils;
import net.lenni0451.minijvm.utils.ExecutorStack;
import net.lenni0451.minijvm.utils.ExecutorTypeUtils;
import net.lenni0451.minijvm.utils.Types;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public class JVMMethodExecutor implements MethodExecutor {

    @Override
    public ExecutionResult execute(ExecutionContext context, ExecutorClass currentClass, MethodNode currentMethod, ExecutorObject instance, StackElement[] arguments) {
        ExecutionManager manager = context.getExecutionManager();
        boolean isStatic = Modifiers.has(currentMethod.access, Opcodes.ACC_STATIC);
        StackElement[] locals = new StackElement[currentMethod.maxLocals];
        {
            if (!isStatic) locals[0] = new StackObject(instance);
            int currentIndex = isStatic ? 0 : 1;
            for (StackElement argument : arguments) {
                locals[currentIndex] = argument;
                currentIndex += argument.size();
            }
        }
        ExecutionContext.StackFrame stackFrame = context.getCurrentStackFrame();
        ExecutorStack stack = new ExecutorStack(context, currentMethod.maxStack);
        AbstractInsnNode currentInstruction = currentMethod.instructions.getFirst();
        ExecutionResult result = null;
        while (true) {
            if (ExecutionManager.DEBUG) {
                System.out.println("  " + currentInstruction.getClass().getSimpleName() + " " + currentInstruction.getOpcode() + " -> " + Arrays.stream(stack.getStack()).map(StackElement::toString).collect(Collectors.joining(", ")));
            }
            int opcode = currentInstruction.getOpcode();
            switch (opcode) {
                case Opcodes.NOP:
                    break;
                case Opcodes.ACONST_NULL:
                    stack.pushSized(StackObject.NULL);
                    break;
                case Opcodes.ICONST_M1:
                    stack.pushSized(StackInt.MINUS1);
                    break;
                case Opcodes.ICONST_0:
                    stack.pushSized(StackInt.ZERO);
                    break;
                case Opcodes.ICONST_1:
                    stack.pushSized(StackInt.ONE);
                    break;
                case Opcodes.ICONST_2:
                    stack.pushSized(StackInt.TWO);
                    break;
                case Opcodes.ICONST_3:
                    stack.pushSized(StackInt.THREE);
                    break;
                case Opcodes.ICONST_4:
                    stack.pushSized(StackInt.FOUR);
                    break;
                case Opcodes.ICONST_5:
                    stack.pushSized(StackInt.FIVE);
                    break;
                case Opcodes.LCONST_0:
                    stack.pushSized(StackLong.ZERO);
                    break;
                case Opcodes.LCONST_1:
                    stack.pushSized(StackLong.ONE);
                    break;
                case Opcodes.FCONST_0:
                    stack.pushSized(StackFloat.ZERO);
                    break;
                case Opcodes.FCONST_1:
                    stack.pushSized(StackFloat.ONE);
                    break;
                case Opcodes.FCONST_2:
                    stack.pushSized(StackFloat.TWO);
                    break;
                case Opcodes.DCONST_0:
                    stack.pushSized(StackDouble.ZERO);
                    break;
                case Opcodes.DCONST_1:
                    stack.pushSized(StackDouble.ONE);
                    break;
                case Opcodes.BIPUSH:
                case Opcodes.SIPUSH:
                    IntInsnNode intInsnNode1 = (IntInsnNode) currentInstruction;
                    stack.pushSized(new StackInt(intInsnNode1.operand));
                    break;
                case Opcodes.LDC:
                    LdcInsnNode ldcInsnNode = (LdcInsnNode) currentInstruction;
                    stack.pushSized(ExecutorTypeUtils.parse(context, ldcInsnNode.cst));
                    break;
                case Opcodes.ILOAD:
                case Opcodes.LLOAD:
                case Opcodes.FLOAD:
                case Opcodes.DLOAD:
                case Opcodes.ALOAD:
                    VarInsnNode varInsnNode = (VarInsnNode) currentInstruction;
                    StackElement value = locals[varInsnNode.var];
                    verifyType(context, value, getTypeFromOpcode(opcode));
                    stack.pushSized(value);
                    break;
                case Opcodes.IALOAD:
                case Opcodes.LALOAD:
                case Opcodes.FALOAD:
                case Opcodes.DALOAD:
                case Opcodes.AALOAD:
                case Opcodes.BALOAD:
                case Opcodes.CALOAD:
                case Opcodes.SALOAD:
                    StackInt index = stack.popSized(StackInt.class);
                    StackObject array = stack.popSized(StackObject.class);
                    StackElement[] arrayElements = ((ArrayObject) array.value()).getElements();
                    //TODO: Type checks
                    if (index.value() < 0 || index.value() >= arrayElements.length) {
                        result = ExceptionUtils.newException(context, Types.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION, "Index: " + index.value() + ", Length: " + arrayElements.length);
                    } else {
                        value = arrayElements[index.value()];
                        if (opcode == Opcodes.BALOAD) value = new StackInt((byte) ((StackInt) value).value());
                        else if (opcode == Opcodes.CALOAD) value = new StackInt((char) ((StackInt) value).value());
                        else if (opcode == Opcodes.SALOAD) value = new StackInt((short) ((StackInt) value).value());
                        stack.pushSized(value);
                    }
                    break;
                case Opcodes.ISTORE:
                case Opcodes.LSTORE:
                case Opcodes.FSTORE:
                case Opcodes.DSTORE:
                case Opcodes.ASTORE:
                    varInsnNode = (VarInsnNode) currentInstruction;
                    value = stack.popSized();
                    verifyType(context, value, getTypeFromOpcode(opcode));
                    locals[varInsnNode.var] = value;
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
                    index = stack.popSized(StackInt.class);
                    array = stack.popSized(StackObject.class);
                    //TODO: Type checks
                    arrayElements = ((ArrayObject) array.value()).getElements();
                    if (index.value() < 0 || index.value() >= arrayElements.length) {
                        result = ExceptionUtils.newException(context, Types.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION, "Index: " + index.value() + ", Length: " + arrayElements.length);
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
                    StackInt int1 = stack.popSized(StackInt.class);
                    StackInt int2 = stack.popSized(StackInt.class);
                    stack.pushSized(new StackInt(int2.value() + int1.value()));
                    break;
                case Opcodes.LADD:
                    StackLong long1 = stack.popSized(StackLong.class);
                    StackLong long2 = stack.popSized(StackLong.class);
                    stack.pushSized(new StackLong(long2.value() + long1.value()));
                    break;
                case Opcodes.FADD:
                    StackFloat float1 = stack.popSized(StackFloat.class);
                    StackFloat float2 = stack.popSized(StackFloat.class);
                    stack.pushSized(new StackFloat(float2.value() + float1.value()));
                    break;
                case Opcodes.DADD:
                    StackDouble double1 = stack.popSized(StackDouble.class);
                    StackDouble double2 = stack.popSized(StackDouble.class);
                    stack.pushSized(new StackDouble(double2.value() + double1.value()));
                    break;
                case Opcodes.ISUB:
                    int1 = stack.popSized(StackInt.class);
                    int2 = stack.popSized(StackInt.class);
                    stack.pushSized(new StackInt(int2.value() - int1.value()));
                    break;
                case Opcodes.LSUB:
                    long1 = stack.popSized(StackLong.class);
                    long2 = stack.popSized(StackLong.class);
                    stack.pushSized(new StackLong(long2.value() - long1.value()));
                    break;
                case Opcodes.FSUB:
                    float1 = stack.popSized(StackFloat.class);
                    float2 = stack.popSized(StackFloat.class);
                    stack.pushSized(new StackFloat(float2.value() - float1.value()));
                    break;
                case Opcodes.DSUB:
                    double1 = stack.popSized(StackDouble.class);
                    double2 = stack.popSized(StackDouble.class);
                    stack.pushSized(new StackDouble(double2.value() - double1.value()));
                    break;
                case Opcodes.IMUL:
                    int1 = stack.popSized(StackInt.class);
                    int2 = stack.popSized(StackInt.class);
                    stack.pushSized(new StackInt(int2.value() * int1.value()));
                    break;
                case Opcodes.LMUL:
                    long1 = stack.popSized(StackLong.class);
                    long2 = stack.popSized(StackLong.class);
                    stack.pushSized(new StackLong(long2.value() * long1.value()));
                    break;
                case Opcodes.FMUL:
                    float1 = stack.popSized(StackFloat.class);
                    float2 = stack.popSized(StackFloat.class);
                    stack.pushSized(new StackFloat(float2.value() * float1.value()));
                    break;
                case Opcodes.DMUL:
                    double1 = stack.popSized(StackDouble.class);
                    double2 = stack.popSized(StackDouble.class);
                    stack.pushSized(new StackDouble(double2.value() * double1.value()));
                    break;
                case Opcodes.IDIV:
                    int1 = stack.popSized(StackInt.class);
                    int2 = stack.popSized(StackInt.class);
                    stack.pushSized(new StackInt(int2.value() / int1.value()));
                    break;
                case Opcodes.LDIV:
                    long1 = stack.popSized(StackLong.class);
                    long2 = stack.popSized(StackLong.class);
                    stack.pushSized(new StackLong(long2.value() / long1.value()));
                    break;
                case Opcodes.FDIV:
                    float1 = stack.popSized(StackFloat.class);
                    float2 = stack.popSized(StackFloat.class);
                    stack.pushSized(new StackFloat(float2.value() / float1.value()));
                    break;
                case Opcodes.DDIV:
                    double1 = stack.popSized(StackDouble.class);
                    double2 = stack.popSized(StackDouble.class);
                    stack.pushSized(new StackDouble(double2.value() / double1.value()));
                    break;
                case Opcodes.IREM:
                    int1 = stack.popSized(StackInt.class);
                    int2 = stack.popSized(StackInt.class);
                    stack.pushSized(new StackInt(int2.value() % int1.value()));
                    break;
                case Opcodes.LREM:
                    long1 = stack.popSized(StackLong.class);
                    long2 = stack.popSized(StackLong.class);
                    stack.pushSized(new StackLong(long2.value() % long1.value()));
                    break;
                case Opcodes.FREM:
                    float1 = stack.popSized(StackFloat.class);
                    float2 = stack.popSized(StackFloat.class);
                    stack.pushSized(new StackFloat(float2.value() % float1.value()));
                    break;
                case Opcodes.DREM:
                    double1 = stack.popSized(StackDouble.class);
                    double2 = stack.popSized(StackDouble.class);
                    stack.pushSized(new StackDouble(double2.value() % double1.value()));
                    break;
                case Opcodes.INEG:
                    int1 = stack.popSized(StackInt.class);
                    stack.pushSized(new StackInt(-int1.value()));
                    break;
                case Opcodes.LNEG:
                    long1 = stack.popSized(StackLong.class);
                    stack.pushSized(new StackLong(-long1.value()));
                    break;
                case Opcodes.FNEG:
                    float1 = stack.popSized(StackFloat.class);
                    stack.pushSized(new StackFloat(-float1.value()));
                    break;
                case Opcodes.DNEG:
                    double1 = stack.popSized(StackDouble.class);
                    stack.pushSized(new StackDouble(-double1.value()));
                    break;
                case Opcodes.ISHL:
                    int1 = stack.popSized(StackInt.class);
                    int2 = stack.popSized(StackInt.class);
                    stack.pushSized(new StackInt(int2.value() << int1.value()));
                    break;
                case Opcodes.LSHL:
                    int1 = stack.popSized(StackInt.class);
                    long1 = stack.popSized(StackLong.class);
                    stack.pushSized(new StackLong(long1.value() << int1.value()));
                    break;
                case Opcodes.ISHR:
                    int1 = stack.popSized(StackInt.class);
                    int2 = stack.popSized(StackInt.class);
                    stack.pushSized(new StackInt(int2.value() >> int1.value()));
                    break;
                case Opcodes.LSHR:
                    int1 = stack.popSized(StackInt.class);
                    long1 = stack.popSized(StackLong.class);
                    stack.pushSized(new StackLong(long1.value() >> int1.value()));
                    break;
                case Opcodes.IUSHR:
                    int1 = stack.popSized(StackInt.class);
                    int2 = stack.popSized(StackInt.class);
                    stack.pushSized(new StackInt(int2.value() >>> int1.value()));
                    break;
                case Opcodes.LUSHR:
                    int1 = stack.popSized(StackInt.class);
                    long1 = stack.popSized(StackLong.class);
                    stack.pushSized(new StackLong(long1.value() >>> int1.value()));
                    break;
                case Opcodes.IAND:
                    int1 = stack.popSized(StackInt.class);
                    int2 = stack.popSized(StackInt.class);
                    stack.pushSized(new StackInt(int2.value() & int1.value()));
                    break;
                case Opcodes.LAND:
                    long1 = stack.popSized(StackLong.class);
                    long2 = stack.popSized(StackLong.class);
                    stack.pushSized(new StackLong(long2.value() & long1.value()));
                    break;
                case Opcodes.IOR:
                    int1 = stack.popSized(StackInt.class);
                    int2 = stack.popSized(StackInt.class);
                    stack.pushSized(new StackInt(int2.value() | int1.value()));
                    break;
                case Opcodes.LOR:
                    long1 = stack.popSized(StackLong.class);
                    long2 = stack.popSized(StackLong.class);
                    stack.pushSized(new StackLong(long2.value() | long1.value()));
                    break;
                case Opcodes.IXOR:
                    int1 = stack.popSized(StackInt.class);
                    int2 = stack.popSized(StackInt.class);
                    stack.pushSized(new StackInt(int2.value() ^ int1.value()));
                    break;
                case Opcodes.LXOR:
                    long1 = stack.popSized(StackLong.class);
                    long2 = stack.popSized(StackLong.class);
                    stack.pushSized(new StackLong(long2.value() ^ long1.value()));
                    break;
                case Opcodes.IINC:
                    IincInsnNode iincInsnNode = (IincInsnNode) currentInstruction;
                    StackElement local = locals[iincInsnNode.var];
                    verifyType(context, local, StackInt.class);
                    locals[iincInsnNode.var] = new StackInt(((StackInt) local).value() + iincInsnNode.incr);
                    break;
                case Opcodes.I2L:
                    int1 = stack.popSized(StackInt.class);
                    stack.pushSized(new StackLong(int1.value()));
                    break;
                case Opcodes.I2F:
                    int1 = stack.popSized(StackInt.class);
                    stack.pushSized(new StackFloat(int1.value()));
                    break;
                case Opcodes.I2D:
                    int1 = stack.popSized(StackInt.class);
                    stack.pushSized(new StackDouble(int1.value()));
                    break;
                case Opcodes.L2I:
                    long1 = stack.popSized(StackLong.class);
                    stack.pushSized(new StackInt((int) long1.value()));
                    break;
                case Opcodes.L2F:
                    long1 = stack.popSized(StackLong.class);
                    stack.pushSized(new StackFloat(long1.value()));
                    break;
                case Opcodes.L2D:
                    long1 = stack.popSized(StackLong.class);
                    stack.pushSized(new StackDouble(long1.value()));
                    break;
                case Opcodes.F2I:
                    float1 = stack.popSized(StackFloat.class);
                    stack.pushSized(new StackInt((int) float1.value()));
                    break;
                case Opcodes.F2L:
                    float1 = stack.popSized(StackFloat.class);
                    stack.pushSized(new StackLong((long) float1.value()));
                    break;
                case Opcodes.F2D:
                    float1 = stack.popSized(StackFloat.class);
                    stack.pushSized(new StackDouble(float1.value()));
                    break;
                case Opcodes.D2I:
                    double1 = stack.popSized(StackDouble.class);
                    stack.pushSized(new StackInt((int) double1.value()));
                    break;
                case Opcodes.D2L:
                    double1 = stack.popSized(StackDouble.class);
                    stack.pushSized(new StackLong((long) double1.value()));
                    break;
                case Opcodes.D2F:
                    double1 = stack.popSized(StackDouble.class);
                    stack.pushSized(new StackFloat((float) double1.value()));
                    break;
                case Opcodes.I2B:
                    int1 = stack.popSized(StackInt.class);
                    stack.pushSized(new StackInt((byte) int1.value()));
                    break;
                case Opcodes.I2C:
                    int1 = stack.popSized(StackInt.class);
                    stack.pushSized(new StackInt((char) int1.value()));
                    break;
                case Opcodes.I2S:
                    int1 = stack.popSized(StackInt.class);
                    stack.pushSized(new StackInt((short) int1.value()));
                    break;
                case Opcodes.LCMP:
                    long1 = stack.popSized(StackLong.class);
                    long2 = stack.popSized(StackLong.class);
                    stack.pushSized(new StackInt(Long.compare(long2.value(), long1.value())));
                    break;
                case Opcodes.FCMPL:
                    float1 = stack.popSized(StackFloat.class);
                    float2 = stack.popSized(StackFloat.class);
                    if (Float.isNaN(float1.value()) || Float.isNaN(float2.value())) stack.pushSized(StackInt.MINUS1);
                    stack.pushSized(new StackInt(Float.compare(float2.value(), float1.value())));
                    break;
                case Opcodes.FCMPG:
                    float1 = stack.popSized(StackFloat.class);
                    float2 = stack.popSized(StackFloat.class);
                    if (Float.isNaN(float1.value()) || Float.isNaN(float2.value())) stack.pushSized(StackInt.ONE);
                    stack.pushSized(new StackInt(Float.compare(float2.value(), float1.value())));
                    break;
                case Opcodes.DCMPL:
                    double1 = stack.popSized(StackDouble.class);
                    double2 = stack.popSized(StackDouble.class);
                    if (Double.isNaN(double1.value()) || Double.isNaN(double2.value())) stack.pushSized(StackInt.MINUS1);
                    stack.pushSized(new StackInt(Double.compare(double2.value(), double1.value())));
                    break;
                case Opcodes.DCMPG:
                    double1 = stack.popSized(StackDouble.class);
                    double2 = stack.popSized(StackDouble.class);
                    if (Double.isNaN(double1.value()) || Double.isNaN(double2.value())) stack.pushSized(StackInt.ONE);
                    stack.pushSized(new StackInt(Double.compare(double2.value(), double1.value())));
                    break;
                case Opcodes.IFEQ:
                    JumpInsnNode jumpInsnNode = (JumpInsnNode) currentInstruction;
                    int1 = stack.popSized(StackInt.class);
                    if (int1.value() == 0) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IFNE:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    int1 = stack.popSized(StackInt.class);
                    if (int1.value() != 0) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IFLT:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    int1 = stack.popSized(StackInt.class);
                    if (int1.value() < 0) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IFGE:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    int1 = stack.popSized(StackInt.class);
                    if (int1.value() >= 0) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IFGT:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    int1 = stack.popSized(StackInt.class);
                    if (int1.value() > 0) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IFLE:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    int1 = stack.popSized(StackInt.class);
                    if (int1.value() <= 0) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IF_ICMPEQ:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    int1 = stack.popSized(StackInt.class);
                    int2 = stack.popSized(StackInt.class);
                    if (int1.value() == int2.value()) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IF_ICMPNE:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    int1 = stack.popSized(StackInt.class);
                    int2 = stack.popSized(StackInt.class);
                    if (int1.value() != int2.value()) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IF_ICMPLT:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    int1 = stack.popSized(StackInt.class);
                    int2 = stack.popSized(StackInt.class);
                    if (int2.value() < int1.value()) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IF_ICMPGE:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    int1 = stack.popSized(StackInt.class);
                    int2 = stack.popSized(StackInt.class);
                    if (int2.value() >= int1.value()) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IF_ICMPGT:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    int1 = stack.popSized(StackInt.class);
                    int2 = stack.popSized(StackInt.class);
                    if (int2.value() > int1.value()) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IF_ICMPLE:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    int1 = stack.popSized(StackInt.class);
                    int2 = stack.popSized(StackInt.class);
                    if (int2.value() <= int1.value()) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IF_ACMPEQ:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    StackObject object1 = stack.popSized(StackObject.class);
                    StackObject object2 = stack.popSized(StackObject.class);
                    if (object1.value() == object2.value()) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IF_ACMPNE:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    object1 = stack.popSized(StackObject.class);
                    object2 = stack.popSized(StackObject.class);
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
                    int1 = stack.popSized(StackInt.class);
                    if (int1.value() >= tableSwitchInsnNode.min && int1.value() <= tableSwitchInsnNode.max) {
                        currentInstruction = tableSwitchInsnNode.labels.get(int1.value() - tableSwitchInsnNode.min);
                    } else {
                        currentInstruction = tableSwitchInsnNode.dflt;
                    }
                    break; //Jump
                case Opcodes.LOOKUPSWITCH:
                    LookupSwitchInsnNode lookupSwitchInsnNode = (LookupSwitchInsnNode) currentInstruction;
                    int1 = stack.popSized(StackInt.class);
                    int caseIndex = lookupSwitchInsnNode.keys.indexOf(int1.value());
                    if (caseIndex != -1) {
                        currentInstruction = lookupSwitchInsnNode.labels.get(caseIndex);
                    } else {
                        currentInstruction = lookupSwitchInsnNode.dflt;
                    }
                    break; //Jump
                case Opcodes.IRETURN:
                    result = ExecutionResult.returnValue(stack.popSized(StackInt.class));
                    break;
                case Opcodes.LRETURN:
                    result = ExecutionResult.returnValue(stack.popSized(StackLong.class));
                    break;
                case Opcodes.FRETURN:
                    result = ExecutionResult.returnValue(stack.popSized(StackFloat.class));
                    break;
                case Opcodes.DRETURN:
                    result = ExecutionResult.returnValue(stack.popSized(StackDouble.class));
                    break;
                case Opcodes.ARETURN:
                    result = ExecutionResult.returnValue(stack.popSized(StackObject.class));
                    break;
                case Opcodes.RETURN:
                    result = ExecutionResult.voidResult();
                    break;
                case Opcodes.GETSTATIC: //TODO: Access checks for all fields and methods
                case Opcodes.PUTSTATIC:
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) currentInstruction;
                    ExecutorClass owner = manager.loadClass(context, Type.getObjectType(fieldInsnNode.owner));
                    ExecutorClass.ResolvedField fieldNode = owner.findField(context, fieldInsnNode.name, fieldInsnNode.desc);
                    if (fieldNode == null) {
                        result = ExceptionUtils.newException(context, Types.NO_SUCH_FIELD_ERROR, fieldInsnNode.name);
                    } else {
                        if (opcode == Opcodes.GETSTATIC) {
                            stack.pushSized(fieldNode.get());
                        } else {
                            value = stack.popSized();
                            verifyType(context, value, ExecutorTypeUtils.typeToStackType(Type.getType(fieldNode.field().desc)));
                            fieldNode.set(value);
                        }
                    }
                    break;
                case Opcodes.GETFIELD:
                    fieldInsnNode = (FieldInsnNode) currentInstruction;
                    StackObject object = stack.popSized(StackObject.class);
                    if (object.isNull()) {
                        result = ExceptionUtils.newException(context, Types.NULL_POINTER_EXCEPTION, "Tried to access field of null object");
                    } else {
                        fieldNode = object.value().getClazz().findField(context, fieldInsnNode.name, fieldInsnNode.desc);
                        if (fieldNode == null) {
                            result = ExceptionUtils.newException(context, Types.NO_SUCH_FIELD_ERROR, fieldInsnNode.name);
                        } else {
                            stack.pushSized(object.value().getField(fieldNode.field()));
                        }
                    }
                    break;
                case Opcodes.PUTFIELD:
                    fieldInsnNode = (FieldInsnNode) currentInstruction;
                    value = stack.popSized();
                    object = stack.popSized(StackObject.class);
                    if (object.isNull()) {
                        result = ExceptionUtils.newException(context, Types.NULL_POINTER_EXCEPTION, "Tried to access field of null object");
                    } else {
                        fieldNode = object.value().getClazz().findField(context, fieldInsnNode.name, fieldInsnNode.desc);
                        if (fieldNode == null) {
                            result = ExceptionUtils.newException(context, Types.NO_SUCH_FIELD_ERROR, fieldInsnNode.name);
                        } else {
                            verifyType(context, value, ExecutorTypeUtils.typeToStackType(Type.getType(fieldNode.field().desc)));
                            object.value().setField(fieldNode.field(), value);
                        }
                    }
                    break;
                case Opcodes.INVOKEVIRTUAL:
                case Opcodes.INVOKESPECIAL:
                case Opcodes.INVOKEINTERFACE:
                    MethodInsnNode methodInsnNode = (MethodInsnNode) currentInstruction;
                    Type[] argumentTypes = Types.argumentTypes(methodInsnNode);
                    List<StackElement> stackElements = new ArrayList<>(argumentTypes.length);
                    for (int i = argumentTypes.length - 1; i >= 0; i--) {
                        StackElement argumentType = stack.popSized();
                        verifyType(context, argumentType, ExecutorTypeUtils.typeToStackType(argumentTypes[i]));
                        stackElements.add(0, argumentType);
                    }
                    StackObject ownerElement = stack.popSized(StackObject.class);
                    if (ownerElement.isNull()) {
                        result = ExceptionUtils.newException(context, Types.NULL_POINTER_EXCEPTION, "Tried to invoke method on null object");
                    } else {
                        ExecutorObject ownerObject = ownerElement.value();
                        //TODO: Interface checks
                        ExecutorClass.ResolvedMethod methodNode;
                        if (opcode == Opcodes.INVOKESPECIAL) {
                            ExecutorClass ownerClass = manager.loadClass(context, Type.getObjectType(methodInsnNode.owner));
                            methodNode = ownerClass.findMethod(context, methodInsnNode.name, methodInsnNode.desc);
                        } else {
                            methodNode = ownerObject.getClazz().findMethod(context, methodInsnNode.name, methodInsnNode.desc);
                        }
                        if (methodNode == null) {
                            if (ExecutionManager.DEBUG) {
                                System.out.println("Cannot find method " + methodInsnNode.owner + "." + methodInsnNode.name + methodInsnNode.desc + " in " + ownerObject.getClazz().getClassNode().name);
                            }
                            result = ExceptionUtils.newException(context, Types.NO_SUCH_METHOD_ERROR, methodInsnNode.name);
                        } else if (Modifiers.has(methodNode.method().access, Opcodes.ACC_STATIC)) {
                            result = ExceptionUtils.newException(context, Types.INCOMPATIBLE_CLASS_CHANGE_ERROR, "Expecting non-static method " + methodInsnNode.owner + "." + methodInsnNode.name + methodInsnNode.desc);
                        } else {
                            ExecutionResult invokeResult = Executor.execute(context, methodNode.owner(), methodNode.method(), ownerObject, stackElements.toArray(new StackElement[0]));
                            if (invokeResult.hasReturnValue()) {
                                verifyType(context, invokeResult.getReturnValue(), ExecutorTypeUtils.typeToStackType(Types.returnType(methodNode.method())));
                                stack.pushSized(invokeResult.getReturnValue());
                            } else if (invokeResult.hasException()) {
                                result = invokeResult;
                            }
                        }
                    }
                    break;
                case Opcodes.INVOKESTATIC:
                    methodInsnNode = (MethodInsnNode) currentInstruction;
                    argumentTypes = Types.argumentTypes(methodInsnNode);
                    stackElements = new ArrayList<>(argumentTypes.length);
                    for (int i = argumentTypes.length - 1; i >= 0; i--) {
                        StackElement argumentType = stack.popSized();
                        verifyType(context, argumentType, ExecutorTypeUtils.typeToStackType(argumentTypes[i]));
                        stackElements.add(0, argumentType);
                    }
                    ExecutorClass ownerClass = manager.loadClass(context, Type.getObjectType(methodInsnNode.owner));
                    ExecutorClass.ResolvedMethod methodNode = ownerClass.findMethod(context, methodInsnNode.name, methodInsnNode.desc);
                    if (methodNode == null) {
                        if (ExecutionManager.DEBUG) {
                            System.out.println("Cannot find method " + methodInsnNode.owner + "." + methodInsnNode.name + methodInsnNode.desc + " in " + ownerClass.getClassNode().name);
                        }
                        result = ExceptionUtils.newException(context, Types.NO_SUCH_METHOD_ERROR, methodInsnNode.name);
                    } else if (!Modifiers.has(methodNode.method().access, Opcodes.ACC_STATIC)) {
                        result = ExceptionUtils.newException(context, Types.INCOMPATIBLE_CLASS_CHANGE_ERROR, "Expecting static method " + methodInsnNode.owner + "." + methodInsnNode.name + methodInsnNode.desc);
                    } else {
                        ExecutionResult invokeResult = Executor.execute(context, methodNode.owner(), methodNode.method(), null, stackElements.toArray(new StackElement[0]));
                        if (invokeResult.hasReturnValue()) {
                            verifyType(context, invokeResult.getReturnValue(), ExecutorTypeUtils.typeToStackType(Types.returnType(methodNode.method())));
                            stack.pushSized(invokeResult.getReturnValue());
                        } else if (invokeResult.hasException()) {
                            result = invokeResult;
                        }
                    }
                    break;
                case Opcodes.INVOKEDYNAMIC:
                    throw new UnsupportedOperationException(currentInstruction.getClass().getSimpleName() + " " + opcode); //TODO
                case Opcodes.NEW:
                    TypeInsnNode typeInsnNode = (TypeInsnNode) currentInstruction;
                    ExecutorClass newClass = manager.loadClass(context, Type.getObjectType(typeInsnNode.desc));
                    ExecutorObject newObject = manager.instantiate(context, newClass);
                    stack.pushSized(new StackObject(newObject));
                    break;
                case Opcodes.NEWARRAY:
                    IntInsnNode intInsnNode = (IntInsnNode) currentInstruction;
                    int length = stack.popSized(StackInt.class).value();
                    switch (intInsnNode.operand) {
                        case Opcodes.T_BOOLEAN -> stack.pushSized(ExecutorTypeUtils.newArray(context, Type.getType(boolean[].class), length, () -> StackInt.ZERO));
                        case Opcodes.T_BYTE -> stack.pushSized(ExecutorTypeUtils.newArray(context, Type.getType(byte[].class), length, () -> StackInt.ZERO));
                        case Opcodes.T_CHAR -> stack.pushSized(ExecutorTypeUtils.newArray(context, Type.getType(char[].class), length, () -> StackInt.ZERO));
                        case Opcodes.T_SHORT -> stack.pushSized(ExecutorTypeUtils.newArray(context, Type.getType(short[].class), length, () -> StackInt.ZERO));
                        case Opcodes.T_INT -> stack.pushSized(ExecutorTypeUtils.newArray(context, Type.getType(int[].class), length, () -> StackInt.ZERO));
                        case Opcodes.T_LONG -> stack.pushSized(ExecutorTypeUtils.newArray(context, Type.getType(long[].class), length, () -> StackLong.ZERO));
                        case Opcodes.T_FLOAT -> stack.pushSized(ExecutorTypeUtils.newArray(context, Type.getType(float[].class), length, () -> StackFloat.ZERO));
                        case Opcodes.T_DOUBLE -> stack.pushSized(ExecutorTypeUtils.newArray(context, Type.getType(double[].class), length, () -> StackDouble.ZERO));
                        default -> throw new ExecutorException(context, "Unknown array type: " + intInsnNode.operand);
                    }
                    break;
                case Opcodes.ANEWARRAY:
                    typeInsnNode = (TypeInsnNode) currentInstruction;
                    length = stack.popSized(StackInt.class).value();
                    newClass = manager.loadClass(context, Types.asArray(Type.getObjectType(typeInsnNode.desc), 1));
                    stack.pushSized(new StackObject(manager.instantiateArray(context, newClass, length)));
                    break;
                case Opcodes.ARRAYLENGTH:
                    array = stack.popSized(StackObject.class);
                    if (!(array.value() instanceof ArrayObject)) {
                        throw new ExecutorException(context, "Expected array but got " + array.getClass().getSimpleName());
                    }
                    stack.pushSized(new StackInt(((ArrayObject) array.value()).getElements().length));
                    break;
                case Opcodes.ATHROW:
                    object = stack.popSized(StackObject.class);
                    if (object.isNull()) {
                        result = ExceptionUtils.newException(context, Types.NULL_POINTER_EXCEPTION);
                    } else if (!object.value().getClazz().isInstance(context, Types.THROWABLE)) {
                        throw new ExecutorException(context, "Expected throwable but got " + object.value().getClazz().getClassNode().name);
                    } else {
                        result = ExecutionResult.exception(object.value());
                    }
                    break;
                case Opcodes.CHECKCAST:
                    typeInsnNode = (TypeInsnNode) currentInstruction;
                    object = stack.popSized(StackObject.class);
                    if (object != StackObject.NULL && !object.value().getClazz().isInstance(context, Type.getObjectType(typeInsnNode.desc))) {
                        result = ExceptionUtils.newException(context, Types.CLASS_CAST_EXCEPTION, "Cannot cast " + object.value().getClazz().getClassNode().name + " to " + typeInsnNode.desc);
                    } else {
                        stack.pushSized(object.withType(Type.getObjectType(typeInsnNode.desc)));
                    }
                    break;
                case Opcodes.INSTANCEOF:
                    typeInsnNode = (TypeInsnNode) currentInstruction;
                    object = stack.popSized(StackObject.class);
                    if (object.isNull()) {
                        stack.pushSized(StackInt.ZERO);
                    } else {
                        boolean isInstance = object.value().getClazz().isInstance(context, Type.getObjectType(typeInsnNode.desc));
                        stack.pushSized(isInstance ? StackInt.ONE : StackInt.ZERO);
                    }
                    break;
                case Opcodes.MONITORENTER:
                    stack.popSized(); //The object to synchronize on
                    break; //TODO
                case Opcodes.MONITOREXIT:
                    stack.popSized(); //The object stop synchronizing on
                    break; //TODO
                case Opcodes.MULTIANEWARRAY:
                    MultiANewArrayInsnNode multiANewArrayInsnNode = (MultiANewArrayInsnNode) currentInstruction;
                    Type arrayType = Type.getType(multiANewArrayInsnNode.desc);
                    Type elementType = arrayType.getElementType();
                    if (arrayType.getSort() != Type.ARRAY && arrayType.getDimensions() != multiANewArrayInsnNode.dims) {
                        throw new ExecutorException(context, "Expected array type with " + multiANewArrayInsnNode.dims + " dimensions but got " + arrayType);
                    }
                    IntFunction<StackElement> arrayInitializer = null;
                    for (int i = multiANewArrayInsnNode.dims - 1; i >= 1; i--) {
                        int dimensions = stack.popSized(StackInt.class).value();
                        if (dimensions < 0) {
                            result = ExceptionUtils.newException(context, Types.NEGATIVE_ARRAY_SIZE_EXCEPTION, "Dimension: " + dimensions);
                            break;
                        }
                        ExecutorClass arrayClass = manager.loadClass(context, Types.asArray(elementType, i));
                        final IntFunction<StackElement> finalArrayInitializer = arrayInitializer;
                        arrayInitializer = j -> {
                            if (finalArrayInitializer == null) {
                                return new StackObject(manager.instantiateArray(context, arrayClass, dimensions));
                            } else {
                                return new StackObject(manager.instantiateArray(context, arrayClass, dimensions, finalArrayInitializer));
                            }
                        };
                    }
                    stack.pushSized(new StackObject(manager.instantiateArray(context, manager.loadClass(context, arrayType), stack.popSized(StackInt.class).value(), arrayInitializer)));
                    break;
                case Opcodes.IFNULL:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    object = stack.popSized(StackObject.class);
                    if (object.isNull()) {
                        //Jump
                        currentInstruction = jumpInsnNode.label;
                    }
                    break;
                case Opcodes.IFNONNULL:
                    jumpInsnNode = (JumpInsnNode) currentInstruction;
                    object = stack.popSized(StackObject.class);
                    if (!object.isNull()) {
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
                    TryCatchBlockNode matchingTryCatchBlock = getMatchingTryCatchBlock(context, currentMethod, currentInstruction, result.getException().getClazz());
                    if (matchingTryCatchBlock == null) {
                        //If no try catch block was found, throw the exception to the caller
                        break;
                    } else {
                        //A try catch block was found, jump to the handler, clear the stack and push the exception
                        currentInstruction = matchingTryCatchBlock.handler; //Jump
                        stack.clear();
                        stack.pushSized(new StackObject(result.getException()));
                        result = null;
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
        return switch (opcode) {
            case Opcodes.ILOAD, Opcodes.ISTORE -> StackInt.class;
            case Opcodes.LLOAD, Opcodes.LSTORE -> StackLong.class;
            case Opcodes.FLOAD, Opcodes.FSTORE -> StackFloat.class;
            case Opcodes.DLOAD, Opcodes.DSTORE -> StackDouble.class;
            case Opcodes.ALOAD, Opcodes.ASTORE -> StackObject.class;
            default -> throw new IllegalStateException("Unknown opcode: " + opcode);
        };
    }

    private static void verifyType(final ExecutionContext context, final StackElement element, final Class<? extends StackElement> expectedType) {
        if (element == null) {
            throw new ExecutorException(context, "Tried to load empty " + element.getClass().getSimpleName() + " value from stack");
        }
        if (!expectedType.isInstance(element)) {
            throw new ExecutorException(context, "Expected " + expectedType.getSimpleName() + " but got " + element.getClass().getSimpleName());
        }
    }

    @Nullable
    private static TryCatchBlockNode getMatchingTryCatchBlock(final ExecutionContext context, final MethodNode method, final AbstractInsnNode currentInstruction, final ExecutorClass exceptionClass) {
        int index = method.instructions.indexOf(currentInstruction);
        for (TryCatchBlockNode tryCatchBlock : method.tryCatchBlocks) {
            int start = method.instructions.indexOf(tryCatchBlock.start);
            int end = method.instructions.indexOf(tryCatchBlock.end);
            if (start < index && end > index) {
                if (tryCatchBlock.type == null) {
                    return tryCatchBlock;
                } else {
                    ExecutorClass catchClass = context.getExecutionManager().loadClass(context, Type.getObjectType(tryCatchBlock.type));
                    if (exceptionClass.isInstance(context, catchClass.getType())) {
                        return tryCatchBlock;
                    }
                }
            }
        }
        return null;
    }

}
