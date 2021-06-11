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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.content;

import com.percussion.services.content.data.PSAutoTranslation;
import com.percussion.services.content.data.PSKeyword;
import com.percussion.services.content.data.PSKeywordChoice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.percussion.utils.testing.IntegrationTest;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

/**
 * Unit test for the {@link IPSContentService} 
 */
@Category(IntegrationTest.class)
public class PSContentServiceTest
{

   private static final Logger log = LogManager.getLogger(PSContentServiceTest.class);

   /**
    * UnitTesting MSM Functionality. This is installing the translation settings
    * fresh on to a system that has no such settings.
    * @throws Exception
    */
   @Test
   public void testDeserializeTranslationsAndSave() throws Exception
   {
      IPSContentService svc = PSContentServiceLocator.getContentService();
      
      String xlnStr = 
         "<auto-translation id=\"1\"> <community-id>10</community-id>" +
         "<content-type-id>301</content-type-id> <locale>en-gb</locale>" +
         "<workflow-id>5</workflow-id> </auto-translation> <!--  -->";
      xlnStr += 
         " <auto-translation id=\"1\"> <community-id>10</community-id>" +
         " <content-type-id>302</content-type-id> <locale>en-gb</locale>" +
         "<workflow-id>5</workflow-id> </auto-translation><!--  -->"; 
      xlnStr += 
         " <auto-translation id=\"1\"> <community-id>10</community-id>" +
         " <content-type-id>310</content-type-id> <locale>en-gb</locale>" +
         "<workflow-id>5</workflow-id> </auto-translation><!--  -->";  
      xlnStr += 
         " <auto-translation id=\"1\"> <community-id>10</community-id>" +
         " <content-type-id>311</content-type-id> <locale>en-gb</locale>" +
         "<workflow-id>5</workflow-id> </auto-translation><!--  -->";      

      List<PSAutoTranslation> xlnList = new ArrayList<PSAutoTranslation>();

      String[] xlns = xlnStr.split("<!--  -->");
      int sz = xlns.length;
      for (int i = 0; i < sz; i++)
      {
         PSAutoTranslation at = new PSAutoTranslation();
         at.fromXML(xlns[i]);
         xlnList.add(at);
      }
      
      Iterator<PSAutoTranslation> it = xlnList.iterator();
      while(it.hasNext())
      {
         PSAutoTranslation at = it.next();
         // delete before you insert it again.
         svc.deleteAutoTranslation(at.getContentTypeId(), at.getLocale());
         at.setVersion(null);
         svc.saveAutoTranslation(at);
      }
      xlnList = svc.loadAutoTranslationsByLocale("en-gb");
      assertTrue(xlnList.size()==4);
      
      // good, now clean up like a good citizen
      it = xlnList.iterator();
      while(it.hasNext())
      {
         PSAutoTranslation at = it.next();
         svc.deleteAutoTranslation(at.getContentTypeId(), at.getLocale());
      }      
   }
   
   /**
    * Test CRUD services for {@link PSAutoTranslation}
    * 
    * @throws Exception if the test fails.
    */
   @Test
   public void testAutoTranslations() throws Exception
   {
      IPSContentService service = PSContentServiceLocator.getContentService();
      
      PSAutoTranslation at1 = null;
      PSAutoTranslation at2 = null;
      
      boolean saved1 = false;
      boolean saved2 = false;
      try
      {
         // test create and save
         at1 = service.createAutoTranslation(123, "test1", 456, 789);
         service.saveAutoTranslation(at1);
         saved1 = true;
         at2 = service.createAutoTranslation(235, "test2", 567, 890);
         service.saveAutoTranslation(at2);
         saved2 = true;
         
         // test load all and individually
         List<PSAutoTranslation> atList = service.loadAutoTranslations();
         assertTrue(atList.size() >= 2);
         for (PSAutoTranslation at : atList)
         {
            assertTrue(!StringUtils.isBlank(at.getLocale()));
            assertTrue(at.getCommunityId() > 0);
            assertTrue(at.getContentTypeId() > 0);
            assertTrue(at.getWorkflowId() > 0);
            assertEquals(at, service.loadAutoTranslation(at.getContentTypeId(), 
               at.getLocale()));
         }
         
         // test re-save and load to compare
         atList = service.loadAutoTranslations();
         for (int i = 0; i < atList.size(); i++)
         {
            PSAutoTranslation at = atList.get(i);
            at.setCommunityId(at.getCommunityId() + i);
            at.setWorkflowId(at.getWorkflowId() + i);
            service.saveAutoTranslation(at);
            assertEquals(at, service.loadAutoTranslation(at.getContentTypeId(), 
               at.getLocale()));
         }
         
         // test delete
         service.deleteAutoTranslation(at1.getContentTypeId(), at1.getLocale());
         saved1 = false;
         assertNull(service.loadAutoTranslation(at1.getContentTypeId(), 
               at1.getLocale()));
         service.deleteAutoTranslation(at2.getContentTypeId(), at2.getLocale());
         saved2 = false;
         assertNull(service.loadAutoTranslation(at2.getContentTypeId(), 
               at2.getLocale()));
      }
      finally
      {
         // quiet cleanup
         try
         {
            if (at1 != null && saved1)
            {
               service.deleteAutoTranslation(at1.getContentTypeId(), 
                  at1.getLocale());
            }
            
            if (at2 != null && saved2)
            {
               service.deleteAutoTranslation(at2.getContentTypeId(), 
                  at2.getLocale());
            }
         }
         catch (Exception e)
         {
            System.out.println("error deleteing auto translations: " + 
               e.getLocalizedMessage());
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
         }
      }
   }
   
