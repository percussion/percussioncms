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
package com.percussion.rx.publisher.jsf.nodes;

import com.percussion.rx.jsf.PSCategoryNodeBase;
import com.percussion.rx.jsf.PSNavigation;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.sitemgr.IPSLocationScheme;
import com.percussion.services.sitemgr.IPSPublishingContext;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.services.sitemgr.data.PSLocationScheme;
import com.percussion.services.sitemgr.data.PSPublishingContext;
import com.percussion.utils.guid.IPSGuid;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.model.SelectItem;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This node represents a single context design object.
 */
public class PSContextNode extends PSDesignNode
{
   /**
    * This ctor is used for existing contexts.
    */
   public PSContextNode(IPSPublishingContext ctx) 
   {
      this(ctx, null);
   }
   
   /**
    * This ctor is used when a new context and set of schemes are created.
    * 
    * @param ctx Never <code>null</code>.
    * @param schemes May be <code>null</code> or empty. If <code>null</code>,
    * it is assumed that the supplied context is an existing one.
    */
   public PSContextNode(IPSPublishingContext ctx,
         List<IPSLocationScheme> schemes)
   {
      super(ctx.getName(), ctx.getGUID());
      if (schemes != null)
      {
         m_context = ctx;
         m_schemes = wrapAndSortSchemes(schemes, true);
      }
      else
         m_description = ctx.getDescription();
   }

   /**
    * See {@link IPSPublishingContext#getDescription()}.
    * 
    * @return Never <code>null</code>, may be empty.
    */
   public String getDescription()
   {
      String desc;
      if (m_context != null)
         desc = m_context.getDescription();
      else
         desc = m_description;
      return desc == null ? StringUtils.EMPTY : desc;
   }
   
   /**
    * See {@link IPSPublishingContext#setDescription(String)} ()}.
    * 
    * @param desc May be <code>null</code> or empty.
    * @see #getDescription()
    */
   public void setDescription(String desc)
   {
      getContext().setDescription(desc);
   }
   
   /**
    * See {@link IPSPublishingContext#getName()}.
    * 
    * @return Never <code>null</code> or empty. 
    */
   public String getName()
   {
      return getContext().getName();
   }
   
   /**
    * See {@link IPSPublishingContext#getName()}.
    * 
    * @param name Never <code>null</code> or empty. It should be unique among
    * objects of this type, but that is not validated by this method.
    */
   public void setName(String name)
   {
      getContext().setName(name);
   }
   
   /**
    * Overridden to prevent a link on the Preview context as that is a system
    * context w/ no user modifiable properties.
    */
   @Override
   public boolean getEnabled()
   {
      if (getGUID().getUUID() == 0)
         return false;
      return super.getEnabled();
   }

   /**
    * Lazily loads the context for this node if it is not already loaded.
    * @return Never <code>null</code>.
    */
   IPSPublishingContext getContext()
   {
      if (m_context == null)
      {
         IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
         try {
            m_context = smgr.loadContextModifiable(getGUID());
         } catch (PSNotFoundException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
            m_context = new PSPublishingContext();
         }
      }
      return m_context;
   }
   
   /**
    * @param schemeName Should be one of the values returned by the
    * {@link #getDefaultSchemeChoices()} method. If it doesn't match a known
    * name, nothing is done.
    */
   public void setDefaultScheme(String schemeName)
   {
      IPSLocationScheme defaultScheme = null;
      for (PSLocationSchemeWrapper wrapper : getLocationSchemes())
      {
         if (wrapper.getName().equals(schemeName))
            defaultScheme = wrapper.getScheme();
      }
      if (defaultScheme != null)
         getContext().setDefaultSchemeId(defaultScheme.getGUID());
      else
         getContext().setDefaultSchemeId(null);
   }
   
