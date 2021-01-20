/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.ant.install;


/**
 * An install action bean to set up a flag indicating a standalone installation.
 * This will be used currently by devtoolssetup.exe.
 * 
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 * 
 * First set the taskdef:
 * 
 *  <code>  
 *  &lt;taskdef name="standaloneFlag"
 *              class="com.percussion.ant.install.PSStandaloneFlag"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 * 
 * Now use the task to set the flag.
 * 
 *  <code>
 *  &lt;standaloneFlag standalone="true"/&gt;
 *  </code>
 * 
 * </pre>
 * 
 * @author vamsinukala
 *
 */
public class PSStandAloneFlag extends PSAction
{
   // see base class
   @Override
   public void execute()
   {}
   
   /**
    *  The standalone property setter.
    *
    *  @param <code>true</code> if this is a standalone install,
    *  <code>false</code> otherwise.
    */
   public void setStandalone(boolean flag)
   {
      m_bStandalone = flag;
   }
   
   /**
    *  The standalone property getter.
    *
    *  @return <code>true</code> if this is a standalone install,
    *  <code>false</code> otherwise.
    */
   public boolean getStandalone()
   {
      return m_bStandalone;
   }
   
   /**
    *  @return <code>true</code> if this is a standalone install,
    *  <code>false</code> otherwise.
    */
   public static boolean isStandalone()
   {
      return m_bStandalone;
   }
   
   /**
    * Sets the type of Rx installation to standalone install.
    * @param type of installation, if <code>true</code> then the install is
    * not a multi suite, but rather a standalone such as DevToolsSetup.exe
    */
   public static void updateStandalone(boolean isStandalone)
   {
      m_bStandalone = isStandalone;
   }
   
   /**
    * standalone flag
    */
   public static boolean m_bStandalone = false;
   
}
