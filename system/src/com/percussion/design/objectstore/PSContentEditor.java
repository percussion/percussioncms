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
package com.percussion.design.objectstore;

import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.data.IPSInternalRequestHandler;
import com.percussion.data.PSExecutionData;
import com.percussion.design.objectstore.legacy.IPSComponentUpdater;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * Implements the PSXContentEditorLocalDef DTD defined in
 * ContentEditorLocalDef.dtd.
 */
public class PSContentEditor extends PSDataSet
{
   /**
    * Creates a new content editor for the provided name, content type and
    * workflow id. Related content is disabled.
    * 
    * @param name the dataSet name, not <code>null</code> or empty.
    * @param contentType the content type this editor will will work with.
    * @param workflowId the workflow id used for items of this content editor.
    */
   public PSContentEditor(String name, long contentType, int workflowId)
   {
      setName(name);

      m_contentType = contentType;
      m_workflowId = workflowId;
      m_enableRelatedContent = false;
   }

   /**
    * Creates a new content editor for the provided name, content type and
    * workflow id. Related content is enabled as specified.
    * 
    * @param name the dataSet name, not <code>null</code> or empty.
    * @param contentType the content type this editor will will work with.
    * @param workflowId the workflow id used for items of this content editor.
    * @param enableRelatedContent this flag defines whether or not related
    *           content is supported by this content editor.
    */
   public PSContentEditor(String name, long contentType, int workflowId,
         boolean enableRelatedContent)
   {
      setName(name);

      m_contentType = contentType;
      m_workflowId = workflowId;
      m_enableRelatedContent = enableRelatedContent;
   }

   /**
    * Creates a new content editor for the provided name, content type, workflow
    * id and stylesheet set. Related content is enabled as specified.
    * 
    * @param name the dataSet name, not <code>null</code> or empty.
    * @param contentType the content type this editor will will work with.
    * @param workflowId the workflow id used for items of this content editor.
    * @param enableRelatedContent this flag defines whether or not related
    *           content is supported by this content editor.
    * @param stylesheetSet the stylesheet set, not <code>null</code>.
    */
   public PSContentEditor(String name, long contentType, int workflowId,
         boolean enableRelatedContent, PSCommandHandlerStylesheets stylesheetSet)
   {
      this(name, contentType, workflowId, enableRelatedContent);

      setStylesheetSet(stylesheetSet);
   }

   /**
    * Creates a new content editor for the provided name, content type, workflow
    * id and stylesheet set. Related content is disabled.
    * 
    * @param name the dataSet name, not <code>null</code> or empty.
    * @param contentType the content type this editor will will work with.
    * @param workflowId the workflow id used for items of this content editor.
    * @param stylesheetSet the stylesheet set, not <code>null</code>.
    */
   public PSContentEditor(String name, long contentType, int workflowId,
         PSCommandHandlerStylesheets stylesheetSet)
   {
      this(name, contentType, workflowId);

      setStylesheetSet(stylesheetSet);
   }

   /**
    * Construct a Java object from its XML representation.
    * 
    * @param sourceNode the XML element node to construct this object from, not
    *           <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object, not
    *           <code>null</code>.
    * @param parentComponents the parent objects of this object, not
    *           <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of the
    *            appropriate type
    */
   public PSContentEditor(Element sourceNode, IPSDocument parentDoc,
         ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      this(sourceNode, parentDoc, parentComponents,true);
   }

   /**
    * Construct a Java object from its XML representation.
    * 
    * @param sourceNode the XML element node to construct this object from, not
    * <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object, not
    * <code>null</code>.
    * @param parentComponents the parent objects of this object, not
    * <code>null</code>.
    * @param runUpdater a flag to indicate whether to run the updaters upon
    * creating the contenteditor or not.
    * @throws PSUnknownNodeTypeException if the XML element node is not of the
    * appropriate type
    */
   public PSContentEditor(Element sourceNode, IPSDocument parentDoc,
         ArrayList parentComponents, boolean runUpdater)
      throws PSUnknownNodeTypeException
   {
      /*
       * This constructor has been created as kind of hack to resolve the
       * content type workflow associations being updated before the content
       * editor gets saved. Content type workflow associations are saved to
       * database during the content type save and the content editor gets
       * updated with the data from the table during the content editor
       * creation. As the workbench call to save the content type first creates
       * the content editor using this constructor the new workflow associations
       * that are coming from the wb are getting updated with the associations
       * from the database table before the content editor's save.
       */
      fromXml(sourceNode, parentDoc, parentComponents);

      // check for an updater
      IPSComponentUpdater updater = getComponentUpdater(this.getClass());

      /*
       * If the updater is not null then try to update the editor.
       */
      if (updater != null && runUpdater)
      {
         updater.updateComponent(this);
      }
   }

