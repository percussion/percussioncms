/*[ PSMenuManager.java ]********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.cx;


/**
 * The manager that listens to selection changes in the applet view panel and
 * updates the menu source that need to be informed of selection changes. In 
 * general the menu source of global menu bar need to be informed of selection 
 * changes in applet.
 */
public class PSMenuManager implements IPSSelectionListener
{
   /**
    * Constructs menu manager with supplied menu source.
    * 
    * @param menuSource the menu source, may not be <code>null</code>
    */
   public PSMenuManager(PSMenuSource menuSource)
   {
      if(menuSource == null)
         throw new IllegalArgumentException("menuSource may not be null.");
         
      m_menuSource = menuSource;
   }
   
   /**
    * Notification event that the selection has changed in main view of applet.
    * Updates the default selection and context selection of the menu source 
    * if the supplied selection is an instance of {@link 
    * #PSNavigationalSelection}, otherwise only context selection.
    * 
    * @param selection the selection object that represents the current 
    * selection in the main view.
    */
   public void selectionChanged(PSSelection selection)
   {
      if(selection == null)
         throw new IllegalArgumentException("selection may not be null.");
         
      if(selection instanceof PSNavigationalSelection)
         m_menuSource.setSource(selection);

      m_menuSource.setContextSource(selection);
   }
   
   /**
    * The menu source object that needs to be informed of selection changes in
    * applet view panel, initialized in the constructor and never <code>null
    * </code> after that.
    */
   private PSMenuSource m_menuSource;
   
}
