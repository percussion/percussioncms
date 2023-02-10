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
 * test.percussion.pso.utils UniqueIdLocatorSetTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.utils;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.pso.utils.UniqueIdLocatorSet;

public class UniqueIdLocatorSetTest
{
   private static final Logger log = LogManager.getLogger(UniqueIdLocatorSetTest.class);
   
   UniqueIdLocatorSet cut;
   
   @Before
   public void setUp() throws Exception
   {
      cut = new UniqueIdLocatorSet(); 
   }
   @Test
   public final void testAddPSLocator()
   {
      log.info("starting test AddPSLocator"); 
      PSLocator l1 = new PSLocator(1); 
      PSLocator l2 = new PSLocator(2,1); 
      PSLocator l2a = new PSLocator(2,2); 
      
      boolean added = cut.add(l1);
      assertTrue(added); 
      added = cut.add(l2); 
      assertTrue(added); 
      added = cut.add(l2a); 
      assertFalse(added); 
      
      assertEquals(2, cut.size());
     
   }
   @Test
   public final void testAddAll()
   {
      log.info("starting test AddAll"); 
      PSLocator l1 = new PSLocator(1); 
      PSLocator l2 = new PSLocator(2,1); 
      PSLocator l2a = new PSLocator(2,2); 
      
      Set<PSLocator> locs = new HashSet<PSLocator>();
      locs.add(l1);
      locs.add(l2a); 
      cut.add(l2);
      
      boolean added = cut.addAll(locs); 
      
      assertTrue(added);
      assertEquals(2, cut.size()); 
      
      added = cut.addAll(locs);
      assertFalse(added); 
       
   }
   
   @Test
   public final void testContains()
   {
      log.info("starting test Contains"); 
      PSLocator l1 = new PSLocator(1); 
      PSLocator l2 = new PSLocator(2,1); 
      PSLocator l2a = new PSLocator(2,2);
      PSLocator l3 = new PSLocator(3); 
      
      cut.add(l1);
      cut.add(l2);
      
      boolean has = cut.contains(l2);
      assertTrue(has); 
      has = cut.contains(l2a);
      assertTrue(has);
      has = cut.contains(l3);
      assertFalse(has); 
      
   }
   
   @Test
   public final void testRemove()
   {
      log.info("starting test Remove"); 
      PSLocator l1 = new PSLocator(1); 
      PSLocator l2 = new PSLocator(2,1); 
      PSLocator l2a = new PSLocator(2,2);
      PSLocator l3 = new PSLocator(3); 
      
      cut.add(l1);
      cut.add(l2);
      
      boolean had = cut.remove(l2a);
      assertTrue(had);
      had = cut.remove(l1);
      assertTrue(had); 

      cut.add(l1);
      cut.add(l2);
 
      had = cut.remove(l3);
      assertFalse(had);
   }
   
   @Test
   public final void testRemoveAll()
   {
      log.info("starting test Remove"); 
      PSLocator l1 = new PSLocator(1); 
      PSLocator l2 = new PSLocator(2,1); 
      PSLocator l2a = new PSLocator(2,2);
      PSLocator l3 = new PSLocator(3); 
    
      cut.add(l1); cut.add(l2); cut.add(l3); 
      
      HashSet<PSLocator> rset = new HashSet<PSLocator>();
      rset.add(l2a); 
      log.debug("rset has " + rset.size() + " items");
      log.debug("cut has " + cut.size() + " items"); 
      
      boolean had = cut.removeAll(rset);
      assertTrue(had); 
      assertEquals(2, cut.size()); 
      
      rset.add(l1);
      rset.add(l3); 
      log.debug("rset has " + rset.size() + " items");
      log.debug("cut has " + cut.size() + " items"); 
      
      had = cut.removeAll(rset); 
      assertTrue(had); 
      assertEquals(0,cut.size()); 
      
   }
}
