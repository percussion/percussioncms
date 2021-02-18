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
package com.percussion.rx.utils;

import com.percussion.design.objectstore.PSApplyWhen;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionCallSet;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldValidationRules;
import com.percussion.design.objectstore.PSRule;
import com.percussion.design.objectstore.PSSingleHtmlParameter;
import com.percussion.design.objectstore.PSValidationException;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.server.PSServer;
import com.percussion.util.PSCollection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

/**
 * The utility class used to modify properties for Content Type related
 * Design Objects, such as {@link PSItemDefinition}, {@link PSContentEditor},
 * ...etc.
 * Note, there is no direct calls to server or remote. It is caller's 
 * responsibility to pass the cataloged data to the utility methods. 
 *
 * @author YuBingChen
 */
public class PSContentTypeUtils
{
   
   /**
    * Checks whether the supplied rules has a required rule are not. Walks
    * through all the rules and checks the name of the each extension for
    * sys_ValidateRequiredField.
    * 
    * @param rules List of rules that needs to be checked
    * @return <code>true</code> if the rules has reuired rule otherwise
    *         <code>false</code>.
    */
   @SuppressWarnings("unchecked")
   public static boolean hasRequiredRule(List<PSRule> rules)
   {
      if (rules == null)
         return false;
      
      for (PSRule rule : rules)
      {
         if (rule.isExtensionSetRule())
         {
            Iterator eiter = rule.getExtensionRules().iterator();
            if (eiter.hasNext())
            {
               PSExtensionCall ext = (PSExtensionCall) eiter.next();
               if (ext.getName().equalsIgnoreCase(RULE_REQUIRED_EXTNAME))
               {
                  return true;
               }
            }
         }
      }
      return false;
   }

   /**
    * Checks whether the supplied rules has a required rule are not. Walks
    * through all the rules and checks the name of the each extension for
    * sys_ValidateRequiredField. If it has then removes it supplied rules. If
    * supplied rules is null then simply returns.
    * 
    * @param rules List of rules from which required rule needs to be removed.
    */
   @SuppressWarnings("unchecked")
   public static void removeRequiredRule(List<PSRule> rules)
   {
      if (rules == null)
         return;
      PSRule reqRule = null;
      for (PSRule rule : rules)
      {
         if (rule.isExtensionSetRule())
         {
            Iterator eiter = rule.getExtensionRules().iterator();
            if (eiter.hasNext())
            {
               PSExtensionCall ext = (PSExtensionCall) eiter.next();
               if (ext.getName().equalsIgnoreCase(RULE_REQUIRED_EXTNAME))
               {
                  reqRule = rule;
                  break;
               }
            }
         }
      }
      if (reqRule != null)
         rules.remove(reqRule);
   }   

   /**
    * Adds the required rule to the supplied list of rules. If supplied rules is
    * <code>null</code> new array list is created and required rule is added.
    * If the required rule already exists in the supplied rules does nothing.
    * 
    * @param rules The rules for which the required rule needs to be added. May
    *           be <code>null</code>.
    * @param fieldName The name of the must not be <code>null</code> or empty.
    * @param fvExits a list of extensions that implemented 
    * {@link IPSFieldValidator}. Never <code>null</code>, may be empty.
    * 
    */
   public static void addRequiredRule(List<PSRule> rules, String fieldName,
         List<PSExtensionRef> fvExits)
   {
      if(StringUtils.isBlank(fieldName))
         throw new IllegalArgumentException("fieldName must not be null");
      if (rules == null)
         throw new IllegalArgumentException("rules may not be null.");
      if (fvExits == null)
         throw new IllegalArgumentException("fvExits may not be null.");
      
      if(hasRequiredRule(rules))
         return;
      PSExtensionCallSet callSet = new PSExtensionCallSet();
         
      for (PSExtensionRef extRef : fvExits)
      {
         String extName = extRef.getExtensionName();
         if (extName.equals(RULE_REQUIRED_EXTNAME))
         {
            PSExtensionParamValue param0 = null;
            param0 = new PSExtensionParamValue(new PSSingleHtmlParameter(
                  fieldName));
            PSExtensionParamValue[] params = new PSExtensionParamValue[1];
            params[0] = param0;
            PSExtensionCall call = new PSExtensionCall(extRef, params);
            callSet.add(call);
            rules.add(0,new PSRule(callSet));
         }
      }
   }

