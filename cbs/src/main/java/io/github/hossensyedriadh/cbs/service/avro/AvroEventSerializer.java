package io.github.hossensyedriadh.cbs.service.avro;

import org.apache.avro.specific.SpecificRecord;

public interface AvroEventSerializer {
    byte[] serialize(SpecificRecord event);
}
