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
 * test.percussion.pso.preview AbstractMenuControllerTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.preview;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.preview.AbstractMenuController;
import com.percussion.pso.preview.SiteFolderLocation;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.security.IPSSecurityWs;

public class AbstractMenuControllerTest
{
   
   TestableAbstractMenuController cut; 
   Mockery context; 
   @Before
   public void setUp() throws Exception
   {
      context = new Mockery(){{setImposteriser(ClassImposteriser.INSTANCE);}};
      cut = new TestableAbstractMenuController();
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public final void testFilterVisibleTemplates()
   {
      final IPSSite site = context.mock(IPSSite.class); 
      final Set<IPSSite> sites = Collections.<IPSSite>singleton(site);
      
      final IPSAssemblyTemplate t1 = context.mock(IPSAssemblyTemplate.class, "t1"); 
      final IPSAssemblyTemplate t2 = context.mock(IPSAssemblyTemplate.class, "t2"); 
      final IPSGuid tg1 = context.mock(IPSGuid.class, "tg1"); 
      final IPSGuid tg2 = context.mock(IPSGuid.class, "tg2"); 
      final Set<IPSAssemblyTemplate> temps = new HashSet<IPSAssemblyTemplate>();
      temps.add(t1); 
      temps.add(t2); 
      
      final IPSSecurityWs secws = context.mock(IPSSecurityWs.class);
      final IPSAssemblyService asm = context.mock(IPSAssemblyService.class);
      AbstractMenuController.setSecws(secws); 
      AbstractMenuController.setAsm(asm); 
      
      
      cut.setTestCommunityVisibility(true); 
      
      context.checking(new Expectations(){{
        allowing(t1).getGUID(); will(returnValue(tg1));
        allowing(t2).getGUID(); will(returnValue(tg2)); 
        allowing(t1).getName(); will(returnValue("t1"));
        allowing(t2).getName(); will(returnValue("t2")); 
        allowing(site).getName();will(returnValue("mySite"));
        allowing(site).getAssociatedTemplates(); will(returnValue(temps)); 
       
        one(secws).filterByRuntimeVisibility(with(any(List.class)));
        will(returnValue(Collections.<IPSGuid>singletonList(tg1)));
      }});
      
      List<IPSAssemblyTemplate> results = cut.filterVisibleTemplates(temps, sites);
      
      assertNotNull(results);
      assertEquals(1,results.size()); 
      assertEquals(t1,results.get(0)); 
      
   }
   
   @Test
   public final void testIsTemplateOnSite()
   {
      final IPSSite site = context.mock(IPSSite.class); 
      final Set<IPSSite> sites = Collections.<IPSSite>singleton(site);
      
      final IPSAssemblyTemplate t1 = context.mock(IPSAssemblyTemplate.class, "t1"); 
      final IPSAssemblyTemplate t2 = context.mock(IPSAssemblyTemplate.class, "t2"); 
      
      final Set<IPSAssemblyTemplate> temps = new HashSet<IPSAssemblyTemplate>();
      temps.add(t1); 
      
      context.checking(new Expectations(){{
         atLeast(1).of(site).getAssociatedTemplates();
         will(returnValue(temps)); 
         allowing(t1).getName();
         will(returnValue("Template1")); 
         allowing(site).getName();
         will(returnValue("Site1")); 
         
      }});
      
      boolean res = cut.isTemplateOnSite(t1, sites); 
      assertTrue(res); 
      
      context.assertIsSatisfied(); 
      
      res = cut.isTemplateOnSite(t2, sites); 
      
      assertFalse(res); 
   }
   
   class TestableAbstractMenuController extends AbstractMenuController
   {

      /**
       * @see AbstractMenuController#filterVisibleTemplates(Collection, Set)
       */
      @Override
      public List<IPSAssemblyTemplate> filterVisibleTemplates(
            Collection<IPSAssemblyTemplate> alltemps, Set<IPSSite> sites)
      {
         return super.filterVisibleTemplates(alltemps, sites);
      }

      /**
       * @see AbstractMenuController#findSitesFromLocations(List)
       */
      @Override
      public Set<IPSSite> findSitesFromLocations(
            List<SiteFolderLocation> locations)
      {
         return super.findSitesFromLocations(locations);
      }

      /**
       * @see AbstractMenuController#isTemplateOnSite(IPSAssemblyTemplate, Set)
       */
      @Override
      public boolean isTemplateOnSite(IPSAssemblyTemplate t,
            Set<IPSSite> sites)
      {
         return super.isTemplateOnSite(t, sites);
      }
      
   }
}
