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

import com.percussion.design.catalog.IPSCatalogErrors;
import com.percussion.log.PSLogError;
import com.percussion.log.PSLogSubMessage;

import java.util.Locale;


/**
 * The PSCatalogRequestError class is used to report an error
 * encountered during a catalog request.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSCatalogRequestError extends PSLogError
{
   /**
    * Report an error encountered during a catalog request.
    * <p>
    * The session id can be obtained from the
    * {@link com.percussion.server.PSUserSession PSUserSession} object
    * contained in the
    * {@link com.percussion.server.PSRequest PSRequest} object.
    *
    * @param      sessionId      the session id of the user making the
    *                            request
    *
    * @param      reqCategory      the catalog request category
    *
    * @param      reqType         the catalog request type
    *
    * @param      errorCode      the error code describing the type of error
    *
    * @param      errorParams    if the error string associated with the
    *                            error code specifies parameters, this is
    *                            an array of values to use to fill the string
    *                            appropriately. Be sure to include the
    *                            correct arguments in their correct
    *                            positions!
    */
   public PSCatalogRequestError(
      String sessionId, String reqCategory, String reqType,
      int errorCode, Object[] errorParams)
   {
      super(0);
      
      m_sessId = (sessionId == null) ? "" : sessionId;
      m_reqCategory = (reqCategory == null) ? "" : reqCategory;
      m_reqType = (reqType == null) ? "" : reqType;
      m_errorCode = errorCode;
      m_errorArgs = errorParams;
   }

   /**
    * sublcasses must override this to build the messages in the
    * specified locale
    */
   protected PSLogSubMessage[] buildSubMessages(Locale loc)
   {
      PSLogSubMessage[] msgs = new PSLogSubMessage[2];

      /* use IPSCatalogErrors.CATALOG_ERROR along with:
       *    [0] = m_sessId
       *    [1] = m_reqCategory
       *    [2] = m_reqType
       * to format the first submessage
       */
      msgs[0]   = new PSLogSubMessage(
         IPSCatalogErrors.CATALOG_ERROR,
         PSErrorManager.createMessage(
            IPSCatalogErrors.CATALOG_ERROR,
            new Object[] { m_sessId, m_reqCategory, m_reqType },
            loc));

      /* use m_errorCode/m_errorArgs to format the second submessage */
      msgs[1]   = new PSLogSubMessage(
         m_errorCode,
         PSErrorManager.createMessage(m_errorCode, m_errorArgs, loc));

      return msgs;
   }


   private String      m_sessId;
   private String      m_reqCategory;
   private String      m_reqType;
   private int         m_errorCode;
   private Object[]   m_errorArgs;
}

