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
