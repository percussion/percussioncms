/**[ RxIAPanel.java ]**********************************************
 *
 * COPYRIGHT (c) 2004 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ****************************************************************************/
package com.percussion.installanywhere;

import com.percussion.installer.action.RxLogger;
import com.percussion.installer.model.RxModel;
import com.percussion.installer.panel.RxVLayoutManager;
import com.percussion.install.RxInstallerProperties;

import com.zerog.ia.api.pub.CustomCodePanel;
import com.zerog.ia.api.pub.CustomCodePanelProxy;
import com.zerog.ia.api.pub.CustomError;
import com.zerog.ia.api.pub.GUIAccess;
import com.zerog.ia.api.pub.VariableAccess;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;


/**
 * Abstract Base for all Rx AWT panel implementations. Sets up a vertical
 * AWT stack layout to ease custom panel development. Derived panels simply
 * call super.initilize and then use rxAdd method to stack UI components one
 * on top of the other with a default vertical gap of 4. Anything can be
 * customized by using {@link RxVLayoutManager} with custom parameters.
 * The default layout however is the most commonly used one.
 */
public abstract class RxIAPanel extends CustomCodePanel implements
IPSProxyLocator
{
   @Override
   @SuppressWarnings("unused")
   public boolean setupUI(CustomCodePanelProxy proxy)
   {
      if (!isInitialized())
         initialize();
      
      RxModel model = getModel();
      if (model == null)
      {
         logEvent(CustomError.ERROR, "RxIAPanel#setupUI : model is null, " +
               getTitle() + " panel will be skipped.");
         return false;
      }
  
      boolean shouldEnter = model.queryEnter();
      if (shouldEnter)
         entering();
      
      return shouldEnter;
   }
   
   @Override
   public void panelIsDisplayed()
   {
      if (!getModel().entered())
      {
         getGUIAccess().goNext();
         return;
      }
      
      entered();
   }
   
   /**
    * Initializes the panel.  Called in {@link #setupUI(CustomCodePanelProxy)}.
    */
   protected void initialize()
   {
      setLayout(new BorderLayout());
      
      m_rxPanel = new Panel();
      m_rxPanel.setLayout(new RxVLayoutManager(RxVLayoutManager.VERTICAL,1));
      
      add(m_rxPanel, BorderLayout.CENTER);
      
      setInit(true);
   }
   
   @Override
   public boolean okToContinue()
   {
      exiting();
      
      boolean ok = getModel().queryExit();
      if (ok)
         getModel().exited();
      
      return ok;
   }
    
   /**
    * See {@link IPSProxyLocator#getProxy()} for details.
    * 
    * @return the proxy object for this console.
    */
   public Object getProxy()
   {
      return customCodePanelProxy;
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
    * Enables/disables the panel's previous button.  Calls
    * {@link GUIAccess#setPreviousButtonEnabled(boolean)}.
    * 
    * @param enabled <code>true</code> to enable the button, <code>false</code>
    * to disable.
    */
   protected void setPreviousButtonEnabled(boolean enabled)
   {
      getGUIAccess().setPreviousButtonEnabled(enabled);
   }
   
   /**
    * Enables/disables the panel's next button.  Calls
    * {@link GUIAccess#setNextButtonEnabled(boolean)}.
    * 
    * @param enabled <code>true</code> to enable the button, <code>false</code>
    * to disable.
    */
   protected void setNxtButtonEnabled(boolean enabled)
   {
      getGUIAccess().setNextButtonEnabled(enabled);
   }
   
   /**
    * Main rx panel.
    * 
    * @return rxPanel, never <code>null</code>.
    */
   protected Panel getRxPanel()
   {
      return m_rxPanel;  
   } 
   
   /**
    * Sets the given item as the selected item for the given component if a
    * match is found.
    * 
    * @param component modified to select <code>item</code>, may not be
    * <code>null</code>. 
    * @param item this item will be selected in <code>component</code> if it
    * exists.
    */
   protected void setSelection(Choice component, String item)
   {
      if (component == null)
         throw new IllegalArgumentException("component may not be null");
      
      for (int i = 0; i < component.getItemCount(); i++)
      {
         if (component.getItem(i).equals(item))
         {
            component.select(i);
            break;
         }
      }
   }
   
   /**
    * Adds given comp. to content pane stacked vertically, aligned by a given
    * tag.
    * @param tag describes alignment of the component.
    * @param c comp., never <code>null</code>.
    * @return comp., never <code>null</code>.
    */
   protected Component rxAdd(String tag, Component c)
   {
      if (c == null)
         throw new IllegalArgumentException("c may not be null");
      
      Panel rxPanel = getRxPanel();
      if (rxPanel == null)
         throw new IllegalStateException("must call initialize before using " +
               "rx methods");

      rxPanel.add(tag, c);
      return c;
   }
      
   /**
    * Adds given comp. to content pane stacked vertically, left top aligned.
    * @param c comp., never <code>null</code>.
    * @return comp., never <code>null</code>.
    */
   protected Component rxAdd(Component c)
   {
      if (c== null)
         throw new IllegalArgumentException("c may not be null");

      return rxAdd("LeftTopWide", c);
   }
   
   /**
    * Adds empty label of a given height that creates a desired v gap.
    * @param height the height of the desired v gap.
    * @return empty label component.
    */
   protected Component rxAdd(int height)
   {
      if (height < 0)
         throw new IllegalArgumentException("height may not < 0");
      
      Label l = new Label();
      l.setSize(0, height);
      
      return rxAdd("LeftTopWide", l);
   }
   
   /**
    * Recursive function for finding and getting the parent frame
    * of the given component; returns null to indicate that no
    * parent frame could be found in that recursion of the function.
    *
    * @param baseComponent for start of recursive call, may be
    * <code>null</code> in which case <code>null</code> is returned
    *
    * @return Frame parent frame, or <code>null</code>
    */
   public Frame getParentFrame(Component baseComponent)
   {
      if (baseComponent == null)
      {
         return null;
      }
      Component parentComponent = baseComponent.getParent();
      if (parentComponent == null)
      {
         return null;
      }
      else if (parentComponent instanceof Frame)
      {
         return (Frame)parentComponent;
      }
      else
      {
         return getParentFrame(parentComponent);
      }
   }      
   
   /**
    * Logs event to log file.
    * @param type must be one of {@link CustomError#ERROR}, 
    * {@link CustomError#WARNING}.
    * @param msg never <code>null</code>. 
    */
   public void logEvent(int type, Object msg)
   {  
      this.logEvent(type, msg, null);
   }
   
   /**
    * Logs event to log file.
    * @param type must be one of {@link CustomError#ERROR}, 
    * {@link CustomError#WARNING}
    * @param msg never <code>null</code>.
    * @param th may be <code>null</code>.
    */
   protected void logEvent(int type, Object msg, Throwable th)
   {
      if (type == CustomError.ERROR)
         RxLogger.logError(msg);
      else if (type == CustomError.WARNING)
         RxLogger.logWarn(msg);
      else
      {
         RxLogger.logWarn("Not supported error level: " + type + " msg: " +
               msg);
      }         
      
      if (th!=null)
         RxLogger.logError(th);
   }
   
   /**
    * Helper method that parses a given color name and returns a corresponding
    * Color instance.
    * @param colorName may be any color name that is supported by the Color
    * class, if <code>null</code> or <code>empty</code> then assumes default. 
    * @param defaultColor Color instance to assume if parse fails,
    * never <code>null</code>.
    * @return color instance, never <code>null</code>.
    */
   public Color parseColor(String colorName, Color defaultColor)
   {
      if (defaultColor == null)
         throw new IllegalArgumentException("defaultColor may not be null");

      if (colorName == null || colorName.trim().length()==0)
         return defaultColor;
      
      Color color = defaultColor;
      
      //speed up most often used ones
      if (colorName.trim().equalsIgnoreCase("white"))
         return Color.WHITE;
      if (colorName.trim().equalsIgnoreCase("lightGray"))
         return Color.lightGray;
      
      //figure out color id from reflected color defines
      boolean found = false;
      final Field[] fields = color.getClass().getDeclaredFields();
      for (int i = 0; i < fields.length; i++)
      {
         Field f = fields[i];
         
         int mod = f.getModifiers();
         
         if (!Modifier.isPublic(mod) && Modifier.isFinal(mod))
             continue;

         String fName = f.getName();
         if (!fName.equalsIgnoreCase(colorName))
            continue;
                            
         try
         {
            color = (Color)f.get(color);
            found = true;
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         break;
         
      }
      
      if (!found)
      {
         //is it an RGB value?
         try
         {
            Color c = Color.decode(colorName);
            color = c;
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
      
      return color;
   } 
   
   @Override
   public String getTitle()
   {
      return getModel().getTitle();
   }
   
   /**
    * Allows ui components to be updated if their corresponding data model
    * property is modified.
    * 
    * @param propName the name of the property which has been modified, may not
    * be <code>null</code>.
    */
   public void propertyChanged(String propName)
   {
      if (propName == null)
         throw new IllegalArgumentException("propName may not be null");
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
         this.logEvent(CustomError.WARNING, "Failed to lookup resource key: " +
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
         this.logEvent(CustomError.WARNING, "Failed to lookup resource key: " +
               key);
      }
      
      return label;
   }     
   
   /**
    * This method is called before the panel is displayed in
    * {@link #setupUI(CustomCodePanelProxy)}.
    */
   protected abstract void entering();
   
   /**
    * This method is called after the panel has been displayed in
    * {@link #panelIsDisplayed()}. 
    */
   protected abstract void entered();
   
   /**
    * This method is called during panel exit in {@link #okToContinue()}.
    */
   protected abstract void exiting();
   
   /**
    * Sets the data model of this panel.  Also sets this panel on the model and
    * adds the model to the current list of data models.
    * 
    * @param model holds user input collected by this panel.
    */
   protected void setModel(RxModel model)
   {
      m_model = model;
      m_model.setPanel(this);
      m_models.add(model);
   }
   
   /**
    * @return data model responsible for storing user input.
    */
   protected RxModel getModel()
   {
      return m_model;
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
      
      return RxIAUtils.getValue(customCodePanelProxy, var);
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
      
      RxIAUtils.setValue(customCodePanelProxy, var, val);
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
    * Constant for the previous button navigation option.
    */
   public static final String NAV_OPT_PREVIOUS = "Previous";
   
   /**
    * Constant for the next button navigation option.
    */
   public static final String NAV_OPT_NEXT = "Next";
   
   /**
    * @return <code>true</code> if the panel has been initialized,
    * <code>false</code> otherwise.
    */
   private boolean isInitialized()
   {
      return m_init;
   }
   
   /**
    * Sets the panel's initialization flag.
    * 
    * @param init <code>true</code> if the panel has been initialized,
    * <code>false</code> otherwise.
    */
   private void setInit(boolean init)
   {
      m_init = init;
   }
   
   /**
    * Provides access to manipulate the panel's control options.
    * 
    * @return the proxy object's <code>GUIAccess</code> control mechanism.
    */
   private GUIAccess getGUIAccess()
   {
      return (GUIAccess) customCodePanelProxy.getService(GUIAccess.class);
   }
   
   /**
    * Default border panel.  Initialized in {@link #initialize()}, never
    * <code>null</code> after that.
    */
   private Panel m_rxPanel = null;   
   
   /**
    * <code>true</code> if the panel has been initialized, <code>false</code>
    * otherwise.  Set in {@link #initialize()}.
    */
   private boolean m_init = false;
   
   /**
    * This is the data model for the panel which is responsible for storing user
    * input.
    */
   private RxModel m_model = null;
   
   /**
    * See {@link #getModels()}.
    */
   private static List<RxModel> m_models = new ArrayList<>();
}
