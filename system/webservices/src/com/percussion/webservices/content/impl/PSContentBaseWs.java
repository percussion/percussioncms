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
package com.percussion.webservices.content.impl;

import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSWebserviceErrors;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Common implementations used with the public and private content 
 * webservices.
 */
public class PSContentBaseWs
{

   @Autowired
   private SessionFactory sessionFactory;

   public SessionFactory getSessionFactory() {
      return sessionFactory;
   }

   public void setSessionFactory(SessionFactory sessionFactory) {
      this.sessionFactory = sessionFactory;
   }

   /**
    * Wraps the specified exception into a {@link PSErrorException} and 
    * rethrow the wrapped exception.
    * 
    * @param operation the description of the attempted operation, 
    *    not <code>null</code> or empty.
    * @param e the unexpected exception, not <code>null</code>.
    * @throws PSErrorException the rethrowed exception.
    */
   protected void throwOperationError(String operation, Exception e)
      throws PSErrorException
   {
      if (StringUtils.isBlank(operation))
         throw new IllegalArgumentException("operation cannot be null or empty");

      if (e == null)
         throw new IllegalArgumentException("e cannot be null");

      int code = IPSWebserviceErrors.OPERATION_FAILED_ERROR;
      throw new PSErrorException(code, PSWebserviceErrors.createErrorMessage(
         code, operation, e.getLocalizedMessage()), ExceptionUtils
         .getFullStackTrace(e));
   }

   /**
    * Wraps the specified exception into a {@link PSErrorException} and 
    * rethrows the wrapped exception.
    * 
    * @param e the unexpected error, not <code>null</code>.
    * @throws PSErrorException the rethrowed exception.
    */
   protected void throwUnexpectedError(Exception e) throws PSErrorException
   {
      if (e == null)
         throw new IllegalArgumentException("e cannot be null");

      int code = IPSWebserviceErrors.UNEXPECTED_ERROR;
      throw new PSErrorException(code, PSWebserviceErrors.createErrorMessage(
         code, e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));

   }
}
