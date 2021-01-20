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

import java.util.EventListener;

/**
 * Listener interface to inform the listeners that the handler's state is 
 * changed. If an object wants to receive notifications from the Rhythmyx 
 * server indicating something has changed with regard to one of its handlers 
 * (such as the handler is stopped or started), it should implement this 
 * interface and register itself by calling {@link com.percussion.server.
 * PSServer#addHandlerStateListener(IPSHandlerStateListener, String, int)}.
 */
public interface IPSHandlerStateListener extends EventListener
{
   /**
    * State of the handler is changed.
    * @param e handler state event, never <code>null</code>.
    */
   void stateChanged(PSHandlerStateEvent e);
}
