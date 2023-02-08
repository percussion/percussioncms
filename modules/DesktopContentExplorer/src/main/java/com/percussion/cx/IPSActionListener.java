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
package com.percussion.cx;

/**
 * The interface to define a listener that listens to the processing of an 
 * action. (Typically menu actions).
 */
public interface IPSActionListener
{
   /**
    * Notification event for the listener to inform the current executing action
    * is completed. It informs the location which should be refreshed.
    * 
    * @param actionEvent the event that describes the action to take by the 
    * listeners, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if actionEvent is <code>null</code>.
    */
   public void actionExecuted(PSActionEvent actionEvent);
   
   /**
    * Notification event for the listener to inform that an action is initiated
    * and may take some time to execute the action. Provides a monitor object to
    * monitor the action process. 
    * 
    * @param processMonitor the monitor may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if processMonitor is <code>null</code>.
    */
   public void actionInitiated(PSProcessMonitor processMonitor);
   
}


