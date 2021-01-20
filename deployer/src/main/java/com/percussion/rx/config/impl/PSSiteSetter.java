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
import com.percussion.services.sitemgr.IPSPublishingContext;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This setter is used to set Site properties.
 *
 * @author YuBingChen
 */
public class PSSiteSetter extends PSPropertySetterWithValidation
{
   @SuppressWarnings("unchecked")
   @Override
   protected boolean applyProperty(Object obj, ObjectState state,
         @SuppressWarnings("unused")
         List<IPSAssociationSet> aSets, String propName, Object propValue)
      throws Exception
   {
      // validate the arguments.
      if (!(obj instanceof IPSSite))         
      {
         throw new PSConfigException("obj must be an instance of IPSSite.");
      }
      IPSSite site = (IPSSite) obj;
      if (ms_propNameMap.get(propName) != null)
      {
         super.applyProperty(site, state, aSets, ms_propNameMap.get(propName),
               propValue);
      }
      else if (VARIABLES.equals(propName))
      {
         applySiteVariables(site, state, propValue);
      }
      else
      {
         super.applyProperty(site, state, aSets, propName, propValue);
      }
      return true;
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
      
      if (VARIABLES.equals(propName))
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
      if (!(obj instanceof IPSSite))
         throw new PSConfigException("obj must be an instance of IPSSite.");

      if (VARIABLES.equals(propName))
      {
         IPSSite site = (IPSSite) obj;
         IPSSiteManager mgr = PSSiteManagerLocator.getSiteManager();
         
         List<Map<String, String>> result = new ArrayList<Map<String, String>>();
         for (IPSPublishingContext ctx : mgr.findAllContexts())
         {
            for (String pname : site.getPropertyNames(ctx.getGUID()))
            {
               Map<String, String> sp = new HashMap<String, String>();
               String value = site.getProperty(pname, ctx.getGUID());
               sp.put(NAME, pname);
               sp.put(CONTEXT, ctx.getName());
               sp.put(VALUE, value);
               result.add(sp);
            }
         }
         return result;
      }
      
      return super.getPropertyValue(obj, propName);
   }   

   /*
    * //see base class method for details
    */
   @Override
   protected List<PSConfigValidation> validate(String objName, ObjectState state,
         String propName, Object propValue, Object otherValue)
   {
      if (!VARIABLES.equals(propName))
         return super.validate(objName, state, propName, propValue, otherValue);
      
      List<Map<String, Object>> myVars = convertObjectToMaps(propValue);
      if (myVars.isEmpty() || state.equals(ObjectState.PREVIOUS))
         return Collections.emptyList();

      List<Map<String, Object>> otherVars = convertObjectToMaps(otherValue);
      if (otherVars.isEmpty())
         return Collections.emptyList();

      PSConfigValidation vError;
      List<PSConfigValidation> result = new ArrayList<PSConfigValidation>();
      for (Map<String, Object> var : myVars)
      {
         PSPair<String, IPSGuid> pair = getSiteVariableNameCtx(var);
         String myVarName = pair.getFirst();
         if (StringUtils.isBlank(myVarName))
            continue;
         for (Map<String, Object> other : otherVars)
         {
            pair = getSiteVariableNameCtx(other);
            if (myVarName.equalsIgnoreCase(pair.getFirst()))
            {
               String msg = " the Site Variable \"" + myVarName
                     + "\" is already configured.";
               vError = new PSConfigValidation(objName, VARIABLES, true, msg);
               result.add(vError);
            }
         }
      }
      return result;
   }
   
   /*
    * //see base class method for details
    */
   @Override
   protected boolean deApplyProperty(Object obj, @SuppressWarnings("unused")
   List<IPSAssociationSet> aSets, String propName, Object propValue)
      throws Exception
   {
      if (!(obj instanceof IPSSite))
      {
         throw new PSConfigException("obj must be an instance of IPSSite.");
      }
      IPSSite site = (IPSSite) obj;
      if (VARIABLES.equals(propName))
      {
         return deleteSiteVariables(site, convertObjectToMaps(propValue));
      }
      return false;
   }
   
