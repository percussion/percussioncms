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

import com.percussion.design.objectstore.PSCloneOverrideField;
import com.percussion.design.objectstore.PSCloneOverrideFieldList;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSProcessCheck;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRule;
import com.percussion.rx.config.IPSConfigHandler.ObjectState;
import com.percussion.rx.config.PSConfigException;
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.util.PSCollection;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Relationship configuration setter to set the following three properties.
 * shallowCloning, deepCloning and fieldOverrides.
 * 
 * @author bjoginipally
 * 
 */
public class PSRelationshipConfigSetter extends PSSimplePropertySetter
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
      if (propName.equals(PROP_SHALLOWCLONING))
      {
         setCloningProperty(relConfig, propValue, true);
      }
      else if (propName.equals(PROP_DEEPCLONING))
      {
         setCloningProperty(relConfig, propValue, false);
      }
      else if (propName.equals(PROP_FIELDOVERRIDES))
      {
         setCloneFieldOverrides(relConfig, propValue);
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
         Object pvalue, Map<String, Object> defs) throws PSNotFoundException {
      if (super.addPropertyDefs(obj, propName, pvalue, defs))
         return true;
      if (!(obj instanceof PSRelationshipConfig))
         throw new IllegalArgumentException(
               "obj type must be PSRelationshipConfig.");
      PSRelationshipConfig relConfig = (PSRelationshipConfig) obj;

      if (PROP_SHALLOWCLONING.equals(propName))
      {
         addPropertyDefsForMap(propName, pvalue, getClonningProperty(
               relConfig, true), defs);
      }
      else if (PROP_DEEPCLONING.equals(propName))
      {
         addPropertyDefsForMap(propName, pvalue, getClonningProperty(
               relConfig, false), defs);
      }
      else if (PROP_FIELDOVERRIDES.equals(propName))
      {
         addFixmePropertyDefsForList(propName, pvalue, defs);
      }
      return true;
   }

   @Override
   protected Object getPropertyValue(Object obj, String propName) throws PSNotFoundException {
      if (!(obj instanceof PSRelationshipConfig))
         throw new IllegalArgumentException(
               "obj type must be PSRelationshipConfig.");
      PSRelationshipConfig relConfig = (PSRelationshipConfig) obj;
      if (PROP_SHALLOWCLONING.equals(propName))
      {
         return getClonningProperty(relConfig, true);
      }
      else if (PROP_DEEPCLONING.equals(propName))
      {
         return getClonningProperty(relConfig, false);
      }
      else if (PROP_FIELDOVERRIDES.equals(propName))
      {
         return getCloneFieldOverrides(relConfig);
      }
      return super.getPropertyValue(obj, propName);
   }
   
   
   /**
    * Helper method to set the clone field overrides property.
    * 
    * @param relConfig The PSRelationshipConfig object on which the property
    * needs to be set, assumed not <code>null</code>.
    * @param propValue The property value to set the clone field overrides.
    */
   @SuppressWarnings("unchecked")
   private void setCloneFieldOverrides(PSRelationshipConfig relConfig,
         Object propValue)
   {
      if (!(propValue instanceof List))
         throw new IllegalArgumentException(PROP_FIELDOVERRIDES
               + "property value must be a List type");
      List<Map<String, Object>> tempMap = (List<Map<String, Object>>) propValue;
      PSCloneOverrideFieldList cofList = new PSCloneOverrideFieldList();
      for (Map<String, Object> map : tempMap)
      {
         String fieldName = (String) map.get(PROP_FIELDNAME);
         if (StringUtils.isBlank(fieldName))
         {
            throw new PSConfigException("The fieldOverride is missing required "
                  + "property \"fieldName\".");
         }

         String extName = (String) map.get(PROP_EXTENSION);
         if (StringUtils.isBlank(extName))
         {
            throw new PSConfigException("The fieldOverride is missing required "
                  + "property \"extension\".");
         }
         
         List<String> extParams = (List<String>) map.get("extensionParams");
         PSExtensionCall extCall = PSConfigUtils.createExtensionCall(extName,
               extParams, "com.percussion.extension.IPSUdfProcessor");
         Object condition = map.get(PROP_CONDITION);
         PSCloneOverrideField cofld = new PSCloneOverrideField(fieldName,
               extCall);
         if (condition != null)
         {
            PSCollection conds = PSConfigUtils.prepareConditions(condition);
            cofld.setRules(conds);
         }
         cofList.add(cofld);
      }
      relConfig.setCloneOverrideFieldList(cofList);
   }

   /**
    * Helper method to get the clone field overrides property.
    * 
    * @param relConfig The PSRelationshipConfig object on which the property
    * needs to be set, assumed not <code>null</code>.
    * @param isShallow boolean flag to indicate whether the cloning property is
    * shallow or not.
    * @return The object corresponding to the cloning property. It is a map of name of
    * the parameter(String and the value of it(String).
    */
   private Map<String, Object> getClonningProperty(
         PSRelationshipConfig relConfig, boolean isShallow)
   {
      Map<String, Object> result = new HashMap<>();
      String cloneName = isShallow ? "rs_cloneshallow" : "rs_clonedeep";
      PSProcessCheck prCheck = relConfig.getProcessCheck(cloneName);
      Iterator iter = prCheck.getConditions();
      //enable condition
      if(iter.hasNext())
      {
         PSRule cond = (PSRule) iter.next();
         PSConditional trueCcond = PSConfigUtils.createBooleanCondition(true);
         PSCollection condRules = new PSCollection(PSConditional.class);
         condRules.add(trueCcond);
         PSRule trueRule = new PSRule(condRules);
         if(cond.equals(trueRule))
         {
            result.put(PROP_ENABLED, Boolean.TRUE);
         }
         else
         {
            result.put(PROP_ENABLED, Boolean.FALSE);
         }
      }
      PSCollection conditions = new PSCollection(PSConditional.class);
      while(iter.hasNext())
      {
         conditions.add((PSConditional) iter.next());
      }
      if(!conditions.isEmpty())
      {
         List<Map<String, Object>> condDefs = PSConfigUtils
               .getCondtionsDef(conditions.iterator());
         result.put(PROP_CONDITION, condDefs);
      }
      return result;
   }

   /**
    * Helper method to get the clone field overrides property.
    * 
    * @param relConfig The PSRelationshipConfig object from which the property
    * needs to be set, assumed not <code>null</code>.
    * @return propValue The property value to set the clone field overrides.
    */
   private List<Map<String, Object>> getCloneFieldOverrides(
         PSRelationshipConfig relConfig)
   {
      List<Map<String, Object>> result = new ArrayList<>();
      PSCloneOverrideFieldList cfList = relConfig.getCloneOverrideFieldList();
      Iterator iter = cfList.iterator();
      while (iter.hasNext())
      {
         PSCloneOverrideField cf = (PSCloneOverrideField) iter.next();

         Map<String, Object> cfEntry = new HashMap<>();
         String name = cf.getName();
         cfEntry.put(PROP_FIELDNAME, name);
         PSExtensionCall extCall = (PSExtensionCall) cf.getReplacementValue();
         cfEntry.putAll(PSConfigUtils.getExtensionCallDef(extCall,
               PROP_EXTENSION));
         PSCollection rules = cf.getRules();
         if (!rules.isEmpty())
         {
            List<Map<String, Object>> rulesDef = PSConfigUtils
                  .getCondtionsDef(rules.iterator());
            cfEntry.put(PROP_CONDITION, rulesDef);
         }
         result.add(cfEntry);
      }
      return result;
   }
   
   /**
    * Common method for setting both deep and shallow cloning properties.
    * 
    * @param relConfig
    * @param cloningPropValue The property value of cloning properties.
    * @param isShallow boolean flag to indicate whether it is shallow cloning or
    * deep cloning.
    */
   @SuppressWarnings("unchecked")
   private void setCloningProperty(PSRelationshipConfig relConfig,
         Object cloningPropValue, boolean isShallow)
   {
      if (!(cloningPropValue instanceof Map))
         throw new PSConfigException("cloningPropValue must be a \"Map\" type");
      Map<String, Object> map = (Map<String, Object>) cloningPropValue;

      String enabled = "";
      Object condition = null;
      enabled = (String) map.get(PROP_ENABLED);
      condition = map.get(PROP_CONDITION);
      if (StringUtils.isBlank(enabled))
      {
         String msg = "The required property ({0}) is missing for the "
               + "supplied property ({1}).";
         String type = isShallow ? PROP_SHALLOWCLONING : PROP_DEEPCLONING;
         Object[] args = { PROP_ENABLED, type };
         throw new PSConfigException(MessageFormat.format(msg, args));
      }
      String cloneName = isShallow ? "rs_cloneshallow" : "rs_clonedeep";
      PSProcessCheck prCheck = relConfig.getProcessCheck(cloneName);
      Boolean enFlag = new Boolean(enabled);
      PSConditional cond = PSConfigUtils.createBooleanCondition(enFlag);
      PSCollection condRules = new PSCollection(PSConditional.class);
      condRules.add(cond);
      PSRule rule = new PSRule(condRules);
      PSCollection rules = new PSCollection(PSRule.class);
      rules.add(rule);
      // If enabled and conditions are not null then add the conditions.
      if (enFlag && condition != null)
      {
         PSCollection conds = new PSCollection(PSRule.class);
         conds = PSConfigUtils.prepareConditions(condition);
         rules.addAll(conds);
      }
      prCheck.setConditions(rules.iterator());
   }

   // Constants for the names of the properties handled by this setter.
   private static final String PROP_SHALLOWCLONING = "shallowCloning";

   private static final String PROP_DEEPCLONING = "deepCloning";

   private static final String PROP_FIELDOVERRIDES = "fieldOverrides";

   private static final String PROP_ENABLED = "enabled";

   private static final String PROP_CONDITION = "condition";

   private static final String PROP_FIELDNAME = "fieldName";

   private static final String PROP_EXTENSION = "extension";
}
