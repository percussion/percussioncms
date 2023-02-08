/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.share.test;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PSTestUtils
{

    public static String resourceToString(Class<?> testCase, String fileName)
    {
        try
        {
            InputStream stream = testCase.getResourceAsStream(fileName);
            if (stream == null) 
                fail("To read: " + fileName);
            return IOUtils.toString(stream);
        }
        catch (IOException e)
        {
            log.error(e);
            fail("To read: " + fileName);
        }
        return null;
    }
    
    public static String resourceToBase64(Class<?> testCase, String fileName)
    {
        try
        {
            InputStream stream = testCase.getResourceAsStream(fileName);
            if (stream == null) 
                fail("To read: " + fileName);
            return resourceToBase64(stream);
        }
        catch (IOException e)
        {
            log.error(e);
            fail("To read: " + fileName);
        }
        return null;
    }
    
    public static String resourceToBase64(InputStream stream) throws IOException
    {

        byte[] raw = IOUtils.toByteArray(stream);
        Base64 encoder = new Base64();
        byte[] converted = encoder.encode(raw);
        return new String(converted, "UTF-8");

    }

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSTestUtils.class);
}
