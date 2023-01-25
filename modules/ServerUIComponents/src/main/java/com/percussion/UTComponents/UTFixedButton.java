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
