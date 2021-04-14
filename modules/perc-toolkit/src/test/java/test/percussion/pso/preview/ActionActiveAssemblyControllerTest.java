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
 * test.percussion.pso.preview ActionActiveAssemblyControllerTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.preview;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.error.PSException;
import com.percussion.pso.jexl.PSOObjectFinder;
import com.percussion.pso.preview.AbstractMenuController;
import com.percussion.pso.preview.ActionActiveAssemblyController;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.utils.guid.IPSGuid;

public class ActionActiveAssemblyControllerTest
{
   private static Log log = LogFactory.getLog(ActionActiveAssemblyControllerTest.class);
   TestableActiveAssemblyController cut; 
   Mockery context; 
   
   IPSAssemblyService asm; 
   IPSCmsContentSummaries sumsvc;
   PSOObjectFinder finder;
   
   @Before
   public void setUp() throws Exception
   {
      context = new Mockery() {{setImposteriser(ClassImposteriser.INSTANCE);}};
      
      cut = new TestableActiveAssemblyController(); 
      asm = context.mock(IPSAssemblyService.class);
      sumsvc = context.mock(IPSCmsContentSummaries.class); 
      finder = context.mock(PSOObjectFinder.class);
      
      AbstractMenuController.setAsm(asm);
      AbstractMenuController.setObjectFinder(finder); 
      
      
      
      cut.setTestCommunityVisibility(false); 
      
   }
   @SuppressWarnings("serial")
@Test
   public final void testFindVisibleTemplates()
   {
      final IPSSite site = context.mock(IPSSite.class);
      final IPSAssemblyTemplate t1 = context.mock(IPSAssemblyTemplate.class);
      final IPSAssemblyTemplate t2 = context.mock(IPSAssemblyTemplate.class); 
      
      final PSComponentSummary summ = context.mock(PSComponentSummary.class);
      final List<IPSAssemblyTemplate> templates = new ArrayList<IPSAssemblyTemplate>()
       {{add(t1);add(t2);}}; 
      
      final IPSGuid ctype = new PSLegacyGuid(123L);
       
      try
      {
         context.checking(new Expectations(){{
            one(asm).findTemplatesByContentType(with(any(IPSGuid.class)));
            will(returnValue(templates)); 
            one(finder).getComponentSummaryById("2"); 
            will(returnValue(summ)); 
            one(summ).getContentTypeGUID(); 
            will(returnValue(ctype));
            allowing(site).getAssociatedTemplates();
            will(returnValue(Collections.singleton(t2)));
            allowing(site).getName();will(returnValue("mySite"));
            allowing(t1).getName();will(returnValue("t1")); 
            allowing(t1).getOutputFormat(); will(returnValue(IPSAssemblyTemplate.OutputFormat.Page));
            allowing(t2).getName();will(returnValue("t2"));
            allowing(t2).getOutputFormat(); will(returnValue(IPSAssemblyTemplate.OutputFormat.Page));
            allowing(t2).getActiveAssemblyType(); will(returnValue(IPSAssemblyTemplate.AAType.Normal)); 
         }});
         
         List<IPSAssemblyTemplate> results = cut.findVisibleTemplates("2", Collections.<IPSSite>singleton(site));
         assertNotNull(results); 
         assertEquals(1, results.size()); 
         
         context.assertIsSatisfied(); 
        
      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception"); 
      } 
   }
   
   private class TestableActiveAssemblyController extends ActionActiveAssemblyController
   {
         
      
      /**
       * @see ActionActiveAssemblyController#findVisibleTemplates(String, Set)
       */
      @Override
      public List<IPSAssemblyTemplate> findVisibleTemplates(
            String contentid, Set<IPSSite> sites) throws PSException,
            PSAssemblyException
      {
         return super.findVisibleTemplates(contentid, sites);
      }
      
   }
}