   /**
    * @return The names of all location schemes associated with this context,
    * sorted in ascending alpha order. Never <code>null</code>, may be empty.
    * The first entry in the list is an empty entry.
    */
   public SelectItem[] getDefaultSchemeChoices()
   {
      List<SelectItem> schemeNames = new ArrayList<>();
      for (PSLocationSchemeWrapper s : getLocationSchemes())
      {
         schemeNames.add(new SelectItem(s.getName()));
      }
      
      Collections.sort(schemeNames, new Comparator<SelectItem>()
      {
         public int compare(SelectItem o1, SelectItem o2)
         {
            return o1.getLabel().compareToIgnoreCase(o2.getLabel());
         }
      });
      schemeNames.add(0, new SelectItem(StringUtils.EMPTY));
      return schemeNames.toArray(new SelectItem[schemeNames.size()]);
   }
   
   /**
    * @return The name of the default scheme. May be empty if one is not
    * currently assigned, never <code>null</code>. Will match one of the entries
    * returned by the {@link #getDefaultSchemeChoices()} method.
    */
   public String getDefaultScheme()
   {
      IPSGuid schemeId = getContext().getDefaultSchemeId();
      if (schemeId == null)
         return StringUtils.EMPTY;
      
      for (PSLocationSchemeWrapper s : getLocationSchemes())
      {
         if (schemeId.equals(s.getScheme().getGUID()))
            return s.getName();
      }
      return StringUtils.EMPTY;
   }
   
   /**
    * We override this method so we can clear the local copy of the context and
    * location schemes as they may have been modified. (The context might be
    * modified if the current default scheme is deleted.)
    */
   @Override
   public String cancel()
   {
      if (hasSchemeChanged())
         return "save-child-scheme-changes-warning";
      
      return discardChanges();
   }

   /**
    * Determines if any of the Location Schemes has been modified.
    * @return <code>true</code> if changes has been made in the Location Scheme.
    */
   private boolean hasSchemeChanged()
   {
      if (m_schemes != null)
      {
         for (PSLocationSchemeWrapper scheme : m_schemes)
         {
            if (scheme.isModified())
               return true;
         }
      }
      
      return (!m_deletedSchemes.isEmpty());
   }
   
   /**
    * Discard all changes if any and clear internal data.  
    * @return the outcome of the next page, which should navigate to the parent.
    */
   public String discardChanges()
   {
      m_context = null; // force to reload in getContext()
      m_context = getContext();
      clearData();
      return gotoParentNode();
   }

   @Override
   public String delete()
   {
      IPSSiteManager siteManager = PSSiteManagerLocator.getSiteManager();
      siteManager.deleteContext(getContext());
      remove();
      return navigateToList();
   }

   @Override
   public String navigateToList()
   {
      return PSContextContainerNode.PUB_DESIGN_CONTEXT_VIEW;
   }

   @Override
   public String copy() throws PSNotFoundException {
      IPSSiteManager siteMgr = PSSiteManagerLocator.getSiteManager();
      IPSPublishingContext copiedContext = siteMgr.createContext();
      copiedContext.copy(getContext());
      String name = getContainer().getUniqueName(copiedContext.getName(), true);
      copiedContext.setName(name);
      
      // reset default Location Scheme
      copiedContext.setDefaultSchemeId(null);
      IPSGuid defSchemeId = getContext().getDefaultSchemeId();
      
      List<IPSLocationScheme> schemes = new ArrayList<>();
      for (PSLocationSchemeWrapper wrapper : getLocationSchemes())
      {         
         IPSLocationScheme copiedScheme = copyScheme(wrapper.getScheme(),
               copiedContext);
         schemes.add(copiedScheme);
         
         // reset Location Scheme if needed
         IPSGuid curId = wrapper.getScheme().getGUID();
         if (defSchemeId != null && defSchemeId.equals(curId))
            copiedContext.setDefaultSchemeId(copiedScheme.getGUID());
      }
      
      PSContextNode node = new PSContextNode(copiedContext, schemes);
     
      //clear the just loaded context and schemes
      clearData();

      return node.handleNewContext((PSCategoryNodeBase) getParent());
   }

