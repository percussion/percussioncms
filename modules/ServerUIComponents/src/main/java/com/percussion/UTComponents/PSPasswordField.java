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
