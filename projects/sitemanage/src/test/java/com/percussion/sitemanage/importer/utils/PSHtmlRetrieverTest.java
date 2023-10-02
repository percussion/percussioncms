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
package com.percussion.sitemanage.importer.utils;

import static org.junit.Assert.*;

import com.percussion.sitemanage.importer.IPSConnectivity;
import com.percussion.sitemanage.importer.helpers.PSHelperTestUtils;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Ignore;
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
    @Ignore
    public void testHtml() throws Exception
    {
        String url = "http://samples.percussion.com/";
        PSHtmlRetriever ret = new PSHtmlRetriever(new PSTestConn(url));
        Document doc = ret.getHtmlDocument();
        assertNotNull(doc);        
    }
    
    @Test
    @Ignore
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
    @Ignore
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
