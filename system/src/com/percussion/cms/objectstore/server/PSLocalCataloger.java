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

import com.percussion.cms.PSChoiceBuilder;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.PSDisplayChoices;
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.IPSCataloger;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSRelationshipInfo;
import com.percussion.cms.objectstore.PSRelationshipInfoSet;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSExecutionData;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSChoices;
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSDisplayText;
import com.percussion.design.objectstore.PSEntry;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSSearchConfig;
import com.percussion.design.objectstore.PSSearchProperties;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.design.objectstore.PSSharedFieldGroup;
import com.percussion.design.objectstore.PSUIDefinition;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.error.PSErrorManager;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.server.PSServerLogHandler;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Cataloger used on the server side to make a request to the server on behalf
 * of a component on the server side e.g a loadable handler could request system
 * shared or local fields from this class rather than requesting directly from
 * the server.
 */
public class PSLocalCataloger implements IPSCataloger
{
   /**
    * Construct an instance with a request object.
    *
    * @param psRequest The request object. It may not be <code>null</code>
    *    It must be an instance of <code>PSRequest</code>.
    */
   public PSLocalCataloger(Object psRequest)
   {
      if (psRequest == null)
         throw new IllegalArgumentException("request may not be null");

      if (!(psRequest instanceof PSRequest))
         throw new IllegalArgumentException(
         "object has to be an instanceof PSRequest");

      m_request = (PSRequest) psRequest;
   }

   /**
    * You get the same fields you would get if calling
    * {@link #getCEFieldXml(int)}, but only system fields are processed. This
    * is much more efficient than using the previously mentioned method and the
    * class that processes the document returned by that method.
    * <p>
    * This method differs slightly from the other one in that you will always
    * get fields back, even if there were no content editors defined in the
    * system. In all practical situations, this will never occur.
    * 
    * @param controlFlags See {@link #getCEFieldXml(int)} for description.
    * 
    * @return Never <code>null</code>. Each entry is the internal name of a
    * system field that matches the supplied flags.
    */
   @SuppressWarnings("unchecked")
   public Set getSystemFields(int controlFlags)
   {
      initFieldCatalog();
      
      processSystemOnlyFields(PSServer.getContentEditorSystemDef(),
            controlFlags);
      return Collections.unmodifiableSet(m_systemMap.keySet());
   }
   
   // see interface
   public Element getCEFieldXml(int controlFlags) throws PSCmsException
   {
      return getCEFieldXml(controlFlags, null);
   }
   
   //see the interface.
   @SuppressWarnings("unused")
   public Element getCEFieldXml(int controlFlags, Set<String> fields)
      throws PSCmsException
   {
      initFieldCatalog();
      m_fieldNames = fields;
      
      PSItemDefManager mgr = PSItemDefManager.getInstance();
      PSSecurityToken secTok = m_request.getSecurityToken();
      int communityId = secTok.getCommunityId();
      if((controlFlags & FLAG_RESTRICT_TOUSERCOMMUNITY)==0)
         communityId = PSItemDefManager.COMMUNITY_ANY;
      long[] typeArray = mgr.getContentTypeIds(communityId);

      int len = 0;
      if (typeArray != null)
         len = typeArray.length;

      PSItemDefinition itemDef = null;
      PSContentEditorMapper mapper = null;
      PSContentEditorPipe pipe = null;
      PSDisplayMapper dispMapper = null;

      boolean foundOne = false;
      for (int k=0; k<len; k++)
      {
         try
         {
            itemDef = mgr.getItemDef(typeArray[k], communityId);
            //If control flags has a 
            if((controlFlags & FLAG_CTYPE_EXCLUDE_HIDDENFROMMENU)!= 0 
               && itemDef.isHidden())
               continue;
            pipe = (PSContentEditorPipe)itemDef.getContentEditor().getPipe();
            mapper = pipe.getMapper();
            dispMapper = mapper.getUIDefinition().getDisplayMapper();
            if (mapper.getFieldSet().isUserSearchable())
            {
               recurseMapping(dispMapper, mapper,
                  mgr.contentTypeIdToLabel(typeArray[k]), controlFlags);
                     
               foundOne = true;
            }
         }
         catch (PSInvalidContentTypeException e)
         {
            /* ignore, this should only happen if a content type is shutdown
               between the time we ask for the list of content types and
               we actually request the specific type */
         }
         catch(SecurityException secEx)
         {
            /* skip this content type and keep going */
         }
      }

      if (foundOne)
      {
         // don't add these system fields unless there is at least 1 editor
         processSystemOnlyFields(PSServer.getContentEditorSystemDef(),
            controlFlags);
      }
      
      // add "saved" shared fields
      addSharedFields(PSServer.getContentEditorSharedDef(), controlFlags);

      return toXml(controlFlags);
   }

