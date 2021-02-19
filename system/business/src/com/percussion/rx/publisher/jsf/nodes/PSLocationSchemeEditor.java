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

import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionManager;
import com.percussion.extension.PSExtensionRef;
import com.percussion.rx.publisher.jsf.nodes.PSContextNode.PSLocationSchemeWrapper;
import com.percussion.rx.publisher.jsf.nodes.PSJexlMethodsForScheme.JexlMethod;
import com.percussion.rx.ui.jsf.beans.PSHelpTopicMapping;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.sitemgr.IPSLocationScheme;
import com.percussion.services.utils.jsf.validators.PSBaseValidator;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.trinidad.component.core.input.CoreSelectOneChoice;

/**
 * The backing bean for Location Scheme Editor.
 *
 * @author YuBingChen
 */
public class PSLocationSchemeEditor extends PSBaseValidator
{
   /**
    * The outcome of the Location Scheme Editor
    */
   public final static String SCHEME_EDITOR = "pub-design-location-scheme-editor";
   
   /**0
    * The outcome of the Legacy Location Scheme Editor
    */
   public final static String LEGACY_SCHEME_EDITOR = "pub-design-location-scheme-legacy-editor";
   
   /**
    * The name of the parameter for JEXL expression.
    */
   public static final String EXPRESSION_PARAM = "expression";

   /**
    * The Generator used to process JEXL expression in previous release.
    */
   public static final String JEXL_GENERATOR = "Java/global/percussion/contentassembler/sys_JexlAssemblyLocation";

   /**
    * The outcome of the warning page when there is selected no parameter.   
    */
   private static final String NO_PARAM_SELECTION = "no-scheme-parameter-selection-warning";
   
   /**
    * The cloned Location Scheme object, never <code>null</code> after
    * the constructor. It is used to hold the UI data.
    */
   private IPSLocationScheme m_clonedScheme;
   
   /**
    * The original Location Scheme. It needs to be updated from
    * {@link #m_clonedScheme} when it is done; but don't update this
    * object if user cancels the editor. It is initialized by constructor,
    * never <code>null</code> after that.
    */
   private PSLocationSchemeWrapper m_srcScheme;
   
   /**
    * The node definition of the {@link #m_clonedScheme}. It is set
    * by  if has not been set yet.
    */
   private IPSNodeDefinition m_nodeDef;
   
   /**
    * A list of parameters. It is used for editing Legacy Location Scheme.
    */
   private List<SchemeParam> m_parameters;
   
   /**
    * The place holder for adding a parameter. Created/Initialized by 
    * .
    */
   private SchemeParam m_createdParam;
   
   /**
    * Determines if the {@link #m_srcScheme} is a created instance or not.
    * If it is a created one, then it should be appended into the 
    * Location Scheme list when user is {@link #done()} with it.
    */
   private boolean m_isCreated;
   
   /**
    * The parent / context node instance, never <code>null</code> after ctor.
    */
   private PSContextNode m_ctxNode;
   
   /**
    * The filter all the predefined JEXL methods and variables. 
    */
   private String m_jexlFilter = "";
   
   /**
    * The constructor. 
    * 
    * @param cxtNode the parent/context node, never <code>null</code>.
    * @param scheme the to be edited Location Scheme wrapper, never 
    *    <code>null</code>.
    * @param isCreated <code>true</code> if the Location Scheme is created
    *    and has not be added into  yet.
    */
   public PSLocationSchemeEditor(PSContextNode cxtNode,
         PSLocationSchemeWrapper scheme, boolean isCreated)
   {
      if (cxtNode == null)
         throw new IllegalArgumentException("cxtNode may not be null.");
      if (scheme == null)
         throw new IllegalArgumentException("scheme may not be null.");
      
      m_ctxNode = cxtNode;
      m_isCreated = isCreated;
      
      m_srcScheme = scheme;
      try
      {
         m_clonedScheme = (IPSLocationScheme) m_srcScheme.getScheme().clone();
      }
      catch (CloneNotSupportedException e)
      {
         // should never happen here.
         e.printStackTrace();
         ms_log.error("Failed to clone IPSLocationScheme", e);
         throw new RuntimeException("Failed to clone IPSLocationScheme"
               + e.getLocalizedMessage());
      }
      
      // init parameters
      m_parameters = new ArrayList<SchemeParam>();
      for (String pname : m_clonedScheme.getParameterNames())
      {
         String type = m_clonedScheme.getParameterType(pname);
         String value = m_clonedScheme.getParameterValue(pname);
         m_parameters.add(new SchemeParam(pname, type, value));
      }
   }

