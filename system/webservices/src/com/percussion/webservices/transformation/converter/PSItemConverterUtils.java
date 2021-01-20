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
package com.percussion.webservices.transformation.converter;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.IPSItemAccessor;
import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSBinaryValue;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSDateValue;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemChild;
import com.percussion.cms.objectstore.PSItemChildEntry;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSItemFieldMeta;
import com.percussion.cms.objectstore.PSItemRelatedItem;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSWebserviceErrors;
import com.percussion.webservices.content.PSChildEntry;
import com.percussion.webservices.content.PSField;
import com.percussion.webservices.content.PSFieldDataType;
import com.percussion.webservices.content.PSFieldDimension;
import com.percussion.webservices.content.PSFieldFieldValueType;
import com.percussion.webservices.content.PSFieldSourceType;
import com.percussion.webservices.content.PSFieldTransferEncoding;
import com.percussion.webservices.content.PSItemChildren;
import com.percussion.webservices.content.PSItemFolders;
import com.percussion.webservices.content.PSItemSlots;
import com.percussion.webservices.content.PSRelatedItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.lang.StringUtils;

/**
 * This class provides utility methods used by the item converter and possibly
 * other item related converters such as the child entry converter.
 */
public class PSItemConverterUtils
{
   /**
    * Creates a new item field for the supplied content type and field name
    * through the item definition manager.
    * 
    * @param contentType the content type name for which to create the new
    *    item field, not <code>null</code> or empty.
    * @param fieldName the name of the field to create, not <code>null</code>
    *    or empty.
    * @return the new item field created, never <code>null</code>.
    * @throws ConversionException if no item definition is found for the
    *    specified content type or if no mapping exists for the supplied
    *    field name. 
    */
   public static PSItemField createItemField(String contentType, 
      String fieldName) throws ConversionException
   {
      if (StringUtils.isBlank(contentType))
         throw new IllegalArgumentException(
            "contentType cannot be null or empty");

      if (StringUtils.isBlank(fieldName))
         throw new IllegalArgumentException(
            "fieldName cannot be null or empty");
      
      PSItemDefinition def = getItemDefinition(contentType);

      // get the field definition
      com.percussion.design.objectstore.PSField fieldDef = 
         def.getFieldByName(fieldName);
      
      // get the ui definition
      PSContentEditorPipe pipe = 
         (PSContentEditorPipe) def.getContentEditor().getPipe();
      PSDisplayMapping mapping = pipe.getMapper().getUIDefinition().getMapping(
         fieldName);
      if (mapping == null)
         throw new ConversionException(
            "Unknown field name " + fieldName +
            " in item definition for content type " + contentType);
      PSUISet uiDef = mapping.getUISet();
      
      // is this a multivalued field
      PSFieldSet fieldSet = pipe.getMapper().getFieldSet();
      boolean isMultiValued = 
         fieldSet.getType() == PSFieldSet.TYPE_SIMPLE_CHILD;
      
      return new PSItemField(fieldDef, uiDef, isMultiValued);
   }

   /**
    * Convert the supplied server children to client children.
    * 
    * @param children the server children to convert, not <code>null</code>, 
    *    may be empty.
    * @param callingConverter the converter calling this method, not
    *    <code>null</code>.
    * @return the client children converted from the supplied server children,
    *    never <code>null</code>, may be empty.
    */
   public static PSItemChildren[] toClientChildren(
      Iterator<PSItemChild> children, PSConverter callingConverter)
   {
      List<PSItemChildren> destChildren = new ArrayList<PSItemChildren>();
      
      while (children.hasNext())
      {
         PSItemChild child = children.next();
         
         PSItemChildren destChild = new PSItemChildren();
         destChild.setName(child.getName());
         destChild.setDisplayName(child.getDisplayName());
         destChildren.add(destChild);
         
         List<PSChildEntry> destEntries = new ArrayList<PSChildEntry>();
         Iterator<PSItemChildEntry> entries = child.getAllEntries();
         while (entries.hasNext())
         {
            PSItemChildEntry entry = entries.next();
            
            destEntries.add((PSChildEntry) callingConverter.getConverter(
               PSChildEntry.class).convert(PSChildEntry.class, entry));
         }

         destChild.setPSChildEntry(destEntries.toArray(
            new PSChildEntry[destEntries.size()]));
      }
      
      return destChildren.toArray(new PSItemChildren[destChildren.size()]);
   }