   /**
    * Adds fields to the shared map for all shared fields specified by the
    * {@link #m_sharedFields} map.
    * 
    * @param sharedDef The shared def to use, may be <code>null</code> in which
    * case this method simply returns.
    * @param controlFlags The control flags to use to filter fields.
    */
   private void addSharedFields(PSContentEditorSharedDef sharedDef, 
      int controlFlags)
   {
      if (sharedDef == null)
         return;
      
      Iterator entries = m_sharedFields.entrySet().iterator();
      while (entries.hasNext())
      {
         Map.Entry entry = (Entry) entries.next();
         String fsName = (String) entry.getKey();
         Set fieldNameSet = (Set) entry.getValue();
         
         Iterator groups = sharedDef.getFieldGroups();
         while (groups.hasNext())
         {
            PSSharedFieldGroup group = (PSSharedFieldGroup) groups.next();
            PSUIDefinition uiDef = group.getUIDefinition();
            
            PSFieldSet fs = group.getFieldSet();
            PSDisplayMapper mapper = uiDef.getDisplayMapper(fs.getName());
            if (!fs.getName().equals(fsName))
            {
               // try for child
               Object o = fs.get(fsName);
               if (o != null && o instanceof PSFieldSet)
               {
                  fs = (PSFieldSet) o;
                  if (!fs.getName().equals(fsName))
                     continue;
                  mapper = uiDef.getDisplayMapper(fsName);
               }
               else
                  continue;
            }
            
            if (mapper == null)
               continue;
            
            Iterator fields = fieldNameSet.iterator();
            while (fields.hasNext())
            {
               String fieldName = (String) fields.next();
               PSField field = fs.findFieldByName(fieldName);
               PSDisplayMapping mapping = mapper.getMapping(fieldName);
               if (mapping != null && field != null)
               {
                  processField(INVALID_CONTENT_TYPE, controlFlags, 
                     mapping.getUISet(), fieldName, field);
               }
            }
         }         
      }
   }

   //  See IPSCataloger#getSearchConfig() interface
   @SuppressWarnings("unused")
   public PSSearchConfig getSearchConfig() throws PSCmsException
   {
      PSServerConfiguration conf = PSServer.getServerConfiguration();
      return conf.getSearchConfig();
   }
   
   /**
    * See {@link IPSCataloger#getRelationshipInfoSet() interface}
    */
   @SuppressWarnings("unused")
   public PSRelationshipInfoSet getRelationshipInfoSet()
      throws PSCmsException
   {
      PSRelationshipInfoSet infoSet = new PSRelationshipInfoSet();

      Iterator configs = PSRelationshipCommandHandler.getRelationshipConfigs();
      while (configs.hasNext())
      {
         PSRelationshipConfig config = (PSRelationshipConfig) configs.next();
         PSRelationshipInfo info = new PSRelationshipInfo(config);
         infoSet.add(info);
      }
      return infoSet;
   }

