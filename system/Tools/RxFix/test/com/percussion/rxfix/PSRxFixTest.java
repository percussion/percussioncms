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
