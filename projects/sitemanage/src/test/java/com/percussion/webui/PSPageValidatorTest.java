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
package com.percussion.webui;

import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.security.xml.PSXmlSecurityOptions;
import com.percussion.share.test.PSMatchers;
import com.percussion.share.test.PSRestTestCase;
import org.apache.commons.httpclient.HttpException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

/**
 * The test methods in this class query the main pages from a running server and
 * validate certain properties such as XML well-formedness and no tabs.
 * <p>
 * This is in the sitemanage project because the webui project is not setup to
 * support java code. For that reason, I didn't use base classes from the
 * sitemanage project.
 * <p>
 * There is 1 test for each primary page in the application.
 * 
 * TODO Use {@link PSMatchers#validXhtml()}
 * @author paulhoward
 */
@Ignore public class PSPageValidatorTest extends PSRestTestCase<PSPageValidatorRestClient>
{
    private static PSPageValidatorRestClient restClient;
    
    /**
     * Creates a connection and logs in using an indirect technique.
     * 
     * @throws Exception
     */
    @BeforeClass
    public static void setUp() throws Exception
    {
        restClient = new PSPageValidatorRestClient(baseUrl);
        setupClient(restClient);
    }
    
    /* (non-Javadoc)
     * @see com.percussion.share.test.PSRestTestCase#getRestClient(java.lang.String)
     */
    @Override
    protected PSPageValidatorRestClient getRestClient(String baseUrl)
    {
        return restClient;
    }
    
    @AfterClass
    public static void tearDown() throws Exception
    {
    }

    @Test
    public final void testEditorPage() throws Exception
    {
        validatePage("editor");
    }

    @Test
    public final void testDesignPage() throws Exception
    {
        validatePage("design");
    }

    @Test
    public final void testSiteArchPage() throws Exception
    {
        validatePage("arch");
    }

    @Ignore
    public final void testPublishPage() throws Exception
    {
        validatePage("publish");
    }

    @Test
    public final void testUserMgtPage() throws Exception
    {
        validatePage("users");
    }

    /**
     * Requests the page using the supplied method, gets the response body and
     * attempts to parse it into a w3c DOM document to check for
     * well-formedness. It removes the leading DOCTYPE and replaces it with an
     * internal DOCTYPE that includes the nbsp entity.
     * 
     * @param viewName Assumed not <code>null</code> or empty. The value for the view
     * parameter supplied in the page request URL.
     * 
     * @throws HttpException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    private void validatePage(String viewName) 
            throws HttpException, IOException, ParserConfigurationException, SAXException
    {
        String src = restClient.getPage(viewName);
        
        src = fixupDoctype(src);
        
        DocumentBuilderFactory factory = PSSecureXMLUtils.getSecuredDocumentBuilderFactory(
                new PSXmlSecurityOptions(
                        true,
                        true,
                        true,
                        false,
                        true,
                        false
                )
        );
        factory.setValidating(false);

        DocumentBuilder parser = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(src));
        parser.parse(is);
        
        Assert.assertTrue("File contains 1 or more tabs.", src.indexOf('\t') < 0);
    }
    
    /**
     * Modifies the DOCTYPE so it points to a local copy of the transitional dtd
     * rather than on the web.
     * 
     * @param src Assumed not <code>null</code>.
     * 
     * @return The supplied string with everything up to the opening html tag
     * removed and replaced with the internal DOCTYPE.
     */
    private String fixupDoctype(String src)
    {
        return src.replace("http://www.w3.org/TR/xhtml1/DTD", "src/test/java/com/percussion/webui");
    }
}
