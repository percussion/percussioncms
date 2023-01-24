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
/*
 * test.percussion.pso.preview MultiSiteResolutionControllerTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.preview;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * 
 *
 * @author DavidBenua
 *
 */
public class MultiSiteResolutionControllerTest
{
   private static final Logger log = LogManager.getLogger(MultiSiteResolutionControllerTest.class);
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
   @Ignore("Test is failing") //TODO: Fix me
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