   /**
    * Convert the server child entries to client child entries.
    * 
    * @param contentTypeId the content type that contains the child entries.
    * @param childId the child id of the entries.
    * @param clientChild the client child to update with the converties entires, 
    *    may not be <code>null</code>.
    * @param entries the entries to convert, never <code>null</code>, may be
    *    empty.
    * @param callingConverter the converter calling this method, not
    *    <code>null</code>.
    */
   public static void toClientChildEntries(long contentTypeId, int childId, 
      PSItemChildren clientChild, Iterator<PSItemChildEntry> entries, 
      PSConverter callingConverter)
   {
      if (clientChild == null)
         throw new IllegalArgumentException("clientChild may not be null");
      
      if (entries == null)
         throw new IllegalArgumentException("entries may not be null");
      
      if (callingConverter == null)
         throw new IllegalArgumentException("callingConverter cannot be null");
      
      List<PSChildEntry> clientEntries = new ArrayList<PSChildEntry>();
      while (entries.hasNext())
      {
         PSItemChildEntry entry = entries.next();
         entry.setGUID(new PSLegacyGuid(contentTypeId, childId, 
            entry.getChildRowId()));
         clientEntries.add((PSChildEntry) callingConverter.getConverter(
            PSChildEntry.class).convert(PSChildEntry.class, entry));
      }
      
      clientChild.setPSChildEntry(clientEntries.toArray(
         new PSChildEntry[clientEntries.size()]));
   }

   /**
    * Convert the values for all supplied client fields to server field values 
    * and update them in the supplied server item.
    * 
    * @param item the server item in which to update the converted 
    *    field values, not <code>null</code>.
    * @param id the id of the item to convert the fields for, 
    *    not <code>null</code>.
    * @param fields an array of client fields to convert the values from, 
    *    not <code>null</code>, may be empty.
    */
   public static void toServerFields(IPSItemAccessor item, IPSGuid id, 
      PSField[] fields)
   {
      if (item == null)
         throw new IllegalArgumentException("item cannot be null");
      
      if (id == null)
         throw new IllegalArgumentException("id cannot be null");
      
      if (fields == null)
         throw new IllegalArgumentException("fields cannot be null");
      
      for (PSField field : fields)
      {
         PSItemField destField = item.getFieldByName(field.getName());
         if (destField == null)
         {
            throw new ConversionException(
               PSWebserviceErrors.createErrorMessage(
                  IPSWebserviceErrors.UNKNOWN_FIELD_NAME, id.toString(), 
                  field.getName()));
         }
         
         toServerFieldValues(destField, field);
      }
   }
   
   /**
    * Convert the values from the supplied client field to server field values 
    * and set them on the provided server field.
    *  
    * @param serverField the server field to which to set the converted client 
    *    field values, not <code>null</code>.
    * @param field the client field from which to convert the values, not
    *    <code>null</code>.
    */
   public static void toServerFieldValues(PSItemField serverField, 
      PSField field)
   {
      if (serverField == null)
         throw new IllegalArgumentException("serverField cannot be null");
      
      if (field == null)
         throw new IllegalArgumentException("field cannot be null");
      
      serverField.clearValues();
   
      com.percussion.webservices.content.PSFieldValue[] values = 
         field.getPSFieldValue();
      
      if (values != null)
      {
         IPSFieldValue destValue = null;
         if (field.getDataType().equals(PSFieldDataType.binary))
         {
            com.percussion.webservices.content.PSFieldValue[] binaryValues = 
               field.getPSFieldValue();
            if (binaryValues != null && binaryValues.length > 0)
            {
               com.percussion.webservices.content.PSFieldValue binaryValue = 
                  binaryValues[0];
               if (binaryValue != null)
               {
                  if (StringUtils.isBlank(binaryValue.getAttachmentId()))
                  {
                     /*
                      * If there is no attachment identifier, we assume they
                      * supplied base64 encoded data.
                      */
                     destValue = serverField.createFieldValue(
                        binaryValue.getRawData());
                  }
                  else
                     serverField.setHrefLocation(binaryValue.getAttachmentId());
               }
            }
            
            if (destValue != null)
               serverField.addValue(destValue);
         }
         else if (field.getDataType().equals(PSFieldDataType.date))
         {
            for (com.percussion.webservices.content.PSFieldValue value : values)
            {
               String rawDate = value.getRawData();
               if (!StringUtils.isBlank(rawDate))
               {
                  destValue = PSDateValue.getDateValueFromString(
                     value.getRawData());
                  serverField.addValue(destValue);
               }
            }
         }
         else if (field.getDataType().equals(PSFieldDataType.number) ||
            field.getDataType().equals(PSFieldDataType.text))
         {
            for (com.percussion.webservices.content.PSFieldValue value : values)
            {
               destValue = new PSTextValue(value.getRawData());
               serverField.addValue(destValue);
            }
         }
      }
   }
   