   /**
    * Determines if creating a Location Scheme or editing an existing one.
    * @return <code>true</code> if creating a Location Scheme.
    */
   public boolean isNew()
   {
      return m_isCreated;
   }
   
   /**
    * @return the parent node (on the tree) of the Location Scheme Editor, 
    *    never <code>null</code>.
    */
   PSContextNode getParentNode()
   {
      return m_ctxNode;
   }
   
   /**
    * The backing bean for the parameter of the edited Location Scheme.
    */
   public static class SchemeParam
   {
      private String mi_name;
      private String mi_type;
      private String mi_value;
      private boolean mi_selected;
      
      /**
       * Create an instance of the Scheme Parameter.
       * @param name the name of the parameter, assumed not <code>null</code>
       *    or empty.
       * @param type the type of the parameter, assumed not <code>null</code>
       *    or empty.
       * @param value the value of the parameter, may be <code>null</code> or
       *    empty.
       */
      private SchemeParam(String name, String type, String value)
      {
         mi_name = name;
         mi_type = type;
         mi_value = value;
         mi_selected = false;
      }
      
      /**
       * @return the name of the parameter, never <code>null</code> or empty.
       */
      public String getName()
      {
         return mi_name;
      }
      
      /**
       * Set the name of the parameter.
       * @param name the new name of the parameter, never <code>null</code> or
       *    empty.
       */
      public void setName(String name)
      {
         if (name == null || name.trim().length() == 0)
            throw new IllegalArgumentException("name must not be null or empty.");
         
         mi_name = name;
      }
      
      /**
       * @return the type of the parameter, never <code>null</code> or empty. 
       */
      public String getType()
      {
         return mi_type;
      }
      
      /**
       * Set the type of the parameter.
       * @param type the new type of the paramter, never <code>null</code> or
       *    empty.
       */
      public void setType(String type)
      {
         if (type == null || type.trim().length() == 0)
            throw new IllegalArgumentException("type may not be null or empty.");
         
         mi_type = type;
      }
      
      /**
       * @return the value of the parameter, may be <code>null</code> or empty.
       */
      public String getValue()
      {
         return mi_value;
      }
      
      /**
       * Set the value of the parameter.
       * @param value the new value of the parameter, never <code>null</code> or
       *    empty.
       */
      public void setValue(String value)
      {
         if (value == null || value.trim().length() == 0)
            throw new IllegalArgumentException("value may not be null or empty.");
         
         mi_value = value;
      }
      
      /**
       * @return <code>true</code> of the parameter is selected.
       */
      public boolean getSelected()
      {
         return mi_selected;
      }
      
      /**
       * Set selected state
       * @param selected the new selected state.
       */
      public void setSelected(boolean selected)
      {
         mi_selected = selected;
      }
      
      /**
       * Get the actual help file name for the parameter editor page.
       * 
       * @return  the help file name, never <code>null</code> or empty.
       */
      public String getHelpFile()
      {
         return PSHelpTopicMapping.getFileName("LocationSchemeParamEditor");   
      }
   }
   
   /**
    * @return the filtered JEXL methods and variables, never <code>null</code>
    *    may be empty.
    */
   public List<JexlMethod> getFilteredJexlMethods()
   {
      return PSJexlMethodsForScheme.getFilteredJexlMethods(m_jexlFilter);
   }
   
   /**
    * @return the JEXL filter pattern, never <code>null</code>.
    */
   public String getJexlFilter()
   {
      return m_jexlFilter;
   }
   
   /**
    * Set the JEXL filter.
    * @param filter the new filter pattern, never <code>null</code>, may be
    *    empty.
    */
   public void setJexlFilter(String filter)
   {
      if (filter == null)
         throw new IllegalArgumentException("filter may not be null.");
      
      m_jexlFilter = filter;
   }
   
   /**
    * Clear the JEXL filter.
    * @return outcome of the current Location Scheme Editor, never 
    *    <code>null</code> or empty.
    */
   public String clearJexlFilter()
   {
      setJexlFilter("");
      return perform();
   }
   
