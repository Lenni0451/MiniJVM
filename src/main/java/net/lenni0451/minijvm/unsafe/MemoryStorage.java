package net.lenni0451.minijvm.unsafe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MemoryStorage {

    private final Map<Long, byte[]> memory = new HashMap<>();

    public byte[] get(final long address, final int length) {
        byte[] mem = this.memory.get(address);
        if (mem == null) {
            return new byte[length];
        } else {
            return Arrays.copyOf(mem, length);
        }
    }

    public void put(final long address, final byte[] data) {
        this.memory.put(address, data);
    }

    public int getInt(final long address) {
        byte[] data = this.get(address, 4);
        return ((data[0] & 0xFF) << 24) |
                ((data[1] & 0xFF) << 16) |
                ((data[2] & 0xFF) << 8) |
                (data[3] & 0xFF);
    }

    public void putInt(final long address, final int value) {
        this.put(address, new byte[]{
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value
        });
    }

    public long getLong(final long address) {
        byte[] data = this.get(address, 8);
        return ((long) data[0] << 56) |
                ((long) data[1] << 48) |
                ((long) data[2] << 40) |
                ((long) data[3] << 32) |
                ((long) data[4] << 24) |
                ((long) data[5] << 16) |
                ((long) data[6] << 8) |
                (long) data[7];
    }

    public void putLong(final long address, final long value) {
        this.put(address, new byte[]{
                (byte) (value >>> 56),
                (byte) (value >>> 48),
                (byte) (value >>> 40),
                (byte) (value >>> 32),
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value
        });
    }

}
