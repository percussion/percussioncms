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
 * Just like a standard text field with a slight change in the resizing
 * behavior. When used with a Box layout, it will take up all available width,
 * but is limited in height.
 */
public class UTFixedHeightTextField extends JTextField
{
   /**
    * Overridden to return the preferred size for the control. 
    */
   public Dimension getPreferredSize()
   {
      return IUTConstants.PREF_CONTROL_SIZE;
   }

   /**
    * Overridden to return the min size of the control, as defined by
    * IUTConstants.MIN_CONTROL_SIZE. When used with the Box layout mgr, this
    * provides the behavior of the control not shrinking beyond a default
    * width, while maintaining a nice height as the container is resized.
    */
   public Dimension getMinimumSize()
   {
      return IUTConstants.MIN_CONTROL_SIZE;
   }

   /**
    * Overridden to return the max size of the control, as defined by
    * IUTConstants.MAX_CONTROL_SIZE. When used with the Box layout mgr, this
    * provides the behavior of taking up all the width, but maintaining a nice
    * height as the container is resized.
    */
   public Dimension getMaximumSize()
   {
      return IUTConstants.MAX_CONTROL_SIZE;
   }
}