   /**
    * Determines if a field has the required rule.
    * 
    * @param field the field in question, never <code>null</code>.
    * 
    * @return <code>true</code> if the field has the required rule.
    */
   public static boolean hasRequiredRule(PSField field)
   {
      PSFieldValidationRules valrules = field.getValidationRules();
      List<PSRule> rules = new ArrayList<>();
      if(valrules != null)
      {
         CollectionUtils.addAll(rules,valrules.getRules());
      }
      
      return hasRequiredRule(rules);
   }
   
   /**
    * Adds or removes the required rule for the specified field. This does 
    * nothing if the required rule needs to be added and the field already has
    * the required rule.
    * 
    * @param field the field in question, never <code>null</code>.
    * @param isRequired <code>true</code> if need to add the required rule to
    * the field; otherwise remove the required rule from the field.
    * @param fvExits a list of extensions that implemented 
    * {@link IPSFieldValidator}. Never <code>null</code>, may be empty.
    */
   public static void setFieldRequiredRule(PSField field, boolean isRequired,
         List<PSExtensionRef> fvExits)
   {
      PSFieldValidationRules valrules = field.getValidationRules();
      List<PSRule> rules = new ArrayList<>();
      PSApplyWhen applyWhen = null;
      if(valrules != null)
      {
         CollectionUtils.addAll(rules,valrules.getRules());
         applyWhen = valrules.getApplyWhen();
      }
      if(isRequired)
      {
         addRequiredRule(rules, field.getSubmitName(), fvExits);
         if(applyWhen == null)
            applyWhen = new PSApplyWhen();
         applyWhen.setIfFieldEmpty(true);
      }
      else
      {
         removeRequiredRule(rules);
         if (applyWhen != null)
            applyWhen.setIfFieldEmpty(false);
      }
      //If there are no rules anymore simply set the validationrules to null.
      if(rules.isEmpty())
         field.setValidationRules(null);
      else
      {
         //Create new Validation Rules if it is null
         if (valrules == null)
            valrules = new PSFieldValidationRules();
         //Set the apply when
         valrules.setApplyWhen(applyWhen);
         //Set the rules
         PSCollection newRules = new PSCollection(PSRule.class);
         newRules.addAll(rules);
         valrules.setRules(newRules);
         
         field.setValidationRules(valrules);
      }

      //Set the oocurence setting.
      int occur = isRequired ? PSField.OCCURRENCE_DIMENSION_REQUIRED
            : PSField.OCCURRENCE_DIMENSION_OPTIONAL;
      try
      {
         field.clearOccurrenceSettings();
         field.setOccurrenceDimension(occur, null);
      }
      catch (PSValidationException ve)
      {
         // We can safely ignore this as we are setting a valid value
         // here.
         ve.printStackTrace();
      }
   }

   /**
    * Adds or removes the required rule for the specified field. This does 
    * nothing if the required rule needs to be added and the field already has
    * the required rule.
    * 
    * @param field the field in question, never <code>null</code>.
    * @param isRequired <code>true</code> if need to add the required rule to
    * the field; otherwise remove the required rule from the field.
    * 
    * @throws PSExtensionException if failed to retrieve registered extensions.
    */
   @SuppressWarnings("unchecked")
   public static void setFieldRequiredRule(PSField field, boolean isRequired)
      throws PSExtensionException
   {
      // get all extension the implemented IPSFieldValidator
      IPSExtensionManager mgr = PSServer.getExtensionManager(null);
      Iterator it = mgr.getExtensionNames(null, null,
            com.percussion.extension.IPSFieldValidator.class.getName(), null);
      List<PSExtensionRef> extensions = new ArrayList<>();
      CollectionUtils.addAll(extensions, it);
      
      // set the property
      setFieldRequiredRule(field, isRequired, extensions);
   }
   
   /**
    * Extension name of the Required rule.
    */
   public static final String RULE_REQUIRED_EXTNAME = "sys_ValidateRequiredField"; //$NON-NLS-1$
}
