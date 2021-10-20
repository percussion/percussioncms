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

package com.percussion.design.objectstore;

import com.percussion.error.PSException;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Objects;


/**
 * The PSRecipient class defines the mail notification settings for a
 * particular user. Notifications are sent based upon various errors
 * which may occur during request processing by an application.
 *
 * @see PSNotifier#getRecipients
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSRecipient extends PSComponent
{
   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param      sourceNode      the XML element node to construct this
    *                              object from
    *
    * @param      parentDoc      the Java object which is the parent of this
    *                              object
    *
    * @param      parentComponents   the parent objects of this object
    *
    * @exception   PSUnknownNodeTypeException
    *                              if the XML element node is not of the
    *                              appropriate type
    */
   public PSRecipient(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, List parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructor for serialization, fromXml, etc.
    */
   PSRecipient() {
      super();
   }

   /**
    * Construct a recipient object for the specified user. It is enabled
    * by default. No notification conditions are initially set.
    *
    * @param   name      the e-mail address of the recipient
    */
   public PSRecipient(java.lang.String name)
      throws PSIllegalArgumentException
   {
      super();
      setName(name);
      m_options = 0;
   }

   /**
    * Gets the e-mail address of the recipient.
    *
    * @return     the e-mail address of the recipient
    */
   public java.lang.String getName()
   {
      return m_name;
   }

   /**
    * Sets the e-mail address of the recipient. The e-mail address is not
    * verified as being a valid mail account. This may result in
    * non-delivery errors at run-time.
    *   This is limited to 255 characters.
    *
    * @param      name   the e-mail address of the recipient
    *
    * @exception   PSIllegalArgumentException   if name is null, empty or
    *                                          exceeds the specified limit
    */
   public void setName(java.lang.String name)
      throws PSIllegalArgumentException
   {
      PSIllegalArgumentException ex = validateName(name);
      if (ex != null)
         throw ex;

      m_name = name;
   }

   private static PSIllegalArgumentException validateName(String name)
   {
      if (null == name || name.length() == 0)
         return new PSIllegalArgumentException(
            IPSObjectStoreErrors.RECIPIENT_NAME_EMPTY);
      else if (name.length() > MAX_RECIPIENT_NAME_LEN) {
         Object[] args = {MAX_RECIPIENT_NAME_LEN,
                 name.length()};
         return new PSIllegalArgumentException(
            IPSObjectStoreErrors.RECIPIENT_NAME_TOO_BIG, args);
      }

      return null;
   }

   /**
    * Is the sending of e-mail notifications to this recipient enabled?
    *
    * @return     <code>true</code> if sending notification is enabled,
    *            <code>false</code> if it is disabled
    */
   public boolean isSendEnabled()
   {
      return ((m_options & MN_SEND_ENABLED) == MN_SEND_ENABLED);
   }

   /**
    * Enable or disable the sending of e-mail notifications to this
    * recipient.
    *
    * @param   enable   <code>true</code> to enable sending notification,
    *                   <code>false</code> to disable it
    */
   public void setSendEnabled(boolean enable)
   {
      if(enable)
         m_options |= MN_SEND_ENABLED;
      else
         m_options &= ~MN_SEND_ENABLED;
   }

   /**
    * Is notification resent after the same error occurs a specified
    * number of times?
    * <p>
    * When a particular type of error persists, it may be undesirable to have
    * notification continually sent. By setting the number of times the error
    * must recur before sending notification can help avoid such situations.
    *
    * @return            <code>true</code> if the error threshold is
    *                   based upon the number of times the error recurs,
    *                   <code>false</code> otherwise
    */
   public boolean isErrorThresholdByCount()
   {
      return ((m_options & MN_ERROR_THRESHOLD_BY_COUNT) == MN_ERROR_THRESHOLD_BY_COUNT);
   }

   /**
    * Enable resending notification after the same error occurs a specified
    * number of times.
    * <p>
    * When a particular type of error persists, it may be undesirable to have
    * notification continually sent. By setting the number of times the error
    * must recur before sending notification can help avoid such situations.
    */

   //Ravi      boolean parameter is included for this mathod
   public void setErrorThresholdByCount(boolean enable)
   {
      if(enable)
         m_options |= MN_ERROR_THRESHOLD_BY_COUNT;
      else
         m_options &= ~MN_ERROR_THRESHOLD_BY_COUNT;
   }

   /**
    * Get the number of times the same error must be encountered before
    * notification is resent.
    * <p>
    * When a particular type of error persists, it may be undesirable to have
    * notification continually sent. By setting the number of times the error
    * must recur before sending notification can help avoid such situations.
    *
    * @return            the number of times the same error must be
    *                   encountered before notification is resent
    */
   public int getErrorThresholdCount()
   {
      return m_errorThresholdCount;
   }

   /**
    * Set the number of times the same error must be encountered before
    * notification is resent.
    * <p>
    * When a particular type of error persists, it may be undesirable to have
    * notification continually sent. By setting the number of times the error
    * must recur before sending notification can help avoid such situations.
    *
    * @param   count    the number of times the same error must be
    *                   encountered before notification is resent
    */
   public void setErrorThresholdCount(int count)
   {
      m_errorThresholdCount = count;
   }

   /**
    * Is notification resent if the same error persists for the specified
    * interval of time?
    * <p>
    * When a particular type of error persists, it may be undesirable to have
    * notification continually sent. By setting the interval for which
    * recurring errors must persist before sending notification can help
    * avoid such situations. It can also be useful as a reminder to the
    * administrator.
    *
    * @return            <code>true</code> if the error threshold is
    *                   based upon the time interval the error recurs,
    *                   <code>false</code> otherwise
    */
   public boolean isErrorThresholdByInterval()
   {
      return ((m_options & MN_ERROR_THRESHOLD_BY_INTERVAL) == MN_ERROR_THRESHOLD_BY_INTERVAL);
   }

   /**
    * Enable resending notification if the same error persists for the
    * specified interval of time.
    * <p>
    * When a particular type of error persists, it may be undesirable to have
    * notification continually sent. By setting the interval for which
    * recurring errors must persist before sending notification can help
    * avoid such situations. It can also be useful as a reminder to the
    * administrator.
    */

   //Ravi      boolean parameter is included for this method
   public void setErrorThresholdByInterval(boolean enable)
   {
      if(enable)
         m_options |= MN_ERROR_THRESHOLD_BY_INTERVAL;
      else
         m_options &= ~MN_ERROR_THRESHOLD_BY_INTERVAL;
   }

   /**
    * Get the time interval for which the same error must be encountered
    * before notification is resent.
    * <p>
    * When a particular type of error persists, it may be undesirable to have
    * notification continually sent. By setting the interval for which
    * recurring errors must persist before sending notification can help
    * avoid such situations. It can also be useful as a reminder to the
    * administrator.
    *
    * @return               the time interval, in minutes
    */
   public int getErrorThresholdInterval()
   {
      return m_errorThresholdInterval;
   }

   /**
    * Set the time interval for which the same error must be encountered
    * before notification is resent.
    * <p>
    * When a particular type of error persists, it may be undesirable to have
    * notification continually sent. By setting the interval for which
    * recurring errors must persist before sending notification can help
    * avoid such situations. It can also be useful as a reminder to the
    * administrator.
    *
    * @param   interval    the time interval, in minutes
    */
   public void setErrorThresholdInterval(int interval)
   {
      m_errorThresholdInterval = interval;
   }

   /**
    * Is notification sent when an application authorization failure is
    * encountered?
    *
    * @return     <code>true</code> if this type of notification is enabled,
    *            <code>false</code> if it is disabled
    */
   public boolean isAppAuthorizationFailureEnabled()
   {
      return ((m_options & MN_APP_AUTH_FAILURE) == MN_APP_AUTH_FAILURE);
   }

   /**
    * Enable or disable sending notification when an application
    * authorization failure is encountered. If this is being enabled
    * and a failure count was not previously set, a default count of 5
    * will be set.
    *
    * @param   enable   <code>true</code> to enable sending notification,
    *                   <code>false</code> to disable it
    * @see      #setAppAuthorizationFailureCount
    */
   public void setAppAuthorizationFailureEnabled(boolean enable)
   {
      if(enable)
         m_options |= MN_APP_AUTH_FAILURE;
      else
         m_options &= ~MN_APP_AUTH_FAILURE;
   }

   /**
    * Get the number of failed logins from a particular host which
    * will cause notification of an application authorization failure
    * to be sent.
    * <p>
    * Login failures are remembered for 15 minutes or until the user
    * successfully logs in.
    *
    * @return            the number of times the same host machine can
    *                   attempt to login before sending notification of
    *                   the authorization failure
    */
   public int getAppAuthorizationFailureCount()
   {
      return m_appAuthFailureCount;
   }

   /**
    * Set the number of failed logins from a particular host which
    * will cause notification of an application authorization failure
    * to be sent.
    * <p>
    * Login failures are remembered for 15 minutes or until the user
    * successfully logs in.
    *
    * @param   count    the number of times the same host machine can
    *                   attempt to login before sending notification of
    *                   the authorization failure
    */
   public void setAppAuthorizationFailureCount(int count)
   {
      m_appAuthFailureCount = count;
   }

   /**
    * Is notification sent when an application design error is
    * encountered?
    *
    * @return     <code>true</code> if this type of notification is enabled,
    *            <code>false</code> if it is disabled
    */
   public boolean isAppDesignErrorEnabled()
   {
      return ((m_options & MN_APP_DESIGN_ERROR) == MN_APP_DESIGN_ERROR);
   }

   /**
    * Enable or disable sending notification when an application
    * design error is encountered.
    *
    * @param   enable   <code>true</code> to enable sending notification,
    *                   <code>false</code> to disable it
    */
   public void setAppDesignErrorEnabled(boolean enable)
   {
      if(enable)
         m_options |= MN_APP_DESIGN_ERROR;
      else
         m_options &= ~MN_APP_DESIGN_ERROR;
   }

   /**
    * Is notification sent when the application encounters a data validation
    * error?
    *
    * @return     <code>true</code> if this type of notification is enabled,
    *            <code>false</code> if it is disabled
    */
   public boolean isAppValidationErrorEnabled()
   {
      return ((m_options & MN_APP_VALIDATION_ERROR) == MN_APP_VALIDATION_ERROR);
   }

   /**
    * Enable or disable sending notification when the application
    * encounters a data valdiation error.
    *
    * @param   enable   <code>true</code> to enable sending notification,
    *                   <code>false</code> to disable it
    */
   public void setAppValidationErrorEnabled(boolean enable)
   {
      if(enable)
         m_options |= MN_APP_VALIDATION_ERROR;
      else
         m_options &= ~MN_APP_VALIDATION_ERROR;
   }

   /**
    * Is notification sent when the application encounters an error
    * processing the XML document?
    *
    * @return     <code>true</code> if this type of notification is enabled,
    *            <code>false</code> if it is disabled
    */
   public boolean isAppXmlErrorEnabled()
   {
      return ((m_options & MN_APP_XML_ERROR) == MN_APP_XML_ERROR);
   }

   /**
    * Enable or disable sending notification when the application
    * encounters an error processing the XML document?
    *
    * @param   enable   <code>true</code> to enable sending notification,
    *                   <code>false</code> to disable it
    */
   public void setAppXmlErrorEnabled(boolean enable)
   {
      if(enable)
         m_options |= MN_APP_XML_ERROR;
      else
         m_options &= ~MN_APP_XML_ERROR;
   }

   /**
    * Is notification sent when the application encounters an error
    * processing HTML data?
    *
    * @return     <code>true</code> if this type of notification is enabled,
    *            <code>false</code> if it is disabled
    */
   public boolean isAppHtmlErrorEnabled()
   {
      return ((m_options & MN_APP_HTML_ERROR) == MN_APP_HTML_ERROR);
   }

   /**
    * Enable or disable sending notification when the application
    * encounters an error processing HTML data?
    *
    * @param   enable   <code>true</code> to enable sending notification,
    *                   <code>false</code> to disable it
    */
   public void setAppHtmlErrorEnabled(boolean enable)
   {
      if(enable)
         m_options |= MN_APP_HTML_ERROR;
      else
         m_options &= ~MN_APP_HTML_ERROR;
   }

   /**
    * Is notification sent when poor application response time is
    * detected?
    *
    * @return     <code>true</code> if this type of notification is enabled,
    *            <code>false</code> if it is disabled
    */
   public boolean isAppResponseTimeEnabled()
   {
      return ((m_options & MN_APP_RESPONSE_TIME) == MN_APP_RESPONSE_TIME);
   }

   /**
    * Enable or disable sending notification when poor application
    * response time is detected. If this is being enabled and the max
    * response time was not previously set, a default max of 5 minutes
    * will be set.
    *
    * @param   enable   <code>true</code> to enable sending notification,
    *                   <code>false</code> to disable it
    * @see      #setAppResponseTimeMax
    */
   public void setAppResponseTimeEnabled(boolean enable)
   {
      if(enable)
         m_options |= MN_APP_RESPONSE_TIME;
      else
         m_options &= ~MN_APP_RESPONSE_TIME;
   }

   /**
    * Get the maximum amount of time permitted before sending notification
    * of poor application response time.
    *
    * @return            the response time threshold
    */
   public int getAppResponseTimeMax()
   {
      return m_appResponseTime;
   }

   /**
    * Set the maximum amount of time permitted before sending notification
    * of poor application response time.
    *
    * @param   max      the response time threshold
    */
   public void setAppResponseTimeMax(int max)
   {
      m_appResponseTime = max;
   }

   /**
    * Is notification sent when the user request queue for the
    * application is too large?
    *
    * @return     <code>true</code> if this type of notification is enabled,
    *            <code>false</code> if it is disabled
    */
   public boolean isAppRequestQueueLargeEnabled()
   {
      return ((m_options & MN_APP_REQUEST_QUEUE) == MN_APP_REQUEST_QUEUE);
   }

   /**
    * Enable or disable sending notification when the user request queue
    * for the application is too large. If this is being enabled and the
    * queue limit was not previously set, a default limit of 50 requests
    * will be set.
    *
    * @param   enable   <code>true</code> to enable sending notification,
    *                   <code>false</code> to disable it
    * @see      #setAppRequestQueueMax
    */
   public void setAppRequestQueueLargeEnabled(boolean enable)
   {
      if(enable)
         m_options |= MN_APP_REQUEST_QUEUE;
      else
         m_options &= ~MN_APP_REQUEST_QUEUE;
   }

   /**
    * Get the maximum number of requests which may be queued before
    * sending notification of a large application user request queue.
    *
    * @return            the request queue limit
    */
   public int getAppRequestQueueMax()
   {
      return m_appRequestQueue;
   }

   /**
    * Set the maximum number of requests which may be queued before
    * sending notification of a large application user request queue.
    *
    * @param   max      the request queue limit
    */
   public void setAppRequestQueueMax(int max)
   {
      m_appRequestQueue = max;
   }

   /**
    * Is notification sent when a back-end authorization failure is
    * encountered?
    *
    * @return     <code>true</code> if this type of notification is enabled,
    *            <code>false</code> if it is disabled
    */
   public boolean isBackEndAuthorizationFailureEnabled()
   {
      return ((m_options & MN_BACKEND_AUTH_FAILURE) == MN_BACKEND_AUTH_FAILURE);
   }

   /**
    * Enable or disable sending notification when a back-end
    * authorization failure is encountered. If this is being enabled
    * and a failure count was not previously set, a default count of 5
    * will be set.
    *
    * @param   enable   <code>true</code> to enable sending notification,
    *                   <code>false</code> to disable it
    * @see      #setBackEndAuthorizationFailureCount
    */
   public void setBackEndAuthorizationFailureEnabled(boolean enable)
   {
      if(enable)
         m_options |= MN_BACKEND_AUTH_FAILURE;
      else
         m_options &= ~MN_BACKEND_AUTH_FAILURE;
   }

   /**
    * Get the number of failed logins from a particular host which
    * will cause notification of a back-end authorization failure
    * to be sent.
    * <p>
    * Login failures are remembered for 15 minutes or until the user
    * successfully logs in.
    *
    * @return            the number of times the same host machine can
    *                   attempt to login before sending notification of
    *                   the authorization failure
    */
   public int getBackEndAuthorizationFailureCount()
   {
      return m_backendAuthFailureCount;
   }

   /**
    * Set the number of failed logins from a particular host which
    * will cause notification of a back-end authorization failure
    * to be sent.
    * <p>
    * Login failures are remembered for 15 minutes or until the user
    * successfully logs in.
    *
    * @param   count    the number of times the same host machine can
    *                   attempt to login before sending notification of
    *                   the authorization failure
    */
   public void setBackEndAuthorizationFailureCount(int count)
   {
      m_backendAuthFailureCount = count;
   }

   /**
    * Is notification sent when the back-end server is down?
    *
    * @return     <code>true</code> if this type of notification is enabled,
    *            <code>false</code> if it is disabled
    */
   public boolean isBackEndServerDownFailureEnabled()
   {
      return ((m_options & MN_BACKEND_SERVER_DOWN_FAILURE) == MN_BACKEND_SERVER_DOWN_FAILURE);
   }

   /**
    * Enable or disable sending notification when the back-end server
    * is down.
    *
    * @param   enable   <code>true</code> to enable sending notification,
    *                   <code>false</code> to disable it
    */
   public void setBackEndServerDownFailureEnabled(boolean enable)
   {
      if(enable)
         m_options |= MN_BACKEND_SERVER_DOWN_FAILURE;
      else
         m_options &= ~MN_BACKEND_SERVER_DOWN_FAILURE;
   }

   /**
    * Is notification sent when a back-end data conversion failure is
    * encountered?
    *
    * @return     <code>true</code> if this type of notification is enabled,
    *            <code>false</code> if it is disabled
    */
   public boolean isBackEndDataConversionErrorEnabled()
   {
      return ((m_options & MN_BACKEND_DATA_CONVERSION_FAILURE) == MN_BACKEND_DATA_CONVERSION_FAILURE);
   }

   /**
    * Enable or disable sending notification when a back-end data
    * conversion failure is encountered.
    *
    * @param   enable   <code>true</code> to enable sending notification,
    *                   <code>false</code> to disable it
    * @see      #setAppAuthorizationFailureCount
    */
   public void setBackEndDataConversionErrorEnabled(boolean enable)
   {
      if(enable)
         m_options |= MN_BACKEND_DATA_CONVERSION_FAILURE;
      else
         m_options &= ~MN_BACKEND_DATA_CONVERSION_FAILURE;
   }

   /**
    * Is notification sent when a back-end query fails?
    *
    * @return     <code>true</code> if this type of notification is enabled,
    *            <code>false</code> if it is disabled
    */
   public boolean isBackEndQueryFailureEnabled()
   {
      return ((m_options & MN_BACKEND_QUERY_FAILURE) == MN_BACKEND_QUERY_FAILURE);
   }

   /**
    * Enable or disable sending notification when a back-end query fails.
    * A query resulting in no data being found is not considered an error.
    *
    * @param   enable   <code>true</code> to enable sending notification,
    *                   <code>false</code> to disable it
    */
   public void setBackEndQueryFailureEnabled(boolean enable)
   {
      if(enable)
         m_options |= MN_BACKEND_QUERY_FAILURE;
      else
         m_options &= ~MN_BACKEND_QUERY_FAILURE;
   }

   /**
    * Is notification sent when a back-end insert, update or delete fails?
    *
    * @return     <code>true</code> if this type of notification is enabled,
    *            <code>false</code> if it is disabled
    */
   public boolean isBackEndUpdateFailureEnabled()
   {
      return ((m_options & MN_BACKEND_UPDATE_FAILURE) == MN_BACKEND_UPDATE_FAILURE);
   }

   /**
    * Enable or disable sending notification when a back-end insert, update
    * or delete fails. Unlike query, an update or delete which does not
    * find a match is considered an error.
    *
    * @param   enable   <code>true</code> to enable sending notification,
    *                   <code>false</code> to disable it
    */
   public void setBackEndUpdateFailureEnabled(boolean enable)
   {
      if(enable)
         m_options |= MN_BACKEND_UPDATE_FAILURE;
      else
         m_options &= ~MN_BACKEND_UPDATE_FAILURE;
   }

   /**
    * Is notification sent when the request queue for the
    * back-end is too large?
    *
    * @return     <code>true</code> if this type of notification is enabled,
    *            <code>false</code> if it is disabled
    */
   public boolean isBackEndRequestQueueLargeEnabled()
   {
      return ((m_options & MN_BACKEND_REQUEST_QUEUE) == MN_BACKEND_REQUEST_QUEUE);
   }

   /**
    * Enable or disable sending notification when the request queue
    * for the back-end is too large. If this is being enabled and the
    * queue limit was not previously set, a default limit of 50 requests
    * will be set.
    *
    * @param   enable   <code>true</code> to enable sending notification,
    *                   <code>false</code> to disable it
    * @see      #setBackEndRequestQueueMax
    */
   public void setBackEndRequestQueueLargeEnabled(boolean enable)
   {
      if(enable)
         m_options |= MN_BACKEND_REQUEST_QUEUE;
      else
         m_options &= ~MN_BACKEND_REQUEST_QUEUE;
   }

   /**
    * Get the maximum number of requests which may be queued before
    * sending notification of a large back-end request queue.
    *
    * @return            the request queue limit
    */
   public int getBackEndRequestQueueMax()
   {
      return    m_backendRequestQueue;
   }

   /**
    * Set the maximum number of requests which may be queued before
    * sending notification of a large back-end request queue.
    *
    * @param   max      the request queue limit
    */
   public void setBackEndRequestQueueMax(int max)
   {
      m_backendRequestQueue = max;
   }


   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXRecipient XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *       PSXRecipient defines the mail notification settings for a
    *       particular user. Notifications are sent based upon various
    *       errors which may occur during request processing by an
    *       application.
    *    --&gt;
    *    &lt;!ELEMENT PSXRecipient            (name, Threshold,
    *                                         ApplicationEvents,
    *                                         BackEndEvents)&gt;
    *
    *    &lt;!--
    *       attributes for this object:
    *
    *       enabled - is the sending of e-mail notifications to this
    *       recipient enabled?
    *    --&gt;
    *    &lt;!ATTLIST PSXRecipient
    *       enabled     %PSXIsEnabled    #OPTIONAL
    *    &gt;
    *
    *    &lt;!--
    *       the e-mail address of the recipient.
    *    --&gt;
    *    &lt;!ELEMENT name                   (#PCDATA)&gt;
    *
    *    &lt;!ELEMENT Threshold               (count, time)&gt;
    *
    *    &lt;!--
    *       what type of threshold is being used?
    *
    *       none - notification is sent for each occurence of an error.
    *
    *       exceedsCount - notification is resent after the same error occurs
    *       a specified number of times. When a particular type of error
    *       persists, it may be undesirable to have notification continually
    *       sent. By setting the number of times the error must recur before
    *       sending notification can help avoid such situations.
    *
    *       exceedsInterval - notification is resent if the same error persists
    *       for the specified interval of time. When a particular type of
    *       error persists, it may be undesirable to have notification
    *       continually sent. By setting the interval for which recurring
    *       errors must persist before sending notification can help avoid
    *       such situations. It can also be useful as a reminder to the
    *       administrator.
    *    --&gt;
    *    &lt;!ENTITY % PSXNotifyThresholdType "(none, exceedsCount, exceedsInterval)"&gt;
    *    &lt;!ATTLIST Threshold
    *        type      (%PSXNotifyThresholdType)    #IMPLIED&gt;
    *
    *    &lt;!--
    *       the number of times the same error must be encountered before
    *       notification is resent.
    *    --&gt;
    *    &lt;!ELEMENT count                  (#PCDATA)&gt;
    *
    *    &lt;!--
    *       the time interval for which the same error must be encountered
    *       before notification is resent.
    *    --&gt;
    *    &lt;!ELEMENT interval              (#PCDATA)&gt;
    *
    *    &lt;!--
    *       application specific events and errors
    *    --&gt;
    *    &lt;!ELEMENT ApplicationEvents      (authorizationFailure?,
    *                                         designError?, validationError?,
    *                                         xmlError?, htmlError?,
    *                                         poorResponseTime?,
    *                                         largeRequestQueue?)&gt;
    *
    *    &lt;!--
    *       For application events - the number of failed logins from a
    *       particular host which will cause notification of an application
    *       authorization failure to be sent.
    *
    *       For back-end events - the number of failed logins from a
    *       particular host which will cause notification of a back-end
    *       authorization failure to be sent.
    *
    *       Login failures are remembered for 15 minutes or until the user
    *       successfully logs in.
    *    --&gt;
    *    &lt;!ELEMENT authorizationFailure   (#PCDATA)&gt;
    *    &lt;!ATTLIST authorizationFailure
    *       enabled     %PSXIsEnabled    #OPTIONAL
    *    &gt;
    *
    *    &lt;!--
    *       is notification sent when an application design error is
    *       ecountered?
    *    --&gt;
    *    &lt;!ELEMENT designError            EMPTY&gt;
    *    &lt;!ATTLIST designError
    *       enabled     %PSXIsEnabled    #OPTIONAL
    *    &gt;
    *
    *    &lt;!--
    *       is notification sent when the application encounters a data
    *       validation error?
    *    --&gt;
    *    &lt;!ELEMENT validationError         EMPTY&gt;
    *    &lt;!ATTLIST validationError
    *       enabled     %PSXIsEnabled    #OPTIONAL
    *    &gt;
    *
    *    &lt;!--
    *       is notification sent when the application encounters an error
    *       processing the XML document?
    *    --&gt;
    *    &lt;!ELEMENT xmlError               EMPTY&gt;
    *    &lt;!ATTLIST xmlError
    *       enabled     %PSXIsEnabled    #OPTIONAL
    *    &gt;
    *
    *    &lt;!--
    *       is notification sent when the application encounters an error
    *       processing the HTML data?
    *    --&gt;
    *    &lt;!ELEMENT htmlError               EMPTY&gt;
    *    &lt;!ATTLIST htmlError
    *       enabled     %PSXIsEnabled    #OPTIONAL
    *    &gt;
    *
    *    &lt;!--
    *       the maximum amount of time permitted before sending notification
    *       of poor application response time.
    *    --&gt;
    *    &lt;!ELEMENT poorResponseTime       (#PCDATA)&gt;
    *    &lt;!ATTLIST poorResponseTime
    *       enabled     %PSXIsEnabled    #OPTIONAL
    *    &gt;
    *
    *    &lt;!--
    *       For application events - the maximum number of requests which may
    *       be queued before sending notification of a large application user
    *       request queue.
    *
    *       For back-end events - the maximum number of requests which may be
    *       queued before sending notification of a large back-end request
    *       queue.
    *    --&gt;
    *    &lt;!ELEMENT largeRequestQueue      (#PCDATA)&gt;
    *    &lt;!ATTLIST largeRequestQueue
    *       enabled     %PSXIsEnabled    #OPTIONAL
    *    &gt;
    *
    *    &lt;!--
    *       back-end specific events and errors
    *    --&gt;
    *    &lt;!ELEMENT BackEndEvents          (authorizationFailure?,
    *                                         serverDown?,
    *                                         dataConversionError?,
    *                                         queryError?, updateError?,
    *                                         largeRequestQueue?)&gt;
    *
    *    &lt;!--
    *       is notification sent when the back-end server is down?
    *    --&gt;
    *    &lt;!ELEMENT serverDown             EMPTY&gt;
    *    &lt;!ATTLIST serverDown
    *       enabled     %PSXIsEnabled    #OPTIONAL
    *    &gt;
    *
    *    &lt;!--
    *       is notification sent when a back-end data conversion failure is
    *       encountered?
    *    --&gt;
    *    &lt;!ELEMENT dataConversionError    EMPTY&gt;
    *    &lt;!ATTLIST dataConversionError
    *       enabled     %PSXIsEnabled    #OPTIONAL
    *    &gt;
    *
    *    &lt;!--
    *       is notification sent when a back-end query fails?
    *    --&gt;
    *    &lt;!ELEMENT queryError             EMPTY&gt;
    *    &lt;!ATTLIST queryError
    *       enabled     %PSXIsEnabled    #OPTIONAL
    *    &gt;
    *
    *    &lt;!--
    *       is notification sent when a back-end insert, update or delete
    *       fails?
    *    --&gt;
    *    &lt;!ELEMENT updateError            EMPTY&gt;
    *    &lt;!ATTLIST updateError
    *       enabled     %PSXIsEnabled    #OPTIONAL
    *    &gt;
    * </code></pre>
    *
    * @return     the newly created PSXRecipient XML element node
    */
   public Element toXml(Document doc)
   {
      Element   root = doc.createElement(ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      if ((m_options & MN_SEND_ENABLED) == MN_SEND_ENABLED)
         root.setAttribute("enabled", "yes");
      else
         root.setAttribute("enabled", "no");

      //private          String      m_name = "";
      PSXmlDocumentBuilder.addElement(doc, root, "name", m_name);

      Element parent = PSXmlDocumentBuilder.addEmptyElement(doc, root, "Threshold");
      if ((m_options & MN_ERROR_THRESHOLD_BY_COUNT) == MN_ERROR_THRESHOLD_BY_COUNT)
         parent.setAttribute("type", "exceedsCount");
      else if ((m_options & MN_ERROR_THRESHOLD_BY_INTERVAL) == MN_ERROR_THRESHOLD_BY_INTERVAL)
         parent.setAttribute("type", "exceedsInterval");
      else
         parent.setAttribute("type", "none");

      //private          int         m_errorThresholdCount = 0;
      PSXmlDocumentBuilder.addElement( doc, parent, "count",
         String.valueOf(m_errorThresholdCount));

      //private          int         m_errorThresholdInterval = 0;
      PSXmlDocumentBuilder.addElement( doc, parent, "interval",
         String.valueOf(m_errorThresholdInterval));

      // build the application events listing
      parent = PSXmlDocumentBuilder.addEmptyElement(doc, root, "ApplicationEvents");

      Element event = PSXmlDocumentBuilder.addElement(
         doc, parent, "authorizationFailure",
         String.valueOf(m_appAuthFailureCount));
      if ((m_options & MN_APP_AUTH_FAILURE) == MN_APP_AUTH_FAILURE)
         event.setAttribute("enabled", "yes");
      else
         event.setAttribute("enabled", "no");

      event = PSXmlDocumentBuilder.addEmptyElement(
         doc, parent, "designError");
      if ((m_options & MN_APP_DESIGN_ERROR) == MN_APP_DESIGN_ERROR)
         event.setAttribute("enabled", "yes");
      else
         event.setAttribute("enabled", "no");

      event = PSXmlDocumentBuilder.addEmptyElement(
         doc, parent, "validationError");
      if ((m_options & MN_APP_VALIDATION_ERROR) == MN_APP_VALIDATION_ERROR)
         event.setAttribute("enabled", "yes");
      else
         event.setAttribute("enabled", "no");

      event = PSXmlDocumentBuilder.addEmptyElement(
         doc, parent, "xmlError");
      if ((m_options & MN_APP_XML_ERROR) == MN_APP_XML_ERROR)
         event.setAttribute("enabled", "yes");
      else
         event.setAttribute("enabled", "no");

      event = PSXmlDocumentBuilder.addEmptyElement(
         doc, parent, "htmlError");
      if ((m_options & MN_APP_HTML_ERROR) == MN_APP_HTML_ERROR)
         event.setAttribute("enabled", "yes");
      else
         event.setAttribute("enabled", "no");

      event = PSXmlDocumentBuilder.addElement(
         doc, parent, "poorResponseTime",
         String.valueOf(m_appResponseTime));
      if ((m_options & MN_APP_RESPONSE_TIME) == MN_APP_RESPONSE_TIME)
         event.setAttribute("enabled", "yes");
      else
         event.setAttribute("enabled", "no");

      event = PSXmlDocumentBuilder.addElement(
         doc, parent, "largeRequestQueue",
         String.valueOf(m_appRequestQueue));
      if ((m_options & MN_APP_REQUEST_QUEUE) == MN_APP_REQUEST_QUEUE)
         event.setAttribute("enabled", "yes");
      else
         event.setAttribute("enabled", "no");

      parent = PSXmlDocumentBuilder.addEmptyElement(doc, root, "BackEndEvents");

      event = PSXmlDocumentBuilder.addElement(
         doc, parent, "authorizationFailure",
         String.valueOf(m_backendAuthFailureCount));
      if ((m_options & MN_BACKEND_AUTH_FAILURE) == MN_BACKEND_AUTH_FAILURE)
         event.setAttribute("enabled", "yes");
      else
         event.setAttribute("enabled", "no");

      event = PSXmlDocumentBuilder.addEmptyElement(
         doc, parent, "serverDown");
      if ((m_options & MN_BACKEND_SERVER_DOWN_FAILURE) == MN_BACKEND_SERVER_DOWN_FAILURE)
         event.setAttribute("enabled", "yes");
      else
         event.setAttribute("enabled", "no");

      event = PSXmlDocumentBuilder.addEmptyElement(
         doc, parent, "dataConversionError");
      if ((m_options & MN_BACKEND_DATA_CONVERSION_FAILURE) == MN_BACKEND_DATA_CONVERSION_FAILURE)
         event.setAttribute("enabled", "yes");
      else
         event.setAttribute("enabled", "no");

      event = PSXmlDocumentBuilder.addEmptyElement(
         doc, parent, "queryError");
      if ((m_options & MN_BACKEND_QUERY_FAILURE) == MN_BACKEND_QUERY_FAILURE)
         event.setAttribute("enabled", "yes");
      else
         event.setAttribute("enabled", "no");

      event = PSXmlDocumentBuilder.addEmptyElement(
         doc, parent, "updateError");
      if ((m_options & MN_BACKEND_UPDATE_FAILURE) == MN_BACKEND_UPDATE_FAILURE)
         event.setAttribute("enabled", "yes");
      else
         event.setAttribute("enabled", "no");

      event = PSXmlDocumentBuilder.addElement(
         doc, parent, "largeRequestQueue",
         String.valueOf(m_backendRequestQueue));
      if ((m_options & MN_BACKEND_REQUEST_QUEUE) == MN_BACKEND_REQUEST_QUEUE)
         event.setAttribute("enabled", "yes");
      else
         event.setAttribute("enabled", "no");

      return root;
   }

   /**
    * This method is called to populate a PSRecipient Java object
    * from a PSXRecipient XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXRecipient
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                        List parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);

      if (!ms_NodeType.equals(sourceNode.getNodeName()))
      {
         Object[] args = { ms_NodeType, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker   tree = new PSXmlTreeWalker(sourceNode);
      Node saveCur;

      String sTemp = tree.getElementData("id");
      try {
         m_id = Integer.parseInt(sTemp);
      } catch (Exception e) {
         Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp) };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
      }

      m_options = 0;
      sTemp = tree.getElementData("enabled");
      if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
         m_options |= MN_SEND_ENABLED;

      try {      //private          String         m_name = 0;
         setName(tree.getElementData("name"));
      } catch (PSIllegalArgumentException e) {
         throw new PSUnknownNodeTypeException(ms_NodeType, "name", e);
      }

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
      firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      nextFlags  |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      org.w3c.dom.Node cur = tree.getCurrent();   // cur = <PSXRecipient>

      if (tree.getNextElement("Threshold", firstFlags) != null) {
         sTemp = tree.getElementData("type", false);
         if (sTemp != null) {
            if (sTemp.equalsIgnoreCase("exceedsCount"))
               m_options |= MN_ERROR_THRESHOLD_BY_COUNT;
            else if (sTemp.equalsIgnoreCase("exceedsInterval"))
               m_options |= MN_ERROR_THRESHOLD_BY_INTERVAL;
         }

         //private          int         m_errorThresholdCount = 0;
         sTemp = tree.getElementData("count", false);
         if (sTemp != null) {
            try {
               m_errorThresholdCount = Integer.parseInt(sTemp);
            } catch (NumberFormatException e) {
               Object[] args = { ms_NodeType, "Threshold/count", sTemp };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }
         }
         else if (isErrorThresholdByCount()) {
            Object[] args = { ms_NodeType, "Threshold/count", "" };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }

         //private          int         m_errorThresholdInterval = 0;
         sTemp = tree.getElementData("interval", false);
         if (sTemp != null) {
            try {
               m_errorThresholdInterval = Integer.parseInt(sTemp);
            } catch (NumberFormatException e) {
               Object[] args = { ms_NodeType, "Threshold/interval", sTemp };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }
         }
         else if (isErrorThresholdByInterval()) {
            Object[] args = { ms_NodeType, "Threshold/interval", "" };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
      }

      tree.setCurrent(cur);
      if (tree.getNextElement("ApplicationEvents", firstFlags) != null) {
         saveCur = tree.getCurrent();

         if (tree.getNextElement("authorizationFailure", firstFlags) != null) {
            sTemp = tree.getElementData("enabled", false);
            if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
               m_options |= MN_APP_AUTH_FAILURE;

            sTemp = tree.getElementData((Element)tree.getCurrent());
            if (sTemp != null) {
               try {
                  m_appAuthFailureCount = Integer.parseInt(sTemp);
               } catch (NumberFormatException e) {
                  Object[] args = { ms_NodeType, "ApplicationEvents/authorizationFailure", sTemp };
                  throw new PSUnknownNodeTypeException(
                     IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
               }
            }
         }

         tree.setCurrent(saveCur);
         if (tree.getNextElement("designError", firstFlags) != null) {
            sTemp = tree.getElementData("enabled", false);
            if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
               m_options |= MN_APP_DESIGN_ERROR;
         }

         tree.setCurrent(saveCur);
         if (tree.getNextElement("validationError", firstFlags) != null) {
            sTemp = tree.getElementData("enabled", false);
            if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
               m_options |= MN_APP_VALIDATION_ERROR;
         }

         tree.setCurrent(saveCur);
         if (tree.getNextElement("xmlError", firstFlags) != null) {
            sTemp = tree.getElementData("enabled", false);
            if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
               m_options |= MN_APP_XML_ERROR;
         }

         tree.setCurrent(saveCur);
         if (tree.getNextElement("htmlError", firstFlags) != null) {
            sTemp = tree.getElementData("enabled", false);
            if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
               m_options |= MN_APP_HTML_ERROR;
         }

         tree.setCurrent(saveCur);
         if (tree.getNextElement("poorResponseTime", firstFlags) != null) {
            sTemp = tree.getElementData("enabled", false);
            if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
               m_options |= MN_APP_RESPONSE_TIME;

            sTemp = tree.getElementData((Element)tree.getCurrent());
            if (sTemp != null) {
               try {
                  m_appResponseTime = Integer.parseInt(sTemp);
               } catch (NumberFormatException e) {
                  Object[] args = { ms_NodeType, "ApplicationEvents/poorResponseTime", sTemp };
                  throw new PSUnknownNodeTypeException(
                     IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
               }
            }
         }

         tree.setCurrent(saveCur);
         if (tree.getNextElement("largeRequestQueue", firstFlags) != null) {
            sTemp = tree.getElementData("enabled", false);
            if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
               m_options |= MN_APP_REQUEST_QUEUE;

            sTemp = tree.getElementData((Element)tree.getCurrent());
            if (sTemp != null) {
               try {
                  m_appRequestQueue = Integer.parseInt(sTemp);
               } catch (NumberFormatException e) {
                  Object[] args = { ms_NodeType, "ApplicationEvents/largeRequestQueue", sTemp };
                  throw new PSUnknownNodeTypeException(
                     IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
               }
            }
         }
      }   // end of outmost if

      tree.setCurrent(cur);
      if (tree.getNextElement("BackEndEvents", firstFlags) != null) {
         saveCur = tree.getCurrent();

         if (tree.getNextElement("authorizationFailure", firstFlags) != null) {
            sTemp = tree.getElementData("enabled", false);
            if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
               m_options |= MN_BACKEND_AUTH_FAILURE;

            sTemp = tree.getElementData((Element)tree.getCurrent());
            if (sTemp != null) {
               try {
                  m_backendAuthFailureCount = Integer.parseInt(sTemp);
               } catch (NumberFormatException e) {
                  Object[] args = { ms_NodeType, "BackEndEvents/authorizationFailure", sTemp };
                  throw new PSUnknownNodeTypeException(
                     IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
               }
            }
         }

         tree.setCurrent(saveCur);
         if (tree.getNextElement("serverDown", firstFlags) != null) {
            sTemp = tree.getElementData("enabled", false);
            if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
               m_options |= MN_BACKEND_SERVER_DOWN_FAILURE;
         }

         tree.setCurrent(saveCur);
         if (tree.getNextElement("dataConversionError", firstFlags) != null) {
            sTemp = tree.getElementData("enabled", false);
            if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
               m_options |= MN_BACKEND_DATA_CONVERSION_FAILURE;
         }

         tree.setCurrent(saveCur);
         if (tree.getNextElement("queryError", firstFlags) != null) {
            sTemp = tree.getElementData("enabled", false);
            if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
               m_options |= MN_BACKEND_QUERY_FAILURE;
         }

         tree.setCurrent(saveCur);
         if (tree.getNextElement("updateError", firstFlags) != null) {
            sTemp = tree.getElementData("enabled", false);
            if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
               m_options |= MN_BACKEND_UPDATE_FAILURE;
         }

         tree.setCurrent(saveCur);
         if (tree.getNextElement("largeRequestQueue", firstFlags) != null) {
            sTemp = tree.getElementData("enabled", false);
            if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
               m_options |= MN_BACKEND_REQUEST_QUEUE;

            sTemp = tree.getElementData((Element)tree.getCurrent());
            if (sTemp != null) {
               try {
                  m_backendRequestQueue = Integer.parseInt(sTemp);
               } catch (NumberFormatException e) {
                  Object[] args = { ms_NodeType, "BackEndEvents/largeRequestQueue", sTemp };
                  throw new PSUnknownNodeTypeException(
                     IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
               }
            }
         }
      }  // end of outmost if
   }

   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSSystemValidationException, but the
    * implementation must not directly throw any exceptions. Instead, it
    * should register any errors with the validation context, which will
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * rethrown).
    *
    * @param   cxt The validation context.
    *
    * @throws PSSystemValidationException According to the implementation of the
    * validation context (on warnings and/or errors).
    */
   public void validate(IPSValidationContext cxt) throws PSSystemValidationException
   {
      if (!cxt.startValidation(this, null))
         return;

      // turn on all invalid bits
      final int all_flags_compliment = ~(
           MN_SEND_ENABLED | MN_ERROR_THRESHOLD_BY_COUNT
         | MN_ERROR_THRESHOLD_BY_INTERVAL | MN_APP_AUTH_FAILURE
         | MN_APP_DESIGN_ERROR | MN_APP_VALIDATION_ERROR | MN_APP_XML_ERROR
         | MN_APP_HTML_ERROR | MN_APP_RESPONSE_TIME | MN_APP_REQUEST_QUEUE
         | MN_BACKEND_AUTH_FAILURE | MN_BACKEND_SERVER_DOWN_FAILURE
         | MN_BACKEND_DATA_CONVERSION_FAILURE | MN_BACKEND_QUERY_FAILURE
         | MN_BACKEND_UPDATE_FAILURE | MN_BACKEND_REQUEST_QUEUE);

      if (0 != (m_options & all_flags_compliment))
      {
         cxt.validationError(this, 0,
            "Invalid flags in recipient options: " + m_options);
      }

      PSException ex = validateName(m_name);
      if (ex != null)
         cxt.validationError(this, ex.getErrorCode(), ex.getErrorArguments());

      if (m_errorThresholdCount < 0)
         cxt.validationError(this, 0,
            "Invalid error threshold count: " + m_errorThresholdCount);

      if (m_errorThresholdInterval < 0)
         cxt.validationError(this, 0,
         "Invalid error Threshold Interval: " + m_errorThresholdInterval); 

      if (m_appAuthFailureCount < 0)
         cxt.validationError(this, 0,
            "Invalid app Auth Failure Count: " + m_appAuthFailureCount);

      if (m_appResponseTime < 0)
         cxt.validationError(this, 0,
            "Invalid app Response Time: " + m_appResponseTime);

      if (m_appRequestQueue < 0)
         cxt.validationError(this, 0,
            "Invalid app Request Queue: " + m_appRequestQueue);

      if (m_backendAuthFailureCount < 0)
         cxt.validationError(this, 0,
         "Invalid back end Auth Failure Count: " + m_backendAuthFailureCount);

      if (m_backendRequestQueue < 0)
         cxt.validationError(this, 0,
            "Invalid back end Request Queue: " + m_backendRequestQueue);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSRecipient)) return false;
      if (!super.equals(o)) return false;
      PSRecipient that = (PSRecipient) o;
      return m_options == that.m_options &&
              m_errorThresholdCount == that.m_errorThresholdCount &&
              m_errorThresholdInterval == that.m_errorThresholdInterval &&
              m_appAuthFailureCount == that.m_appAuthFailureCount &&
              m_appResponseTime == that.m_appResponseTime &&
              m_appRequestQueue == that.m_appRequestQueue &&
              m_backendAuthFailureCount == that.m_backendAuthFailureCount &&
              m_backendRequestQueue == that.m_backendRequestQueue &&
              Objects.equals(m_name, that.m_name);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_options, m_name, m_errorThresholdCount, m_errorThresholdInterval, m_appAuthFailureCount, m_appResponseTime, m_appRequestQueue, m_backendAuthFailureCount, m_backendRequestQueue);
   }
//define constant flags to handle all error types. The constants are defined as
   //private for now assuming no body else need to use them outside this class. In case
   //they found to be usefull outside this class

   private  static  final   int MN_SEND_ENABLED                     =       0x00000001;

   private  static  final   int MN_ERROR_THRESHOLD_BY_COUNT         =       0x00000004;
   private  static  final   int MN_ERROR_THRESHOLD_BY_INTERVAL      =       0x00000008;

   private  static  final   int MN_APP_AUTH_FAILURE                 =       0x00000010;
   private  static  final   int MN_APP_DESIGN_ERROR                 =       0x00000020;

   private  static  final   int MN_APP_VALIDATION_ERROR             =       0x00000040;
   private  static  final   int MN_APP_XML_ERROR                    =       0x00000080;
   private  static  final   int MN_APP_HTML_ERROR                   =       0x00000100;

   private  static  final   int MN_APP_RESPONSE_TIME                =       0x00000200;
   private  static  final   int MN_APP_REQUEST_QUEUE               =       0x00000400;

   private  static  final   int MN_BACKEND_AUTH_FAILURE             =       0x00000800;
   private  static  final   int MN_BACKEND_SERVER_DOWN_FAILURE      =       0x00001000;
   private  static  final   int MN_BACKEND_DATA_CONVERSION_FAILURE  =       0x00002000;
   private  static  final   int MN_BACKEND_QUERY_FAILURE            =       0x00004000;
   private  static  final   int MN_BACKEND_UPDATE_FAILURE           =       0x00008000;
   private  static  final   int MN_BACKEND_REQUEST_QUEUE           =       0x00010000;

   private          int         m_options = 0;
   private          String      m_name = "";
   private          int         m_errorThresholdCount = 0;
   private          int         m_errorThresholdInterval = 0;
   private          int         m_appAuthFailureCount = 0;
   private          int         m_appResponseTime = 0;
   private          int         m_appRequestQueue = 0;
   private          int         m_backendAuthFailureCount = 0;
   private          int         m_backendRequestQueue = 0;

   private static final int   MAX_RECIPIENT_NAME_LEN   = 255;

   /* package access on this so they may reference each other in fromXml */
   static final String   ms_NodeType            = "PSXRecipient";
}

