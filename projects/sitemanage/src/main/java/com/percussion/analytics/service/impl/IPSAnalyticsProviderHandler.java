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
package com.percussion.analytics.service.impl;

import com.percussion.analytics.error.PSAnalyticsProviderException;
import com.percussion.share.service.exception.PSValidationException;
import java.util.Map;

/**
 * Handles connections and data transfer for a specific analytics provider.
 */
public interface IPSAnalyticsProviderHandler
{
   
   /**
    * Retrieves a list of "profiles" from the provider. Profiles are basically id's used to
    * get access to a particular data set from the provider.
    * @param uid the user id for access to the provider. Cannot be <code>null</code> or empty.
    * @param password the password for access to the provider. Cannot be <code>null</code> or empty.
    * @return a map of strings, with the key being the profile value and the value being the
    * profile display value. Never <code>null</code>, may be empty.
    * @throws <code>PSAnalyticsProviderException</code>, upon any error.
    */
   public Map<String, String> getProfiles(String uid, String password) throws PSAnalyticsProviderException, PSValidationException;
   
   /**
    * Tests a connection to the provider using the specified credentials.
    * @param uid the user id for access to the provider. Cannot be <code>null</code> or empty.
    * @param password the password for access to the provider. Cannot be <code>null</code> or empty.
    * @throws PSAnalyticsProviderException if failed to connect.
    */
   public void testConnection(String uid, String password) throws PSAnalyticsProviderException, PSValidationException;
}
