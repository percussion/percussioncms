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

/**
 * Interface for classes that wish to be notified of request handler 
 * initializations.   To be registered, {@link PSServer} must be modified so 
 * that {@link PSServer#getHandlerInitListeners()} returns an instance of the 
 * class impelementing this interface.  The application handler will notify
 * each listener as described below, passing each dataset handler as it is
 * initialized or shutdown.
 */
public interface IPSHandlerInitListener 
{
   
   /**
    * Method is called when a handler is initialized.  Implementers of this
    * interface must have registered for such events.
    * 
    * @param requestHandler The handler being initialized, guaranteed not
    * <code>null</code> by this interface.
    */
   public void initHandler(IPSRequestHandler requestHandler);

   /**
    * Inform the listener that the app containing this resource is
    * shutting down.
    *
    * @param requestHandler The request handler that is shutting down.  May not 
    * be <code>null</code>.
    */
   public void shutdownHandler(IPSRequestHandler requestHandler);
}