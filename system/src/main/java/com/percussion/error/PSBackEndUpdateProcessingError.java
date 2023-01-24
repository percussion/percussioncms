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

import com.percussion.data.IPSDataErrors;
import com.percussion.log.PSLogSubMessage;
import com.percussion.server.IPSServerErrors;
import org.w3c.dom.Element;

import java.util.Locale;


/**
 * The PSBackEndUpdateProcessingError class is used to report an error
 * encountered during back-end (database) update processing.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSBackEndUpdateProcessingError extends PSBackEndError
{
   /**
    * Report an error encountered during
    * back-end (database) update processing using the native error
    * information and the UPDATE statement.
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
    * @param      errorCode      the error code reported by the back-end
    *                            (database)
    *
    * @param      errorString    the error text reported by the back-end
    *                            (database)
    *
    * @param      updateString   the UPDATE statement which caused the
    *                            error
    */
   public PSBackEndUpdateProcessingError(   int applId,
                                          java.lang.String sessionId,
                                          int errorCode,
                                          java.lang.String errorString,
                                          java.lang.String updateString)
   {
      this(   applId,
            sessionId,
            IPSServerErrors.NATIVE_ERROR,
            new Object[] { new Integer(errorCode),
                           ((errorString == null) ? "" : errorString) },
            updateString,
            null);   // xml tree
   }

   /**
    * Report an error encountered during
    * back-end (database) update processing using the internal error
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
    * @param      source         the sub-tree containing the element(s)
    *                            causing the error (may be null)
    */
   public PSBackEndUpdateProcessingError(   int applId,
                                          java.lang.String sessionId,
                                          int errorCode,
                                          Object[] errorParams,
                                          Element source)
   {
      this(   applId, sessionId, errorCode, errorParams,
            null,   // sql statement
            source);
   }
   
   /**
    * Report an error encountered during
    * back-end (database) update processing using the native error
    * information and the UPDATE statement.
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
    * @param      updateString   the UPDATE statement which caused the
    *                            error
    */
   public PSBackEndUpdateProcessingError(int applId,
                                          java.lang.String sessionId,
                                          int errorCode,
                                          Object[] errorParams,
                                          java.lang.String source)
   {
      this( applId, sessionId, errorCode, errorParams, source,
            null);   // xml tree
   }

   /**
    * Report an error encountered during
    * back-end (database) update processing using the native error
    * information and the UPDATE statement.
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
    * @param      sourceSql   the UPDATE statement which caused the
    *                         error
    *
    * @param      sourceTree  the sub-tree containing the element(s)
    *                         causing the error (may be null)
    */
   public PSBackEndUpdateProcessingError( int applId,
                                          java.lang.String sessionId,
                                          int errorCode,
                                          Object[] errorParams,
                                          java.lang.String sourceSql,
                                          Element sourceTree)
   {
      super(applId, errorCode, errorParams);

      if (sessionId == null)
         m_sessId = "";
      else
         m_sessId = sessionId;

      if (sourceSql == null)
         m_sourceSql = "";
      else
         m_sourceSql = sourceSql;

      m_sourceTree = sourceTree;
   }

   /**
    * Get the next update error in the chain.
    */
   public PSBackEndUpdateProcessingError getNext()
   {
      return m_next;
   }

   /**
    * Appends an update error to the chain (on the end).
    */
   public void setNext(PSBackEndUpdateProcessingError err)
   {
      PSBackEndUpdateProcessingError e;
      for (e = this; e.m_next != null; e = e.m_next);
      e.m_next = err;
   }

   /**
    * sublcasses must override this to build the messages in the
    * specified locale
    */
   protected PSLogSubMessage[] buildSubMessages(Locale loc)
   {
      PSBackEndUpdateProcessingError err;
      int errorCount = 0;
      for (err = this; err != null; err = err.m_next)
         errorCount += 3;

      PSLogSubMessage[] msgs = new PSLogSubMessage[errorCount];

      // and all chained error blocks
      errorCount = 0;
      for (err = this; err != null; err = err.m_next)
      {
         /* use IPSDataErrors.UPDATE_PROCESSING_ERROR along with:
           *    [0] = sessionId
           * to format the first submessage
           */
         msgs[errorCount++] = new PSLogSubMessage(
            IPSDataErrors.UPDATE_PROCESSING_ERROR,
            PSErrorManager.createMessage(
               IPSDataErrors.UPDATE_PROCESSING_ERROR,
               new Object[] { m_sessId },
               loc));

         /* use the errorCode/errorParams to format the second submessage
           */
         msgs[errorCount++] = new PSLogSubMessage(
            m_errorCode, 
            PSErrorManager.createMessage(
               m_errorCode, m_errorArgs, loc));

         /* use IPSServerErrors.RAW_DUMP along with source
           * to format the third submessage
           */
         msgs[errorCount++] = new PSLogSubMessage(
            IPSServerErrors.RAW_DUMP,
            PSErrorManager.createMessage(
               IPSServerErrors.RAW_DUMP,
               new Object[] { ((m_sourceSql == null) ? "" : m_sourceSql) },
               loc));
      }

      return msgs;
   }


   private String                              m_sessId;
   private String                              m_sourceSql;
   private Element                           m_sourceTree;
   private PSBackEndUpdateProcessingError      m_next = null;
}

