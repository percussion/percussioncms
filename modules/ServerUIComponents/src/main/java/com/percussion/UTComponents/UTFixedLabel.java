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
 * Just like a standard label, with a slight change in the resizing
 * behavior. The height is maintained at a constant value as defined by
 * IUTConstants.FIXED_HEIGHT.
 */
public class UTFixedLabel extends JLabel
{
   public UTFixedLabel( String label, int position )
   { super( label, position );}

   /**
    * Overridden to return constant height for the control. When used w/ a Box,
    * this provides the behavior of taking enough room for the text in the
    * control, but maintaining a nice height.
    */
   public Dimension getPreferredSize()
   {
      return new Dimension( super.getPreferredSize().width,
         getMaximumSize().height );
   }

   /**
    * Overridden to return the min size of the control, which is the underlying
    * minimum size for width, and a constant height, as defined by
    * IUTConstants.FIXED_HEIGHT.
    */
   public Dimension getMinimumSize()
   {
      return new Dimension( super.getMinimumSize().width,
         IUTConstants.MIN_CONTROL_SIZE.height );
   }

   /**
    * Overridden to return the max size of the control, which is the underlying
    * maximum size for width, and a constant height, as defined by
    * IUTConstants.FIXED_HEIGHT.
    */
   public Dimension getMaximumSize()
   {
      return new Dimension( super.getMaximumSize().width,
         IUTConstants.MAX_CONTROL_SIZE.height );
   }
}
