/******************************************************************************
 *
 * [ RxStandAloneFlag.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.action;

import com.percussion.installanywhere.RxIAAction;


/**
 * An install action bean to set up a flag indicating a standalone installation.
 * This will be used currently by devtoolssetup.exe.
 * 
 * @author vamsinukala
 */
public class RxStandAloneFlag extends RxIAAction
{
   /**
    * Constructs an {@link RxStandAloneFlag}.
    */
    public RxStandAloneFlag()
    {
       super();
    }

    @Override
    public void execute()
    {
       setStandalone(true);
    }

   /**************************************************************************
    * Bean Properties
    *************************************************************************/
   /**
    *  The standalone property setter.
    *
    *  @param flag <code>true</code> if this is a standalone install,
    *  <code>false</code> otherwise.
    */
    public void setStandalone(boolean flag)
    {
       ms_bStandalone = flag;
    }
    
    /**
     *  The standalone property getter.
     *
     *  @return <code>true</code> if this is a standalone install,
     *  <code>false</code> otherwise.
     */
     public boolean getStandalone()
     {
        return ms_bStandalone;
     }

    /**
     *  Used to determine if an installation is standalone.
     * 
     *  @return <code>true</code> if this is a standalone install,
     *  <code>false</code> otherwise.
     */
    public static boolean isStandalone()
    {
       return ms_bStandalone;
    }

    /**
     * Sets the type of Rx installation to standalone install.
     * @param isStandalone type of installation, if <code>true</code> then the
     * install is not a multi suite, but rather a standalone such as
     * DevToolsSetup.exe.
     */
    public static void updateStandalone(boolean isStandalone)
    {
       ms_bStandalone = isStandalone;
    }
   
    /**
     * Standalone flag.
     */
     public static boolean ms_bStandalone = false;
}
