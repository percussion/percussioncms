/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.share.test;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private static final Log log = LogFactory.getLog(PSTestUtils.class);
}