   /**
    * Copy a given Location Scheme. 
    * @param scheme the source of the copied Location Scheme, assumed not
    *    <code>null</code>.
    * @param parent The new parent context for the cloned scheme. If 
    * <code>null</code>, this node's context is used.
    * @return the cloned Location Scheme, never <code>null</code>.
    */
   private IPSLocationScheme copyScheme(IPSLocationScheme scheme, 
         IPSPublishingContext parent) throws PSNotFoundException {
      IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();

      // todo ph: there should be a copy method on the service that clones
      // the object, assigns a new GUID and clears the hibernate version
      IPSLocationScheme copiedScheme = new PSLocationScheme(); 
      copiedScheme.copy(scheme); 
      
      ((PSLocationScheme) copiedScheme).setGUID(guidMgr.createGuid(
            PSTypeEnum.LOCATION_SCHEME));
      if (parent == null)
         copiedScheme.setContextId(m_context.getGUID());
      else
         copiedScheme.setContextId(parent.getGUID());
      PSContextContainerNode parentNode = 
         (PSContextContainerNode) getParent();
      copiedScheme.setName(parentNode.getUniqueName(scheme.getName(), true,
            getSchemeNames()));

      return copiedScheme;
   }
   
   /**
    * Sets the data objects associated with this node to <code>null</code>. 
    * They will be loaded the next time they are accessed.
    */
   private void clearData()
   {
      if (m_context != null)
      {
         setTitle(m_context.getName());
         m_description = m_context.getDescription();
      }
      m_context = null;
      m_schemes = null;
      m_deletedSchemes.clear();
   }
   
   /**
    * Permanently persists the current context. 
    * 
    * @return The outcome that controls navigation. <code>null</code> if the
    * save fails.
    */
   public String save()
   {
      doSave();
      clearData();
      return gotoParentNode();
   }
   
   /**
    * Queries the server to obtain a list of all known content types. The list
    * is cached after it is queried the first time.
    * 
    * @return A map containing pairs in which the key is the ctype id and the
    * value is the display name. Empty if a failure occurs while querying the
    * server, in which case a message is logged.
    */
   public Map<IPSGuid, String> catalogContentTypes()
   {
      if (m_ctypeLabels != null)
         return m_ctypeLabels;
      
      Map<IPSGuid, String> results = new HashMap<>();
      try
      {
         IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();
         List<IPSNodeDefinition> ctypes = mgr.findAllItemNodeDefinitions();
         for (IPSNodeDefinition def : ctypes)
         {
            results.put(def.getGUID(), def.getLabel() == null
                  ? def.getInternalName()
                  : def.getLabel());
         }
         m_ctypeLabels = results;
      }
      catch (RepositoryException e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         log.error("Failed while cataloging content types for context node. {}", e.getMessage());
      }
      return results;
   }
   
   /**
    * Queries the server to obtain a list of all known templates. The list
    * is cached after it is queried the first time.
    * 
    * @return A map containing pairs in which the key is the template id and the
    * value is the display name. Empty if a failure occurs while querying the
    * server, in which case a message is logged.
    */
   public Map<IPSGuid, String> catalogTemplates()
   {
      if (m_templateLabels != null)
         return m_templateLabels;
      
      Map<IPSGuid, String> results = new HashMap<>();
      try
      {
         IPSAssemblyService svc = PSAssemblyServiceLocator.getAssemblyService();
         Set<IPSAssemblyTemplate> templates = svc.findAllTemplates();
         for (IPSAssemblyTemplate t : templates)
         {
            results.put(t.getGUID(), t.getLabel());
         }
         m_templateLabels = results;
      }
      catch (PSAssemblyException e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         log.error("Failed while cataloging templates for context node. {}", e.getMessage());
      }
      return results;
   }
   