   /**
    * Scans the supplied set looking for read-only fields. All readonly fields
    * are processed the same as those that had been included in the content
    * editors. So called read-only fields are populated by the content
    * editor engine and not allowed to be modified directly by users.
    *
    * @param sysDef The system def. Assumed not <code>null</code>.  If its 
    * fieldset is <code>null</code>, returns immediately.
    *
    * @param controlFlags A set of flags to indicate whether to include certain
    * fields. Composed of the <code>FLAG_xxx</code> values or'd together.
    */
   private void processSystemOnlyFields(PSContentEditorSystemDef sysDef,
      int controlFlags)
   {
      PSFieldSet fs = sysDef.getFieldSet();
      if (null == fs)
         return;

      PSUIDefinition uiDef = sysDef.getUIDefinition();
      PSField[] fields = fs.getAllFields(true);
      for (int i=0; i < fields.length; i++)
      {
         // try to get choices from the display mapping for this field if found.
         String submitName = fields[i].getSubmitName();
         if (!isFieldIncluded(submitName))
            continue;
         
         PSDisplayMapping dispMapping = uiDef != null ?
            uiDef.getMapping(submitName) : null;
         PSChoices choices = dispMapping != null ?
            dispMapping.getUISet().getChoices() : null;
            
         String displayName = "";
         if (dispMapping != null && dispMapping.getUISet().getLabel() != null)
            displayName = dispMapping.getUISet().getLabel().getText();

         String mnemonic = dispMapping != null ?
            dispMapping.getUISet().getAccessKey() : "";

         populateMap(fields[i], submitName, displayName, mnemonic, 
            INVALID_CONTENT_TYPE, choices, controlFlags);
      }
   }

   /**
    * Recurse through each mapper within a mapping to populate 
    * <code>m_systemMap</code>, <code>m_sharedMap</code> and 
    * <code>m_localMap</code> with the corresponding content editor fields. 
    * See the documentation for the respective maps.
    *
    * @param dispMapper assumed to be <code>null</code>.
    * @param mapper assumed to be <code>null</code>.
    * @param contentType The unique (text) identifier of the content editor type
    *    that owns this display mapping. Assumed not <code>null</code> or empty.
    * @param controlFlags a set of flags to indicate whether to include certain
    *    fields. Composed of the <code>FLAG_xxx</code> values or'd together.
    */
   private void recurseMapping(PSDisplayMapper dispMapper, 
      PSContentEditorMapper mapper, String contentType, int controlFlags)
   {
      PSFieldSet fieldSet = mapper.getFieldSet(dispMapper.getFieldSetRef());
      /* 
       * We don't need to check if FTS is enabled because this flag is true
       * by default. This handles whole editors and children.
       */
      if (!fieldSet.isUserSearchable())
         return;
         
      PSContentEditorSystemDef sysDef = PSServer.getContentEditorSystemDef();
      
      Iterator mappings = dispMapper.iterator();
      PSDisplayMapping mapping = null;
      PSUISet uiSet = null;
      if (mappings != null)
      {
         while (mappings.hasNext())
         {
            mapping = (PSDisplayMapping) mappings.next();
            PSDisplayMapper displayMapper = mapping.getDisplayMapper();
            if (displayMapper != null)
            {
               recurseMapping(displayMapper, mapper, contentType, controlFlags);
            }

            String fieldName = mapping.getFieldRef();
            
            Object o = fieldSet.get(fieldName);

            // check for sdmp fields in child fieldset
            if (o == null)
            {
               o = fieldSet.getChildField(mapping.getFieldRef(),
                  fieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD);
            }

            if (o != null && o instanceof PSField)
            {
               PSField field = (PSField)o;
               uiSet = mapping.getUISet();
               
               // Optimize so we only process system and shared fields once
               if (alreadyhaveSystemOrSharedInfo(field, mapping.getFieldRef()))
                  continue;

               // get orignal field if system, defer shared fields till later
               if (field.isSystemField() && sysDef != null)
               {
                  PSField sysField = sysDef.getFieldSet().findFieldByName(
                     fieldName);
                  if (sysField != null)
                     field = sysField;
                  
                  PSDisplayMapping sysMapping = sysDef.getUIDefinition()
                  .getMapping(fieldName);
                  if (sysMapping != null)
                     mapping = sysMapping;
               }
               else if (field.isSharedField())
               {
                  String fsName = fieldSet.getName();
                  if (fieldSet.get(fieldName) == null)
                  {
                     Object fso = fieldSet.getChildsFieldSet(fieldName,
                        PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD);
                     if (!(fso instanceof PSFieldSet))
                        continue;
                     fsName = ((PSFieldSet) fso).getName();
                  }
                  Set<String> nameSet = m_sharedFields.get(fsName);
                  if (nameSet == null)
                     nameSet = new HashSet<>();
                  nameSet.add(fieldName);
                  m_sharedFields.put(fsName, nameSet);
                  continue;
               }
               
               processField(contentType, controlFlags, uiSet, fieldName, field);
            }
         }
      }
   }