   /**
    * Convert the supplied server field to a client field.
    * 
    * @param source the server field to convert, not <code>null</code>.
    * @param callingConverter the converter calling this method, not
    *    <code>null</code>.
    * @return the converted server field, never <code>null</code>.
    * @throws PSCmsException for any error.
    */
   @SuppressWarnings("unchecked")
   public static PSField toClientField(PSItemField source, 
      PSConverter callingConverter) throws PSCmsException
   {
      if (source == null)
         throw new IllegalArgumentException("source cannot be null");
      
      if (callingConverter == null)
         throw new IllegalArgumentException("callingConverter cannot be null");

      String contentType = source.getContentType();
      if (StringUtils.isBlank(contentType))
         throw new ConversionException(
            "You must set the contentType for each field to convert.");
      
      PSItemFieldMeta meta = source.getItemFieldMeta();
      com.percussion.design.objectstore.PSField def = 
         getFieldDefinition(contentType, source.getName());
      if (def == null)
         throw new ConversionException(
            "No field definition found for content type " + contentType + 
            " and field " + source.getName());

      PSField dest = new PSField();
      dest.setName(source.getName());
      dest.setAllowActiveTags(def.isAllowActiveTags());
      dest.setCleanupNamespaces(def.isCleanupNamespaces());
      dest.setDataType((PSFieldDataType) callingConverter.getConverter(
         PSFieldDataType.class).convert(PSFieldDataType.class, 
            new Integer(meta.getBackendDataType())));
      if (def.getDeclaredNamespaces() != null)
         dest.setDeclaredNamespaces(def.getDeclaredNamespaces().toString());
      dest.setDisplayName(meta.getDisplayName());
      dest.setFieldValueType(
         (PSFieldFieldValueType) callingConverter.getConverter(
            PSFieldFieldValueType.class).convert(PSFieldFieldValueType.class, 
               new Integer(meta.getFieldValueType())));
      dest.setMimeType(def.getMimeType());
      dest.setShowInPreview(meta.showInPreview());
      dest.setSourceType((PSFieldSourceType) callingConverter.getConverter(
         PSFieldSourceType.class).convert(PSFieldSourceType.class, 
            new Integer(meta.getSourceType())));
      dest.setTransferEncoding(
         (PSFieldTransferEncoding) callingConverter.getConverter(
            PSFieldTransferEncoding.class).convert(
               PSFieldTransferEncoding.class, 
               new Integer(meta.getTransferEncoding())));
      dest.setDimension(
         (PSFieldDimension) callingConverter.getConverter(
            PSFieldDimension.class).convert(PSFieldDimension.class, 
               com.percussion.design.objectstore.PSField.PSDimensionEnum.valueOf(
                  def.getOccurrenceDimension(null))));
      
      List<com.percussion.webservices.content.PSFieldValue> fieldValues = 
         toClientFieldValues(source);
      dest.setPSFieldValue(fieldValues.toArray(
         new com.percussion.webservices.content.PSFieldValue[fieldValues.size()]));
      
      return dest;
   }
   
