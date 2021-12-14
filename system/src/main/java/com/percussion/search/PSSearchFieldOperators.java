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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.search;

import com.percussion.cms.objectstore.PSSearchField;
import com.percussion.design.objectstore.PSDisplayText;
import com.percussion.design.objectstore.PSEntry;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.i18n.ui.PSI18NTranslationKeyValues;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class exposes all functionality to translate search field operators
 * from / to the display mapping and from / to the internal mapping. Also all
 * data transformations for updating the UI and repository is provided.
 */
public class PSSearchFieldOperators
{
   /**
    * Get the internationalized operator mappings for the supplied parameters.
    * 
    * @param field the search field for which to get the operator mappings,
    *    not <code>null</code>.
    * @param translator the translator used to internationalize the entry
    *    labels, if <code>null</code> is supplied, <code>PSI18nUtils</code>
    *    will be used as translator along with the specified locale.
    * @param locale the locale for which to internationalize the entry
    *    labels, may <code>null</code> or empty in which case the default
    *    locale is used. Ignored if the <code>translator</code> is not 
    *    <code>null</code>.
    * @return an array of <code>PSEntry</code> objects with the operator
    *    mappings for the supplied field type, never <code>null</code>, may
    *    be empty. The entry values are set to the I18N key of the operator, the
    *    labels are set to the internationalized operator.
    */
   public static Object[] getOperators(PSSearchField field, 
      PSI18NTranslationKeyValues translator, String locale)
   {
      if (field == null)
         throw new IllegalArgumentException("field cannot be null");
      
      List operators = getOperatorsList(field);
   
      Object[] results = new PSEntry[operators.size()];
      for (int i=0; i<results.length; i++)
      {
         String operator = (String) operators.get(i);
         String label = null;
         if (translator == null)
         {
            if (locale != null && locale.trim().length() > 0)
               label = PSI18nUtils.getString(operator);
            else
               label = PSI18nUtils.getString(operator, locale);
         }
         else
            label = translator.getTranslationValue(operator);
         
         PSEntry entry = new PSEntry(operator, new PSDisplayText(label));
      
         results[i] = entry;
      }
   
      return results;
   }
   
   /**
    * Get the operator entry for the supplied parameters.
    * 
    * @param field the field for which to get the operator entry, not
    *    <code>null</code>.
    * @param key the I18N operator key for which to get the operator,
    *    not <code>null</code> or empty.
    * @return the entry found for the supplied field and key, may be 
    *    <code>null</code> if no entry was found for the supplied key.
    */
   public static PSEntry getOperator(PSSearchField field, String key)
   {
      if (field == null)
         throw new IllegalArgumentException("field cannot be null");
         
      if (key == null)
         throw new IllegalArgumentException("key cannot be null");
         
      key = key.trim();
      if (key.length() == 0)
         throw new IllegalArgumentException("key cannot be empty");
      
      List operators = getOperatorsList(field);
      for (int i=0; i<operators.size(); i++)
      {
         String operator = (String) operators.get(i);
         if (operator.equalsIgnoreCase(key))
            return new PSEntry(operator, operator);
      }
      
      return null;
   }
   
   /**
    * Get the internationalized operator entry for the supplied parameters.
    * 
    * @param field the field for which to get the operator entry, not
    *    <code>null</code>.
    * @param key the I18N operator key fro which to get the operator,
    *    not <code>null</code> or empty.
    * @param translator the translator used to internationalize the entry
    *    labels, if <code>null</code> is supplied, <code>PSI18nUtils</code>
    *    will be used as translator along with the specified locale.
    * @param locale the locale for which to internationalize the entry
    *    labels, may <code>null</code> or empty in which case the default
    *    locale is used. Ignored if the <code>translator</code> is not 
    *    <code>null</code>.
    * @return the internationalized entry found for the supplied field and key, 
    *    may be <code>null</code> if no entry was found for the supplied key.
    */
   public static PSEntry getOperator(PSSearchField field, String key, 
      PSI18NTranslationKeyValues translator, String locale)
   {
      if (translator == null)
         throw new IllegalArgumentException("translator cannot be null");
      
      PSEntry entry = getOperator(field, key);
      if (entry != null)
      {
         String label = "";
         if (translator == null)
         {
            if (locale != null && locale.trim().length() > 0)
               label = PSI18nUtils.getString(entry.getValue());
            else
               label = PSI18nUtils.getString(entry.getValue(), locale);
         }
         else
            label = translator.getTranslationValue(entry.getValue());
            
         entry.setLabel(new PSDisplayText(label));
      }
      
      return entry;
   }
   
