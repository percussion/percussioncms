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
package com.percussion.webservices.transformation.converter;

import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSDateValue;
import com.percussion.cms.objectstore.PSItemChild;
import com.percussion.cms.objectstore.PSItemChildEntry;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSItemFieldMeta;
import com.percussion.cms.objectstore.PSItemRelatedItem;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.content.PSChildEntry;
import com.percussion.webservices.content.PSItem;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit tests for the {@link PSItemConverter} class.
 */
@Category(IntegrationTest.class)
public class PSItemConverterTest extends PSConverterTestBase
{
   /**
    * Tests the conversion from a server to a client item as well as a
    * server array of items to a client array of items and back.
    * 
    * @throws Exception if an error occurs.
    */
   public void testItemConversion() throws Exception
   {
      // create the source object
      PSCoreItem source = createItem();
      addRelatedItems(source);
      
      PSCoreItem target = (PSCoreItem) roundTripConversion(
         PSCoreItem.class, PSItem.class, source);
      
      // verify the round-trip object is equal to the source object
      fixUnexposedFields(source, target);
      assertTrue(source.equals(target));
      
      // create the source array
      PSCoreItem[] sourceArray = new PSCoreItem[1];
      sourceArray[0] = source;
      
      PSCoreItem[] targetArray = (PSCoreItem[]) roundTripConversion(
         PSCoreItem[].class, PSItem[].class, sourceArray);
      
      // verify the the round-trip array is equal to the source array
      assertTrue(sourceArray.length == targetArray.length);
      for (int i=0; i<sourceArray.length; i++)
         fixUnexposedFields(sourceArray[i], targetArray[i]);
      assertTrue(sourceArray[0].equals(targetArray[0]));
   }
   
   /**
    * Test a list of server item conversion to client array, and vice versa.
    * 
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   public void testItemListToArray() throws Exception
   {
      PSCoreItem source = createItem();

      List<PSCoreItem> sourceList = new ArrayList<PSCoreItem>();
      sourceList.add(source);
      
      List<PSCoreItem> targetList = roundTripListConversion(
         PSItem[].class, sourceList);

      for (int i=0; i<sourceList.size(); i++)
         fixUnexposedFields(sourceList.get(i), targetList.get(i));
      assertTrue(sourceList.equals(targetList));
   }

   /**
    * Tests the conversion from a server to a client child item as well as a
    * server array of child items to a client array and back.
    * 
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   public void testChildItemConversion() throws Exception
   {
      // create the source object
      PSItemChild child = createItemChild(); 
      List<PSItemChildEntry> srcList = new ArrayList<PSItemChildEntry>();
      Iterator entries = child.getAllEntries();
      while (entries.hasNext())
      {
         PSItemChildEntry src = (PSItemChildEntry) entries.next();
         PSItemChildEntry target = (PSItemChildEntry) roundTripConversion(
            PSItemChildEntry.class, PSChildEntry.class, src);
         
         // verify the the round-trip object is equal to the source object
         assertTrue(src.equals(target));
         
         srcList.add(src);
      }
      
      List<PSItemChildEntry> tgtList = roundTripListConversion(
         PSChildEntry[].class, srcList);
      
      assertEquals(srcList, tgtList);
   }
   
   /**
    * Test a list of server child item conversion to client array, and back.
    * 
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   public void testChildItemListToArray() throws Exception
   {
      PSCoreItem source = createItem();

      List<PSCoreItem> sourceList = new ArrayList<PSCoreItem>();
      sourceList.add(source);
      
      List<PSCoreItem> targetList = roundTripListConversion(
         PSItem[].class, sourceList);
      
      for (int i=0; i<sourceList.size(); i++)
         fixUnexposedFields(sourceList.get(i), targetList.get(i));
      assertTrue(sourceList.equals(targetList));
   }
   
   /**
    * Create a server item for testing.
    * 
    * @return the new server item, never <code>null</code>.
    * 
    * @throws Exception If there are any errors.
    */
   private PSCoreItem createItem() throws Exception
   {
      int contentId = 1001;
      int revision = 1;
      
      // create test item
      PSCoreItem item = new PSCoreItem(m_def);
      item.setContentId(contentId);
      item.setRevision(revision);
      item.setCurrentRevision(revision);
      item.setEditRevision(revision);
      item.setRequestedRevision(revision);
      item.setDataLocale(new Locale("de-de"));
      item.setSystemLocale(new Locale("de-ch"));
      item.setCheckedOutByName("admin1");
      
      // create test child
      Iterator<PSItemChildEntry> childEntries = 
         createItemChild().getAllEntries();
      PSItemChild child = item.getChildByName("child");
      while (childEntries.hasNext())
         child.addEntry(childEntries.next());
      
      // create test folders
      List<String> folderPaths = new ArrayList<String>();
      folderPaths.add("//sites");
      folderPaths.add("//folders/test");
      item.setFolderPaths(folderPaths);
      
      return item;
   }
   