   /**
    * Processes the supplied field and after filtering by the control flags adds
    * it to the correct map.
    * 
    * @param contentType The content type, ignored for system and shared fields.
    * @param controlFlags The flags to use to restrict which fields are added.
    * @param uiSet The ui set for the field, may be <code>null</code>.
    * @param fieldName The name of the field, assumed not <code>null</code> or 
    * empty.
    * @param field The field object, assumed not <code>null</code>.
    */
   private void processField(String contentType, int controlFlags, 
      PSUISet uiSet, String fieldName, PSField field)
   {
      if (!isFieldIncluded(fieldName))
         return;
      
      PSDisplayText label = null;
      String text = null;               
      PSChoices choices = null;
      String mnemonic = "";               
      if (uiSet != null)
      {
         label = uiSet.getLabel();
         if (label != null)
         {
            text = label.getText();
            if (text == null)
               text = "";
         }
         else
            text = "";
            
         choices = uiSet.getChoices();
         mnemonic = uiSet.getAccessKey();
      }
      else
         text = "";
         
      if (text.endsWith(":"))
         text = text.substring(0, text.length()-1);
         
      populateMap(field, fieldName, text, mnemonic, 
         contentType, choices, controlFlags);
   }

   /**
    * We only want to process a given system field once. Extracting
    * choices is expensive and processing once per content type is wasteful of
    * bandwidth as well in the returned document.
    * 
    * @param field the field, assumed not <code>null</code>
    * @param internalName the internal name of the field used to store data in
    *           the maps, assumed not <code>null</code> or empty
    * @return <code>true</code> if the field is a system and
    *         has already been recorded.
    */
   private boolean alreadyhaveSystemOrSharedInfo(PSField field,
         String internalName)
   {
      if (field.isSystemField())
      {
         return m_systemMap.get(internalName) != null;
      }
      
      return false;
   }

