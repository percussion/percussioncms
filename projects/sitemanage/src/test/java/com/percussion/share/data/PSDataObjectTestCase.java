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
