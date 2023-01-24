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

import com.percussion.guitools.BitmapManager;

import javax.swing.*;
import java.awt.*;

/**
 * Creates a small, square button that has 3 dots that is typically used to
 * bring up some sort of browser dialog. The button attempts to maintain a
 * fixed size.
 */
public class UTBrowseButton extends JButton
{
   /**
    * The standard ctor for the browse button object.
    */
   public UTBrowseButton()
   {
      ImageIcon icon = BitmapManager.getBitmapManager(this.getClass()).getImage(
         "images/optional.gif" );
      setIcon( icon );
      Dimension d = new Dimension( IUTConstants.FIXED_HEIGHT,
         IUTConstants.FIXED_HEIGHT );
      setSize( d );
      setMaximumSize( d );
      setMinimumSize( d );
      setPreferredSize( d );
      setAlignmentY( CENTER_ALIGNMENT);
   }
}