   /**
    * Populates system, shared and local fields in their respective maps. See
    * {@link #recurseMapping(PSDisplayMapper, PSContentEditorMapper, String, 
    * int)}. If the includeAll flag is <code>false</code>, then all fields
    * whose user searchable property is <code>true</code> will be returned. If
    * <code>true</code>, then additional fields whose searchable property is
    * <code>false</code> but are not read only will also be included. Binary
    * fields are never included.
    * 
    * @param field {@link com.percussion.design.objectstore.PSField}, assumed
    * to be not <code>null</code>.
    * @param internalName internal name of the field, assumed to be not <code>
    *    null</code>.
    * @param displayName display name of the field, assumed to be not <code>
    *    null</code>.
    * @param mnemonic the menmonic for the display name, assumed not
    * <code>null</code>.
    * @param id content id of the field. Supply "-1" if there isn't one (
    * example system and shared fields).
    * @param choices An optional set of choices to use with the field. May be
    * <code>null</code>. If <code>null</code> and the supplied
    * <code>field</code> supplies its own choices, those will be used instead.
    * @param controlFlags A set of flags to indicate whether to include certain
    * fields. Composed of the <code>FLAG_xxx</code> values or'd together.
    */
   private void populateMap(PSField field, String internalName, String
      displayName, String mnemonic, String id, PSChoices choices, 
      int controlFlags)
   {
      boolean excludeHidden = (controlFlags & FLAG_INCLUDE_HIDDEN) == 0;
      boolean excludeResultOnly = (controlFlags & FLAG_INCLUDE_RESULTONLY) == 0;
      boolean userSearch = (controlFlags & FLAG_USER_SEARCH) != 0;
      PSSearchConfig sc = PSServer.getServerConfiguration().getSearchConfig();
      PSSearchProperties sp = field.getSearchProperties();
      // special handling for state name search
      boolean isReadOnly = (field.isReadOnly() && !field.getSubmitName().equals(
         IPSHtmlParameters.SYS_STATE_NAME));
      if ((excludeHidden && !(isReadOnly || sp.isUserSearchable()))
            || (excludeResultOnly && isReadOnly)
            || (userSearch && !sp.isUserCustomizable())
            || (isReadOnly && !sp.isUserSearchable())
            || (sc.isFtsEnabled() && sp.isEnableTransformation())
            || (field.isSystemInternal()))
      {
         return;
      }

      if (displayName.length() == 0)
         displayName = field.getSearchProperties().getDefaultSearchLabel();
      String dtype = field.getDataType();
      //it's possible to get an empty one
      if (dtype.length() == 0)
         dtype = PSField.DT_TEXT;
      if (choices == null)
         choices = field.getChoices();

      String lang =
       (String)m_request.getUserSession().getSessionObject(
        PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
      if (lang == null)
         lang = PSI18nUtils.DEFAULT_LANG;

      Map map = null;
      if (field.isSystemField())
      {
         map = m_systemMap;
         choices = localizeChoices(choices, lang, "psx.ce.system." +
            field.getSubmitName() + "@");
         id = ANY;
      }
      else if (field.isSharedField())
      {
         map = m_sharedMap;
         choices = localizeChoices(choices, lang, "psx.ce.shared." +
            field.getSubmitName() + "@");
         id = ANY;
      }
      else if (field.isLocalField())
      {
         map = m_localMap;
         choices = localizeChoices(choices, lang, "psx.ce.local." + id + "." +
            field.getSubmitName() + "@");
      }

      if (map == null)
      {
         // we've added an unsupported field type?
         throw new RuntimeException("Unsupported field type: " +
            field.getType());
      }

      FieldObject fob = new FieldObject(dtype, displayName, mnemonic, id,
            field.isReadOnly(), choices);

      addToMap(map, fob, internalName);
   }

   /**
    * Localizes the supplied choices using the specified language.  If the
    * <code>choices</code> object is <code>null</code> or does not define any
    * local choice entries, the supplied object is returned unmodified.
    * Otherwise a copy is returned with the display text of the local choice
    * entries translated.
    *
    * @param choices The choices, assumed not <code>null</code>.
    * @param lang The language string to use for the translation, assumed not
    * <code>null</code> or empty.
    * @param keyBase The base of the key to use when getting the translation
    * from the bundle.  Specifies everything up through the "@", and the label
    * of each local entry is appended onto this to build the full key used to
    * retrieve the translation.  Assumed not <code>null</code> or empty.
    *
    * @return The localized choices, never <code>null</code>.
    */
   private PSChoices localizeChoices(PSChoices choices, String lang,
      String keyBase)
   {
      //suppress eclipse warning
      if (null == lang);
      
      if (choices == null || choices.getType() != PSChoices.TYPE_LOCAL)
         return choices;

      // make a copy so we don't modify the actual choices
      PSChoices newChoices = new PSChoices(new PSCollection(
         choices.getLocal()));
      newChoices.copyFrom(choices);

      // build list of translated entries
      List<PSEntry> newChoiceList = new ArrayList<>();
      Iterator oldChoices = choices.getLocal();
      while (oldChoices.hasNext())
      {
         PSEntry entry = (PSEntry)oldChoices.next();
         PSDisplayText newText = new PSDisplayText(PSI18nUtils.getString(
            keyBase + entry.getLabel().getText()));
         PSEntry newEntry = new PSEntry(entry.getValue(), newText);
         newChoiceList.add(newEntry);
      }

      // set the new entries on the copied choices
      newChoices.setLocal(new PSCollection(newChoiceList.iterator()));

      return newChoices;
   }

   /**
    * Looks for entries in map with the supplied key. If found, expected to
    * be a Collection. If found, f is added to this collection. If not found,
    * a new collection is created and added to the map and f is added to this
    * new collection.
    *
    * @param map Assumed not <code>null</code>.
    *
    * @param f Assumed not <code>null</code>.
    *
    * @param key Assumed not <code>null</code> or empty. Lowercased before
    *    performing search on map.
    */
   @SuppressWarnings("unchecked")
   private void addToMap(Map map, FieldObject f, String key)
   {
      Collection c = (Collection) map.get(key);
      if (null == c)
      {
         c = new ArrayList();
         map.put(key, c);
      }

      c.add(f);
   }

   /**
    * Converts the data in the shared, system and local maps to an Xml element
    * conforming to a DTD in {@link com.percussion.cms.objectstore.IPSCataloger
    * #getCEFieldXml(int)}
    * @param controlFlags The control flags supplied to the 
    * {@link #getCEFieldXml(int)} request.
    *
    * @return element containing content editor fields, never <code>null</code>.
    */
   private Element toXml(int controlFlags)
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root =  doc.createElement(ROOT_ELEM);
      if (!m_systemMap.isEmpty())
         addElement(SYSTEM, root, m_systemMap, doc, controlFlags);
      if (!m_sharedMap.isEmpty())
         addElement(SHARED, root, m_sharedMap, doc, controlFlags);
      if (!m_localMap.isEmpty())
         addElement(LOCAL, root, m_localMap, doc, controlFlags);
      doc.appendChild(root);
      
      return root;
   }