   /**
    * Apply the supplied a specified Site Variable to a Site.
    * 
    * @param site the Site that need to apply the Site Variable to, assumed not
    * <code>null</code> or empty.
    * @param state the state of the Site, assumed not <code>null</code>.
    * @param propValue the value of the site variable.
    * 
    * @return <code>true</code> if the object has been modified.
    */
   @SuppressWarnings("unchecked")
   private boolean applySiteVariables(IPSSite site, ObjectState state,
         Object propValue)
   {
      if (state.equals(ObjectState.PREVIOUS))
      {
         return deleteSiteVariables(site, getPrevSiteVariables());
      }
      else if (state.equals(ObjectState.CURRENT))
      {
         return mergeSiteVariables(site, propValue);
      }
      else // ObjectState.BOTH
      {
         return mergeAndDeleteSiteVariables(site, propValue);
      }
   }

   /**
    * Merges current Site Variables into the given Site, and removes the
    * Site Variables defined in previous configuration, but not in current
    * properties.
    * 
    * @param site the Site to merge the variable into, assumed not 
    * <code>null</code>.
    * @param props the map contains 0 or more merged Site Variables, 
    * it may be <code>null</code> or empty if there is nothing to merge.
    * 
    * @return <code>true</code> if the object has been modified.
    */
   @SuppressWarnings("unchecked")
   private boolean mergeAndDeleteSiteVariables(IPSSite site, Object propValue)
   {
      boolean isChanged = mergeSiteVariables(site, propValue);
      List<Map<String, Object>> prevVars = getPrevSiteVariables();
      if (prevVars.isEmpty())
         return isChanged;

      // collect variables in previous, but not in current
      List<Map<String, Object>> curVars = convertObjectToMaps(propValue);
      List<Map<String, Object>> deletedVars = new ArrayList<Map<String, Object>>();
      for (Map<String, Object> var : prevVars)
      {
         boolean found = false;
         String vname = getSiteVariableNameCtx(var).getFirst();
         for (Map<String, Object> curVar : curVars)
         {
            String curName = getSiteVariableNameCtx(curVar).getFirst();
            if (vname.equalsIgnoreCase(curName))
            {
               found = true;
               continue;
            }
         }
         if (!found)
            deletedVars.add(var);
      }
      // remove the collected variables
      return deleteSiteVariables(site, deletedVars) ? true : isChanged;
   }
   
   /**
    * Converts the given object to a list of maps.
    * 
    * @param propValue the object in question, may be <code>null</code>.
    * 
    * @return the converted list of maps, never <code>null</code>, may be empty.
    */
   @SuppressWarnings("unchecked")
   private List<Map<String, Object>> convertObjectToMaps(Object propValue)
   {
      if (propValue == null)
         return Collections.emptyList();

      if (!(propValue instanceof List))
         throw new PSConfigException("The type of property \"" + VARIABLES
               + "\" of the Site Variable Setter must be List.");
      
      return (List<Map<String, Object>>) propValue;
   }
   
   /**
    * Gets the {@link #VARIABLES} property value.
    * 
    * @return the property value, may be <code>null</code> or empty if it is
    * undefined.
    */
   @SuppressWarnings("unchecked")
   private List<Map<String, Object>> getPrevSiteVariables()
   {
      Map<String, Object> props = getPrevProperties();
      if (props == null || props.isEmpty())
         return Collections.emptyList();
      
      return convertObjectToMaps(props.get(VARIABLES));
   }
   
   /**
    * Deletes the Site Variables that were applied in previous configuration.
    * 
    * @param site the Site object with state as {@link ObjectState#PREVIOUS}.
    * @param props the Site Variables were applied in previous configuration.
    * 
    * @return <code>true</code> if the object has been modified.
    */
   @SuppressWarnings("unchecked")
   private boolean deleteSiteVariables(IPSSite site,
         List<Map<String, Object>> vars)
   {
      if (vars.isEmpty())
         return false;
      
      for (Map<String, Object> var : vars)
      {
         deleteSiteVariable(site, var);
      }
      return true;
   }

   /**
    * Merges a list of Site Variables into the given Site.
    * 
    * @param site the Site to merge the variable into, assumed not 
    * <code>null</code>.
    * @param props the map contains 0 or more merged Site Variables, 
    * it may be <code>null</code> or empty if there is nothing to merge.
    * 
    * @return <code>true</code> if the object has been modified.
    */
   @SuppressWarnings("unchecked")
   private boolean mergeSiteVariables(IPSSite site, Object propValue)
   {
      // apply the property
      List<Map<String, Object>> vars = convertObjectToMaps(propValue);
      if (vars.isEmpty())
         return false;
      
      for (Map<String, Object> var : vars)
      {
         mergeSiteVariable(site, var);
      }
      return true;
   }

