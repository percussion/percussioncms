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
