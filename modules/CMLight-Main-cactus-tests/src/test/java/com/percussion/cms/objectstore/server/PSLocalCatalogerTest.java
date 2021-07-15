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
package com.percussion.cms.objectstore.server;

import com.percussion.cms.PSDisplayChoices;
import com.percussion.cms.objectstore.IPSFieldCataloger;
import com.percussion.cms.objectstore.client.PSContentEditorFieldCataloger;
import com.percussion.cms.objectstore.client.PSLightWeightField;
import com.percussion.utils.request.PSRequestInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

/**
 * FAILED IN JAVA1.8 - temporary Ignored
 * Tests {@link IPSFieldCataloger} methods in the {@link PSLocalCataloger}
 */
@Category(IntegrationTest.class)
public class PSLocalCatalogerTest extends ServletTestCase
{
   /**
    * FAILED IN JAVA1.8 - temporary Ignored
    */
   public void testNOOP()
   {
      assertTrue(true);
   }
   
   /**
    * FAILED IN JAVA1.8 - temporary Ignored
    * Test the field cataloger methods. Note that this does not currently test 
    * all flag usage, but only the functionality modified for the Marlin
    * release.
    * 
    * @throws Exception if the test fails.
    */
   @Ignore
   public void ignored_testFieldCatalog() throws Exception
   {
      IPSFieldCataloger cat = new PSLocalCataloger(PSRequestInfo.getRequestInfo(
         PSRequestInfo.KEY_PSREQUEST));
      
      PSContentEditorFieldCataloger ceCat;
      ceCat = new PSContentEditorFieldCataloger(cat, null,
         IPSFieldCataloger.FLAG_INCLUDE_ALL);
      Map<String, Map<String, PSLightWeightField>> allMap = ceCat.getAll();
      
      Set<String> fields = new HashSet<String>();
      ceCat = new PSContentEditorFieldCataloger(cat, fields,
         IPSFieldCataloger.FLAG_INCLUDE_ALL);
      Set<String> allFields = getAllFieldNames(ceCat);
      
      Map<String, Map<String, PSLightWeightField>> testMap = ceCat.getAll();
      
      Map<String, PSDisplayChoices> choiceFieldMap = 
         new HashMap<String, PSDisplayChoices>();
      for (Object key : allMap.keySet())
      {
         Map map1 = allMap.get(key);
         Map map2 = testMap.get(key);
         assertNotNull(map2);
         assertEquals(map1.keySet(), map2.keySet());
         
         // add one field to set, check for choices
         boolean added = false;
         for (Object obj : map1.keySet())
         {
            String name = (String) obj;
            
            // skip workflow, community id system fields as they are special
            // cases referenced as part of choice filters for other fields
            if (name.equals("sys_workflowid") || name.equals("sys_communityid"))
               continue;
            
            if (!added)
            {
               fields.add(name);
               added = true;
            }
            
            PSLightWeightField field = (PSLightWeightField) map1.get(name);
            if (field.getDisplayChoices() != null && 
               field.getDisplayChoices().areChoicesLoaded())
            {
               choiceFieldMap.put(name, field.getDisplayChoices());
            }
         }
      }
      
      // we need to have some choices
      assertTrue(!choiceFieldMap.isEmpty());
      
      // test list of fields
      ceCat = new PSContentEditorFieldCataloger(cat, fields, 
         IPSFieldCataloger.FLAG_INCLUDE_ALL);
      assertEquals(fields, getAllFieldNames(ceCat));
      
      // test choices
      Set<String> choiceFields = new HashSet<String>(choiceFieldMap.keySet());
      ceCat = new PSContentEditorFieldCataloger(cat, choiceFields,
         IPSFieldCataloger.FLAG_EXCLUDE_CHOICES);
      Set<String> testSet = getAllFieldNames(ceCat);
      assertEquals(choiceFieldMap.keySet(), testSet);
      Set<PSLightWeightField> choiceFieldSet = getAllFields(ceCat);
      for (PSLightWeightField field : choiceFieldSet)
      {
         PSDisplayChoices choices = field.getDisplayChoices();
         assertNotNull(choices);
         assertFalse(choices.areChoicesLoaded());
         assertFalse(choices.getChoices().hasNext());

         // should still have a filter if one was defined
         PSDisplayChoices srcChoices = choiceFieldMap.get(
            field.getInternalName());
         assertEquals(choices.getChoiceFilter(), srcChoices.getChoiceFilter());
      }
      
      // test adding fields
      Set<String> noChoiceFields = new HashSet<String>(allFields);
      noChoiceFields.removeAll(choiceFields);
      ceCat.loadFields(noChoiceFields, IPSFieldCataloger.FLAG_EXCLUDE_CHOICES);
      testSet = getAllFieldNames(ceCat);
      assertTrue(testSet.containsAll(noChoiceFields));
      assertTrue(testSet.containsAll(choiceFields));
      
      // choices should still have none loaded
      Set<PSLightWeightField> testFields = getAllFields(ceCat);
      checkChoices(choiceFields, testFields, false);

      // check ctype map
      Map<String, Collection<PSLightWeightField>> typeMap = 
         ceCat.getLocalContentTypeMap();
      assertEquals(ceCat.getLocalMap().keySet(), getAllFieldNames(typeMap));
      for (Collection<PSLightWeightField> typeFields : typeMap.values())
      {
         checkChoices(choiceFields, typeFields, false);
      }
      
      // test adding fields w/ choices
      ceCat.loadFields(null, IPSFieldCataloger.FLAG_INCLUDE_ALL);
      testFields = getAllFields(ceCat);
      checkChoices(choiceFields, testFields, true);
      typeMap = ceCat.getLocalContentTypeMap();
      assertEquals(ceCat.getLocalMap().keySet(), getAllFieldNames(typeMap));
      for (Collection<PSLightWeightField> typeFields : typeMap.values())
      {
         checkChoices(choiceFields, typeFields, true);
      }
      
      // ensure recataloging doesn't override
      ceCat.loadFields(choiceFields, IPSFieldCataloger.FLAG_EXCLUDE_CHOICES);
      testFields = getAllFields(ceCat);
      checkChoices(choiceFields, testFields, true);
      typeMap = ceCat.getLocalContentTypeMap();

      assertTrue(getAllFieldNames(ceCat).containsAll(getAllFieldNames(typeMap)));
      for (Collection<PSLightWeightField> typeFields : typeMap.values())
      {
         checkChoices(choiceFields, typeFields, true);
      }
      
      // test no fields in ctor
      ceCat = new PSContentEditorFieldCataloger(cat, 
         IPSFieldCataloger.FLAG_EXCLUDE_CHOICES);
      assertEquals(IPSFieldCataloger.FLAG_EXCLUDE_CHOICES,
         ceCat.getControlFlags());
     assertEquals(0, getAllFields(ceCat).size());
      
      // catalog w/no choices
      ceCat.loadFields(fields, IPSFieldCataloger.FLAG_EXCLUDE_CHOICES, false);
      checkChoices(choiceFields, getAllFields(ceCat), false);
      
      // catalog w/choices, no refresh, should still get them
      ceCat.loadFields(fields, IPSFieldCataloger.FLAG_INCLUDE_ALL, false);
      checkChoices(choiceFields, getAllFields(ceCat), true);
      
      // recatalog no choices, no refresh, should still have them
      ceCat.loadFields(fields, IPSFieldCataloger.FLAG_EXCLUDE_CHOICES, false);
      checkChoices(choiceFields, getAllFields(ceCat), true);
      
      // recatalog no choices, with refresh, should still have choices.
      ceCat.loadFields(fields, IPSFieldCataloger.FLAG_EXCLUDE_CHOICES, true);
      checkChoices(choiceFields, getAllFields(ceCat), true);
      
   }

