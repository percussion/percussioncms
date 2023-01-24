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

package com.percussion.error;

import com.percussion.log.PSLogError;


/**
 * The PSBackEndError class is the base class for all back-end (database)
 * error classes.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public abstract class PSBackEndError extends PSLogError
{
   /**
    * Report an error encountered against the specified
    * back-end (database).
    * <p>
    * The application id is most commonly obtained by calling
    * {@link com.percussion.data.PSExecutionData#getId PSExecutionData.getId()} or
    * {@link com.percussion.server.PSApplicationHandler#getId PSApplicationHandler.getId()}.
    * <p>
    * The session id can be obtained from the
    * {@link com.percussion.server.PSUserSession PSUserSession} object
    * contained in the
    * {@link com.percussion.server.PSRequest PSRequest} object.
    *
    * @param      applId      the id of the application that generated
    *                           the error
    *
    * @param      errorCode   the error code reported by the back-end
    *                           (database)
    *
    * @param      errorParams   if the error string associated with the
    *                           error code specifies parameters, this is
    *                           an array of values to use to fill the string
    *                           appropriately. Be sure to include the
    *                           correct arguments in their correct
    *                           positions!
    */
   protected PSBackEndError(
      int applId, int errorCode, Object[] errorParams)
   {
      super(applId);
      m_errorCode = errorCode;
      m_errorArgs = errorParams;
   }

   /**
    * Allows the user to determine the underlying exception type.
    *
    * @return The error code passed to the ctor of this object.
    */
   public int getErrorCode()
   {
      return m_errorCode;
   }

   protected int         m_errorCode;
   protected Object[]   m_errorArgs;
}

