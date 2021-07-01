/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
/*
 * test.percussion.pso.preview ActionPreviewControllerTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.preview;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.pso.jexl.PSOObjectFinder;
import com.percussion.pso.preview.ActionPreviewController;
import com.percussion.pso.preview.SiteFolderFinder;
import com.percussion.pso.preview.SiteFolderLocation;
import com.percussion.pso.preview.UrlBuilder;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.security.IPSSecurityWs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class ActionPreviewControllerTest
{
   private static final Logger log = LogManager.getLogger(ActionPreviewControllerTest.class);
   
   private ActionPreviewController cut; 
   private Mockery context;
   private SiteFolderFinder finder;
   private PSOObjectFinder objectFinder;
   private UrlBuilder urlbuilder; 
   private IPSAssemblyService asm; 
   private IPSSecurityWs secws; 
  
   @Before
   public void setUp() throws Exception
   {
      context = new Mockery(){{setImposteriser(ClassImposteriser.INSTANCE);}};
      cut = new ActionPreviewController();
      finder = context.mock(SiteFolderFinder.class);
      cut.setSiteFolderFinder(finder);
      cut.setViewName("myView"); 
      cut.setTestCommunityVisibility(false); 
      objectFinder = context.mock(PSOObjectFinder.class);
      ActionPreviewController.setObjectFinder(objectFinder); 
      
      asm = context.mock(IPSAssemblyService.class);
      ActionPreviewController.setAsm(asm);
      
      secws = context.mock(IPSSecurityWs.class);
      ActionPreviewController.setSecws(secws); 
      
      urlbuilder = context.mock(UrlBuilder.class);
      cut.setUrlBuilder(urlbuilder);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   @Ignore("Test is failing") //TODO: Fix me
   public final void testHandleRequestInternalHttpServletRequestHttpServletResponse()
   {
      MockHttpServletRequest request = new MockHttpServletRequest();
      MockHttpServletResponse response = new MockHttpServletResponse();
      
      try
      {
         final PSComponentSummary summary = context.mock(PSComponentSummary.class); 
         final List<SiteFolderLocation> locations = new ArrayList<SiteFolderLocation>();
         final IPSSite site = context.mock(IPSSite.class);
         final IPSAssemblyTemplate template = context.mock(IPSAssemblyTemplate.class);
         
         SiteFolderLocation location1 = new SiteFolderLocation() {{
           setFolderid(2);
           setFolderPath("//Sites/foo/bar/baz");
           setSite(site);
         }};
         locations.add(location1);   
         request.setMethod("GET"); 
         
         request.addPreferredLocale(new Locale("en","us")); 
         
         request.addParameter(IPSHtmlParameters.SYS_CONTENTID, "1");
         request.addParameter(IPSHtmlParameters.SYS_REVISION, "1");
         request.addParameter(IPSHtmlParameters.SYS_FOLDERID, "2"); 
         
         context.checking(new Expectations(){{
            one(finder).findSiteFolderLocations("1", "2", "");
            will(returnValue(locations));
            one(objectFinder).getComponentSummaryById("1");
            will(returnValue(summary));
            one(summary).getContentTypeGUID(); 
            will(returnValue(new PSLegacyGuid(3,4)));
            one(asm).findTemplatesByContentType(with(any(IPSGuid.class)));
            will(returnValue(Collections.<IPSAssemblyTemplate>singletonList(template)));
            one(site).getAssociatedTemplates();
            will(returnValue(Collections.<IPSAssemblyTemplate>singleton(template)));
            allowing(template).getName();
            will(returnValue("myTemplate")); 
            allowing(template).getLabel();
            will(returnValue("My Template"));
            one(template).getOutputFormat();
            will(returnValue(IPSAssemblyTemplate.OutputFormat.Page)); 
            allowing(site).getName();
            will(returnValue("mySite"));
            one(urlbuilder).buildUrl(with(any(IPSAssemblyTemplate.class)), with(any(Map.class)), with(any(SiteFolderLocation.class)), with(any(Boolean.class)));
            will(returnValue("http://localhost/foo/bar/baz")); 
         }});
         
         ModelAndView mav = cut.handleRequest(request, response); 
         assertNotNull(mav);
         assertEquals("myView", mav.getViewName());
         
         log.info("Mav is " + mav);
         context.assertIsSatisfied(); 
      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception caught"); 
      }
   }
}