   /**
    * Remove a selected parameter.
    * 
    * @return outcome of the next page. It may be <code>null</code> if
    *    successfully removed a parameter.
    */
   public String removeParameter()
   {
      int i = getSelectedParameter();
      if (i == -1)
         return NO_PARAM_SELECTION;
      
      m_parameters.remove(i);
      return null;
   }
   
   /**
    * @return the index of the selected parameter. It is <code>-1</code> if
    *    there is no selected parameter.
    */
   private int getSelectedParameter()
   {
      int i=0;
      for (SchemeParam p : m_parameters)
      {
         if (p.mi_selected)
         {
            return i;
         }
         i++;
      }
      return -1;
   }
   
   /**
    * Moves the selected parameter up one, switch position with the previous
    * element.
    * 
    * @return the outcome of the no selection warning page if no parameter
    *    is selected, or <code>null</code> if successfully done the switch. 
    */
   public String moveUpParameter()
   {
      int i = getSelectedParameter();
      if (i == -1)
         return NO_PARAM_SELECTION;
      
      if (i == 0)
         return null; // do nothing if already at the top
      
      SchemeParam p1 = m_parameters.get(i-1);
      SchemeParam p2 = m_parameters.get(i);
      
      m_parameters.set(i-1, p2);
      m_parameters.set(i, p1);
      
      return null;
   }
   
   /**
    * Moves the selected parameter down one, switch position with the next
    * element.
    * 
    * @return the outcome of the no selection warning page if no parameter
    *    is selected, or <code>null</code> if successfully done the switch. 
    */
   public String moveDownParameter()
   {
      int i = getSelectedParameter();
      if (i == -1)
         return NO_PARAM_SELECTION;
      
      if (i == (m_parameters.size()-1))
         return null; // no nothing if already at the bottom
      
      SchemeParam p1 = m_parameters.get(i);
      SchemeParam p2 = m_parameters.get(i+1);
      
      m_parameters.set(i, p2);
      m_parameters.set(i+1, p1);
      
      return null;         
   }

   /**
    * Creates a parameter for the edited Location Scheme.
    * @return the outcome of the parameter editor, never <code>null</code> 
    *    or empty.
    */
   public String createParameter() throws PSNotFoundException {
      // get a unique parameter name
      List<String> names = new ArrayList<String>();
      for (SchemeParam p : m_parameters)
      {
         names.add(p.getName());
      }
      PSContextContainerNode parent = (PSContextContainerNode) m_ctxNode
            .getParent();
      String pname = parent.getUniqueName("Param", false, names);

      m_createdParam = new SchemeParam(pname, "String", "");
      return "pub-design-location-scheme-param-editor";
   }
   
   /**
    * Add the created parameter to the edited Location Scheme.
    * @return outcome of the Location Scheme Editor.
    */
   public String addCreatedParameter()
   {
      if (m_createdParam == null)
         throw new IllegalStateException("m_createdParam must not be null");
      
      m_parameters.add(m_createdParam);
      m_createdParam = null;
      return this.perform();
   }
   
   /**
    * @return the created Parameter, never <code>null</code>.
    */
   public SchemeParam getCreatedParameter()
   {
      if (m_createdParam == null)
         throw new IllegalStateException("m_createdParam must not be null");

      return m_createdParam;         
   }
   
   /**
    * @return the parameters, never <code>null</code>, may be empty.
    */
   public List<SchemeParam> getParameters()
   {
      return m_parameters;
   }
   
   /*
    * //see base class method for details
    * This is used to make sure the name of the edited Location Scheme is 
    * unique among its siblings.
    */
   public void validate(@SuppressWarnings("unused")FacesContext context, 
         @SuppressWarnings("unused")UIComponent component,
         Object value)
   {
      if (! (value instanceof String))
         throw new IllegalArgumentException("value must be instanceof String");
      
      String svalue = (String) value;
      for (PSLocationSchemeWrapper w : m_ctxNode.getLocationSchemes())
      {
         if (w.getScheme() != m_srcScheme.getScheme() &&
               w.getScheme().getName().equalsIgnoreCase(svalue))
         {
            fail(FacesMessage.SEVERITY_ERROR, "jsf@non_unique_value"); 
         }
      }
   }
   
