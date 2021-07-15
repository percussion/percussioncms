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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.analytics.service.impl.google;

import com.percussion.analytics.error.IPSAnalyticsErrorMessageHandler;
import com.percussion.analytics.error.PSAnalyticsProviderException;
import com.percussion.analytics.error.PSAnalyticsProviderException.CAUSETYPE;

import java.util.HashMap;
import java.util.Map;

/**
 * @author erikserating
 *
 */
public class PSGoogleAnalyticsErrorMessageHandler
         implements
            IPSAnalyticsErrorMessageHandler
{

   /* (non-Javadoc)
    * @see com.percussion.analytics.error.IPSAnalyticsErrorMessageHandler#getMessage(
    * com.percussion.analytics.error.PSAnalyticsProviderException)
    */
   public String getMessage(PSAnalyticsProviderException e)
   {
      String preMsg = "Unable to retrieve analytics data.  ";
      String errorMsg;
      CAUSETYPE causetype = e.getCauseType();
      if (causetype != null)
      {           
         errorMsg = messages.get(causetype); 
         if(errorMsg == null)
            errorMsg = e.getLocalizedMessage();
      }
      else
      {
          errorMsg = e.getLocalizedMessage();
      }
      
      return preMsg + errorMsg;
  }
   
   private static Map<CAUSETYPE, String> messages = new HashMap<>();
   static
   {
      messages.put(CAUSETYPE.ACCOUNT_DELETED, "The analytics account has been deleted.");
      messages.put(CAUSETYPE.ACCOUNT_DISABLED, "The analytics account has been disabled.");         
      messages.put(CAUSETYPE.ANALYTICS_NOT_CONFIG, "Please use the Google Setup gadget to connect to your analytics account.");         
      messages.put(CAUSETYPE.AUTHENTICATION_ERROR, "The analytics account could not be authenticated.");         
      messages.put(CAUSETYPE.NO_PROFILE, "Please use the Google Setup gadget to select a profile for the desired site(s).");         
      messages.put(CAUSETYPE.NO_ANALYTICS_ACCOUNT, "A valid analytics account is required.");         
      messages.put(CAUSETYPE.NOT_VERIFIED, "The analytics account could not be verified.");         
      messages.put(CAUSETYPE.INVALID_CREDS, "Invalid Google configuration. Please use the Google Setup gadget to connect to your "
             + "analytics account.");         
      messages.put(CAUSETYPE.INVALID_DATA, "Invalid data.");         
      messages.put(CAUSETYPE.SESSION_EXPIRED, "The current session has expired.");         
      messages.put(CAUSETYPE.SERVICE_UNAVAILABLE, "The service is currently unavailable.");         
      messages.put(CAUSETYPE.TERMS_NOT_AGREED, "Terms not agreed.");
         
   }
   
}

