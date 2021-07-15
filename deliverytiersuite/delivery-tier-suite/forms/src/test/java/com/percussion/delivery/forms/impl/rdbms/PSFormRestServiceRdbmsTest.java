/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.delivery.forms.impl.rdbms;

import com.percussion.delivery.email.data.IPSEmailRequest;
import com.percussion.delivery.forms.IPSFormRestService;
import com.percussion.delivery.forms.data.PSFormSummaries;
import com.percussion.delivery.forms.data.PSFormSummary;
import com.percussion.delivery.forms.impl.PSBaseFormServiceTest;
import com.percussion.delivery.forms.impl.PSFormRestServiceBaseTest;
import com.percussion.delivery.forms.impl.PSMockEmailHelper;
import com.percussion.security.PSEncryptor;
import com.percussion.legacy.security.deprecated.PSLegacyEncrypter;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.internal.PropertiesDelegate;
import org.glassfish.jersey.server.ContainerRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the rest layer service interface directly, by-passing Jersey.  Tests
 * which need to validate the REST interface/Jersey plumbing should 
 * be placed in a different test class extending {@link PSFormRestServiceBaseTest}
 * to leverage Grizzly
 * 
 * @author JaySeletz
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)


@ContextConfiguration(locations = {"classpath:test-beans.xml"})
public class PSFormRestServiceRdbmsTest extends PSBaseFormServiceTest
{
    @Autowired
    private IPSFormRestService formRestService;


    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private String rxdeploydir;

    @Before
    public void setup() throws IOException {

        rxdeploydir = System.getProperty("rxdeploydir");
        System.setProperty("rxdeploydir", temporaryFolder.getRoot().getAbsolutePath());
    }

    @After
    public void teardown(){
        if(rxdeploydir != null)
            System.setProperty("rxdeploydir",rxdeploydir);
    }

    @Test
    @Ignore("TODO: FixMe!")
    public void testSubmitFormWithEmail() throws Exception
    {
    	//FIXME
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };
        String subject = "TestFormSubject";
        String toList = "testEmail@testEmail.com";