   /**
    * Reproduces a test case when deleting a keyword choice with value "1"
    * removed all the keywords. See RX-12295.
    */
   @Test
   public void testSaveKeyword_WithChoice1() throws PSContentException
   {
      final IPSContentService service =
            PSContentServiceLocator.getContentService();
      final PSKeyword keyword =
            service.createKeyword("Label 1", "Keyword Description 1");
      add0_1KeywordChoices(keyword);
      
      service.saveKeyword(keyword);
      
      keyword.getChoices().remove(0);
      service.saveKeyword(keyword);
      assertNotNull(service.loadKeyword(keyword.getGUID(), null));
      
      keyword.getChoices().remove(0);
      service.saveKeyword(keyword);
      assertTrue(keyword.getChoices().isEmpty());
      assertNotNull(service.loadKeyword(keyword.getGUID(), null));

      service.deleteKeyword(keyword.getGUID());
      try
      {
         service.loadKeyword(keyword.getGUID(), null);
         fail();
      }
      catch (PSContentException success) {}
   }

   /**
    * Adds a couple of sample keyword choices with values "0", "1"
    * to the keyword.
    * @param keyword the keyword to add the choices to. Assumed not null.
    */
   private void add0_1KeywordChoices(final PSKeyword keyword)
   {
      {
         final PSKeywordChoice choice = new PSKeywordChoice();
         choice.setLabel("Choice 0 label");
         choice.setDescription("Choice 0 description");
         choice.setValue("0");
         choice.setSequence(0);
         
         keyword.setChoice(choice);
      }

      {
         final PSKeywordChoice choice = new PSKeywordChoice();
         choice.setLabel("Choice 1 label");
         choice.setDescription("Choice 1 description");
         choice.setValue("1");
         choice.setSequence(1);
         
         keyword.setChoice(choice);
      }
   }

   @Test
   public void testDeleteKeyword() throws PSContentException
   {
      final IPSContentService service =
         PSContentServiceLocator.getContentService();
      final PSKeyword keyword =
            service.createKeyword("Label 1", "Keyword Description 1");
      add0_1KeywordChoices(keyword);
      
      service.saveKeyword(keyword);
      assertNotNull("Keyword should be retrieved",
            service.loadKeyword(keyword.getGUID(), null));
      checkKeywordDeletion(keyword);

      try
      {
         service.deleteKeyword(null);
         fail();
      }
      catch (IllegalArgumentException success) {}
   }

   public void testDeleteKeyword_noChoices() throws PSContentException
   {
      final IPSContentService service =
         PSContentServiceLocator.getContentService();
      final PSKeyword keyword =
            service.createKeyword("Label 1", "Keyword Description 1");
      
      service.saveKeyword(keyword);
      assertNotNull("Keyword should be retrieved",
            service.loadKeyword(keyword.getGUID(), null));
      checkKeywordDeletion(keyword);
   }

   /**
    * Checks that a keyword can be safely deleted.
    * @param keyword
    */
   private void checkKeywordDeletion(final PSKeyword keyword)
   {
      final IPSContentService service =
         PSContentServiceLocator.getContentService();
      service.deleteKeyword(keyword.getGUID());
      try
      {
         service.loadKeyword(keyword.getGUID(), null);
         fail("Keyword should not be retrieved");
      }
      catch (PSContentException success) {}
   }
}

