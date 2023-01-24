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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;

/** 
 * Constraint for validating String-based component values. Validates empty 
 * strings and invalid characters if the invalid characters are specified.
 *
 * @see ValidationConstraint
 */
public class StringConstraint implements ComponentValidationConstraint
{
   /** 
    * Constructs the object to do basic validation for checking empty component
    * value.
    */
   public StringConstraint()
   {
      m_invalidChar = null;
   }

   /** 
    * Constructs this object to validate all the invalid characters in the 
    * supplied string do not present in the component value in addition to empty
    * string validation.
    * 
    * @param s the string with invalid characters, may not be <code>null</code>
    * or empty.
    */
   public StringConstraint(String s)
   {
      if(s == null || s.trim().length() == 0)
         throw new IllegalArgumentException("s may not be null or empty.");
         
      m_invalidChar = s;
   }

   // implementing interface ComponentValidationConstraint
   public String getErrorText(String label)
   {
      if(label == null || label.trim().length() == 0)
         label = "";
      if (label.endsWith(":"))
         label = label.substring(0, label.length()-1);
      List args = new ArrayList();
      args.add(label);
      String key;
      if (m_errorMsg[0] == null)
      {
         key = "emptyField";
      }
      else
      {
         key = "invalidChar";
         args.add(m_errorMsg[0]);
         args.add(m_invalidChar);
      }
      return MessageFormat.format(ms_res.getString(key), args.toArray()); 
   }
   
   // implementing interface ValidationConstraint
   public String getErrorText()
   {
      return getErrorText(null);
   }
   

   // implementing interface ValidationConstraint
   public void checkComponent(Object suspect) throws ValidationException
   {
      String data;
      // initializing data
      if (suspect instanceof JTextComponent)
      {
         JTextComponent c = (JTextComponent) suspect;
         if ( null != c.getDocument())
            data = c.getText();
         else
            data = "";
      }
      else if (suspect instanceof JComboBox)
      {
         Object o = ((JComboBox)suspect).getSelectedItem();
         if ( null == o )
            data = "";
         else
            data = o.toString();
      }
      else   // this should never happen... 
         throw new IllegalArgumentException( 
            "Component null or not text field or combo box" );
      
      if ( null == data )
         data = "";
         
      // begin validation
      if (m_invalidChar != null)
      {
         for (int j = 0; j < data.length(); j++)
         {
            for (int i = 0; i < m_invalidChar.length(); i++)
            {
               if (data.charAt(j) == m_invalidChar.charAt(i))
               {
                  m_errorMsg[0] = new String(String.valueOf(data.charAt(j)));
                  throw new ValidationException();
               }            
            }
         }
      }
      
      // if component is empty      
      if ( data.trim().length() == 0 )  
      {
         m_errorMsg[0] = null;
         throw new ValidationException();
      }
   }

   /**
    * The string representing not allowed characters in the component value. 
    * Initialized to <code>null</code> and set with a value if it is 
    * constructed using {@link #StringConstraint(String) }. 
    */
   private String m_invalidChar;
   
   /**
    * The array of error messages, initally set with <code>null</code>s for its
    * elements and may be modified with actual error messages when the 
    * components to validate fails on validation.
    */
   private Object[] m_errorMsg = {null};

   /** 
    * A string of invalid characters that is typically not used in the normal
    * identifier convention.
    */
   public static final String NO_SPECIAL_CHAR = 
      " ~!@#$%^&*()+`-=[]{}|;':,.<>/?";
   
   /** A string of characters that is not accepted as a part of a class name. */
   public static final String CLASS_NAME_CHAR_ONLY = 
      " ~!@#%^&*()+`-=[]{}|;':,<>/?";

   /**
    * The static resource bundle to provide the error messages.
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

 