   /**
    * Looks up the supplied <code>id</code> in {@link #m_ctypeLabels} and returns
    * the associated display label. If not found, the id is returned.
    * 
    * @param id The content UUID.
    * 
    * @return Never <code>null</code> or empty.
    */
   private String getContentTypeLabel(long id)
   {
      Map<IPSGuid, String> ctypes = catalogContentTypes();
      String result = null;
      for (IPSGuid guid : ctypes.keySet())
      {
         if (guid.getUUID() == id)
         {
            result = ctypes.get(guid);
            break;
         }
      }
      if (result == null)
         result = String.valueOf(id);
      return result;
   }
   
   /**
    * Looks up the supplied <code>id</code> in {@link #m_templateLabels} and returns
    * the associated display label. If not found, the id is returned.
    * 
    * @param id The content UUID.
    * 
    * @return Never <code>null</code> or empty.
    */
   private String getTemplateLabel(long id)
   {
      Map<IPSGuid, String> templates = catalogTemplates();
      String result = null;
      for (IPSGuid guid : templates.keySet())
      {
         if (guid.getUUID() == id)
         {
            result = templates.get(guid);
            break;
         }
      }
      if (result == null)
         result = String.valueOf(id);
      return result;
   }
   
   /**
    * @return All schemes associated with this context in an unmodifiable list.
    * Never <code>null</code>, may be empty. Ordered in ascending alpha order
    * by name. The caller should treat the objects as read-only.
    */
   public List<PSLocationSchemeWrapper> getLocationSchemes()
   {
      IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
      if (m_schemes == null)
      {
         m_schemes = wrapAndSortSchemes(smgr.findSchemesByContextId(getContext()
               .getGUID()), false);
      }
      return Collections.unmodifiableList(m_schemes);
   }

   /**
    * Wrap each supplied scheme and add it to a list, which is then sorted by
    * name, ascending and returned.
    * 
    * @param schemes Assumed not <code>null</code>.
    * @param isNew If the supplied schemes have never been persisted, supply
    * <code>true</code>, otherwise <code>false</code>.
    * @return Never <code>null</code>.
    */
   private List<PSLocationSchemeWrapper> wrapAndSortSchemes(
         List<IPSLocationScheme> schemes, boolean isNew)
   {
      List<PSLocationSchemeWrapper> wrappedSchemes = 
         new ArrayList<>();
      for (IPSLocationScheme scheme : schemes)
         wrappedSchemes.add(new PSLocationSchemeWrapper(scheme, isNew));
      Collections.sort(wrappedSchemes, new Comparator<PSLocationSchemeWrapper>()
      {
         public int compare(PSLocationSchemeWrapper o1,
               PSLocationSchemeWrapper o2)
         {
            return o1.getName().compareToIgnoreCase(o2.getName());
         }
      });
      return wrappedSchemes;
   }
   
   /**
    * Creates an instance of a Location Scheme for Location Scheme Editor.
    * The created Legacy Location Scheme may not include some of the required 
    * properties, such as Template ID. We will rely on Location Scheme Editor 
    * to validate all required fields.
    * 
    * @return the outcome of the Location Scheme editor.
    */
   public String createScheme() throws PSNotFoundException {
      PSLocationSchemeWrapper scheme = createLocationScheme(false);
      m_editedScheme = new PSLocationSchemeEditor(this, scheme, true);

      return PSLocationSchemeEditor.SCHEME_EDITOR;
   }

