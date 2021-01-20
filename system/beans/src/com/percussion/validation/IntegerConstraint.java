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

package com.percussion.validation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JComboBox;
import javax.swing.JTextField;

/** 
 * Handles integer validation includes the component value to be a valid integer
 * and with in the range if the bounds are provided.
 *
 * @see java.lang.Integer
 */
public class IntegerConstraint implements ComponentValidationConstraint
{

   /** 
    * Constructs a basic <code>IntegerConstraint</code> object that handles only
    * valid integers, no range checking.
    */
   public IntegerConstraint() 
   {
   }

   /** 
    * Constructs this object that handles valid integers and checks the 
    * specified range. Enter <code>Integer.MAX_VALUE</code> for max if no 
    * maximum boundary is required, and enter <code>Integer.MIN_VALUE</code> for
    * min if no minimum boundary is is desired.
    *
    * @param min input for the range minimum. Must be < max.
    * @param max input for the range maximum. Must be > min.
    * 
    * @throws IllegalArgumentException if max is less than or equal to min.
    */
   public IntegerConstraint(int min, int max) 
   {
      if (min < max)
      {
         if (max < Integer.MAX_VALUE)
            m_rangeMax = new Integer(max);  
         if (min > Integer.MIN_VALUE)
            m_rangeMin = new Integer(min);
      }
      else
         throw new IllegalArgumentException(
               "Maximum can not be less than or equal to minimum");
  }

   //see interface for description
   public String getErrorText()
   {
      return getErrorText(null);   
   }

   // implementing definition from interface ComponentValidationConstraint
   public String getErrorText(String label)
   {
      if (null == label || label.trim().length() == 0)
      {
         label = "";
      }
      else
      {
         if (!label.endsWith(":"))
            label += ":";
      }
      List args = new ArrayList();
      args.add(label);
      String key = null;
      if (null != m_invalidNumber)
      {
         if (m_invalidNumber.trim().length() > 0)
         {
            key = "notInteger";
            args.add(m_invalidNumber);
         }
         else
         {
            key = "missingInteger";
         }
      }
      if (null == m_invalidNumber)
      {
         if (null != m_rangeMin)
         {
            if (null == m_rangeMax)
               key = "lessThanMin";
            args.add(m_rangeMin);
         }
         if (null != m_rangeMax) 
         {
            if (null == m_rangeMin)
               key = "moreThanMax";
            args.add(m_rangeMax);
         }
         if (null == key)
         {
            key = "notInRange";
         }
      }
      return MessageFormat.format(ms_res.getString(key), args.toArray());
   }

   // implementing definition from interface ValidationConstraint
   public void checkComponent(Object suspect) 
      throws ValidationException
   {
      m_invalidNumber = null;
      Integer value;
      String enteredText = null;
      
      // Validating the input for correct integer input 
      if (suspect instanceof JTextField)
      {
         try 
         {
            enteredText = ((JTextField)suspect).getText();
            value = new Integer(enteredText);
         }
         catch (NumberFormatException e)
         {
            m_invalidNumber = enteredText;
            throw new ValidationException();
         }
      }
      else if (suspect instanceof JComboBox)
      {
         try 
         {
            enteredText = ((JComboBox)suspect).getEditor().getItem().toString();
            value = new Integer(enteredText);
         }
         catch (NumberFormatException e)
         {
            m_invalidNumber = enteredText;
            throw new ValidationException();
         }
      }
      else   
         throw new IllegalArgumentException(
            "suspect must be an instance of JTextField or JComboBox");
      
      // Making sure the value is within range provided in constraints
      if ((null != m_rangeMin && value.intValue() < m_rangeMin.intValue())
         || (null != m_rangeMax && value.intValue() > m_rangeMax.intValue()))
      {
         throw new ValidationException();         
      }
   }

   /**
    * The integer rerpresenting the allowed maximum value, initialized to <code>
    * null</code> and set with maximum value to be checked if it is 
    * constructed using {@link #IntegerConstraint(int, int) }. If not 
    * <code>null</code>, its value will not be <code>Integer.MAX_VALUE</code>.
    */
   private Integer  m_rangeMax = null;
   
   /**
    * The integer rerpresenting the allowed minimum value, initialized to <code>
    * null</code> and set with minimum value to be checked if it is 
    * constructed using {@link #IntegerConstraint(int, int) }. If not 
    * <code>null</code>, its value will not be <code>Integer.MIN_VALUE</code>.
    */
   private Integer  m_rangeMin = null;
   
   /**
    * Used to pass data between the validation method and the error message
    * generator method. If the user entered a non-integer, it will be placed
    * in this field by the validation method.
    */
   private String m_invalidNumber = null;
   
   /**
    * The static resource bundle to provide the error messages, never <code>null
    * </code>
    */
   private static ResourceBundle ms_res = null;
   static
   {
      try
      {
          ms_res = ResourceBundle.getBundle( 
            "com.percussion.validation.ValidationResources",
                                         Locale.getDefault() );
      }catch(MissingResourceException mre)
      {
          System.out.println( mre );
      }
   }
}

 
