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

package com.percussion.security;

import java.util.Iterator;

/**
 * This version extends its base class by returning more information about the
 * failure. Specifically, a list of security providers for which authentication
 * was attempted and failed is returned. This can aid the user in their re-attempt
 * to login by indicating what UID/pw should be used. For example, if the user
 * thought that NT security was being used, but DBMS security was actually used,
 * the name of the failed provider would point them to the correct credentials
 * to use. <p/>
 * If this information is not available to the user, the base class should be
 * used as this class won't allow instances w/o at least 1 named security
 * provider.<p/>
 * Note that this will allow a hacker to easily catalog security providers.
 * For this reason, a flag is available in the server config that will tell
 * the server not to return this exception.
 *
 * @see com.percussion.design.objectstore.PSServerConfiguration#allowDetailedAuthenticationMessages
 */
public class PSAuthenticationFailedExException extends
   PSAuthenticationFailedException
{
   /**
    * Creates a new instance of this object. If no list of failed security
    * provider errors is available, use the base class rather than this class.
    *
    * @param failedSPExceptions A non-null, non-empty list of security providers
    * for which authentication failed. Each entry in the list must be a
    * PSAuthenticationFailedException.
    *
    * @throws IllegalArgumentException If the supplied list is null, empty or
    * contains null entries.
    *
    * @throws ClassCastException If any entry in the supplied list is not a
    * PSAuthenticationFailedException
    */
   public PSAuthenticationFailedExException( Iterator failedSPExceptions )
   {
      /* Note: Iterator was used instead of [] for future extensibility.
         We may want to return different kinds of objects with more info at a
         later time. */
      super( IPSSecurityErrors.MULTI_AUTHENTICATION_FAILED, new Object[1] );
      if ( null == failedSPExceptions || !failedSPExceptions.hasNext())
      {
         throw new IllegalArgumentException(
            "Must provide at least 1 security provider." );
      }

      StringBuilder msg = new StringBuilder(255);
      while ( failedSPExceptions.hasNext())
      {
         PSAuthenticationFailedException e =
            (PSAuthenticationFailedException) failedSPExceptions.next();
         if ( null == e )
            throw new IllegalArgumentException(
               "Invalid entry for security provider exception" );
         msg.append( "\r\n" );
         msg.append( e.getLocalizedMessage());
      }
      m_args[0] = msg.toString();
   }
}
