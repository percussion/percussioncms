/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.cms.objectstore.client;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.PSDisplayChoices;
import com.percussion.cms.objectstore.IPSFieldCataloger;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Client side object containing the cataloged content editor fields. 
 */
public class PSContentEditorFieldCataloger
{
   /**
    * Construct this cataloger without an initial set of fields.  See
    * {@link #PSContentEditorFieldCataloger(IPSFieldCataloger, Set, int)} for
    * more info.
    */
   public PSContentEditorFieldCataloger(IPSFieldCataloger cataloger,
      int controlFlags) throws PSCmsException
   {
      if (cataloger == null)
         throw new IllegalArgumentException("cataloger may not be null");
      
      m_fieldCat = cataloger;
      m_controlFlags = controlFlags;
   }
   
   /**
    * Construct this cataloger with an initial set of fields
    * 
    * @param cataloger The field cataloger to use to retrieve the fields, may
    * not be <code>null</code>.
    * @param fields An optional list of fields to initially catalog, may be
    * <code>null</code> or empty to catalog all fields.
    * @param controlFlags Any of the {@link IPSFieldCataloger}.FLAG_XXX values
    * or'd together used to filter the fields returned.
    * 
    * @throws PSCmsException if there are any errors loading the fields.
    */
   public PSContentEditorFieldCataloger(IPSFieldCataloger cataloger, 
      Set<String> fields, int controlFlags) throws PSCmsException
   {
      this(cataloger, controlFlags);
      loadFields(fields, controlFlags);
   }

   /**
    * Convenience method that calls {@link #loadFields(Set, int, boolean) 
    * loadFields(fields, controlFlags, true)}
    */
   public void loadFields(Set<String> fields, int controlFlags) 
   throws PSCmsException
   {
      loadFields(fields, controlFlags, true);
   }
   
   /**
    * Load the specified fields and add them to the catalog. If the field
    * already exists in the catalog, it will not be replaced if the existing
    * field in the catalog has choices loaded and the loaded field does not have
    * choices (regardless of the value for the <code>refresh</code> parameter). 
    * See {@link IPSFieldCataloger#FLAG_EXCLUDE_CHOICES} for more info.
    * 
    * @param fields An optional list of fields to catalog, may be
    * <code>null</code> or empty to catalog all fields.
    * @param controlFlags Any of the {@link IPSFieldCataloger}.FLAG_XXX values
    * or'd together used to filter the fields returned.
    * @param refresh <code>true</code> recatalog the field even if it has been
    * previously cataloged, <code>false</code> to skip previously cataloged
    * fields.  Ignored if <code>fields</code> is <code>null</code> or empty.
    * 
    * @throws PSCmsException if there are any errors loading the fields. 
    */
   public void loadFields(Set<String> fields, int controlFlags, boolean refresh) 
      throws PSCmsException
   {
      Set<String> fieldsToLoad = null;
      boolean noChoices = IPSFieldCataloger.FLAG_EXCLUDE_CHOICES == 
         (controlFlags & IPSFieldCataloger.FLAG_EXCLUDE_CHOICES);
      boolean allFields = (fields == null || fields.isEmpty());
      
      if (!refresh)
      {
         if (allFields)
         {
            if (m_fieldTracker.allFieldsLoaded(noChoices))
               return;
         }
         else
         {
            fieldsToLoad = m_fieldTracker.getFieldsToLoad(fields, noChoices);
            if (fieldsToLoad.isEmpty())
               return;
         }
      }
      else
         fieldsToLoad = fields;
      
      Element e = m_fieldCat.getCEFieldXml(controlFlags, fieldsToLoad);
      
      if(e != null)
    	  fromXml(e);
      
      if (allFields)
         m_fieldTracker.setAllLoaded(noChoices);
      else
         m_fieldTracker.addLoadedFields(fields);
   }
   
   /**
    * Get the control flags supplied during construction.
    * 
    * @return The flags.
    */
   public int getControlFlags()
   {
      return m_controlFlags;
   }