   /**
    * Creates an instance of a Legacy Location Scheme for Location Scheme Editor.
    * The created Legacy Location Scheme may not include some of the required 
    * properties, such as Template ID. We will rely on Location Scheme Editor 
    * to validate all required fields.
    * 
    * @return the outcome of the Legacy Location Scheme editor.
    */
   public String createLegacyScheme() throws PSNotFoundException {
      PSLocationSchemeWrapper scheme = createLocationScheme(true);
      m_editedScheme = new PSLocationSchemeEditor(this, scheme, true);

      return PSLocationSchemeEditor.LEGACY_SCHEME_EDITOR;
   }
   
   
   /**
    * Creates a Location Scheme, which may not include some of the required
    * properties, such as Template ID. 
    *  
    * @return the created Location Scheme, never <code>null</code>.
    */
   private PSLocationSchemeWrapper createLocationScheme(boolean isLegacy) throws PSNotFoundException {
      final IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
      IPSLocationScheme scheme = smgr.createScheme();

      // set name and context
      PSContextContainerNode parent = (PSContextContainerNode) getParent();
      scheme.setName(parent.getUniqueName("Scheme", false, getSchemeNames()));
      scheme.setContextId(m_context.getGUID());
      
      if (!isLegacy)
      {
         scheme.setGenerator(PSLocationSchemeEditor.JEXL_GENERATOR);
         // set a note to remind user to enter JEXL for none legacy scheme
         scheme.setParameter(PSLocationSchemeEditor.EXPRESSION_PARAM, "String",
               "Please enter JEXL Expression here.");
      }
      
      // set Content Type ID
      Map<IPSGuid, String> map = catalogContentTypes();
      if (map.isEmpty())
         throw new IllegalStateException("No Conetnt Type available.");
      
      for (IPSGuid id : map.keySet())
      {
         IPSNodeDefinition node = getNodeDef(id);
         if (node != null && (! node.getVariantGuids().isEmpty()))
         {
            scheme.setContentTypeId(id.longValue());
            return new PSLocationSchemeWrapper(scheme, true);
         }
      }
      throw new IllegalStateException(
         "There is no template registered in any Conetnt Types.");
   }
   
   /**
    * @return a list of Location Scheme names for the current Context. It never
    *    be <code>null</code>, but may be empty.
    */
   private List<String> getSchemeNames()
   {
      List<String> names = new ArrayList<>();
      for (PSLocationSchemeWrapper wrapper : m_schemes)
      {
         names.add(wrapper.getName());
      }
      return names;
   }
   
   /**
    * Gets the node definition for the given Content Type ID.
    * @param ctId the Content Type ID, assumed not <code>null</code>.
    * @return the node definition, may be <code>null</code> if failed to load.
    */
   IPSNodeDefinition getNodeDef(IPSGuid ctId)
   {
      if (ctId == null)
         throw new IllegalArgumentException("ctId may not be null.");
      
      IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();
      List<IPSNodeDefinition> cts;
      try
      {
         cts = mgr.loadNodeDefinitions(Collections.singletonList(ctId));
      }
      catch (RepositoryException e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         return null;
      }
      if (cts.size() == 0)
         return null;
      else
         return cts.get(0);
   }

   /**
    * Saves {@link #m_context} and schemes to persistent store. If
    * <code>false</code> is returned, what was actually saved is not defined.
    * 
    * @return <code>true</code> if the save is successful, <code>false</code>
    * otherwise, in which case the error is logged.
    */
   private boolean doSave()
   {
      final IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
      try
      {
         //todo ph: I would really like a transaction here.
         smgr.saveContext(m_context);
         
         for (PSLocationSchemeWrapper wrapper : m_schemes)
         {
            IPSLocationScheme s = wrapper.getScheme();
            if (wrapper.isModified())
            {
               smgr.saveScheme(s);
               wrapper.setModified(false);
            }
         }

         for (IPSLocationScheme scheme : m_deletedSchemes)
         {
            smgr.deleteScheme(scheme);
         }
         m_deletedSchemes.clear();
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         log.error("Failure saving context or location scheme: {} ", e.getMessage());
         return false;
      }

      return true;
   }
   
