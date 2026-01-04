package com.mathias.kafka.schema.producer.component;

import lombok.RequiredArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
@RequiredArgsConstructor
public class Validation {

    public void validateAvroRecord(SpecificRecord rec) {

        if (rec == null) {
            throw new IllegalArgumentException("Record is null");
        }
        Schema schema = rec.getSchema();

        if (!SpecificData.get().validate(schema, rec)) {
            throw new IllegalArgumentException("Record violates Avro schema: " + schema.getFullName());
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Encoder enc = EncoderFactory.get()
                    .validatingEncoder(schema, EncoderFactory.get().binaryEncoder(out, null));
            new SpecificDatumWriter<SpecificRecord>(schema).write(rec, enc);
            enc.flush();
        } catch (Exception e) {
            throw new IllegalArgumentException("Avro encode validation failed", e);
        }
    }
}
