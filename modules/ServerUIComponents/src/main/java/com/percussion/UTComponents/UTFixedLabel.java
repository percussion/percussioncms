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