   /**
    * See {@link IPSLocationScheme#getName()} for detail. 
    */
   public String getName()
   {
      return m_clonedScheme.getName();
   }

   /**
    * See {@link IPSLocationScheme#setName(String)} for detail. 
    */
   public void setName(String name)
   {
      m_clonedScheme.setName(name);
   }
   
   /**
    * See {@link IPSLocationScheme#getDescription()} for detail. 
    */
   public String getDescription()
   {
      return m_clonedScheme.getDescription();
   }

   /**
    * See  for detail.
    */
   public void setDescription(String name)
   {
      m_clonedScheme.setDescription(name);
   }
   
   /**
    * @return the value of expression parameter, may be <code>null</code> or
    *    empty. This is also the JEXL expression.
    */
   public String getExpression()
   {
      return m_clonedScheme.getParameterValue(EXPRESSION_PARAM);
   }
   
   /**
    * Set the JEXL expression. 
    * @param exp the expression, never <code>null</code> or empty.
    */
   public void setExpression(String exp)
   {
      if (exp == null || exp.trim().length() == 0)
         throw new IllegalArgumentException("exp may not be null or empty.");
      
      m_clonedScheme.setParameter(EXPRESSION_PARAM, "String", exp);
   }
   
   /**
    * @return <code>true</code> if it is legacy location scheme.
    */
   public boolean isLegacy()
   {
      List<String> params = m_clonedScheme.getParameterNames();
      return !(params.size() == 1
            && (EXPRESSION_PARAM.equalsIgnoreCase(params.get(0))) 
            && JEXL_GENERATOR.equalsIgnoreCase(getGenerator()));
   }
   
   /**
    * @return the name of all Content Types, never <code>null</code>, but
    *    may be empty.
    */
   public SelectItem[] getContentTypes()
   {
      List<SelectItem> ctNames = new ArrayList<SelectItem>();
      Map<IPSGuid, String> cts = m_ctxNode.catalogContentTypes();
      for (String ctName : cts.values())
      {
         ctNames.add(new SelectItem(ctName));
      }
      
      Collections.sort(ctNames, new Comparator<SelectItem>()
      {
         public int compare(SelectItem o1, SelectItem o2)
         {
            return o1.getLabel().compareToIgnoreCase(o2.getLabel());
         }
      });

      return ctNames.toArray(new SelectItem[ctNames.size()]);
   }

   /**
    * @return the name of the Content Type, may be <code>null</code> or empty.
    */
   public String getContentType()
   {
      Long ctId = m_clonedScheme.getContentTypeId();
      if (ctId == null)
         return null;
      
      IPSGuid ctGuid = PSGuidUtils.makeGuid(ctId, PSTypeEnum.NODEDEF);
      return m_ctxNode.catalogContentTypes().get(ctGuid);
   }

   /**
    * Set the Content Type of the Location Scheme from a given Content Type
    * name.
    * 
    * @param ctName the name of the Content Type, never <code>null</code> or
    *    empty.
    */
   public void setContentType(String ctName)
   {
      if (ctName == null || ctName.trim().length() == 0)
         throw new IllegalArgumentException(
               "ctName may not be null or empty");

      for (Map.Entry<IPSGuid, String> entry : m_ctxNode.catalogContentTypes()
            .entrySet())
      {
         if (entry.getValue().equalsIgnoreCase(ctName))
         {
            IPSGuid ctId = entry.getKey();
            m_clonedScheme.setContentTypeId(ctId.longValue());
         }
      }
   }

   /**
    * Content Type changed event listener. It is called by the JFS framework
    * whenever user select different Content Type.
    * 
    * @param event the change event object.
    */
   public void contentTypeChanged(ValueChangeEvent event)
   {
      FacesContext ctx = FacesContext.getCurrentInstance();
      
      // always set the new Content Type here. This is because the new
      // Content Type may not be set if one of other required fields
      // (such as, name or JEXL Expression) failed its validation.
      String newContentType = (String) event.getNewValue();
      setContentType(newContentType);
      
      // flush this cached Content Type object.  
      m_nodeDef = null; 

      // reset the template choice 
      UIComponent c = ctx.getViewRoot().findComponent("templateName");
      ((CoreSelectOneChoice) c).resetValue(); 
   }
   