   /**
    * Needed for serialization.
    */
   protected PSContentEditor()
   {
   }

   // see interface for description
   public Object clone()
   {
      PSContentEditor copy = (PSContentEditor) super.clone();
      if (m_applicationFlow != null)
         copy.m_applicationFlow = (PSApplicationFlow) m_applicationFlow.clone();

      // TODO: switch m_customActionGroups to PSCollection and clone

      copy.m_inputTranslations = (PSInputTranslations) m_inputTranslations
            .clone(); // TODO
      copy.m_outputTranslations = (PSOutputTranslations) m_outputTranslations
            .clone(); // TODO

      copy.m_sectionLinkList = new PSCollection(PSUrlRequest.class);
      for (int i = 0; i < m_sectionLinkList.size(); i++)
      {
         PSUrlRequest request = (PSUrlRequest) m_sectionLinkList.elementAt(i);
         copy.m_sectionLinkList.add(i, request.clone());
      }

      copy.m_stylesheetSet = (PSCommandHandlerStylesheets) m_stylesheetSet
            .clone(); // TODO
      copy.m_validationRules = (PSValidationRules) m_validationRules.clone(); // TODO

      if (m_workflowInfo != null)
         copy.m_workflowInfo = (PSWorkflowInfo) m_workflowInfo.clone();

      return copy;
   }

   /**
    * Overwritten to rethrow IllegalArgumentException.
    * 
    * @param name the content editor name, not <code>null</code> or empty.
    */
   @Override
   public void setName(String name)
   {
      try
      {
         super.setName(name);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }
   }

   /**
    * Retrieves the field control ref for the specified field in this content
    * editor.
    * 
    * @param fieldname the name of the field. Cannot be <code>null</code> or
    *           empty.
    * @return the control ref or <code>null</code> if not found.
    */
   public PSControlRef getFieldControl(String fieldname)
   {
      if (StringUtils.isBlank(fieldname))
         throw new IllegalArgumentException(
               "fieldname cannot be null or empty.");
      
      PSUISet uiset = getFieldUiSet(fieldname);
      return uiset == null ? null : uiset.getControl();
   }

   /**
    * Retrieves the field UI Set for the specified field in this content
    * editor.
    * 
    * @param fieldname the name of the field. Cannot be <code>null</code> or
    *           empty.
    * @return the UI set or <code>null</code> if cannot find the field.
    */
   public PSUISet getFieldUiSet(String fieldname)
   {
      if (StringUtils.isBlank(fieldname))
         throw new IllegalArgumentException(
               "fieldname cannot be null or empty.");
      
      PSContentEditorPipe pipe = (PSContentEditorPipe) getPipe();
      PSContentEditorMapper mapper = pipe.getMapper();
      PSDisplayMapper dispMapper = mapper.getUIDefinition().getDisplayMapper();
      PSDisplayMapping mapping = dispMapper.getMapping(fieldname);
      if (mapping == null)
         return null;
      
      return mapping.getUISet();
   }
   
   /**
    * Get the content type ID.
    * 
    * @return the content type ID.
    */
   public long getContentType()
   {
      return m_contentType;
   }

   /**
    * Set a new content type ID.
    * 
    * @param contentType the new content type ID.
    */
   public void setContentType(long contentType)
   {
      m_contentType = contentType;
   }

   /**
    * Get the workflow ID.
    * 
    * @return the workflow ID.
    */
   public int getWorkflowId()
   {
      return m_workflowId;
   }

   /**
    * Set a new workflow ID.
    * 
    * @param workflowId the new workflow ID.
    */
   public void setWorkflowId(int workflowId)
   {
      m_workflowId = workflowId;
   }

   /**
    * Enables or disables related content for this content editor.
    * 
    * @param enable <code>true</code> to enable, <code>false</code> to
    *           disable.
    */
   public void enableRelatedContent(boolean enable)
   {
      m_enableRelatedContent = enable;
   }

   /**
    * Returns the status whether related content is enabled or not.
    * 
    * @return <code>true</code> if enabled, <code>false</code> if disabled.
    */
   public boolean isRelatedContentEnabled()
   {
      return m_enableRelatedContent;
   }

   /**
    * Sets produces resource type behavior of the content editor.
    * 
    * @param flag <code>true</code> to set the behavior to true.
    */
   public void producesResource(boolean flag)
   {
      m_producesResource = flag;
   }

   /**
    * Returns the status whether the content editor produces resource type item
    * or not.
    * 
    * @return <code>true</code> if it does, otherwise <code>false</code>.
    */
   public boolean doesProduceResource()
   {
      return m_producesResource;
   }

