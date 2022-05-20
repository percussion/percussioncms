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

import com.percussion.utils.string.PSStringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

/**
 * A label that will abbreviate the contents of the label, which is assumed to
 * be a path of some kind. The path contains forward slashes to separate the
 * path components
 * 
 * @author dougrand
 */
public class PSPathLabel extends JTextField
{
   /**
    * Text to display, will be abbreviated according to available size, never
    * <code>null</code>
    */
   protected String m_text = "";

   /**
    * 
    */
   private static final long serialVersionUID = 5446758279307498471L;

   /**
    * Ctor
    */
   public PSPathLabel() {
      super();
      this.setBorder(null);
      this.setEditable(false);
   }

   /**
    * (non-Javadoc)
    * 
    * @see JLabel#getText()
    */
   @Override
   public String getText()
   {
      return super.getText();
   }

   /**
    * (non-Javadoc)
    * 
    * @see JLabel#setText(String)
    */
   @Override
   public void setText(String text)
   {
      m_text = text;
      calculateVisibleText();
   }

   /**
    * Calculate and set visible text according to the currently available size
    * of the control
    */
   private void calculateVisibleText()
   {
      String displayed = PSStringUtils.abbreviatePath(m_text, getSize(),
            getFont());
      super.setText(displayed);
   }

   /**
    * (non-Javadoc)
    * 
    * @see JComponent#getMaximumSize()
    */
   @Override
   public Dimension getMaximumSize()
   {
      Graphics2D g = (Graphics2D) getGraphics();
      FontRenderContext ctx = g.getFontRenderContext();
      Rectangle2D rect = getFont().getStringBounds(m_text, ctx);
      return new Dimension((int) rect.getWidth(), (int) rect.getHeight());
   }

   /**
    * (non-Javadoc)
    * 
    * @see JComponent#getMinimumSize()
    */
   @Override
   public Dimension getMinimumSize()
   {
      Graphics2D g = (Graphics2D) getGraphics();
      FontRenderContext ctx = g.getFontRenderContext();
      return new Dimension(10, (int) getFont().getMaxCharBounds(ctx)
            .getHeight());
   }

   /**
    * (non-Javadoc)
    * 
    * @see JComponent#getPreferredSize()
    */
   @Override
   public Dimension getPreferredSize()
   {
      return getMaximumSize();
   }

   /** (non-Javadoc)
    * @see Component#setBounds(int, int, int, int)
    */
   @Override
   public void setBounds(int x, int y, int width, int height)
   {
      super.setBounds(x, y, width, height);
      calculateVisibleText();
   }
}
