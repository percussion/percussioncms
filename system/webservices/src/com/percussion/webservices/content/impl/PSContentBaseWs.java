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
package com.percussion.webservices.content.impl;

import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSWebserviceErrors;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Common implementations used with the public and private content 
 * webservices.
 */
@Transactional
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
