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
package com.percussion.services.utils.xml;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.percussion.services.publisher.IPSPubItemStatus;
import com.percussion.services.publisher.data.PSPubItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.junit.Test;

public class PSXStreamObjectStreamTest extends TestCase
{

   private static final int SIZE = 10 * 10 * 10 * 10 * 10;

   @Test
   public void testWriteObjects() throws Exception
   {
      PSXStreamObjectStream<IPSPubItemStatus> pubItems = new PSXStreamObjectStream<IPSPubItemStatus>();
      List<IPSPubItemStatus> items = new ArrayList<IPSPubItemStatus>();
      PSPubItem a = new PSPubItem();
      a.contentId = 10;
      a.date = new Date();
      a.folderId = 100;
      
      PSPubItem b = new PSPubItem();
      b.contentId = 20;
      b.date = new Date();
      b.folderId = 200;
      items.add(a);
      items.add(b);
      
      pubItems.writeObjects(items.iterator());
      ArrayList<IPSPubItemStatus> actual = new ArrayList<IPSPubItemStatus>();
      Iterables.addAll(actual, pubItems);
      assertEquals(items, actual);
      
   }
   
   @Test
   public void testMassiveWriteObjects() throws Exception
   {
      final PSXStreamObjectStream<IPSPubItemStatus> pubItems = new PSXStreamObjectStream<IPSPubItemStatus>();

      pubItems.writeObjects(new AbstractIterator<IPSPubItemStatus>()
      {
         int i = 0;
         @Override
         protected IPSPubItemStatus computeNext()
         {
            // 100,000 items.
            if (i == SIZE) return endOfData();
            PSPubItem a = new PSPubItem();
            a.contentId = ++i;
            a.date = new Date();
            a.folderId = i * 10;
            return a;
         }
         
      });
      
      int i = 0;
      for (IPSPubItemStatus s : pubItems) {
         assertEquals (++i, s.getContentId());
      }
      Executor e = Executors.newCachedThreadPool();
      ExecutorCompletionService<Integer> es = new ExecutorCompletionService<Integer>(e);
      Future<Integer> f = es.submit(new Callable<Integer>()
      {
         public Integer call() throws Exception
         {
            int i = 0;
            for (IPSPubItemStatus s : pubItems) {
               assertEquals (++i, s.getContentId());
            }
            return i;
         }
      });
      TimeUnit.SECONDS.sleep(1);
      pubItems.dispose();
      Integer j = f.get();
      assertTrue( "should do some calculation", SIZE >= j);
      
      
   }

}
