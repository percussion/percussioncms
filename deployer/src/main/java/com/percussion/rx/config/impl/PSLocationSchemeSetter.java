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

import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionManager;
import com.percussion.extension.PSExtensionRef;
import com.percussion.rx.config.IPSConfigHandler.ObjectState;
import com.percussion.rx.config.PSConfigException;
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.rx.publisher.jsf.nodes.PSLocationSchemeEditor;
import com.percussion.server.PSServer;
import com.percussion.services.sitemgr.IPSLocationScheme;
import com.percussion.utils.types.PSPair;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This setter is used to set Location Scheme specific properties.
 * Assumed the to be configured Location Scheme already contains valid
 * Context, Content Type and Template.
 *
 * @author YuBingChen
 */
public class PSLocationSchemeSetter extends PSSimplePropertySetter
{
   @Override
   public boolean applyProperties(Object obj, ObjectState state,
         List<IPSAssociationSet> aSets)
   {
      boolean isApplied = processGenerator(obj);
      boolean isSuperApplied = super.applyProperties(obj, state, aSets);
      validateScheme(obj);
      
      return isApplied || isSuperApplied;
   }

   /**
    * Validates the Location Scheme, make sure it contains all required 
    * properties.
    * 
    * @param obj the Location Scheme in question, assumed not <code>null</code>.
    */
   private void validateScheme(Object obj)
   {
      IPSLocationScheme scheme = (IPSLocationScheme) obj;
      if (StringUtils.isBlank(scheme.getGenerator()))
         throw new PSConfigException("Failed to configure Location Scheme \""
               + scheme.getName() + "\". This is because either \""
               + EXPRESSION + "\" or \"" + GENERATOR
               + "\" property is not defined.");
   }
   
   /**
    * Process or set the generator property if it is defined. This also
    * validates the defined properties, make sure there is confusion regarding
    * configuring a legacy or JEXL generator, but not both. 
    * 
    * @param obj the Location Scheme to be configured with, assumed not
    * <code>null</code>.
    * 
    * @return <code>true</code> if set a new generator.
    */
   private boolean processGenerator(Object obj)
   {
      // validate the arguments.
      if (!(obj instanceof IPSLocationScheme))
      {
         throw new PSConfigException("obj must be an instance of IPSLocationScheme.");
      }
      IPSLocationScheme scheme = (IPSLocationScheme) obj;
      
      Object generator = getProperties().get(GENERATOR);
      Object expr = getProperties().get(EXPRESSION);
      Object params = getProperties().get(GENERATOR_PARAMS);
      if (expr != null && generator != null)
      {
         throw new PSConfigException("Failed to configure Location Scheme \""
               + scheme.getName() + "\". This is because both \"" + EXPRESSION
               + "\" and \"" + GENERATOR
               + "\" properties cannot be specified at the same time.");
      }
      if (expr != null && params != null)
      {
         throw new PSConfigException("Failed to configure Location Scheme \""
               + scheme.getName() + "\". This is because both \"" + EXPRESSION
               + "\" and \"" + GENERATOR_PARAMS
               + "\" properties cannot be specified at the same time.");
      }
      
      if (generator == null)
         return false;
      
      PSExtensionRef ext = getExtensionRef((String) generator);
      scheme.setGenerator(ext.getFQN());
      
      return true;
   }
  
   @SuppressWarnings("unchecked")
   @Override
   protected boolean applyProperty(Object obj, ObjectState state,
         @SuppressWarnings("unused")
         List<IPSAssociationSet> aSets, String propName, Object propValue)
      throws Exception
   {
      IPSLocationScheme scheme = (IPSLocationScheme) obj;
      if (EXPRESSION.equals(propName))
      {
         setJexlExpression(scheme, propValue);
      }
      else if (GENERATOR_PARAMS.equals(propName))
      {
         setGeneratorParams(scheme, propValue);
      }
      else if (GENERATOR.equals(propName))
      {
         // this should have processed at the beginning of applyProperties
         // processGenerator()
         return true; 
      }
      else
      {
         super.applyProperty(scheme, state, aSets, propName, propValue);
      }
      return true;
   }

   /*
    * //see base class method for details
    */
   @Override
   protected Object getPropertyValue(Object obj, String propName)
   {
      if (!(obj instanceof IPSLocationScheme))
         throw new PSConfigException("obj must be an instance of IPSLocationScheme.");

      IPSLocationScheme scheme = (IPSLocationScheme) obj;
      if (EXPRESSION.equals(propName))
      {
         return scheme.getParameterValue(EXPRESSION);
      }
      else if (GENERATOR_PARAMS.equals(propName))
      {
         List<PSPair<String, String>> params = new ArrayList<PSPair<String, String>>();
         for (String n : scheme.getParameterNames())
         {
            String value = scheme.getParameterValue(n);
            params.add(new PSPair<String, String>(n, value));
         }
         return params;
      }
      
      return super.getPropertyValue(obj, propName);
   }   