   /**
    * Convert the supplied server fields to client fields.
    * 
    * @param fields the server fields to convert, not <code>null</code>, may
    *    be empty.
    * @param contentType the content type for which to make the conversion,
    *    not <code>null</code> or empty.
    * @param callingConverter the converter calling this method, not
    *    <code>null</code>.
    * @return the child field array, never <code>null</code>, may be empty.
    * @throws PSCmsException for field value conversion errors.
    */
   @SuppressWarnings("unchecked")
   public static PSField[] toClientFields(Iterator<PSItemField> fields, 
      String contentType, PSConverter callingConverter) throws PSCmsException
   {
      if (fields == null)
         throw new IllegalArgumentException("fields cannot be null");
      
      if (StringUtils.isBlank(contentType))
         throw new IllegalArgumentException(
            "contentType cannot be null or empty");
      
      if (callingConverter == null)
         throw new IllegalArgumentException("callingConverter cannot be null");
      
      List<PSField> destFields = new ArrayList<PSField>();
      while (fields.hasNext())
      {
         PSItemField field = fields.next();
         field.setContentType(contentType);
         destFields.add(toClientField(field, callingConverter));
      }
      
      return destFields.toArray(new PSField[destFields.size()]);
   }
   
   /**
    * Convert the supplied server field values into client field values.
    * 
    * @param source the source field for which to convert all values, 
    *    not <code>null</code>.
    * @return a list with all client field values converted from the supplied 
    *    source, never <code>null</code>, may be empty.
    * @throws PSCmsException for any error.
    */
   public static List<com.percussion.webservices.content.PSFieldValue> toClientFieldValues(
      PSItemField source) throws PSCmsException
   {
      if (source == null)
         throw new IllegalArgumentException("source cannot be null");
      
      List<com.percussion.webservices.content.PSFieldValue> destValues = 
         new ArrayList<com.percussion.webservices.content.PSFieldValue>();

      Iterator<IPSFieldValue> values = source.getAllValues();
      while (values.hasNext())
      {
         IPSFieldValue value = values.next();

         com.percussion.webservices.content.PSFieldValue destValue = 
            new com.percussion.webservices.content.PSFieldValue();
         
         if (value instanceof PSBinaryValue)
         {
            PSBinaryValue binary = (PSBinaryValue) value;
            if (StringUtils.isBlank(source.getHrefLocation()))
            {
               /*
                * If there is no attachment identifier, we assume they
                * supplied base64 encoded data.
                */
               destValue.setRawData(binary.getBase64Encode());
            }
            else
            {
               // the value will be attached to the message
               destValue.setAttachmentId(source.getHrefLocation());
            }
         }
         else if (value instanceof PSTextValue ||
            value instanceof PSDateValue)
         {
            destValue.setRawData(value.getValueAsString());
         }
         
         destValues.add(destValue);
      }
      
      return destValues;
   }
   
   /**
    * Convert the client children to server children and set them on the 
    * supplied item.
    * 
    * @param serverItem The item to set the children on, not <code>null</code>.
    * @param children The children to convert, may be <code>null</code> or 
    *    empty.
    * @param callingConverter the converter calling this method, not
    *    <code>null</code>.
    */
   public static void toServerChildren(PSCoreItem serverItem, 
      PSItemChildren[] children, PSConverter callingConverter)
   {
      if (serverItem == null)
         throw new IllegalArgumentException("serverItem cannot be null");
      
      if (children == null)
         return;
      
      if (callingConverter == null)
         throw new IllegalArgumentException("callingConverter cannot be null");

      for (PSItemChildren child : children)
      {
         PSItemChild destChild = serverItem.getChildByName(child.getName());
         toServerChildEntries(destChild, child.getPSChildEntry(), 
            callingConverter);
      }
   }
   
   /**
    * Convert the client child entries to server entries and set them on the
    * supplied child.
    * 
    * @param serverChild the child to set the entries on, never 
    *    <code>null</code>.
    * @param entries the entries, may be <code>null</code> or empty.
    * @param callingConverter the converter calling this method, not
    *    <code>null</code>.
    */
   public static void toServerChildEntries(PSItemChild serverChild, 
      PSChildEntry[] entries, PSConverter callingConverter)
   {
      if (serverChild == null)
         throw new IllegalArgumentException("serverChild cannot be null");
      
      if (entries == null)
         return;
      
      if (callingConverter == null)
         throw new IllegalArgumentException("callingConverter cannot be null");

      for (PSChildEntry entry : entries)
      {
         serverChild.addEntry((PSItemChildEntry) callingConverter.getConverter(
            PSItemChildEntry.class).convert(PSItemChildEntry.class, entry));
      }      
   }
   
