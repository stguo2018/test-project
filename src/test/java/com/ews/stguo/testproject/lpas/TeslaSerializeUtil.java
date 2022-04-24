package com.ews.stguo.testproject.lpas;

import com.expedia.lodging.lodgingpricingavailabilityservice.lpasobject.LodgingPricingAvailabilityRequest;
import com.expedia.lodging.lodgingpricingavailabilityservice.lpasobject.LodgingPricingAvailabilityResponse;
import com.expedia.tesla.schema.SchemaVersion;
import com.expedia.tesla.serialization.BinaryReader;
import com.expedia.tesla.serialization.BinaryWriter;
import com.expedia.tesla.serialization.ISerializable;
import com.expedia.tesla.serialization.ITeslaReader;
import com.expedia.tesla.serialization.ITeslaWriter;
import com.expedia.tesla.serialization.JsonWriter;
import com.expedia.tesla.serialization.TeslaDeserializationException;
import com.expedia.tesla.serialization.TeslaSerializationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public class TeslaSerializeUtil {

    private TeslaSerializeUtil() {
    }

    public static byte[] serializeEntity(LodgingPricingAvailabilityRequest lpasRequest, long teslaSchemaHash) throws
            IOException, TeslaSerializationException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ITeslaWriter teslaWriter = new BinaryWriter(buffer, teslaSchemaHash)) {
            lpasRequest.serialize(teslaWriter);
        }
        return buffer.toByteArray();
    }

    public static LodgingPricingAvailabilityResponse deserializeResponse(byte[] responseBytes,
                                                                         long teslaSchemaHash) throws IOException,
            TeslaDeserializationException {
        LodgingPricingAvailabilityResponse lpasResponse = new LodgingPricingAvailabilityResponse();
        try (ITeslaReader reader = new BinaryReader(responseBytes, teslaSchemaHash)) {
            lpasResponse.deserialize(reader);
        }
        return lpasResponse;
    }

    public static String serializeJson(ISerializable object, long teslaSchemaHash) throws TeslaSerializationException, IOException {
        Writer writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer, new SchemaVersion(teslaSchemaHash));
        object.serialize(jsonWriter);
        jsonWriter.flush();
        return writer.toString();
    }

}