   /**
    * Before 5.0, there was only 1 type of content, namely item. In 5.0, we
    * introduced a new type, folder. We genericized the editors to work with the
    * concept of different content (or object) types. These types are defined in
    * the OBJECTS table. The type is the primary key of that table.
    * 
    * @return One of the <code>PSCmsObject.TYPE_XXX</code> values. The default
    *         value is PSCmsObject.TYPE_ITEM.
    */
   public int getObjectType()
   {
      return m_objectType;
   }

   /**
    * See {@link #getObjectType()} for a full description.
    * 
    * @param type Must be one of the OBJECT_TYPE_xxx values.
    */
   public void setObjectType(int type)
   {
      if (!PSCmsObject.isValidType(type))
         throw new IllegalArgumentException("Invalid object type supplied.");
      m_objectType = type;
   }

   /**
    * @return the PSWorkflowInfo object that holds the dynamic workflow rules,
    *         or <code>null</code>, and if <code>null</code> it is treated
    *         as all workflows allowed by community visibility.
    */
   public PSWorkflowInfo getWorkflowInfo()
   {
      return m_workflowInfo;
   }

   /**
    * Sets the workflow info field.
    * 
    * @param workflowInfo may be <code>null</code>
    */
   public void setWorkflowInfo(PSWorkflowInfo workflowInfo)
   {
      m_workflowInfo = workflowInfo;
   }

   /**
    * Get the section link list.
    * 
    * @return the section link list, never <code>null</code>, might be empty.
    *         An iterator of PSUrlRequest objects.
    */
   public Iterator getSectionLinkList()
   {
      return m_sectionLinkList.iterator();
   }

   /**
    * Set a new section link list.
    * 
    * @param sectionList a collection of PSUrlRequest objects, might be
    *           <code>null</code> or empty.
    */
   public void setSectionLinkList(PSCollection sectionList)
   {
      if (sectionList != null
            && !sectionList.getMemberClassName().equals(
                  m_sectionLinkList.getMemberClassName()))
         throw new IllegalArgumentException("PSUrlRequest collection expected");

      m_sectionLinkList.clear();
      if (sectionList != null)
         m_sectionLinkList.addAll(sectionList);
   }

   /**
    * Get the stylesheet set.
    * 
    * @return the current stylesheet set, never <code>null</code>, might be
    *         empty.
    */
   public PSCommandHandlerStylesheets getStylesheetSet()
   {
      return m_stylesheetSet;
   }

   /**
    * Set a new stylesheet set.
    * 
    * @param stylesheetSet the new stylesheet set, not <code>null</code>.
    */
   public void setStylesheetSet(PSCommandHandlerStylesheets stylesheetSet)
   {
      if (stylesheetSet == null)
         throw new IllegalArgumentException("stylesheetSet cannot be null");

      m_stylesheetSet = stylesheetSet;
   }

   /**
    * Get the application flow.
    * 
    * @return the application flow, might be <code>null</code>.
    */
   public PSApplicationFlow getApplicationFlow()
   {
      return m_applicationFlow;
   }

   /**
    * Set a new application flow.
    * 
    * @param applicationFlow the new application flow, might be
    *           <code>null</code>.
    */
   public void setApplicationFlow(PSApplicationFlow applicationFlow)
   {
      m_applicationFlow = applicationFlow;
   }

   /**
    * Get the group validation rules.
    * 
    * @return the group validation rules (a collection of PSConditionalExit
    *         objects), never <code>null</code>, might be empty.
    */
   public Iterator getValidationRules()
   {
      return m_validationRules.iterator();
   }

   /**
    * Get the maximal number of errors that can occur until itme validation is
    * stopped.
    * 
    * @return the number of errors that can occur to stop item validation,
    *         always > 0.
    */
   public int getMaxErrorsToStopValidation()
   {
      return m_validationRules.getMaxErrorsToStop();
   }

   /**
    * Set new group validation rules.
    * 
    * @param groupValidations the new group validation rules, might be
    *           <code>null</code>.
    */
   public void setValidationRules(PSValidationRules groupValidations)
   {
      m_validationRules.clear();
      if (groupValidations != null)
      {
         m_validationRules.addAll(groupValidations);
         m_validationRules.setMaxErrorsToStop(groupValidations
               .getMaxErrorsToStop());
      }
   }

   /**
    * Get the group input translations.
    * 
    * @return the group input translations (a collection of PSConditionalExit
    *         objects), never <code>null</code> might be empty.
    */
   public Iterator getInputTranslations()
   {
      return m_inputTranslations.iterator();
   }

   /**
    * Set new group input translations.
    * 
    * @param groupInputTranslations the new input group translations, might be
    *           <code>null</code>.
    */
   public void setInputTranslation(PSInputTranslations groupInputTranslations)
   {
      m_inputTranslations.clear();
      if (groupInputTranslations != null)
         m_inputTranslations.addAll(groupInputTranslations);
   }