   /**
    * Get the item definition for the specified content type.
    * 
    * @param contentType the content type for which to get the item
    *    definition, not <code>null</code> or empty.
    * @return the requested item definition, never <code>null</code>.
    * @throws ConversionException if no item definition is registered for
    *    the specified content type.
    */
   public static PSItemDefinition getItemDefinition(String contentType) 
      throws ConversionException
   {
      if (StringUtils.isBlank(contentType))
         throw new IllegalArgumentException(
            "contentType cannot be null or empty");
      
      PSItemDefManager itemDefMgr = PSItemDefManager.getInstance();
      
      try
      {
         PSItemDefinition def = itemDefMgr.getItemDef(contentType, 
            PSItemDefManager.COMMUNITY_ANY);
         
         return def;
      }
      catch (PSInvalidContentTypeException e)
      {
         throw new ConversionException(
            "Unregistered content type : " + contentType);
      }
   }
   
   /**
    * Get the item definition for the specified content type id.
    * 
    * @param contentTypeId the id of the content type for which to get the item 
    *    definition.
    * @return the requested item definition, never <code>null</code>.
    * @throws ConversionException if no item definition is registered for
    *    the specified content type id.
    */
   public static PSItemDefinition getItemDefinition(long contentTypeId)
      throws ConversionException
   {
      PSItemDefManager itemDefMgr = PSItemDefManager.getInstance();
      try
      {
         return getItemDefinition(itemDefMgr.contentTypeIdToName(
            contentTypeId));
      }
      catch (PSInvalidContentTypeException e)
      {
         throw new ConversionException(
            "Unregistered content type id : " + contentTypeId);
      }
   }
   
   /**
    * Get the field definition for the supplied parameters.
    * 
    * @param contentType the content type fo which to get the field 
    *    definitions, not <code>null</code> or empty.
    * @param fieldName the field name for which to get the definition, not
    *    <code>null</code> or empty.
    * @return the field definition, may be <code>null</code> if not found.
    */
   public static com.percussion.design.objectstore.PSField getFieldDefinition(
      String contentType, String fieldName)
   {
      if (StringUtils.isBlank(contentType))
         throw new IllegalArgumentException(
            "contentType cannot be null or empty");

      if (StringUtils.isBlank(fieldName))
         throw new IllegalArgumentException(
            "fieldName cannot be null or empty");
      
      PSItemDefinition def = getItemDefinition(contentType);

      com.percussion.design.objectstore.PSField fieldDef = 
         def.getFieldByName(fieldName);
      
      return fieldDef;
   }
   
   /**
    * Convert the all folder paths of the supplied item to an array of
    * client folders.
    * 
    * @param folderPaths the list of folder paths to convert, may be 
    *    <code>null</code> or empty.
    * @return an array with all client item folder paths for the supplied id,
    *    never <code>null</code>, may be empty.
    */
   public static PSItemFolders[] toClientFolders(List<String> folderPaths) 
   {
      if (folderPaths == null)
         return new PSItemFolders[0];
      
      PSItemFolders[] folders = new PSItemFolders[folderPaths.size()];
      for (int i=0; i<folderPaths.size(); i++)
         folders[i] = new PSItemFolders(folderPaths.get(i));
      
      return folders;
   }
   
   /**
    * Convert the supplied client folders to server folders for the specified 
    * item id. Existing parent folders of the specified item will remain 
    * untouched, new ones will be added and removed ones will be deleted.
    * 
    * @param folders a list with client folders to convert to server folders,
    *    may be <code>null</code> or empty.
    * @return a list with all server folders, never <code>null</code>, may
    *    be empty.
    */
   public static List<String> toServerFolders(List<PSItemFolders> folders)
   {
      List<String> folderPaths = new ArrayList<String>();

      if (folders != null)
      {
         for (PSItemFolders folder : folders)
            folderPaths.add(folder.getPath());
      }
      
      return folderPaths;
   }
   