   /**
    * Saves this object to persistent storage and passes control to
    * {@link #editNewNode(PSCategoryNodeBase, PSDesignNode)}.
    * 
    * @param parent The node to that this node will be added to. Assumed never
    * <code>null</code>.
    * @return The outcome, which will generally direct to the editor screen.
    */
   public String handleNewContext(PSCategoryNodeBase parent)
   {
      if (!doSave())
         return null;
      return editNewNode(parent, this);
   }

   /**
    * @return The wrapper containing the location scheme that is currently 
    * selected, or <code>null</code> if one is not selected.
    */
   public PSLocationSchemeWrapper getSelectedScheme()
   {
      if (m_schemes == null)
         return null;
      
      for(PSLocationSchemeWrapper wrapper : m_schemes)
      {
         if (wrapper.isSelected())
         {
            return wrapper;
         }
      }
      return null;
   }
   
   /**
    * Add a Location Scheme.
    * @param scheme the to be added scheme, never <code>null</code>.
    */
   void addScheme(PSLocationSchemeWrapper scheme)
   {
      if (scheme == null)
         throw new IllegalArgumentException("scheme may not be null.");
      m_schemes.add(scheme);
   }
   
   /**
    * Action to copy a selected Location Scheme, then edit the copied one
    * @return the outcome to navigate to Location Scheme Editor.
    */
   public String copyScheme() throws PSNotFoundException {
      for(PSLocationSchemeWrapper wrapper : m_schemes)
      {
         if (wrapper.isSelected())
         {
            IPSLocationScheme copiedScheme = copyScheme(wrapper.mi_scheme, 
                  null);
            m_editedScheme = new PSLocationSchemeEditor(this,
                  new PSLocationSchemeWrapper(copiedScheme, true), true);
            return m_editedScheme.perform();
         }
      }
      return PSNavigation.NONE_SELECT_WARNING;      
   }
   
   /**
    * Action to edit a selected Location Scheme.
    * @return the outcome to navigate to Location Scheme Editor.
    */
   public String editScheme()
   {
      return editScheme(null);
   }

   /**
    * Edit the specified or selected Location Scheme.
    *   
    * @param w the edited Location Scheme if specified. If it is 
    *    <code>null</code>, then edit the selected one.
    * 
    * @return the outcome of the Location Scheme editor if there is one to be
    *    edited with.
    */
   private String editScheme(PSLocationSchemeWrapper w)
   {
      if (w == null)
         w = getSelectedScheme();
      
      if (w == null)
         return PSNavigation.NONE_SELECT_WARNING;      

      if (! reloadScheme(w))
         return null;
      
      m_editedScheme = new PSLocationSchemeEditor(this, w, false);
      return m_editedScheme.perform();
   }

   /**
    * Reload the Location Scheme from the database.
    * @param wrapper the Location Scheme wrapper, assumed not <code>null</code>.
    * @return <code>true</code> if load successfully loaded. 
    */
   private boolean reloadScheme(PSLocationSchemeWrapper wrapper)
   {
      // don't reload if it is new or modified.
      if (wrapper.isModified())
         return true;
      
      IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
      try
      {
         wrapper.mi_scheme = smgr.loadSchemeModifiable(wrapper.mi_scheme
               .getGUID());
      }
      catch (PSNotFoundException e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         log.error("Failure loading Location Scheme: {}", e.getMessage());
         return false;
      }
      return true;
   }
   
   /**
    * Action to remove a selected location scheme. Navigates to the removal
    * confirmation page.
    * @return the outcome
    */
   public String deleteScheme()
   {
      PSLocationSchemeWrapper w = getSelectedScheme();
      if (w != null)
         return "remove-scheme";
      else
         return PSNavigation.NONE_SELECT_WARNING;
   }
   