   /**
    * Create an item child with multiple child entries
    * 
    * @return The child, never <code>null</code>.
    * 
    * @throws Exception if the test fails. 
    */
   private PSItemChild createItemChild() throws Exception
   {
      PSCoreItem item = new PSCoreItem(m_def);
      Iterator children = item.getAllChildren();
      if (!children.hasNext())
         throw new IllegalStateException("no children defined in item def");
      
      PSItemChild itemChild = (PSItemChild) children.next();
      
      for (int i = 0; i < 2; i++)
      {
         PSItemChildEntry entry = itemChild.createAndAddChildEntry();
         entry.setAction(PSItemChildEntry.CHILD_ACTION_INSERT);
         entry.setChildRowId(i);
         
         // must set guid for conversion
         entry.setGUID(new PSLegacyGuid(item.getContentTypeId(), 
            itemChild.getChildId(), i));
         
         Iterator fields = entry.getAllFields();
         setFieldValues(i, fields);
      }
      
      return itemChild;
   }
   
   /**
    * Adds test related content to the supplied item.
    * 
    * @param item the item to which to add the related items, assumed not
    *    <code>null</code>.
    * @throws Exception for any error.
    */
   private void addRelatedItems(PSCoreItem item) throws Exception
   {
      int count = 3;
      List<PSCoreItem> relatedItems = new ArrayList<PSCoreItem>();
      for (int i=0; i<count; i++)
         relatedItems.add(createItem());
      
      PSLocator owner = new PSLocator(item.getContentId(), 
         item.getCurrentRevision());

      IPSTemplateSlot slot = new PSTemplateSlot();
      slot.setGUID(new PSGuid(PSTypeEnum.SLOT, 103));
      slot.setName("slot");
      slot.setRelationshipName("ActiveAssembly");
      
      IPSAssemblyTemplate template = new PSAssemblyTemplate();
      template.setGUID(new PSGuid(PSTypeEnum.TEMPLATE, 301));
      template.setName("template");
      
      PSRelationshipCommandHandler.reloadConfigs();
      long rid = 1;
      List<PSAaRelationship> relationships = new ArrayList<PSAaRelationship>();
      for (PSCoreItem relatedItem : relatedItems)
      {
         PSLocator dependent = new PSLocator(relatedItem.getContentId(), 
            relatedItem.getCurrentRevision());
         
         PSAaRelationship relationship = new PSAaRelationship(owner, dependent, 
            slot, template);
         relationship.setGUID(new PSGuid(PSTypeEnum.RELATIONSHIP, rid++));
         relationships.add(relationship);
      }
      
      item.setRelatedItems(createRelatedItems(relatedItems, relationships));
   }
   
   /**
    * Create test related items for the supplied parameters.
    * 
    * @param items the items to relate, assumed not <code>null</code>, may be
    *    empty.
    * @param relationships the relationships to use, assumed not 
    *    <code>null</code>, may be empty.
    * @return the related items, never <code>null</code>, may be empty.
    * @throws Exception for any error.
    */
   private Map<String, PSItemRelatedItem> createRelatedItems(
      List<PSCoreItem> items, List<PSAaRelationship> relationships) 
      throws Exception
   {
      Map<String, PSItemRelatedItem> relatedItems = 
         new HashMap<String, PSItemRelatedItem>();
      
      for (int i=0; i<items.size(); i++)
      {
         PSCoreItem item = items.get(i);
         PSAaRelationship relationship = relationships.get(i);
         
         PSItemRelatedItem relatedItem = new PSItemRelatedItem();
         relatedItem.setAction("ignore");
         relatedItem.setDependentId(item.getContentId());
         relatedItem.setRelatedItemData(item.toXml(
            PSXmlDocumentBuilder.createXmlDocument()));
         relatedItem.setRelatedType(relationship.getConfig().getName());
         relatedItem.setRelationshipId(relationship.getId());
         relatedItem.setRelationship(relationship);
         
         relatedItems.put(Integer.toString(relationship.getId()), relatedItem);
      }
      
      
      return relatedItems;
   }

