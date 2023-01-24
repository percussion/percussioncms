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
