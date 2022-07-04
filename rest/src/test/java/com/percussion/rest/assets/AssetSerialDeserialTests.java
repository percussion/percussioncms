package com.percussion.rest.assets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AssetSerialDeserialTests {


    private Asset getTestAsset(){
        Asset a = new Asset();
        a.setName("testName");
        a.setFolderPath("/Assets/uploads/testPath");
        a.setCreatedDate(new Date());
        a.setId("testId");
        a.setLastModifiedDate(new Date());
        a.setType("percTest");
        a.setRemove(false);
        a.setFields(new AssetFieldList());
        a.getFields().add(new AssetField("testProp","testValue"));
        return a;
    }
    @Test
    public void testSerialize() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE,true);
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE,true);
        Asset a = getTestAsset();
        assertTrue(mapper.canSerialize(Asset.class));
        String assetString = mapper.writeValueAsString(a);

        assertNotNull(assetString);

        Asset n = mapper.readValue(assetString,Asset.class);

        assertFalse(n.getFields().isEmpty());
        assertEquals(a,n);
    }
}
