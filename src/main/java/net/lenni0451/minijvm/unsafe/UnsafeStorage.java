package net.lenni0451.minijvm.unsafe;

import net.lenni0451.minijvm.object.ExecutorObject;

import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class UnsafeStorage {

    private final Cleaner cleaner = Cleaner.create();
    private final Map<WeakReference<ExecutorObject>, Integer> objectToAddress = new HashMap<>();
    private final Map<Integer, WeakReference<ExecutorObject>> addressToObject = new HashMap<>();

    public int getObjectAddress(ExecutorObject object) {
        WeakReference<ExecutorObject> reference = new WeakReference<>(object);
        if (this.objectToAddress.containsKey(reference)) {
            return this.objectToAddress.get(reference);
        } else {
            int address = Math.abs(System.identityHashCode(object)); //TODO: Maybe check for collisions
            this.objectToAddress.put(reference, address);
            this.addressToObject.put(address, reference);
            this.cleaner.register(object, () -> {
                this.objectToAddress.remove(reference);
                this.addressToObject.remove(address);
            });
            return address;
        }
    }

}
