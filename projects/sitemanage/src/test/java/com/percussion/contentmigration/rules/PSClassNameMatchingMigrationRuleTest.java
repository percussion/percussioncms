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
package com.percussion.contentmigration.rules;

import static org.junit.Assert.*;

import com.percussion.share.test.PSTestUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

/**
 * @author JaySeletz
 *
 */
public class PSClassNameMatchingMigrationRuleTest
{

    private static final String SRC_DOC = "source.html";
    private static final String MULTI_CLASS_SRC_DOC = "multiClassSource.html";
    private static final String SINGLE_WIDGET_ID = "1";
    private static final String PARENT_WIDGET_ID = "2";
    private static final String MULTI_WIDGET_ID = "3";
    private static final String SINGLE_CONTENT_TO_MATCH = "<p>Content to Migrate</p>";
    private static final String SINGLE_PARENT_CONTENT_TO_MATCH = "singleParentMatchTgt.html";
    private static final String SINGLE_PARENT_CONTENT_TO_MATCH_ID = "singleParentMatch";
    private static final String MULTI_PARENT_CONTENT_TO_MATCH = "multiParentMatchTgt.html";
    private static final String MULTI_PARENT_CONTENT_NO_MATCH = "multiParentNoMatchTgt.html";
    private static final String MULTI_PARENT_CONTENT_TO_MATCH_ID = "multiParentMatch";
    private static final String MULTI_CLASS_ALL_MATCH = "multiClassMatchTgt.html";
    private static final String MULTI_CLASS_PARENT_MATCH = "multiClassParentMatchTgt.html";
    private static final String MULTI_CLASS_NO_PARENT_MATCH = "multiClassNoParentMatchTgt.html";
    
    private static PSClassNameMatchingMigrationRule RULE = new PSClassNameMatchingMigrationRule();

    @Test
    public void testNoMatch()
    {
        assertNull(extractMatch(SRC_DOC, "noMatchTgt.html", SINGLE_WIDGET_ID));
    }


    @Test
    public void testSingleMatch()
    {
        String match = extractMatch(SRC_DOC, "singleMatchTgt.html", SINGLE_WIDGET_ID); 
        assertNotNull(match);
        assertEquals(SINGLE_CONTENT_TO_MATCH, match);
    }
    
    @Test
    public void testSingleNoParentMatch()
    {
        String match = extractMatch(SRC_DOC, SINGLE_PARENT_CONTENT_TO_MATCH, PARENT_WIDGET_ID); 
        String expected = loadResourceAsDocument(SINGLE_PARENT_CONTENT_TO_MATCH).getElementById(SINGLE_PARENT_CONTENT_TO_MATCH_ID).html();
        assertNull(match);        
    }
    
    @Test
    public void testMutliParentMatch()
    {
        String match = extractMatch(SRC_DOC, MULTI_PARENT_CONTENT_TO_MATCH, MULTI_WIDGET_ID); 
        String expected = loadResourceAsDocument(MULTI_PARENT_CONTENT_TO_MATCH).getElementById(MULTI_PARENT_CONTENT_TO_MATCH_ID).html();
        assertNotNull(match);
        
        assertEquals(expected, match);
    }

    @Test
    public void testNoParentMatch()
    {
        String match = extractMatch(SRC_DOC, MULTI_PARENT_CONTENT_NO_MATCH, MULTI_WIDGET_ID); 
        assertNull(match);        
    }
    
    @Test
    public void testMultiClassMatch()
    {
        String match = extractMatch(MULTI_CLASS_SRC_DOC, MULTI_CLASS_ALL_MATCH, SINGLE_WIDGET_ID); 
        assertNotNull(match);        
    }

    @Test
    public void testMultiClassParentMatch()
    {
        String match = extractMatch(MULTI_CLASS_SRC_DOC, MULTI_CLASS_PARENT_MATCH, SINGLE_WIDGET_ID); 
        assertNotNull(match);        
    }

    @Test
    public void testMultiClassNoParentMatch()
    {
        String match = extractMatch(MULTI_CLASS_SRC_DOC, MULTI_CLASS_NO_PARENT_MATCH, SINGLE_WIDGET_ID); 
        assertNull(match);        
    }

    private String extractMatch(String srcFile, String tgtFile, String widgetId)
    {
        Document sourceDoc = loadResourceAsDocument(srcFile);
        Document targetDoc = loadResourceAsDocument(tgtFile);
        return RULE.findMatchingContent(widgetId, sourceDoc, targetDoc);
    }
    
    private Document loadResourceAsDocument(String fileName)
    {
        String content = PSTestUtils.resourceToString(getClass(), fileName);
        return Jsoup.parseBodyFragment(content);        
    }    
}
