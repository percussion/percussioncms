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

import java.awt.*;

/**
 * A simple interface that contains constant values useful to the UT...
 * classes (and possibly other UI windows/components). All of our classes
 * should be modified to use these constants so they look and behave
 * consistently.
 */
public interface IUTConstants
{
   /**
    * The standard height, in pixels of single-line, text editing controls
    * such as text fields and combo boxes. We use a fixed height on these
    * controls so that as the container is resized, these guys remain the
    * same height (although functional when taller, they are ugly).
    */
   public static final int FIXED_HEIGHT = 20;
   
   /**
    * The constant to indicate preferred width of a field or area.
    */
   public static final int PREF_WIDTH = 200;

   /**
    * A dimension useful for setting up fixed height controls. Use this
    * values to set the maximum size for the control. When used
    * with a Box layout, the width will take up the available width and not
    * be limited like the UTFixed[ComboBox, TextField] controls are.
    */
   public static final Dimension MAX_CONTROL_SIZE = new Dimension( 10000,
      FIXED_HEIGHT );

   /**
    * A dimension useful for setting up fixed height controls. Use this
    * values to set the minimum size for the control. When used
    * with a Box layout, the width will take up the available width but not
    * get narrower than specified here.
    */
   public static final Dimension MIN_CONTROL_SIZE = new Dimension( 40,
      FIXED_HEIGHT );
 
   /**
    * A dimension useful for setting up preferred width and fixed height. Use 
    * this values to set the preferred size for the control.
    */      
   public static final Dimension PREF_CONTROL_SIZE = new Dimension( PREF_WIDTH,
      FIXED_HEIGHT );

} 
