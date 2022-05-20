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

