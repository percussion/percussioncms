/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