   /**
    * Get the template name.
    * @return template name, may be <code>null</code> or empty.
    */
   public String getTemplate()
   {
      if (m_clonedScheme.getTemplateId() == null)
         return null;
      
      IPSGuid id = PSGuidUtils.makeGuid(m_clonedScheme.getTemplateId(),
            PSTypeEnum.TEMPLATE);
      
      return m_ctxNode.catalogTemplates().get(id);
   }

   /**
    * @return the edited Location Scheme instance, never <code>null</code>
    */
   IPSLocationScheme getEditedScheme()
   {
      if (m_clonedScheme == null)
         throw new IllegalStateException("m_cloneScheme must not be null.");
      
      return m_clonedScheme;
   }
   
   /**
    * Set the template name.
    * @param tpName the template name, never <code>null</code> or empty.
    */
   public void setTemplate(String tpName)
   {
      if (tpName == null || tpName.trim().length() == 0)
         throw new IllegalArgumentException("tpName may not be null or empty");
      
      for (Map.Entry<IPSGuid, String> entry : m_ctxNode.catalogTemplates()
            .entrySet())
      {
         if (entry.getValue().equalsIgnoreCase(tpName))
         {
            m_clonedScheme.setTemplateId(entry.getKey().longValue());
            break;
         }
      }
   }
   
   /**
    * @return the name of all Content Types, never <code>null</code>, but
    *    may be empty.
    */
   public SelectItem[] getTemplates()
   {
      List<String> tpNames = getTemplateList();

      // sort the list in ascending order
      Collections.sort(tpNames, new Comparator<String>()
      {
         public int compare(String o1, String o2)
         {
            return o1.compareToIgnoreCase(o2);
         }
      });

      // convert to UI specific components
      SelectItem[] retval = new SelectItem[tpNames.size()];
      for (int i=0; i<tpNames.size(); i++)
         retval[i] = new SelectItem(tpNames.get(i));
      return retval;
   }
   
   /**
    * @return a list of Template names which are registered for the 
    *    Content Type of the Location Scheme and these Templates are not
    *    used by other Location Schemes (in the current Context). Never 
    *    <code>null</code>, may be empty if the Content Type is unknown.
    */
   @SuppressWarnings("unchecked")
   private List<String> getTemplateList()
   {
      List<IPSGuid> usedTemplateIds = getUsedTemplateIds(m_clonedScheme
            .getContentTypeId());
      
      IPSNodeDefinition ct = getCachedNodeDef();
      
      List<String> tpNames = new ArrayList<String>();
      if (ct == null)
         return Collections.EMPTY_LIST;
      
      Map<IPSGuid, String> tps = m_ctxNode.catalogTemplates();
      for (IPSGuid tpId : ct.getVariantGuids())
      {
         if (!usedTemplateIds.contains(tpId))
         {
            String name = tps.get(tpId);
            if (name != null)
               tpNames.add(name);
         }
      }
      
      // gather templates used by other Location Schemes
      return tpNames;
   }
   
   /**
    * Get a list of Template IDs which are registered by the given 
    * Content Type and they are also used by other sibling Location Schemes,
    * not including the edited Location Scheme.
    * 
    * @param ctId the ID of the Content Type, assumed not <code>null</code>.
    * 
    * @return the list of Template IDs, never <code>null</code>, may be empty.
    */
   private List<IPSGuid> getUsedTemplateIds(Long ctId)
   {
      List<IPSGuid> tpIds = new ArrayList<IPSGuid>();
      for (PSLocationSchemeWrapper wrapper : m_ctxNode.getLocationSchemes())
      {
         if (wrapper != m_srcScheme &&
               ctId.equals(wrapper.getScheme().getContentTypeId()))
         {
            tpIds.add(PSGuidUtils.makeGuid(wrapper.getScheme()
                  .getTemplateId(), PSTypeEnum.TEMPLATE));
         }                  
      }
      return tpIds;
   }
   
   /**
    * @return the Node Definition of the {@link #m_clonedScheme}.
    */
   private IPSNodeDefinition getCachedNodeDef()
   {
      if (m_nodeDef != null)
         return m_nodeDef;
      
      if (m_clonedScheme.getContentTypeId() == null)
         return null;
      
      IPSGuid ctId = PSGuidUtils.makeGuid(m_clonedScheme.getContentTypeId(),
            PSTypeEnum.NODEDEF);

      m_nodeDef = m_ctxNode.getNodeDef(ctId);
      return m_nodeDef;
   }

