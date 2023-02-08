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
 * test.percussion.pso.preview CachingSiteLoaderImplTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.preview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.preview.CachingSiteLoaderImpl;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;

public class CachingSiteLoaderImplTest
{
   private static final Logger log = LogManager.getLogger(CachingSiteLoaderImplTest.class);
   
   private CachingSiteLoaderImpl cut; 
   
   Mockery context;
   
   IPSSiteManager siteMgr; 
   
   public CachingSiteLoaderImplTest()
   {
      
   }
   @Before
   public void setUp() throws Exception
   {
      cut = new CachingSiteLoaderImpl();
      context = new Mockery();
      siteMgr = context.mock(IPSSiteManager.class);
      CachingSiteLoaderImpl.setSiteMgr(siteMgr);
      
   }
   @Test
   public final void testFindAllSites()
   {
      cut.setSiteReloadDelay(0L);
      final IPSSite site1 = context.mock(IPSSite.class);
      final List<IPSSite> sites = new ArrayList<IPSSite>();
      sites.add(site1); 
      
      try
      {
         context.checking(new Expectations(){{
            one(siteMgr).findAllSites();
            will(returnValue(sites)); 
         }});
         
         cut.afterPropertiesSet();
         
         List<IPSSite> results = cut.findAllSites(); 
         assertNotNull(results);
         assertEquals(1, results.size());
         assertEquals(site1, results.get(0)); 
         
         context.assertIsSatisfied(); 
         
      } catch (Exception ex)
      {
          log.error("Unexpected Exception " + ex,ex);
          fail("Exception"); 
      }
   }
   
 
   
      
}
