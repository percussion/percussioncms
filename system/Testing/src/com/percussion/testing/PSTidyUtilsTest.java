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

package com.percussion.testing;

import com.percussion.util.PSResourceUtils;
import com.percussion.util.PSTidyUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.testing.UnitTest;
import org.apache.commons.io.IOUtils;

import junit.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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
          input = new ByteArrayInputStream(text.getBytes("UTF-8"));

          m_tidyProperties.load(input);
          PSTidyUtils.setTidyProperties(m_tidyProperties);
       }
       catch (Exception e)
       {
           log.error(e.getMessage());
           log.debug(e.getMessage(), e);
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
