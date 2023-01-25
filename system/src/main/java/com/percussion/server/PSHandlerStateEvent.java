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
 * Handler state change event. The caller will make use of this object to 
 * inform the handler state change listener that the state of the handler is
 * changed.
 * <p>
 * Only two states are currently supported by this class:
 * <ol>
 * <li> The handler that was not running previously is started and </li> 
 * <li> The handler that was running previously is stopped </li>
 * </ol>
 * This is an immutable class.
 * @see IPSHandlerStateListener
 * 
 */
public class PSHandlerStateEvent
{
   /**
    * Constructor. Takes the handler name and the handler state event.
    * @param handlerName name of the handler the state event is applicable, 
    * must not be <code>null</code> or empty. No validation is performed 
    * to see if a handler with this name really exists in the system. {@link 
    * #getHandlerName()} for more detailed description.
    * @param event handler state event, must be one of the 
    * HANDLER_EVENT_XXX (one bit value) constants defined in this class.
    * Though the listener registartion (@link com.percussion.server.PSServer
    * #addHandlerStateListener(IPSHandlerStateListener, String, int)} can be 
    * done for multiple events (the flags ORed) the triggered event will have 
    * only one bit flag indicating the event.
    */
   public PSHandlerStateEvent(String handlerName, int event)
   {
      if (handlerName == null || handlerName.length() < 1)
         throw new IllegalArgumentException("handlerName must not be empty");
      if (!isValidEvent(event))
      {
         throw new IllegalArgumentException(
            "event must be one of the HANDLER_EVENT_XXX");
      }

      m_handlerName = handlerName;
      m_stateEvent = event;
   }

   /**
    * Access method for the handler name. The handler name has the following 
   * meaning:
   * <ul>
   * <li>Application name for Rhythmyx applications, and </li>
   * <li>handler name for the loadable requets handlers (handlerName 
   * attribute of RequestHandlerDef element in the handler definition file
   * i.e. rxconfig/Server/RequestHandlers.xml).</li>
   * </ul>
    * @return Rhytmyx handler name the event is applicable for.
    */   
   public String getHandlerName()
   {
      return m_handlerName;
   }
   
   /**
    * Accessor for the state event the handler. The server will create an 
    * event object with one of the HANDLER_EVENT_XXX flags and inform the 
    * registered listeners indicating the state of the handler is changed. 
    * @return state event for the handler. One of the HANDLER_EVENT_XXX 
    * values defined in this class.
    */
   public int getStateEvent()
   {
      return m_stateEvent;
   }

   /**
    * Check if the supplied state event is a valid one. A valid state event 
    * must have one of the HANDLER_EVENT_XXX values.
    * @param event event value to be checked.
    * @return <code>true</code> if the event value is from one of the 
    * HANDLER_EVENT_XXX constants, <code>false</code> otherwise.
    */
   private boolean isValidEvent(int event)
   {
      if (event == HANDLER_EVENT_STARTED || event == HANDLER_EVENT_STOPPED)
         return true;

      return false;
   }
   
   /**
    * Name of the Rhythmyx handler the state event is applicable for. See 
    * {@link #getHandlerName()} for more detailed description.
    * Initialized in the constructor and never <code>null</code> after that.
    */
   private String m_handlerName = null;

   /**
    * Event flag this handler state event is valid for. See {@link 
    * #getStateEvent()} for more details. Initialized in the 
    * constructor. Shall be one of the HANDLER_EVENT_XXX constants 
    * after initialization.
    */
   private int m_stateEvent = 0;

   /**
    * Event flag indicating the handler has just started. This event will be 
   * triggered just after the applications/handler is started.
   */
   public static final int HANDLER_EVENT_STARTED = 1;

   /**
    * Event flag indicating the handler has just stopped. This event will be 
   * triggered just after the applications/handler is stopped.
    */
   public static final int HANDLER_EVENT_STOPPED = 2;
}
