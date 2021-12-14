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
 * test.percussion.pso.preview ActionSiteForwardingControllerTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.preview;

import com.percussion.pso.preview.AbstractMenuController;
import com.percussion.pso.preview.ActionSiteForwardingController;
import com.percussion.pso.preview.SiteFolderFinder;
import com.percussion.pso.preview.SiteFolderLocation;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.webservices.security.IPSSecurityWs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 
 *
 * @author DavidBenua
 *
 */
public class ActionSiteForwardingControllerTest
{
   private static final Logger log = LogManager.getLogger(ActionSiteForwardingControllerTest.class);
   
   Mockery context; 
   ActionSiteForwardingController cut;
   
   IPSAssemblyService asm; 
   IPSSecurityWs secws; 
   
   SiteFolderFinder finder;
   
   MockHttpServletRequest req; 
   MockHttpServletResponse resp; 
   /**
    * @throws Exception
    */
   @Before
   public void setUp() throws Exception
   {
      context = new Mockery();
      cut = new ActionSiteForwardingController(); 
      
      asm = context.mock(IPSAssemblyService.class);
      ActionSiteForwardingController.setAsm(asm); 
      secws = context.mock(IPSSecurityWs.class);    
      AbstractMenuController.setSecws(secws); 
      
      finder = context.mock(SiteFolderFinder.class);
      cut.setSiteFolderFinder(finder); 
      
      req = new MockHttpServletRequest(); 
      req.setMethod("POST");
      
      resp = new MockHttpServletResponse(); 
      
      cut.setViewName("myView");
      cut.setBaseUrl("myBaseUrl"); 
   }
   
   /**
    * Test method for {@link ActionSiteForwardingController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
    */
   @Test
   @Ignore("Test is failing") //TODO: Fix me
   public final void testHandleRequestWithSiteId()
   {
      req.addParameter(IPSHtmlParameters.SYS_SITEID, "1");
      try
      {
         ModelAndView mav = cut.handleRequest(req, resp);
         assertNotNull(mav);
         assertTrue(mav.getView() instanceof RedirectView); 
      } catch (Exception ex)
      {
        log.error("Unexpected Exception " + ex,ex);
        fail("Exception"); 
      } 
   }
   /**
    * Test method for {@link ActionSiteForwardingController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
    */
   @Test
   @Ignore("Test is failing") //TODO: Fix me
   public final void testHandleRequestOneSite()
   {
      try
      {
         req.addParameter(IPSHtmlParameters.SYS_CONTENTID, "123");
         req.addParameter(IPSHtmlParameters.SYS_FOLDERID, "301"); 
         req.addParameter(IPSHtmlParameters.SYS_REVISION, "1"); 
                  
         final IPSSite site = context.mock(IPSSite.class);
         
         SiteFolderLocation loc = new SiteFolderLocation();
         loc.setFolderid(301);
         loc.setFolderPath("myPath");
         loc.setSite(site);
         
         final List<SiteFolderLocation> locs = Collections.<SiteFolderLocation>singletonList(loc);
         
         context.checking(new Expectations(){{
            one(finder).findSiteFolderLocations("123", "301", "");
            will(returnValue(locs)); 
            allowing(site).getName(); will(returnValue("mySite"));
            allowing(site).getSiteId(); will(returnValue(302L)); 
         }});
         
         ModelAndView mav = cut.handleRequest(req, resp);
         assertNotNull(mav);
         assertTrue(mav.getView() instanceof RedirectView);
         
         context.assertIsSatisfied(); 
         
      } catch (Exception ex)
      {
        log.error("Unexpected Exception " + ex,ex);
        fail("Exception"); 
      } 
   }
   /**
    * Test method for {@link ActionSiteForwardingController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
    */
   @Test
   @Ignore("Test is failing") //TODO: Fix me
   public final void testHandleRequestInternalTwoSites()
   {
      try
      {
         req.addParameter(IPSHtmlParameters.SYS_CONTENTID, "123");
         req.addParameter(IPSHtmlParameters.SYS_FOLDERID, "301"); 
         req.addParameter(IPSHtmlParameters.SYS_REVISION, "1"); 
                  
         final IPSSite site1 = context.mock(IPSSite.class,"site1");
         final IPSSite site2 = context.mock(IPSSite.class,"site2");
         
         SiteFolderLocation loc1 = new SiteFolderLocation();
         loc1.setFolderid(301);
         loc1.setFolderPath("myPath");
         loc1.setSite(site1);
         
         SiteFolderLocation loc2 = new SiteFolderLocation();
         loc2.setFolderid(303);
         loc2.setFolderPath("myPath");
         loc2.setSite(site2);
         
         final List<SiteFolderLocation> locs = Arrays.asList(
               new SiteFolderLocation[]{loc1,loc2});
         
         context.checking(new Expectations(){{
            one(finder).findSiteFolderLocations("123", "301", "");
            will(returnValue(locs)); 
            allowing(site1).getName(); will(returnValue("site1"));
            allowing(site1).getSiteId(); will(returnValue(302L));
            allowing(site2).getName(); will(returnValue("site2"));
            allowing(site2).getSiteId(); will(returnValue(303L)); 

         }});
         
         ModelAndView mav = cut.handleRequest(req, resp);
         assertNotNull(mav);
         
         
         context.assertIsSatisfied(); 
         
      } catch (Exception ex)
      {
        log.error("Unexpected Exception " + ex,ex);
        fail("Exception"); 
      } 
   }
   
}
