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

import com.percussion.content.IPSMimeContentTypes;
import com.percussion.data.PSConversionException;
import com.percussion.data.PSStyleSheetMerger;
import com.percussion.design.objectstore.PSCustomError;
import com.percussion.design.objectstore.PSErrorWebPages;
import com.percussion.design.objectstore.PSLogger;
import com.percussion.design.objectstore.PSNotifier;
import com.percussion.design.objectstore.PSRecipient;
import com.percussion.log.PSLogInformation;
import com.percussion.log.PSLogError;
import com.percussion.mail.PSMailSendException;
import com.percussion.mail.PSSmtpMailProvider;
import com.percussion.server.IPSHttpErrors;
import com.percussion.server.PSConsole;
import com.percussion.server.PSResponse;
import com.percussion.util.PSCharSets;
import com.percussion.util.PSMapClassToObject;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The PSErrorHandler class defines the error handling rules (customizations)
 * for a particular server or application. The server and each
 * application may follow different error handling rules. As such, each
 * has its own PSErrorHandler object. This object is used to determine
 * what page should be returned when a particular error is encountered.
 *
 * @see         PSErrorManager
 * @see         com.percussion.design.objectstore.PSErrorWebPages
 * @see         com.percussion.design.objectstore.PSCustomError
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSErrorHandler {

   /**
    * Construct an error handler with the specified customizations. For each
    * error a custom page has not been defined, the default error page
    * defined in the
    * {@link com.percussion.error.PSErrorManager PSErrorManager}
    * will be used. Use <code>null</code> if no custom error pages are being
    * defined.
    *
    * @param   pages               the custom error pages to use.
    *
    * @param   returnHtmlErrors   if <code>true</code>, the error response
    *                              text is returned as HTML; otherwise,
    *                              XML is used
    *
    * @param   notify            the notification settings.
    *
    * @param   rules               defines how to log the errors
    *
    * @param   rules   the rules that determine when/if errors are to be written
    *   to the log. if it is <CODE>null</CODE>, then logging will be enabled for all
    *   errors.
    */
   public PSErrorHandler(PSMapClassToObject pages,
      boolean returnHtmlErrors,
      PSNotifier notify,
      PSLogger rules)
   {
      super();

      m_logHandler = new com.percussion.log.PSLogHandler(rules);
      m_pages = pages;
      m_notify = notify;
      m_returnHtmlErrors = returnHtmlErrors;
   }

   /**
    * A convenience constructor that calls the
    * {@link #PSErrorHandler(PSMapClassToObject,boolean,PSNotifier,PSLogger) constructor}
    * with a <CODE>null</CODE> PSLogger, enabling logging for all errors.
    */
   public PSErrorHandler(PSMapClassToObject pages,
      boolean returnHtmlErrors,
      PSNotifier notify)
      throws IllegalArgumentException
   {
      this(pages, returnHtmlErrors, notify, null);
   }

   /**
    * A convenience constructor that calls the
    * {@link #PSErrorHandler(PSMapClassToObject,boolean,PSNotifier,PSLogger) constructor}
    * after converting the PSErrorWebPages object to a map
    */
   public PSErrorHandler(
      PSErrorWebPages pages, PSNotifier notify, PSLogger rules)
      throws IllegalArgumentException
   {
      this(createPageMapFromErrorPages(pages),
         ((pages == null) ? false : pages.isHtmlReturned()),
         notify, rules);
   }

   /**
    * A convenience constructor that calls the
    * {@link #PSErrorHandler(PSErrorWebPages,PSNotifier,PSLogger) constructor}
    * with a <CODE>null</CODE> logger, after converting the PSErrorWebPages object
    * to a map
    */
   public PSErrorHandler(PSErrorWebPages pages, PSNotifier notify)
      throws IllegalArgumentException
   {
      this(pages, notify, null);
   }

   /**
    * Report an error.
    * <p>
    * This is done by constructing the appropriate error object and
    * passing it into this method. The following steps will occur with
    * the error:
    * <ol>
    * <li>the error will be logged (if the logging rules are enabled for
    * this error)</li>
    * <li>notification will be sent (if notification for this error
    *      is enabled)</li>
    * <li>the appropriate error output will be generated</li>
    * </ol>
    *
    * @param   response      the response object to use to report the error
    *                        to the user
    *
    * @param   error         the error to be reported
    */
   public void reportError(PSResponse response, PSLogError err)
   {
      // log the message first (in case we hit more errors below)
      m_logHandler.write(err);

      // let the user (requestor) know what's going on
      notifyRequestor(response, err);

      // notify any administrators defined in the PSNotifier object
      notifyAdmins(err);
   }

   public static Document fillErrorResponse(Throwable t)
   {
      // create a blank doc
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();

      // and append the error (which will make it the root element)
      appendError(respDoc, respDoc, t);

      return respDoc;
   }

   public static Element appendError(
      Document respDoc, Node rootNode, Throwable t)
   {
      // can't use addEmptyElement, so use DOM directly
       Element root = respDoc.createElement("PSXError");
      rootNode.appendChild(root);

      PSXmlDocumentBuilder.addElement(
         respDoc, root, "message", t.getMessage());
      PSXmlDocumentBuilder.addElement(
         respDoc, root, "exceptionClass", t.getClass().getName());
      if (t instanceof PSException) {
         PSException e = (PSException)t;

         PSXmlDocumentBuilder.addElement(
            respDoc, root, "errorCode", String.valueOf(e.getErrorCode()));

         Element argsNode = PSXmlDocumentBuilder.addEmptyElement(
               respDoc, root, "errorArgs");

         Object[] args = e.getErrorArguments();
         int size = (args == null) ? 0 : args.length;
         for (int i = 0; i < size; i++) {
            String argText = (args[i] == null) ? "" : args[i].toString();
            PSXmlDocumentBuilder.addElement(respDoc, argsNode, "arg", argText);
         }
      }

      return root;
   }

   /**
    * When server is shut down, this method is called to send out all those pending
    * error notifications.
    */
   public void shutdown()
   {
      try
      {
         // just in case we're being shutdown twice
         if (m_notify != null)
         {
            // set up SMTP mail provider object
            Properties props = new Properties();
            props.put( PSSmtpMailProvider.PROPERTY_HOST, m_notify.getServer() );
            PSSmtpMailProvider smtp = new PSSmtpMailProvider( props );

            if ((m_queNotif != null) && (m_queNotif.getGeneralErrorCount() > 0))
            {
               smtp.send( m_queNotif );
               m_queNotif.setInitDate( new java.util.Date() );  // for test
               m_queNotif.setGeneralErrorCount( 0 );            // for test
               m_queNotif.resetBodyText();
            }
         }

         m_logHandler = null;
         m_notify = null;
         m_queNotif = null;
      } catch (PSIllegalArgumentException e1)
      {
         PSConsole.printMsg(
            "Caught PSIllegalArgumentException in PSErrorHandler/shutdown(): ",
            e1.getMessage() );
      } catch (IOException e2)
      {
         PSConsole.printMsg(
            "Caught IOException in class PSErrorHandler/shutdown(): ",
            e2.getMessage() );
      } catch (PSMailSendException e3)
      {
         PSConsole.printMsg(
            "Caught PSMailSendException in class PSErrorHandler/shutdown(): ",
            e3.getMessage() );
      }
   }

   /**
    * Notify the requestor what went wrong.
    *
    * @param   response      the response object to use to report the error
    *                        to the user
    *
    * @param   err         the error to be reported
    */
   void notifyRequestor(PSResponse response, PSLogError err) {
      if (response == null)   /* no response, nothing to do */
         return;

      Locale loc = response.getPreferredLocale();

      // get the URL associated with this error
      URL errorPageURL = null;

      if (m_pages != null) {
         errorPageURL = (URL)m_pages.getMapping(err.getClass());
      }

      if (errorPageURL == null) {
         // no custom mapping was found, so use the default mapping
         errorPageURL = PSErrorManager.getErrorURL(err, loc);
      }

      int statusCode = PSErrorHttpCodes.getHttpCode(err, loc);
      response.setStatus(statusCode);
      if (statusCode == IPSHttpErrors.HTTP_UNAUTHORIZED) {
         /* *TODO*
           *
           * We do not currently support realms, though we probably
           * should. When realm support is added, "Basic" should be converted
           * to "Basic realm=\"<realm-name>\""
           *
           * Additionally, we do not support Digest or NTLM authentication
           * mechanisms.
           */
         response.setResponseHeader(
            PSResponse.RHDR_WWW_AUTH, "Basic realm=\"\"");
      }

      InputStream input = null;
      try
      {
         PSStyleSheetMerger merger;
         if (errorPageURL == null)
         {
            // still nothing, log it and use the PSLogError message text
            m_logHandler.write(new PSApplicationDesignError(
               err.getApplicationId(), 0, new String[] {
                  "Could not find error response for : " + err.getClass().toString() },
               null));
            input = setBasicErrorResponseContent(response, loc, err);
         }
         else if (   // is this a style sheet to be merged with XML?
            ((merger = PSStyleSheetMerger.getMerger(errorPageURL)) != null) &&
             (err.getXmlErrorData(loc) != null) )
         {
            java.io.ByteArrayOutputStream bout =
               new java.io.ByteArrayOutputStream();
            if (m_returnHtmlErrors) {
               try {
                  merger.merge(null, err.getXmlErrorData(loc), bout, errorPageURL);
                  long length = bout.size();
                  input = new java.io.ByteArrayInputStream(bout.toByteArray());
                  response.setContent(
                     input, length, IPSMimeContentTypes.MIME_TYPE_TEXT_HTML);
               } catch (PSConversionException e) {
                  m_logHandler.write(new PSApplicationDesignError(
                     err.getApplicationId(), 0, new String[] {
                        "Specified style sheet may be invalid : " +
                        errorPageURL.toString() + " : " + e.toString() },
                     null));
                  input = setBasicErrorResponseContent(response, loc, err);
               }
            }
            else {
               response.setContent(err.getXmlErrorData(loc));
            }
         }
         else {
            // we have a URL for this error, so respond with the file contents
            try {
               // get information on the response file
               File file = new File(errorPageURL.getFile());
               long length = file.length();
               input = new FileInputStream(file);

               // Get MIME type for the content of the input stream
               // based on file extension. We'll assume HTML by default
               String mimeType = IPSMimeContentTypes.MIME_TYPE_TEXT_HTML;

               String fileExtension = null;
               if (file != null) {
                  String oneFileName = file.getName();
                  int fileNameLength = (oneFileName == null) ? 0 : oneFileName.length();
                  if (fileNameLength > 0) {
                     // bump the position by one since we need to anyway
                     // we must check for 0 as the error condition rather than -1
                     // and pos == fileNameLength as the upper bound rather than
                     // pos+1
                     int pos = oneFileName.lastIndexOf(".") + 1;
                       if ((pos != 0) && (pos != fileNameLength))
                          fileExtension = oneFileName.substring(pos);
                  }
               }

               if ((fileExtension != null) && (fileExtension.length() != 0)){
                  if (fileExtension.equalsIgnoreCase("xml"))
                     mimeType = IPSMimeContentTypes.MIME_TYPE_TEXT_XML;
                  else if (fileExtension.equalsIgnoreCase("html"))
                     mimeType = IPSMimeContentTypes.MIME_TYPE_TEXT_HTML;
                  else if (fileExtension.equalsIgnoreCase("htm"))
                     mimeType = IPSMimeContentTypes.MIME_TYPE_TEXT_HTML;
                  else if (fileExtension.equalsIgnoreCase("txt"))
                     mimeType = IPSMimeContentTypes.MIME_TYPE_TEXT_PLAIN;
               }

               response.setContent(input, length, mimeType);
            } catch (java.io.FileNotFoundException e) {
               m_logHandler.write(new PSApplicationDesignError(
                  err.getApplicationId(), 0, new String[] {
                     "Could not find custom error response for : " +
                     err.getClass().toString() + " : " + errorPageURL.toString() },
                  null));
               input = setBasicErrorResponseContent(response, loc, err);
            }
         }
      }
      catch (SocketException se)
      {
         // Address Rx-04-05-0089:
         // Ignore these as they simply raise concerns without cause
      }
      catch (IOException e)
      {
            /* Bug Id Rx-00-02-0017 Changing stack backtrace to 
               simply return the exception string  - make IO exception
               on response look less scary at the server */
            Object[] args = { null,
                     e.toString() };
            com.percussion.log.PSLogManager.write(
                  new com.percussion.log.PSLogServerWarning(
                  com.percussion.server.IPSServerErrors.RESPONSE_SEND_ERROR, args,
                  true, "PSErrorHandler"));
      }
      finally
      {
         try { if (input != null) input.close(); }
         catch (Exception e) { /* may already be closed, ignore this */ }
      }
   }

   private InputStream setBasicErrorResponseContent(
      PSResponse response, Locale loc, PSLogError err)
      throws IOException
   {
      final String stdEnc =  PSCharSets.rxStdEnc();
      final String javaEnc = PSCharSets.rxJavaEnc();

      byte[] ba = ("<HTML>" + err.toString(loc) + "</HTML>").getBytes(javaEnc);
      InputStream input = new java.io.ByteArrayInputStream(ba);
      response.setContent(
         input, ba.length,
         IPSMimeContentTypes.MIME_TYPE_TEXT_HTML + "; charset=" + stdEnc);
      return input;
   }

   /**
    * Notify any administrators defined in the PSNotifier object.
    *
    * @param   err         the error to be reported
    */
   void notifyAdmins(PSLogError err)
   {
      if (m_notify == null)
         return;

      String className = err.getClass().toString();
      int pos = 0;
      try{
         pos = className.lastIndexOf(".");
         if (pos != -1)
            className = className.substring(pos+1);
      } catch (NullPointerException e1) { /*cannot happen here*/
      } catch (IndexOutOfBoundsException e2){ /*cannot happen here*/
      }

      int classFlag = getClassFlagBasedUponClassName(className);
      boolean isSpecialCase = false;
      if (classFlag == -1)
         return;
      else if ((classFlag == ms_authorizationError) ||
               (classFlag == ms_backAuthorizationError) ||
               (classFlag == ms_requestQueueError) ||
               (classFlag == ms_responseTimeError)){
         isSpecialCase = true;
      }

      String from     = m_notify.getFrom();
      String subject  = "Error notify";
      String bodyText = err.toString();
      
      try {
         // set up SMTP mail provider object
         java.util.Properties props = new java.util.Properties();
         props.put(PSSmtpMailProvider.PROPERTY_HOST, m_notify.getServer());
         PSSmtpMailProvider smtp = new PSSmtpMailProvider(props);

         com.percussion.util.PSCollection collect = m_notify.getRecipients();
         int collectSize = (collect == null) ? 0 : collect.size();

         PSRecipient rec = null;
         boolean errorExist = false;

         for (int i = 0; i < collectSize; i++){
            rec = (PSRecipient)(collect.get(i));

            if (!rec.isSendEnabled())  // cannot send, seek next one to send
               continue;

            m_queNotif = (PSQueuedNotification)(m_queNotifHash.get(rec.getName()));
            if (m_queNotif == null){
               m_queNotif = new PSQueuedNotification();
               m_queNotif.setFrom(from);
               m_queNotif.setSubject(subject);
               m_queNotif.addSendTo(rec.getName());
            }

            if (isSpecialCase == false){
               m_queNotif.appendBodyText(bodyText);
            }
            
            switch(classFlag){
            case ms_authorizationError: // special case
               if (rec.isAppAuthorizationFailureEnabled()){
                  processAppAuthorizationFailure(rec, err, m_queNotif);
                  errorExist = true;
               }
               break;
            case ms_designError:
               if (rec.isAppDesignErrorEnabled()){
                  m_queNotif.addGeneralErrorCountByOne();
                  errorExist = true;
               }
               break;
            case ms_htmlProcessingError:
               if (rec.isAppHtmlErrorEnabled()){
                  m_queNotif.addGeneralErrorCountByOne();
                  errorExist = true;
               }
               break;
            case ms_requestQueueError: // special case
               int fixedAppQueueCount = rec.getAppRequestQueueMax();
               int qSize = ((PSLargeRequestQueueError)err).getRequestQueueSize();
               if (rec.isAppRequestQueueLargeEnabled()){
                  if (qSize >= fixedAppQueueCount){
                     m_queNotif.addGeneralErrorCountByOne();
                     m_queNotif.appendBodyText(bodyText);
                  }
                  errorExist = true;
               }
               break;
            case ms_largeRequestError: // in case we need it, GUI does not have this
               break;
            case ms_responseTimeError:  // special case
               int fixedAppRespTime = rec.getAppResponseTimeMax();
               int respTime = ((PSPoorResponseTimeError)err).getResponseTimeMS();
               if (rec.isAppResponseTimeEnabled()){
                  if (respTime > fixedAppRespTime){
                     m_queNotif.addGeneralErrorCountByOne();
                     m_queNotif.appendBodyText(bodyText);
                  }
                  errorExist = true;
               }
               break;
            case ms_validationError:
               if (rec.isAppValidationErrorEnabled()){
                  m_queNotif.addGeneralErrorCountByOne();
                  errorExist = true;
               }
               break;
            case ms_xmlProcessingError:
               if (rec.isAppXmlErrorEnabled()){
                  m_queNotif.addGeneralErrorCountByOne();
                  errorExist = true;
               }
               break;
            case ms_backAuthorizationError: // special case
               if (rec.isBackEndAuthorizationFailureEnabled()){
                  processBackEndAuthorizationFailure(rec, err, m_queNotif);
                  errorExist = true;
               }
               break;
            case ms_dataConversionError:
               if (rec.isBackEndDataConversionErrorEnabled()){
                  m_queNotif.addGeneralErrorCountByOne();
                  errorExist = true;
               }
               break;
            case ms_backQueryError:
               if (rec.isBackEndQueryFailureEnabled()){
                  m_queNotif.addGeneralErrorCountByOne();
                  errorExist = true;
               }
               break;
            case ms_backRequestQueueError: // should be special, not implemented
               if (rec.isBackEndRequestQueueLargeEnabled()){
                  m_queNotif.addGeneralErrorCountByOne();
                  errorExist = true;
               }
               break;
            case ms_backServerDownError:
               if (rec.isBackEndServerDownFailureEnabled()){
                  m_queNotif.addGeneralErrorCountByOne();
                  errorExist = true;
               }
               break;
            case ms_backUpdateError:
               if (rec.isBackEndUpdateFailureEnabled()){
                  m_queNotif.addGeneralErrorCountByOne();
                  errorExist = true;
               }
               break;
            }

            if (errorExist == false)
               continue;

            if (rec.isErrorThresholdByCount()){
               int generalErrorCount = m_queNotif.getGeneralErrorCount();
               if (generalErrorCount >= rec.getErrorThresholdCount()){
                  smtp.send(m_queNotif);
                  m_queNotif.setGeneralErrorCount(0);   // for test
                  m_queNotif.resetBodyText(); // remove the bodyText
               }
               else{
                  m_queNotifHash.put(rec.getName(), m_queNotif);
               }
            }
            else if (rec.isErrorThresholdByInterval()){
               long initialTimeMS = m_queNotif.getInitDate().getTime();
               java.util.Date now = new java.util.Date();
               long waitTime = now.getTime() - initialTimeMS;
               long normalTime = (rec.getErrorThresholdInterval())*60000;
               int  generalErrorCount = m_queNotif.getGeneralErrorCount();
               if ((waitTime >= normalTime) && (generalErrorCount > 0)){
                  smtp.send(m_queNotif);
                  m_queNotif.setInitDate(now);           // for test
                  m_queNotif.setGeneralErrorCount(0);    // for test
                  m_queNotif.resetBodyText(); // remove the bodyText
               }
               else{
                  m_queNotifHash.put(rec.getName(), m_queNotif);
               }
            }

            errorExist = false;
         }  // end of for loop

      } catch (PSIllegalArgumentException e1) {
         com.percussion.server.PSConsole.printMsg(
            "Caught PSIllegalArgumentException in PSErrorHandler/notifyAdmins: ",
            e1.getMessage());
      } catch (IOException e2) {
         com.percussion.server.PSConsole.printMsg(
            "Caught IOException in class PSErrorHandler/notifyAdmins: ",
            e2.getMessage());
      } catch (com.percussion.mail.PSMailSendException e3) {
         com.percussion.server.PSConsole.printMsg(
            "Caught PSMailSendException in class PSErrorHandler/notifyAdmins: ",
            e3.getMessage());
      }
   }

   /**
    * Process when application authorization failure happened.
    *
    * @param   rec      the recipient of the emails
    * @param   err      the authorization error as a PSLogError object
    * @param   queNotif   the object controls the sending of emails
    */
   private void processAppAuthorizationFailure(PSRecipient rec, PSLogError err,
                                                PSQueuedNotification queNotif)
   {
      int ipCount;
      String bodyText;

      int    fixedAuthFailCount = rec.getAppAuthorizationFailureCount();
      String ipAddress = ((PSApplicationAuthorizationError)err).getHost();

      Integer counter = (Integer)(m_appCountHash.get(ipAddress));
      if (counter != null){
         ipCount = counter.intValue(); // last time
         ipCount += 1;   // this time
         if (ipCount < fixedAuthFailCount)
            m_appCountHash.put(ipAddress, new java.lang.Integer(ipCount));
         else
            m_appCountHash.put(ipAddress, new java.lang.Integer(0));
      }
      else{
         ipCount = 1;
         m_appCountHash.put(ipAddress, new java.lang.Integer(ipCount));
      }

      if (ipCount >= fixedAuthFailCount){
         bodyText = err.toString();
         queNotif.addGeneralErrorCountByOne();
         queNotif.appendBodyText(bodyText);
      }
   }

   /**
    * Process when backend authorization failure happened.
    *
    * @param   rec      the recipient of the emails
    * @param   err      the authorization error as a PSLogError object
    * @param   queNotif   the object controls the sending of emails
    */
   private void processBackEndAuthorizationFailure(PSRecipient rec, PSLogError err,
                                                   PSQueuedNotification queNotif)
   {
      int ipCount;
      String bodyText;

      int    fixedAuthFailCount = rec.getBackEndAuthorizationFailureCount();
      String ipAddress = ((PSBackEndAuthorizationError)err).getHost();

      Integer counter = (Integer)(m_beCountHash.get(ipAddress));
      if (counter != null){
         ipCount = counter.intValue(); // last time
         ipCount += 1;   // this time
         if (ipCount < fixedAuthFailCount)
            m_beCountHash.put(ipAddress, new Integer(ipCount));
         else
            m_beCountHash.put(ipAddress, new Integer(0));
      }
      else{
         ipCount = 1;
         m_beCountHash.put(ipAddress, new Integer(ipCount));
      }

      if (ipCount >= fixedAuthFailCount){
         bodyText = err.toString();
         queNotif.addGeneralErrorCountByOne();
         queNotif.appendBodyText(bodyText);
      }
   }

   /**
    * Get an integer classFlag based on the given className argument.
    *
    * @param   className   the name of a java class
    * @return                an integer classFlag associated with the className
    */
   private int getClassFlagBasedUponClassName(String className)
   {
      int classFlag = -1;

      if (className.equalsIgnoreCase("PSApplicationAuthorizationError"))
         classFlag = ms_authorizationError;
      else if (className.equalsIgnoreCase("PSApplicationDesignError"))
         classFlag = ms_designError;
      else if (className.equalsIgnoreCase("PSHtmlProcessingError"))
         classFlag = ms_htmlProcessingError;
      else if (className.equalsIgnoreCase("PSLargeRequestQueueError"))
         classFlag = ms_requestQueueError;
      else if (className.equalsIgnoreCase("PSLargeApplicationRequestQueueError"))
         classFlag = ms_largeRequestError;
      else if (className.equalsIgnoreCase("PSPoorResponseTimeError"))
         classFlag = ms_responseTimeError;
      else if (className.equalsIgnoreCase("PSValidationError"))
         classFlag = ms_validationError;
      else if (className.equalsIgnoreCase("PSXmlProcessingError"))
         classFlag = ms_xmlProcessingError;
      else if (className.equalsIgnoreCase("PSBackEndAuthorizationError"))
         classFlag = ms_backAuthorizationError;
      else if (className.equalsIgnoreCase("PSDataConversionError"))
         classFlag = ms_dataConversionError;
      else if (className.equalsIgnoreCase("PSBackEndQueryProcessingError"))
         classFlag = ms_backQueryError;
      else if (className.equalsIgnoreCase("PSLargeBackEndRequestQueueError"))
         classFlag = ms_backRequestQueueError;
      else if (className.equalsIgnoreCase("PSBackEndServerDownError"))
         classFlag = ms_backServerDownError;
      else if (className.equalsIgnoreCase("PSBackEndUpdateProcessingError"))
         classFlag = ms_backUpdateError;

      return classFlag;
   }

   private static PSMapClassToObject createPageMapFromErrorPages(
      PSErrorWebPages pages)
      throws IllegalArgumentException
   {
      PSMapClassToObject map;
      if (pages != null) {
         map = new PSMapClassToObject();
         PSCustomError custom;

         for (int i = 0; i < pages.size(); i++)
         {
            custom = (PSCustomError)pages.get(i);

            /* the custom errors are using the class name string, not the
             * human readable name. As such, now loading the class
             * directly from the error code (fixes bug id ATEG-4BHQG5)
             */

            // - this is the original version, using human readable names -
            // map.addReplaceMapping(PSErrorHumanReadableNames.getErrorClass(
            //    custom.getErrorCode()), custom.getURL());

            try {
               map.addReplaceMapping(
                  Class.forName(custom.getErrorCode()), custom.getURL());
            } catch (ClassNotFoundException e) {
               throw new IllegalArgumentException(e.toString());
            }
         }
      }
      else
         map = null;
      return map;
   }


   // The following constants are also defined in class PSQueuedNotification
   private static final int ms_authorizationError     = 0;
   private static final int ms_designError            = 1;
   private static final int ms_htmlProcessingError    = 2;
   private static final int ms_requestQueueError      = 3;
   private static final int ms_largeRequestError      = 4; // GUI does not have it
   private static final int ms_responseTimeError      = 5;
   private static final int ms_validationError        = 6;
   private static final int ms_xmlProcessingError     = 7;
   private static final int ms_backAuthorizationError = 8;
   private static final int ms_dataConversionError    = 9;
   private static final int ms_backQueryError         = 10;
   private static final int ms_backRequestQueueError  = 11;
   private static final int ms_backServerDownError    = 12;
   private static final int ms_backUpdateError        = 13;

   /** log handler used to write log messages when an error occurs */
   private com.percussion.log.PSLogHandler m_logHandler;

   /** map from error objects to error pages */
   private PSMapClassToObject m_pages;

   /** notification settings; defines who should be notified and when */
   private PSNotifier m_notify;

   /** queued notification object */
   private PSQueuedNotification m_queNotif = null;

   /** queued notifications with the receiver name as the key */
   private java.util.HashMap m_queNotifHash = new java.util.HashMap();

   /** a hash from ip addresses to a count of app auth failures */
   private java.util.HashMap m_appCountHash = new java.util.HashMap();

   /** a hash from ip addresses to a count of back end auth failures */
   private java.util.HashMap m_beCountHash  = new java.util.HashMap();

   /** return errors as HTML or use whatever the source type is? */
   private boolean m_returnHtmlErrors = false;
}
