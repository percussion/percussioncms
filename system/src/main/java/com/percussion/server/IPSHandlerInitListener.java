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
