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
package com.percussion.services.utils.orm;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.utils.orm.data.PSTempId;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.timing.PSStopwatch;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

/**
 * Test the data collection helper for correct behavior.
 * 
 * @author dougrand
 */
@Category(IntegrationTest.class)
public class PSDataCollectionHelperTest
{
   /**
    * Test
    */
   @Test
   public void testCreateIdSet()
   {
      SessionFactory sf = 
         (SessionFactory) PSBaseServiceLocator.getBean("sys_sessionFactory");
      Session s = sf.openSession();
      
      List<Long> ids = new ArrayList<Long>();
      for (long i = 0; i < 1000; i++)
      {
         ids.add(i);
      }
      // Add a few dups to test for RX-11516
      for (long i = 1; i < 5; i++)
      {
         ids.add(i);
      }

      PSStopwatch sw = new PSStopwatch();
      sw.start();
      long idset1 = PSDataCollectionHelper.createIdSet(s, ids);
      sw.stop();
      System.out.println("First set created in " + sw);
      sw.start();
      long idset2 = PSDataCollectionHelper.createIdSet(s, ids);
      sw.stop();
      System.out.println("Second set created in " + sw);
      sw.start();
      long idset3 = PSDataCollectionHelper.createIdSet(s, ids);
      sw.stop();
      System.out.println("Third set created in " + sw);
      sw.start();
      long idset4 = PSDataCollectionHelper.createIdSet(s, ids);
      sw.stop();
      System.out.println("Fourth set created in " + sw);
      
      // Check contents
      Criteria c = s.createCriteria(PSTempId.class);
      c.add(Restrictions.eq("pk.id", idset1));
      List results = c.list();
      assertEquals(ids.size(), results.size());
      
      ids.clear();
      for (long i = 0; i < 10000; i++)
      {
         ids.add(i);
      }
      sw.start();
      long idset5 = PSDataCollectionHelper.createIdSet(s, ids);
      sw.stop();
      System.out.println("Fifth (10000) set created in " + sw);

      // Check contents
      c = s.createCriteria(PSTempId.class);
      c.add(Restrictions.eq("pk.id", idset5));
      results = c.list();
      assertEquals(ids.size(), results.size());
      
      
      // Clear
      sw.start();
      PSDataCollectionHelper.clearIdSet(s, idset1);
      sw.stop();
      System.out.println("First set cleared in " + sw);
      sw.start();
      PSDataCollectionHelper.clearIdSet(s, idset2);
      sw.stop();
      System.out.println("Second set cleared in " + sw);
      sw.start();
      PSDataCollectionHelper.clearIdSet(s, idset3);
      sw.stop();
      System.out.println("Third set cleared in " + sw);
      sw.start();
      PSDataCollectionHelper.clearIdSet(s, idset4);
      sw.stop();
      System.out.println("Fourth set cleared in " + sw);
      sw.start();
      PSDataCollectionHelper.clearIdSet(s, idset5);
      sw.stop();
      System.out.println("Fifth set cleared in " + sw);
      
      s.close();
   }

}
