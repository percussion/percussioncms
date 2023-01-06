/*[ PSMenuSource.java ]********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.cx;

/**
 * The class that holds selection source for a menu and context selection source 
 * for a context menu. 
 */
public class PSMenuSource
{   

   /**
    * Default constructor for this object that represents no selection.
    */
   public PSMenuSource()
   {
   }
   
   /**
    * Constructs the menu with the supplied selection.
    * 
    * @param selection the default (navigational) selection for the menu, may
    * not be <code>null</code>
    */
   public PSMenuSource(PSSelection selection)
   {
      setSource(selection);
   }
   
   /**
    * Sets the selection object to be used as source for the menu. 
    * 
    * @param selection the default (navigational) selection for the menu, may
    * not be <code>null</code>
    */
   public void setSource(PSSelection selection)
   {
      if(selection == null)
         throw new IllegalArgumentException("selection may not be null.");
         
      m_source = selection;
   }
   
   /**
    * Gets the current selection source.
    * 
    * @return the selection, may be <code>null</code>
    */
   public PSSelection getSource()
   {
      return m_source;   
   }
   
   /**
    * Gets the current context selection source. See {@link 
    * #setContextSource(PSSelection)} for more information.
    * 
    * @return the selection, may be <code>null</code>
    */
   public PSSelection getContextSource()
   {
      return m_contextSrc;   
   }
   
   /**
    * Sets the context selection source of the menu. Generally this should be
    * called whereever the selection changes (either in navigational or main 
    * display panel).
    * 
    * @param contextSource the context selection source, may not be <code>null
    * </code>
    */
   public void setContextSource(PSSelection contextSource)
   {
      if(contextSource == null)
         throw new IllegalArgumentException("contextSource may not be null.");
         
      m_contextSrc = contextSource;
   }
   
   /**
    * The selection for the menu actions for which this object is set as source.
    * In general this is the selection in navigational panel. <code>null</code>
    * until <code>setSource(PSSelection)</code> is called. 
    */
   private PSSelection m_source = null;
   
   /**
    * The selection for the context menu actions for which this object is set as
    * source. In general this is the recent selection of navigational panel or 
    * main display panel selections. <code>null</code> until <code>
    * setContextSource(PSSelection)</code> is called. 
    */
   private PSSelection m_contextSrc = null;
}
