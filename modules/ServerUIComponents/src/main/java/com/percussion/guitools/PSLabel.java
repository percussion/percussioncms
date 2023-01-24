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

package com.percussion.guitools;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.Map;

/**
 * The default font used by <code>JLabel</code> (family - Dialog, size - 12,
 * weight - Regular) is unable to display special characters such as TM
 * symbol. This class sets the font family to "Arial" which is able to display
 * such characters. This class mimics all constructors of <code>JLabel</code>
 * for ease of use.
 */
public class PSLabel extends JLabel
{
   // see base class
   public PSLabel()
   {
      super();
      init();
   }

   // see base class
   public PSLabel(Icon image)
   {
      super(image);
      init();
   }

   // see base class
   public PSLabel(Icon image, int horizontalAlignment)
   {
      super(image, horizontalAlignment);
      init();
   }

   // see base class
   public PSLabel(String text)
   {
      super(text);
      init();
   }

   // see base class
   public PSLabel(String text, Icon icon, int horizontalAlignment)
   {
      super(text, icon, horizontalAlignment);
      init();
   }

   // see base class
   public PSLabel(String text, int horizontalAlignment)
   {
      super(text, horizontalAlignment);
      init();
   }

   /**
    * Sets the font family to "Arial". Does not modify any other font
    * attribute.
    */
   private void init()
   {
      Map fontAttr = getFont().getAttributes();
      fontAttr.put(TextAttribute.FAMILY, "Arial");
      setFont(new Font(fontAttr));
   }
}

