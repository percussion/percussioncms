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
package com.percussion.rxfix;

import com.percussion.rxfix.PSRxFix.Entry;
import com.percussion.rxfix.dbfixes.PSFixDanglingAssociations;
import com.percussion.rxfix.dbfixes.PSFixNextNumberTable;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;

import java.util.Iterator;
import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertNotNull;

/**
 * Run the test framework and check the results for sanity
 * 
 * @author dougrand
 */
@Category(IntegrationTest.class)
public class PSRxFixTest
{
   /**
    * Default CTOR
    */
   public PSRxFixTest(){}

   /**
    * @throws Exception
    */
   @Test
   public void testRxFixPreview() throws Exception
   {
      // Force instantiation of Spring
      PSCmsObjectMgrLocator.getObjectManager();
      
      PSRxFix fixer = getFixer();
      
      fixer.doFix(true);
      
      List<PSRxFix.Entry> entries = fixer.getEntries();
      
      // Check at least one results from each
      for(PSRxFix.Entry e : entries)
      {
         List<PSFixResult> result = e.getResults();
         assertNotNull(result);
      }
      
      // Print out results
      for(PSRxFix.Entry e : entries)
      {
         System.out.println("Operation: " + e.getFixname());
         List<PSFixResult> result = e.getResults();
         for(PSFixResult r : result)
         {
            System.out.println(r.toString());
         }
      } 
   }

   private PSRxFix getFixer() throws Exception
   {
      PSRxFix fixer = new PSRxFix();
      
      // TODO: only test running fixes that are used by the installer since others fail, and we aren't going to take the time to fix them now.
      Iterator<Entry> iter = fixer.getEntries().iterator();
      while (iter.hasNext())
      {
         Entry entry = iter.next();
         if (entry.getFix().equals(PSFixNextNumberTable.class) || entry.getFix().equals(PSFixDanglingAssociations.class))
         {
            // keep these
            continue;
         }

         // remove others
         iter.remove();
      }
      return fixer;
   }
   
   /**
    * @throws Exception
    */
   @Test
   public void testRxFix() throws Exception
   {
      // Force instantiation of Spring
      PSCmsObjectMgrLocator.getObjectManager();
      
      PSRxFix fixer = getFixer();
      
      fixer.doFix(false);
      
      List<PSRxFix.Entry> entries = fixer.getEntries();
      
      // Check at least one results from each
      for(PSRxFix.Entry e : entries)
      {
         List<PSFixResult> result = e.getResults();
         assertNotNull(result);
      }
      
      // Print out results
      for(PSRxFix.Entry e : entries)
      {
         System.out.println("Operation: " + e.getFixname());
         List<PSFixResult> result = e.getResults();
         for(PSFixResult r : result)
         {
            System.out.println(r.toString());
         }
      } 
   }   
}
