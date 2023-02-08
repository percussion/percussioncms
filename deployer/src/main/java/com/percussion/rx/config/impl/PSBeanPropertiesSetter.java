/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.rx.config.impl;

import com.percussion.rx.config.IPSBeanProperties;
import com.percussion.rx.config.IPSConfigHandler.ObjectState;
import com.percussion.rx.config.PSBeanPropertiesLocator;
import com.percussion.rx.design.IPSAssociationSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides a way to set arbitrary properties that are managed by
 * {@link IPSBeanProperties}. This setter will override the existing properties
 * or append new properties to the {@link IPSBeanProperties} instance. 
 *
 * @author YuBingChen
 */
public class PSBeanPropertiesSetter extends PSSimplePropertySetter
{
   @Override
   public boolean applyProperties(Object obj, ObjectState state,
         List<IPSAssociationSet> aSets)
   {
      m_configProps.clear();
      
      if (!super.applyProperties(obj, state, aSets))
         return false;

      if (!m_configProps.isEmpty())
      {
         m_propsMgr.save(m_configProps);
         return true;
      }
      else
      {
         return false;
      }
   }
   
   @Override
   protected boolean applyProperty(@SuppressWarnings("unused")
   Object obj, @SuppressWarnings("unused")
   ObjectState state, @SuppressWarnings("unused")
   List<IPSAssociationSet> aSets, String propName, Object propValue)
      throws Exception
   {
      m_configProps.put(propName, propValue);
      return true;
   }
   
   /*
    * //see base class method for details
    */
   @SuppressWarnings({ "unchecked", "cast" })
   @Override
   protected boolean addPropertyDefs(@SuppressWarnings("unused")
   Object obj, String propName, Object pvalue, Map<String, Object> defs)
   {
      if (defs == null)
         throw new IllegalArgumentException("defs may not be null.");

      if (pvalue instanceof String)
      {
         super.addPropertyDefs((String)pvalue, null, defs);
      }
      else if (pvalue instanceof List)
      {
         addFixmePropertyDefsForList(propName, (List)pvalue, defs);
      }
      else if (pvalue instanceof Map)
      {
         addPropertyDefsForMap(propName, pvalue, null, defs);
      }
      return true;
   }
   
   
   /**
    * The bean property manager.
    */
   IPSBeanProperties m_propsMgr = PSBeanPropertiesLocator.getBeanProperties();
   
   /**
    * The place-holder to collect all defined properties while executing
    * {@link #applyProperty(Object, ObjectState, List, String, Object)}.
    */
   Map<String, Object> m_configProps = new HashMap<>();
}
