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
package com.percussion.share.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.util.Date;

import org.junit.Test;

public class PSDateUtilsTest
{
    /**
     * Tests {@link PSDateUtils#getDateToString(java.util.Date)} and {@link PSDateUtils#getDateFromString(String)}.
     * 
     * @throws Exception
     */
    @Test
    public void testGetDateToFromString() throws Exception
    {
        Date now = new Date();
        String date = PSDateUtils.getDateToString(now);
        
        Date d = PSDateUtils.getDateFromString(date);
        String dStr = PSDateUtils.getDateToString(d);
        assertEquals(date, dStr);
        assertEquals(d, PSDateUtils.getDateFromString(dStr));
        
        assertEquals("", PSDateUtils.getDateToString(null));
        assertNull(PSDateUtils.getDateFromString(null));
        assertNull(PSDateUtils.getDateFromString(""));
        
        try
        {
            PSDateUtils.getDateFromString("This is not a date!");
            fail("Invalid date string was accepted");
        }
        catch (ParseException e)
        {
            // expected
        }
    }
        
}
