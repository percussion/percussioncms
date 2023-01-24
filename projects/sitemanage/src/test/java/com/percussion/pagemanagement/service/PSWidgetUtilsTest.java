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

