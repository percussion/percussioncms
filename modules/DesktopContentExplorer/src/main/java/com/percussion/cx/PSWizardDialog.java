/* *****************************************************************************
 *
 * [ PSWizardDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/
package com.percussion.cx;

import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSDialog;
import com.percussion.wizard.IPSWizardDialog;
import com.percussion.wizard.IPSWizardPanel;
import com.percussion.wizard.PSWizardCommandPanel;
import com.percussion.wizard.PSWizardPanel;
import com.percussion.wizard.PSWizardValidationError;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * A standard wizard dialog using the card layout for easy 'next' and 'back'
 * wizard functonality.
 */
public class PSWizardDialog extends PSDialog implements IPSWizardDialog
{
   /**
    * Construct a new wizard dialog for the supplied pages.
    * 
    * @param parent the parent for this dialog, may be <code>null</code>.
    * @param pages and array of page arrays. For each page the user must supply
    *    an array of page data, where the first element is the name of the
    *    page panel as <code>String</code>, the second element is the page 
    *    panel input data as <code>Object</code> and the third element is the
    *    page instruction as <code>String</code>.
    * @param title the dialog title, may be <code>null</code> or empty.
    */
   public PSWizardDialog(Frame parent, Object[][] pages, String title, PSContentExplorerApplet applet)
   {
      super(parent, "");
      
      if (title == null)
         throw new IllegalArgumentException("title cannot be null");
      
      title = title.trim();
      if (title.length() == 0)
         throw new IllegalArgumentException("title cannot be empty");
      setTitle(title);
      
      if (pages == null || pages.length == 0)
         throw new IllegalArgumentException("pages cannot be null or empty");
      
      if (applet == null)
         throw new IllegalArgumentException("applet must not be null");
      
      m_applet = applet;
      
      initDialog(pages);
   }

   /* (non-Javadoc)
    * @see IPSWizardDialog#onNext()
    */
   public void onNext()
   {
      try
      {
         Integer key = new Integer(m_pageIndex);
         IPSWizardPanel panel = (IPSWizardPanel) m_pages.get(key);
         panel.validatePanel();
         
         while (panel.skipNext())
         {
            m_cards.next(m_mainPanel);
            ++m_pageIndex;
            
            key = new Integer(m_pageIndex);
            m_skippedPages.put(key, m_pages.get(key));
            panel = (IPSWizardPanel) m_pages.get(key);
         }
         
         m_cards.next(m_mainPanel);
         ++m_pageIndex;
         
         updateControls();
      }
      catch (PSWizardValidationError e)
      {
         ErrorDialogs.showErrorMessage(this, e.getMessage(),
               m_applet.getResourceString(getClass(), "Error"));
      }
   }

   /* (non-Javadoc)
    * @see IPSWizardDialog#onBack()
    */
   public void onBack()
   {
      m_cards.previous(m_mainPanel);
      --m_pageIndex;
      
      Integer key = new Integer(m_pageIndex);
      IPSWizardPanel panel = (IPSWizardPanel) m_skippedPages.get(key);
      while (panel != null)
      {
         m_skippedPages.remove(key);

         m_cards.previous(m_mainPanel);
         --m_pageIndex;

         key = new Integer(m_pageIndex);
         panel = (IPSWizardPanel) m_skippedPages.get(key);
      }
      
      updateControls();
   }

   /* (non-Javadoc)
    * @see IPSWizardDialog#onCancel()
    */
   public void onCancel()
   {
      super.onCancel();
   }

   /* (non-Javadoc)
    * @see IPSWizardDialog#onFinish()
    */
   public void onFinish()
   {
      super.onOk();
   }

   /* (non-Javadoc)
    * @see Object[] IPSWizardDialog#getData()
    */
   public Object[] getData()
   {
      Object[] data = new Object[m_pages.size()];
      for (int i=0; i<m_pages.size(); i++)
         data[i] = ((IPSWizardPanel) m_pages.get(new Integer(i))).getData();
      
      return data;
   }
   
   /**
    * Initialize the dialog for the supplied pages and page instructions.
    * 
    * @param pages and array of page arrays. For each page the user must supply
    *    an array of page data, where the first element is the name of the
    *    page panel as <code>String</code>, the second element is the page 
    *    panel input data as <code>Object</code> and the third element is the
    *    page instruction as <code>String</code>.
    */
   private void initDialog(Object[][] pages)
   {
      Container cp = getContentPane();
      
      cp.setLayout(new BorderLayout());
      m_mainPanel = createMainPanel(pages);
      cp.add(m_mainPanel, BorderLayout.CENTER);
      m_wizardCommands = new PSWizardCommandPanel(this);
      cp.add(m_wizardCommands, BorderLayout.SOUTH);
      
      pack();
      center();
      setResizable(true);
      setVisible(true);
   }
   
   /**
    * Creates tha main dialog panel.
    * 
    * @param pages and array of page arrays. For each page the user must supply
    *    an array of page data, where the first element is the name of the
    *    page panel as <code>String</code>, the second element is the page 
    *    panel input data as <code>Object</code> and the third element is the
    *    page instruction as <code>String</code>.
    * @return the new panel, never <code>null</code>.
    */
   private JPanel createMainPanel(Object[][] pages)
   {
      JPanel panel = new JPanel();
      panel.setLayout(m_cards);
      panel.setPreferredSize(new Dimension(600, 300));
      System.out.println("Number of pages ="+pages.length);
      for (int i=0; i<pages.length; i++)
      {
         Object[] page = (Object[]) pages[i];
         System.out.println("create page "+(String) page[PAGE_PANEL]);
         
         
         Integer key = new Integer(i);
         PSWizardPanel pagePanel = instantiate((String) page[PAGE_PANEL]);
         if (pagePanel == null)
            throw new IllegalArgumentException(
               "a supplied wizard page does not conform to the described " +
               "interface");
         
         pagePanel.setData(page[PAGE_DATA]);
         pagePanel.setInstruction((String) page[PAGE_INSTRUCTION]);
         
         m_pages.put(key, pagePanel);
         panel.add(key.toString(), pagePanel);
      }
      
      return panel;
   }
   
