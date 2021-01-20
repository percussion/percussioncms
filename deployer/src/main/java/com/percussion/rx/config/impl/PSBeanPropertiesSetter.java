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
   Map<String, Object> m_configProps = new HashMap<String, Object>();
}