   /**
    * The method creates the objects from the supplied xml and adds them to the
    * catalog.
    * 
    * @param src assumed not <code>null</code> and to conform to the element
    * returned by {@link IPSFieldCataloger#getCEFieldXml(int, Set)}
    * 
    * @throws PSCmsException if there are any errors.
    */
   private void fromXml(Element src) throws PSCmsException
   {
      processNode(src, SYSTEM, m_systemMap, null);
      processNode(src, SHARED, m_sharedMap, null);
      processNode(src, LOCAL, m_localMap, m_localContentTypeMap);
   }
   
   /**
    * Parse the field elements from the specified child of the supplied node and 
    * populate the supplied field map.
    * 
    * @param src The node to process, assumed not <code>null</code> and to 
    * conform to the element returned by 
    * {@link IPSFieldCataloger#getCEFieldXml(int, Set)}.
    * @param tagName The tag name of the immediate child of the supplied
    * element from which fields will be parsed, assumed not <code>null</code> or 
    * empty.
    * @param fieldMap The map to populate, assumed not <code>null</code>, the 
    * key is the field name, the value is the field.
    * @param typeMap Optional map to populate mapping content type to a list of
    * fields, may be <code>null</code> to avoid this processing.
    * 
    * @throws PSCmsException If the XML is invalid.
    */
   private void processNode(Element src, String tagName, 
      Map<String, PSLightWeightField> fieldMap, Map<String, 
      Collection<PSLightWeightField>> typeMap) throws PSCmsException
   {
      NodeList nodeList = src.getElementsByTagName(tagName);
      Node node = nodeList.item(0);
      
      if (node != null)
      {
         Map<String, List<PSLightWeightField>> tmpMap = 
            new HashMap<>();
         populateMaps((Element) node, tmpMap, typeMap);
         Iterator entries = tmpMap.keySet().iterator();
         while (entries.hasNext())
         {
            String name = (String) entries.next();
            List<PSLightWeightField> l = tmpMap.get(name);
            Collections.sort(l);
            PSLightWeightField newField = l.get(0);
            PSLightWeightField curField = fieldMap.get(name);
            if (curField == null || replaceField(curField, newField))
               fieldMap.put(name, newField);
         }
      }      
   }

