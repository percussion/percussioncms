/******************************************************************************
*
* [ PSStackedPropertyPanel.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
/**
 * 
 */
package com.percussion.guitools;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The utility class to create a panel with controls to set properties.
 * This panel will layout the each row with horizontally withe the label
 * stacked above its corresponding control.
 */
public class PSStackedPropertyPanel extends JPanel implements IPSPropertyPanel
{

   /**
    * 
    */
   public PSStackedPropertyPanel()
   {
      super();
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      
   }

   /* 
    * @see com.percussion.guitools.IPSPropertyPanel#addPropertyRow(
    * java.lang.String, javax.swing.JComponent)
    */
   public void addPropertyRow(String name, JComponent control)
   {
      addPropertyRow(name, new JComponent[] {control} );
   }

   /* 
    * @see com.percussion.guitools.IPSPropertyPanel#addPropertyRow(
    * java.lang.String, javax.swing.JComponent, char)
    */
   public void addPropertyRow(String name, JComponent control, char mnemonic)
   {
      addPropertyRow(name, new JComponent[] {control}, control, mnemonic, null);
   }

   /* 
    * @see com.percussion.guitools.IPSPropertyPanel#addPropertyRow(
    * java.lang.String, javax.swing.JComponent, javax.swing.JComponent, char,
    *  java.lang.String)
    */
   public void addPropertyRow(String name, JComponent control,
      JComponent mnControl, char mnemonic, String tooltip)
   {
      addPropertyRow(name, new JComponent[]{control},
         mnControl, mnemonic, tooltip);

   }

   /* 
    * @see com.percussion.guitools.IPSPropertyPanel#addPropertyRow(
    * java.lang.String, javax.swing.JComponent[])
    */
   public void addPropertyRow(String name, JComponent[] controls)
   {
      addPropertyRow(name, controls, null, (char) 0, null);
   }

   /* 
    * @see com.percussion.guitools.IPSPropertyPanel#addPropertyRow(
    * java.lang.String, javax.swing.JComponent[], javax.swing.JComponent,
    *  char, java.lang.String)
    */
   public void addPropertyRow(String name, JComponent[] controls,
      JComponent mnemonicControl, char mnemonic, String tooltip)
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("Name cannot be null or empty.");
      final Box box = Box.createVerticalBox();
      List controlList = new ArrayList();
      if(m_controls.size() > 0)
         add(Box.createVerticalStrut(5));
      m_controls.add(box);
      m_controlLabels.add(name);
      final JLabel label = new JLabel(name);
      label.setAlignmentX(JLabel.LEFT_ALIGNMENT);
      
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
      
      JPanel labelPanel = new JPanel();
      labelPanel.setLayout(new GridLayout(1, 1));
      labelPanel.add(label);
      
      labelPanel.setMaximumSize(MAX_SIZE);
      labelPanel.setPreferredSize(PREF_SIZE);
      labelPanel.setMinimumSize(MIN_SIZE); 
      
      controlList.add(label);
      box.add(labelPanel);
      
      JPanel controlPanel = new JPanel();
      controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
      for(int i=0; i<controls.length; )
      {
         JComponent component = controls[i];
         setPreferredSizes(component);

         controlPanel.add(component);
         controlList.add(component);

         if(++i < controls.length)
         {
            Component comp = Box.createVerticalStrut(5);
            controlPanel.add(comp);
         }
            
      }
      box.add(controlPanel);
            
      box.setAlignmentX(LEFT_ALIGNMENT);
      box.setAlignmentY(CENTER_ALIGNMENT);
      
      m_controlLists.add(controlList);
      
      add(box);

   }

   /* 
    * @see com.percussion.guitools.IPSPropertyPanel#hideRow(int)
    */
   public void hideRow(int index)
   {
      Box box = (Box)m_controls.get(index);
      box.setVisible(false);
   }

   /* 
    * @see com.percussion.guitools.IPSPropertyPanel#showRow(int)
    */
   public void showRow(int index)
   {
      Box box = (Box)m_controls.get(index);
      box.setVisible(true);
   }

   /* 
    * @see com.percussion.guitools.IPSPropertyPanel#hideRow(java.lang.String)
    */
   public boolean hideRow(String label)
   {
      int idx = m_controlLabels.indexOf(label);
      if(idx == -1)
         return false;
      hideRow(idx);
      return true;
   }

   /* 
    * @see com.percussion.guitools.IPSPropertyPanel#showRow(java.lang.String)
    */
   public boolean showRow(String label)
   {
      int idx = m_controlLabels.indexOf(label);
      if(idx == -1)
         return false;
      showRow(idx);
      return true;
   }

   /* 
    * @see com.percussion.guitools.IPSPropertyPanel#getRowCount()
    */
   public int getRowCount()
   {
      return m_controls.size();
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
      int idx = m_controlLabels.indexOf(label);
      if(idx == -1)
         return null;
      
      return (List)m_controlLists.get(idx);
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
         component instanceof JCheckBox || component instanceof JRadioButton ||
         component instanceof JLabel)
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
   
   public static void main(String[] args)
   {
      JFrame frame = new JFrame("Test Panel");
      PSStackedPropertyPanel panel = new PSStackedPropertyPanel();
      
      JComponent comp1 = new JTextField("Test me");
      JComponent comp2 = new JTextField("Test me 2");
      panel.addPropertyRow("Row 1", new JComponent[]{comp1}, null, (char)-1, "");
      panel.addPropertyRow("Row 2", new JComponent[]{comp2}, null, (char)-1, "");
      
      frame.getContentPane().add(panel);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setSize(400, 500);
      frame.setVisible(true);
      
   }
   
   private List m_controls = new ArrayList();
   private List m_controlLabels = new ArrayList();
   private List m_controlLists = new ArrayList();
   
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

}
