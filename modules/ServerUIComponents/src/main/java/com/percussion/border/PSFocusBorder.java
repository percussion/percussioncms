/*[ PSFocusBorder.java ]******************************************************
 *
 * COPYRIGHT (c) 2004 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.border;

import com.percussion.cx.PSDisplayOptions;
import javafx.embed.swing.*;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * @author dougrand
 *
 * This border is drawn only when the component has focus. Components without
 * focus have an empty border drawn. The border uses the supplied color, but 
 * if no color is specified it will use the component's foreground color. If 
 * there is no foreground color then red is used.
 * 
 * This class also implements a {@link FocusListener} to enable the component
 * to repaint the border on focus changes.
 */
public class PSFocusBorder extends AbstractBorder implements FocusListener
{
   /**
    * Ctor that calls {@link #PSFocusBorder(int, PSDisplayOptions, boolean)
    * PSFocusBorder(thickness, options, false)}.
    * @param thickness see other constructor
    * @param options see other constructor
    */
   public PSFocusBorder(int thickness, PSDisplayOptions options)
   {
      this(thickness, options, false);
   }
   
   /**
    * Create a new focus border using the given display options. See
    * @{link #PSFocusBorder(int, Color) } for more details.
    * 
    * @param thickness the thickness of the border, must be zero or
    * larger.
    * @param options the options, may be <code>null</code>.
    * @param always if this is <code>true</code> then the border
    * is always painted, without regard for the components 
    * {@link Component#hasFocus()} method. 
    */
   public PSFocusBorder(int thickness, PSDisplayOptions options, boolean always)
   {
      if (options == null)
      {
         init(thickness, null);
      }
      else
      {
         init(thickness, options.getFocusColor());
      }
      
      m_always = always;
   }
   
   /**
    * See {@link #PSFocusBorder(int, Color, boolean) 
    * PSFocusBorder(thickness, highlight, false)}
    * @param thickness see other ctor
    * @param highlight see other ctor
    */
   public PSFocusBorder(int thickness, Color highlight)
   {
      this(thickness, highlight, false);
   }
   
   /**
    * Create a new focus border 
    * 
    * @param thickness the thickness of the border, must be zero or
    * larger
    * @param highlight the color to use when drawing the border on a
    * component that has focus
    * @param always if this is <code>true</code> then the border
    * is always painted, without regard for the components 
    * {@link Component#hasFocus()} method. 
    */
   public PSFocusBorder(int thickness, Color highlight, boolean always)
   {
      if (thickness < 0)
      {
         throw new IllegalArgumentException("Thickness must not be negative");
      }
      
      init(thickness, highlight);
      m_always = always;
   }

   /* (non-Javadoc)
    * @see javax.swing.border.Border#paintBorder(java.awt.Component, java.awt.Graphics, int, int, int, int)
    */
   public void paintBorder(Component c, Graphics g, int x, int y,
      int width, int height)
   {      
      if (c instanceof JCheckBox)
      {
         JCheckBox checkbox = (JCheckBox)c;
         if (!checkbox.isBorderPainted())
            checkbox.setBorderPainted(true);
         c.repaint();
      }
      if (c.hasFocus() || m_always)
      {
         // Set color
         if (m_color != null)
         {
            g.setColor(m_color);
         }
         else
         {
            g.setColor(Color.red);
         }
         
         Stroke dottedstroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, 
            BasicStroke.JOIN_MITER, 1.0f, new float[] { 1.0f, 1.0f }, 0.0f);
         Stroke dottedstroke2 = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, 
            BasicStroke.JOIN_MITER, 1.0f, new float[] { 1.0f, 1.0f }, 1.0f);
            
