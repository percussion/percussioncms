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

package com.percussion.testing;

import com.percussion.error.PSExceptionUtils;
import com.percussion.util.PSResourceUtils;
import com.percussion.utils.testing.IntegrationTest;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

/**
 * Testing {@link PSTidyUtils}. This test has to be run within server environment
 * because it is rely on PSServer.getRxDir() to load tidy property file.
 */
@Category(IntegrationTest.class)
public class PSTidyUtilsTest
{

    private static final Logger log = LogManager.getLogger(PSTidyUtilsTest.class);

    Properties m_tidyProperties = new Properties();;

    @Before
    public void setUp()
    {
       InputStream input = null;
       try
       {
          String text = IOUtils.toString(new FileInputStream(PSResourceUtils.getFile(PSTidyUtilsTest.class,"/com/percussion/testing/rxW2Ktidy.properties",null)));
          input = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));

          m_tidyProperties.load(input);
          PSTidyUtils.setTidyProperties(m_tidyProperties);
       }
       catch (Exception e)
       {
           log.error(PSExceptionUtils.getMessageForLog(e));
           log.debug(PSExceptionUtils.getDebugMessageForLog(e));
       }
       finally
       {
          IOUtils.closeQuietly(input);
       }
    }

    @Test
    public void testTidy() throws Exception
    {
        String missingPTag = "<html><head> <title>peter</title> </header> <body> <summary>hello</summary> <div> <p>Hello</div> </body></html>";
        String html = PSTidyUtils.applyTidy(missingPTag, null);
        assertTrue(html.contains("</p>"));
        
        html = PSTidyUtils.applyTidy(MISSING_P_TAG, null);
        
        // tidy will add HTML tag

        assertTrue(html.contains("<html>"));
        assertTrue(html.contains("</html>"));
        assertTrue(html.contains("<body>"));
        assertTrue(html.contains("</body>"));
    }

    @Test
    public void testTidyHtml_html5ClosedTags() throws Exception
    {
       String source = IOUtils.toString(new FileInputStream(PSResourceUtils.getFile(PSTidyUtilsTest.class,"/com/percussion/testing/PSTidyUtilsTest.txt",null)));
       validateTags(html5Tags, source);

       String html = PSTidyUtils.applyTidy(source, null);
       validateTags(html5Tags, html);
       
       html = PSTidyUtils.applyTidy(MISSING_P_TAG, null);
    }

    @Test
    public void testTidyHtml_html5SelfClosedTags() throws Exception
    {
       String source = IOUtils.toString(new FileInputStream(PSResourceUtils.getFile(PSTidyUtilsTest.class,"/com/percussion/testing/PSTidyUtilsTest_selfClosedTags.txt",null)));
       validateTags(html5SelfClosedTags, source);

       String html = PSTidyUtils.applyTidy(source, null);
       validateTags(html5SelfClosedTags, html);
       
       html = PSTidyUtils.applyTidy(MISSING_P_TAG, null);
    }

    @Test
    public void testTidyHtml_html5UnclosedTags() throws Exception
    {
       String source = IOUtils.toString(new FileInputStream(PSResourceUtils.getFile(PSTidyUtilsTest.class,"/com/percussion/testing/PSTidyUtilsTest_unclosedTags.txt",null)));
       validateTags(html5SelfClosedTags, source);

       String html = PSTidyUtils.applyTidy(source, null);
       validateTags(html5SelfClosedTags, html);
       
       html = PSTidyUtils.applyTidy(MISSING_P_TAG, null);
    }
    
    /**
     * Tidy cannot parse unknown (HTML) tags.
     * 
     * @throws Exception if an error occurs.
     */
    @Test
    public void testTidy_Negative() throws Exception
    {
        String unknownTag = "<html><head> <title>peter</title> </header> <body> <div> <unknown>Hello</div> </body></html>";
        try
        {
            PSTidyUtils.applyTidy(unknownTag, null);
            assertTrue(false);
        }
        catch (Exception e)
        {
            System.out.println("got tidy error: " + e.getMessage());
        }
        
    }

   /**
    * Checks that the given html contains the specified tags.
    * 
    * @param tags {@link String String[]} with the tags to check. Assumed not
    *           <code>null</code>.
    * @param html the html content to verify that it contains the given tags.
    *           Assumed not <code>null</code>.
    */
    public void validateTags(String[] tags, String html)
    {
       for(String tag : tags)
       {
          assertTrue("HTML after tidy: it should have contained tag: " + tag, html.contains("<" + tag));
       }
    }
    
    static String MISSING_P_TAG = "<div> <p>Hello</div>";

    private String[] html5Tags = new String[]
    {"video", "menu", "command", "source", "embed", "article", "aside", "audio",
        "bdi", "canvas", "datalist", "details", "figure", "figcaption",
        "footer", "header", "hgroup", "keygen", "mark", "meter", "nav",
        "output", "progress", "rt", "rp", "ruby", "section", "summary",
        "time", "track", "wbr"};

    private String[] html5SelfClosedTags = new String[]
    {"source", "embed", "keygen", "output", "progress", "track", "wbr"};

}
