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
