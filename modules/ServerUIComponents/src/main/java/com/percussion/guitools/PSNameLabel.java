/******************************************************************************
*
* [ PSNameLabel.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.guitools;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

/**
 * A label that allows itself to be resized smaller than the contained text
 * 
 * @author dougrand
 */
public class PSNameLabel extends JLabel
{

   /**
    * 
    */
   private static final long serialVersionUID = -6181683380332613367L;

   /**
    * Ctor
    */
   public PSNameLabel() {
      super();
      // TODO Auto-generated constructor stub
   }

   /**
    * Ctor
    * @param text text to display in label
    */
   public PSNameLabel(String text) {
      super(text);
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
      Rectangle2D rect = getFont().getStringBounds(getText(), ctx);
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
}
