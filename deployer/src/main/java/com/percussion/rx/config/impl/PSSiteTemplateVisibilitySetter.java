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
package com.percussion.rx.config.impl;

import com.percussion.rx.config.IPSConfigHandler.ObjectState;
import com.percussion.rx.config.PSConfigException;
import com.percussion.rx.config.PSConfigValidation;
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.rx.design.IPSDesignModel;
import com.percussion.rx.design.IPSDesignModelFactory;
import com.percussion.rx.design.PSDesignModelFactoryLocator;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.sitemgr.IPSSite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PSSiteTemplateVisibilitySetter extends PSPropertySetterWithValidation
{
   @SuppressWarnings("unchecked")
   @Override
   protected boolean applyProperty(Object obj, ObjectState state,
         @SuppressWarnings("unused")
         List<IPSAssociationSet> aSets, String propName, Object propValue)
      throws Exception
   {
      return applyToSite(getSite(obj, propName), state, propValue);
   }

   /*
    * //see base class method for details
    */
   @Override
   protected boolean addPropertyDefs(Object obj, String propName,
         Object pvalue, Map<String, Object> defs)
   {
      if (super.addPropertyDefs(obj, propName, pvalue, defs))
         return true;
      
      if (VISIBILITY.equals(propName))
      {
         addFixmePropertyDefsForList(propName, pvalue, defs);
      }
      return true;
   }

   /*
    * //see base class method for details
    */
   @Override
   protected Object getPropertyValue(Object obj, String propName)
   {
      if (VISIBILITY.equals(propName))
      {
         IPSSite site = getSite(obj, propName);
         List<String> templates = new ArrayList<String>();
         for (IPSAssemblyTemplate t : site.getAssociatedTemplates())
         {
            templates.add(t.getName());
         }
         return templates;
      }
      
      return super.getPropertyValue(obj, propName);
   }   


   /**
    * Validates the specified object and property name.
    * 
    * @param obj the site object in question, it must be an instance of 
    * {@link IPSSite}.
    * @param propName the property name, it must be {@link #VISIBILITY}.
    * 
    * @return the site object, never <code>null</code>.
    */
   private IPSSite getSite(Object obj, String propName)
   {
      // validate the arguments.
      if (!(obj instanceof IPSSite))         
      {
         throw new PSConfigException("obj must be an instance of IPSSite.");
      }
      if (!VISIBILITY.equals(propName))
      {
         throw new PSConfigException("Unknow property name, \"" + propName
               + "\".");
      }
      
      return (IPSSite) obj;
   }
   
   @Override
   protected boolean deApplyProperty(Object obj,
         @SuppressWarnings("unused")
         List<IPSAssociationSet> aSets, String propName, Object propValue)
      throws Exception
   {
      IPSSite site = getSite(obj, propName);
      Collection<String> curList = convertObjectToList(propValue);
      
      if (curList.isEmpty())
         return false;
      
      mergeOrRemoveTemplates(site, curList, true);
      return true;
   }
   
   @Override
   protected List<PSConfigValidation> validate(String objName, ObjectState state,
         String propName, Object propValue, Object otherValue)
   {
      if (!VISIBILITY.equals(propName))
         return super.validate(objName, state, propName, propValue, otherValue);
      
      Collection<String> curList = convertObjectToList(propValue);
      Collection<String> otherList = convertObjectToList(otherValue);
      if (curList.isEmpty() || otherList.isEmpty())
         return Collections.emptyList();

      Collection<String> commons = new ArrayList<String>();
      commons.addAll(curList);
      commons.retainAll(otherList);
      if (commons.isEmpty())
         return Collections.emptyList();
      
      PSConfigValidation vError;
      String msg = " Site \"" + objName + "\" associates with Templates \""
            + curList.toString()
            + "\" is already configured.";
      vError = new PSConfigValidation(objName, VISIBILITY, true, msg);
      return Collections.singletonList(vError);
   }
   

   /**
    * Apply the given property value to the specific site.
    * 
    * @param site the Site object, assumed not <code>null</code>.
    * @param state the state of the site, assumed not <code>null</code>.
    * @param propValue the property value, may be <code>null</code>.
    * 
    * @return <code>true</code> if the Site was modified by this method.
    * 
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   private boolean applyToSite(IPSSite site, ObjectState state,
         Object propValue) throws Exception
   {
      Collection<String> curList = convertObjectToList(propValue);
      Collection<String> prevList = getPrevTemplates();
      
      if (curList.isEmpty() && prevList.isEmpty())
         return false;
      
      if (state.equals(ObjectState.PREVIOUS))
      {
         mergeOrRemoveTemplates(site, prevList, true);
         return true;
      }
      
      List<String> templates = new ArrayList<String>();
      templates.addAll(prevList);
      templates.removeAll(curList);
      mergeOrRemoveTemplates(site, templates, true);
      
      mergeOrRemoveTemplates(site, curList, false);
      
      return true;
   }
   
   /**
    * Converts the specified object to a list of string.
    * 
    * @param propValue the object in question, it may be <code>null</code>.
    * 
    * @return the converted list, never <code>null</code>, may be empty.
    */
   @SuppressWarnings("unchecked")
   private Collection<String> convertObjectToList(Object propValue)
   {
      if (propValue == null)
         return Collections.emptyList();

      IPSDesignModelFactory dmFactory = PSDesignModelFactoryLocator
            .getDesignModelFactory();
      IPSDesignModel model = dmFactory.getDesignModel(PSTypeEnum.TEMPLATE);
      return PSConfigUtils.getObjectNames(propValue, model, VISIBILITY);
   }
   
   /**
    * Gets the template list specified in previous properties.
    * 
    * @return the template list, never <code>null</code>, but may be empty.
    */
   private Collection<String> getPrevTemplates()
   {
      Map<String, Object> props = getPrevProperties();
      if (props == null)
         return Collections.emptyList();
      
      return convertObjectToList(props.get(VISIBILITY));
   }
   
   /**
    * Merge or remove a list of Templates from the specified Site.
    * 
    * @param site the Site in question, assumed not <code>null</code>.
    * @param tgtNames the list of name of the Templates that will be merged or
    * removed from the site, assumed not <code>null</code> or empty. 
    * @param isRemove <code>true</code> if remove the above Templates from the
    * Site; otherwise, merge the Templates into the Site association.
    * 
    * @return <code>true</code> if have done merge or remove operation.
    */
   private boolean mergeOrRemoveTemplates(IPSSite site,
         Collection<String> tgtNames, boolean isRemove)
   {
      boolean isModified = false;
      
      IPSAssemblyService srv = PSAssemblyServiceLocator.getAssemblyService();
      for (String name : tgtNames)
      {
         IPSAssemblyTemplate t = getNamedTemplate(name, site
               .getAssociatedTemplates());
         if (t != null)
         {
            if (isRemove)
            {
               site.getAssociatedTemplates().remove(t);
               isModified = true;
            }
            continue;
         }
         else if (isRemove)
         {
            continue;
         }

         // add the template.
         t = getTemplate(srv, name);
         if (t == null)
            continue;
         
         site.getAssociatedTemplates().add(t);
         isModified = true;
      }
      return isModified;
   }

   /**
    * Gets the template with the specified name from a list of templates.
    * 
    * @param name the lookup name, assumed not <code>null</code> or empty.
    * @param templates the list of templates, assumed not <code>null</code>,
    * but may be empty.
    * 
    * @return the template with the name. It may be <code>null</code> if cannot
    * find one.
    */
   private IPSAssemblyTemplate getNamedTemplate(String name,
         Collection<IPSAssemblyTemplate> templates)
   {
      for (IPSAssemblyTemplate t : templates)
      {
         if (t.getName().equalsIgnoreCase(name))
            return t;
      }
      return null;
   }
   
   /**
    * Finds a template with the specified name.
    * 
    * @param srv the service to retrieve the template, assumed not 
    * <code>null</code>.
    * @param name the template name, assumed not <code>null</code>.
    * 
    * @return the template with the name. It may be <code>null</code> if cannot
    * find the template.
    */
   private IPSAssemblyTemplate getTemplate(IPSAssemblyService srv, String name)
   {
      try
      {
         IPSAssemblyTemplate t = srv.findTemplateByName(name);
         return t;
      }
      catch (Exception e)
      {
         ms_log.error("Failed to load Template \"" + name + "\".", e);
      }
      return null;
   }
   
   /**
    * The property name for this setter.
    */
   public static final String VISIBILITY = "templateVisibility";
   
   /**
    * The logger of the setter.
    */
   private static Log ms_log = LogFactory
         .getLog("PSSiteTemplateVisibilitySetter");

}