   /**
    * Get the input field operator, the operator used in user interfaces.
    * 
    * @param field the field for which to get the input search operator,
    *    not <code>null</code>.
    * @return the i18n key value for the UI representation of the operator, 
    *    never <code>null</code>.
    */
   public static String getInputOperator(PSSearchField field)
   {
      if (field == null)
         throw new IllegalArgumentException("field cannot be null");
      
      String result = "";
      
      String operator = field.getOperator();
      String value = field.getFieldValue();
      
      if (field.isDateValue())
      {
         if (operator.equals(PSSearchField.OP_GREATERTHAN))
            result = PSCommonSearchUtils.OP_AFTER;
         else if (operator.equals(PSSearchField.OP_LESSTHAN))
            result = PSCommonSearchUtils.OP_BEFORE;
         else if (operator.equals(PSSearchField.OP_BETWEEN))
            result = PSCommonSearchUtils.OP_BETWEEN;
         else if (operator.equals(PSSearchField.OP_EQUALS))
            result = PSCommonSearchUtils.OP_ON;
      }
      else if (field.isNumberValue())
      {
         if (operator.equals(PSSearchField.OP_BETWEEN))
            result = PSCommonSearchUtils.OP_BETWEEN;
         else if (operator.equals(PSSearchField.OP_EQUALS))
            result = PSCommonSearchUtils.OP_EQUALS;
         else if (operator.equals(PSSearchField.OP_GREATERTHAN))
            result = PSCommonSearchUtils.OP_GREATER_THAN;
         else if (operator.equals(PSSearchField.OP_LESSTHAN))
            result = PSCommonSearchUtils.OP_LESS_THAN;
      }
      else if (field.isTextValue())
      {
         result = PSCommonSearchUtils.OP_CONTAINS;
         if (operator.length() > 0 && !operator.equals(PSSearchField.OP_EQUALS))
         {
            if (value.startsWith("%") && value.endsWith("%"))
               result = PSCommonSearchUtils.OP_CONTAINS;
            else if (value.startsWith("%"))
               result = PSCommonSearchUtils.OP_ENDS_WITH;
            else if (value.endsWith("%"))
               result = PSCommonSearchUtils.OP_STARTS_WITH;
         }
         else if (operator.equals(PSSearchField.OP_EQUALS))
            result = PSCommonSearchUtils.OP_EXACT;
      }
      
      return result;
   }

   /**
    * Get the output field operator, the operator used internally.
    * 
    * @param field the field for which to get the output search operator,
    *    not <code>null</code>.
    * @param operator the UI operator for which to get the output search 
    *    operator, not <code>null</code> or empty, one of the 
    *    <code>PSSearchField.OP_xxx</code> values.
    * @return the search field operator how it is used internally, never 
    *    <code>null</code>, one of the <code>PSSearchField.OP_XXX</code> 
    *    values, or <code>PSSearchField.OP_EQUALS</code> if no match is found.
    */
   public static String getOutputOperator(PSSearchField field, String operator)
   {
      if (field == null)
         throw new IllegalArgumentException("field cannot be null");

      if (operator == null)
         throw new IllegalArgumentException("operator cannot be null");
         
      operator = operator.trim();
      if (operator.length() == 0)
         throw new IllegalArgumentException("operator cannot be empty");
      
      String result = PSSearchField.OP_EQUALS;
      
      if (field.isDateValue())
      {
         if (operator.equals(PSCommonSearchUtils.OP_AFTER))
            result = PSSearchField.OP_GREATERTHAN;
         else if (operator.equals(PSCommonSearchUtils.OP_BEFORE))
            result = PSSearchField.OP_LESSTHAN;
         else if (operator.equals(PSCommonSearchUtils.OP_BETWEEN))
            result = PSSearchField.OP_BETWEEN;
         else if (operator.equals(PSCommonSearchUtils.OP_ON))
            result = PSSearchField.OP_EQUALS;
      }
      else if (field.isNumberValue())
      {
         if (operator.equals(PSCommonSearchUtils.OP_BETWEEN))
            result = PSSearchField.OP_BETWEEN;
         else if (operator.equals(PSCommonSearchUtils.OP_EQUALS))
            result = PSSearchField.OP_EQUALS;
         else if (operator.equals(PSCommonSearchUtils.OP_GREATER_THAN))
            result = PSSearchField.OP_GREATERTHAN;
         else if (operator.equals(PSCommonSearchUtils.OP_LESS_THAN))
            result = PSSearchField.OP_LESSTHAN;
      }
      else if (field.isTextValue())
      {
         result = PSSearchField.OP_LIKE;
         if (operator.equals(PSCommonSearchUtils.OP_EXACT))
            result = PSSearchField.OP_EQUALS;
      }
      
      return result;
   }
   
