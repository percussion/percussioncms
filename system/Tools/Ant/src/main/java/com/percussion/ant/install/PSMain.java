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

import org.apache.tools.ant.BuildException;

/**
 * This class is used to allow the installer to continue running after the Ant
 * install is invoked.  This is made possible by overriding the
 * <code>exit</code> method of Ant's <code>Main</code> class (as of v1.7.0).
 * 
 * @author peterfrontiero
 */
public class PSMain extends org.apache.tools.ant.Main
{
   public PSMain(String[] args) throws BuildException {     
        super(args);
   }
   
   /**
    * This method does nothing.
    * 
    * @param exitCode not used.
    */
   protected void exit(int exitCode)
   {
      //Don't exit the JVM
   }
}
