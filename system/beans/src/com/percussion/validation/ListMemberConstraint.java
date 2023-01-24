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

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Constraint for validating String-based component values to validate that the
 * component value is not one of the supplied list of values.
 * 
 * @see ValidationConstraint
 */
public class ListMemberConstraint implements ValidationConstraint
{
                      
   /**
    * Default ctor for this type of constraint.
    *
    * @param existing A list of names against which to validate. The 
    * control&apos;s text cannot appear in this list. The objects in the list 
    * are converted to strings using <code>toString()</code>. <code>null</code>
    * entries are ignored.
    *
    * @param caseSensitive If <code>true</code>, the check is performed using
    * case sensitive comparison.
    *
    */
   public ListMemberConstraint( Collection existing, boolean caseSensitive )
   {
      if ( null == existing)
      {
         existing = new ArrayList();
      }
      m_caseSensitive = caseSensitive;
      m_existingElems = existing;
   }

   /**
    * A convenience method. Equivalent to <code>this( existing, false )</code>.
    * See {@link #ListMemberConstraint( Collection, boolean) this} for a
    * description.
    */
   public ListMemberConstraint( Collection existing )
   {
      this( existing, false );
   }


/* ################ ValidationConstraint interface impl ################## */

   //implements interface method
   public String getErrorText()
   {
      if ( null == m_errorMsg )
      {
         StringBuilder buf = new StringBuilder( 400 );

         // Error checking should never happen if the collection of existing
         // names is null or empty, but take care anyway.
         if (!(null == m_existingElems  || m_existingElems.size() < 1))
         {
            Iterator iter = m_existingElems.iterator();
            boolean first = true;
            while ( iter.hasNext())
            {
               if ( !first )
                  buf.append( ", " );
               buf.append( iter.next().toString());
               first = false;
            }
         }

         String [] args = new String[]
         {
            buf.toString()
         };
         String pattern = ms_res.getString( "uniqueListConstraintError" );
         m_errorMsg = MessageFormat.format( pattern, args );
      }
      return m_errorMsg;
   }

   /**
    * Validates the supplied component by checking its text against the list
    * of names that were supplied with the constructor. If any matches are
    * found, the validation fails and an exception is thrown.
    *
    * @param suspect A JTextComponent or JComboBox to check.
    *
    * @throws IllegalArgumentException if component is not an expected component
    * @throws ValidationException if the text from the component is found
    * on the disallowed list.
    */
   public void checkComponent( Object suspect )
      throws ValidationException
   {

      // If the collection of elements is null or empty, just return
      if (null == m_existingElems  || m_existingElems.size() < 1)
      {
         return;
      }

      String text = null;
      if (suspect instanceof JTextComponent)
      {
         JTextComponent tf = (JTextComponent)suspect;
         if ( null != tf.getDocument())
            text = tf.getText();
         else
            text = "";
      }
      else if (suspect instanceof JComboBox)
         text = new String(((JComboBox)suspect).getSelectedItem().toString());
      else   // this should never happen... 
         throw new IllegalArgumentException(
                             "Component null or not text field or combo box" );

      // begin validation
      Iterator iter = m_existingElems.iterator();
      while ( iter.hasNext())
      {
         boolean found = false;
         String existing = iter.next().toString();
         if ( m_caseSensitive )
            found = existing.equals( text );
         else
            found = existing.equalsIgnoreCase( text );
         if ( found )
            throw new ValidationException();
      }
   }

   /**
    * A list of 1 or more elements that contain the names that the component
    * attached to this constraint can't use, initialized in the constructor and
    * never modified after that.
    */
   private Collection m_existingElems;

   /**
    * Flag indicating whether the comparisons between the value and the list
    * are case sensitive or not. If <code>true</code>, then the comparisons
    * are case sensitive. Set in the constructor and never modified after that.
    */
   private boolean m_caseSensitive;

   /**
    * <code>null</code> until the first time {@link #getErrorText() 
    * getErrorText} is called. Then it caches the dynamically created message 
    * for subsequent calls.
    */
   private String m_errorMsg = null;
   
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


