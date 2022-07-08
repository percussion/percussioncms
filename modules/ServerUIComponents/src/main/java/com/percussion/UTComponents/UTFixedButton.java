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
import java.awt.*;

/**
 * A fixed sized JButton.
 */
////////////////////////////////////////////////////////////////////////////////
public class UTFixedButton extends JButton
{
   /**
    * The size of button used by default by this class.
    */
   public static final Dimension STANDARD_BUTTON_SIZE = new Dimension(80, 24);

   /**
   * Construct a new fixed button with the standard size.
   *
   * @param string      the button name
    */
  //////////////////////////////////////////////////////////////////////////////
   public UTFixedButton(String name)
   {
     super(name);
    setPreferredSize(STANDARD_BUTTON_SIZE);
   }

   /**
   * Construct a new fixed button of passed size.
   *
   * @param string      the button name
   * @param size         the button size
    */
  //////////////////////////////////////////////////////////////////////////////
   public UTFixedButton(String name, Dimension size)
   {
     super(name);
    setPreferredSize(size);
   }

   /**
   * Construct a new fixed button of passed width/height
   *
   * @param string      the button name
   * @param width         the button width
   * @param height      the button height
    */
  //////////////////////////////////////////////////////////////////////////////
   public UTFixedButton(String name, int width, int height)
   {
     super(name);
    setPreferredSize(new Dimension(width, height));
   }
   /**
   * Construct a new fixed button with the standard size.
   *
   * @param icon      the button icon
    */
  //////////////////////////////////////////////////////////////////////////////
   public UTFixedButton(ImageIcon icon)
   {
     super(icon);
    setPreferredSize(STANDARD_BUTTON_SIZE);
   }

   /**
   * Construct a new fixed button of passed size.
   *
   * @param icon      the button icon
   * @param size         the button size
    */
  //////////////////////////////////////////////////////////////////////////////
   public UTFixedButton(ImageIcon icon, Dimension size)
   {
     super(icon);
    setPreferredSize(size);
   }

   /**
   * Construct a new fixed button of passed width/height
   *
   * @param icon         the button icon
   * @param width         the button width
   * @param height      the button height
    */
  //////////////////////////////////////////////////////////////////////////////
   public UTFixedButton(ImageIcon icon, int width, int height)
   {
     super(icon);
    setPreferredSize(new Dimension(width, height));
   }

   /**
   * Make size fix.
   *
    */
  //////////////////////////////////////////////////////////////////////////////
   public Dimension getMinimumSize()
   {
      return getPreferredSize();
   }

   /**
   * Make size fix.
   *
    */
  //////////////////////////////////////////////////////////////////////////////
   public Dimension getMaximumSize()
   {
      return getPreferredSize();
   }
}