   /**
    * Merges the one Site Variable into the given Site.
    * 
    * @param site the Site to merge the variable into, assumed not 
    * <code>null</code>.
    * @param props the map contains the merged Site Variable properties, 
    * assumed not <code>null</code> or empty.
    */
   private void mergeSiteVariable(IPSSite site, Map<String, Object> props)
   {
      if (props == null || props.isEmpty())
         throw new PSConfigException(
               "Properties of Site Variable cannot be null or empty.");

      PSPair<String, IPSGuid> pair = getSiteVariableNameCtx(props);
      site.setProperty(pair.getFirst(), pair.getSecond(), (String) props
            .get(VALUE));
   }

   /**
    * Deletes the supplied Site Variable for the given Site.
    * 
    * @param site the Site to delete the variable from, assumed not 
    * <code>null</code>.
    * @param props the map contains the merged Site Variable properties, 
    * assumed not <code>null</code> or empty.
    */
   private void deleteSiteVariable(IPSSite site, Map<String, Object> props)
   {
      if (props == null || props.isEmpty())
         throw new PSConfigException(
               "Properties of Site Variable cannot be null or empty.");

      PSPair<String, IPSGuid> pair = getSiteVariableNameCtx(props);
      site.removeProperty(pair.getFirst(), pair.getSecond());
   }

   /**
    * Gets the values of {@link #NAME} and {@link #CONTEXT} properties from the
    * given property map
    * 
    * @param props the map contains the retrieved properties, assumed not
    * <code>null</code>.
    * 
    * @return the property values in a pair, where 1st value is the name;
    * 2nd value is the context ID. Never <code>null</code>.
    */
   private PSPair<String, IPSGuid> getSiteVariableNameCtx(
         Map<String, Object> props)
   {
      String name = (String) props.get(NAME);
      if (name == null || StringUtils.isBlank(name))
         throw new PSConfigException("The property \"" + NAME
               + "\" cannot be null or empty.");

      String context = (String) props.get(CONTEXT);
      if (context == null || StringUtils.isBlank(context))
         throw new PSConfigException("The property \"" + CONTEXT
               + "\" cannot be null or empty.");

      IPSPublishingContext ctx = getSiteMgr().loadContext(context);

      return new PSPair<String, IPSGuid>(name, ctx.getGUID());
   }
   
   /**
    * Gets the Site Manager service instance.
    * 
    * @return the Site Manager service instance, never <code>null</code>.
    */
   private IPSSiteManager getSiteMgr()
   {
      if (m_siteMgr != null)
         return m_siteMgr;
      
      m_siteMgr = PSSiteManagerLocator.getSiteManager();
      return m_siteMgr;
   }

   /**
    * The cached Site Manager service instance. Default to <code>null</code>.
    */
   private IPSSiteManager m_siteMgr = null;
   
   /**
    * The name of the "name" property of the Site Variable.
    */
   public static final String NAME = "name";
   
   /**
    * The name of the "context" property of the Site Variable.
    */
   public static final String CONTEXT = "context";

   /**
    * The name of the "value" property of the Site Variable.
    */
   public static final String VALUE = "value";
   
   /**
    * The name of the property contains a collection of Site variables.
    * The expected value is a {@link Map Map&lt;String, Map&lt;String, Object>>}.
    */
   public static final String VARIABLES = "variables";

   /**
    * This maps the logic property name to an actual property name defined in
    * {@link IPSSite}.
    */
   private static final Map<String, String> ms_propNameMap = new HashMap<String, String>();
   
   static
   {
      ms_propNameMap.put("siteFolderPath", "folderRoot");
      ms_propNameMap.put("publishedPath", "root");
      ms_propNameMap.put("publishedUrl", "baseUrl");
      ms_propNameMap.put("ftpAddress", "ipAddress");
      ms_propNameMap.put("ftpPort", "port");
      ms_propNameMap.put("ftpUser", "userId");
      ms_propNameMap.put("ftpPassword", "password");
   }
}
