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

package com.percussion.server;



/**
 * The PSObjectStoreListener class is used by the server to hook 
 * changes in the object store. When a change is made to an object store
 * object (such as an E2 Application) the server is notified immediately
 * so it may alter its behavior when handling effected requests.
 * <p>
 * The server listens for the following object store changes:
 * <ul>
 * <li>SERVER_REQUEST_ROOT - notify on changes to the server
 *     request root</li>
 * <li>SERVER_DATA_ENCRYPTOR - notify on changes to the server's
 *     data encryption settings</li>
 * <li>SERVER_ACL - notify on changes to the server's ACL</li>
 * <li>APPLICATION_REQUEST_ROOT - notify on changes to an application
 *     request root</li>
 * <li>APPLICATION_DATA_ENCRYPTOR - notify on changes to an application's
 *     data encryption settings</li>
 * <li>APPLICATION_ACL - notify on changes to an application's ACL</li>
 * <li>DATASET_REQUEST_PAGE - notify on changes to a data set's request
 *     page name</li>
 * <li>DATASET_CONDITIONS - notify on changes to a data set's request
 *     conditions</li>
 * <li>DATASET_DATA_ENCRYPTOR - notify on changes to a data set's
 *     data encryption settings</li>
 * </ul>
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSObjectStoreListener extends Thread {

   /**
    * Construct an object store listener.
    */
   PSObjectStoreListener()
   {
      super();
   }

   /**
    * This method is called by Thread.start to begin execution of this
    * thread. This is where we'll create the connection listener and wait
    * for incoming requests.
    */
   public void run()
   {
   }
}

