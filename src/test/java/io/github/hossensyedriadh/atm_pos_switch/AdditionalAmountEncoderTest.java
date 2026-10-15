package io.github.hossensyedriadh.atm_pos_switch;

import io.github.hossensyedriadh.atm_pos_switch.iso8583.AdditionalAmountEncoder;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.IsoMessageParser;
import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AdditionalAmountEncoderTest {
    private final AdditionalAmountEncoder encoder =
            new AdditionalAmountEncoder();

    private final IsoMessageParser parser = new IsoMessageParser();

    @Test
    void shouldEncodeBalanceWithExpectedFormat() {
        String encoded = encoder.encode(
                "00", "01", "050", 'C', 12_500
        );

        Assertions.assertEquals("0001050C000000012500", encoded);
        Assertions.assertEquals(20, encoded.length());
    }

    @Test
    void shouldPackAndUnpackField54WithoutDataLoss() throws Exception {
        String expected = encoder.encode(
                "00", "01", "050", 'C', 12_500
        );

        ISOMsg response = new ISOMsg();
        response.setMTI("0210");
        response.set(3, "310000");
        response.set(11, "000123");
        response.set(39, "00");
        response.set(41, "ATM00001");
        response.set(49, "050");
        response.set(54, expected);

        byte[] packed = parser.pack(response);
        Assertions.assertTrue(packed.length > 0);

        ISOMsg unpacked = parser.unpack(packed);

        Assertions.assertEquals("0210", unpacked.getMTI());
        Assertions.assertEquals("00", unpacked.getString(39));
        Assertions.assertEquals(expected, unpacked.getString(54));
        Assertions.assertEquals(20, unpacked.getString(54).length());
    }

    @Test
    void shouldRejectInvalidSign() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> encoder.encode("00", "01", "050", 'X', 12_500)
        );
    }
}
