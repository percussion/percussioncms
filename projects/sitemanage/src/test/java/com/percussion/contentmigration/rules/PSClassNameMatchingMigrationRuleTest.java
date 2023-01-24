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
