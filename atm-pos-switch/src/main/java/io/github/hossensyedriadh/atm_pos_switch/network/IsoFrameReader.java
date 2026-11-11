package io.github.hossensyedriadh.atm_pos_switch.network;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

@Log4j2
@Component
public class IsoFrameReader {
    private static final int MAX_FRAME_LENGTH = 8192;

    public byte[] read(DataInputStream inputStream) throws IOException {
        int highByte = inputStream.read();

        if (highByte == -1) {
            return null;
        }

        int lowByte = inputStream.read();

        if (lowByte == -1) {
            log.warn("Incomplete message length header");
            throw new EOFException("Incomplete message length header");
        }

        int length = (highByte << 8) | lowByte;

        if (length == 0 || length > MAX_FRAME_LENGTH) {
            log.warn("Invalid ISO 8583 frame length: {}", length);
            throw new IOException("Invalid ISO 8583 frame length: " + length);
        }

        byte[] payload = inputStream.readNBytes(length);

        if (payload.length != length) {
            log.warn("Incomplete ISO 8583 message payload");
            throw new EOFException("Incomplete ISO 8583 message payload");
        }

        return payload;
    }
}
