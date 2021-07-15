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
package com.percussion.sitemanage.importer;

import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.percussion.sitemanage.data.PSPageContent;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogObjectType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author LucasPiccoli
 *
 */
@Category(IntegrationTest.class)
@Ignore public class PSSiteImporterTest
{
    
    private String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:12.0) Gecko/20100101 Firefox/12.0";
    

    /**
     * Placeholder test to keep junit happy, all other tests ignored as tech debt
     */
    @Test
    public void testNothing()
    {
        
    }
    
    @Ignore
    public void ignore_testConnectToUrl()
    {
        PSSiteImportCtx importContext = new PSSiteImportCtx();
        importContext.setLogger(new PSSiteImportLogger(PSLogObjectType.SITE));
        importContext.setUserAgent(USER_AGENT);
        
        try
        {
            List<String> invalidUrls = new ArrayList<String>();
            invalidUrls.add("");
            invalidUrls.add(null);
            invalidUrls.add("#$%");
            invalidUrls.add("www.badurl-");
            invalidUrls.add("www.urlwithouthttp.com");

            for (String url : invalidUrls)
            {
                try
                {
                    importContext.setSiteUrl(url);
                    PSSiteImporter.getPageContentFromSite(importContext);
                    fail("Invalid URL passed.");
                }
                catch(Exception e)
                {
                    assertTrue(e.getClass().equals(IllegalArgumentException.class));
                }            
            }
            
            String unreachableUrl = "http://www.unreachable123321123321123321.com";
            try
            {
                importContext.setSiteUrl(unreachableUrl);
                PSSiteImporter.getPageContentFromSite(importContext);
                fail("Unreachable URL didn't throw exception.");
            }
            catch(IOException e)
            {
            }
            
            String validUrl = "http://www.percussion.com";
            try
            {
                importContext.setSiteUrl(validUrl);
                PSSiteImporter.getPageContentFromSite(importContext);
            }
            catch(IOException e)
            {
                fail("Couldn't connect to existing URL.");
            }            
        }
        catch(RuntimeException e)
        {
            fail("The following error occurred: "+ e.getMessage());
        }
    }
    
    @Ignore
    public void ignore_testParsing()
    {
        String validUrl = "http://www.percussion.com";
        try
        {
            PSSiteImportCtx importContext = new PSSiteImportCtx();
            importContext.setLogger(new PSSiteImportLogger(PSLogObjectType.SITE));
            importContext.setSiteUrl(validUrl);
            importContext.setUserAgent(USER_AGENT);
            
            PSPageContent pageContent = PSSiteImporter.getPageContentFromSite(importContext);
            assertNotNull(pageContent.getTitle());
            assertNotNull(pageContent.getHeadContent());
            assertNotSame("", pageContent.getHeadContent());
            assertNotNull(pageContent.getBodyContent());
            assertNotSame("", pageContent.getBodyContent());
        }
        catch(RuntimeException e)
        {
            fail("The document couldn't be parsed: "+ e.getMessage());
        }
        catch(IOException e)
        {
            fail("Couldn't connect to existing URL.");
        }
    }    
    
    @Ignore
    public void ignore_testGetRedirectedUrl_302Response()
    {
        try
        {
            String url = "http://www.firefox.com";

            if(!isHostReacheable(url))
            {
                return;
            }

            IPSSiteImportLogger logger = new PSSiteImportLogger(PSLogObjectType.TEMPLATE);
            String redirectedUrl = PSSiteImporter.getRedirectedUrl(url, logger, USER_AGENT);
            
            assertRedirection(url, redirectedUrl, logger);
        }
        catch (IOException e)
        {
            fail("No exception should have been thrown.");
        }        
    }
        
    @Ignore
    public void ignore_testGetRedirectedUrl_301Response()
    {
        try
        {
            String url = "http://firefox.com";

            if(!isHostReacheable(url))
            {
                return;
            }
            
            IPSSiteImportLogger logger = new PSSiteImportLogger(PSLogObjectType.TEMPLATE);
            String redirectedUrl = PSSiteImporter.getRedirectedUrl(url, logger, USER_AGENT);

            assertRedirection(url, redirectedUrl, logger);
        }
        catch (IOException e)
        {
            fail("No exception should have been thrown.");
        }        
    }

    @Ignore
    public void ignore_testGetRedirectedUrl_notRedirected()
    {
        try
        {
            String url = "http://www.percussion.com";

            if(!isHostReacheable(url))
            {
                return;
            }
            
            IPSSiteImportLogger logger = new PSSiteImportLogger(PSLogObjectType.TEMPLATE);
            String redirectedUrl = PSSiteImporter.getRedirectedUrl(url, logger, USER_AGENT);
            
            assertTrue("The original site should not have been redirected, but was redirected from '" + url + "' to '"
                    + redirectedUrl + "'.", equalsIgnoreCase(url, redirectedUrl));
        }
        catch (IOException e)
        {
            fail("No exception should have been thrown.");
        }        
    }

    /**
     * Checks if the given host is reacheable or not.
     * 
     * @param url {@link String} with the url to check. Assumed not
     *            <code>null</code>.
     * @return <code>true</code> if the response to the request is lower than
     *         <code>HTTP 400 response</code>. <code>false</code> otherwise.
     */
    private boolean isHostReacheable(String url)
    {
        try
        {
            Connection con = Jsoup.connect(url);
            con.ignoreContentType(true);
            con.followRedirects(true);
            con.get();
            Response response = con.response();
         
            if (response.statusCode() >= 400)
            {
                return false;
            }
            return true;
        }
        catch (IOException e)
        {
            return false;
        }        
    }

    /**
     * Checks that the url was correctly changed and the logs reflect that
     * situation.
     * 
     * @param url {@link String} with the original url. Assumed not
     *            <code>null</code>.
     * @param newUrl {@link String} with the url that the request will be
     *            redirected to. Assumed not <code>null</code>.
     * @param redirectedUrl {@link String} with the url that the system will
     *            redirect the request. Assumed not <code>null</code>.
     * @param logger {@link IPSSiteImportLogger} to check the log messages.
     *            Assumed not <code>null</code>.
     */
    private void assertRedirection(String url, String redirectedUrl, IPSSiteImportLogger logger)
    {
        assertTrue("The original site was not redirected.", !url.equalsIgnoreCase(redirectedUrl));
        
        String logLine = PSSiteImporter.REDIRECTED_FROM_URL.replace("{originalUrl}", url);
        int lineEnd = logLine.indexOf(url) + url.length();
        logLine = logLine.substring(0, lineEnd);
        assertTrue("The logging messages should contain the log line indicating the redirection", logger.getLog()
                .contains(logLine));
    }

}
