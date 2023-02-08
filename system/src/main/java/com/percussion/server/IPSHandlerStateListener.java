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
