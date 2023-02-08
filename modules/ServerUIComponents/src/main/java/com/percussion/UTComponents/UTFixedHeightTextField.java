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