         // Draw focus rectangle
         for(int i = 0; i < m_insets.left; i++)
         {
            Stroke save = null;
            
            if (g instanceof Graphics2D)
            {
               Graphics2D g2d = (Graphics2D) g;
               save = g2d.getStroke();
               if (x % 2 == 0)
               {
                  g2d.setStroke(dottedstroke);
               }
               else
               {
                  g2d.setStroke(dottedstroke2);
               }
               
            }
            g.drawRect(x + i, y + i, width - i - i - 1, height - i - i - 1);
            if (save != null)
            {
               ((Graphics2D) g).setStroke(save);
            }
         }
      }
   }

   /* (non-Javadoc)
    * @see javax.swing.border.Border#getBorderInsets(java.awt.Component)
    */
   public Insets getBorderInsets(Component arg0)
   {
      // XXX Auto-generated method stub
      return m_insets;
   }

   /* (non-Javadoc)
    * @see javax.swing.border.Border#isBorderOpaque()
    */
   public boolean isBorderOpaque()
   {
      return true;
   }

   /* (non-Javadoc)
    * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
    */
   public void focusGained(FocusEvent e)
   {
      doRepaint(e.getComponent());
   }

   /* (non-Javadoc)
    * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
    */
   public void focusLost(FocusEvent e)
   {
      doRepaint(e.getComponent());
   }
   
   /**
    * Add this focus border to the given component.
    * @param c a component, must never be <code>null</code>.
    * @param override If <code>true</code>, then install this
    * border on a component regardless of class. If <code>false</code> then
    * only install this on components for whom 
    * {@link #isHandleClass(JComponent)} returns <code>true</code>.
    */
   public void addToComponent(JComponent c, boolean override)
   {
      if (c == null)
      {
         throw new IllegalArgumentException("c must never be null");
      }
      try
      {
         Border current = c.getBorder();
         // Do nothing if the control already has a focus border, or
         // a compound border with a focus border
         if (current instanceof PSFocusBorder)
         {
            return;
         }
         else if (current instanceof CompoundBorder)
         {
            CompoundBorder cb = (CompoundBorder) current;
            if (cb.getOutsideBorder() instanceof PSFocusBorder)
            {
               return;
            }
         }
         if (isHandleClass(c) || override)
         {
            
            if (c instanceof JCheckBox)
            {
               // Border not enabled on JCheckBox
               ((JCheckBox)c).setBorderPainted(true);
               c.setBorder(this);
            }       
            else {
               CompoundBorder cb = new CompoundBorder(this, c.getBorder());
               c.setBorder(cb);
            }
            c.addFocusListener(this);
         }
      }
      catch(IllegalArgumentException e)
      {
         // Ignore, borders are not supported on all components
      }
   }
   
   /**
    * Is this passed component an instance of a class that we want to 
    * put a new focus border on?
    * @param c The component, assumed not <code>null</code>
    * @return <code>true</code> if this component type should get a
    * focus border.
    */
   private boolean isHandleClass(JComponent c)
   {
      return
         c instanceof JMenuBar ||
         c instanceof JMenu ||
         c instanceof JFXPanel ||
         c instanceof JTextArea ||
         c instanceof JCheckBox ||
         c instanceof JTextField ||
         c instanceof JComboBox ||
         c instanceof JSplitPane ||
         c instanceof JButton ||
         c instanceof JScrollBar ||
         c instanceof JList ||
         (c instanceof JLabel && ((JLabel)c).isFocusable()) ||
         (c instanceof JPanel && ((JPanel)c).isFocusable())  ||
         c instanceof JSlider;
   }

   /**
    * Add the focus border to this component and any contained components
    * that appear to be focusable. If this component is a container, then
    * add a listener so any newly added widgets will have the focus border
    * added as well.
    * @param c a component, perhaps a container, must never be <code>null</code>
    */
   public void addToAllNavigable(JComponent c)
   {
      if (c == null)
      {
         throw new IllegalArgumentException("c must never be null");
      }
      if (c.isFocusable())
      {
         addToComponent(c, false);
      }
      if (c instanceof Container)
      {
         // Walk all children
         Container container = (Container) c;
         Component children[] = container.getComponents();
         for(int i = 0; i < children.length; i++)
         {
            if (children[i] instanceof JComponent)
            {
               addToAllNavigable((JComponent) children[i]);
            }
         }
         
         // Remove any existing before adding a container listener
         container.removeContainerListener(m_containerListener);
         container.addContainerListener(m_containerListener);
      } 
   }
    
   /**
    * Do an appropriate repaint for the given component. Just repaint the 
    * borders.
    * @param c Component, assumed to be not <code>null</code>.
    */
   protected void doRepaint(Component c)
   {
      long delay = 200; // 200 ms

      c.repaint(delay); 
   }
      
   /**
    * Initialize the instance with the given values 
    * 
    * @param thickness the thickness of the border, must be zero or
    * larger
    * @param highlight the color to use when drawing the border on a
    * component that has focus
    */
   protected void init(int thickness, Color highlight)
   {
      m_insets = new Insets(thickness, thickness, thickness, thickness);
      m_color = highlight;
   }

   /**
    * Initialized in ctor, never <code>null</code> afterward. Contains the 
    * thickness of the border on each side of the box.
    */
   private Insets m_insets = null;

   /**
    * The color to use when drawing the border on a component that has
    * focus. Initialized in the ctor, the value may be <code>null</code>.
    */
   private Color m_color = null;
   
   /**
    * If this is <code>true</code> then always paint the border without
    * regard to the focus state of the widget. Useful for things that 
    * don't really take focus like tree labels.
    */
   private boolean m_always = false;
   
   /**
    * This container listener is used to handle updates (the addition or 
    * removal of components in a container). This is initialized here and
    * never modified or <code>null</code>.
    */
   private ContainerListener m_containerListener = 
   new ContainerListener() {
      public void componentAdded(ContainerEvent e)
      {
         if (e.getComponent() instanceof JComponent)
         {
            addToAllNavigable((JComponent) e.getComponent());
         }
      }
   
      public void componentRemoved(ContainerEvent e)
      {
         // Ignore
      }
   };

}
