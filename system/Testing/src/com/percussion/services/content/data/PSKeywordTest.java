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
package com.percussion.services.content.data;

import com.percussion.services.content.IPSContentService;
import com.percussion.services.content.PSContentServiceLocator;

import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link PSKeyword} class.
 */
@Category(IntegrationTest.class)
public class PSKeywordTest
{

   @Test
   public void testServiceContracts() throws Exception
   {
      IPSContentService service = PSContentServiceLocator.getContentService();
      
      // create test keyword
      PSKeyword keyword = service.createKeyword(
         "keyword_A", "description_A");
      
      for (int i=0; i<3; i++)
      {
         PSKeywordChoice choice = new PSKeywordChoice();
         choice.setLabel("choice_A." + i);
         choice.setDescription("description_A." + i);
         choice.setValue("value_A." + i);
         choice.setSequence(i);
         
         keyword.setChoice(choice);
      }
      
      service.saveKeyword(keyword);
      
      try
      {
         // try create with null label
         service.createKeyword(null, null);
         assertFalse("Should have thrown exception", false);
      }
      catch (IllegalArgumentException e)
      {
         // expected exception
         assertTrue(true);
      }
      
      try
      {
         // try create with empty label
         service.createKeyword(" ", null);
         assertFalse("Should have thrown exception", false);
      }
      catch (IllegalArgumentException e)
      {
         // expected exception
         assertTrue(true);
      }
      
      try
      {
         // try create with existing label
         service.createKeyword("keyword_a", null);
         assertFalse("Should have thrown exception", false);
      }
      catch (IllegalArgumentException e)
      {
         // expected exception
         assertTrue(true);
      }
      
      try
      {
         // try find choices with null type
         service.createKeyword(null, null);
         assertFalse("Should have thrown exception", false);
      }
      catch (IllegalArgumentException e)
      {
         // expected exception
         assertTrue(true);
      }
      
      try
      {
         // try find choices with empty type
         service.createKeyword(" ", null);
         assertFalse("Should have thrown exception", false);
      }
      catch (IllegalArgumentException e)
      {
         // expected exception
         assertTrue(true);
      }
      
      try
      {
         // try load keyword with null id
         service.loadKeyword(null, null);
         assertFalse("Should have thrown exception", false);
      }
      catch (IllegalArgumentException e)
      {
         // expected exception
         assertTrue(true);
      }
      
      try
      {
         // try save keyword with null keyword
         service.saveKeyword(null);
         assertFalse("Should have thrown exception", false);
      }
      catch (IllegalArgumentException e)
      {
         // expected exception
         assertTrue(true);
      }
      
      try
      {
         // try delete keyword with null id
         service.deleteKeyword(null);
         assertFalse("Should have thrown exception", false);
      }
      catch (IllegalArgumentException e)
      {
         // expected exception
         assertTrue(true);
      }
      
      // delete test keyword
      service.deleteKeyword(keyword.getGUID());
   }
   
   /**
    * Test all CRUD services for keywords.
    * 
    * @throws Exception for any error.
    */
   @Test
   public void testCRUDServices() throws Exception
   {
      IPSContentService service = PSContentServiceLocator.getContentService();

      // create some keywords  with choices
      int keywordCount = 3;
      int choiceCount = 5;
      for (int i=0; i<keywordCount; i++)
      {
         PSKeyword keyword = service.createKeyword(
            "keyword_" + i, "description_" + i);
         
         for (int j=0; j<choiceCount; j++)
         {
            PSKeywordChoice choice = new PSKeywordChoice();
            choice.setLabel("choice_" + i + "." + j);
            choice.setDescription("description_" + i + "." + j);
            choice.setValue("value_" + i + "." + j);
            choice.setSequence(j);
            
            keyword.setChoice(choice);
         }
         
         service.saveKeyword(keyword);
      }
      
      // find the created keywords
      List<PSKeyword> keywords = service.findKeywordsByLabel(
         "keyword_%", "label");
      assertTrue(keywords != null && keywords.size() == keywordCount);
      assertTrue(keywords.get(0).getChoices().size() == choiceCount);
      assertTrue(keywords.get(1).getChoices().size() == choiceCount);
      assertTrue(keywords.get(2).getChoices().size() == choiceCount);
      
      // add some keyword choice and save
      PSKeyword keyword = keywords.get(0);
      PSKeywordChoice choice = new PSKeywordChoice();
      choice.setLabel("choice_added");
      choice.setDescription("description_added");
      choice.setValue("value_added");
      choice.setSequence(0);
      keyword.setChoice(choice);
      service.saveKeyword(keyword);
      keywords = service.findKeywordsByLabel("keyword_%", "label");
      assertTrue(keywords.get(0).getChoices().size() == choiceCount+1);
      
      // update some keyword choice and save
      keyword = keywords.get(0);
      choice = keyword.getChoices().get(choiceCount);
      choice.setDescription("description_changed");
      choice.setValue("value_changed");
      choice.setSequence(100);
      service.saveKeyword(keyword);
      keywords = service.findKeywordsByLabel("keyword_%", "label");
      assertTrue(keywords.get(0).getChoices().size() == choiceCount+1);
      assertTrue(keywords.get(0).getChoices().get(choiceCount).getValue().equals(
         "value_changed"));
      
      // remove some keyword choice and save
      keyword = keywords.get(0);
      keyword.getChoices().remove(choiceCount);
      service.saveKeyword(keyword);
      keywords = service.findKeywordsByLabel("keyword_%", "label");
      assertTrue(keywords.get(0).getChoices().size() == choiceCount);
      
      // remove one keyword
      keyword = keywords.get(0);
      service.deleteKeyword(keyword.getGUID());
      List<PSKeyword> choices = service.findKeywordChoices(
         keyword.getValue(), null);
      assertTrue(choices.isEmpty());
      
      // remove all test keywords
      keywords = service.findKeywordsByLabel("keyword_%", null);
      for (PSKeyword k : keywords)
         service.deleteKeyword(k.getGUID());
      keywords = service.findKeywordsByLabel("keyword_%", null);
      assertTrue(keywords.isEmpty());
   }
}

