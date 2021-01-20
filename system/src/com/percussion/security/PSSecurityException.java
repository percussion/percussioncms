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

package com.percussion.security;

import com.percussion.error.IPSException;
import com.percussion.error.PSRuntimeException;


/**
 * This exception is thrown when a serious problem occurs that prevents the
 * security sub-system from obtaining the information it needs to perform
 * the requested action.
 */
public class PSSecurityException extends PSRuntimeException
{
   /**
    * Constructs a new exception based on an existing exception. Used to
    * pass through exceptions.
    *
    * @param errorCode The error code from the original exception.
    *
    * @param args The message arguments from the original exception.
    */
   public PSSecurityException( IPSException e )
   {
      super( e.getErrorCode(), e.getErrorArguments());
   }


   /**
    * See {@link PSRuntimeException#PSRuntimeException(int) base class} for
    * desc.
    */
   public PSSecurityException( int errorCode )
   {
      super( errorCode );
   }


   /**
    * See {@link PSRuntimeException#PSRuntimeException(int,Object[]) base
    * class} for desc.
    */
   public PSSecurityException( int errorCode, Object [] messageArgs )
   {
      super( errorCode, messageArgs );
   }

   /**
    * See {@link PSRuntimeException#PSRuntimeException(int,Object[],Throwable) base
    * class} for desc.
    */
   public PSSecurityException( int errorCode, Object [] messageArgs, Throwable cause )
   {
      super( errorCode, messageArgs, cause );
   }

   /**
    * Creates an exception that indicates cataloging for some type of
    * security object failed.
    *
    * @param detailMsg The text describing the problem.
    */
   public PSSecurityException( String detailMsg )
   {
      super(IPSSecurityErrors.METADATA_UNAVAILABLE,
            new Object[] { detailMsg });
   }
}

