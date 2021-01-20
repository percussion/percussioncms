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
import com.percussion.rx.design.IPSAssociationSet.AssociationAction;
import com.percussion.rx.design.IPSAssociationSet.AssociationType;
import com.percussion.rx.design.IPSDesignModel;
import com.percussion.rx.design.IPSDesignModelFactory;
import com.percussion.rx.design.PSDesignModelFactoryLocator;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles configuring properties for {@link IPSTemplateSlot} object.
 *
 * @author YuBingChen
 */
public class PSTemplateSlotSetter extends PSPropertySetterWithValidation
{
   @Override
   protected boolean applyProperty(Object obj, ObjectState state,
         List<IPSAssociationSet> aSets, String propName, Object propValue)
      throws Exception
   {
      if (! (obj instanceof IPSTemplateSlot))
         throw new IllegalArgumentException("obj type must be IPSTemplateSlot.");
      
      IPSTemplateSlot slot = (IPSTemplateSlot) obj;
      if (SLOT_ASSOCIATION.equals(propName))
      {
         setSlotAssociations(aSets, propValue);
      }
      else if (FINDER_ARGUMENTS.equals(propName)
            || FINDER_PARAMS.equals(propName))
      {
         setFinderArguments(slot, propValue);
      }
      else
      {
         super.applyProperty(obj, state, aSets, propName, propValue);
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
      
      if (FINDER_ARGUMENTS.equals(propName) || FINDER_PARAMS.equals(propName))
      {
         IPSTemplateSlot slot = (IPSTemplateSlot) obj;
         Map<String, Object> srcMap = new HashMap<String, Object>();
         srcMap.putAll(slot.getFinderArguments());
         addPropertyDefsForMap(propName, pvalue, srcMap, defs);
      }
      return true;
   }
   
   /*
    * //see base class method for details
    */
   @Override
   protected Object getPropertyValue(Object obj, String propName)
   {
      IPSTemplateSlot slot = (IPSTemplateSlot) obj;
      if (FINDER_ARGUMENTS.equals(propName) || FINDER_PARAMS.equals(propName))
      {
         return slot.getFinderArguments();
      }
      else if (SLOT_ASSOCIATION.equals(propName))
      {
         Collection<PSPair<IPSGuid, IPSGuid>> pairs = slot.getSlotAssociations();
         List<PSPair<String, String>> assocs = new ArrayList<PSPair<String, String>>();
         IPSDesignModelFactory factory = PSDesignModelFactoryLocator
               .getDesignModelFactory();
         IPSDesignModel ctModel = factory.getDesignModel(PSTypeEnum.NODEDEF);
         IPSDesignModel tpModel = factory.getDesignModel(PSTypeEnum.TEMPLATE);
         PSPair<String, String> assoc;
         for (PSPair<IPSGuid, IPSGuid> pair : pairs)
         {
            String ctName = ctModel.guidToName(pair.getFirst());
            String tpName = tpModel.guidToName(pair.getSecond());
            assoc = new PSPair<String, String>(ctName, tpName);
            assocs.add(assoc);
         }
         return assocs;
      }
      
      return super.getPropertyValue(obj, propName);
   }   

   @Override
   protected List<PSConfigValidation> validate(String objName, ObjectState state,
         String propName, Object propValue, Object otherValue)
   {
      if (!SLOT_ASSOCIATION.equals(propName))
         return super.validate(objName, state, propName, propValue, otherValue);
      
      List<PSPair<String, String>> curAssoc = convertObjectToList(propValue);
      List<PSPair<String, String>> otherAssoc = convertObjectToList(otherValue);
      if (curAssoc.isEmpty() || otherAssoc.isEmpty())
         return Collections.emptyList();
      
      Collection<PSPair<String, String>> commons = new ArrayList<PSPair<String, String>>();
      commons.addAll(curAssoc);
      commons.retainAll(otherAssoc);
      if (commons.isEmpty())
         return Collections.emptyList();
      
      StringBuffer buffer = new StringBuffer();
      buffer
            .append("the following pairs of Content Type / Template associations are already configured: ");
      for (PSPair<String, String> pair : curAssoc)
      {
         buffer.append(" (" + pair.getFirst() + ", " + pair.getSecond() + ")");
      }
      PSConfigValidation vError = new PSConfigValidation(objName,
            SLOT_ASSOCIATION, true, buffer.toString());
      return Collections.singletonList(vError );
   }
   

   @Override
   protected boolean deApplyProperty(@SuppressWarnings("unused")
   Object obj, List<IPSAssociationSet> aSets, String propName, Object propValue)
      throws Exception
   {
      if (SLOT_ASSOCIATION.equals(propName))
      {
         removeSlotAssociations(aSets, propValue);
         return true;
      }
      else
      {
         return false;
      }
   }
   
   /**
    * Remove the slot associations specified in current properties.
    * 
    * @param aSets the target associated, not <code>null</code> or empty.
    * @param propValue the new association, not <code>null</code>, but be empty.
    * 
    * @return <code>true</code> if the associations have been removed;
    * otherwise have done nothing or no association has been removed.
    */
   @SuppressWarnings("unchecked")
   private boolean removeSlotAssociations(List<IPSAssociationSet> aSets,
         Object propValue)
   {
      if (aSets == null || aSets.isEmpty())
         throw new IllegalArgumentException(
               "The Slot to ContentType/Template association list must not be null or empty");

      List<PSPair<String, String>> curAssoc = convertObjectToList(propValue);
      if (curAssoc.isEmpty())
         return false;
      
      IPSAssociationSet assocSet = getAssoc(aSets, AssociationAction.DELETE);
      assocSet.setAssociations(curAssoc);
      return true;
   }

   /**
    * Sets the {@link #FINDER_ARGUMENTS} property for the given slot.
    * 
    * @param slot the slot object, assumed not <code>null</code>.
    * @param propValue the new value of the property, assumed it is a Map 
    * object, may be <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private void setFinderArguments(IPSTemplateSlot slot, Object propValue)
   {
      if (propValue == null)
         return;
      
      if (!(propValue instanceof Map))
         throw new PSConfigException("The value of \"" + FINDER_ARGUMENTS
               + "\" must be Map.");
      
      slot.setFinderArguments(filterParameters(slot, propValue));
   }
   
   /**
    * Filter the given filter-arguments for the supplied slot. It removes 
    * the entries of the map that the keys are not defined as the parameters
    * of the slot-filter. The slot-filter is a Java extension.  
    *  
    * @param slot the slot, assumed not <code>null</code>.
    * @param propValue the new filter-arguments, assumed is a type of Map,
    * not <code>null</code>.
    * 
    * @return the filtered map, never <code>null</code>, may be empty.
    */
   @SuppressWarnings("unchecked")
   private Map<String, String> filterParameters(IPSTemplateSlot slot,
         Object propValue)
   {
      Map<String, String> props = (Map<String, String>)propValue; 
      Map<String, String> params = new HashMap<String, String>();
      params.putAll(props);
      List<String> names = PSConfigUtils.getExtensionParameterNames(slot
            .getFinderName());
      for (String name : props.keySet())
      {
         if (!names.contains(name))
         {
            ms_log.warn("Skip finder argument \"" + name
                  + "\" since it is not a parameter defined by finder \""
                  + slot.getFinderName() + "\".");
            params.remove(name);
         }
      }
      return params;
   }
   
   /**
    * Sets the slot associations.
    * 
    * @param aSets the target associated, not <code>null</code> or empty.
    * @param propValue the new association, not <code>null</code>, but be empty.
    */
   @SuppressWarnings("unchecked")
   private void setSlotAssociations(List<IPSAssociationSet> aSets,
         Object propValue)
   {
      if (aSets == null || aSets.isEmpty())
         throw new IllegalArgumentException(
               "The Slot to ContentType/Template association list must not be null or empty");

      List<PSPair<String, String>> curAssoc = convertObjectToList(propValue);
      List<PSPair<String, String>> prevAssoc = getPrevAssoc();
      if (curAssoc.isEmpty() && prevAssoc.isEmpty())
         return;
      
      List<PSPair<String, String>> assoc = new ArrayList<PSPair<String,String>>();
      
      // get previous only associations
      assoc.addAll(prevAssoc);
      assoc.removeAll(curAssoc);
      if (!assoc.isEmpty())
      {
         IPSAssociationSet assocSet = getAssoc(aSets, AssociationAction.DELETE);
         assocSet.setAssociations(assoc);
      }

      // merge current associations
      if (!curAssoc.isEmpty())
      {
         IPSAssociationSet assocSet = getAssoc(aSets, AssociationAction.MERGE);
         assocSet.setAssociations(curAssoc);
      }
   }
   
   /**
    * Gets the specified associations from the specified association list.
    * 
    * @param aSets the association list, assumed not <code>null</code>.
    * @param action the action of the searched association, assumed not 
    * <code>null</code>.
    * 
    * @return the association with the specified action, never <code>null</code>.
    */
   private IPSAssociationSet getAssoc(List<IPSAssociationSet> aSets,
         AssociationAction action)
   {
      for (IPSAssociationSet assoc : aSets)
      {
         if (assoc.getAction().equals(action)
               && assoc.getType().equals(
                     AssociationType.SLOT_CONTENTTYPE_TEMPLATE))
            return assoc;
      }
      throw new IllegalArgumentException(
            "Cannot find associations with action = " + action.name()
                  + ", type = "
                  + AssociationType.SLOT_CONTENTTYPE_TEMPLATE.name());
   }
   
   /**
    * Converts the specified object to a list of pair strings.
    * 
    * @param propValue the object in question, it may be <code>null</code>.
    * 
    * @return the converted list, never <code>null</code>, but may be empty
    * if the object is <code>null</code> or empty list.
    */
   @SuppressWarnings({ "unused", "unchecked" })
   private List<PSPair<String, String>> convertObjectToList(Object propValue)
   {
      if (propValue == null)
         return Collections.emptyList();
      
      if (!(propValue instanceof List))
         throw new IllegalArgumentException(
               "The slot association value type must be List.");

      return (List<PSPair<String, String>>) propValue;
   }

   /**
    * Gets the slot association from previous properties.
    * 
    * @return the slot association, never <code>null</code>, may be empty.
    */
   private List<PSPair<String, String>> getPrevAssoc()
   {
      Map<String, Object> prevProps = getPrevProperties();
      if (prevProps == null || prevProps.isEmpty())
         return Collections.emptyList();
      
      return convertObjectToList(prevProps.get(SLOT_ASSOCIATION));
   }
   
   /**
    * The logger for this class
    */
   static Log ms_log = LogFactory.getLog("PSTemplateSlotSetter");


   /**
    * The property name for the finder arguments.
    */
   public static final String FINDER_ARGUMENTS = "finderArguments";
   
   /**
    * The property name for the finder arguments, the same as
    * {@link #FINDER_ARGUMENTS}.
    */
   public static final String FINDER_PARAMS = "finderParams";
      
   
   /**
    * The property name for the associations between the Slot to a list of 
    * Content-Type / Template.
    */
   public static final String SLOT_ASSOCIATION = "contentTypeTemplatePairs";
}
