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
package com.percussion.share.data;

import static org.junit.Assert.*;
import static com.percussion.share.test.PSDataObjectTestUtils.*;

import org.junit.Before;
import org.junit.Test;

import com.percussion.share.test.PSDataObjectTestUtils;

public abstract class PSDataObjectTestCase<T>
{
    
    public abstract T getObject() throws Exception;
    
    protected T object;
    
    @Before
    public void setUp() throws Exception
    {
        object = getObject();
        assertNotNull(object);
    }
    
    
    protected T getCopy() {
        return PSDataObjectTestUtils.doXmlSerialization(object).actualSerialized;
    }
    
    
    @Test
    public void testXmlSerialization() throws Exception
    {
        assertXmlSerialization(object);
    }
    
    @Test
    public void testEquals() throws Exception
    {
        assertEqualsMethod(object);
    }
    
    @Test
    public void testToString() throws Exception
    {
        assertNotNull(object.toString());
    }
    
    @Test
    public void testHashCode() throws Exception
    {
        
    }
    
    @Test
    public void testClone() throws Exception
    {
        
    }

}
