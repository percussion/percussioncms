package com.percussion.html;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestPSHtmlUtils {


    @Test
    public void testGetQueryParams() throws UnsupportedEncodingException {

        Map<String,String> test = PSHtmlUtils.getQueryParams(
                PSHtmlUtils.replaceAmpInURL("http://crt-cm1:9992/Rhythmyx/assembler/render?sys_revision=1&amp;sys_context=0&amp;sys_authtype=0&amp;sys_variantid=375&amp;sys_contentid=26505"));
        assertEquals("1", test.get("sys_revision"));
        assertEquals("0", test.get("sys_context"));
        assertEquals("0", test.get("sys_authtype"));
        assertEquals("375", test.get("sys_variantid"));
        assertEquals("26505", test.get("sys_contentid"));


    }
}
