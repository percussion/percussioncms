/******************************************************************************
 *
 * [ PSPropertyPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.guitools;

import com.percussion.layout.PSGridBoxLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The utility class to create a panel with controls to set properties. Uses
 * {@link com.percussion.layout.PSGridBoxLayout PSGridBoxLayout} for laying out
 * the components. Basically this panel will have 2 columns with the left column
 * for label and right column for the control to edit the property value. All
 * the labels added using {@link #addPropertyRow } are aligned to their
 * right and controls are aligned to the left. The label will be center of
 * controls.
 */
public class PSPropertyPanel extends JPanel implements IPSPropertyPanel
{
   /**
    * Creates a panel with {@link com.percussion.layout.PSGridBoxLayout} with 2
    * columns. Uses <code>10</code> for gap between rows or columns.
    */
   public PSPropertyPanel()
   {
      super();
      setLayout(new PSGridBoxLayout(this, 0, 2, 10, 10));
   }

   /* 
    * @see com.percussion.guitools.IPSPropertyPanel#addPropertyRow(java.lang.String, javax.swing.JComponent)
    */
   public void addPropertyRow(String name, JComponent control)
   {
      addPropertyRow(name, new JComponent[] {control} );
   }

   /* 
    * @see com.percussion.guitools.IPSPropertyPanel#addPropertyRow(java.lang.String, javax.swing.JComponent, char)
    */
   public void addPropertyRow(String name, JComponent control, char mnemonic)
   {
      addPropertyRow(name, new JComponent[] {control}, control, mnemonic, null);
   }

   /* 
    * @see com.percussion.guitools.IPSPropertyPanel#addPropertyRow(java.lang.String, javax.swing.JComponent, javax.swing.JComponent, char, java.lang.String)
    */
   public void addPropertyRow(String name, JComponent control,
         JComponent mnControl, char mnemonic, String tooltip)
   {
      addPropertyRow(name, new JComponent[] { control }, mnControl, mnemonic,
            tooltip);
   }
   
   /* 
    * @see com.percussion.guitools.IPSPropertyPanel#addPropertyRow(java.lang.String, javax.swing.JComponent[])
    */
   public void addPropertyRow(String name, JComponent[] controls)
   {
      addPropertyRow(name, controls, null, (char) 0, null);
   }

   /* 
    * @see com.percussion.guitools.IPSPropertyPanel#addPropertyRow(java.lang.String, javax.swing.JComponent[], javax.swing.JComponent, char, java.lang.String)
    */
   public void addPropertyRow(String name, JComponent[] controls,
         JComponent mnemonicControl, char mnemonic, String tooltip)
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty.");

      if(controls == null || controls.length == 0)
         throw new IllegalArgumentException(
            "controls may not be null or empty.");

      List rowComps = new ArrayList();
      
      JLabel label = new JLabel(name, SwingConstants.RIGHT);
      // set mnemonic
      if (mnemonicControl != null && mnemonic != 0)
      {
         if (mnemonicControl instanceof AbstractButton)
            ((AbstractButton)mnemonicControl).setMnemonic(mnemonic);
         else
            label.setLabelFor(mnemonicControl);
         if (tooltip != null && tooltip.trim().length() > 0)
            mnemonicControl.setToolTipText(tooltip);

         label.setDisplayedMnemonic(mnemonic);
      }
      
      // set accessible info
      label.getAccessibleContext().setAccessibleDescription(name);
      label.getAccessibleContext().setAccessibleName(name);
      label.setAlignmentX(m_labelAlignment);
      add(label);
      rowComps.add(label);

      if (controls.length > 1)
      {
         JPanel controlBox = new JPanel();
         controlBox.setLayout(new BoxLayout(controlBox, BoxLayout.Y_AXIS));

         for(int i=0; i<controls.length; )
         {
            JComponent component = controls[i];
            setPreferredSizes(component);

            controlBox.add(component);
            rowComps.add(component);

            if(++i < controls.length)
            {
               Component comp = Box.createVerticalStrut(5);
               controlBox.add(comp);
               rowComps.add(comp);
            }
               
         }
         controlBox.setAlignmentX(LEFT_ALIGNMENT);
         controlBox.setAlignmentY(CENTER_ALIGNMENT);

         add(controlBox);
         rowComps.add(controlBox);
      }
      else 
      {
         addControl(controls[0], LEFT_ALIGNMENT, rowComps);
      }
      