   /**
    * Are we on the first wizard page?
    * 
    * @return <code>true</code> if we are, <code>false</code> otherwise.
    */
   private boolean isFirst()
   {
      return m_pageIndex == 0;
   }
   
   /**
    * Are we on the last wizard page?
    * 
    * @return <code>true</code> if we are, <code>false</code> otherwise.
    */
   private boolean isLast()
   {
      return m_pageIndex >= m_pages.size()-1;
   }
   
   /**
    * Get the wizard page type.
    * 
    * @return the type of the currently active page, one of the 
    *    <code>TYPE_XXX</code> values.
    */
   private int getPageType()
   {
      if (isFirst())
         return TYPE_FIRST;
      else if (isLast())
         return TYPE_LAST;

      return TYPE_MID;
   }
   
   /**
    * Updates the wizard controls based on the current page type. The command
    * panel is always updated. If we are on the last wizard page, all pages
    * summary informations are collected and set as the page instruction for 
    * the last page.
    */
   private void updateControls()
   {
      m_wizardCommands.updateControls(getPageType());
      
      if (isLast())
      {
         /*
          * Collect the summaries from all pages to build the instruction
          * set on the last wizard page.
          */
         StringBuffer summary = new StringBuffer();
         
         IPSWizardPanel page = null;
         Iterator keys = m_pages.keySet().iterator();
         while (keys.hasNext())
         {
            Integer key = (Integer) keys.next();
            page = (IPSWizardPanel) m_pages.get(key);
            if (m_skippedPages.get(key) != null)
               continue;
            
            String sum = page.getSummary();
            if (sum.trim().length() == 0)
               continue;
            
            if (keys.hasNext())
               summary.append(sum + "\n");
         }
         
         /*
          * Pre-pend the last page instruction to the summaries collected
          * from all pages and set the new instruction for the last wizard
          * page.
          */
         if (m_lastPageInstruction == null)
            m_lastPageInstruction = page.getInstruction();
         summary.insert(0, m_lastPageInstruction + "\n\n");
         page.setInstruction(summary.toString());
      }
   }
   
   /**
    * Instantiate the wizard panel for the supplied class name.
    * 
    * @param className the name of the class to be instantiated, it is 
    *    assumed that the class exists and has a default constructor.
    * @return the new panel, may be <code>null</code> if the instantiation
    *    failed.
    */
   private PSWizardPanel instantiate(String className)
   {
      System.out.println("Instantiate PSWizardPanel");
      PSWizardPanel page = null;
      try
      {
         Class c = Class.forName(className);
         Constructor ctor = c.getConstructor(new Class[] {PSContentExplorerApplet.class});

         page = (PSWizardPanel) ctor.newInstance(new Object[] {m_applet});
      }
      catch (Exception e)
      {
        System.out.println("Error finding class "+className);
        e.printStackTrace();
      }
      
      return page;
   }
   
   /**
    * The original last page instruction, saved in the first call to 
    * {@link updateControls()} for the last page, never <code>null</code> or 
    * changed after that.
    */
   private String m_lastPageInstruction = null;
   
   /**
    * This keeps track of the ccurrent wizard page.
    */
   private int m_pageIndex = 0;
   
   /**
    * A map of wizard pages. The map key is an <code>Integer</code> with the
    * page index (starting at 0) while the map value is the page as 
    * <code>IPSWizardPanel</code>. Initialized in 
    * {@link #createMainPanel(String[], String[])}, never changed after that.
    */
   private Map m_pages = new TreeMap();
   
   /**
    * The map used to keep track of skipped wizard pages. The map key is an 
    * <code>Integer</code> with the page index (starting at 0) while the map 
    * value is the page as <code>IPSWizardPanel</code>. Initialized to an 
    * empty map, updated on calls to {@link #onNext()} or {@link #onBack()}.
    */
   private Map m_skippedPages = new TreeMap();
   
   /**
    * The card layout used for the wizards main panel.
    */
   private CardLayout m_cards = new CardLayout();
   
   /**
    * The main wizard panel, initialized during construction, never
    * <code>null</code> or changed after that.
    */
   private JPanel m_mainPanel = null;
   
   /**
    * The wizards command panel, initialized during construction, never
    * <code>null</code> or changed after that.
    */
   private PSWizardCommandPanel m_wizardCommands = null;
   
   /**
    * The index of the page panel for the page array as supplied in constructor.
    */
   private static final int PAGE_PANEL = 0;
   
   /**
    * The index of the page data for the page array as supplied in constructor.
    */
   private static final int PAGE_DATA = 1;
   
   /**
    * The index of the page instruction for the page array as supplied in 
    * constructor.
    */
   private static final int PAGE_INSTRUCTION = 2;
   

   /**
    * A reference back to the applet that initiated this action manager.
    */
   private PSContentExplorerApplet m_applet;
}
