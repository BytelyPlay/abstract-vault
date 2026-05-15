package org.abstractvault.bytelyplay.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ResettableInputStream extends FilterInputStream {
    private ByteBuffer markData;
    private boolean reset = false;

    public ResettableInputStream(InputStream in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        if (markData != null) {
            if (!reset) {
                if (markData.position() >= markData.capacity()) {
                    resetResettableData();
                    return super.read();
                }
                int original = super.read();
                markData.put((byte) original);

                return original;
            } else {
                if (markData.position() < markData.limit()) return markData.get();
                else {
                    resetResettableData();
                }
            }
        }
        return super.read();
    }

    @Override
    public void mark(int readLimit) {
        if (markSupported()) {
            super.mark(readLimit);
            return;
        }
        resetResettableData();
        markData = ByteBuffer.allocate(readLimit);
    }

    @Override
    public void reset() throws IOException {
        if (markSupported()) {
            super.reset();
            return;
        }
        if (markData != null)
            markData.flip();
        reset = true;
    }
    private void resetResettableData() {
        markData = null;
        reset = false;
    }
}
