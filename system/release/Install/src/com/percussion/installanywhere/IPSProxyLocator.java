/******************************************************************************
 *
 * [ IPSProxyLocator.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installanywhere;

import com.zerog.ia.api.pub.CustomCodeConsoleProxy;
import com.zerog.ia.api.pub.CustomCodePanelProxy;
import com.zerog.ia.api.pub.CustomCodeRuleProxy;
import com.zerog.ia.api.pub.InstallerProxy;


/**
 * A proxy object is used by installer custom components to access the install
 * environment at runtime.  This includes access to variables, services, and
 * resources.
 * 
 * @author peterfrontiero
 */
public interface IPSProxyLocator
{
   /**
    * Gets the installer specific proxy object associated with the implementing
    * action, console, panel, or rule.  Currently, the type of proxy object
    * returned will be one of the following: {@link InstallerProxy},
    * {@link CustomCodePanelProxy}, {@link CustomCodeConsoleProxy},
    * {@link CustomCodeRuleProxy}.
    * 
    * @return the proxy object used for install runtime access.  It must be cast
    * to the appropriate type.
    */
   public abstract Object getProxy();
}