   /**
    * @return a list of Extension names, which implemented 
    *    {@link com.percussion.extension.IPSAssemblyLocation}, never 
    *    <code>null</code>, but may be empty.
    */
   @SuppressWarnings("unchecked")
   public SelectItem[] getGenerators()
   {
      PSExtensionManager mgr = (PSExtensionManager) PSServer
            .getExtensionManager(null);
      Iterator iterator;
      try
      {
         iterator = mgr.getExtensionNames(null, null,
               "com.percussion.extension.IPSAssemblyLocation", null);
      }
      catch (PSExtensionException e)
      {
         return new SelectItem[0];
         
      }

      List<SelectItem> extNames = new ArrayList<SelectItem>();
      while (iterator.hasNext())
      {
         PSExtensionRef exit = (PSExtensionRef) iterator.next();
         // skip JEXL generator, which should not be used by legacy 
         if (! JEXL_GENERATOR.equalsIgnoreCase(exit.getFQN()))
         {
            extNames.add(new SelectItem(exit.getFQN(), 
                  exit.getExtensionName()));
         }
      }
      SelectItem[] retval = new SelectItem[extNames.size()];
      return extNames.toArray(retval);
   }

   /**
    * See {@link IPSLocationScheme#setGenerator(String)} for detail. 
    */
   public void setGenerator(String gen)
   {
      m_clonedScheme.setGenerator(gen);
   }
   
   /**
    * See {@link IPSLocationScheme#getGenerator()} for detail. 
    */
   public String getGenerator()
   {
      return m_clonedScheme.getGenerator();
   }
   
   /**
    * Have done editing current Location Scheme, navigate to Context Editor.
    * @return the outcome of the Context Editor.
    */
   public String done()
   {
      m_srcScheme.getScheme().copy(m_clonedScheme);      
      if (isLegacy())
      {
         setParameters(m_srcScheme.getScheme(), m_parameters);
      }
      if (m_isCreated)
      {
         m_ctxNode.addScheme(m_srcScheme);
      }
      m_srcScheme.setModified(true);
      
      return PSContextNode.DONE_OUTCOME;
   }
   
   /**
    * @return the outcome of the editor.
    */
   public String perform()
   {
      if (isLegacy())
         return LEGACY_SCHEME_EDITOR;
      else
         return SCHEME_EDITOR;
   }
   
   /**
    * Set the parameters of the given Location Scheme.
    * 
    * @param scheme the Location Scheme, assumed not <code>null</code>.
    * @param ps the new list of parameters, assumed not <code>null</code>,
    *    but may be empty.
    */
   private void setParameters(IPSLocationScheme scheme, List<SchemeParam> ps)
   {
      List<String> pnames = scheme.getParameterNames();
      
      // merge parameters
      int i = 0;
      for (SchemeParam p : ps)
      {
         scheme.addParameter(p.mi_name, i++, p.mi_type, p.mi_value);
         pnames.remove(p.mi_name);
      }
      
      // remove remaining ones
      for (String name : pnames)
      {
         scheme.removeParameter(name);
      }
   }
   
   /**
    * Cancel the Location Scheme editor and navigate to Context Editor.
    * @return the outcome of the Context Editor.
    */
   public String cancel()
   {
      return PSContextNode.DONE_OUTCOME;
   }
 
   /**
    * @return the backing bean for the test panel, never <code>null</code>.
    */
   public PSSchemeJexlTestPanel getJexlTestPanel()
   {
      return m_testPanel;
   }
   
   /**
    * Get the actual help file name for the Location Scheme Editor page.
    * 
    * @return  the help file name, never <code>null</code> or empty.
    */
   public String getHelpFile()
   {
      String topic = isLegacy() ? "LocationSchemeLegacyEditor"
            : "LocationSchemeEditor";
      return PSHelpTopicMapping.getFileName(topic);   
   }
   
   /**
    * The place holder for the Test Panel.
    */
   private PSSchemeJexlTestPanel m_testPanel = new PSSchemeJexlTestPanel(this);
   
   /**
    * The class log.
    */
   private final static Log ms_log = LogFactory.getLog(PSLocationSchemeEditor.class);
   
}

