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

package com.percussion.UTComponents;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/** The default password field used by E2.  It displays 14 *'s at construction.
  * Then when selected and changed, it behaves like JPasswordField.
*/

public class PSPasswordField extends JPasswordField implements FocusListener
{
   public PSPasswordField()
   {
     super();
     init(null);
   }

   public PSPasswordField(int columns)
   {
     super(columns);
     init(null);
   }

   public PSPasswordField(String text)
   {
      super(STARTER);
      init(text);
   }

   public PSPasswordField(String text, int columns)
   {
      super(STARTER, columns);
      init(text);
   }

/** Overridden to prevent 14 *'s being passed back when it should be empty.
  * Otherwise, this method behaves just like its parent's.
*/

   public char[] getPassword()
   {
      String text = new String(super.getPassword());

      if (text.equals(STARTER))
      {
            return m_input.toCharArray();
      }
      else
         return super.getPassword();
   }

/** A reset method for reinitializing the passwordField without calling a new one.
  *
  * @param newText could be null, it would then be an empty String.
*/

   public void resetPasswordField(String newText)
   {
      if (newText == null)
         newText = "";

      setText(STARTER);
      m_input = newText;
      m_isTyped = false;
   }

/** A reset method for reinitializing the passwordField clean, without text in
  * the field.
*/

   public void resetPasswordField()
   {
      setText("");
      m_input = "";
      m_isTyped = false;
   }

  


/** If this field gained the focus, select all the text.
*/
   public void focusGained(FocusEvent e)
   {
     this.selectAll();
   }

   public void focusLost(FocusEvent e) {}

/** Default initialization.
  *
  * @param pw The password passed in. Can be null.
*/
   private void init(String pw)
   {
     addFocusListener(this);

     if (pw != null)
       m_input = pw;
   }


   private String m_input = "";

   private boolean m_isTyped = false;

   private static final String STARTER = "**************";
}
