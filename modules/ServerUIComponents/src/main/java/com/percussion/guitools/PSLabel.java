/*[ PSLabel.java ]*************************************************************
 *
 * COPYRIGHT (c) 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

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