   /**
    * Action to remove the selected scheme from local memory. This change is
    * not persisted until the context is saved.
    * 
    * @return outcome, never <code>null</code> or empty.
    */
   public String deleteSchemeCompletion()
   {
      PSLocationSchemeWrapper w = getSelectedScheme();
      if (w != null)
      {
         m_schemes.remove(w);
         IPSLocationScheme s = w.getScheme();
         m_deletedSchemes.add(s);
         if (m_context.getDefaultSchemeId() != null)
         {
            if (m_context.getDefaultSchemeId().equals(s.getGUID()))
               m_context.setDefaultSchemeId(null);
         }
      }
      
      return DONE_OUTCOME;
   }

   /**
    * @return The outcome that will navigate back to the editor page from the
    * scheme removal page if that operation is cancelled.
    */
   public String cancelSchemeAction()
   {
      return DONE_OUTCOME;
   }

   /**
    * A simple class to wrap the location schemes associated with this context
    * to store the scheme and UI specific data.
    *
    * @author paulhoward
    */
   public class PSLocationSchemeWrapper
   {
      /**
       * ctor
       * 
       * @param scheme Assumed never <code>null</code>. 
       * @param isNew Should be supplied as <code>true</code> if this scheme
       * has never been persisted.
       */
      public PSLocationSchemeWrapper(IPSLocationScheme scheme, boolean isNew)
      {
         mi_scheme = scheme;
         mi_modified = isNew;
      }
      
      /**
       * Convenience method that calls
       * {@link PSLocationSchemeWrapper#PSLocationSchemeWrapper(IPSLocationScheme, boolean) <code>this</code>(<code>scheme</code>, <code>false</code>)}.
       */
      public PSLocationSchemeWrapper(IPSLocationScheme scheme)
      {
         this(scheme, false);
      }
      
      /**
       * @return The scheme wrapped by this object. Never <code>null</code>.
       * Any changes to the returned object will be reflected in the wrapped
       * object.
       */
      public IPSLocationScheme getScheme()
      {
         return mi_scheme;
      }
      
      /**
       * Sets the location scheme.
       * 
       * @param scheme the new location scheme, never <code>null</code>.
       */
      public void setLocationScheme(IPSLocationScheme scheme)
      {
         if (scheme == null)
            throw new IllegalArgumentException("scheme may not be null.");
         
         mi_scheme = scheme;
      }
      
      /**
       * See {@link IPSLocationScheme#getName()}.
       */
      public String getName()
      {
         return mi_scheme.getName();
      }

      /**
       * Builds a concatenation of the content type name and id with the
       * template name and id for display purposes.
       * 
       * @return Never <code>null</code> or empty.
       */
      public String getUsedBy()
      {
         long ctypeId = getContentTypeId();
         long templateId = getTemplateId();
         String ctypeName = getContentTypeLabel(ctypeId);
         String templateName = getTemplateLabel(templateId);
         return MessageFormat.format("{0} ({1}):{2} ({3})", ctypeName,
               ctypeId, templateName, templateId); 
      }
      
      /**
       * Calls {@link PSDesignNode#getNameWithId(String, long)} with the 
       * schemes name and UUID and returns it.
       * @return Never <code>null</code> or empty.
       */
      public String getNameWithId()
      {
         return PSDesignNode.getNameWithId(getName(), mi_scheme.getGUID()
               .getUUID());
      }
      
      /**
       * See {@link IPSLocationScheme#getDescription()}.
       */
      public String getDescription()
      {
         return mi_scheme.getDescription();
      }
      
      /**
       * See {@link IPSLocationScheme#getGenerator()}.
       */
      public String getGenerator()
      {
         return mi_scheme.getGenerator();
      }
      
      /**
       * See {@link IPSLocationScheme#getGUID()}.
       */
      public IPSGuid getGuid()
      {
         return mi_scheme.getGUID();
      }
      
      /**
       * @return the long value of the Content Type ID; It is <code>-1</code>
       *    if the Content Type is not defined. 
       */
      public long getContentTypeId()
      {
         if (mi_scheme.getContentTypeId() == null)
            return -1L;
         else 
            return mi_scheme.getContentTypeId().longValue();
      }
      