   /**
    * Get the group output translations.
    * 
    * @return the group output translations (a collection of PSConditionalExit
    *         objects), never <code>null</code> might be empty.
    */
   public Iterator getOutputTranslations()
   {
      return m_outputTranslations.iterator();
   }

   /**
    * Set the group output translations.
    * 
    * @param groupOutputTranslations the new group output translations, might be
    *           <code>null</code>.
    */
   public void setOutputTranslation(PSOutputTranslations groupOutputTranslations)
   {
      m_outputTranslations.clear();
      if (groupOutputTranslations != null)
         m_outputTranslations.addAll(groupOutputTranslations);
   }

   /**
    * Get all custom action groups currently defined. A custom action can be
    * used by the designer to add and remove default buttons supplied by the
    * system in various locations.
    * 
    * @return the current custom action group, might be <code>null</code>.
    * 
    * @see #getCustomActionGroup(int, int, String)
    * @see com.percussion.design.objectstore.PSLocation
    */
   public Iterator getCustomActionGroups()
   {
      return m_customActionGroups.iterator();
   }

   /**
    * Scans all custom actions currently defined and returns a set of them that
    * match the supplied parameters. See {@link 
    * com.percussion.design.objectstore.PSLocation#hasCustomActions(int, int,
    * String) PSLocation.hasCustomActions} for a description of the params.
    * 
    * @return A valid action that matches the supplied criteria, or <code>null
    *    </code>
    *         if no match was found.
    */
   public PSCustomActionGroup getCustomActionGroup(int pageType,
         int pageLocation, String fieldRef)
   {
      PSCustomActionGroup matchGroup = null;
      Iterator groups = m_customActionGroups.iterator();
      while (groups.hasNext())
      {
         PSCustomActionGroup group = (PSCustomActionGroup) groups.next();
         if (group.getLocation().hasCustomActions(pageType, pageLocation,
               fieldRef))
         {
            matchGroup = group;
            break;
         }
      }
      return matchGroup;
   }

   /**
    * Adds a new custom action group to the set of existing actions.
    * 
    * @param customActionGroup the new custom action group, Never <code>null
    *    </code>.
    */
   public void addCustomActionGroup(PSCustomActionGroup customActionGroup)
   {
      if (null == customActionGroup)
         throw new IllegalArgumentException("action cannot be null");
      m_customActionGroups.add(customActionGroup);
   }

   /**
    * Remove a custom action group from the set of existing groups.
    * 
    * @param customActionGroup an existing custom action group, If <code>null
    *    </code>,
    *           nothing is done.
    */
   public void removeCustomActionGroup(PSCustomActionGroup customActionGroup)
   {
      if (null != customActionGroup)
         m_customActionGroups.remove(customActionGroup);
   }

