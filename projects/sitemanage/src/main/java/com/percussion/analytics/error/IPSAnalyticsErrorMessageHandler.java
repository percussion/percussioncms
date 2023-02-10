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
package com.percussion.analytics.error;


/**
 * @author erikserating
 *
 */
public interface IPSAnalyticsErrorMessageHandler
{
   /**
    * Returns the proper business message for the analytics exception
    * passed in.
    * @param e the analytics provider exception that was thrown. Cannot be <code>null</code>
    * @return the message, never <code>null</code>, may be empty.
    */
   public String getMessage(PSAnalyticsProviderException e);
}
