/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package test.percussion.pso.legacy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.pso.legacy.PSOProxyQueryResource;
import com.percussion.server.IPSRequestContext;

@RunWith(JMock.class)
public class PSOProxyQueryResourceTest {
    Mockery context = new JUnit4Mockery();
    PSOProxyQueryResource proxy = new PSOProxyQueryResource();
    
    @Before
    public void setUp() throws Exception {
        proxy.init(makeExtensionDef("url"), null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    @Ignore
    //TODO: Fix Me
    public void shouldFailIfUrlIsNotProvided() throws Exception {
        IPSRequestContext request = makeRequest(makeRequestParams(null, null, null));
        proxy.processResultDocument(new Object[]{}, request, null);
    }
    
    @Test
    public void shouldReturnAnXmlDocumentFromTheUrlProvidedAsAnExtensionParameter() throws Exception {
        IPSRequestContext request = makeRequest(makeRequestParams(null, null, null));
        Object[] eParams = makeParams("http://news.google.com/?output=atom");
        Document doc = proxy.processResultDocument(eParams, request, null);
        assertNotNull(doc);
        assertEquals("feed",doc.getDocumentElement().getTagName());
    }
    
    @Test
    public void shouldReturnAnXmlDocumentFromTheUrlProvidedAsARequestParameter() throws Exception {
        IPSRequestContext request = makeRequest(
                makeRequestParams("http://news.google.com/?output=atom", null, null));
        Object[] eParams = new Object[] {};
        Document doc = proxy.processResultDocument(eParams, request, null);
        assertNotNull(doc);
        assertEquals("feed",doc.getDocumentElement().getTagName());
    }
    
    @Test
    public void 
    shouldReturnAnXmlDocumentFromTheGivenUrlWithTheRequestParametersAppendedToTheUrl() 
    throws Exception {
        Map<String,String> params = makeRequestParams(null, null, null);
        params.put("output", "atom");
        IPSRequestContext request = makeRequest(params);
        Object[] eParams = makeParams("http://news.google.com/");
        Document doc = proxy.processResultDocument(eParams, request, null);
        assertNotNull(doc);
        assertEquals("feed",doc.getDocumentElement().getTagName());
    }
    
    public IPSExtensionDef makeExtensionDef(final String ... names) {
        final IPSExtensionDef extensionDef = context.mock(IPSExtensionDef.class);
        context.checking(new Expectations() {{ 
            one(extensionDef).getRuntimeParameterNames();
            will(returnIterator(names));
        }});
        return extensionDef;
    }
    
    public IPSRequestContext makeRequest(final Map<String,String> parameters) {
        final IPSRequestContext request = context.mock(IPSRequestContext.class);
        context.checking(new Expectations() {{
            for (Entry<String, String> entry : parameters.entrySet()) {
                atMost(1).of(request).getParameter(entry.getKey());
                will(returnValue(entry.getValue()));
            }
            
            one(request).getParametersIterator();
            will(returnValue(parameters.entrySet().iterator()));
        }});
        return request;
    }
    
    public Map<String,String> makeRequestParams(String url, String user, String password) {
        Map<String,String> ps = new HashMap<String, String>();
        ps.put("url", url);
        ps.put("user", user);
        ps.put("password", password);
        return ps;
    }
    
    public Object[] makeParams(final String ... params) throws Exception{
        final Object [] rvalue = new Object[params.length];
        context.checking(new Expectations() {{
            for (int i = 0; i < params.length; i++) {
                IPSReplacementValue irv = context.mock(IPSReplacementValue.class);
                one(irv).getValueText();
                will(returnValue(params[i]));
                rvalue[i] = irv;
            }
        }});
        return rvalue;
    }
}