        MockResponse resp = submitForm(fieldValue1, fieldValue2, subject, toList);
        assertEquals("success.html", resp.mi_redirect);
        validateEmailSent(fieldValue1, fieldValue2, toList, subject);
    }

    @Test
    @Ignore("TODO: FixMe!")
    public void testSubmitFormWithEmailNotConfigured() throws Exception
    {
    	//FIXME
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };
        String subject = "TestFormSubject";
        String toList = "testEmail@testEmail.com";

        PSMockEmailHelper.setConfigured(false);

        MockResponse resp = submitForm(fieldValue1, fieldValue2, subject, toList);
        assertEquals("success.html", resp.mi_redirect);
        List<IPSEmailRequest> emails = PSMockEmailHelper.getEmailRequests();
        assertNotNull(emails);
        assertTrue(emails.isEmpty());
    }

    @Test
    @Ignore("TODO: FixMe!")
    public void testSubmitFormWithoutEmail() throws IOException
    {
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };

        PSMockEmailHelper.setConfigured(false);

        MockResponse resp = submitForm(fieldValue1, fieldValue2, null, null);
        assertEquals("success.html", resp.mi_redirect);
        List<IPSEmailRequest> emails = PSMockEmailHelper.getEmailRequests();
        assertNotNull(emails);
        assertTrue(emails.isEmpty());
    }

    @Test
    @Ignore("TODO: FixMe!")
    public void testSubmitFormWithEmailError() throws IOException
    {
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };
        String subject = "TestFormSubject";
        String toList = "testEmail@testEmail.com";

        String errorMsg = "Sorry, you lose!";
        PSMockEmailHelper.setError(errorMsg);

        MockResponse resp = submitForm(fieldValue1, fieldValue2, subject, toList);
        assertEquals("success.html", resp.mi_redirect);
        List<IPSEmailRequest> emails = PSMockEmailHelper.getEmailRequests();
        assertNotNull(emails);
        assertTrue(emails.isEmpty());
    }

    @Test
    @Ignore("TODO: Fix Me!")
    public void testSubmitFormWithInvalidName() throws IOException
    {
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };
        String subject = "TestFormSubject";
        String toList = "testEmail@testEmail.com";

        for (int i = 0; i < INVALID_TEST_CHARS.length(); i++)
        {
            String formName = INVALID_FORM_NAME_PREFIX + INVALID_TEST_CHARS.charAt(i);

            MockResponse resp = submitForm(fieldValue1, fieldValue2, subject, toList,formName);
            assertEquals("error.html", resp.mi_redirect);
        }


    }

    @Test
    @Ignore("TODO: FixMe!")
    public void getSummariesWithInvalidFormNamesInDb() throws IOException
    {
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };
        String subject = "TestFormSubject";
        String toList = "testEmail@testEmail.com";

        MockResponse resp = submitForm(fieldValue1, fieldValue2, subject, toList);
        assertEquals("success.html", resp.mi_redirect);

        addInvalidFormToDb();
        String pattern = "^[a-zA-Z0-9_\\-]*$";
        try
        {
            PSFormSummaries summaries = formRestService.get();
            assertEquals("Expecting one valid summary",1,summaries.getSummaries().size());
            for (PSFormSummary summary : summaries.getSummaries())
            {
                String formName = summary.getName();

                if (!formName.matches(pattern))
                {
                    fail("found form with invalid characters " + formName);
                }
            }
        }
        finally
        {
            removeInvalidFormsFromDb();
        }
    }

    private MockResponse submitForm(String[] fieldValue1, String[] fieldValue2, String subject, String toList)
            throws IOException
    {
        return submitForm(fieldValue1,fieldValue2,subject,toList,"testFormEmail");
    }

    private MockResponse submitForm(String[] fieldValue1, String[] fieldValue2, String subject, String toList, String formName)
            throws IOException
    {
        HttpHeaders header = new MockHeaders();
        MockResponse resp = new MockResponse();
        MockHttpServletRequest request = new MockHttpServletRequest();

        String action = "";
        MultivaluedMap<String, String> params = new MultivaluedHashMap();
        params.put("perc_formName", Arrays.asList(formName));
        if (subject != null && toList != null)
        {
            params.put("perc_emnt", Arrays.asList(PSLegacyEncrypter.getInstance(
                    temporaryFolder.getRoot().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
            ).encrypt(toList, PSLegacyEncrypter.getInstance(
                    temporaryFolder.getRoot().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
            ).DEFAULT_KEY())));
            params.put("perc_emns", Arrays.asList(PSLegacyEncrypter.getInstance(
                    temporaryFolder.getRoot().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
            )
                    .encrypt(subject, PSLegacyEncrypter.getInstance(
                            temporaryFolder.getRoot().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
                    ).DEFAULT_KEY())));
        }

        params.put(fieldValue1[0], Arrays.asList(fieldValue1[1]));
        params.put(fieldValue2[0], Arrays.asList(fieldValue2[1]));

     //   formRestService.create(containerRequest, action, header, request, resp);
        return resp;
    }
    private class MockResponse implements HttpServletResponse
    {

        private String mi_redirect;

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#getCharacterEncoding()
         */
        @Override
        public String getCharacterEncoding()
        {
            // noop
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#getContentType()
         */
        @Override
        public String getContentType()
        {
            // noop
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#getOutputStream()
         */
        @Override
        public ServletOutputStream getOutputStream()
        {
            // noop
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#getWriter()
         */
        @Override
        public PrintWriter getWriter()
        {
            // noop
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
         */
        @Override
        public void setCharacterEncoding(String charset)
        {
            // noop

        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#setContentLength(int)
         */
        @Override
        public void setContentLength(int len)
        {
            // noop

        }

        @Override
        public void setContentLengthLong(long l) {

        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
         */
        @Override
        public void setContentType(String type)
        {
            // noop

        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#setBufferSize(int)
         */
        @Override
        public void setBufferSize(int size)
        {
            // noop

        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#getBufferSize()
         */
        @Override
        public int getBufferSize()
        {
            // noop
            return 0;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#flushBuffer()
         */
        @Override
        public void flushBuffer()
        {
            // noop

        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#resetBuffer()
         */
        @Override
        public void resetBuffer()
        {
            // noop

        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#isCommitted()
         */
        @Override
        public boolean isCommitted()
        {
            // noop
            return false;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#reset()
         */
        @Override
        public void reset()
        {
            // noop

        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
         */
        @Override
        public void setLocale(Locale loc)
        {
            // noop

        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#getLocale()
         */
        @Override
        public Locale getLocale()
        {
            // noop
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
         */
        @Override
        public void addCookie(javax.servlet.http.Cookie cookie)
        {
            // noop

        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
         */
        @Override
        public boolean containsHeader(String name)
        {
            // noop
            return false;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
         */
        @Override
        public String encodeURL(String url)
        {
            // noop
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
         */
        @Override
        public String encodeRedirectURL(String url)
        {
            // noop
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
         */
        @Override
        public String encodeUrl(String url)
        {
            // noop
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
         */
        @Override
        public String encodeRedirectUrl(String url)
        {
            // noop
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
         */
        @Override
        public void sendError(int sc, String msg)
        {
            // noop

        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#sendError(int)
         */
        @Override
        public void sendError(int sc)
        {
            // noop

        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
         */
        @Override
        public void sendRedirect(String location)
        {
            mi_redirect = StringUtils.substringAfterLast(location, "/");
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
         */
        @Override
        public void setDateHeader(String name, long date)
        {
            // noop

        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
         */
        @Override
        public void addDateHeader(String name, long date)
        {
            // noop

        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
         */
        @Override
        public void setHeader(String name, String value)
        {
            // noop

        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
         */
        @Override
        public void addHeader(String name, String value)
        {
            // noop

        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
         */
        @Override
        public void setIntHeader(String name, int value)
        {
            // noop

        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
         */
        @Override
        public void addIntHeader(String name, int value)
        {
            // noop

        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#setStatus(int)
         */
        @Override
        public void setStatus(int sc)
        {
            // noop

        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#setStatus(int, java.lang.String)
         */
        @Override
        public void setStatus(int sc, String sm)
        {
            // noop

        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#getStatus()
         */
        public int getStatus()
        {
            // noop
            return 0;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#getHeader(java.lang.String)
        */
        public String getHeader(String name)
        {
            // noop
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#getHeaders(java.lang.String)
         */
        public Collection<String> getHeaders(String name)
        {
            // noop
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#getHeaderNames()
         */
        public Collection<String> getHeaderNames()
        {
            // noop
            return null;
        }

        public void setTrailerFields(Supplier<Map<String, String>> supplier) {

    }

        public Supplier<Map<String, String>> getTrailerFields() {
            return null;
        }

    }

    private class MockContainerRequest extends ContainerRequest{

        /**
         * Create new Jersey container request context.
         *
         * @param baseUri            base application URI.
         * @param requestUri         request URI.
         * @param httpMethod         request HTTP method name.
         * @param securityContext    security context of the current request. Must not be {@code null}.
         *                           The {@link SecurityContext#getUserPrincipal()} must return
         *                           {@code null} if the current request has not been authenticated
         *                           by the container.
         * @param propertiesDelegate custom {@link PropertiesDelegate properties delegate}
         *                           to be used by the context.
         * @param configuration      the server {@link Configuration}. If {@code null}, the default behaviour is expected.
         */
        public MockContainerRequest(URI baseUri, URI requestUri, String httpMethod, SecurityContext securityContext, PropertiesDelegate propertiesDelegate, Configuration configuration) {
            super(baseUri, requestUri, httpMethod, securityContext, propertiesDelegate, configuration);
        }
    }

    private class MockHeaders implements HttpHeaders
    {

        public String getHeaderString(String key){
            return null;
        }

        /* (non-Javadoc)
         * @see javax.ws.rs.core.HttpHeaders#getRequestHeader(java.lang.String)
         */
        @Override
        public List<String> getRequestHeader(String name)
        {
            // noop
            return null;
        }

        /* (non-Javadoc)
         * @see javax.ws.rs.core.HttpHeaders#getRequestHeaders()
         */
        @Override
        public MultivaluedMap<String, String> getRequestHeaders()
        {
            // noop
            return null;
        }

        /* (non-Javadoc)
         * @see javax.ws.rs.core.HttpHeaders#getAcceptableMediaTypes()
         */
        @Override
        public List<MediaType> getAcceptableMediaTypes()
        {
            // noop
            return null;
        }

        /* (non-Javadoc)
         * @see javax.ws.rs.core.HttpHeaders#getAcceptableLanguages()
         */
        @Override
        public List<Locale> getAcceptableLanguages()
        {
            // noop
            return null;
        }

        /* (non-Javadoc)
         * @see javax.ws.rs.core.HttpHeaders#getMediaType()
         */
        @Override
        public MediaType getMediaType()
        {
            // noop
            return null;
        }

        /* (non-Javadoc)
         * @see javax.ws.rs.core.HttpHeaders#getLanguage()
         */
        @Override
        public Locale getLanguage()
        {
            // noop
            return null;
        }

        /* (non-Javadoc)
         * @see javax.ws.rs.core.HttpHeaders#getCookies()
         */
        @Override
        public Map<String, Cookie> getCookies()
        {
            // noop
            return null;
        }

        public int getLength(){
            return 0;
        }

        public Date getDate(){
            return Date.from(Instant.now());
        }
    }
}