   /**
    * Create and set values for the supplied fields.  Handles only text, numeric
    * and date for now.
    * 
    * @param index Used to generate unique values, must be less than 363 if 
    * date fields are present
    * @param fields Iterator of fields, assumed not <code>null</code>.
    */
   private void setFieldValues(int index, Iterator fields)
   {
      while (fields.hasNext())
      {
         PSItemField field = (PSItemField) fields.next();
         int type = field.getItemFieldMeta().getBackendDataType();
         // add values for basic types
         switch (type)
         {
            case PSItemFieldMeta.DATATYPE_TEXT :
               field.addValue(new PSTextValue("test" + index));
               break;
            case PSItemFieldMeta.DATATYPE_NUMERIC :
               field.addValue(new PSTextValue(String.valueOf(index)));
               break;
               
            case PSItemFieldMeta.DATATYPE_DATE :
               Calendar calendar = Calendar.getInstance();
               calendar.setTime(new Date());
               calendar.set(Calendar.DAY_OF_YEAR, 1 + index);
               field.addValue(new PSDateValue(calendar.getTime()));
               break;
               
            default :
               break;
         }
      }
   }
   
   /**
    * Get the item definition manager used for testing. This will have the
    * test item definition registered.
    * 
    * @return the test item definition manager, never <code>null</code>.
    */
   public static PSItemDefManager getTestItemDefManager()
   {
      PSItemDefManager itemDefMgr = PSItemDefManager.getInstance();
      itemDefMgr.registerDef(m_def, m_cmsObject);
      
      return itemDefMgr;
   }
   
   /**
    * Load the item definition from the specified file.
    * 
    * @param fileName the file name, relative to the source code location,
    *    may be <code>null</code> or empty to use the default.
    * @return the item definition created from the specified file, never
    *    <code>null</code>.
    * @throws Exception for any error loading the item definition.
    */
   public static PSItemDefinition loadItemDefinition(String fileName) 
      throws Exception
   {
      InputStream in = null;

      try
      {
         if (StringUtils.isBlank(fileName))
            fileName = "itemDefinition.xml";
         
         in = PSItemConverterTest.class.getResourceAsStream(fileName);
         Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);

         return new PSItemDefinition(doc.getDocumentElement());
      }
      finally
      {
         if (in != null)
            try { in.close(); } catch (IOException e) { /* ignore */ }
      }
   }
   
   /**
    * Load the icms object from the specified file.
    * 
    * @param fileName the file name, relative to the source code location,
    *    may be <code>null</code> or empty to use the default.
    * @return the cms object created from the specified file, never
    *    <code>null</code>.
    * @throws Exception for any error loading the cms object.
    */
   public static PSCmsObject loadCmsObject(String fileName) 
      throws Exception
   {
      InputStream in = null;
   
      try
      {
         if (StringUtils.isBlank(fileName))
            fileName = "cmsObject.xml";
         
         in = PSItemConverterTest.class.getResourceAsStream(fileName);
         Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
   
         return new PSCmsObject(doc.getDocumentElement());
      }
      finally
      {
         if (in != null)
            try { in.close(); } catch (IOException e) { /* ignore */ }
      }
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      getTestItemDefManager();
   }   
   
   /**
    * This fixes up fields which are not exposed with the webservice item.
    * 
    * @param source the source from which to get the field values to be fixed,
    *    assumed not <code>null</code>.
    * @param target the target for which to fix the field values, assumed
    *    not <code>null</code>.
    */
   private void fixUnexposedFields(PSCoreItem source, PSCoreItem target)
   {
      target.setCurrentRevision(source.getCurrentRevision());
      target.setEditRevision(source.getEditRevision());
      target.setRequestedRevision(source.getRequestedRevision());
      Iterator<PSItemRelatedItem> relatedItems = target.getAllRelatedItems();
      while (relatedItems.hasNext())
      {
         PSItemRelatedItem relatedItem = relatedItems.next();
         Element data = relatedItem.getRelatedItemData();
         data.setAttribute("currentRevision", "" + source.getCurrentRevision());
         data.setAttribute("editRevision", "" + source.getEditRevision());
         data.setAttribute("requestedRevision", "" + 
            source.getRequestedRevision());
         data.setAttribute("revisionCount", "" + source.getRevisionCount());
      }
   }
   
   /**
    * The item definition used for testing, never <code>null</code>.
    */
   private static PSItemDefinition m_def = null;
   
   /**
    * The cms object used for testing, never <code>null</code>.
    */
   private static PSCmsObject m_cmsObject = null;
   
   /*
    * Initialize item stuff needed for testing.
    */
   static
   {
      try
      {
         m_def = loadItemDefinition(null);
         m_cmsObject = loadCmsObject(null);
      }
      catch (Exception e)
      {
         throw new IllegalStateException(
            "Test initialization failed because of: " + 
            e.getLocalizedMessage());
      }
   }
}
