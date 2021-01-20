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
package com.percussion.server;

import java.util.Properties;

/**
 * A process that runs during server startup
 * 
 * @author JaySeletz
 *
 */
public interface IPSStartupProcess
{

   /**
    * Do any startup work required.  
    * 
    * @param startupProps The startup properties, indicating values each process can use to
    * determine what work, if any, to perform.  Processes may modify these values, which are
    * then persisted for the next server startup.
    */
   void doStartupWork(Properties startupProps) throws Exception;
   
   /**
    * Set the manager on the process at init, process should call {@link IPSStartupProcessManager#addStartupProcess(IPSStartupProcess)}
    * @param mgr The mgr, not <code>null</code>.
    */
   void setStartupProcessManager(IPSStartupProcessManager mgr);

}
