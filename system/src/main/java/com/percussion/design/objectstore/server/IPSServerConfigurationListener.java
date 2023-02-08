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

package com.percussion.design.objectstore.server;

import com.percussion.design.objectstore.PSServerConfiguration;

import java.util.EventListener;


/**
 * The IPSServerConfigurationListener interface is implemented by classes
 * interested in trapping changes to the server's configuration object
 * in the object store. This allows applications to immediately react to
 * the changes.
 * <P>
 * At this time, changes are not vetoable. The recipient is merely notified
 * of the change after the action has been processed and a response has
 * been sent to the originator.
 * <P>
 * We are also not supporting notification for only changed components.
 * For instance, if you're only interested in changes to the
 * back-end connection objects, you must implement configurationUpdated
 * and determine if the back-end connection objects were changed on your
 * own.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public interface IPSServerConfigurationListener extends EventListener
{
   /**
    * Handle notification of changes to the server configuration object.
    *
    * @param   config         the configuration object
    */
   public void configurationUpdated(PSServerConfiguration config);
}

