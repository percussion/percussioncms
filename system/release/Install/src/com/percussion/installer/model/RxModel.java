/******************************************************************************
 *
 * [ RxIAModel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.installer.model;

import com.percussion.installanywhere.RxIAPanel;
import com.percussion.installanywhere.RxIAUtils;
import com.percussion.installer.action.RxLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for all Rx data model classes. 
 */
public abstract class RxModel
{
   /**
    * This method is called before the corresponding panel or console is
    * displayed.
    * 
    * @return <code>true</code> if the panel or console should be displayed,
    * <code>false</code> otherwise.  Default return value is <code>true</code>.
    */
   public boolean queryEnter()
   {
      return true;
   }
   
   /**
    * This method is called when the user attempts to go to the next panel or
    * console.
    * 
    * @return <code>true</code> if the panel or console should be exited,
    * <code>false</code> otherwise.  Default return value is <code>true</code>.
    */
   public boolean queryExit()
   {
      return true;
   }
   
   /**
    * This method is called when the corresponding panel or console is
    * displayed and is visible.
    * 
    * @return <code>true</code> if the panel or console should remain visible
    * until the user navigates to another panel/console, <code>false</code> to
    * skip the panel/console.  Default return value is <code>true</code>.
    */
   public boolean entered()
   {
      return true;
   }
   
   /**
    * This method is called when the corresponding panel or console is exited
    * but still visible.
    */
   public void exited()
   {
   }
   
   /**
    * Stores/updates the persistent property/value pair.
    * 
    * @param property the property to be persisted.
    * @param value the value to associate with the persisted property.
    */
   public void setValue(String property, Object value)
   {
      m_properties.put(property, value);
   }
   
   /**
    * Retrieves the specified property's value.
    * 
    * @param property the persistent property.
    * 
    * @return the value associated with the persistent property, may be
    * <code>null</code>.  Must be cast accordingly.
    */
   public Object getValue(String property)
   {
      return m_properties.get(property);
   }
     
   /**
    * Validates the model.
    *
    * @return <CODE>true</CODE> if the brand code is valid
    */
   protected boolean validateModel()
   {
      logEvent(RxIAUtils.WARNING, "validateModel must be overridden");
      return false;
   } 
   
   /**
    * Logs event to log file.
    * @param type assumed INFO if unrecognized.
    * @param msg never <code>null</code>. 
    */
   public void logEvent(int type, Object msg)
   {  
      if (msg == null)
         throw new IllegalArgumentException("msg may not be null");
      
      this.logEvent(type, msg, null);
   }
   
   /**
    * Logs event to log file.
    * @param type assumes INFO if unrecognized.
    * @param msg never <code>null</code>.
    * @param th may be <code>null</code>.
    */
   protected void logEvent(int type, Object msg, Throwable th)
   {
      if (msg == null)
         throw new IllegalArgumentException("msg may not be null");
      
      if (type == RxIAUtils.ERROR)
         RxLogger.logError(msg);
      else if (type == RxIAUtils.WARNING)
         RxLogger.logWarn(msg);
      else
      {
         // just log the message
         RxLogger.logInfo(msg);
      }         
      
      if (th!=null)
         RxLogger.logError(th);
   }
   
   /**
    * Any errors encountered by this model can be retrieved using this method.
    * 
    * @return a message containing the error(s) which occurred during the life
    * of this model.
    */
   public String getError()
   {
      return m_strError;
   }
      
   /**
    * Each model will have a corresponding panel or console depending on the
    * mode of installation.  This method is used by each panel/console to 
    * retrieve the appropriate title to display.
    * 
    * @return the title to be displayed by the corresponding panel/console.
    */
   public abstract String getTitle();
   
   /**
    * Each model is associated with a panel.
    * 
    * @return this model's associated panel, may be <code>null</code>.
    */
   protected RxIAPanel getPanel()
   {
      return m_panel;
   }
   
   /**
    * Calls <code>propertyChanged</code> on this model's associated panel.
    * 
    * @param propName the name of the property whose value has changed.
    */
   protected void propertyChanged(String propName)
   {
      if (m_panel != null)
         m_panel.propertyChanged(propName);
   }
   
   /**
    * Sets the associated panel for this model.
    * 
    * @param panel the panel associated with this model.
    */
   public void setPanel(RxIAPanel panel)
   {
      m_panel = panel; 
   }
   
   /**
    * The error for this panel.
    */
   protected String m_strError = "";
   
   /**
    * Map containing persistent properties and their values.  Never
    * <code>null</code>, may be empty.
    */
   private Map<String, Object> m_properties = new HashMap<>();
   
   /**
    * See {@link #getPanel()}, {@link #setPanel(RxIAPanel)}.
    */
   private RxIAPanel m_panel = null;
}