   /**
    * Convenience method to add shared and local elements to the root document.
    *
    * @param type content type - shared, system or local, assumed to be not
    * <code>null</code>.
    *
    * @param root root element, <code>ROOT_ELEM</code>, assumed to be not
    * <code>null</code>.
    *
    * @param map shared or local map, assumed to be not <code>null</code> or
    * empty.
    *
    * @param doc parent document, assumed to be not <code>null</code>.
    * 
    * @param controlFlags The control flags supplied to 
    * {@link #getCEFieldXml(int)}
    */
   private void addElement(String type, Element root, Map map, Document doc, 
      int controlFlags)
   {
      Element elem = doc.createElement(type);
      Iterator itr = map.keySet().iterator();
      String key = null;
      FieldObject value = null;
      Element fieldElem = null;
      String id = null;
      String dataType = null;
      String displayName = null;
      while (itr.hasNext())
      {
         Element fieldInstancesEl = doc.createElement(SEARCH_FIELD);
         key = (String)itr.next();
         fieldInstancesEl.setAttribute(INTERNALNAME, key);

         Iterator fobs = ((Collection) map.get(key)).iterator();
         boolean firstPass = false;

         while (fobs.hasNext())
         {
            fieldElem = doc.createElement(FIELD);

            value = (FieldObject) fobs.next();
            if (!firstPass)
            {
               firstPass = true;
               if (value.isReadOnly())
                  fieldInstancesEl.setAttribute(RESULT_ONLY, RESULT_ONLY_VALUE);
            }

            id = value.getId();
            if (!id.equals(INVALID_CONTENT_TYPE))
               fieldElem.setAttribute(CONTENTID, id);

            dataType = value.getDataType();
            fieldElem.setAttribute(TYPE, dataType);

            displayName = value.getDisplayName();
            if (displayName.length() > 0)
               fieldElem.setAttribute(DISPLAYNAME, displayName);
            
            String mnemonic = value.getMnemonic();   
            if (StringUtils.isNotEmpty(mnemonic))
            {
               fieldElem.setAttribute(MNEMONIC, mnemonic);
            }
            
            fieldInstancesEl.appendChild(fieldElem);

            // add choices if we have them
            try
            {
               PSChoices choices = value.getChoices();
               if (choices != null)
               {
                  if ((controlFlags & FLAG_EXCLUDE_CHOICES) != 0)
                  {
                     PSDisplayChoices dispChoices = new PSDisplayChoices(null, 
                        choices.getChoiceFilter());
                     fieldElem.appendChild(dispChoices.toXml(doc));
                  }
                  else
                  {
                     PSChoiceBuilder.addChoiceElement(doc, fieldElem, choices,
                        new PSExecutionData(null, null, m_request), false, true,
                        false);
                  }
               }

            }
            catch (PSDataExtractionException e)
            {
               /*Not throwing exceptions from here, because the side effect
                 is such that if choices in one CE are broken then users
                 can not start content explorer as well as they can not open
                 ContentViews in the Workbench.
                 Debug Log seems like the only option here.
                 Format a more descriptive error message:
                 "Invalid CE field choices. Content type: {0}, field type: {1},
                 field name: {2}, lookup Url: {3}."
               */
               Object[] args = new Object[4];
               args[0] = value.getId();
               args[1] = type;
               args[2] = key;
               args[3] = null;

               PSChoices choices = value.getChoices();

               if (choices!=null && choices.getLookup()!=null)
                  args[3] = choices.getLookup().toString();

               String msg = PSErrorManager.createMessage(
                  IPSObjectStoreErrors.INVALID_CE_FIELD_CHOICES_ERROR, args);

               //log it
               PSServerLogHandler.logException(msg, e);
            }

         }

         elem.appendChild(fieldInstancesEl);
      }

      root.appendChild(elem);
   }
   
