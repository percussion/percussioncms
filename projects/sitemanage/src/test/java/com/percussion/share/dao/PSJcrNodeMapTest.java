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
package com.percussion.share.dao;

import static com.percussion.share.test.PSMatchers.emptyString;
import static org.apache.commons.lang.Validate.notNull;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.percussion.util.PSPurgableTempFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

//import static java.util.Arrays.*;
//import static org.hamcrest.CoreMatchers.*;
//import static org.junit.matchers.JUnitMatchers.*;

/**
 * Scenario description: 
 * @author adamgent, Oct 23, 2009
 */
@RunWith(JMock.class)
public class PSJcrNodeMapTest
{

    Mockery context = new JUnit4Mockery();

    PSJcrNodeMap sut;

    Node collaborator;
    
    PropertyIterator pi;
    
    Sequence sequence;

    @Before
    public void setUp() throws Exception
    {
        collaborator = context.mock(Node.class);
        sut = new PSJcrNodeMap(collaborator);
        pi = context.mock(PropertyIterator.class);
        sequence = context.sequence("pi sequence");

    }

    @Test
    public void shouldGetFromNode() throws Exception
    {

        expectProperty("testKey", "testValue");
        context.checking(new Expectations() {{
            
        }});

        
        String actual = (String) sut.get("testKey");

        assertEquals("testValue", actual);
    }
    
    @Test
    public void shouldGetFromOverride() throws Exception
    {
        //expectNoProperty("testKey");
        
        sut.put("testKey", "fromOverride");
        String actual = (String) sut.get("testKey");

        assertEquals("fromOverride", actual);
        
        PSPurgableTempFile ptf = null;
        try
        {
            ptf = new PSPurgableTempFile("tmp", null, null);
            sut.put("testBinary", ptf);
            assertEquals(sut.get("testBinary"), ptf);
        }
        finally
        {
            if (ptf != null)
            {
                ptf.delete();
            }
        }
    }
    
    @Test
    public void shouldGetEmptyStringForBinary() throws Exception
    {
        expectProperty("testKey", "testValue", PropertyType.BINARY);
        String actual = (String) sut.get("testKey");
        assertThat(actual, is(emptyString()));
    }
    
    @Test
    public void shouldGetEntrySet() throws Exception
    {
        /*
         * Given: TODO initial setup.
         */

        /* 
         * Expect: TODO expect some methods to be called on the collaborator.
         */
        
        
        context.checking(new Expectations(){{
            one(collaborator).getProperties();
            will(returnValue(pi));
            
            one(pi).hasNext();
            will(returnValue(true)); inSequence(sequence);
            
            one(pi).nextProperty();
            will(returnValue(expectProperty("a", "A"))); inSequence(sequence);
            
            one(pi).hasNext();
            will(returnValue(true)); inSequence(sequence);
            
            one(pi).nextProperty();
            will(returnValue(expectProperty("b", "B"))); inSequence(sequence);
            
            one(pi).hasNext();
            will(returnValue(false)); inSequence(sequence);
            
        }});

        /*
         * When: TODO executes GetEntrySet on the SUT
         */

        /*
         * Then: TODO check to see if the behavior is correct.
         */

        Set<Entry<String, Object>> set = sut.entrySet();
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("a", "A");
        expected.put("b", "B");
        Map<String, Object> actual = new HashMap<String, Object>();
        for(Entry<String,Object> e : set) {
            actual.put(e.getKey(), e.getValue());
        }
        assertEquals(expected,actual);
        
    }
    
    
    public void expectNoProperty(final String name) throws Exception {
        notNull(name);
        
        context.checking(new Expectations(){{
            one(collaborator).hasProperty(name);
            will(returnValue(false));
        }});        
        
    }
    public Property expectProperty(final String name, final String value) throws Exception {
        notNull(name);
        return expectProperty(name, value, PropertyType.STRING);
    }
    
    public Property expectProperty(final String name, final String value, final int type) throws Exception {
        notNull(name);
        final Property property = context.mock(Property.class, Property.class.getSimpleName() + " " + name);
        
        context.checking(new Expectations(){{
            
            allowing(property).getName();
            will(returnValue(name));
            
            allowing(property).getType();
            will(returnValue(type));
            
            allowing(collaborator).hasProperty(name);
            will(returnValue(true));
            
            one(collaborator).getProperty(name);
            will(returnValue(property));
            
            if (type != PropertyType.BINARY) {
                one(property).getString();
                will(returnValue(value));
            }
        }});  
        
        return property;
        
    }

}
