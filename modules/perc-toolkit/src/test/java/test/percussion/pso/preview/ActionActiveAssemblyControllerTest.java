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
 * test.percussion.pso.preview ActionActiveAssemblyControllerTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.preview;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class ActionActiveAssemblyControllerTest
{
   private static final Logger log = LogManager.getLogger(ActionActiveAssemblyControllerTest.class);
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
   @Ignore("Test us failing") //TODO: Fix me
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
