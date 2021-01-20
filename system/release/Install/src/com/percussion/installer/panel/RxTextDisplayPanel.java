/******************************************************************************
 *
 * [ RxTextDisplayPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;

import com.percussion.installanywhere.RxIAPanel;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;


/**
 * A panel which provides a scrollable, word-wrapped, non-editable text area
 * allowing for subclasses to display various messages.
 */
public abstract class RxTextDisplayPanel extends RxIAPanel
{
   @Override
   public void initialize()
   {
      //must call base class
      super.initialize();
         
      m_textArea = new JTextArea("");
      m_textArea.setEditable(false);
      m_textArea.setLineWrap(true);
      m_textArea.setWrapStyleWord(true);
      m_scrollPane = new JScrollPane(m_textArea);
      
      add(m_scrollPane, BorderLayout.CENTER);
   }
  
   /**
    * Sets the text displayed by this panel.  Resets the caret position to 0 so
    * the beginning of the text will be displayed in the case of long messages
    * which extend beyond the field of view.
    * 
    * @param text the displayable message.
    */
   protected void setText(String text)
   {
      m_textArea.setText(text);
      m_textArea.setCaretPosition(0);
   }
   
   /**  
    * Pane which allows for scrollable text area.  Initialized in
    * {@link #initialize()}, never <code>null</code> after that.
    */
   private JScrollPane m_scrollPane = null;
   
   /**
    * Component which displays the text for this panel.  Initialized in
    * {@link #initialize()}, never <code>null</code> after that.
    */
   private JTextArea m_textArea = null;
}
