/******************************************************************************
 *
 * [ PSPagingControl.java ]
 * 
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.guitools;

import com.percussion.UTComponents.UTFixedButton;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A control that consists of four buttons, a slider, and a label 
 * showing the current page and page total. The label uses an input
 * field for the current page value so that a user can directly modify 
 * it.
 * 
 * <pre>
 *  The control looks something like the following example:
 * 
 *          Page [1] of 999
 *  [&lt;&lt;][&lt;] &lt;======V======&gt; [&gt;][&gt;&gt;]
 * </pre>
 * 
 * @author erikserating
 *
 */
public class PSPagingControl extends JPanel
{
      
    
   public PSPagingControl(int pageCount, int currentPage)
   {
      if(pageCount < 1)
         throw new IllegalArgumentException("count must be greater than 0.");
      if(currentPage > pageCount)
         throw new IllegalArgumentException(
            "Current page cannot be greater than the total page count.");
      m_pageCount = pageCount;
      m_currentPage = Math.max(1, currentPage);
      init();
   }
   
   /**
    * Initialize all the panel controls and lay them out and add any 
    * required listeners.
    */
   private void init()
   {
      setLayout(new GridBagLayout());
      setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
      
      Box topBox = new Box(BoxLayout.X_AXIS);
      Box bottomBox = new Box(BoxLayout.X_AXIS);
      
      JLabel pageLabel = new JLabel(ms_res.getString("PSPagingControl.page.label"));
      JLabel ofLabel = new JLabel(ms_res.getString("PSPagingControl.of.label"));
      m_currentPageField = createCurrentPageField();
      m_totalPagesLabel = new JLabel(String.valueOf(m_pageCount));
      
      topBox.add(Box.createHorizontalGlue());
      topBox.add(Box.createHorizontalStrut(5));
      topBox.add(pageLabel);
      topBox.add(Box.createHorizontalStrut(5));
      topBox.add(m_currentPageField);
      topBox.add(Box.createHorizontalStrut(5));
      topBox.add(ofLabel);
      topBox.add(Box.createHorizontalStrut(5));
      topBox.add(m_totalPagesLabel);
      topBox.add(Box.createHorizontalStrut(5));
      topBox.add(Box.createHorizontalGlue());
      
      GridBagConstraints c = new GridBagConstraints();
      c.gridx = 0;
      c.gridy = 0;
      
      add(topBox, c);      
      
      m_firstButton = createButton(BUTTON_TYPE_FIRST);
      m_lastButton = createButton(BUTTON_TYPE_LAST);
      m_nextButton = createButton(BUTTON_TYPE_NEXT);
      m_previousButton = createButton(BUTTON_TYPE_PREV);
      m_slider = createSlider();
      
      bottomBox.add(Box.createHorizontalStrut(5));
      bottomBox.add(m_firstButton);
      bottomBox.add(Box.createHorizontalStrut(5));
      bottomBox.add(m_previousButton);
      bottomBox.add(Box.createHorizontalStrut(5));
      bottomBox.add(m_slider);
      bottomBox.add(Box.createHorizontalStrut(5));
      bottomBox.add(m_nextButton);
      bottomBox.add(Box.createHorizontalStrut(5));
      bottomBox.add(m_lastButton);
      bottomBox.add(Box.createHorizontalStrut(5));
      
      c.gridy = 1;
      add(bottomBox, c);
   }
   
  
   
   /**
    * Set the current page index.
    * @param pageIndex the current page index to be set or
    * -1 to "unset".
    */
   public void setCurrentPage(int pageIndex)
   {
      if(pageIndex > m_pageCount)
         throw new IllegalArgumentException(
            "Current page cannot be greater than the total page count.");
      if(m_currentPage == pageIndex)
         return;
      m_currentPage = Math.max(1, pageIndex);
      
      m_slider.setValue(m_currentPage);
      m_currentPageField.setText(String.valueOf(m_currentPage));
      
   }
   
   /**
    * Sets the total page count value. Changing this value may result
    * in setting the current page value to 1 or 0 if the current page value exceeds
    * the page count after it is set.
    * @param count a non negative integer greater than zero indicating the total number of pages
    * for this control. 
    */
   public void setPageCount(int count)
   {
      if(count < 1)
         throw new IllegalArgumentException("count must be greater than 0.");
      if(m_pageCount == count)
         return;
      m_pageCount = count;
      
      if(m_currentPage > count)
         setCurrentPage(1);
   }
   
   /**
    * Add a paging control listener to listen for page change events.
    * @param listener cannot be <code>null</code>.
    */
   public void addPagingControlListener(IPSPagingControlListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null.");
      if(!m_listeners.contains(listener))
      {
         m_listeners.add(listener);
      }
   }
   
   /**
    * Remove a paging control listener.
    * @param listener cannot be <code>null</code>.
    */
   public void removePagingControlListener(IPSPagingControlListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null.");
      if(m_listeners.contains(listener))
      {
         m_listeners.remove(listener);
      }
   }
     
