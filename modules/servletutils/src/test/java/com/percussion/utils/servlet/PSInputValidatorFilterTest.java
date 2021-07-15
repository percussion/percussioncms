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
package com.percussion.utils.servlet;

import static com.percussion.util.PSResourceUtils.getResourcePath;
import static com.percussion.utils.servlet.PSInputValidatorFilter.RESPONSE_ERROR_STATUS;
import static com.percussion.utils.servlet.PSInputValidatorFilter.VALIDATOR_CONFIG_RESOURCE_PROP_NAME;
import static com.percussion.utils.servlet.PSInputValidatorFilter.VALIDATOR_ENABLE_PROP_NAME;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.percussion.utils.testing.IntegrationTest;
import junit.framework.TestCase;

import org.junit.experimental.categories.Category;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;


/**
 * @author erikserating
 * @author adamgent
 *
 */
@Category(IntegrationTest.class)
public class PSInputValidatorFilterTest extends TestCase
{
    
    private PSInputValidatorFilter filter;
    private MockHttpServletResponse response = new MockHttpServletResponse();
    private MockFilterChain filterChain = new MockFilterChain();
    private MockFilterConfig filterConfig;
        
    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        String filePath = getResourcePath(PSInputValidatorFilterTest.class,"/com/percussion/utils/servlet/"+ getClass().getSimpleName()
                + ".properties");

        URL url = new File(filePath).toURI().toURL();
        setupFilter("true", url.toExternalForm());
    }

   private void setupFilter(String enable, String customConfigPath) throws ServletException
   {
      filter = new PSInputValidatorFilter();
      filterConfig = new MockFilterConfig();
      MockServletContext context = (MockServletContext) filterConfig.getServletContext();
      if (enable != null)
         context.addInitParameter(VALIDATOR_ENABLE_PROP_NAME, enable);
      if (customConfigPath != null)
         context.addInitParameter(VALIDATOR_CONFIG_RESOURCE_PROP_NAME, customConfigPath);
      filter.init(filterConfig);
   }
    
    // Parameter name constants from test properties
    private static final String PARAM_TEST_NUMERIC = "testNumeric";
    private static final String PARAM_TEST_BOOLEAN = "testBool";
    private static final String PARAM_TEST_GUID = "testGuid";
    private static final String PARAM_TEST_MULTI_NO = "testNoCcNoLtGtNoQ";
    private static final String PARAM_TEST_NOLTGT = "testNoLtGt";
    private static final String PARAM_TEST_NOCC = "testNoCc";
    private static final String PARAM_TEST_NOQUOTES = "testNoQ";
    private static final String PARAM_TEST_NORESTRICT = "testNoRestrict";
    private static final String PARAM_TEST_SINGLE_REGEX = "testSingleRegex"; // [^Z]*
    private static final String PARAM_TEST_MULTI_REGEX = "testMultiRegex"; // [^X]* and [^Y]* 
    private static final String PARAM_TEST_REGEX_NOLTGT = "testRegexNoLtGt"; // [^T]*
    

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        
    }
    private void assertErrorMessage(String badParam, String goodParam)
    {
       String actualBody = response.getErrorMessage();
         assertTrue(actualBody.contains(badParam));
         if (goodParam != null)
            assertFalse(actualBody.contains(goodParam));
    }

    private void assertErrorStatus()
    {
       int actualStatus = response.getStatus();
         assertEquals("status should be", RESPONSE_ERROR_STATUS, actualStatus);
    }
    
    private void assertOkStatus()
    {
       int actualStatus = response.getStatus();
         assertEquals("status should be", 200, actualStatus);
    }
    
    public void testRestrictToGuid() throws Exception
    {
        HttpServletRequest req = createMockRequest(
           "sys_contentid", "NonGuidValue",
           "sys_folderid", "334"
           
        );
        filter.doFilter(req, response, filterChain);
        assertErrorMessage("sys_contentid", "sys_folderid");
        assertErrorStatus();
    }
    
    public void testNoRestrict() throws Exception
    {
        HttpServletRequest req = createMockRequest(
           PARAM_TEST_NORESTRICT, "NonGuidValue <"           
        );
        filter.doFilter(req, response, filterChain);
        assertOkStatus();
        
    }
    
    public void testInvalidParamNameRemoval() throws Exception
    {
        HttpServletRequest req = createMockRequest(
           "<script>alert('')</script>", "NonGuidValue",
           "sys_folderid", "334"           
        );
        filter.doFilter(req, response, filterChain);
        assertErrorStatus();
    }
