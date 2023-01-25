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
package com.percussion.services.notification;

import com.percussion.services.error.PSNotFoundException;
import com.percussion.share.service.exception.PSDataServiceException;

/**
 * A notification listener is informed of a change that has been reported to the
 * notification service.
 * 
 * @author dougrand
 */
public interface IPSNotificationListener
{
   /**
    * Notify the listener of an event. An implementer of this method should take
    * care to not spend a great deal of time responding to the notification.
    * Instead, state should be stored and the actual response handled
    * asynchronously.
    * <p>
    * A listener can count on only being called for the event types that it was
    * registered to. Once it knows the type, it should check the information
    * passed to see if the event should be handled. It should not modify any
    * data passed with the event.
    * 
    * @param notification the notification event, never <code>null</code>
    */
   void notifyEvent(PSNotificationEvent notification) throws PSDataServiceException, PSNotFoundException;
}
