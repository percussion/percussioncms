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

package com.percussion.error;

import com.percussion.data.IPSDataErrors;
import com.percussion.log.PSLogSubMessage;
import com.percussion.server.IPSServerErrors;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Element;

import java.util.Locale;


/**
 * The PSBackEndQueryProcessingError class is used to report an error
 * encountered during back-end (database) query processing.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSBackEndQueryProcessingError extends PSBackEndError
{
   /**
    * Report an error encountered during
    * back-end (database) query processing using the native error
    * information and the SELECT statement.
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
    * @param      sessionId   the session id of the user making the
    *                           request
    *
    * @param      errorCode   the error code reported by the back-end
    *                           (database)
    *
    * @param      errorString   the error text reported by the back-end
    *                           (database)
    *
    * @param      queryString   the SELECT statement which caused the
    *                           error
    */
   public PSBackEndQueryProcessingError(   int applId,
                                          java.lang.String sessionId,
                                          int errorCode,
                                          java.lang.String errorString,
                                          java.lang.String queryString)
   {
      this(   applId,
            sessionId,
            IPSServerErrors.NATIVE_ERROR,
            new Object[] { new Integer(errorCode),
                           ((errorString == null) ? "" : errorString) },
            queryString);
   }
   
   /**
    * Report an error encountered during
    * back-end (database) query processing using the internal error
    * information. This usually occurs when E2 encounters an error
    * pre-processing the request.
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
    * @param      sessionId   the session id of the user making the
    *                           request
    *
    * @param      errorCode   the error code describing the type of error
    *
    * @param      errorParams   if the error string associated with the
    *                           error code specifies parameters, this is
    *                           an array of values to use to fill the string
    *                           appropriately. Be sure to include the
    *                           correct arguments in their correct
    *                           positions!
    *
    * @param      source      the sub-tree containing the element(s)
    *                           causing the error (may be null)
    */
   public PSBackEndQueryProcessingError(   int applId,
                                          java.lang.String sessionId,
                                          int errorCode,
                                          Object[] errorParams,
                                          Element source)
   {
      this(   applId, sessionId, errorCode, errorParams,
            PSXmlDocumentBuilder.toString(source));
   }

   /**
    * Report an error encountered during
    * back-end (database) query processing using the specified error
    * information and the SELECT statement.
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
    * @param      sessionId   the session id of the user making the
    *                           request
    *
    * @param      errorCode   the error code describing the type of error
    *
    * @param      errorParams   if the error string associated with the
    *                           error code specifies parameters, this is
    *                           an array of values to use to fill the string
    *                           appropriately. Be sure to include the
    *                           correct arguments in their correct
    *                           positions!
    *
    * @param      queryString   the SELECT statement which caused the
    *                           error
    */
   public PSBackEndQueryProcessingError(   int applId,
                                          java.lang.String sessionId,
                                          int errorCode,
                                          Object[] errorParams,
                                          java.lang.String source)
   {
      super(applId, errorCode, errorParams);

      if (sessionId == null)
         m_sessId = "";
      else
         m_sessId = sessionId;

      if (source == null)
         m_source = "";
      else
         m_source = source;
   }

   /**
    * sublcasses must override this to build the messages in the
    * specified locale
    */
   protected PSLogSubMessage[] buildSubMessages(Locale loc)
   {
      PSLogSubMessage[] msgs = new PSLogSubMessage[3];

      /* use IPSDataErrors.QUERY_PROCESSING_ERROR along with:
       *    [0] = m_sessId
       * to format the first submessage
       */
      msgs[0] = new PSLogSubMessage(
         IPSDataErrors.QUERY_PROCESSING_ERROR,
         PSErrorManager.createMessage(
            IPSDataErrors.QUERY_PROCESSING_ERROR,
            new Object[] { m_sessId },
            loc));

      /* use the errorCode/errorParams to format the second submessage
       */
      msgs[1] = new PSLogSubMessage(
         m_errorCode, 
         PSErrorManager.createMessage(
            m_errorCode, m_errorArgs, loc));

      /* use IPSServerErrors.RAW_DUMP along with source
       * to format the third submessage
       */
      msgs[2] = new PSLogSubMessage(
         IPSServerErrors.RAW_DUMP,
         PSErrorManager.createMessage(
            IPSServerErrors.RAW_DUMP, new Object[] { m_source }, loc));

      return msgs;
   }


   private String      m_sessId;
   private String      m_source;
}

