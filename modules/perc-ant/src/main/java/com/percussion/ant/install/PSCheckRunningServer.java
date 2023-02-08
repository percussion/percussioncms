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

import com.percussion.install.InstallUtil;
import org.apache.tools.ant.BuildException;


/**
 * An install action bean to check for a running Rhythmyx server.  If a running
 * server is found, a build exception will be thrown.
 * 
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 * 
 * First set the taskdef:
 * 
 *  <code>  
 *  &lt;taskdef name="checkRunningServer"
 *              class="com.percussion.ant.install.PSCheckRunningServer"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 * 
 * Now use the task to check for a running server.
 * 
 *  <code>
 *  &lt;checkRunningServer/&gt;
 *  </code>
 * 
 * </pre>
 * 
 * @author vamsinukala
 *
 */
public class PSCheckRunningServer extends PSAction
{
   // see base class
   @Override
   public void execute()
   {
      if (InstallUtil.checkServerRunning(getRootDir()))
      {
         throw new BuildException("A running Rhythmyx server has been " +
               "detected in the installation directory " + getRootDir() +
               ".  Please shut down this instance of Rhythmyx before " +
               "installing/upgrading to this location.");
      }
   }
}