   /**
    * Convert the supplied server related items to client related items.
    * 
    * @param relatedItems the server related items to convert, not 
    *    <code>null</code>.
    * @param callingConverter the converter calling this method, not
    *    <code>null</code>.
    * @return the server related items converted to client rellated items
    *    grouped by slots, never <code>null</code>, may be empty.
    */
   public static PSItemSlots[] toClientRelatedContent(
      Iterator<PSItemRelatedItem> relatedItems, PSConverter callingConverter) 
   {
      Map<String, List<PSRelatedItem>> relatedItemsBySlot = 
         new HashMap<String, List<PSRelatedItem>>();
      Map<IPSGuid, String> slotIdName = new HashMap<IPSGuid, String>();
      IPSAssemblyService asrv = PSAssemblyServiceLocator.getAssemblyService();
      
      while (relatedItems.hasNext())
      {
         PSItemRelatedItem relatedItem = relatedItems.next();
         
         String slotName = getSlotName(relatedItem, slotIdName, asrv);
         List<PSRelatedItem> destRelatedItems = 
            relatedItemsBySlot.get(slotName);
         if (destRelatedItems == null)
         {
            destRelatedItems = new ArrayList<PSRelatedItem>();
            relatedItemsBySlot.put(slotName, destRelatedItems);
         }
         
         destRelatedItems.add((PSRelatedItem) callingConverter.getConverter(
            PSRelatedItem.class).convert(PSRelatedItem.class, relatedItem));
      }
      
      PSItemSlots[] destSlots = new PSItemSlots[relatedItemsBySlot.size()];
      int index = 0;
      for (String slotName : relatedItemsBySlot.keySet())
      {
         PSItemSlots destSlot = new PSItemSlots();
         destSlot.setName(slotName);
         
         List<PSRelatedItem> destRelatedItems = 
            relatedItemsBySlot.get(slotName);
         destSlot.setPSRelatedItem(destRelatedItems.toArray(
            new PSRelatedItem[destRelatedItems.size()]));
         
         destSlots[index++] = destSlot;
      }
      
      return destSlots;
   }
   
   /**
    * Get the slot name from the given related item.
    * 
    * @param relatedItem the related item that contains slot ID, assumed not
    *    <code>null</code>.
    * @param slotIdName slot ID/name mapping. The key is the slot ID, value is
    *    the slot name, assumed not <code>null</code>, may be empty. This map i
    *    s used to cache data that is retrieved by this method for use in 
    *    future calls to this method. The first time in, it should be empty, 
    *    then pass the same map back in for each additional call.
    * @param asrv the assembly service, assumed not <code>null</code>.
    * 
    * @return the slot name, never <code>null</code>.
    */
   private static String getSlotName(PSItemRelatedItem relatedItem,
         Map<IPSGuid, String> slotIdName, IPSAssemblyService asrv)
   {
      PSAaRelationship rel = relatedItem.getRelationship();
      if (rel == null)
         throw new IllegalArgumentException(
            "missing required related item relationship");
      String name = rel.getSlotName();
      if (StringUtils.isNotBlank(name))
         return name;
      
      IPSGuid id = rel.getSlotId();
      name = slotIdName.get(id);
      if (name != null)
         return name;
      
      name = asrv.loadSlot(id).getName();
      slotIdName.put(id, name);
      
      return name;
   }
   
   /**
    * Converter the supplied slots into server related items and save them
    * to the supplied item.
    * 
    * @param serverItem the item to which to set the converter slots, not
    *    <code>null</code>. 
    * @param slots the slots to convert, may be <code>null</code> or empty.
    * @param callingConverter the converter calling this method, not
    *    <code>null</code>.
    */
   public static void toServerRelatedContent(PSCoreItem serverItem, 
      PSItemSlots[] slots, PSConverter callingConverter)
   {
      if (serverItem == null)
         throw new IllegalArgumentException("serverItem cannot be null");
      
      if (callingConverter == null)
         throw new IllegalArgumentException("callingConverter cannot be null");
      
      if (slots == null)
         return;
      
      Map<String, PSItemRelatedItem> destRelatedItems = 
         new HashMap<String, PSItemRelatedItem>();

      for (PSItemSlots slot : slots)
      {
         PSRelatedItem[] relatedItems = slot.getPSRelatedItem();
         for (PSRelatedItem relatedItem : relatedItems)
         {
            PSItemRelatedItem destRelatedItem = 
               (PSItemRelatedItem) callingConverter.getConverter(
                  PSItemRelatedItem.class).convert(
                     PSItemRelatedItem.class, relatedItem);
            
            PSDesignGuid guid = new PSDesignGuid(
               relatedItem.getPSAaRelationship().getId());
            destRelatedItems.put(Integer.toString(guid.getUUID()), 
               destRelatedItem);
         }
      }
      
      serverItem.setRelatedItems(destRelatedItems);
   }
}