   /**
    * Determine if the specified field is to be included based on the set of 
    * field names supplied when the catalog was requested.
    * 
    * @param fieldName The field name to check, assumed not <code>null</code> or 
    * empty.
    * 
    * @return <code>true</code> if the field is included, <code>false</code> if
    * not.
    */
   private boolean isFieldIncluded(String fieldName)
   {
      return m_fieldNames == null || m_fieldNames.isEmpty() || 
         m_fieldNames.contains(fieldName);
   }
   
   /**
    * Resets any state in preparation for a new call to catalog fields.
    */
   private void initFieldCatalog()
   {
      m_fieldNames = null;
      m_localMap.clear();
      m_sharedFields.clear();
      m_sharedMap.clear();
      m_systemMap.clear();
   }

   /**
    * Convenience class abstracting the display name, datatype and id of a
    * content field. It's being used as a value in <code>m_sharedMap</code>
    * <code>m_localMap</code> with field's internal name as the key.
    */
   private class FieldObject
   {
      /**
       * Constructs the object supplying all possible parameters.
       *
       * @param dataType the data type of the field, assumed to be not <code>
       *    null</code>.
       * @param dispName the display name, assumed to be not <code>
       *    null</code>.
       * @param mnemonic the menmonic assigend to the display name, assumed
       *    not <code>null</code>.
       * @param id the id of the field, assumed to be not <code>
       *    null</code>.
       * @param readOnly <code>true</code> if the field is read only, 
       *    <code>false</code> otherwise.
       * @param choices The choices for the field, may be <code>null</code>.
       */
      FieldObject(String dataType, String dispName, String mnemonic, String id,
            boolean readOnly, PSChoices choices)
      {
         m_dataType = dataType;
         m_displayName = dispName;
         m_mnemonic = mnemonic;
         m_contentId = id;
         m_readOnly = readOnly;
         m_choices = choices;
      }

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
       * 
       * @param readOnly <code>true</code> if the field is read only, 
       * <code>false</code> otherwise.
       */
      FieldObject(String dataType, String dispName, String id,
            boolean readOnly)
      {
         this(dataType, dispName, "", id, readOnly, null);
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
      FieldObject(String dataType, int id)
      {
         this (dataType, "", Integer.toString(id), false);
      }

      /**
       * Get the data type.
       *
       * @return never <code>null</code>.
       */
      public String getDataType()
      {
         return m_dataType;
      }

      /**
       * Get the id.
       *
       * @return never <code>null</code>.
       */
      public String getId()
      {
         return m_contentId;
      }

      /**
       * Get the display name.
       *
       * @return never <code>null</code>.
       */
      public String getDisplayName()
      {
         return m_displayName;
      }

      /**
       * Get the mnemonic.
       *
       * @return the mnemonic, never <code>null</code>, may be empty.
       */
      public String getMnemonic()
      {
         return m_mnemonic;
      }

      /**
       * Indicates whether the field described by this summary is read-only,
       * meaning it can't be used in a search field, only as a result.
       * 
       * @return <code>true</code> if it is read only, <code>false</code> if not
       */
      public boolean isReadOnly()
      {
         return m_readOnly;
      }

      /**
       * Get optional choices for the field's value.
       *
       * @return The choices, may be <code>null</code>.
       */
      public PSChoices getChoices()
      {
         return m_choices;
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

      /**
       * Mnemonic of the field, initialized in one of the ctors, not 
       * <code>null</code>, may be empty.
       */
      private String m_mnemonic;

      /**
       * See {@link #isReadOnly()}. Set in ctor, then never modified.
       */
      private boolean m_readOnly;

      /**
       * Optional choices for the field's value, may be <code>null</code>.
       */
      private PSChoices m_choices;
   }

   /**
    * The request supplied to the ctor.  Never <code>null</code> or modified
    * directly after that.
    */
   private PSRequest m_request = null;

   /**
    * System map for system content editor fields. The key is the internal name
    * of the field and the value is a Collection containing <code>FieldObject
    * </code> types, abstracting display name, data type and content type id.
    * Never <code>null</code>.
    */
   private Map m_systemMap = new HashMap();

   /**
    * Shared map for shared content editor fields. The key is the internal name
    * of the field and the value is a List containing <code>FieldObject</code>
    * types, abstracting display name, data type and content type id. Since
    * any given name could occur in multiple editors, we provide all the data
    * so the client can display the results in different ways. Never
    * <code>null</code>.
    */
   private Map m_sharedMap = new HashMap();

   /**
    * Local map for local fields. See <code>m_sharedMap</code> for details.
    * Never <code>null</code>.
    */
   private Map m_localMap = new HashMap();
   
   /**
    * Map of shared fields to process in order to use the source field rather
    * than the overriden field defintion.  Key is the fieldset name as a 
    * <code>String</code>, value is a <code>Set</code> of field names as 
    * <code>String</code> objects.
    */
   private Map<String, Set<String>> m_sharedFields = 
      new HashMap<>();
   
   /**
    * Set of names to include specified when the catalog is requested.   May be
    * <code>null</code> or empty to allow all fields.
    */
   private Set<String> m_fieldNames;

   /**
    * Used to represent that there is not content type associated with a field.
    */
   private static final String INVALID_CONTENT_TYPE = "-1";

   // node names
   private static final String ROOT_ELEM = "ContentEditorFieldCatalogResults";
   private static final String SYSTEM = "System";
   private static final String SHARED = "Shared";
   private static final String LOCAL = "Local";
   private static final String FIELD = "Field";
   private static final String SEARCH_FIELD = "SearchField";
   //attribute names for "Field" node
   private static final String CONTENTID = "contentTypeId";
   private static final String DISPLAYNAME = "displayName";
   private static final String MNEMONIC = "mnemonic";
   private static final String TYPE = "datatype";
   private static final String INTERNALNAME = "name";
   private static final String RESULT_ONLY = "resultOnly";
   private static final String ANY = "anyContentType";
   private static final String RESULT_ONLY_VALUE = "yes";
}
