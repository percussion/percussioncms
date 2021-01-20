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
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateBinding;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.services.assembly.data.PSTemplateBinding;
import com.percussion.utils.types.PSPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible to set properties for a Template.
 *
 * @author YuBingChen
 */
public class PSTemplateSetter extends PSSimplePropertySetter
{
   @Override
   protected boolean applyProperty(Object obj, ObjectState state,
         List<IPSAssociationSet> aSets, String propName, Object propValue)
      throws Exception
   {
      if (! (obj instanceof IPSAssemblyTemplate))
         throw new IllegalArgumentException("obj type must be IPSAssemblyTemplate.");
      
      PSAssemblyTemplate template = (PSAssemblyTemplate) obj;
      if (GLOBAL_TEMPLATE.equals(propName))
      {
         setGlobalTemplate(template, propValue);
      }
      else if (SLOTS.equals(propName))
      {
         setListAssociation(aSets,
               IPSAssociationSet.AssociationType.TEMPLATE_SLOT, propValue);
      }
      else if (BINDINGS.equals(propName))
      {
         setBindings(template, propValue);
      }
      else if (BINDING_SET.equals(propName))
      {
         setBindingSet(template, propValue);
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
      
      if (SLOTS.equals(propName) || BINDING_SET.equals(propName))
      {
         addFixmePropertyDefsForList(propName, pvalue, defs);
      }
      else if (BINDINGS.equals(propName))
      {
         IPSAssemblyTemplate tp = (IPSAssemblyTemplate) obj;
         addPropertyDefsForMap(propName, pvalue, getBindings(tp), defs);
      }
      return true;
   }
   
   /*
    * //see base class method for details
    */
   @Override
   protected Object getPropertyValue(Object obj, String propName)
   {
      IPSAssemblyTemplate template = (IPSAssemblyTemplate) obj;
      if (GLOBAL_TEMPLATE.equals(propName))
      {
         if (template.getGlobalTemplate() == null)
            return null;
         
         IPSAssemblyService srv = PSAssemblyServiceLocator
               .getAssemblyService();
         IPSAssemblyTemplate t = srv
               .findTemplate(template.getGlobalTemplate());

         return t == null ? null : t.getName();
      }
      else if (SLOTS.equals(propName))
      {
         List<String> slots = new ArrayList<String>();
         for (IPSTemplateSlot s : template.getSlots())
         {
            slots.add(s.getName());
         }
         return slots;
      }
      else if (BINDING_SET.equals(propName))
      {
         List<PSPair<String, String>> bList = new ArrayList<PSPair<String, String>>();
         for (IPSTemplateBinding b : template.getBindings())
         {
            bList.add(new PSPair<String, String>(b.getVariable(), b
                  .getExpression()));
         }
         return bList;
      }
      else if (BINDINGS.equals(propName))
      {
         return getBindings(template);
      }
      
      return super.getPropertyValue(obj, propName);
   }   
   
   /**
    * Retrieves the bindings from the specified template into a map.
    * 
    * @param template the template in question, assumed not <code>null</code>.
    * 
    * @return the bindings that contains a {@link #BINDING_SEQ} entry for the
    * order of the bindings. It cannot be <code>null</code>, but may be empty.
    */
   private Map<String, Object> getBindings(IPSAssemblyTemplate template)
   {
      List<String> seq = new ArrayList<String>();
      Map<String, Object> bindings = new HashMap<String, Object>();
      for (IPSTemplateBinding b : template.getBindings())
      {
         bindings.put(b.getVariable(), b.getExpression());
         seq.add(b.getVariable());
      }
      if (!bindings.isEmpty())
      {
         bindings.put(BINDING_SEQ, seq);
      }
      return bindings;
   }
   
   /**
    * Sets the {@link #GLOBAL_TEMPLATE} property.
    * 
    * @param template the template, assumed not <code>null</code>.
    * @param propValue the name of the new global template, may not be
    * <code>null</code>, but may be empty.
    * 
    * @throws PSAssemblyException if failed when search the global template.
    */
   private void setGlobalTemplate(IPSAssemblyTemplate template,
         Object propValue) throws PSAssemblyException
   {
      IPSAssemblyService srv = PSAssemblyServiceLocator.getAssemblyService();
      IPSAssemblyTemplate t = srv.findTemplateByName((String)propValue);

      if (t == null)
         throw new PSConfigException("Cannot find global template \""
               + propValue + "\".");
      
      template.setGlobalTemplate(t.getGUID());
   }
   
   /**
    * Set the {@link #BINDINGS} property. The bindings defined in this property
    * will be merged into the current bindings of the template.
    * 
    * @param template the template, assumed not <code>null</code>.
    * @param propValue the value of the new binding, may not be
    * <code>null</code>, but may be empty.
    */
   @SuppressWarnings("unchecked")
   private void setBindings(PSAssemblyTemplate template, Object propValue)
   {
      if (!(propValue instanceof Map))
         throw new PSConfigException("The value type of the " + BINDINGS
               + " must be Map");
      
      Map<String, Object> props = (Map<String, Object>) propValue;
      if (props.isEmpty())
         return;
      
      List<String> seq = mergeBinding(template, props);
      reorderBindings(template, seq);      
   }
   
   /**
    * Reorder the bindings in the given template according to supplied sequence.
    * 
    * @param template the template, assumed not <code>null</code>.
    * @param seq the sequence, may be <code>null</code> (do nothing in this 
    * case).
    */
   private void reorderBindings(PSAssemblyTemplate template, List<String> seq)
   {
      if (seq == null)
         return;
      
      List<PSTemplateBinding> src = template.getBindings();
      List<PSTemplateBinding> target = new ArrayList<>();
      
      // add bindings from "src" to "target" according to "seq"
      int index = 1; // it is 1 based index/sequence
      for (String var : seq)
      {
         PSTemplateBinding b = getBinding(src, var);
         if (b != null)
         {
            target.add(b);
            src.remove(b);
            b.setExecutionOrder(index++);
         }
      }
      // append whatever left in "src" into "target"
      for (PSTemplateBinding b : src)
      {
         b.setExecutionOrder(index++);
         target.add(b);
      }
      template.setBindings(target);
   }

   /**
    * Merges the given bindings into the supplied template.
    * 
    * @param template the template, assumed not <code>null</code>.
    * @param map the binding properties, assumed not <code>null</code>, may be 
    * empty.
    * 
    * @return the sequence property specified in the binding properties. It may
    * be <code>null</code> if there is no sequence property specified.
    */
   private List<String> mergeBinding(IPSAssemblyTemplate template, 
         Map<String, Object> map)
   {
      List<String> seqList = null;
      for (Map.Entry<String, Object> entry : map.entrySet())
      {
         PSTemplateBinding binding = getBinding(template.getBindings(), entry.getKey());
         if (binding != null)
         {
            binding.setExpression((String)entry.getValue());
         }
         else if (BINDING_SEQ.equalsIgnoreCase(entry.getKey()))
         {
            seqList = getSequenceList(entry.getValue());
         }
         else
         {
            binding = new PSTemplateBinding(template.getBindings().size(),
                  entry.getKey(), (String)entry.getValue());
            template.addBinding(binding);            
         }
      }
      return seqList;
   }
   
   /**
    * Validates the sequence object.
    * 
    * @param seq the sequence object in question, may not be <code>null</code>.
    * 
    * @return the sequence object in the expected type.
    */
   @SuppressWarnings("unchecked")
   private List<String> getSequenceList(Object seq)
   {
      if (!(seq instanceof List))
         throw new PSConfigException("\"" + BINDING_SEQ
               + "\" property must be \"List\" type.");

      return (List<String>) seq;
   }
   
   /**
    * Gets the specified binding from the given binding list.
    * 
    * @param bindings the binding list, assumed not <code>null</code>.
    * @param name the name of the binding variable, may be <code>null</code> or
    * empty.
    *
    * @return the looked up binding, may be <code>null</code> if cannot find.
    */
   private PSTemplateBinding getBinding(List<PSTemplateBinding> bindings,
                                        String name)
   {
      for (IPSTemplateBinding binding : bindings)
      {
         if (binding.getVariable().equals(name))
            return (PSTemplateBinding) binding;
      }
      return null;
   }
   
   /**
    * Set the whole bindings from the {@link #BINDING_SET} property.
    * 
    * @param template the template object, assumed not <code>null</code>.
    * @param obj the {@link #BINDING_SET} property value. It may not be 
    * <code>null</code>, but may be empty if clear the bindings of the template.
    */
   @SuppressWarnings("unchecked")
   private void setBindingSet(IPSAssemblyTemplate template, Object obj)
   {
      if (!(obj instanceof List))
         throw new PSConfigException("The value type of the " + BINDING_SET
               + " must be List");
      
      List<PSPair<String, String>> bindings = (List<PSPair<String, String>>) obj;
      template.getBindings().clear();
      for (int i=0;  i < bindings.size(); i++)
      {
         PSPair<String, String> b = bindings.get(i);
         PSTemplateBinding binding = new PSTemplateBinding(i + 1,
               b.getFirst(), b.getSecond());
         template.addBinding(binding);
      }
   }

   /**
    * The property name for the Global Template.
    */
   public static final String GLOBAL_TEMPLATE = "globalTemplate";
   

   /**
    * The property name for Template and Slots association.
    */
   public static final String SLOTS = "slots";
   
   /**
    * The value of this property is used to replace the entire bindings of the
    * template. Expected type of the value is {@link PSPair}
    */
   public static final String BINDING_SET = "bindingSet";

   /**
    * The value of this property is used to merge the specified bindings into
    * the current binding list of the template. The expected type of the value
    * is <code>Map</code>. A optional entry with {@link #BINDING_SEQ} key can
    * be used to reorder the specified bindings.
    */
   public static final String BINDINGS = "bindings";

   /**
    * The key name can be optionally specified in the {@link #BINDINGS} map.
    */
   public static final String BINDING_SEQ = "binding_sequence";
}
