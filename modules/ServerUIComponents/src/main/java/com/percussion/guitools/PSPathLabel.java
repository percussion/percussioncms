/******************************************************************************
 *
 * [ PSPathLabel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
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
    * @see javax.swing.JLabel#getText()
    */
   @Override
   public String getText()
   {
      return super.getText();
   }

   /**
    * (non-Javadoc)
    * 
    * @see javax.swing.JLabel#setText(java.lang.String)
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
    * @see javax.swing.JComponent#getMaximumSize()
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
    * @see javax.swing.JComponent#getMinimumSize()
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
    * @see javax.swing.JComponent#getPreferredSize()
    */
   @Override
   public Dimension getPreferredSize()
   {
      return getMaximumSize();
   }

   /** (non-Javadoc)
    * @see java.awt.Component#setBounds(int, int, int, int)
    */
   @Override
   public void setBounds(int x, int y, int width, int height)
   {
      super.setBounds(x, y, width, height);
      calculateVisibleText();
   }
}
