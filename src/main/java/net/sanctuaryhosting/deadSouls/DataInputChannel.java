package net.sanctuaryhosting.deadSouls;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channel;
import java.nio.channels.SeekableByteChannel;

final class DataInputChannel implements DataInput, Channel {

    @NotNull
    private final SeekableByteChannel chn;
    @NotNull
    private final ByteBuffer buffer;

    public DataInputChannel(@NotNull SeekableByteChannel chn, int bufferSize) {
        this.chn = chn;
        buffer = ByteBuffer.allocate(bufferSize).order(ByteOrder.BIG_ENDIAN);
        buffer.limit(0);
    }

    public DataInputChannel(@NotNull SeekableByteChannel chn) {
        this(chn, 4096);
    }

    private void require(int bytes) throws IOException {
        final ByteBuffer buffer = this.buffer;
        if (buffer.remaining() < bytes) {
            buffer.compact();
            do {
                if (chn.read(buffer) <= 0) {
                    break;
                }
            } while (buffer.hasRemaining());
            buffer.flip();
        }
    }

    public long remaining() throws IOException {
        return buffer.remaining() + chn.size() - chn.position();
    }

    public boolean hasRemaining() throws IOException {
        return buffer.hasRemaining() || remaining() > 0;
    }

    @Override
    public void readFully(byte @NotNull [] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(byte @NotNull [] b, int off, int len) throws IOException {
        if (len <= 0) {
            return;
        }

        final ByteBuffer buffer = this.buffer;
        while (true) {
            require(1);
            final int remaining = buffer.remaining();
            if (remaining <= 0) {
                throw new EOFException("End of channel");
            }
            if (remaining >= len) {
                // Can do everything in one go
                buffer.get(b, off, len);
                return;
            }
            // Must read in parts
            buffer.get(b, off, remaining);
            off += remaining;
            len -= remaining;
        }
    }

    @Override
    public int skipBytes(int n) throws IOException {
        int skipped = 0;
        final ByteBuffer buffer = this.buffer;
        while (n > 0) {
            require(1);
            final int remaining = buffer.remaining();
            if (remaining <= 0) {
                // EOF
                break;
            }

            if (remaining >= n) {
                buffer.position(buffer.position() + n);
                skipped += n;
                break;
            }
            buffer.position(buffer.position() + remaining);
            skipped += remaining;
            n -= remaining;
        }
        return skipped;
    }

    @Override
    public boolean readBoolean() throws IOException {
        require(1);
        return buffer.get() != 0;
    }

    @Override
    public byte readByte() throws IOException {
        require(1);
        return buffer.get();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        require(1);
        return buffer.get() & 0xFF;
    }

    @Override
    public short readShort() throws IOException {
        require(2);
        return buffer.getShort();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        require(2);
        return buffer.getShort() & 0xFFFF;
    }

    @Override
    public char readChar() throws IOException {
        require(2);
        return buffer.getChar();
    }

    @Override
    public int readInt() throws IOException {
        require(4);
        return buffer.getInt();
    }

    @Override
    public long readLong() throws IOException {
        require(8);
        return buffer.getLong();
    }

    @Override
    public float readFloat() throws IOException {
        require(4);
        return buffer.getFloat();
    }

    @Override
    public double readDouble() throws IOException {
        require(8);
        return buffer.getDouble();
    }

    @Contract("-> fail")
    @Override
    public String readLine() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("readLine is deprecated and not supported");
    }

    private byte @Nullable [] byteArr;
    private char @Nullable [] charArr;

    @NotNull
    @Override
    public String readUTF() throws IOException {
        final int utfLen = readUnsignedShort();

        byte[] byteArr = this.byteArr;
        char[] charArr = this.charArr;
        if (byteArr == null || byteArr.length < utfLen) {
            byteArr = this.byteArr = new byte[utfLen * 2];
            charArr = this.charArr = new char[utfLen * 2];
        } else {
            assert charArr != null;
        }

        int c, char2, char3;
        int count = 0;
        int chararr_count = 0;

        readFully(byteArr, 0, utfLen);

        while (count < utfLen) {
            c = (int) byteArr[count] & 0xff;
            if (c > 127)
                break;
            count++;
            charArr[chararr_count++] = (char) c;
        }

        while (count < utfLen) {
            c = (int) byteArr[count] & 0xff;
            switch (c >> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    /* 0xxxxxxx*/
                    count++;
                    charArr[chararr_count++] = (char) c;
                    break;
                case 12:
                case 13:
                    /* 110x xxxx   10xx xxxx*/
                    count += 2;
                    if (count > utfLen)
                        throw new UTFDataFormatException("malformed input: partial character at end");
                    char2 = byteArr[count - 1];
                    if ((char2 & 0xC0) != 0x80)
                        throw new UTFDataFormatException("malformed input around byte " + count);
                    charArr[chararr_count++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
                    break;
                case 14:
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    count += 3;
                    if (count > utfLen)
                        throw new UTFDataFormatException("malformed input: partial character at end");
                    char2 = byteArr[count - 2];
                    char3 = byteArr[count - 1];
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                        throw new UTFDataFormatException("malformed input around byte " + (count - 1));
                    charArr[chararr_count++] = (char) (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | char3 & 0x3F);
                    break;
                default:
                    /* 10xx xxxx,  1111 xxxx */
                    throw new UTFDataFormatException("malformed input around byte " + count);
            }
        }
        // The number of chars produced may be less than utflen
        return new String(charArr, 0, chararr_count);
    }

    @Override
    public boolean isOpen() {
        return chn.isOpen();
    }

    @Override
    public void close() throws IOException {
        chn.close();
    }
}