/******************************************************************************
 *
 * [ RxIARule.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installanywhere;

import com.zerog.ia.api.pub.CustomCodeRule;
import com.zerog.ia.api.pub.VariableAccess;

/**
 * This is an abstract, intermediate level class which links the InstallAnywhere
 * platform {@link CustomCodeRule} class with Rx custom rules.  Each
 * custom rule subclass will override the {@link #evaluate()} method with the
 * appropriate rule implementation.  This method is invoked at install time
 * by the {@link CustomCodeRule#evaluateRule()} method.
 * 
 * @author peterfrontiero
 */
public abstract class RxIARule extends CustomCodeRule implements
IPSProxyLocator
{
   @Override
   public boolean evaluateRule()
   {
      return evaluate();
   }
   
   /**
    * See {@link IPSProxyLocator#getProxy()} for details.
    * 
    * @return the proxy object for this console.
    */
   public Object getProxy()
   {
      return ruleProxy;
   }
   
   /**
    * The main processing method to be overridden by all subclasses and called
    * at install time in {@link #evaluateRule()}.
    * 
    * @return <code>true</code> if the rule passed, <code>false</code>
    * otherwise.
    */
   protected abstract boolean evaluate();
   
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
      
      return RxIAUtils.getValue(ruleProxy, var);
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
      
      RxIAUtils.setValue(ruleProxy, var, val);
   }
}