      /**
       * @return the long value of the Template ID; It is <code>-1</code>
       *    if the Template is not defined. 
       */
      public long getTemplateId()
      {
         if (mi_scheme.getTemplateId() == null)
            return -1L;
         else
            return mi_scheme.getTemplateId().longValue();
      }
      
      /**
       * Used by UI as a flag to indicate whether this scheme should be a target
       * for some action. Defaults to <code>false</code>.
       */
      public boolean isSelected()
      {
         return mi_selected;
      }

      /**
       * See @link {@link #isModified()} for details.
       */
      public void setModified(boolean modified)
      {
         mi_modified = modified;
      }
      
      /**
       * @return <code>true</code> if this object was created with the flag
       * set or any property has been modified since creation.
       */
      public boolean isModified()
      {
         return mi_modified;
      }
      
      /**
       * Sets the selected state of this scheme. See {@link #isSelected()} for
       * more details.
       */
      public void setSelected(boolean sel)
      {
         mi_selected = sel;
      }
      
      /**
       * @return the outcome of the Location Scheme Editor, never 
       *    <code>null</code>.
       */
      public String perform()
      {
         return editScheme(this);
      }
      
      /**
       * See {@link #isSelected()} for details.
       */
      private boolean mi_selected;
      
      /**
       * This flag is set once any value has been changed on a scheme. It is
       * cleared after being persisted. 
       */
      private boolean mi_modified = false;
      
      /**
       * The real object that this class wraps. Set in ctor, then never
       * <code>null</code>.
       */
      private IPSLocationScheme mi_scheme;
   }
   
   /**
    * @return the edited Location Scheme, never <code>null</code>.
    */
   public PSLocationSchemeEditor getScheme()
   {
      if (m_editedScheme == null)
         throw new IllegalStateException("m_editedScheme must not be null.");
      
      return m_editedScheme;
   }

   @Override
   public String getHelpTopic()
   {
      return "ContextEditor";
   }

   /**
    * The class log.
    */
   private final static Logger log = LogManager.getLogger(PSContextNode.class);
   
   /**
    * The data object that this node represents. Lazily loaded by
    * {@link #getContext()}. Cleared under various conditions. The intent is 
    * to be <code>null</code> unless the object is being edited.
    */
   private IPSPublishingContext m_context;

   /**
    * This member is used to store the description when the {@link #m_context}
    * member is not loaded. Always check that member first before using this
    * value and only use this value if that member is <code>null</code>.
    * The {@link #getDescription()} method manages this rule.
    */
   private String m_description;

   /**
    * Stores all associated schemes, initially sorted in ascending alpha order
    * by name. Defaults to <code>null</code>. Lazily loaded by
    * {@link #getLocationSchemes()} and cleared under various conditions w/
    * {@link #m_context}. The intent is to be <code>null</code> unless the
    * object is being edited.
    */
   private List<PSLocationSchemeWrapper> m_schemes = null; 
   
   /**
    * As schemes are deleted, they are added to this list so the deletes can be
    * persisted when the context is saved. Never <code>null</code>.
    */
   private Collection<IPSLocationScheme> m_deletedSchemes = 
      new ArrayList<>();

   /**
    * Lazily set by {@link #catalogContentTypes()}, then never modified. See
    * that method for description.
    */
   private Map<IPSGuid, String> m_ctypeLabels;

   /**
    * Lazily set by {@link #catalogTemplates()}, then never modified. See that
    * method for description.
    */
   private Map<IPSGuid, String> m_templateLabels;

   /**
    * The action result indicating that it completed successfully. 
    */
   static final String DONE_OUTCOME = "done";
   
   /**
    * The place holder for the edited Location Scheme.
    */
   private PSLocationSchemeEditor m_editedScheme;
   
}