      m_rowList.add(rowComps);
   }

   /* 
    * @see com.percussion.guitools.IPSPropertyPanel#addControlsRow(javax.swing.JComponent, javax.swing.JComponent)
    */
   public void addControlsRow(JComponent leftControl, JComponent rightControl)
   {
      List rowComps = new ArrayList();
      if(leftControl != null)
         addControl(leftControl, RIGHT_ALIGNMENT, rowComps);
      else
      {
         Component comp = Box.createGlue();
         add(comp);
         rowComps.add(comp);
      }         

      if(rightControl != null)
         addControl(rightControl, LEFT_ALIGNMENT, rowComps);
      else
      {
         Component comp = Box.createGlue();
         add(comp);
         rowComps.add(comp);

      }
      m_rowList.add(rowComps);
   }

   /**
    * Adds the control to this panel with the specified x-alignment. The
    * controls will not grow in vertical direction when the panel is resized.
    * The controls which takes up an area to display are wrapped in scroll pane
    * to be able to see data.
    *
    * @param control the control to add, assumed not to be <code>null</code>
    * @param xAlignment the x-alignment, assumed to be one of
    * <code>RIGHT_ALIGNMENT</code>, <code>LEFT_ALIGNMENT</code>, <code>
    * CENTER_ALIGNMENT</code> values.
    * @param rowComps List of row components to which any new controls are 
    * added, assumed not <code>null</code>.
    */
   private void addControl(JComponent control, float xAlignment, List rowComps)
   {
      JComponent component;
      if(control instanceof JTextArea ||
         control instanceof JTable ||
         control instanceof JTree ||
         control instanceof JList)
      {
         //As these controls takes up an area we need to put them in a
         //scroll pane as data in the control crosses the size, we have to
         //able to see the data.
         component = new JScrollPane(control,
                  JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                  JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

      }
      else
      {
         component = control;
      }

      setPreferredSizes(component);
      component.setAlignmentX(xAlignment);
      component.setAlignmentY(CENTER_ALIGNMENT);
      add(component);
      rowComps.add(component);
   }


   /**
    * Sets the maximum, preferred, and minimum sizes of the component. Tries to
    * keep constant value in height for text components and standard size for
    * buttons.
    *
    * @param component the component to be set with sizes, assumed not to be
    * <code>null</code>
    */
   private void setPreferredSizes(JComponent component)
   {
      if(component instanceof JTextField || component instanceof JComboBox ||
         component instanceof JCheckBox || component instanceof JRadioButton)
      {
         component.setMaximumSize(MAX_SIZE);
         component.setPreferredSize(PREF_SIZE);
         component.setMinimumSize(MIN_SIZE);
      }
      else if(component instanceof JButton)
      {
         component.setMaximumSize(STANDARD_BUTTON_SIZE);
         component.setPreferredSize(STANDARD_BUTTON_SIZE);
         component.setMinimumSize(STANDARD_BUTTON_SIZE);
      }
      else if(!(component instanceof JPanel))
      {
         component.setMaximumSize(MAX_AREA_SIZE);
         component.setPreferredSize(PREF_AREA_SIZE);
         component.setMinimumSize(MIN_AREA_SIZE);
      }
   }

   /**
    * Supports only <code>PSGridBoxLayout</code> with 2 columns and unlimited
    * number of rows. All other layouts are ignored.
    *
    * @param mgr the layout manager, may be <code>null</code>
    *
    */
   public void setLayout(LayoutManager mgr)
   {
      if(mgr instanceof PSGridBoxLayout)
      {
         PSGridBoxLayout layout = (PSGridBoxLayout) mgr;
         if(layout.getRows() == 0 && layout.getColumns() == 2)
            super.setLayout(layout);
      }
   }
   
   /* 
    * @see com.percussion.guitools.IPSPropertyPanel#hideRow(int)
    */
   public void hideRow(int index)
   {
      if (!isValidRowIndex(index))
         throw new IllegalArgumentException("Invalid index");
   
      setRowCompsVisible(index, false);
   }

   /* 
    * @see com.percussion.guitools.IPSPropertyPanel#showRow(int)
    */
   public void showRow(int index)
   {
      if (!isValidRowIndex(index))
         throw new IllegalArgumentException("Invalid index");

      setRowCompsVisible(index, true);   
   }
   
   /* 
    * @see com.percussion.guitools.IPSPropertyPanel#hideRow(java.lang.String)
    */
   public boolean hideRow(String label)
   {
      if (label == null || label.trim().length() == 0)
         throw new IllegalArgumentException("label may not be null or empty");

      boolean didHide = false;
      int index = getMatchingRowCompsByLabel(label);
      if (index != -1)
      {
         setRowCompsVisible(index, false);
         didHide = true;
      }
      
      return didHide;
   }

   /* 
    * @see com.percussion.guitools.IPSPropertyPanel#showRow(java.lang.String)
    */
   public boolean showRow(String label)
   {
      if (label == null || label.trim().length() == 0)
         throw new IllegalArgumentException("label may not be null or empty");

      boolean didShow = false;
      int index = getMatchingRowCompsByLabel(label);
      if (index != -1)
      {
         setRowCompsVisible(index, true);
         didShow = true;
      }
      
      return didShow;   
   }   
   
   /* 
    * @see com.percussion.guitools.IPSPropertyPanel#getRowCount()
    */
   public int getRowCount()
   {
      return m_rowList.size();
   }

   /**
    * Sets label alignment. Defaults to RIGHT_ALIGNMENT
    *
    * @param the alignment LEFT_ALIGNMENT, RIGHT_ALIGNMENT
    * or CENTER_ALIGNMENT
    */
   public void setLabelAlignment(float alignment)
   {
      m_labelAlignment = alignment;
   }
   
   /**
    * Determines if the supplied row index is valid.
    * 
    * @param index The index to test.
    * 
    * @return <code>true</code> if the index is must be less than 
    * <code>m_rowList.size()</code> and greater than or equal to 0.
    */
   private boolean isValidRowIndex(int index)
   {
      return (index >= 0 && index < m_rowList.size());
   }
   
   /**
    * Sets the <code>isVisible()</code> property on the row components at the
    * specified index.
    *   
    * @param index The index of the row to modify, assumed to be a valid
    * row index (see {@link #isValidRowIndex(int)}).
    * @param isVisible <code>true</code> to set the components visible, 
    * <code>false</code> to hide them.
    */
   private void setRowCompsVisible(int index, boolean isVisible)
   {
      List compList = (List)m_rowList.get(index);
      Iterator comps = compList.iterator();
      while (comps.hasNext())
         ((Component)comps.next()).setVisible(isVisible);
   }
   
   /**
    * Gets index of the first row found with a <code>JLabel</code> that has
    * text matching the supplied <code>label</code>.
    *  
    * @param label The text to match, assumed not <code>null</code> or empty.
    * 
    * @return The index of the matching row, or <code>-1</code> if no match
    * is found.
    */
   private int getMatchingRowCompsByLabel(String label)
   {
      int match = -1;
      int onRow = 0;
      
      Iterator rows = m_rowList.iterator();
      while (rows.hasNext())
      {
         List rowComps = (List)rows.next();
         Iterator comps = rowComps.iterator();
         while (comps.hasNext())
         {
            Component comp = (Component)comps.next();
            if (comp instanceof JLabel && 
               ((JLabel)comp).getText().equals(label))
            {
               match = onRow;
            }
         }
         onRow++;
      }
      
      return match;
   }

   /**
    * Gets List of components of the first row found with a <code>JLabel</code> 
    * that has text matching the supplied <code>label</code>.
    *  
    * @param label The text to match, assumed not <code>null</code> or empty.
    * 
    * @return The component list of the matching row, or <code>null</code> 
    * if no match is found.
    */
   public List getMatchingRowByLabel(String label)
   {
      List match = null;
      int onRow = 0;
      boolean found=false;
      Iterator rows = m_rowList.iterator();
      while (rows.hasNext())
      {
         List rowComps = (List)rows.next();
         Iterator comps = rowComps.iterator();
         while (comps.hasNext()&& !found)
         {
            Component comp = (Component)comps.next();
            if (comp instanceof JLabel && 
               ((JLabel)comp).getText().equals(label))
            {
               match = rowComps;
               found = true;
            }
         }
         onRow++;
      }
      return match;
   }
   /**
    * Alignment of the label. Defaults to RIGHT_ALIGNMENT
    */
   private float m_labelAlignment = RIGHT_ALIGNMENT;

   /**
    * The constant to indicate fixed field height.
    */
   public static final int FIXED_FIELD_HEIGHT = 20;

   /**
    * The constant to indicate fixed area height.
    */
   public static final int FIXED_AREA_HEIGHT = 100;

   /**
    * The constant to indicate preferred width of a field or area.
    */
   public static final int PREF_WIDTH = 200;

   /**
    * The constant to indicate minimum width of a field or area.
    */
   public static final int MIN_WIDTH = 40;

   /**
    * The maximum dimension for a control which takes up an area,
    * height and width are the maximum it can have.
    */
   public static final Dimension MAX_AREA_SIZE = new Dimension(
      Integer.MAX_VALUE, Integer.MAX_VALUE);

   /**
    * The preferred dimension for a control which takes up an area,
    * height is fixed.
    */
   public static final Dimension PREF_AREA_SIZE = new Dimension(PREF_WIDTH,
      FIXED_AREA_HEIGHT);

   /**
    * The minimum dimension for a control which takes up an area,
    * height is fixed.
    */
   public static final Dimension MIN_AREA_SIZE = new Dimension(MIN_WIDTH,
      FIXED_AREA_HEIGHT);

   /**
    * The maximum dimension for a field control, height is fixed, width is
    * maximum it can have.
    */
   public static final Dimension MAX_SIZE = new Dimension(Integer.MAX_VALUE,
      FIXED_FIELD_HEIGHT);

   /**
    * The preferred dimension for a control.
    */
   public static final Dimension PREF_SIZE = new Dimension(PREF_WIDTH,
      FIXED_FIELD_HEIGHT);

   /**
    * The minimum dimension for a control.
    */
   public static final Dimension MIN_SIZE = new Dimension(MIN_WIDTH,
      FIXED_FIELD_HEIGHT);

   /**
    * The standard button size for all buttons.
    */
   public static final Dimension STANDARD_BUTTON_SIZE = new Dimension(80, 24);
   
   /**
    * List of components by row, never <code>null</code>, may be empty.  Each 
    * element in the list is a <code>List</code> of <code>Component</code> 
    * objects, added as controls are added to this panel. Each entry is never 
    * <code>null</code>, may be empty.  
    */
   private List m_rowList = new ArrayList();
}