   /**
    * Fires a page change event, notifying all listeners. 
    * @param newPageIndex the current page index that results from the
    * action that fired the event. Set to -1 if no page indicated.
    */
   private void firePageChangeEvent(int newPageIndex)
   {
      PSPagingControlEvent event = new PSPagingControlEvent(this, newPageIndex);
      for(IPSPagingControlListener listener : m_listeners)
      {
         listener.onPageChange(event);       
      }
   }
   
   /**
    * Helper method to create the current page field and attach listeners.
    * @return the text field. Never <code>null</code>.
    */
   private FixedWholeNumberField createCurrentPageField()
   {
      FixedWholeNumberField field = new FixedWholeNumberField(String.valueOf(m_currentPage));
      field.addFocusListener(m_multiListener);
      field.addKeyListener(m_multiListener);
      return field;
   }
   
   /**
    * Helper method to create the various buttons needed for this control and
    * add listeners.
    * @param type one of the BUTTON_TYPE_XXX constants.
    * @return the button, never <code>null</code>.
    */
   private UTFixedButton createButton(short type)
   {
      ImageIcon icon = null;
      String tooltip = "";
      
      switch(type)
      {
         case BUTTON_TYPE_FIRST:
            icon = getIcon("doubleTriangleLeft16.gif");
            tooltip = ms_res.getString("PSPagingControl.firstButton.tooltip");
            break;
         case BUTTON_TYPE_LAST:
            icon = getIcon("doubleTriangleRight16.gif");
            tooltip = ms_res.getString("PSPagingControl.lastButton.tooltip");
            break;
         case BUTTON_TYPE_NEXT:
            icon = getIcon("rightTriangle16.gif");
            tooltip = ms_res.getString("PSPagingControl.nextButton.tooltip");
            break;            
         case BUTTON_TYPE_PREV:
            icon = getIcon("leftTriangle16.gif");
            tooltip = ms_res.getString("PSPagingControl.prevButton.tooltip");
            break;
         default:            
            
      }
      UTFixedButton button = new UTFixedButton(icon, 20, 20);
      if(tooltip != null && tooltip.length() > 0)
         button.setToolTipText(tooltip);
      button.addActionListener(m_multiListener);
      return button;      
      
   }
   
   /**
    * Helper method to create the slider control and add listeners.
    * @return the slider, never <code>null</code>.
    */
   private JSlider createSlider()
   {
      JSlider slider = new JSlider(1, Math.max(1, m_pageCount));
      slider.setValue(m_currentPage);
      slider.addChangeListener(m_multiListener);
      return slider;
   }
   
   /**
    * Convienience method to retrieve icon.
    * @param file the image file, assumed not <code>null</code> or
    * empty.
    * @return the image if it exists.
    */
   private ImageIcon getIcon(String file)
   {
      ImageIcon image =
         new ImageIcon(PSPagingControl.class.getResource(
            "images/" + file));
      return image;
   }
   
   /**
    * Helper method to set current page from text field, resetting
    * the text field to the current page value if it is not in a
    * valid range.
    *
    */
   private void setCurrentPageFromTextField()
   {
      String rawValue = m_currentPageField.getText();
      if(rawValue.length() > 0)
      {
         int value = Integer.parseInt(rawValue);
         if(value < m_pageCount && value >= 1)
         {
            setCurrentPage(value);
            return;
         }
      }
      m_currentPageField.setText(String.valueOf(m_currentPage));
   }
   
   // Mian method used for testing
   public static void main(String[] args) throws Exception
   {
      TestFrame frame = new TestFrame();
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setVisible(true);
   }
   
   /**
    * A text field that only accepts whole number characters and
    * is fixed in width and height.
    * @author erikserating
    *
    */
   public class FixedWholeNumberField extends JTextField
   {
      public FixedWholeNumberField(String name)
      {
         super(name);
         setPreferredSize(new Dimension(40, 20));
      }

      /* (non-Javadoc)
       * @see javax.swing.JComponent#getMaximumSize()
       */
      @Override
      public Dimension getMaximumSize()
      {
         return getPreferredSize();
      }

      /* (non-Javadoc)
       * @see javax.swing.JComponent#getMinimumSize()
       */
      @Override
      public Dimension getMinimumSize()
      {
         return getPreferredSize();
      }

      /* (non-Javadoc)
       * @see javax.swing.JTextField#createDefaultModel()
       */
      @Override
      protected Document createDefaultModel()
      {
         return new WholeNumberDocument();
      }      
      
   }
   
   /**
    * Document class that only allows whole numbers
    */
   private class WholeNumberDocument extends PlainDocument
   {

      @Override
      public void insertString(int offs, String str, AttributeSet a)
               throws BadLocationException
      {

         char[] source = str.toCharArray();
         char[] result = new char[source.length];
         int j = 0;

         for (int i = 0; i < result.length; i++)
         {
            if (Character.isDigit(source[i]))
               result[j++] = source[i];
            else
            {
               Toolkit.getDefaultToolkit().beep();
            }
         }
         super.insertString(offs, new String(result, 0, j), a);
      }
   }
   
