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


