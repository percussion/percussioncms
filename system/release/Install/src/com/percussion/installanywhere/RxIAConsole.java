/**[ RxIAConsole.java ]******************************************
 *
 * COPYRIGHT (c) 2004 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ****************************************************************************/
package com.percussion.installanywhere;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installer.action.RxLogger;
import com.percussion.installer.model.RxModel;
import com.zerog.ia.api.pub.CustomCodeConsoleAction;
import com.zerog.ia.api.pub.CustomCodeConsoleProxy;
import com.zerog.ia.api.pub.PreviousRequestException;
import com.zerog.ia.api.pub.VariableAccess;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all console implementations which links InstallAnywhere's
 * {@link CustomCodeConsoleAction} with Rx custom consoles.
 */
public abstract class RxIAConsole extends CustomCodeConsoleAction implements
IPSProxyLocator
{
   /**
    * This method is called before the console is displayed in
    * {@link #setup()}.
    */
   protected void entering()
   {
   }
   
   /**
    * Logs event to log file.
    * @param type must be one of {@link RxIAUtils#ERROR}, 
    * {@link RxIAUtils#WARNING}.
    * @param msg to log, never <code>null</code>. 
    */
   public void logEvent(int type, Object msg)
   {  
      if (msg == null)
         throw new IllegalArgumentException("msg may not be null");
      
      this.logEvent(type, msg, null);
   }
   
   /**
    * Logs event to log file.
    * @param type must be one of {@link RxIAUtils#ERROR}, 
    * {@link RxIAUtils#WARNING}.
    * @param msg to log, never <code>null</code>.
    * @param th {@link Throwable}, may be <code>null</code>.
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
         RxLogger.logWarn("Not supported Log level: " + type + " msg: " +
               msg);
      }         
      
      if (th != null)
         RxLogger.logError(th);
   }
   
   /**
    * Looks up a field label from the IS resource for a given field name.
    * The resource key is constructed using the following scheme:
    * BeanName + "." + fieldName + ".label".
    * If not found logs a DBG level message and returns a given fieldName.  
    * 
    * @param fieldName never <code>null</code>, may be <code>empty</code>.
    * @return field label, never <code>null</code>, may be <code>empty</code>.
    */
   protected String getFieldLabel(String fieldName)
   {
      if (fieldName== null)
         throw new IllegalArgumentException("fieldName may not be null");
      
      if (fieldName.trim().length()==0)
         return fieldName;
      
      String label = fieldName;
      String key = fieldName;
      
      try
      {
         label = RxInstallerProperties.getResources().
            getString(getTitle() + "." + fieldName + ".label");
      }
      catch(Exception ex)
      {
         this.logEvent(RxIAUtils.WARNING, "Failed to lookup resource key: " +
               key);
      }
      
      return label;
   }
   
   /**
    * Looks up a field default value from the IS resource for a given field name.
    * The resource key is constructed using the following scheme:
    * BeanName + "." + fieldName.
    * If not found logs a DBG level message and returns a given fieldName.  
    * 
    * @param fieldName never <code>null</code>, may be <code>empty</code>.
    * @return field label, never <code>null</code>, may be <code>empty</code>.
    */
   protected String getFieldDefault(String fieldName)
   {
      if (fieldName== null)
         throw new IllegalArgumentException("fieldName may not be null");
      
      if (fieldName.trim().length()==0)
         return fieldName;
      
      String label = fieldName;
      String key = fieldName;
      
      try
      {
         label = RxInstallerProperties.getResources().
            getString(getTitle() + "." + fieldName);
      }
      catch(Exception ex)
      {
         this.logEvent(RxIAUtils.WARNING, "Failed to lookup resource key: " +
               key);
      }
      
      return label;
   }

   /**
    * Initializes the panel.  Called in {@link #setup()}.
    */
   protected void initialize()
   {
      if (m_consoleUtils == null)
         m_consoleUtils = new RxIAConsoleUtils(consoleProxy);
      
      setInit(true);
   }
   
   /*
    * (non-Javadoc)
    * @see com.zerog.ia.api.pub.CustomCodeConsoleAction#executeConsoleAction()
    */
   @Override
   public void executeConsoleAction() throws PreviousRequestException
   {   
      do
      {
         execute();
      }
      while (!getModel().queryExit());
   }      
   
   /*
    * (non-Javadoc)
    * @see com.zerog.ia.api.pub.CustomCodeConsoleAction#setup()
    */
   @Override
   public boolean setup()
   {
      if (!isInitialized())
         initialize();
      
      RxModel model = getModel();
      
      if (!model.entered())
         return false;
      
      boolean shouldEnter = model.queryEnter();
      if (shouldEnter)
         entering();
      
      return shouldEnter;
   }
   
   /**
    * The main processing method to be overridden by all subclasses and called
    * at install time in {@link #executeConsoleAction()}.
    * 
    * @throws RxIAPreviousRequestException
    */
   protected abstract void execute() throws RxIAPreviousRequestException;
      
   /**
    * @return <code>true</code> if the panel has been initialized,
    * <code>false</code> otherwise.
    */
   private boolean isInitialized()
   {
      return m_init;
   }
   
   /**
    * Sets the console's initialization flag.
    * 
    * @param init <code>true</code> if the console has been initialized,
    * <code>false</code> otherwise.
    */
   private void setInit(boolean init)
   {
      m_init = init;      
   }
   
   /**
    * Sets the data model of this console.  Also adds the model to the list of
    * current data models.
    * 
    * @param model holds user input collected by this console.
    */
   protected void setModel(RxModel model)
   {
      m_model = model;
      m_models.add(model);
   }
   
   /**
    * @return data model responsible for storing user input.
    */
   protected RxModel getModel()
   {
      return m_model;
   }
   
   @Override
   public String getTitle()
   {
      return getModel().getTitle();
   }
   
   /**
    * See {@link IPSProxyLocator#getProxy()} for details.
    * 
    * @return the proxy object for this console.
    */
   public Object getProxy()
   {
      return consoleProxy;
   }
   
   /**
    * Provides access to the list of current data models.
    * 
    * @return the list of <code>RxModel</code> objects currently in memory.
    */
   public static List<RxModel> getModels()
   {
      return m_models;
   }
   
   /**
    * Method to get the utility which can be used by custom console components
    * for collecting user input by generating various types of prompts.
    * 
    * @return utility for reading from/writing to the console, never
    * <code>null</code> after console is initialized.
    */
   protected RxIAConsoleUtils getConsoleUtils()
   {
      return m_consoleUtils;
   }
   
   /**
    * Calls {@link RxIAUtils#getValue(VariableAccess, String)} to get the
    * value of the specified variable.
    * 
    * @param var the install variable, may not be <code>null</code> or
    * empty.
    * 
    * @return the value of the variable.
    */
   protected String getInstallValue(String var)
   {
      if (var == null || var.trim().length() == 0)
         throw new IllegalArgumentException("var may not be null or empty");
      
      return RxIAUtils.getValue(consoleProxy, var);
   }
   
   /**
    * Calls {@link RxIAUtils#setValue(VariableAccess, String, String)} to set
    * the value of the specified variable.
    * 
    * @param var the install variable, may not be <code>null</code> or
    * empty.
    * @param val the value to set for the install variable, may not be
    * <code>null</code> or empty.
    */
   protected void setInstallValue(String var, String val)
   {
      if (var == null || var.trim().length() == 0)
         throw new IllegalArgumentException("var may not be null or empty");
      
      if (val == null || val.trim().length() == 0)
         throw new IllegalArgumentException("val may not be null or empty");
      
      RxIAUtils.setValue(consoleProxy, var, val);
   }
   
   /**
    * See {@link CustomCodeConsoleProxy#abortInstallation(int)} for details.
    */
   protected void abortInstallation()
   {
      consoleProxy.abortInstallation(0);
   }
   
   /**
    * Gives the user the option to abort the installation by pressing enter.
    * 
    * @param enablePrevious if <code>true</code> the user will be able to go to
    * the previous panel.
    * 
    * @throws RxIAPreviousRequestException
    */
   protected void promptAndAbort(boolean enablePrevious) throws
   RxIAPreviousRequestException
   {
      String prompt = "PRESS [ENTER] TO ABORT THE INSTALLATION";
      
      if (enablePrevious)
         getConsoleUtils().enterToContinue(prompt);
      else
         getConsoleUtils().enterToContinuePrevDisabled(prompt);
      
      abortInstallation();
   }
   
   /**
    * Finds an item in an array and returns the index of the item.
    * 
    * @param items an array of String values to search, may not be
    * <code>null</code>.
    * @param item the String value to search for.
    * 
    * @return the index of the item in the array, 0 if not found.
    */
   protected int getItemIndex(String[] items, String item)
   {
      if (items == null)
         throw new IllegalArgumentException("items may not be null");
      
      int index = 0;
      for (int i = 0; i < items.length; i++)
      {
         if (items[i].equals(item))
         {
            index = i;
            break;
         }
      }
      
      return index;
   }
   
   /**
    * Provides access to variable manipulation methods.
    * 
    * @return the proxy object's <code>VariableAccess</code> control mechanism.
    */
   protected VariableAccess getVariableAccess()
   {
      return (VariableAccess) getProxy();
   }
   
   /**
    * <code>true</code> if the console has been initialized, <code>false</code>
    * otherwise.  Set in {@link #initialize()}.
    */
   private boolean m_init = false;
   
   /**
    * This is the data model for the console which is responsible for storing
    * user input.
    */
   private RxModel m_model = null;
   
   /**
    * Used by custom console components for collecting user input by 
    * generating prompts and writing to the console.  Initialized in
    * {@link #initialize()}, never <code>null</code> after that.
    */
   private static RxIAConsoleUtils m_consoleUtils = null;
   
   /**
    * See {@link #getModels()}.
    */
   private static List<RxModel> m_models = new ArrayList<>();
}