//    
    public void testNoControlChars() throws Exception
    {
        //If this test fails then the custom property files
        //is probably not loading.

        HttpServletRequest req = createMockRequest(PARAM_TEST_NOCC, "\u0000NonGuidValue");
        filter.doFilter(req, response, filterChain);
        assertErrorStatus();
        assertErrorMessage(PARAM_TEST_NOCC, null);
    }
//    
//    public void testNoQuotes() throws Exception
//    {
//       
//        HttpServletRequest req = createMockRequest(
//           PARAM_TEST_NOQUOTES, "\nNonGuid\"Va\"lue",
//           PARAM_TEST_MULTI_NO, "testpath"                        
//        );
//        HttpServletRequest result = filter.cleanseRequest(req);
//        assertEquals("", result.getParameter(PARAM_TEST_NOQUOTES));
//        assertEquals("testpath", result.getParameter(PARAM_TEST_MULTI_NO)); 
//        
//        
//        req = createMockRequest(PARAM_TEST_NOQUOTES, "\u0000Non'GuidValue");
//        result = filter.cleanseRequest(req);
//        assertEquals("", result.getParameter(PARAM_TEST_NOQUOTES));        
//        
//    }    
//    
//    public void testRestrictToBoolean() throws Exception
//    {
//        HttpServletRequest req = createMockRequest(PARAM_TEST_BOOLEAN, "NonBoolValue");
//        HttpServletRequest result = filter.cleanseRequest(req);
//        assertEquals("", result.getParameter(PARAM_TEST_BOOLEAN));
//        
//        req = createMockRequest(PARAM_TEST_BOOLEAN, "true");
//        result = filter.cleanseRequest(req);
//        assertEquals("true", result.getParameter(PARAM_TEST_BOOLEAN));
//        
//        req = createMockRequest(PARAM_TEST_BOOLEAN, "false");
//        result = filter.cleanseRequest(req);
//        assertEquals("false", result.getParameter(PARAM_TEST_BOOLEAN));
//        
//        req = createMockRequest(PARAM_TEST_BOOLEAN, "TRUE");
//        result = filter.cleanseRequest(req);
//        assertEquals("TRUE", result.getParameter(PARAM_TEST_BOOLEAN));
//        
//        req = createMockRequest(PARAM_TEST_BOOLEAN, "FALSE");
//        result = filter.cleanseRequest(req);
//        assertEquals("FALSE", result.getParameter(PARAM_TEST_BOOLEAN));
//        
//        req = createMockRequest(PARAM_TEST_BOOLEAN, "yes");
//        result = filter.cleanseRequest(req);
//        assertEquals("yes", result.getParameter(PARAM_TEST_BOOLEAN));
//        
//        req = createMockRequest(PARAM_TEST_BOOLEAN, "no");
//        result = filter.cleanseRequest(req);
//        assertEquals("no", result.getParameter(PARAM_TEST_BOOLEAN));
//        
//        req = createMockRequest(PARAM_TEST_BOOLEAN, "YES");
//        result = filter.cleanseRequest(req);
//        assertEquals("YES", result.getParameter(PARAM_TEST_BOOLEAN));
//        
//        req = createMockRequest(PARAM_TEST_BOOLEAN, "NO");
//        result = filter.cleanseRequest(req);
//        assertEquals("NO", result.getParameter(PARAM_TEST_BOOLEAN));
//    }
//    
//    public void testRestrictToNumeric() throws Exception
//    {
//        HttpServletRequest req = createMockRequest(PARAM_TEST_NUMERIC, "3NonNumericValue2");
//        HttpServletRequest result = filter.cleanseRequest(req);
//        assertEquals("", result.getParameter(PARAM_TEST_NUMERIC));
//        
//        req = createMockRequest(PARAM_TEST_NUMERIC, "10");
//        result = filter.cleanseRequest(req);
//        assertEquals("10", result.getParameter(PARAM_TEST_NUMERIC));
//        
//        req = createMockRequest(PARAM_TEST_NUMERIC, "10.5");
//        result = filter.cleanseRequest(req);
//        assertEquals("10.5", result.getParameter(PARAM_TEST_NUMERIC));
//        
//        req = createMockRequest(PARAM_TEST_NUMERIC, ".5");
//        result = filter.cleanseRequest(req);
//        assertEquals(".5", result.getParameter(PARAM_TEST_NUMERIC));
//        
//        req = createMockRequest(PARAM_TEST_NUMERIC, "1.345");
//        result = filter.cleanseRequest(req);
//        assertEquals("1.345", result.getParameter(PARAM_TEST_NUMERIC));
//        
//        req = createMockRequest(PARAM_TEST_NUMERIC, "-2");
//        result = filter.cleanseRequest(req);
//        assertEquals("-2", result.getParameter(PARAM_TEST_NUMERIC));
//        
//        req = createMockRequest(PARAM_TEST_NUMERIC, "1.3.5");
//        result = filter.cleanseRequest(req);
//        assertEquals("", result.getParameter(PARAM_TEST_NUMERIC));        
//        
//    }
//    
//    public void testNoLtGt()
//    {
//        HttpServletRequest req = createMockRequest(PARAM_TEST_NOLTGT, "value");
//        HttpServletRequest result = filter.cleanseRequest(req);
//        assertEquals("value", result.getParameter(PARAM_TEST_NOLTGT));
//        
//        req = createMockRequest(PARAM_TEST_NOLTGT, "value<script>alert('lol')</script>");
//        result = filter.cleanseRequest(req);
//        assertEquals("", result.getParameter(PARAM_TEST_NOLTGT));
//        
//        req = createMockRequest(PARAM_TEST_NOLTGT, "<iframe");
//        result = filter.cleanseRequest(req);
//        assertEquals("", result.getParameter(PARAM_TEST_NOLTGT));
//        
//    }    
//    
//    public void testCustomRegex()
//    {
//        HttpServletRequest req = createMockRequest(PARAM_TEST_SINGLE_REGEX, "value");
//        HttpServletRequest result = filter.cleanseRequest(req);
//        assertEquals("value", result.getParameter(PARAM_TEST_SINGLE_REGEX));
//        
//        req = createMockRequest(PARAM_TEST_SINGLE_REGEX, "Contains Z");
//        result = filter.cleanseRequest(req);
//        assertEquals("", result.getParameter(PARAM_TEST_SINGLE_REGEX));
//        
//        req = createMockRequest(PARAM_TEST_MULTI_REGEX, "Contains Z");
//        result = filter.cleanseRequest(req);
//        assertEquals("Contains Z", result.getParameter(PARAM_TEST_MULTI_REGEX));
//        
//        req = createMockRequest(PARAM_TEST_MULTI_REGEX, "Contains X");
//        result = filter.cleanseRequest(req);
//        assertEquals("", result.getParameter(PARAM_TEST_MULTI_REGEX));
//        
//        req = createMockRequest(PARAM_TEST_MULTI_REGEX, "Contains Y");
//        result = filter.cleanseRequest(req);
//        assertEquals("", result.getParameter(PARAM_TEST_MULTI_REGEX));
//        
//        req = createMockRequest(PARAM_TEST_REGEX_NOLTGT, "Contains T");
//        result = filter.cleanseRequest(req);
//        assertEquals("", result.getParameter(PARAM_TEST_REGEX_NOLTGT));
//        
//        req = createMockRequest(PARAM_TEST_REGEX_NOLTGT, "Contains X <script");
//        result = filter.cleanseRequest(req);
//        assertEquals("", result.getParameter(PARAM_TEST_REGEX_NOLTGT));
//        
//    }
    private HttpServletRequest createMockRequest(String... params)
    {
        Map<String, String> paramMap = new HashMap<String, String>();
        for(int i = 0; i < (params.length - 1); i += 2)
        {
           paramMap.put(params[i], params[i + 1]);    
        }
        MockHttpServletRequest request = new MockHttpServletRequest();
        if(params != null)
           request.addParameters(paramMap);
        return request;
        
    }
    
    

}
