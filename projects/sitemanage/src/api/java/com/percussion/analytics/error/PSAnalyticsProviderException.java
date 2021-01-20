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
package com.percussion.analytics.error;

import com.percussion.share.service.exception.PSDataServiceException;

/**
 * Exception thrown by the analytics provider service and handlers.
 * @author erikserating
 *
 */
public class PSAnalyticsProviderException extends PSDataServiceException
{   

   public PSAnalyticsProviderException(String message, Throwable cause)
   {
      this(message, cause, null);
   }
  
   public PSAnalyticsProviderException(String message, Throwable cause, CAUSETYPE type)
   {
     super(message, cause);
     if(type == null)
        type = CAUSETYPE.UNKNOWN;
     causetype = type;
   }

   public PSAnalyticsProviderException(String message)
   {
      this(message, CAUSETYPE.UNKNOWN);
   }
   
   public PSAnalyticsProviderException(String message, CAUSETYPE type)
   {
      super(message);
      if(type == null)
         type = CAUSETYPE.UNKNOWN;
      causetype = type;
   }

   public PSAnalyticsProviderException(Throwable cause)
   {
      this(cause, null);
   }
   
   public PSAnalyticsProviderException(Throwable cause, CAUSETYPE type)
   {
      super(cause);
      if(type == null)
         type = CAUSETYPE.UNKNOWN;
      causetype = type;
      
   }
   
   /**
    * Get the cause type enum value. 
    * @return the cause type enum value, never <code>null</code>. Defaults
    * to <code>CAUSETYPE.UNKNOWN</code> if not set.
    */
   public CAUSETYPE getCauseType()
   {
      return causetype;
   }  
   
   public enum CAUSETYPE
   {
       ACCOUNT_DELETED,
       ACCOUNT_DISABLED,
       ANALYTICS_NOT_CONFIG,
       AUTHENTICATION_ERROR,
       NO_PROFILE,
       NO_ANALYTICS_ACCOUNT,
       NOT_VERIFIED,
       INVALID_CREDS,
       INVALID_DATA,
       SESSION_EXPIRED,
       SERVICE_UNAVAILABLE,
       TERMS_NOT_AGREED,
       UNKNOWN
   }
   
   private CAUSETYPE causetype = CAUSETYPE.UNKNOWN;
}
