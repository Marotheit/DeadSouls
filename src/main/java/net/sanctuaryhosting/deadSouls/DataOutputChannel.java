package net.sanctuaryhosting.deadSouls;

import org.jetbrains.annotations.NotNull;

import java.io.DataOutput;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channel;
import java.nio.channels.SeekableByteChannel;

final class DataOutputChannel implements DataOutput, Channel {

    @NotNull
    private final SeekableByteChannel chn;
    @NotNull
    private final ByteBuffer buffer;

    public DataOutputChannel(@NotNull SeekableByteChannel chn, int bufferSize) {
        this.chn = chn;
        buffer = ByteBuffer.allocate(bufferSize).order(ByteOrder.BIG_ENDIAN);
    }

    public DataOutputChannel(@NotNull SeekableByteChannel chn) {
        this(chn, 4096);
    }

    public long position() throws IOException {
        return chn.position() + buffer.position();
    }

    public void position(long newPosition) throws IOException {
        if (newPosition != position()) {
            flush();
            chn.position(newPosition);
        }
    }

    /**
     * Truncate to the current position
     */
    public void truncate() throws IOException {
        flush();
        chn.truncate(chn.position());
    }

    private void require(int bytes) throws IOException {
        if (this.buffer.remaining() < bytes) {
            flush();
        }
    }

    public void flush() throws IOException {
        final ByteBuffer buffer = this.buffer;
        buffer.flip();
        while (buffer.hasRemaining()) {
            chn.write(buffer);
        }
        buffer.clear();
    }

    @Override
    public void write(int b) throws IOException {
        require(1);
        buffer.put((byte) b);
    }

    @Override
    public void write(byte @NotNull [] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte @NotNull [] b, int off, int len) throws IOException {
        require(1);
        if (len <= 0) {
            return;
        }
        final ByteBuffer buffer = this.buffer;
        while (true) {
            final int remainingInBuffer = buffer.remaining();
            if (remainingInBuffer >= len) {
                // There is plenty of space in the buffer
                buffer.put(b, off, len);
                return;
            }
            // Not enough space in buffer
            buffer.put(b, off, remainingInBuffer);
            flush();
            off += remainingInBuffer;
            len -= remainingInBuffer;
        }
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        write(v ? ~0: 0);
    }

    @Override
    public void writeByte(int v) throws IOException {
        write(v);
    }

    @Override
    public void writeShort(int v) throws IOException {
        require(2);
        buffer.putShort((short) v);
    }

    @Override
    public void writeChar(int v) throws IOException {
        require(2);
        buffer.putChar((char) v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        require(4);
        buffer.putInt(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        require(8);
        buffer.putLong(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        require(4);
        buffer.putFloat(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        require(8);
        buffer.putDouble(v);
    }

    @Override
    public void writeBytes(@NotNull String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            writeByte(s.charAt(i));
        }
    }

    @Override
    public void writeChars(@NotNull String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            writeChar(s.charAt(i));
        }
    }

    @Override
    public void writeUTF(@NotNull String str) throws IOException {
        final int charLength = str.length();
        int utfLength = 0;

        for (int i = 0; i < charLength; i++) {
            char c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utfLength++;
            } else if (c > 0x07FF) {
                utfLength += 3;
            } else {
                utfLength += 2;
            }
        }

        if (utfLength > 65535)
            throw new UTFDataFormatException("encoded string too long: " + utfLength + " bytes");

        final ByteBuffer buffer = this.buffer;
        if (this.buffer.capacity() >= 2 + utfLength) {
            // Fast path
            require(2 + utfLength);
            buffer.putShort((short) utfLength);

            for (int i = 0; i < charLength; i++) {
                char c = str.charAt(i);
                if ((c >= 0x0001) && (c <= 0x007F)) {
                    buffer.put((byte) c);
                } else if (c > 0x07FF) {
                    buffer.put((byte) (0xE0 | ((c >> 12) & 0x0F)));
                    buffer.put((byte) (0x80 | ((c >> 6) & 0x3F)));
                    buffer.put((byte) (0x80 | ((c) & 0x3F)));
                } else {
                    buffer.put((byte) (0xC0 | ((c >> 6) & 0x1F)));
                    buffer.put((byte) (0x80 | ((c) & 0x3F)));
                }
            }
        } else {
            // Slow path
            require(2);
            buffer.putShort((short) utfLength);

            for (int i = 0; i < charLength; i++) {
                char c = str.charAt(i);
                if ((c >= 0x0001) && (c <= 0x007F)) {
                    require(1);
                    buffer.put((byte) c);
                } else if (c > 0x07FF) {
                    require(3);
                    buffer.put((byte) (0xE0 | ((c >> 12) & 0x0F)));
                    buffer.put((byte) (0x80 | ((c >> 6) & 0x3F)));
                    buffer.put((byte) (0x80 | ((c) & 0x3F)));
                } else {
                    require(2);
                    buffer.put((byte) (0xC0 | ((c >> 6) & 0x1F)));
                    buffer.put((byte) (0x80 | ((c) & 0x3F)));
                }
            }
        }
    }

    @Override
    public boolean isOpen() {
        return chn.isOpen();
    }

    @Override
    public void close() throws IOException {
        flush();
        chn.close();
    }
}