   /**
    * This inner class implements the various listeners needed by
    * the controls that make up the paging control.
    * @author erikserating
    *
    */
   private class MultiListener implements ActionListener, ChangeListener,
      FocusListener, KeyListener
   {

      /* (non-Javadoc)
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(ActionEvent e)
      {
         Object source = e.getSource();
         if(source == m_firstButton)
         {
            if(m_currentPage > 1)
            {
               setCurrentPage(1);
            }
         }
         else if(source == m_lastButton)
         {
            if(m_currentPage < m_pageCount)
            {
               setCurrentPage(m_pageCount);               
            }
         }
         else if(source == m_previousButton)
         {
            if(m_currentPage > 1)
            {
               setCurrentPage(m_currentPage - 1);
            }
         }
         else if(source == m_nextButton)
         {
            if(m_currentPage < m_pageCount)
            {
               setCurrentPage(m_currentPage + 1);               
            }
         }
      }

      /* (non-Javadoc)
       * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
       */
      public void stateChanged(ChangeEvent e)
      {
         JSlider slider = (JSlider)e.getSource();
         if(slider.getValueIsAdjusting())
            return;
         m_currentPage = slider.getValue();
         m_currentPageField.setText(String.valueOf(m_currentPage));
         firePageChangeEvent(m_currentPage);         
      }     

      /* (non-Javadoc)
       * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
       */
      public void focusGained(@SuppressWarnings("unused") FocusEvent e)
      {
         // no op
         
      }

      /* (non-Javadoc)
       * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
       */
      public void focusLost(FocusEvent e)
      {
         if(e.getSource() == m_currentPageField)
         {
            setCurrentPageFromTextField();
         }
      }

      /* (non-Javadoc)
       * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
       */
      public void keyTyped(@SuppressWarnings("unused") KeyEvent e)
      {
         // no op
         
      }

      /* (non-Javadoc)
       * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
       */
      public void keyPressed(@SuppressWarnings("unused") KeyEvent e)
      {
         // no op
         
      }

      /* (non-Javadoc)
       * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
       */
      public void keyReleased(KeyEvent e)
      {
         if(e.getSource() == m_currentPageField)
         {
            if(e.getKeyCode() == 10)
            {
               setCurrentPageFromTextField();
            }
            else if(e.getKeyCode() == 27)
            {
               m_currentPageField.setText(String.valueOf(m_currentPage));
            }
         }
         
      }
      
   }
      
   /**
    * Multi listener instance for the class. Never <code>null</code>.
    */
   private MultiListener m_multiListener = new MultiListener();
   
   /**
    * Current page index value. Will be 0 if no page is indicated.
    */
   private int m_currentPage = 0;
   
   /**
    * Total page count value.
    */
   private int m_pageCount;
   
   /**
    * List of all paging control listeners. Never <code>null</code>
    * but may be empty.
    */
   private List<IPSPagingControlListener> m_listeners = 
      new ArrayList<IPSPagingControlListener>();
   
   /**
    * The textfield to modify and display the current page value.
    * Initialized in {@link #init()}, never <code>null</code> after
    * that.
    */
   private FixedWholeNumberField m_currentPageField;
   
   /**
    * Initialized in {@link #init()}, never <code>null</code> after
    * that.
    */
   private UTFixedButton m_firstButton;
   
   /**
    * Initialized in {@link #init()}, never <code>null</code> after
    * that.
    */
   private UTFixedButton m_lastButton;
   
   /**
    * Initialized in {@link #init()}, never <code>null</code> after
    * that.
    */
   private UTFixedButton m_nextButton;
   
   /**
    * Initialized in {@link #init()}, never <code>null</code> after
    * that.
    */
   private UTFixedButton m_previousButton;
   
   /**
    * Initialized in {@link #init()}, never <code>null</code> after
    * that.
    */
   private JSlider m_slider;
   
   /**
    * Initialized in {@link #init()}, never <code>null</code> after
    * that.
    */
   private JLabel m_totalPagesLabel;
   
   // Button types
   private static final short BUTTON_TYPE_FIRST = 0;
   private static final short BUTTON_TYPE_LAST = 1;
   private static final short BUTTON_TYPE_NEXT = 2;
   private static final short BUTTON_TYPE_PREV = 3;
   
   /**
    * Resource bundle reference. Never <code>null</code> after that.
    */
   private static ResourceBundle ms_res;

   static
   {
      try
      {
         ms_res = ResourceHelper.getResources();
      }
      catch(MissingResourceException ex)
      {
         System.out.println(ex);
         throw ex;
      }
   }   

}

class TestFrame extends JFrame
{
   public TestFrame()
   {
      super("Test PSPagingControl");
      setSize(400, 400);
      getContentPane().add(new PSPagingControl(199,1));
   }
   
}

