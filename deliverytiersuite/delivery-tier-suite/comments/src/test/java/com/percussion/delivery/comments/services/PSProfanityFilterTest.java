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