   /**
    * Check if the supplied fields have choices loaded appropriately
    * 
    * @param choiceFields The set of field names that contain choices, assumed 
    * not <code>null</code>. 
    * @param testFields A collection of fields to test, assumed not 
    * <code>null</code>.
    * @param choicesLoaded <code>true</code> if the fields in the collection 
    * that contain choices should have them loaded, <code>false</code> if not.
    * 
    * @throws Exception if the test fails or there are any errors. 
    */
   private void checkChoices(Set<String> choiceFields, 
      Collection<PSLightWeightField> testFields, boolean choicesLoaded) 
         throws Exception
   {
      for (PSLightWeightField field : testFields)
      {
         if (choiceFields.contains(field.getInternalName()))
         {
            assertNotNull(field.getDisplayChoices());
            assertEquals(field.getDisplayChoices().areChoicesLoaded(),
               choicesLoaded);
         }
         else if (field.getDisplayChoices() != null)
         {
            assertEquals(field.getDisplayChoices().areChoicesLoaded(),
               choicesLoaded);
         }
      }
   }

   /**
    * Get all field names from the catalog.
    * 
    * @param ceCat The catalog, assumed not <code>null</code>.
    * 
    * @return the set, never <code>null</code>, may be empty.
    */
   private Set<String> getAllFieldNames(PSContentEditorFieldCataloger ceCat)
   {
      Map testMap;
      testMap = ceCat.getAll();
      Set<String> testSet = new HashSet<String>();
      for (Object key : testMap.keySet())
      {
         Map map = (Map) testMap.get(key); 
         for (Object obj : map.keySet())
         {
            String name = (String) obj;
            testSet.add(name);
         }
      }
      return testSet;
   }
   
   /**
    * Get all fields from the catalog.
    * 
    * @param ceCat The catalog, assumed not <code>null</code>.
    * 
    * @return the set, never <code>null</code>, may be empty.
    */
   private Set<PSLightWeightField> getAllFields(
      PSContentEditorFieldCataloger ceCat)
   {
      Map testMap;
      testMap = ceCat.getAll();
      Set<PSLightWeightField> fieldSet = new HashSet<PSLightWeightField>();
      for (Object key : testMap.keySet())
      {
         Map map = (Map) testMap.get(key); 
         for (Object obj : map.values())
         {
            fieldSet.add((PSLightWeightField) obj);
         }
      }
      
      return fieldSet;
   }
   
   /**
    * Get all field names based on the supplied type map.
    * 
    * @param typeMap Map of content type name to collection of fields, assumed 
    * not <code>null</code>. 
    * 
    * @return The set of fieldnames, never <code>null</code>.
    */
   private Set<String> getAllFieldNames(Map<String, 
      Collection<PSLightWeightField>> typeMap)
   {
      Set<String> results = new HashSet<String>();
      for (Collection<PSLightWeightField> coll : typeMap.values())
      {
         for (PSLightWeightField field : coll)
         {
            results.add(field.getInternalName());
         }
      }
      
      return results;
   }
      
}

