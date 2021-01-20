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
