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

package com.percussion.server;

import com.percussion.conn.IPSConnection;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.error.PSApplicationAuthorizationError;
import com.percussion.error.PSErrorHandler;
import com.percussion.error.PSErrorManager;
import com.percussion.error.PSFatalError;
import com.percussion.error.PSInternalError;
import com.percussion.error.PSRequestHandlerNotFoundError;
import com.percussion.error.PSRequestPreProcessingError;
import com.percussion.error.PSRequestWaitTooLongError;
import com.percussion.error.PSValidationError;
import com.percussion.log.PSLogError;
import com.percussion.log.PSLogHandler;
import com.percussion.log.PSLogInformation;
import com.percussion.log.PSLogSubMessage;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The PSServerLogHandler class provides logging utilities for use within
 * the server core. It mainly provides convenience routines for common
 * type of logging.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSServerLogHandler {
   static Log ms_logger = LogFactory.getLog(PSServerLogHandler.class);
   
   /**
    * Construction of this class is not permitted.
    */
   private PSServerLogHandler()
   {
      super();
   }

   /**
    * Log an exception caught during request pre-processing.
    *
    * @param   conn            the requestor's connection
    *
    * @param   error            the exception that was thrown
    */
   public static void handlePreProcessingError(   IPSConnection conn,
                                                Exception error)
   {
      if (error instanceof PSRequestParsingException) {
         InetAddress host = null;
         try {
            if (conn != null)
               host = conn.getHost();
         } catch (Exception e) { /* no big deal */ }

         reportError(conn, new PSRequestPreProcessingError(
                                    host, (PSRequestParsingException)error));
      }
      else {
         Object[] args = { error.getMessage() };
         handlePreProcessingError(conn, IPSServerErrors.RAW_DUMP, args);
      }
   }

   /**
    * Log an error encountered during request pre-processing.
    * <p>
    * The error string is formatted by loading the string
    * associated with the error code and passing it the array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param   conn            the requestor connection
    *
    * @param   errorCode      the associated error code
    *
    * @param   args            the array of arguments to use as the arguments
    *                                                                                 in the error message
    */
   public static void handlePreProcessingError(   IPSConnection conn,
                                                int errorCode,
                                                Object[] args)
   {
      InetAddress host = null;
      try {
         if (conn != null)
            host = conn.getHost();
      } catch (Exception e) { /* no big deal */ }

      reportError(conn, new PSRequestPreProcessingError(
                                          host, errorCode, args));
   }

  /**
   * Handle the user request removal from the queue list due to the expiration of
   * request waiting time
   *
   * @param conn     the requestor's connection
   */
  public static void handleRequestWaitingTimeExpired( IPSConnection conn )
  {
    int applId = 0;
    String sessionId = "";
    int size = 0;

    reportError(conn, new PSRequestWaitTooLongError(applId, sessionId, size));
  }

  /**
   * Handle error occurance when trying to check a user's access level
   */
  public static void handleAccessError(PSRequest request, int applId, String ipAddress,
                                 String loginId, int errorCode, String errorString)
  {
      reportError(request,
         new PSApplicationAuthorizationError(
            applId, ipAddress, loginId, errorCode, errorString));
  }
  
   /**
    * Handle a terminal exception, which will cause server shut-down.
    * <P><EM>Note:</EM> A call to this method will never return.
    * <P>
    * The error string is formatted by loading the string
    * associated with the error code and passing it the array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param errorCode the associated error code
    *
    * @param args the array of arguments to use as the arguments
    * in the error message. May be <code>null</code>.
    */
   public static void handleTerminalError(int errorCode, Object[] args)
   {
      PSFatalError err = new PSFatalError(errorCode, args);

      /* log it to the screen so we definitely have some record of this */
      logToScreen(err);

      /* log the condition causing the shutdown */
      reportError(null, null, null, err);

      /* and shut the server down */
      PSServer.scheduleShutdown(0);
   }

   /**
    * Log an error encountered during request pre-processing.
    * <p>
    * The error string is formatted by loading the string
    * associated with the error code and passing it the array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param   conn            the requestor's connection
    */
   public static void handleServerShuttingDown(IPSConnection conn)
   {
      try {
         if (conn != null) {
            /* fail the request as server being too busy */
            PSResponse resp = new PSResponse(null);

            resp.setStatus(IPSHttpErrors.HTTP_SERVICE_UNAVAILABLE,
                           PSErrorManager.getErrorText(
                              IPSServerErrors.SERVER_SHUTDOWN_MSG));
         }
      } catch (Exception e) { /* not much we can do here */ }
   }

   /**
    * The request handler could not be found for the specified request.
    *
    * @param   req         the request object
    */
   public static void handleRequestHandlerNotFound(PSRequest req)
   {
      // log the user activity to tie back the error
      logUserActivityForError(req);

      Object[] params = { "", req.getRequestFileURL() };

      reportError(req,
                  new PSRequestHandlerNotFoundError(
                  PSLogInformation.NULL_APPLID,
                  IPSServerErrors.REQUEST_HANDLER_NOT_FOUND,
                  params));
   }

   /**
    * The data set could not be found for the specified request.
    *
    * @param   req         the request object
    *
    * @param   applId      the id of the application
    *
    * @param   applName      the name of the application
    */
   public static void handleDataSetNotFound(   PSRequest req,
                                             int applId,
                                             java.lang.String applName)
   {
      // log the user activity to tie back the error
      logUserActivityForError(req);

      String sessId = "";
      if (req.getUserSession() != null)
      sessId = req.getUserSessionId();

      Object[] params = { sessId, req.getRequestFileURL(), applName };

      reportError(req,
                  new PSRequestHandlerNotFoundError(
                                       applId,
                                       IPSServerErrors.APP_DATASET_NOT_FOUND,
                                       params));
   }

   /**
    * The data set request handler could not be found for the
    * specified request.
    *
    * @param   req         the request object
    *
    * @param   applId      the id of the application
    *
    * @param   applName      the name of the application
    *
    * @param   dataSetName   the name of the data set
    *
    * @param   requestType   the type of request
    */
   public static void handleDataSetHandlerNotFound(PSRequest req,
                                                   int applId,
                                                   java.lang.String applName,
                                                   java.lang.String dataSetName,
                                                   java.lang.String requestType)
   {
      // log the user activity to tie back the error
      logUserActivityForError(req);

      String sessId = "";
      if (req.getUserSession() != null)
      sessId = req.getUserSessionId();

      Object[] params = { sessId, applName, dataSetName, requestType };

      reportError(req,
                  new PSRequestHandlerNotFoundError(
                              applId,
                              IPSServerErrors.APP_DATASET_HANDLER_NOT_FOUND,
                              params));
   }

   /**
    * Report an application validation error.
    *
    * @param   applId      the id of the application
    *
    * @param   e            the validation exception to report
    */
   public static void handleValidationError(
      int applId, PSSystemValidationException e)
   {
      org.w3c.dom.Element xmlData = null;
      org.w3c.dom.Document doc;

      if (e.getSourceComponent() != null) {
         doc = PSXmlDocumentBuilder.createXmlDocument();
         xmlData = e.getSourceComponent().toXml(doc);
      }
      else if (e.getSourceContainer() != null) {
         doc = e.getSourceContainer().toXml();
         xmlData = doc.getDocumentElement();
      }

      PSValidationError err = new PSValidationError(
         applId, "", e.getErrorCode(), e.getErrorArguments(), xmlData);

      reportError(null, null, null, err);
   }

   /**
    * Log the specified message using the server's log handler. If
    * logging of the specified action is disabled, it is not performed.
    *
    * @param   msg      the message to log
    */
   public static void logMessage(PSLogInformation msg)
   {
      /*
      PSLogHandler lh = PSServer.getLogHandler();
      if (lh != null)
         lh.write(msg);
      else
         logToScreen(msg);
         */
      String message = "appid: " + msg.getApplicationId()
         + " type: " + msg.getMessageType() 
         + " time: " + msg.getMessageTime()
         + " msg: " + msg.getSubMessageText();
      ms_logger.info(message);
   }

   /**
    * Log the specified exception message using the server's log handler.
    * The log message includes a full stack trace and a localized message.
    * If logging of the specified action is disabled, it is not performed.
    *
    * @param message   the message to log before the stack trace, may be
    * <code>null</code> or <code>empty</code>.
    * @param t an exception object, never <code>null</code>.
    *
    * @return log error with the full message logged, never <code>null</code>.
    */
   public static PSLogError logException(String message, Throwable t)
   {
      if (t == null)
         throw new IllegalArgumentException("Throwable must not be null");

      StringWriter callStack = new StringWriter();
      PrintWriter p = new PrintWriter(callStack);
      t.printStackTrace(p);

      if (message==null || message.trim().length()<=0)
         message = "" + t.getLocalizedMessage();

      Object[] args = { message, callStack.toString() };

      PSLogInformation logInfo =
         new PSInternalError(IPSServerErrors.UNEXPECTED_EXCEPTION_LOG, args);

      logMessage(logInfo);

      return (PSLogError)logInfo;
   }


   /**
    * Report an error which does not have a request or response object
    * associated with it.
    *
    * @param   conn         the connection which encountered the error
    *
    * @param   error         the PSLogError subclass describing the error
    */
   private static void reportError(   IPSConnection conn,
                                    PSLogError error)
   {
      PSResponse resp = null;

      try {
         if (conn != null) {
            resp = new PSResponse(null);
            // PSRequest.discard(conn);
         }
      } catch (Exception e) { /* not much we can do here */ }

      reportError(null, null, resp, error);
   }

   /**
    * Report an error which does not have a request or response object
    * associated with it.
    *
    * @param   req         the request which encountered the error
    *
    * @param   error         the PSLogError subclass describing the error
    */
   private static void reportError(   PSRequest req,
                                    PSLogError error)
   {
      /* We are now grabbing the error/log handlers from the request
       * to fix bug id's TGIS-4BL4CZ and TGIS-4BL44N
       */
      PSErrorHandler errorHandler = req.getErrorHandler();
      PSLogHandler logHandler = req.getLogHandler();

      // Call PSRequest.getResponse with inError = true.  This suppresses
      // some actions that the default PSRequest.getResponse() would perform
      // on non-error responses.  In particular, it ensures that it doesn't
      // create a new psessionid cookie if none could be found in the request.
      PSResponse response = req.getResponse(true);

      reportError(
         errorHandler, logHandler, response, error);
   }

   /**
    * Report an error which does not have a request or response object
    * associated with it.
    *
    * @param   eh            the error handler to report through
    *                                                                              (or null to use the servers)
    *
    * @param   lh            the log handler to report through
    *                                                                              (or null to use the servers)
    *
    * @param   resp         the response object to report the error to
    *
    * @param   error         the PSLogError subclass describing the error
    */
   private static void reportError(   PSErrorHandler eh,
                                    PSLogHandler lh,
                                    PSResponse resp,
                                    PSLogError error)
   {
      /* We are now accepting the error/log handler to use as input
       * to fix bug id's TGIS-4BL4CZ and TGIS-4BL44N
       */

      if (eh == null)
         eh = PSServer.getErrorHandler();
      if (eh != null) {
         eh.reportError(resp, error);
      }
      else {
         if (lh == null)
            lh = PSServer.getLogHandler();
         if (lh != null)
            lh.write(error);
      }
   }

   /**
    * Log the specified error's sub-messages to the screen.
    *
    * @param   err      the error to log
    */
   private static void logToScreen(PSLogInformation err)
   {
      PSLogSubMessage[] msgs = err.getSubMessages();
      if (msgs != null) {
         for (int i = 0; i < msgs.length; i++)
            PSConsole.printMsg("Server", msgs[i].getText(), null);
      }
   }


   private static void logUserActivityForError(PSRequest req)
   {
      /* need to log basic/detailed user activity info
       * so we can trace back to the user that was denied
       */
      PSLogHandler lh = PSServer.getLogHandler();
      if (lh != null) {
         lh.logBasicUserActivity(req);
         lh.logDetailedUserActivity(req);
      }
   }
}

