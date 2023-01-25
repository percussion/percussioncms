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
package com.percussion.utils;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author JaySeletz
 *
 */
public class PSTokenReplacingReaderTest
{

    private static final String[] TOKENS = new String[] {"${TOKEN1}", "${TOKEN2}"};
    private static final String[] TOKENS_XML = new String[] {"$XML{TOKEN1}", "$XML{TOKEN2}"};
    private static final String[] VALUES = new String[] {"TOKEN1_REP&LACED", "TOKEN2_REP&LACED"};
    private static final String[] VALUES_XML = new String[] {"TOKEN1_REP&amp;LACED", "TOKEN2_REP&amp;LACED"};
    private static final String SRC = "This is a test with some tokens in it. The are ${TOKEN1} and also '${TOKEN2}'";
    private static final String SRC_XML = "This is a test with some tokens in it. The are $XML{TOKEN1} and also '$XML{TOKEN2}'";
    
    
    @Test
    public void test() throws Exception
    {
        final Set<String> tokens = new HashSet<String>();
        
        Reader reader = new PSTokenReplacingReader(new StringReader(SRC), new IPSTokenResolver()
        {
            
            @Override
            public String resolveToken(String tokenName)
            {
                tokens.add("${" + tokenName + "}");
                return tokenName + "_REP&LACED";
            }
        });
        
        Writer writer = new StringWriter();
        IOUtils.copy(reader, writer);
        
        assertEquals(TOKENS.length, tokens.size());
        assertTrue(tokens.containsAll(Arrays.asList(TOKENS)));
        
        String result = writer.toString();
        assertFalse(SRC.equals(result));
        
        String replaced = StringUtils.replaceEach(SRC, TOKENS, VALUES);
        assertEquals(replaced, result);
    }
    
    @Test
    public void testXmlEncode() throws Exception
    {
        final Set<String> tokens = new HashSet<String>();
        
        Reader reader = new PSTokenReplacingReader(new StringReader(SRC_XML), new IPSTokenResolver()
        {
            
            @Override
            public String resolveToken(String tokenName)
            {
                tokens.add("$XML{" + tokenName + "}");
                return tokenName + "_REP&LACED";
            }
        });
        
        Writer writer = new StringWriter();
        IOUtils.copy(reader, writer);
        
        assertEquals(TOKENS_XML.length, tokens.size());
        assertTrue(tokens.containsAll(Arrays.asList(TOKENS_XML)));
        
        String result = writer.toString();
        assertFalse(SRC_XML.equals(result));
        
        String replaced = StringUtils.replaceEach(SRC_XML, TOKENS_XML, VALUES_XML);
        assertEquals(replaced, result);
    }
    
}
