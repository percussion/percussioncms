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
