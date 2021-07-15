/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
