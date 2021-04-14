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
 * test.percussion.pso.preview MultiSiteResolutionControllerTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.preview;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import com.percussion.pso.preview.MultiSiteResolutionController;
import com.percussion.pso.preview.PreviewLocation;
import com.percussion.pso.preview.SiteFolderFinder;
import com.percussion.pso.preview.SiteFolderLocation;
import com.percussion.pso.preview.UrlBuilder;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;

/**
 * 
 *
 * @author DavidBenua
 *
 */
public class MultiSiteResolutionControllerTest
{
   Log log = LogFactory.getLog(MultiSiteResolutionControllerTest.class);
   Mockery context; 
   MultiSiteResolutionController cut;
   MockHttpServletRequest req;
   MockHttpServletResponse resp;
   IPSGuidManager gmgr;
   IPSAssemblyService asm;
   SiteFolderFinder finder;
   UrlBuilder builder; 
   
   /**
    * @throws Exception
    */
   @Before
   public void setUp() throws Exception
   {
      context = new Mockery();
      cut = new MultiSiteResolutionController();
      req = new MockHttpServletRequest(); 
      req.setMethod("POST"); 
      resp = new MockHttpServletResponse();
      gmgr = context.mock(IPSGuidManager.class); 
      asm = context.mock(IPSAssemblyService.class);
      finder = context.mock(SiteFolderFinder.class); 
      builder = context.mock(UrlBuilder.class);
      
      MultiSiteResolutionController.setGmgr(gmgr);
      MultiSiteResolutionController.setAsm(asm);
      cut.setSiteFolderFinder(finder); 
      cut.setUrlBuilder(builder);
   }
   /**
    * Test method for {@link MultiSiteResolutionController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
    */
   @Test
   @SuppressWarnings("unchecked")
   public final void testHandleRequestInternalHttpServletRequestHttpServletResponse()
   {
      
      try
      {
         cut.setViewName("xyz"); 
         req.setParameter(IPSHtmlParameters.SYS_VARIANTID, "123"); 
         req.setParameter(IPSHtmlParameters.SYS_CONTENTID, "345"); 
         req.setParameter(IPSHtmlParameters.SYS_FOLDERID, "1" ); 
         req.setParameter(IPSHtmlParameters.SYS_SITEID, "2" ); 
         
       
         final List<SiteFolderLocation> locs = new ArrayList<SiteFolderLocation>(); 
         final SiteFolderLocation loc = new SiteFolderLocation(); 
         loc.setFolderid(1);
         loc.setFolderPath("xyzzy"); 
         final IPSSite site = context.mock(IPSSite.class);
         loc.setSite(site); 
         locs.add(loc); 
         final IPSGuid templateGuid = new PSLegacyGuid(123L);
         final IPSAssemblyTemplate template = context.mock(IPSAssemblyTemplate.class);
         
         context.checking(new Expectations(){{
            one(finder).findSiteFolderLocations("345" , "1", "2");   
            will(returnValue(locs)); 
            one(gmgr).makeGuid(123L, PSTypeEnum.TEMPLATE); 
            will(returnValue(templateGuid));
            one(asm).loadTemplate(templateGuid, false); 
            will(returnValue(template));
            one(template).getName();
            will(returnValue("myTemplate")); 
            one(site).getName(); will(returnValue("mySite")); 
            one(builder).buildUrl(with(any(IPSAssemblyTemplate.class)), with(any(Map.class)), 
                  with(any(SiteFolderLocation.class)), with(any(Boolean.class)));
            will(returnValue("myUrl"));
         }}); 
         
         ModelAndView mav = cut.handleRequest(req, resp);
         
         assertNotNull(mav); 
         List<PreviewLocation> previews = (List<PreviewLocation>) mav.getModel().get("previews");
         assertNotNull(previews);
         assertEquals(1,previews.size());
         
         PreviewLocation myLoc = previews.get(0);
         assertEquals("mySite", myLoc.getSiteName()); 
         context.assertIsSatisfied();
         
      } catch (Exception ex)
      {
        log.error("Unexpected Exception " + ex,ex);
        fail("Exception");
      } 
      
   }
   

}
