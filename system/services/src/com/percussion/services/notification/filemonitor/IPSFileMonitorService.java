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
package com.percussion.services.notification.filemonitor;

import java.io.File;

/**
 * The File Monitor service allows the monitoring of change events to files
 * on the operating system and reacting to those changes.
 * 
 * The implementation will send File Change Notifications that are handled by 
 * the IPSNotificationService, IPSNotificationListener, (and optionally 
 * PSNotificationHelper.)
 * Specifically, the objects receiving notification of the changes (to the 
 * monitored files) must:
 * <ol>
 *  <li> Register for the notifications using the IPSNotificationListener,</li>
 *  <li> Receive the notification by implementing IPSNotificationListener.</li>
 * </ol>
 * 
 */
public interface IPSFileMonitorService
{
   /**
    * Start a monitor for the file represented by the file object
    *
    * @param fileObject the object representing file to monitor, never <code>null</code>
    */
   void monitorFile(File fileObject);

   /**
    * Halt a monitor for the file represented by the file object
    *
    * @param fileObject the object representing file to monitor, never <code>null</code>
    */
   void unmonitorFile(File fileObject);

}
