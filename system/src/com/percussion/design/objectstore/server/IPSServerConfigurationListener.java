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

