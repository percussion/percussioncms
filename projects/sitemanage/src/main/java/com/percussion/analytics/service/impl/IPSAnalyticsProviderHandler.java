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
package com.percussion.analytics.service.impl;

import com.percussion.analytics.error.PSAnalyticsProviderException;

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
   public Map<String, String> getProfiles(String uid, String password) throws PSAnalyticsProviderException;
   
   /**
    * Tests a connection to the provider using the specified credentials.
    * @param uid the user id for access to the provider. Cannot be <code>null</code> or empty.
    * @param password the password for access to the provider. Cannot be <code>null</code> or empty.
    * @throws PSAnalyticsProviderException if failed to connect.
    */
   public void testConnection(String uid, String password) throws PSAnalyticsProviderException;
}
