package net.lenni0451.minijvm;

import net.lenni0451.commons.asm.io.ClassIO;
import net.lenni0451.commons.asm.provider.ClassProvider;
import net.lenni0451.commons.asm.provider.MapClassProvider;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.lenni0451.commons.asm.ASMUtils.slash;

public class ClassPool {

    private final ClassProvider classProvider;
    private final Map<String, byte[]> classes;

    public ClassPool(final ClassProvider classes) {
        this(classes, new HashMap<>());
    }

    public ClassPool(final Map<String, byte[]> classes) {
        this(new MapClassProvider(classes, MapClassProvider.NameFormat.SLASH), new HashMap<>());
    }

    public ClassPool(final ClassProvider classProvider, final Map<String, byte[]> classes) {
        this.classProvider = classProvider;
        this.classes = new HashMap<>(classes);
    }

    public boolean loadClass(@Nullable final String name, final byte[] bytes) {
        return this.loadClass(name, bytes, 0, bytes.length);
    }

    public boolean loadClass(@Nullable final String name, final byte[] bytes, final int offset, final int length) {
        byte[] slice = Arrays.copyOfRange(bytes, offset, length);
        ClassNode classNode = ClassIO.fromBytes(slice);
        if (name != null && !classNode.name.equals(slash(name))) return false;
        this.classes.put(classNode.name, slice);
        return true;
    }

    @Nullable
    public byte[] getClass(final String internalName) throws ClassNotFoundException {
        byte[] bytes = this.classes.get(internalName);
        if (bytes != null) return bytes;
        return this.classProvider.getClass(internalName);
    }

    @Nullable
    public ClassNode getClassNode(final String internalName) throws ClassNotFoundException {
        byte[] bytes = this.getClass(internalName);
        if (bytes == null) return null;
        ClassNode classNode = ClassIO.fromBytes(bytes);
        List<MethodNode> methods = classNode.methods;
        for (int i = 0; i < methods.size(); i++) {
            MethodNode method = methods.get(i);
            JSRInlinerAdapter adapter = new JSRInlinerAdapter(method, method.access, method.name, method.desc, method.signature, method.exceptions == null ? null : method.exceptions.toArray(new String[0]));
            method.accept(adapter); //Maybe remove this later if JSR is supported
            methods.set(i, adapter);
        }
        return classNode;
    }

}