   /**
    * Removes all custom action groups from this editor.
    */
   public void clearCustomActionGroups()
   {
      m_customActionGroups.clear();
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    * 
    * @param c a valid PSContentEditor, not <code>null</code>.
    */
   public void copyFrom(PSContentEditor c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      setApplicationFlow(c.getApplicationFlow());
      setContentType(c.getContentType());
      setValidationRules(c.m_validationRules);
      setInputTranslation(c.m_inputTranslations);
      setOutputTranslation(c.m_outputTranslations);
      setSectionLinkList(c.m_sectionLinkList);
      setStylesheetSet(c.getStylesheetSet());
      setWorkflowId(c.getWorkflowId());
      setWorkflowInfo(c.getWorkflowInfo());
      m_customActionGroups.clear();
      m_customActionGroups.addAll(c.m_customActionGroups);
      enableRelatedContent(c.isRelatedContentEnabled());
      m_objectType = c.m_objectType;
      m_iconSource = c.m_iconSource;
      m_iconValue = c.m_iconValue;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSContentEditor)) return false;
      if (!super.equals(o)) return false;
      PSContentEditor that = (PSContentEditor) o;
      return m_contentType == that.m_contentType &&
              m_workflowId == that.m_workflowId &&
              m_enableRelatedContent == that.m_enableRelatedContent &&
              m_producesResource == that.m_producesResource &&
              m_objectType == that.m_objectType &&
              Objects.equals(m_iconSource, that.m_iconSource) &&
              Objects.equals(m_iconValue, that.m_iconValue) &&
              Objects.equals(m_sectionLinkList, that.m_sectionLinkList) &&
              Objects.equals(m_stylesheetSet, that.m_stylesheetSet) &&
              Objects.equals(m_applicationFlow, that.m_applicationFlow) &&
              Objects.equals(m_workflowInfo, that.m_workflowInfo) &&
              Objects.equals(m_validationRules, that.m_validationRules) &&
              Objects.equals(m_inputTranslations, that.m_inputTranslations) &&
              Objects.equals(m_outputTranslations, that.m_outputTranslations) &&
              Objects.equals(m_customActionGroups, that.m_customActionGroups) &&
              Objects.equals(m_viewSet, that.m_viewSet);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_contentType, m_workflowId, m_enableRelatedContent, m_producesResource, m_objectType, m_iconSource, m_iconValue, m_sectionLinkList, m_stylesheetSet, m_applicationFlow, m_workflowInfo, m_validationRules, m_inputTranslations, m_outputTranslations, m_customActionGroups, m_viewSet);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.design.objectstore.IPSComponent#fromXml(org.w3c.dom.Element,
    *      com.percussion.design.objectstore.IPSDocument, java.util.ArrayList)
    */
   @Override
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
         ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args =
         {XML_NODE_NAME, sourceNode.getNodeName()};
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      String data = null;
      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         // REQUIRED: get the content type attribute
         data = tree.getElementData(CONTENT_TYPE_ATTR);
         try
         {
            m_contentType = Long.parseLong(data);
         }
         catch (Exception e)
         {
            Object[] args =
            {XML_NODE_NAME, CONTENT_TYPE_ATTR, ((data == null) ? "null" : data)};
            throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }

         // REQUIRED: get the worflow ID attribute
         data = tree.getElementData(WORKFLOW_ID_ATTR);
         try
         {
            m_workflowId = Integer.parseInt(data);
         }
         catch (Exception e)
         {
            Object[] args =
            {XML_NODE_NAME, WORKFLOW_ID_ATTR, ((data == null) ? "null" : data)};
            throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }

         // OPTIONAL: get the enableRelatedContent flag, defaults to disabled
         data = tree.getElementData(ENABLE_RELATED_CONTENT_ATTR);
         if (data != null)
            m_enableRelatedContent = data.toString().trim().equalsIgnoreCase(
                  "yes");
         else
            m_enableRelatedContent = false;

         // OPTIONAL: get the producesResource flag, defaults to false
         data = tree.getElementData(PRODUCES_RESOURCE_ATTR);
         if (data != null)
            m_producesResource = data.toString().trim().equalsIgnoreCase(
                  "yes");
         else
            m_producesResource = false;

         // OPTIONAL: get the object type, defaults to item
         data = tree.getElementData(OBJECT_TYPE_ATTR);
         if (data != null)
         {
            // if supplied, it must be valid
            try
            {
               int objectType = Integer.parseInt(data);
               if (!PSCmsObject.isValidType(m_objectType))
                  // just get us into the catch clause to throw the real one
                  throw new RuntimeException("");
               m_objectType = objectType;
            }
            catch (Exception e)
            {
               Object[] args =
               {XML_NODE_NAME, OBJECT_TYPE_ATTR, data};
               throw new PSUnknownNodeTypeException(
                     IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
         }
         else
            m_objectType = PSCmsObject.TYPE_ITEM;

         // OPTIONAL: get the icon source
         data = tree.getElementData(ICON_SOURCE_ATTR);
         if (data != null)
         {
            // if supplied, it must be valid
            try
            {
               if (StringUtils.isBlank(data)
                     || !ArrayUtils.contains(ms_iconSources, data))
                  throw new RuntimeException("");
               m_iconSource = data;
            }
            catch (Exception e)
            {
               Object[] args =
               {XML_NODE_NAME, ICON_SOURCE_ATTR, data};
               throw new PSUnknownNodeTypeException(
                     IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
         }
         else
         {
            m_iconSource = ICON_SOURCE_NONE;
            m_iconValue = null;
         }
         // Get the icon value only if the icon source is not none, other wise
         // set it to null
         if(!m_iconSource.equals(ICON_SOURCE_NONE))
         {
            data = tree.getElementData(ICON_VALUE_ATTR);
            if(StringUtils.isNotBlank(data))
            {
               m_iconValue = data;
            }
            else
            {
               Object[] args =
               {XML_NODE_NAME, ICON_VALUE_ATTR, data};
               throw new PSUnknownNodeTypeException(
                     IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
            
         }
         else
         {
            m_iconValue = null;
         }
            

         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN
               | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
         int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS
               | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         Element node = tree.getNextElement(firstFlags);

         // restore the PSDataSet
         super.fromXml(node, parentDoc, parentComponents);

         // OPTIONAL: get the stylesheet set
         node = tree.getNextElement(PSCommandHandlerStylesheets.XML_NODE_NAME,
               nextFlags);
         if (node != null)
         {
            m_stylesheetSet = new PSCommandHandlerStylesheets(node, parentDoc,
                  parentComponents);
         }

         // get all optional elements
         node = tree.getNextElement(nextFlags);
         while (node != null)
         {
            String elementName = node.getTagName();
            if (elementName.equals(PSApplicationFlow.XML_NODE_NAME))
            {
               m_applicationFlow = new PSApplicationFlow(node, parentDoc,
                     parentComponents);
            }
            else if (elementName.equals(SECTION_LINK_LIST_ELEM))
            {
               Node current = tree.getCurrent();

               PSUrlRequest urlRequest = null;
               node = tree.getNextElement(PSUrlRequest.XML_NODE_NAME,
                     firstFlags);
               while (node != null)
               {
                  urlRequest = new PSUrlRequest(node, parentDoc,
                        parentComponents);
                  m_sectionLinkList.add(urlRequest);

                  node = tree.getNextElement(PSUrlRequest.XML_NODE_NAME,
                        nextFlags);
               }

               tree.setCurrent(current);
            }
            else if (elementName.equals(PSValidationRules.XML_NODE_NAME))
            {
               m_validationRules = new PSValidationRules(node, parentDoc,
                     parentComponents);
            }
            else if (elementName.equals(PSInputTranslations.XML_NODE_NAME))
            {
               m_inputTranslations = new PSInputTranslations(node, parentDoc,
                     parentComponents);
            }
            else if (elementName.equals(PSOutputTranslations.XML_NODE_NAME))
            {
               m_outputTranslations = new PSOutputTranslations(node, parentDoc,
                     parentComponents);
            }
            else if (elementName.equals(PSCustomActionGroup.XML_NODE_NAME))
            {
               m_customActionGroups.add(new PSCustomActionGroup(node,
                     parentDoc, parentComponents));
            }
            else if (elementName.equals(PSWorkflowInfo.XML_NODE_NAME))
            {
               m_workflowInfo = new PSWorkflowInfo(node);
            }
            node = tree.getNextElement(nextFlags);
         }
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   // see IPSComponent
   public Element toXml(Document doc)
   {
      // create root and its attributes
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(CONTENT_TYPE_ATTR, "" + m_contentType);
      root.setAttribute(WORKFLOW_ID_ATTR, "" + m_workflowId);
      root.setAttribute(ENABLE_RELATED_CONTENT_ATTR, m_enableRelatedContent
            ? "yes"
            : "no");
      root.setAttribute(PRODUCES_RESOURCE_ATTR, m_producesResource
            ? "yes"
            : "no");
      root.setAttribute(OBJECT_TYPE_ATTR, Integer.toString(m_objectType));
      root.setAttribute(ICON_SOURCE_ATTR, m_iconSource);
      root
            .setAttribute(ICON_VALUE_ATTR, StringUtils
                  .defaultString(m_iconValue));
      // store the base class info
      root.appendChild(super.toXml(doc));

      // create the stylesheet set
      if (m_stylesheetSet != null
            && m_stylesheetSet.getCommandHandlerNames().hasNext())
         root.appendChild(m_stylesheetSet.toXml(doc));

      // create application flow
      if (m_applicationFlow != null)
         root.appendChild(m_applicationFlow.toXml(doc));

      // create section link list
      Iterator it = getSectionLinkList();
      if (it.hasNext())
      {
         Element elem = doc.createElement(SECTION_LINK_LIST_ELEM);
         while (it.hasNext())
            elem.appendChild(((IPSComponent) it.next()).toXml(doc));

         root.appendChild(elem);
      }

      // create validation rules
      if (m_validationRules != null)
         root.appendChild(m_validationRules.toXml(doc));

      // create input translations
      if (m_inputTranslations != null)
         root.appendChild(m_inputTranslations.toXml(doc));

      // create output translations
      if (m_outputTranslations != null)
         root.appendChild(m_outputTranslations.toXml(doc));

      // create the custom action group
      Iterator groups = m_customActionGroups.iterator();
      while (groups.hasNext())
      {
         PSCustomActionGroup group = (PSCustomActionGroup) groups.next();
         root.appendChild(group.toXml(doc));
      }

      if (m_workflowInfo != null)
         root.appendChild(m_workflowInfo.toXml(doc));

      return root;
   }

   /**
    * Validates this object's internal state by checking that:
    * <ol>
    * <li>Content type is a valid id
    * <li>Workflow is a valid id
    * <li>A <code>PSContentEditorPipe</code> is attached
    * <li>Each child node is valid
    * </ol>
    * 
    * @param context The validation context, not <code>null</code>.
    * @throws PSValidationException if a validation error or warning occurs.
    */
   public void validate(IPSValidationContext context)
         throws PSValidationException
   {
      if (!context.startValidation(this, null))
         return;

      // This is assumed to be called at the backend
      PSCmsObject cmsObject = PSServer.getCmsObjectRequired(m_objectType);

      // simple: workflow ids are positive, contentid can't be zero
      if (m_contentType == 0)
      {
         context.validationError(this,
               IPSObjectStoreErrors.INVALID_CONTENT_TYPE, String
                     .valueOf(m_contentType));
      }
      if (cmsObject.isWorkflowable() && (m_workflowId <= 0))
      {
         context.validationError(this,
               IPSObjectStoreErrors.INVALID_WORKFLOW_ID, String
                     .valueOf(m_workflowId));
      }

      // complex: ensure content type id is registered in the CMS
      IPSInternalRequestHandler rh = PSServer
            .getInternalRequestHandler("sys_psxCms/contentTypes");
      if (rh != null)
      {
         PSRequest req = PSRequest.getContextForRequest();
         req.setParameter("sys_contenttype", String.valueOf(m_contentType));
         PSExecutionData data = null;
         try
         {
            data = rh.makeInternalRequest(req);
            if (data != null)
            {
               ResultSet rs = data.getNextResultSet();
               if (!(rs != null && data.readRow()))
               {
                  context.validationError(this,
                        IPSObjectStoreErrors.INVALID_CONTENT_TYPE, String
                              .valueOf(m_contentType));
               }
            }
         }
         catch (RuntimeException e)
         {
            throw e;
         }
         catch (PSValidationException e)
         {
            throw e;
         }
         catch (Exception e)
         {
            // ignore
         }
         finally
         {
            if (data != null)
               data.release();
         }
      }

      
      PSPipe pipe = getPipe();
      if (pipe == null || !(pipe instanceof PSContentEditorPipe))
      {
         String arg = (pipe == null) ? "null" : pipe.getClass().getName();
         Object[] args =
         {arg};
         context.validationError(this,
               IPSObjectStoreErrors.INVALID_CONTENT_EDITOR_PIPE, args);
      }

      // do children
      context.pushParent(this);
      try
      {
         if (m_stylesheetSet != null)
            m_stylesheetSet.validate(context);

         if (m_applicationFlow != null)
            m_applicationFlow.validate(context);

         if (m_sectionLinkList != null)
         {
            Iterator it = this.getSectionLinkList();
            while (it.hasNext())
               ((PSUrlRequest) it.next()).validate(context);
         }

         if (m_validationRules != null)
            m_validationRules.validate(context);

         if (m_inputTranslations != null)
            m_inputTranslations.validate(context);

         if (m_outputTranslations != null)
            m_outputTranslations.validate(context);

         if (getWorkflowInfo() != null)
            getWorkflowInfo().validate(context);

         Iterator groups = m_customActionGroups.iterator();
         while (groups.hasNext())
         {
            PSCustomActionGroup group = (PSCustomActionGroup) groups.next();
            group.validate(context);
         }
      }
      finally
      {
         context.popParent();
      }
   }

   /**
    * Gets this content editor's view set. Used to filter the fields that are
    * displayed.
    * 
    * @return The view set, may be <code>null</code> if one has not been set
    *         by a call to {@link #setViewSet(PSViewSet)}.
    */
   public PSViewSet getViewSet()
   {
      return m_viewSet;
   }

   /**
    * Sets a view set on this editor. See {@link #getViewSet()} for more info.
    * 
    * @param viewSet The viewSet, may not be <code>null</code>.
    */
   public void setViewSet(PSViewSet viewSet)
   {
      if (viewSet == null)
         throw new IllegalArgumentException("viewSet may not be null");

      m_viewSet = viewSet;
   }

   /**
    * Gets the source of the icon for this content editor one of
    * ICON_SOURCE_XXX value. Defaulted to {@link #ICON_SOURCE_NONE}, never
    * <code>null</code>.
    */
   public String getIconSource()
   {
      return m_iconSource;
   }
   
   /**
    * Gets the icon value. If the {@link getIconSource()} is none then the value
    * will be <code>null</code>. The value depends on the mode. See
    * ICON_SOURCE_XXX for the details.
    */
   public String getIconValue()
   {
      return m_iconValue;
   }
   
   /**
    * Sets the icon source and values.
    * @param source Must be one of ICON_SOURCE_XXX values.
    * @param value Must not be blank if icon source is not ICON_SOURCE_NONE.
    */
   public void setContentTypeIcon(String source, String value)
   {
      if (StringUtils.isBlank(source))
         throw new IllegalArgumentException("source must not be blank");
      if (!ArrayUtils.contains(ms_iconSources, source))
         throw new IllegalArgumentException("invalid source");
      if (!source.equals(ICON_SOURCE_NONE) && StringUtils.isBlank(value))
         throw new IllegalArgumentException(
               "value must not be blank for the source values other than 0");

      m_iconSource = source;
      m_iconValue = value;
   }
   
   /**
    * Sets the icon source.
    * 
    * @param source the new icon source, it must be one of ICON_SOURCE_XXX
    * values.
    */
   public void setIconSource(String source)
   {
      if (StringUtils.isBlank(source))
         throw new IllegalArgumentException("source must not be blank");
      if (!ArrayUtils.contains(ms_iconSources, source))
         throw new IllegalArgumentException("invalid source");

      m_iconSource = source;
   }

   /**
    * Sets icon value.
    * @param iconValue the new icon value. It may be <code>null</code> or empty
    * if the icon source is {@link #ICON_SOURCE_NONE}.
    */
   public void setIconValue(String iconValue)
   {
      m_iconValue = iconValue;
   }
   
   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXContentEditor";

   /**
    * A numeric id that uniquely identifies the type of content this editor
    * works with.
    */
   private long m_contentType = -1;

   /**
    * The numeric id of the workflow into which new content items will be sent
    * once they are created.
    */
   private int m_workflowId = -1;

   /**
    * A flag specifying if this content editor supports related content.
    * Defaults to <code>true</code>.
    */
   private boolean m_enableRelatedContent = true;

   /**
    * A flag specifying if this content editor produces a resource kind of item
    * for example images, files etc... Defaults to <code>false</code>.
    */
   private boolean m_producesResource = false;

   /**
    * The object type for this content editor. Default to
    * <code>PSCmsObject.TYPE_ITEM</code>. See {@link #getObjectType()} for
    * more details. Always one of the PSCmsObject.TYPE_xxx values.
    */
   private int m_objectType = PSCmsObject.TYPE_ITEM;

   /**
    * See {@link #getIconSource()}
    */
   private String m_iconSource = ICON_SOURCE_NONE;

   /**
    * See {@link #getIconValue()}
    */
   private String m_iconValue = null;

   /**
    * Constant for icon mode none. Indicates that the icon value is
    * <code>null</code>.
    */
   public static final String ICON_SOURCE_NONE = "0";
   
   /**
    * Constant for icon source specified. Indicates that the icon value
    * contains a icon file name.
    */
   public static final String ICON_SOURCE_SPECIFIED = "1";

   /**
    * Constant for icon source from a file field. Indicates that the icon value
    * contains a file field name.
    */
   public static final String ICON_SOURCE_FROMFILEEXT = "2";

   /**
    * Static array of the allowed icon sources.
    */
   private static final String[] ms_iconSources =
   {ICON_SOURCE_NONE, ICON_SOURCE_SPECIFIED, ICON_SOURCE_FROMFILEEXT};  

   /**
    * A collection of PSUrlRequest objects, never <code>null</code>.
    */
   private PSCollection m_sectionLinkList = new PSCollection(PSUrlRequest.class);

   /**
    * The command handler stylesheets, never <code>null</code> after
    * construction.
    */
   private PSCommandHandlerStylesheets m_stylesheetSet = new PSCommandHandlerStylesheets();

   /** The application flow map, might be <code>null</code>. */
   private PSApplicationFlow m_applicationFlow = null;

   /**
    * Specifies which workflows are permitted for content items created by this
    * editor. Might be <code>null</code>, in which case all content items
    * will use the default workflow.
    */
   private PSWorkflowInfo m_workflowInfo = null;

   /** The group validation rules, never <code>null</code> might be empty. */
   private PSValidationRules m_validationRules = new PSValidationRules();

   /**
    * The group input translations, never <code>null</code> might be empty.
    */
   private PSInputTranslations m_inputTranslations = new PSInputTranslations();

   /**
    * The group output translations, never <code>null</code> might be empty.
    */
   private PSOutputTranslations m_outputTranslations = new PSOutputTranslations();

   /**
    * A set of PSCustomActionGroup objects. Never <code>null</code>, may be
    * empty. A custom action group allows the designer to add to or replace
    * actions in various parts of the editors (example: the main form button
    * could be replaced with the designer specific action).
    */
   private Collection m_customActionGroups = new ArrayList();

   /**
    * This content editor's viewset, contains the set of views used to filter
    * which fields will be displayed. May be <code>null</code>, modified by a
    * call to {@link #setViewSet(PSViewSet)}.
    */
   private PSViewSet m_viewSet;

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String CONTENT_TYPE_ATTR = "contentType";

   private static final String WORKFLOW_ID_ATTR = "workflowId";

   private static final String ENABLE_RELATED_CONTENT_ATTR = "enableRelatedContent";

   private static final String PRODUCES_RESOURCE_ATTR = "producesResource";

   private static final String SECTION_LINK_LIST_ELEM = "SectionLinkList";

   /**
    * The XML attribute name for object type property.
    */
   public static final String OBJECT_TYPE_ATTR = "objectType";

   /**
    * The XML attribute name for icon mode property.
    */
   public static final String ICON_SOURCE_ATTR = "iconSource";

   /**
    * The XML attribute name for icon value property.
    */
   public static final String ICON_VALUE_ATTR = "iconValue";
}
