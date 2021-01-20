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
package com.percussion.sitemanage.importer.utils;

import static org.junit.Assert.*;

import com.percussion.sitemanage.importer.IPSConnectivity;
import com.percussion.sitemanage.importer.helpers.PSHelperTestUtils;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;


/**
 * Test the fact that {@link PSHtmlRetriever} class relies on specific text in the IOException thrown by JSoup.  This
 * test verifies that for the know case where we expect that text, we interpret the exception correctly.
 * This will fail if a newer version of JSoup is introduced and the exception text changes or is handled 
 * differently.
 * 
 * @author JaySeletz
 *
 */
public class PSHtmlRetrieverTest
{
    @Test
    public void testHtml() throws Exception
    {
        String url = "http://samples.percussion.com/";
        PSHtmlRetriever ret = new PSHtmlRetriever(new PSTestConn(url));
        Document doc = ret.getHtmlDocument();
        assertNotNull(doc);        
    }
    
    @Test
    public void test404() throws Exception
    {
        boolean didThrow = false;
        String url = "http://samples.percussion.com/foo";
        PSHtmlRetriever ret = new PSHtmlRetriever(new PSTestConn(url));
        try
        {
            ret.getHtmlDocument();
            fail("Expected IOException to be thrown");
        }
        catch (IOException e)
        {
            didThrow = true;
        }
        
        assertTrue(didThrow);
    }
    
    @Test
    public void testNonHtmlContent() throws Exception
    {
        String url = "http://samples.percussion.com/assets/snow.jpg";
        PSHtmlRetriever ret = new PSHtmlRetriever(new PSTestConn(url));
        Document doc = ret.getHtmlDocument();
        assertNull(doc); 
        
        // make sure it's really there
        PSTestConn testConnectivity = new PSTestConn(url);
        Connection connection = testConnectivity.getConnection();
        connection.ignoreContentType(true);
        doc = connection.get();
        assertNotNull(doc);
    }
    
    private class PSTestConn implements IPSConnectivity
    {
        Connection mi_conn;
        
        private PSTestConn(String url)
        {
            mi_conn = Jsoup.connect(url);
            mi_conn.ignoreContentType(false);
            mi_conn.followRedirects(false);
            mi_conn.userAgent(PSHelperTestUtils.USER_AGENT);  

        }
        
        @Override
        public Document get() throws IOException
        {
            return mi_conn.get();
        }

        @Override
        public int getResponseStatusCode()
        {
            return mi_conn.response().statusCode();
        }

        @Override
        public String getResponseUrl()
        {
            return mi_conn.response().url().toString();
        }
        
        public Connection getConnection()
        {
            return mi_conn;
        }
    }
}