   /**
     * Convenience version of {@link #getInputValues(PSSearchField)} that
     * assumes the field has a single value.
     * 
     * @return The value, never <code>null</code> may be empty
     */
    public static String getInputValue(PSSearchField field)
    {
       if (field == null)
          throw new IllegalArgumentException("field cannot be null");
         
       List values = getInputValues(field);
       
       return values.isEmpty() ? "" : (String)values.get(0);
    }
   
    /**
     * Convert the supplied search field values on input, e.g. while 
     * initializing the user interface from the internal data.
     * 
     * @param field the search field for which to convert it's value for input,
     *    not <code>null</code>.
     * @return the converted values based on the search field type, 
     *    never <code>null</code>, may be empty.  The caller takes ownership of 
     *    the returned list.
     */
    public static List getInputValues(PSSearchField field)
    {
       if (field == null)
          throw new IllegalArgumentException("field cannot be null");
         
       List values = field.getFieldValues();
       String operator = field.getOperator();
      
       if (field.isDateValue())
       {
          // noop
       }
       else if (field.isNumberValue())
       {
          // noop
       }
       else if (field.isTextValue())
       {
          if (operator.length() > 0 && !operator.equals(
            PSSearchField.OP_EQUALS))
          {
             List newVals = new ArrayList();
             Iterator iter = values.iterator();
             while (iter.hasNext())
             {
                String value = (String)iter.next();
                if (value.startsWith("%") && value.endsWith("%"))
                {
                   if (value.length() > 1)
                      value = value.substring(1, value.length()-1);
                   else
                      value = "";
                }
                else if (value.startsWith("%"))
                   value = value.substring(1);
                else if (value.endsWith("%"))
                   value = value.substring(0, value.length()-1);
               
                newVals.add(value);
             }
             values = newVals;
          }
       }
         
       return values;
    }   
   /**
    * Convert the supplied search field value on output, e.g. while saving
    * the user interface data to the internal data.
    * 
    * @param value the value to convert, not <code>null</code>, may be empty.
    * @param operator the operator for which to do the conversion, not
    *    <code>null</code> or empty.
    * @param field the search field for which to convert it's value for output,
    *    not <code>null</code>.
    * @return the converted value base on the supplied search field type and 
    *    operator, never <code>null</code>, may be empty.
    */
   public static String getOutputValue(String value, String operator, 
      PSSearchField field)
   {
      if (value == null)
         throw new IllegalArgumentException("value cannot be null");
         
      if (operator == null)
         throw new IllegalArgumentException("operator cannot be null");

      operator = operator.trim();
      if (operator.length() == 0)
         throw new IllegalArgumentException("operator cannot be empty");
         
      if (field == null)
         throw new IllegalArgumentException("field cannot be null");
         
      if (field.isDateValue())
      {
         // noop
      }
      else if (field.isNumberValue())
      {
         // noop
      }
      else if (field.isTextValue())
      {
         value = value.trim();
         if (value.trim().length() > 0)
         {
            if (!operator.equals(PSCommonSearchUtils.OP_EXACT))
            {
               if (operator.equals(PSCommonSearchUtils.OP_STARTS_WITH))
               {
                  if (!value.endsWith("%"))
                     value += "%";
               }
               else if (operator.equals(PSCommonSearchUtils.OP_CONTAINS))
               {
                  if (!value.startsWith("%"))
                     value = "%" + value;
                  if (!value.endsWith("%"))
                     value += "%";
               }
               else if (operator.equals(PSCommonSearchUtils.OP_ENDS_WITH))
               {
                  if (!value.startsWith("%"))
                     value = "%" + value;
               }
            }            
         }
      }
         
      return value;
   }

