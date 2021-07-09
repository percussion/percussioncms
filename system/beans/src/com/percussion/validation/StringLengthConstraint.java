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
package com.percussion.validation;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;

/**
 * Constraint for validating String-based component values.
 * @see ValidationConstraint
 */
public class StringLengthConstraint implements ComponentValidationConstraint
{
   /**
    * Constructs a new maximum length constraint.
    * 
    * @param maxLen the maximum length to be enforced, must be > 0.
    */
   public StringLengthConstraint(int maxLen)
   {
      if (maxLen < 1)
         throw new IllegalArgumentException(
            "Maximum length must be greater than 0");
      
      m_maxLen = maxLen;
   }

   // implementing interface ValidationConstraint
   public String getErrorText()
   {
      return getErrorText(null);
   }

   // implementing interface ComponentValidationConstraint
   public String getErrorText(String label)
   {
      if (label == null || label.trim().length() == 0)
         label = "?";
      label = "<" + label.trim() + ">";

      Object[] args =
      {
         label,
         Integer.toString(m_maxLen)
      };
      return MessageFormat.format(
         ms_res.getString("stringlengthconstraint.exceeds"), args);
   }

   // implementing interface ValidationConstraint
   public void checkComponent(Object suspect) throws ValidationException
   {
      // assume the supplied text is empty
      String text = "";
      if (suspect instanceof JTextComponent)
      {
         JTextComponent test = (JTextComponent) suspect;
         if (test.getDocument() != null)
            text = test.getText();
      }
      else if (suspect instanceof JComboBox)
      {
         JComboBox test = (JComboBox) suspect;
         if (test.getSelectedItem() != null)
            text = test.getSelectedItem().toString();
      }
      else
      {
         // this should never happen
         throw new IllegalArgumentException(
            "Component null or not text field or combo box");
      }

      // begin validation
      if (text.length() > m_maxLen)
         throw new ValidationException();
   }

   /**
    * Tha maximum allowed lenght, initialized in constructor and never changed
    * after that. Always > 0.
    */
   private int m_maxLen;

   /**
    * The validation framework resource bundle. Initialized while constructed,
    * never changed after that.
    */
   private static ResourceBundle ms_res = null;
   static
   {
      ms_res = ResourceBundle.getBundle(
         ValidationFramework.VALIDATION_RESOURCES, Locale.getDefault());
   }
}


