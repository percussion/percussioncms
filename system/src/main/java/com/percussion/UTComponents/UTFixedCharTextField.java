/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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
package com.percussion.UTComponents;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;

/**
 * Just like a standard text field except for restricting the number of
 * characters in the field.
 */
public class UTFixedCharTextField extends JTextField
{
   /**
    * Ctor for creating a fixed character text field.
    * 
    * @param maxChars int greater than 0.
    */
   public UTFixedCharTextField(int maxChars) 
   {
      setDocument(new LimitingDocument(maxChars));     
   }
   
   /**
    * Set the error texts.
    * 
    * @param title the title for the error message dialog, not <code>null</code>
    *    or empty.
    * @param message the error message to show if to many characters are
    *    entered, not <code>null</code> or empty.
    * @param parent the parent component for the error message dialog, may
    *    be <code>null</code>.
    */
   public void setError(String title, String message, Component parent)
   {
      if (title == null)
         throw new IllegalArgumentException("title cannot be null");
         
      title = title.trim();
      if (title.length() == 0)
         throw new IllegalArgumentException("title cannot be empty");
         
      if (message == null)
         throw new IllegalArgumentException("message cannot be null");
         
      message = message.trim();
      if (message.length() == 0)
         throw new IllegalArgumentException("message cannot be empty");
         
      m_errorTitle = title;
      m_errorMessage = message;
      m_parent = parent;
   }
   
   /**
    * Is this text field a valid mnemonic for the supplied label?
    * 
    * @param label the label to test against, not <code>null</code>.
    * @return <code>true</code> if valid, <code>false</code> otherwise.
    */
   public boolean isValidMnemonic(String label)
   {
      if (getText().length() > 0)
      {
         String normLabel = label.toUpperCase();
         String normText = getText().toUpperCase();
         if (normLabel.indexOf(normText.charAt(0)) == -1)
            return false;
      }
      
      return true;
   }
   
   /**
    * Inner class to limit the number of chars in this text field.
    */
   class LimitingDocument extends PlainDocument
   {
      /**
       * Construct the limiting document.
       * 
       * @param maxChars the maximal number of characters allowed for this
       *    document, must be > 0.
       */
      public LimitingDocument(int maxChars)
      {
         if (maxChars < 1)
            throw new IllegalArgumentException(
               "maxChars must be greater than 0");

         mi_maxChars = maxChars;
      }
      
      /**
       * Overwritten to limit the number of characters entered to the one 
       * specified at construction time.
       * 
       * See {@link PlainDocument#insertString(int, String, AttributeSet)} for
       * parameter description.
       */
      public void insertString(int offs, String str, AttributeSet a)
         throws BadLocationException
      {
         /*  
          * If the String length plus the current document length is
          * greater than the maximum number allowed, don't do the insert
          * and beep to notify the user.
          */
         if ((str.length() + this.getLength()) > mi_maxChars)
         {
            Toolkit.getDefaultToolkit().beep();
            
            if (m_errorTitle != null && m_errorMessage != null)
            {
               JOptionPane.showMessageDialog(null, m_errorMessage, m_errorTitle, 
                  JOptionPane.ERROR_MESSAGE);
            }
         }
         else
         {
            super.insertString(offs, str, a);
         }
      }
      
      /**
       * The maximum number of characters allowed in this text field, 
       * initialized at construction time, > 0 and never chanegd after that.
       */
      private int mi_maxChars = 0;
   }
   
   /**
    * The titel for the error message dialog shown if to many characters are
    * entered. Initialized or updated through {@link setError(String, String)},
    * may be <code>null</code> but not empty.
    */
   private String m_errorTitle = null;
   
   /**
    * The error message shown if to many characters are entered. Initialized 
    * or updated through {@link setError(String, String)}, may be 
    * <code>null</code> but not empty.
    */
   private String m_errorMessage = null;
   
   /**
    * The parent component for the error message dialog. Initialized 
    * or updated through {@link setError(String, String, Component)}, may be 
    * <code>null</code>.
    */
   private Component m_parent = null;
}
