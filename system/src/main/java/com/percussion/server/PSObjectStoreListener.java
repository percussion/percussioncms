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

