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

import com.percussion.rx.config.IPSConfigHandler.ObjectState;
import com.percussion.rx.config.PSConfigException;
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.IPSFilterService;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.filter.PSFilterServiceLocator;
import com.percussion.services.publisher.IPSContentList;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSUrlUtils;
import com.percussion.utils.guid.IPSGuid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The setter for configuring the properties of {@link IPSContentList} object.
 *
 * @author YuBingChen
 */
public class PSContentListSetter extends PSSimplePropertySetter
{
   @Override
   protected boolean applyProperty(Object obj, ObjectState state,
         List<IPSAssociationSet> aSets, String propName, Object propValue)
      throws Exception
   {
      if (! (obj instanceof IPSContentList))
         throw new IllegalArgumentException("obj type must be IPSContentList.");
 
      IPSContentList cList = (IPSContentList) obj;
      if (DELIVERY_TYPE.equals(propName))
      {
         setDeliveryType(cList, propValue);
      }
      else if (EXPANDER_PARAMS.equals(propName))
      {
         setExpanderParams(cList, propValue);
      }
      else if (GEN_PARAMS.equals(propName))
      {
         setGeneratorParams(cList, propValue);
      }
      else if (FILTER.equals(propName))
      {
         setFilter(cList, propValue);
      }
      else
      {
         super.applyProperty(cList, state, aSets, propName, propValue);
      }
      return true;
   }

   /*
    * //see base class method for details
    */
   @Override
   protected boolean addPropertyDefs(Object obj, String propName,
         Object pvalue, Map<String, Object> defs) throws PSNotFoundException {
      if (super.addPropertyDefs(obj, propName, pvalue, defs))
         return true;
      
      IPSContentList cList = (IPSContentList) obj;
      if (EXPANDER_PARAMS.equals(propName))
      {
         Map<String, Object> params = new HashMap<>();
         params.putAll(cList.getExpanderParams());
         addPropertyDefsForMap(propName, pvalue, params, defs);
      }
      else if (GEN_PARAMS.equals(propName))
      {
         Map<String, Object> params = new HashMap<>();
         params.putAll(cList.getGeneratorParams());
         addPropertyDefsForMap(propName, pvalue, params, defs);
      }
      return true;
   }

   /*
    * //see base class method for details
    */
   @Override
   protected Object getPropertyValue(Object obj, String propName) throws PSNotFoundException {
      IPSContentList cList = (IPSContentList) obj;
      if (DELIVERY_TYPE.equals(propName))
      {
         String url = cList.getUrl();
         return PSUrlUtils.getUrlParameterValue(url,
               IPSHtmlParameters.SYS_DELIVERYTYPE);
      }
      else if (EXPANDER_PARAMS.equals(propName))
      {
         return cList.getExpanderParams();
      }
      else if (GEN_PARAMS.equals(propName))
      {
         return cList.getGeneratorParams();
      }
      else if (FILTER.equals(propName))
      {
         IPSFilterService srv = PSFilterServiceLocator.getFilterService();      
         IPSItemFilter filter = srv.findFilterByID(cList.getFilterId());
         return filter == null ? null : filter.getName(); 
      }
      
      return super.getPropertyValue(obj, propName);
   }

   /**
    * Sets the {@link #DELIVERY_TYPE} property.
    * 
    * @param cList the Content List, assumed not <code>null</code>.
    * @param value the value of the new property, assumed not <code>null</code>.
    */
   private void setDeliveryType(IPSContentList cList, Object value) throws PSNotFoundException {
      // make sure the delivery type name exists
      String deliveryName = value.toString();
      IPSPublisherService srv = PSPublisherServiceLocator.getPublisherService();
      srv.loadDeliveryType(deliveryName);

      // set the property
      String url = cList.getUrl();
      url = PSUrlUtils.replaceUrlParameterValue(url,
            IPSHtmlParameters.SYS_DELIVERYTYPE, deliveryName);
      cList.setUrl(url);
   }
   
   /**
    * Sets the {@link #EXPANDER_PARAMS} property.
    * 
    * @param cList the Content List, assumed not <code>null</code>.
    * @param value the value of the new property, assumed not <code>null</code>,
    * it must be a Map object.
    */
   @SuppressWarnings("unchecked")
   private void setExpanderParams(IPSContentList cList, Object value)
   {
      if (!(value instanceof Map))
         throw new PSConfigException("The type of property \""
               + EXPANDER_PARAMS + "\" must be a Map.");
      
      cList.setExpanderParams((Map<String, String>)value);
   }
   
   /**
    * Sets the {@link #GEN_PARAMS} property.
    * 
    * @param cList the Content List, assumed not <code>null</code>.
    * @param value the value of the new property, assumed not <code>null</code>,
    * it must be a Map object.
    */
   @SuppressWarnings("unchecked")
   private void setGeneratorParams(IPSContentList cList, Object value)
   {
      if (!(value instanceof Map))
         throw new PSConfigException("The type of property \""
               + GEN_PARAMS + "\" must be a Map.");
      
      cList.setGeneratorParams((Map<String, String>)value);
   }
   
   /**
    * Sets the {@link #FILTER} property.
    * 
    * @param cList the Content List, assumed not <code>null</code>.
    * @param value the item filter name, assumed not <code>null</code>,
    * it must be an existing filter name.
    */
   private void setFilter(IPSContentList cList, Object value)
      throws PSFilterException
   {
      IPSFilterService srv = PSFilterServiceLocator.getFilterService();      
      IPSGuid id = srv.findFilterByName((String)value).getGUID();
      cList.setFilterId(id);
   }
   
   /**
    * The property name of the expander parameters. The value of the property 
    * type is expected to be a {@link Map}
    */
   public static final String EXPANDER_PARAMS = "expanderParams";
   
   /**
    * The property name of the generator parameters. The value of the property 
    * type is expected to be a {@link Map}
    */
   public static final String GEN_PARAMS = "generatorParams";
   
   /**
    * The property name of a delivery type (name). The value of the property 
    * type is expected to be a {@link String}
    */
   public static final String DELIVERY_TYPE = "deliveryType";
   
   /**
    * The property name of an item filter (name). The value of the property type
    * is expected to be a {@link String}
    */
   public static final String FILTER = "filter";
   
}