   /**
    * Populate several maps that hold info related to system, shared and
    * local fields from all the defined content editors.
    *
    * @param elem 'System', 'Shared' or 'Local' element. Assumed to be not
    * <code>null</code>.
    * @param groups maps search field name to a list of fields,
    * assumed to be not <code>null</code>.
    * @param contentTypes maps content type name to one or more light weight
    * fields, may be <code>null</code>.
    *
    * @throws PSCmsException if there are any errors.
    */
   private void populateMaps(Element elem, 
      Map<String, List<PSLightWeightField>> groups, 
      Map<String, Collection<PSLightWeightField>> contentTypes) 
      throws PSCmsException
   {
      Element fieldElem = null;
      if (elem != null)
      {
         NodeList searchFields = elem.getElementsByTagName(SEARCH_FIELD);
         int len = searchFields.getLength();

         for (int k = 0; k < len; k++)
         {
            Element searchFieldElem = (Element) searchFields.item(k);
            String fieldName = searchFieldElem.getAttribute(INTERNALNAME);
            List<PSLightWeightField> group = groups.get(fieldName);
            if (null == group)
            {
               group = new ArrayList<>();
               groups.put(fieldName, group);
            }
            
            NodeList fieldList = searchFieldElem.getElementsByTagName(FIELD);
            int fieldCt = fieldList.getLength();
            for (int i=0; i < fieldCt; i++)
            {
               fieldElem = (Element) fieldList.item(i);
               String ctype = fieldElem.getAttribute(CONTENTID);

               PSLightWeightField f = new PSLightWeightField(fieldName,
                     fieldElem.getAttribute(TYPE),
                     fieldElem.getAttribute(DISPLAYNAME),
                     fieldElem.getAttribute(MNEMONIC));
               

               // see if there are choices defined
               PSXmlTreeWalker tree = new PSXmlTreeWalker(fieldElem);
               Element choices = tree.getNextElement(
                  PSDisplayChoices.XML_NODE_NAME,
                  PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
               if (choices != null)
               {
                  try
                  {
                     f.setDisplayChoices(new PSDisplayChoices(choices));
                  }
                  catch (PSUnknownNodeTypeException e)
                  {
                     // this should never happen
                     throw new PSCmsException(IPSCmsErrors.UNEXPECTED_ERROR,
                        e.getLocalizedMessage());
                  }
               }

               group.add(f);

               if (ctype != INVALID_CONTENT_TYPE && contentTypes != null)
               {
                  Collection<PSLightWeightField> c = contentTypes.get(ctype);
                  if (null == c)
                  {
                     c = new ArrayList<>();
                     contentTypes.put(ctype, c);
                  }
                  
                  addField(f, c);
               }
            }
         }
      }
   }

   /**
    * Adds the supplied field to the collection, unless the field already exists
    * in the collection with choices defined, and the supplied field does not
    * have choices defined.
    * 
    * @param field The field to add, assumed not <code>null</code>.
    * @param coll The collection to add to, assumed not <code>null</code>.
    */
   private void addField(PSLightWeightField field, 
      Collection<PSLightWeightField> coll)
   {
      boolean foundMatch = false;
      Iterator<PSLightWeightField> fields = coll.iterator();
      while (fields.hasNext())
      {
         PSLightWeightField test = fields.next();
         if (field.getInternalName().equals(test.getInternalName()))
         {
            if (replaceField(test, field))
            {
               fields.remove();
               coll.add(field);
            }
            
            foundMatch = true;
            break;
         }
      }
      
      if (!foundMatch)
         coll.add(field);
   }

   /**
    * Determine if the current field should be replaced by the new field based
    * on whether they have choices loaded. Chooses the new field unless the
    * current field has choices loaded and the new field does not.
    * 
    * @param curField The current field, assumed not <code>null</code>.
    * @param newField The new field, assumed not <code>null</code> and to 
    * represent the same field as <code>curField</code>.
    * 
    * @return <code>true</code> if the new field should replace the current
    * field, <code>false</code> if the current field should still be used.
    */
   private boolean replaceField(PSLightWeightField curField, 
      PSLightWeightField newField)
   {
      return !hasChoicesLoaded(curField) || hasChoicesLoaded(newField);
   }
   
   /**
    * Determine if the supplied field has choices that are loaded.
    * 
    * @param field The field to check, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the field has choices and the choices are
    * loaded, <code>false</code> if not.
    */
   private boolean hasChoicesLoaded(PSLightWeightField field)
   {
      PSDisplayChoices fieldChoices = field.getDisplayChoices();
      return (fieldChoices != null && fieldChoices.areChoicesLoaded());
   }
   
   /**
    * Gets a map containing system fields with internal field name as key and
    * {@link PSLightWeightField} object as value. It's provided as a convenience
    * for getting unique fields over a similar method which returns a list.
    * 
    * @return The map, never <code>null</code> or empty.
    */
   public Map<String, PSLightWeightField> getSystemMap()
   {
      return Collections.unmodifiableMap(m_systemMap);
   }

   /**
    * Gets a map containing local fields with internal field name as key and
    * {@link PSLightWeightField} object as value. It's provided as a convenience
    * for getting unique fields over a similar method which returns a list.
    * 
    * @return The map, never <code>null</code> or empty.
    */
   public Map<String, PSLightWeightField> getLocalMap()
   {
      return Collections.unmodifiableMap(m_localMap);
   }

   /**
    * Returns a sorted map that maps content type name to a Collection of one or
    * more LOCAL fields wrapped in as objects of {@link PSLightWeightField}.
    * 
    * @return map as described above, never <code>null</code> or empty.
    */
   public Map<String, Collection<PSLightWeightField>> getLocalContentTypeMap()
   {
      return Collections.unmodifiableMap(m_localContentTypeMap);
   }

   /**
    * Gets a map containing shared fields with internal field name as key and
    * {@link PSLightWeightField} object as value. It's provided as a convenience
    * for getting unique fields over a similar method which returns a list.
    * 
    * @return The map, never <code>null</code> or empty.
    */
   public Map<String, PSLightWeightField> getSharedMap()
   {
      return Collections.unmodifiableMap(m_sharedMap);
   }

   /**
    * Gets a map of maps containing type - 'Shared', 'System', 'Local' as key
    * and system, shared and local maps as value.
    *
    * @return The map, never <code>null</code> or empty.
    */
   public Map<String, Map<String, PSLightWeightField>> getAll()
   {
      Map<String, Map<String, PSLightWeightField>> all = 
         new HashMap<>();
      all.put(SYSTEM, getSystemMap());
      all.put(SHARED, getSharedMap());
      all.put(LOCAL, getLocalMap());
      
      return all;
   }

   /**
    * Get the display choices for the matching field in this catalog.  Checks 
    * for a matching field with choices in the local fields, then shared, and 
    * finally system, matching case-sensitive on internal name and 
    * case-insensitive on data type.  
    *
    * @param field The field, may not be <code>null</code>.
    *
    * @return The choices, may be <code>null</code> if choices have not been
    * set.
    */
   public PSDisplayChoices getDisplayChoices(PSLightWeightField field)
   {
      if (field == null)
         throw new IllegalArgumentException("field may not be null");
         
      return getDisplayChoices(field.getInternalName(), field.getDataType());
   }
   
   /**
    * Get the display choices for the matching field in this catalog.  Checks 
    * for a matching field with choices in the local fields, then shared, and 
    * finally system, matching case-sensitive on internal name and
    * case-insensitive on data type.  
    *
    * @param intName The internal name of the field, may not be 
    * <code>null</code> or empty.
    * @param dataType The datatype of the field, may not be <code>null</code> 
    * or empty, usually one of the <code>PSField.DT_xxx</code> constants.
    *
    * @return The choices, may be <code>null</code> if choices have not been
    * set.
    */
   public PSDisplayChoices getDisplayChoices(String intName, String dataType)
   {
      if (intName == null || intName.trim().length() == 0)
         throw new IllegalArgumentException("intName may not be null or empty");
      if (dataType == null || dataType.trim().length() == 0)
         throw new IllegalArgumentException(
            "dataType may not be null or empty");
      
      
      // see if we have choices.  first check local map, then shared,
      // then system.
      PSDisplayChoices choices = null;
      PSLightWeightField ltField;
      ltField = locateField(m_localMap, intName, dataType);
      if (ltField != null && ltField.getDisplayChoices() != null)
         choices = ltField.getDisplayChoices();
      else
      {
         ltField = locateField(m_sharedMap, intName, dataType);
         if (ltField != null && ltField.getDisplayChoices() != null)
            choices = ltField.getDisplayChoices();
         else
         {
            ltField = locateField(m_systemMap, intName, dataType);
            if (ltField != null && ltField.getDisplayChoices() != null)
               choices = ltField.getDisplayChoices();
         }
      }

      return choices;
   }

   /**
    * Get the mnemonic key for the field with given internal name. Looks up in 
    * the local, shared and system field map in order.   
    *
    * @param intName Internal name of the field, may not be <code>null</code> 
    * or empty.
    *
    * @return knemonic key as string, will be <code>null</code> if it is not 
    * defined in any of the field maps for the field. 
    */
   public String getMnemonicKey(String  intName)
   {
      if (StringUtils.isEmpty(intName))
      {
         throw new IllegalArgumentException("intName must not be null or empty");      
      }
      
      // see if we have mnemonic key for the field.  first check local map, 
      //then shared, then system.
      String mnemonic = null;

      PSLightWeightField ltField =
         m_localMap.get(intName);
      if (ltField == null)
         ltField = m_sharedMap.get(intName);
      if (ltField == null)
         ltField = m_systemMap.get(intName);

      if (ltField != null && ltField.getMnemonic() != null)
         mnemonic = ltField.getMnemonic();

      return mnemonic;
   }

   /**
    * Get the matching lightweight field from the supplied map, matching
    * case-sensitive on internal name, case-insensitive on data type.
    *
    * @param catalogMap The map to check, the key is the internal name as a
    * <code>String</code>, the value is the matching
    * <code>PSLightWeightField</code> object, assumed not <code>null</code>.
    * @param intName The internal name of the field, assumed not 
    * <code>null</code> or empty.
    * @param dataType The datatype of the field, assumed not <code>null</code> 
    * or empty, usually one of the <code>PSField.DT_xxx</code> constants.
    * 
    * @return The matching field, or <code>null</code> if not found.
    */
   private PSLightWeightField locateField(Map catalogMap, String intName, 
      String dataType)
   {
      PSLightWeightField ltField = null;

      PSLightWeightField test = (PSLightWeightField)catalogMap.get(intName);
      if (test != null)
      {
         if (dataType.equalsIgnoreCase(test.getDataType()))
         {
            ltField = test;
         }
      }

      return ltField;
   }

   
   
   /**
    * Convenience class abstracting the display name, datatype and id of a
    * content field. It's being used as a value in <code>m_sharedMap</code>
    * <code>m_localMap</code> with field's internal name as the key.
    */
   public class Field implements Comparable
   {
      /**
       * Constructs the object using three arguments.
       *
       * @param dataType the data type of the field, assumed to be not <code>
       * null</code>.
       *
       * @param dispName the display name, assumed to be not <code>
       * null</code>.
       *
       * @param id the id of the field, assumed to be not <code>
       * null</code>.
       */
      Field(String dataType, String dispName, String id)
      {
         m_dataType = dataType;
         m_displayName = dispName;
         m_contentId = id;
      }

      // see interface for description
      public int compareTo(Object o)
      {
         return m_displayName.compareTo(((Field) o).m_displayName);
      }

      /**
       * Constructs the object using two arguments.
       *
       * @param dataType the data type of the field, assumed to be not <code>
       * null</code>.
       *
       * @param id the id of the field, assumed to be not <code>
       * null</code>.
       */
      Field(String dataType, int id)
      {
         m_dataType = dataType;
         m_contentId = Integer.toString(id);
         m_displayName = "";
      }

      /**
       * Get the data type.
       *
       * @return The data type, never <code>null</code>.
       */
      public String getDataType()
      {
         return m_dataType;
      }

      /**
       * Get the id.
       *
       * @return The id, never <code>null</code>.
       */
      public String getId()
      {
         return m_contentId;
      }

      /**
       * Get the display name.
       *
       * @return The name, never <code>null</code>.
       */
      public String getDisplayName()
      {
         return m_displayName;
      }


      /**
       * Content id of the field, initialized in the ctor, never <code>null
       * </code> or empty after that.
       */
      private String m_contentId;

      /**
       * Data type of the field, initialized in the ctor, never <code>null
       * </code> or empty after that.
       */
      private String m_dataType;

      /**
       * Display name of the field, initialized in one of the ctors, may be <
       * code>null</code>.
       */
      private String m_displayName;
   }
   
   /**
    * Field cataloger supplied during ctor, never <code>null</code> or modified
    * after that.
    */
   private IPSFieldCataloger m_fieldCat;

   /**
    * System map for system content editor fields. The key is the internal name
    * of the field and the value is a Collection containing <code>PSLightWeightField
    * </code> types, abstracting display name, data type and content type id.
    * Never <code>null</code>.
    */
   private Map<String, PSLightWeightField> m_systemMap = 
      new HashMap<>();

   /**
    * Shared map for shared content editor fields. The key is the internal name
    * of the field and the value is a List containing <code>PSLightWeightField</code>
    * types, abstracting display name, data type and content type id. Since
    * any given name could occur in multiple editors, we provide all the data
    * so the client can display the results in different ways. Never
    * <code>null</code>.
    */
   private Map<String, PSLightWeightField> m_sharedMap = 
      new HashMap<>();

   /**
    * Local map for local fields. See <code>m_sharedMap</code> for details.
    * Never <code>null</code>.
    */
   private Map<String, PSLightWeightField> m_localMap = 
      new HashMap<>();

   /**
    * Maps local content types to one or more light weight fields,
    * never <code>null</code>.
    */
   private Map<String, Collection<PSLightWeightField>> m_localContentTypeMap = 
      new TreeMap<>();
   
   /**
    * The control flags supplied during construction.
    */
   private int m_controlFlags;

   private PSFieldTracker m_fieldTracker = new PSFieldTracker();

   /**
    * Used to represent that there is not content type associated with a field.
    */
   private static final String INVALID_CONTENT_TYPE = "-1";

   // node names
   public  static final String SYSTEM = "System";
   public  static final String SHARED = "Shared";
   public  static final String LOCAL = "Local";
   private static final String FIELD = "Field";
   private static final String SEARCH_FIELD = "SearchField";
   //attribute names for "Field" node
   private static final String CONTENTID = "contentTypeId";
   private static final String DISPLAYNAME = "displayName";
   private static final String MNEMONIC = "mnemonic";
   private static final String TYPE = "datatype";
   private static final String INTERNALNAME = "name";
   
   /**
    * Class to track which fields have been previously loaded.
    */
   private class PSFieldTracker
   {
      /**
       * Set that all fields have been loaded.
       * 
       * @param noChoices <code>true</code> if the request to load indicated
       * that choices should not be loaded, <code>false</code> otherwise.  If
       * <code>false</code> and this method has been previously called with
       * a <code>true</code> value, then the call is a noop.
       */
      public void setAllLoaded(boolean noChoices)
      {
         if (noChoices)
         {
            if (!m_allLoadedChoices)
               m_allLoadedNoChoices = true;
         }
         else
         {
            m_allLoadedChoices = true;
            m_allLoadedNoChoices = false;
         }
      }

      /**
       * Determine if all fields have been previously loaded.
       * 
       * @param noChoices <code>true</code> to indicate loading without choices,
       * <code>false</code> for loading with choices. 
       * 
       * @return <code>true</code> if all fields should have been considered
       * loaded.  If <code>noChoices</code> is <code>false</code>, returns
       * <code>true</code> if previously loaded either with or without choices,
       * if <code>true</code>, then returns whether all fields have been 
       * previously loaded with choices.
       */
      public boolean allFieldsLoaded(boolean noChoices)
      {
         if (noChoices)
            return m_allLoadedChoices || m_allLoadedNoChoices;

         return m_allLoadedChoices;
      }

      /**
       * Given a set of fields to load, returns only those fields that have not
       * yet been loaded.
       * 
       * @param requestedFields The set of fields requested to load, assumed not 
       * <code>null</code>.
       * @param noChoices <code>true</code> to indicate loading without choices,
       * <code>false</code> for loading with choices. 
       * 
       * @return A set containing only the requested fields that have not
       * been previously loaded.
       */
      public Set<String> getFieldsToLoad(Set<String> requestedFields, 
         boolean noChoices)
      {
         Set<String> fieldsToLoad = new HashSet<>(requestedFields);
         if (allFieldsLoaded(noChoices))
            fieldsToLoad.clear();
         else
            fieldsToLoad.removeAll(m_loadedFields);
         
         return fieldsToLoad;
      }
    
      /**
       * Add a set of fields that have been loaded.
       * 
       * @param fields The set of fields, assumed not <code>null</code>.
       */
      public void addLoadedFields(Set<String> fields)
      {
         m_loadedFields.addAll(fields);
      }
      
      
      /**
       * Set of field names that have been loaded.
       */
      private Set<String> m_loadedFields = new HashSet<>();
      
      private boolean m_allLoadedNoChoices = false;
      private boolean m_allLoadedChoices = false;      
   }
}
