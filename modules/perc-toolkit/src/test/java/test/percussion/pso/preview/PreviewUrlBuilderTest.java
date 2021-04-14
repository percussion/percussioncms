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
 * test.percussion.pso.preview PreviewUrlBuilderTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.preview;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.preview.PreviewUrlBuilder;
import com.percussion.pso.preview.SiteFolderLocation;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;

public class PreviewUrlBuilderTest
{
   private static Log log = LogFactory.getLog(PreviewUrlBuilderTest.class); 
   
   private PreviewUrlBuilder cut; 
   private Mockery context; 
   @Before
   public void setUp() throws Exception
   {
      cut = new PreviewUrlBuilder();
      context = new Mockery();
      cut.setDefaultLocationUrl("defaultLocation");
      cut.setMultipleLocationUrl("multipleLocation"); 
   }
   @Test
   public final void testBuildPreviewUrl()
   {
      final IPSGuid tempGuid = new PSLegacyGuid(123L);
      final IPSAssemblyTemplate template = context.mock(IPSAssemblyTemplate.class); 
      
      Map<String, Object> urlParams = new HashMap<String, Object>(); 
      urlParams.put(IPSHtmlParameters.SYS_CONTENTID, "23"); 
      urlParams.put(IPSHtmlParameters.SYS_REVISION, "1"); 
      
      try
      {
         context.checking(new Expectations(){{
            one(template).getGUID();
            will(returnValue(tempGuid)); 
         }});
         String result = cut.buildUrl(template, urlParams, null, true);
         assertNotNull(result); 
         log.info("url result is " + result);
         assertTrue(result.contains("multiple")); 
         context.assertIsSatisfied(); 
         
      } catch (Exception ex)
      {
        log.error("Unexpected Exception " + ex,ex);
        fail("Exception"); 
      } 
      
   }
   
   @Test
   public final void testBuildPreviewUrlAssembly()
   {
      final IPSGuid tempGuid = new PSLegacyGuid(123L);
      final IPSAssemblyTemplate template = context.mock(IPSAssemblyTemplate.class); 
      final IPSSite site = context.mock(IPSSite.class); 
      SiteFolderLocation location = new SiteFolderLocation(); 
      location.setSite(site); 
      location.setFolderid(457); 
      Map<String, Object> urlParams = new HashMap<String, Object>(); 
      urlParams.put(IPSHtmlParameters.SYS_CONTENTID, "23"); 
      urlParams.put(IPSHtmlParameters.SYS_REVISION, "1"); 
      
      try
      {
         context.checking(new Expectations(){{
            one(template).getGUID();
            will(returnValue(tempGuid));
            allowing(template).getAssemblyUrl();
            will(returnValue("/Rhythmyx/baz/bat"));
            allowing(site).getSiteId();
            will(returnValue(123L)); 
         }});
         cut.setDefaultLocationUrl("/Rhythmyx/foo/bar"); 
         String result = cut.buildUrl(template, urlParams, location, false );
         assertNotNull(result); 
         log.info("url result is " + result);
         assertTrue(result.contains("myx/foo/bar"));
         assertFalse(result.contains("myx/baz/bat")); 
         context.assertIsSatisfied(); 
         
      } catch (Exception ex)
      {
        log.error("Unexpected Exception " + ex,ex);
        fail("Exception"); 
      } 
      
   }
   
   @Test
   public final void testBuildPreviewUrlAssemblyNull()
   {
      final IPSGuid tempGuid = new PSLegacyGuid(123L);
      final IPSAssemblyTemplate template = context.mock(IPSAssemblyTemplate.class); 
      final IPSSite site = context.mock(IPSSite.class); 
      
      SiteFolderLocation location = new SiteFolderLocation(); 
      location.setSite(site); 
      //location.setSiteid(123L);
      location.setFolderid(457); 
      Map<String, Object> urlParams = new HashMap<String, Object>(); 
      urlParams.put(IPSHtmlParameters.SYS_CONTENTID, "23"); 
      urlParams.put(IPSHtmlParameters.SYS_REVISION, "1"); 
      
      try
      {
         context.checking(new Expectations(){{
            one(template).getGUID();
            will(returnValue(tempGuid));
            allowing(template).getAssemblyUrl();
            will(returnValue(null)); 
            allowing(site).getSiteId();
            will(returnValue(123L)); 
         }});
         cut.setDefaultLocationUrl("/Rhythmyx/foo/bar"); 
         String result = cut.buildUrl(template, urlParams, location, false);
         assertNotNull(result); 
         log.info("url result is " + result);
         assertTrue(result.contains("myx/foo/bar"));
         context.assertIsSatisfied(); 
         
      } catch (Exception ex)
      {
        log.error("Unexpected Exception " + ex,ex);
        fail("Exception"); 
      } 
      
   }
}