   /**
    * Sets the generator with the Jexl expression. 
    * @param scheme the Location Scheme to set to, assumed not <code>null</code>
    * @param propValue the new expression, assumed not <code>null</code>.
    */
   private void setJexlExpression(IPSLocationScheme scheme, Object propValue)
   {
      if (!(propValue instanceof String))
         throw new PSConfigException(EXPRESSION + " property must be a string.");
      
      String expr = (String) propValue;
      scheme.setGenerator(PSLocationSchemeEditor.JEXL_GENERATOR);
      scheme.setParameter(EXPRESSION, "String", expr);
   }

   /**
    * Set the generator's parameters with the specified one.
    * 
    * @param scheme the Location Scheme to set the parameters to, assumed not
    * <code>null</code>.
    * @param propValue the new set of parameters. Never <code>null</code>, may
    * be empty. Expect to be a List type.
    */
   private void setGeneratorParams(IPSLocationScheme scheme, Object propValue)
   {
      List<PSPair<String, String>> params = filterParameters(scheme
            .getGenerator(), propValue);

      // cleanup existing parameters
      List<String> pnames = new ArrayList<String>();
      pnames.addAll(scheme.getParameterNames());
      for (String n : pnames)
         scheme.removeParameter(n);
      
      // add the new parameters
      for (int i=0; i < params.size(); i++)
      {
         PSPair<String, String> p = params.get(i);
         scheme.addParameter(p.getFirst(), i, "String", p.getSecond());
      }
   }
   
   /**
    * Filter the given parameter for the supplied java extension. It removes 
    * the entries of the map that the keys are not defined as the parameters
    * of the slot-filter. The slot-filter is a Java extension.  
    *  
    * @param extFQN the FQN java extension, never <code>null</code> or empty.
    * @param propValue the new parameters for the above java extension., never
    * <code>null</code>.
    * 
    * @return the filtered map, never <code>null</code>, may be empty.
    */
   @SuppressWarnings("unchecked")
   private List<PSPair<String, String>> filterParameters(String extFQN,
         Object propValue)
   {
      if (!(propValue instanceof List))
         throw new PSConfigException("\"" + GENERATOR_PARAMS
               + "\" property must be defined by pvalues, as a list of pairs.");

      List<PSPair<String, String>> props = (List<PSPair<String, String>>) propValue;
      List<PSPair<String, String>> params = new ArrayList<PSPair<String, String>>();
      List<String> names = PSConfigUtils.getExtensionParameterNames(extFQN);
      for (PSPair<String, String> p : props)
      {
         if (!names.contains(p.getFirst()))
         {
            PSExtensionRef ref = new PSExtensionRef(extFQN);
            ms_log.warn("Skip finder argument \"" + p.getFirst()
                  + "\" since it is not a parameter defined by generator \""
                  + ref.getExtensionName() + "\".");
            continue;
         }
         params.add(p);
      }
      return params;
   }
   

   
   /**
    * Gets the Extension Reference for the specified generator name.
    *  
    * @param extName the extension/generator name. This is not FQN of the
    * java extension. 
    * 
    * @return the extension reference. It may be <code>null</code> if there is
    * no such generator/exist.
    */
   @SuppressWarnings("unchecked")
   private PSExtensionRef getExtensionRef(String extName)
   {
      PSExtensionManager mgr = (PSExtensionManager) PSServer
            .getExtensionManager(null);
      try
      {
         Iterator iterator = mgr.getExtensionNames(null, null,
               "com.percussion.extension.IPSAssemblyLocation", extName);
         while (iterator.hasNext())
         {
            PSExtensionRef exit = (PSExtensionRef) iterator.next();
            return exit;
         }
      }
      catch (PSExtensionException e)
      {
         e.printStackTrace();
      }
      return null;
   }

   /**
    * The logger for this class
    */
   static Log ms_log = LogFactory.getLog("PSLocationSchemeSetter");

   /**
    * The Jexl expression property name.
    */
   public static final String EXPRESSION = PSLocationSchemeEditor.EXPRESSION_PARAM;
   
   /**
    * The generator name property. 
    */
   public static final String GENERATOR = "generator";
   
   /**
    * The generator parameter property
    */
   public static final String GENERATOR_PARAMS = "generatorParams";
}
