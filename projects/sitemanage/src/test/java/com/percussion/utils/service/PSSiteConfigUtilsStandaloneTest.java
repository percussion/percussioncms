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

package com.percussion.utils.service;

import static org.junit.Assert.*;

import com.percussion.sitemanage.data.PSSectionNode;
import com.percussion.utils.service.impl.PSSiteConfigUtils;
import com.percussion.utils.service.impl.PSSiteConfigUtils.SecureXmlData;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.StringReader;

import org.junit.Test;
import org.w3c.dom.Document;

public class PSSiteConfigUtilsStandaloneTest
{

    @Test
    public void testGenerateCacheControlFilters() throws Exception
    {
        SecureXmlData xmlData = new SecureXmlData();
        xmlData.addSecureOrMemberSection("/section1/", "");
        xmlData.addSecureOrMemberSection("/section2/section2-2/", "editor,admin");
        xmlData.addSecureOrMemberSection("/section3/section3-1/", "");
        Document sourceDoc = getWebXmlDoc("source-web.xml");
        Document expectedDoc1 = getWebXmlDoc("expected1-web.xml");
        Document expectedDoc2 = getWebXmlDoc("expected2-web.xml");
        Document expectedDoc3 = getWebXmlDoc("expected3-web.xml");
        
        PSSiteConfigUtils.generateCacheControlFilters(xmlData, sourceDoc);        
        assertXmlEquals(expectedDoc1, sourceDoc);
        
        // test update
        xmlData = new SecureXmlData();
        xmlData.addSecureOrMemberSection("/section4/section4-1/", "");
        PSSiteConfigUtils.generateCacheControlFilters(xmlData, sourceDoc);
        
        assertXmlEquals(expectedDoc2, sourceDoc);
        
        // test no section
        xmlData = new SecureXmlData();
        PSSiteConfigUtils.generateCacheControlFilters(xmlData, sourceDoc);
        
        assertXmlEquals(expectedDoc3, sourceDoc);
    }

    /**
     * @param expectedDoc
     * @param resultDoc
     */
    private void assertXmlEquals(Document expectedDoc, Document resultDoc) throws Exception
    {
        String expected = PSXmlDocumentBuilder.toString(expectedDoc);
        String result = PSXmlDocumentBuilder.toString(resultDoc);
        
        expected = PSXmlDocumentBuilder.createXmlDocument(new StringReader(expected), false).toString();
        result = PSXmlDocumentBuilder.createXmlDocument(new StringReader(result), false).toString();
        
        assertEquals(expected, result);
    }

    /**
     * @return
     */
    private Document getWebXmlDoc(String name) throws Exception
    {
        return PSXmlDocumentBuilder.createXmlDocument(this.getClass().getResourceAsStream(name), false);
    }

}
