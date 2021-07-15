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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
