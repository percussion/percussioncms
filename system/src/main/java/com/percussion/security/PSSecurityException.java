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

