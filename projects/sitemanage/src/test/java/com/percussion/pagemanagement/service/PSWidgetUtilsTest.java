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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.pagemanagement.service;

import static com.percussion.pagemanagement.service.impl.PSWidgetUtils.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.percussion.pagemanagement.service.impl.PSWidgetUtils.PSWidgetPropertyCoercionException;

/**
 * Test widget utils.
 * @author adamgent
 *
 */
public class PSWidgetUtilsTest
{
 
    @Test
    public void testCoerceString() throws Exception
    {
        String actual = coerceProperty("my", "true", String.class);
        assertEquals("true",actual);
    }
    
    @Test(expected = PSWidgetPropertyCoercionException.class)
    public void testCoerceBadString() throws Exception
    {
        coerceProperty("my", 1, String.class);
    }
    
    @Test
    public void testCoerceBoolean() throws Exception
    {
        Boolean actual = coerceProperty("my", "true", Boolean.class);
        assertTrue(actual);
        
        actual = coerceProperty("my", true, Boolean.class);
        assertTrue(actual);
    }
    
    @Test
    public void testCoerceNumber() throws Exception
    {
        Number actual = coerceProperty("my", 1, Number.class);
        assertEquals(1, actual);
        
        actual = coerceProperty("my", "1", Number.class);
        assertEquals(1, actual);
    }
    
    @Test(expected=PSWidgetPropertyCoercionException.class)
    public void testCoerceBlankSpaces() throws Exception
    {
        Number actual = coerceProperty("my", "  ", Number.class);
        assertEquals(1, actual);
    }
    
    @Test(expected=PSWidgetPropertyBlankStringCoercionException.class)
    public void testCoerceBlankString() throws Exception
    {
        Number actual = coerceProperty("my", "", Number.class);
        assertEquals(1, actual);
    }

}

