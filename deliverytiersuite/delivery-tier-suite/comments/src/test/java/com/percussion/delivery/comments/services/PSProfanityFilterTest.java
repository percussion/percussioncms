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

package com.percussion.delivery.comments.services;

import com.percussion.delivery.comments.services.PSProfanityFilter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations =
{"classpath:test-beans.xml"})
public class PSProfanityFilterTest extends TestCase
{
    private static PSProfanityFilter profanityFilter;   
    
    private static final String FILE_CONTENT = "ass, balls,bastard, shit";
    
    /**
     * Sample file
     */
    private File m_file;
       
    @Before
    public void setUp() throws Exception
    {   
        super.setUp();
        m_file = createTempFile();
        profanityFilter = new PSProfanityFilter(m_file);
    }
    
    @After
    public void tearDown()
    {
        m_file.delete();
    }
    
    @Test
    public void testContainsProfanity() throws Exception
    {
        // Set the text to validate
        String commentText = "This text contains a profanity word:shit. This comment should be rejected."; 
        assertTrue("Word exists in the profanity list", profanityFilter.containsProfanity(commentText));
    }
    
    @Test
    public void testStartsWithProfanity() throws Exception
    {
        // Set the text to validate
        String commentText = "Shit this text starts with a bad word. This comment should be rejected."; 
        assertTrue("Word exists in the profanity list", profanityFilter.containsProfanity(commentText));
    }
    
    @Test
    public void testEndsWithProfanity() throws Exception
    {
        // Set the text to validate
        String commentText = "This text ends with a bad word. This comment should be rejected shit "; 
        assertTrue("Word exists in the profanity list", profanityFilter.containsProfanity(commentText));
    }
    
    @Test
    public void testContainsCapsProfanity() throws Exception
    {
        // Set the text to validate
        String commentText = "This text contains a bad word ShIt. This comment should be rejected."; 
        assertTrue("Word exists in the profanity list", profanityFilter.containsProfanity(commentText));
    }
    
    @Test
    public void testNoContainsProfanity() throws Exception
    {
        // Set the text to validate
        String commentText = "This text does not contain a profanity word sh$$t and should be accepted."; 
        assertFalse("Word doesn't exist in the profanity list", profanityFilter.containsProfanity(commentText));
    }

    /**
     * Creates temporary file and writes to it string 
     */
    private File createTempFile() throws IOException
    {
       final File file = File.createTempFile("testProfanity", ".txt");
       FileUtils.writeStringToFile(file, FILE_CONTENT, "UTF-8"
       );
       return file;
    }
    
}
