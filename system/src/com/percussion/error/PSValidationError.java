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

package com.percussion.error;

import com.percussion.log.PSLogError;
import com.percussion.log.PSLogSubMessage;
import com.percussion.server.IPSServerErrors;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.Locale;

import org.w3c.dom.Element;


/**
 * The PSValidationError class is used to report a calidation
 * error. Validation errors usually occur when an a user submits data
 * which does not meet the validation requirements defined in the 
 * application's data set.
 * <p>
 * <p>
 * An error message containing the user's session id and the text of the
 * message is logged when this error is encountered. If
 * detailed user activity logging is also enabled, the request can be
 * tracked back to see all the data associated with the request.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSValidationError extends PSLogError {
   
   /**
    * Report an application validation error.
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
    * @param      applId         the id of the application that generated
    *                            the error
    *
    * @param      sessionId      the session id of the user making the
    *                            request
    *
    * @param      errorCode      the error code describing the type of error
    *
    * @param      errorParams    if the error string associated with the
    *                            error code specifies parameters, this is
    *                            an array of values to use to fill the string
    *                            appropriately. Be sure to include the
    *                            correct arguments in their correct
    *                            positions!
    *
    * @param      source         the XML representation of the object
    *                              in error
    */
   public PSValidationError(  int applId,
                              java.lang.String sessionId,
                              int errorCode,
                              Object[] errorParams,
                              Element source)
   {
      super(applId);

      if (sessionId == null)
         m_sessId = "";
      else
         m_sessId = sessionId;

      m_errorCode = errorCode;
      m_errorArgs = errorParams;

      if (source == null)
         m_source = "";
      else
         m_source = PSXmlDocumentBuilder.toString(source);
   }

   /**
    * sublcasses must override this to build the messages in the
    * specified locale
    */
   protected PSLogSubMessage[] buildSubMessages(Locale loc)
   {
      int msgCount = (m_source.length() > 0) ? 3 : 2;
      PSLogSubMessage[] msgs = new PSLogSubMessage[msgCount];

      /* the generic submessage first (contains session id) */
      msgs[0]   = new PSLogSubMessage(
                                 IPSServerErrors.VALIDATION_ERROR,
                                 PSErrorManager.createMessage(
                                       IPSServerErrors.VALIDATION_ERROR,
                                       new Object[] { m_sessId },
                                       loc));

      /* the submessage containing m_errorCode/m_errorArgs */
      msgs[1]   = new PSLogSubMessage(
                                 m_errorCode,
                                 PSErrorManager.createMessage(   m_errorCode,
                                                               m_errorArgs,
                                                               loc));

      if (msgCount == 3)   /* write the source data */
         msgs[2]   = new PSLogSubMessage(
                                 IPSServerErrors.RAW_DUMP,
                                 PSErrorManager.createMessage(
                                       IPSServerErrors.RAW_DUMP,
                                       new Object[] { m_source },
                                       loc));

      return msgs;
   }


   private String      m_sessId;
   private int         m_errorCode;
   private Object[]   m_errorArgs;
   private String      m_source;
}