   /**
    * Validates that the value supplied for the search field is correct for the
    * field's type. Currently only validate that numeric values are supplied to
    * a field of type {@link PSSearchField#TYPE_NUMBER}.
    * 
    * @param field The field, may not be <code>null</code>.
    * @param translator the translator used to internationalize the error
    * message, if <code>null</code> is supplied, <code>PSI18nUtils</code>
    * will be used as translator along with the specified locale.
    * @param locale the locale for which to internationalize the error message,
    * may be <code>null</code> or empty in which case the default locale is
    * used. Ignored if the <code>translator</code> is not <code>null</code>.
    * 
    * @return <code>null</code> if the values are valid, otherwise a non-
    * <code>null</code> internationalized error message.
    */
   public static String validateSearchFieldValue(PSSearchField field,
      PSI18NTranslationKeyValues translator, String locale)
   {
      if (field == null)
         throw new IllegalArgumentException("field may not be null");
      
      String msg = null;
      
      if (!field.usesExternalOperator() && 
         field.getFieldType().equalsIgnoreCase(PSSearchField.TYPE_NUMBER))
      {
         Iterator values = field.getFieldValues().iterator();
         while (values.hasNext() && msg == null)
         {
            String val = values.next().toString();
            if (val.trim().length() == 0)
               continue;
            
            try
            {
               Float.parseFloat(val);
            }
            catch (NumberFormatException e)
            {
               String key = PSSearchFieldOperators.class.getName() + 
                  "@NonNumericFieldValueException";
               if (translator == null)
               {
                  if (locale != null && locale.trim().length() > 0)
                     msg = PSI18nUtils.getString(key, locale);
                  else
                     msg = PSI18nUtils.getString(key);
               }
               else
                  msg = translator.getTranslationValue(key);
               
               Object[] args = {val, field.getFieldName()};
               msg = MessageFormat.format(msg, args);               
            }            
         }         
      }
      
      return msg;
   }
   
   /**
    * Do not instantiate this class. Use it's static members instead.
    */
   private PSSearchFieldOperators()
   {
   }
   
   /**
    * Get the operators list for the supplied field.
    * 
    * @param field the search field for which to get the operators list, 
    *    assumed not <code>null</code>.
    * @return a list of I18N key's with all operators supported for the 
    *    supplied search field, never <code>null</code>, may be empty if the 
    *    type of the supplied search field is not supported.
    */
   private static List getOperatorsList(PSSearchField field)
   {
      List operators = new ArrayList();
      
      if (field.isDateValue())
         operators = ms_dateOperators;
      else if (field.isNumberValue())
         operators = ms_numberOperators;
      else if (field.isTextValue())
         operators = ms_textOperators;
         
      return operators;
   }

   /**
    * A list of operators used for search fields of type <code>Date</code>. The
    * list contains the I18N key's used to lookup the localized strings used
    * in user interfaces.
    */
   private static final List ms_dateOperators = new ArrayList();
   static
   {
      ms_dateOperators.add(PSCommonSearchUtils.OP_ON);
      ms_dateOperators.add(PSCommonSearchUtils.OP_BEFORE);
      ms_dateOperators.add(PSCommonSearchUtils.OP_AFTER);
      ms_dateOperators.add(PSCommonSearchUtils.OP_BETWEEN);
   };

   /**
    * A list of operators used for search fields of type number.
    * @see PSSearchFieldOperators#ms_dateOperators for more info.
    */
   private static final List ms_numberOperators = new ArrayList();
   static
   {
      ms_numberOperators.add(PSCommonSearchUtils.OP_EQUALS);
      ms_numberOperators.add(PSCommonSearchUtils.OP_GREATER_THAN);
      ms_numberOperators.add(PSCommonSearchUtils.OP_LESS_THAN);
      ms_numberOperators.add(PSCommonSearchUtils.OP_BETWEEN);
   };

   /**
    * A list of operators used for search fields of type text.
    * @see PSSearchFieldOperators#ms_dateOperators for more info.
    */
   private static final List ms_textOperators = new ArrayList();
   static 
   {
      ms_textOperators.add(PSCommonSearchUtils.OP_STARTS_WITH);
      ms_textOperators.add(PSCommonSearchUtils.OP_CONTAINS);
      ms_textOperators.add(PSCommonSearchUtils.OP_ENDS_WITH);
      ms_textOperators.add(PSCommonSearchUtils.OP_EXACT);
   };
}
