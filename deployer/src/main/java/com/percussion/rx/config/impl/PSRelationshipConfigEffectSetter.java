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

import com.percussion.design.objectstore.PSConditionalEffect;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.rx.config.IPSConfigHandler.ObjectState;
import com.percussion.rx.config.PSConfigException;
import com.percussion.rx.config.PSConfigValidation;
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.util.PSCollection;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Setter for the relationship configuration effects. As it is a distributed
 * property and as well the collection, the new effects are added amd effects
 * from previous properties are removed.
 * 
 * @author bjoginipally
 * 
 */
public class PSRelationshipConfigEffectSetter extends
      PSPropertySetterWithValidation
{
   @Override
   protected boolean applyProperty(Object obj, ObjectState state,
         List<IPSAssociationSet> aSets, String propName, Object propValue)
      throws Exception
   {
      if (!(obj instanceof PSRelationshipConfig))
         throw new IllegalArgumentException(
               "obj type must be PSRelationshipConfig.");
      PSRelationshipConfig relConfig = (PSRelationshipConfig) obj;
      if (propName.equals(PROP_EFFECTS))
      {
         setEffectsProperty(relConfig, propValue);
      }
      else
      {
         super.applyProperty(obj, state, aSets, propName, propValue);
      }
      return true;
   }

   @Override
   protected boolean deApplyProperty(Object obj,
         List<IPSAssociationSet> aSets, String propName, Object propValue)
      throws Exception
   {
      if (!(obj instanceof PSRelationshipConfig))
         throw new IllegalArgumentException(
               "obj type must be PSRelationshipConfig.");
      PSRelationshipConfig relConfig = (PSRelationshipConfig) obj;
      if (propName.equals(PROP_EFFECTS))
      {
         if (propValue != null)
            removeEffects(relConfig, propValue);
      }
      else
      {
         return super.deApplyProperty(obj, aSets, propName, propValue);
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
      if (!(obj instanceof PSRelationshipConfig))
         throw new IllegalArgumentException(
               "obj type must be PSRelationshipConfig.");

      if (PROP_EFFECTS.equals(propName))
      {
         addFixmePropertyDefsForList(propName, pvalue, defs);
      }
      return true;
   }

   @Override
   protected Object getPropertyValue(Object obj, String propName)
   {
      if (!(obj instanceof PSRelationshipConfig))
         throw new IllegalArgumentException(
               "obj type must be PSRelationshipConfig.");
      PSRelationshipConfig relConfig = (PSRelationshipConfig) obj;
      if (PROP_EFFECTS.equals(propName))
      {
         return getEffectProperty(relConfig);
      }
      return super.getPropertyValue(obj, propName);
   }
   
   @Override
   protected List<PSConfigValidation> validate(String objName,
         ObjectState state, String propName, Object propValue,
         Object otherValue)
   {
      if (!PROP_EFFECTS.equals(propName))
         return super
               .validate(objName, state, propName, propValue, otherValue);

      List<PSConditionalEffect> curEffects = createEffects(propValue);
      List<PSConditionalEffect> otherEffects = createEffects(otherValue);
      if (curEffects.isEmpty() || otherEffects.isEmpty())
         return Collections.emptyList();
      List<String> curEffNames = getEffectNames(curEffects);
      List<String> othEffNames = getEffectNames(otherEffects);
      curEffNames.retainAll(othEffNames);
      if (curEffNames.isEmpty())
         return Collections.emptyList();

      PSConfigValidation vError;
      String msg = " Relationship Type  \"" + objName + "\" has effects \""
            + curEffNames.toString()
            + "\" that are already configured.";
      vError = new PSConfigValidation(objName, PROP_EFFECTS, true, msg);
      return Collections.singletonList(vError);
   }

   /**
    * Helper method to get the list of effect names for the given list of effect
    * objects.
    * 
    * @param effects The list effects, assumed not <code>null</code>.
    * @return List of names, may be empty if the supplied list of effects is
    * empty, never <code>null</code>.
    */
   private List<String> getEffectNames(List<PSConditionalEffect> effects)
   {
      List<String> effNames = new ArrayList<String>();
      for (PSConditionalEffect effect : effects)
      {
         effNames.add(effect.getEffect().getName());
      }
      return effNames;
   }

   /**
    * Sets the effects property on the configuration. If previous properties
    * exist and if effects property exists in the previous properties then
    * removes the effects from the previous properties. If new properties are
    * not <code>null</code> then adds the new properties.
    * 
    * @param relConfig The relationship configuration design object assumed not
    * <code>null</code>.
    * @param propValue The new properties consisting of effects configuration.
    */
   @SuppressWarnings("unchecked")
   private void setEffectsProperty(PSRelationshipConfig relConfig,
         Object propValue)
   {
      // If previous properties are not null or empty them remove the old
      // effects
      Map<String, Object> prevProps = getPrevProperties();
      Object prevPropValue = null;
      if (prevProps != null && !prevProps.isEmpty())
      {
         prevPropValue = prevProps.get("effects");
      }
      mergeEffects(relConfig, propValue, prevPropValue);
   }

   private List<Map<String,Object>> getEffectProperty(PSRelationshipConfig relConfig)
   {
      List<Map<String,Object>> effects = new ArrayList<Map<String,Object>>();
      Iterator iter = relConfig.getEffects();
      while(iter.hasNext())
      {
         PSConditionalEffect effect = (PSConditionalEffect) iter.next();
         effects.add(getEffectPropertyDef(effect));
      }
      return effects;
   }

   private Map<String, Object> getEffectPropertyDef(PSConditionalEffect effect)
   {
      Map<String, Object> efPropDef = new HashMap<String, Object>();
      // ExecutionContext prop
      List<String> execCtxtsList = new ArrayList<String>();
      Collection<Integer> execCtxts = effect.getExecutionContexts();
      for (Integer integer : execCtxts)
      {
         execCtxtsList.add(PSConditionalEffect
               .getExecutionContextNameForValue(integer, true));
      }
      efPropDef.put(PROP_EXECUTION_CONTEXT, execCtxtsList);

      // Direction prop
      String endPoint = effect.getActivationEndPoint();
      String direction = (String) PSConfigUtils.getReverseMap(
            ms_directionConsts).get(endPoint);
      efPropDef.put(PROP_DIRECTION, direction);

      // Extension prop
      PSExtensionCall extCall = effect.getEffect();
      efPropDef.putAll(PSConfigUtils.getExtensionCallDef(extCall,
            PROP_EXTENSION));

      // Condtion prop
      List<Map<String, Object>> condDef = PSConfigUtils.getCondtionsDef(effect
            .getConditions());
      efPropDef.put(PROP_CONDITION, condDef);
      return efPropDef;
   }

   /**
    * Removes the effects from the supplied relationship config object.
    * 
    * @param relConfig assumed not <code>null</code>.
    * @param propValue assumed not <code>null</code>.
    */
   private void removeEffects(PSRelationshipConfig relConfig, Object propValue)
   {
      Iterator iter = relConfig.getEffects();
      List<PSConditionalEffect> curEffects = new ArrayList<PSConditionalEffect>();
      while (iter.hasNext())
      {
         curEffects.add((PSConditionalEffect) iter.next());
      }
      List<PSConditionalEffect> removals = createEffects(propValue);
      curEffects.removeAll(removals);
      relConfig.setEffects(curEffects.iterator());
   }

   /**
    * Merges the effects to the supplied relationship config object.
    * 
    * @param relConfig assumed not <code>null</code>.
    * @param propValue assumed not <code>null</code>.
    */
   private void mergeEffects(PSRelationshipConfig relConfig, Object propValue,
         Object prevPropValue)
   {
      if (propValue == null && prevPropValue == null)
         return;
      Iterator iter = relConfig.getEffects();
      List<PSConditionalEffect> curEffects = new ArrayList<PSConditionalEffect>();
      while (iter.hasNext())
      {
         curEffects.add((PSConditionalEffect) iter.next());
      }

      List<PSConditionalEffect> newEffects = new ArrayList<PSConditionalEffect>();
      if (propValue != null)
         newEffects = createEffects(propValue);

      // Find the old effects that are not in new effects anymore.
      List<PSConditionalEffect> oldEffects = new ArrayList<PSConditionalEffect>();
      if (prevPropValue != null)
         oldEffects = createEffects(prevPropValue);

      List<PSConditionalEffect> removals = new ArrayList<PSConditionalEffect>();
      for (PSConditionalEffect effect : oldEffects)
      {
         if (getCorrespondingEffect(effect, newEffects) == null)
            removals.add(effect);
      }

      List<PSConditionalEffect> mergedEffects = new ArrayList<PSConditionalEffect>();
      // add the current effects to the merged list, if an effect exists in the
      // new effects use that effect instead of current effect and remove it
      // from the new effects.
      for (PSConditionalEffect effect : curEffects)
      {
         PSConditionalEffect mergedEffect = getCorrespondingEffect(effect,
               newEffects);
         if (mergedEffect == null)
         {
            mergedEffects.add(effect);
         }
         else
         {
            mergedEffects.add(mergedEffect);
            newEffects.remove(mergedEffect);
         }

      }
      // Now add the rest of the new effects.
      mergedEffects.addAll(newEffects);

      // Now remove the old effects that are not in new effects anymore
      mergedEffects.removeAll(removals);

      relConfig.setEffects(mergedEffects.iterator());
   }

   /**
    * If an effect exists with the same name in the supplied effects list, then
    * returns that effect otherwise returns <code>null</code>.
    * 
    * @param effect The effect that needs to be checked, assumed not
    * <code>null</code>.
    * @param effects The list of eefects, assumed not <code>null</code>.
    * @return The corresponding effect if exists, otherwise <code>null</code>.
    */
   private PSConditionalEffect getCorrespondingEffect(
         PSConditionalEffect effect, List<PSConditionalEffect> effects)
   {
      PSConditionalEffect result = null;
      for (PSConditionalEffect e : effects)
      {
         if (effect.getEffect().getName().equalsIgnoreCase(
               e.getEffect().getName()))
         {
            result = e;
            break;
         }
      }
      return result;
   }

   /**
    * Creates the effects from the supplied property value corresponding to the
    * relationship effect configuration.
    * 
    * @param propValue if <code>null</code> returns empty list.
    * @return List of effects, never <code>null</code>.
    */
   private List<PSConditionalEffect> createEffects(Object propValue)
   {
      List<PSConditionalEffect> effects = new ArrayList<PSConditionalEffect>();
      if(propValue == null)
         return effects;
      if (!(propValue instanceof List))
         throw new PSConfigException(
               "The type of the propValue must be \"List\"");
      List<Map<String, Object>> tempMaps = (List<Map<String, Object>>) propValue;
      for (Map<String, Object> map : tempMaps)
      {
         PSConditionalEffect effect = createEffect(map);
         effects.add(effect);
      }
      return effects;
   }

   /**
    * Creates the effect for the given map of properties corresponding to the
    * configuration of an effect.
    * 
    * @param map assumed not <code>null</code>.
    * @return The conditional effect never <code>null</code>.
    */
   private PSConditionalEffect createEffect(Map<String, Object> map)
   {
      String extension = (String) map.get(PROP_EXTENSION);
      String direction = (String) map.get(PROP_DIRECTION);
      List<String> execContext = (List<String>) map.get(PROP_EXECUTION_CONTEXT);
      Object condition = map.get(PROP_CONDITION);
      if (StringUtils.isBlank(extension))
      {
         throw new PSConfigException("The effect is missing required "
               + "property \"extension\".");
      }

      if (StringUtils.isBlank(direction))
      {
         throw new PSConfigException("The effect is missing required "
               + "property \"direction\".");
      }

      List<Integer> execCtxts = getExecutionContexts(execContext);
      List<String> extParams = (List<String>) map.get("extensionParams");
      PSExtensionCall extCall = PSConfigUtils.createExtensionCall(extension,
            extParams, "com.percussion.relationship.IPSEffect");
      String endPoint = ms_directionConsts.get(direction.toLowerCase());
      if (endPoint == null)
      {
         String msg = "The supplied direction ({0}) is invalid.";
         Object[] args = { direction };
         throw new PSConfigException(MessageFormat.format(msg, args));
      }

      // Now create the conditional effect and add it to the config
      PSConditionalEffect effect = new PSConditionalEffect(extCall);
      effect.setActivationEndPoint(endPoint);
      effect.setExecutionContexts(execCtxts);
      if (condition != null)
      {
         PSCollection conds = PSConfigUtils.prepareConditions(condition);
         effect.setConditions(conds.iterator());
      }
      return effect;
   }

   /**
    * Helper method to validate the supplied String representation of end points
    * and return their corresponding integer values. Throws
    * <code>PSConfigException</code> if the supplied list is empty or any of
    * the entry is not a valid execution context.
    * 
    * @param execCtxts The execution contexts, must not be <code>null</code>
    * or empty.
    * @return A valid list of Integers corresponding to the end points.
    */
   private List<Integer> getExecutionContexts(List<String> execCtxts)
   {
      if (execCtxts == null || execCtxts.isEmpty())
      {
         throw new PSConfigException("The effect is missing required "
               + "property \"executionContext\".");
      }
      List<Integer> ecs = new ArrayList<Integer>();
      for (String ec : execCtxts)
      {
         Integer ectx = PSConditionalEffect
               .getExecutionContextValueForName(ec);
         if (ectx == null)
         {
            String msg = "The supplied executionContext ({0}) is invalid.";
            Object[] args = { ec };
            throw new PSConfigException(MessageFormat.format(msg, args));
         }
         ecs.add(ectx);
      }
      return ecs;
   }

   // Constants for the names of the properties handled by this setter.
   private static final String PROP_EFFECTS = "effects";

   // The the UI the end points as direction, the following map consists of the
   // mapping values between UI and actual end point constants
   private static final Map<String, String> ms_directionConsts = new HashMap<String, String>();

   static
   {
      ms_directionConsts.put("up",
            PSRelationshipConfig.ACTIVATION_ENDPOINT_DEPENDENT);
      ms_directionConsts.put("down",
            PSRelationshipConfig.ACTIVATION_ENDPOINT_OWNER);
      ms_directionConsts.put("either",
            PSRelationshipConfig.ACTIVATION_ENDPOINT_EITHER);
   }

   private static final String PROP_CONDITION = "condition";

   private static final String PROP_EXECUTION_CONTEXT = "executionContext";

   private static final String PROP_DIRECTION = "direction";

   private static final String PROP_EXTENSION = "extension";


}
