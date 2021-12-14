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

import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;


/**
 * The PSLogger class defines the log settings for an application or
 * server. Various levels of logging can be defined, which may be used
 * for a variety of tasks ranging from usage tracking to application
 * debugging.
 *
 * @see PSApplication#getLogger
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSLogger extends PSComponent
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
   public PSLogger(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, List parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Construct a logger object. No log settings are enabled by default.
    */
   public PSLogger()
   {
      super();
      m_options = 0;
   }
   
   /**
    * Is the logging of errors enabled?
    *
    * @return      <code>true</code> if this type of logging is enabled,
    *             <code>false</code> if it is disabled
    */
   public boolean isErrorLoggingEnabled()
   {
      return ((m_options & LOG_ERRORS) == LOG_ERRORS);
   }
   
   /**
    * Enable or disable logging errors.
    *
    * @param   enable   <code>true</code> to enable this type of logging,
    *                   <code>false</code> to disable it
    */
   public void setErrorLoggingEnabled(boolean enable)
   {
      if(enable)
         m_options |= LOG_ERRORS;
      else
         m_options &= ~LOG_ERRORS;
   }
   
   /**
    * Is the logging of server startup and shutdown events enabled?
    *
    * @return      <code>true</code> if this type of logging is enabled,
    *             <code>false</code> if it is disabled
    */
   public boolean isServerStartStopLoggingEnabled()
   {
      return ((m_options & LOG_SERVER_START_STOP) == LOG_SERVER_START_STOP);
   }
   
   /**
    * Enable or disable the logging of server startup and shutdown events.
    *
    * @param   enable   <code>true</code> to enable this type of logging,
    *                   <code>false</code> to disable it
    */
   public void setServerStartStopLoggingEnabled(boolean enable)
   {
      if(enable)
         m_options |= LOG_SERVER_START_STOP;
      else
         m_options &= ~LOG_SERVER_START_STOP;
   }
   
   /**
    * Is the logging of application startup and shutdown events enabled?
    *
    * @return      <code>true</code> if this type of logging is enabled,
    *             <code>false</code> if it is disabled
    */
   public boolean isAppStartStopLoggingEnabled()
   {
      return ((m_options & LOG_APP_START_STOP) == LOG_APP_START_STOP);
   }
   
   /**
    * Enable or disable the logging of application startup and shutdown
    * events.
    *
    * @param   enable   <code>true</code> to enable this type of logging,
    *                   <code>false</code> to disable it
    */
   public void setAppStartStopLoggingEnabled(boolean enable)
   {
      if(enable)
         m_options |= LOG_APP_START_STOP;
      else
         m_options &= ~LOG_APP_START_STOP;
   }
   
   /**
    * Is the logging of application statistics when the application
    * shuts down enabled?
    * <p>
    * The following information is logged:
    * <ul>
    * <li>the amount of time the application was up for</li>
    * <li>the number of events processed</li>
    * <li>the number of events left pending</li>
    * <li>the number of events failed</li>
    * <li>the number of query cache hits</li>
    * <li>the number of query cache misses</li>
    * <li>the minimum amount of time to process an event</li>
    * <li>the maximum amount of time to process an event</li>
    * <li>the average amount of time to process an event</li>
    * </ul>
    *
    * @return      <code>true</code> if this type of logging is enabled,
    *             <code>false</code> if it is disabled
    */
   public boolean isAppStatisticsLoggingEnabled()
   {
      return ((m_options & LOG_APP_STATISTICS) == LOG_APP_STATISTICS);
   }
   
   /**
    * Enable or disable the logging of application statistics when the
    * application shuts down enabled?
    * <p>
    * The following information is logged:
    * <ul>
    * <li>the amount of time the application was up for</li>
    * <li>the number of events processed</li>
    * <li>the number of events left pending</li>
    * <li>the number of query cache hits</li>
    * <li>the number of query cache misses</li>
    * <li>the minimum amount of time to process an event</li>
    * <li>the maximum amount of time to process an event</li>
    * <li>the average amount of time to process an event</li>
    * </ul>
    *
    * @param   enable   <code>true</code> to enable this type of logging,
    *                   <code>false</code> to disable it
    */
   public void setAppStatisticsLoggingEnabled(boolean enable)
   {
      if(enable)
         m_options |= LOG_APP_STATISTICS;
      else
         m_options &= ~LOG_APP_STATISTICS;
   }

   /**
    * Is the execution plan logged when an application is started?
    * <p>
    * All information deemed relevant for understanding what will be done
    * is logged. This includes, but is not limited to:
    * <ul>
    * <li>prepared statements</li>
    * <li>statistics used to build join plans</li>
    * <li>DTD information used to build XML document structures</li>
    * </ul>
    *
    * @return      <code>true</code> if this type of logging is enabled,
    *             <code>false</code> if it is disabled
    */
   public boolean isExecutionPlanLoggingEnabled()
   {
      return ((m_options & LOG_EXECUTION_PLAN) == LOG_EXECUTION_PLAN);
   }
   
   /**
    * Enable or disable the logging of
    * the execution plan logged when an application is started.
    * <p>
    * All information deemed relevant for understanding what will be done
    * is logged. This includes, but is not limited to:
    * <ul>
    * <li>prepared statements</li>
    * <li>statistics used to build join plans</li>
    * <li>DTD information used to build XML document structures</li>
    * </ul>
    *
    * @param   enable   <code>true</code> to enable this type of logging,
    *                   <code>false</code> to disable it
    */
   public void setExecutionPlanLoggingEnabled(boolean enable)
   {
      if(enable)
         m_options |= LOG_EXECUTION_PLAN;
      else
         m_options &= ~LOG_EXECUTION_PLAN;
   }

   /**
    * Is the logging of basic user activity enabled?
    * <p>
    * The following information is logged:
    * <ul>
    * <li>the session id of the user making this request</li>
    * <li>the host address of the requestor</li>
    * <li>the user name of the requestor (if authenticated)</li>
    * <li>the requested URL</li>
    * </ul>
    *
    * @return      <code>true</code> if this type of logging is enabled,
    *             <code>false</code> if it is disabled
    */
   public boolean isBasicUserActivityLoggingEnabled()
   {
      return ((m_options & LOG_BASIC_USER_ACTIVITY) == LOG_BASIC_USER_ACTIVITY);
   }
   
   /**
    * Enable or disable the logging of basic user activity.
    * <p>
    * The following information is logged:
    * <ul>
    * <li>the session id of the user making this request</li>
    * <li>the host address of the requestor</li>
    * <li>the user name of the requestor (if authenticated)</li>
    * <li>the requested URL</li>
    * </ul>
    *
    * @param   enable   <code>true</code> to enable this type of logging,
    *                   <code>false</code> to disable it
    */
   public void setBasicUserActivityLoggingEnabled(boolean enable)
   {
      if(enable)
         m_options |= LOG_BASIC_USER_ACTIVITY;
      else
         m_options &= ~LOG_BASIC_USER_ACTIVITY;
   }
   
   /**
    * Is the logging of detailed user activity enabled?
    * <p>
    * The following information is logged:
    * <ul>
    * <li>the session id of the user making this request</li>
    * <li>all data submitted with the request (POST body or XML file)</li>
    * <li>request statistics (rows processed, etc.)</li>
    * </ul>
    * All basic user activity logging information is also logged. Enabling
    * detailed logging automatically enables basic logging as well.
    *
    * @return      <code>true</code> if this type of logging is enabled,
    *             <code>false</code> if it is disabled
    * @see         #setBasicUserActivityLoggingEnabled
    */
   public boolean isDetailedUserActivityLoggingEnabled()
   {
      return ((m_options & LOG_DETAILED_USER_ACTIVITY) == LOG_DETAILED_USER_ACTIVITY);
   }

   /**
    * Enable or disable the logging of detailed user activity.
    * <p>
    * The following information is logged:
    * <ul>
    * <li>the session id of the user making this request</li>
    * <li>all data submitted with the request (POST body or XML file)</li>
    * <li>request statistics (rows processed, etc.)</li>
    * </ul>
    * All basic user activity logging information is also logged. Enabling
    * detailed logging automatically enables basic logging as well.
    *
    * @param   enable   <code>true</code> to enable this type of logging,
    *                   <code>false</code> to disable it
    */
   public void setDetailedUserActivityLoggingEnabled(boolean enable)
   {
      if(enable)
         m_options |= LOG_DETAILED_USER_ACTIVITY;
      else
         m_options &= ~LOG_DETAILED_USER_ACTIVITY;
   }

   /**
    * Is the logging of full user activity enabled?
    * <p>
    * The following information is logged:
    * <ul>
    * <li>the session id of the user making this request</li>
    * <li>any useful information encountered during the execution of the request</li>
    * </ul>
    * All basic and detailed user activity logging information is also
    * logged. Enabling full logging automatically enables basic
    * and detailed logging as well.
    *
    * @return      <code>true</code> if this type of logging is enabled,
    *             <code>false</code> if it is disabled
    *
    * @see         #setBasicUserActivityLoggingEnabled
    * @see         #setDetailedUserActivityLoggingEnabled
    */
   public boolean isFullUserActivityLoggingEnabled()
   {
      return ((m_options & LOG_FULL_USER_ACTIVITY) == LOG_FULL_USER_ACTIVITY);
   }

   /**
    * Enable or disable the logging of full user activity.
    * <p>
    * The following information is logged:
    * <ul>
    * <li>the session id of the user making this request</li>
    * <li>any useful information encountered during the execution of the request</li>
    * </ul>
    * All basic and detailed user activity logging information is also
    * logged. Enabling full logging automatically enables basic
    * and detailed logging as well.
    *
    * @param   enable   <code>true</code> to enable this type of logging,
    *                   <code>false</code> to disable it
    */
   public void setFullUserActivityLoggingEnabled(boolean enable)
   {
      if(enable)
         m_options |= LOG_FULL_USER_ACTIVITY;
      else
         m_options &= ~LOG_FULL_USER_ACTIVITY;
   }

   /**
    * Is the detection and logging of multiple handlers for a request
    * enabled?
    * <p>
    * When processing requests, the first data set matching the request
    * criteria handles it. If other data sets exist which are alse
    * interested in the request, they will be ignored. This may cause
    * confusion to end users and application designers. By enabling logging,
    * E2 will check subsequent applications and log a message for each
    * application also matching the request criteria. This should only be
    * used for debugging purposes as it may impact performance.
    * <p>
    * The following information is logged:
    * <ul>
    * <li>the name of each application/dataset matching the request
    *     criteria</li>
    * <li>the session id of the user making this request. This can be used to
    *     map back to the request when logging detailed user activity is
    *     enabled.</li>
    * </ul>
    *
    * @return      <code>true</code> if this type of logging is enabled,
    *             <code>false</code> if it is disabled
    */
   public boolean isMultipleHandlerLoggingEnabled()
   {
      return ((m_options & LOG_MULTIPLE_HANDLERS) == LOG_MULTIPLE_HANDLERS);
   }
   
   /**
    * Enable or disable the detection and logging of multiple handlers for
    * a request.
    * <p>
    * When processing requests, the first data set matching the request
    * criteria handles it. If other data sets exist which are alse
    * interested in the request, they will be ignored. This may cause
    * confusion to end users and application designers. By enabling logging,
    * E2 will check subsequent applications and log a message for each
    * application also matching the request criteria. This should only be
    * used for debugging purposes as it may impact performance.
    * <p>
    * The following information is logged:
    * <ul>
    * <li>the name of each application/dataset matching the request
    *     criteria</li>
    * <li>the session id of the user making this request. This can be used to
    *     map back to the request when logging detailed user activity is
    *     enabled.</li>
    * </ul>
    */
   public void setMultipleHandlerLoggingEnabled(boolean enable)
   {
      if(enable)
         m_options |= LOG_MULTIPLE_HANDLERS;
      else
         m_options &= ~LOG_MULTIPLE_HANDLERS;
   }
   
   
   /* **************  IPSComponent Interface Implementation ************** */
   
   /**
    * This method is called to create a PSXLogger XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *       PSXLogger defines the log settings for an application or server.
    *       Various levels of logging can be defined, which may be used for a
    *       variety of tasks ranging from usage tracking to application
    *       debugging.
    *    --&gt;
    *    &lt;!ELEMENT PSXLogger   EMPTY&gt;
    *
    *    &lt;!--
    *       which events cause logging?
    *
    *       logErrors - is the logging of errors enabled?
    *
    *       logServerStartStop - is the logging of server startup and
    *       shutdown events enabled?
    *
    *       logAppStartStop - is the logging of application startup and
    *       shutdown events enabled?
    *
    *       logAppStatistics - is the logging of application statistics
    *       when the application shuts down enabled?
    *
    *         logExecutionPlan - is the execution plan logged when an app
    *         is started?
    *
    *       logBasicUserActivity - is the logging of basic user activity
    *       enabled?
    *
    *       logDetailedUserActivity - is the logging of detailed user
    *       activity enabled?
    *
    *       logFullUserActivity - is the logging of full user
    *       activity enabled?
    *
    *       logMultipleHandlers - is the detection and logging of
    *       multiple handlers for a request enabled? When processing
    *       requests, the first data set matching the request criteria
    *       handles it. If other data sets exist which are alse interested
    *       in the request, they will be ignored. This may cause confusion
    *       to end users and application designers. By enabling logging, E2
    *       will check subsequent applications and log a message for each
    *       application also matching the request criteria. This should only
    *       be used for debugging purposes as it may impact performance.
    *    --&gt;
    *    &lt;!ATTLIST PSXLogger
    *       logErrors               %PSXIsEnabled      #OPTIONAL
    *       logServerStartStop      %PSXIsEnabled      #OPTIONAL
    *       logAppStartStop         %PSXIsEnabled      #OPTIONAL
    *       logAppStatistics         %PSXIsEnabled      #OPTIONAL
    *       logBasicUserActivity    %PSXIsEnabled      #OPTIONAL
    *       logDetailedUserActivity %PSXIsEnabled      #OPTIONAL
    *       logFullUserActivity      %PSXIsEnabled      #OPTIONAL
    *       logMultipleHandlers      %PSXIsEnabled      #OPTIONAL
    *    &gt;
    * </code></pre>
    *
    * @return     the newly created PSXLogger XML element node
    */
   public Element toXml(Document doc)
   {
      Element   root = doc.createElement(ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));
      
      root.setAttribute("logErrors",
         isErrorLoggingEnabled() ? "yes" : "no");
      
      root.setAttribute("logServerStartStop",
         isServerStartStopLoggingEnabled() ? "yes" : "no");
      
      root.setAttribute("logAppStartStop",
         isAppStartStopLoggingEnabled() ? "yes" : "no");
      
      root.setAttribute("logAppStatistics",
         isAppStatisticsLoggingEnabled() ? "yes" : "no");

      root.setAttribute("logExecutionPlan",
         isExecutionPlanLoggingEnabled() ? "yes" : "no");

      root.setAttribute("logBasicUserActivity",
         isBasicUserActivityLoggingEnabled() ? "yes" : "no");
      
      root.setAttribute("logDetailedUserActivity",
         isDetailedUserActivityLoggingEnabled() ? "yes" : "no");
      
      root.setAttribute("logFullUserActivity",
         isFullUserActivityLoggingEnabled() ? "yes" : "no");

      root.setAttribute("logMultipleHandlers",
         isMultipleHandlerLoggingEnabled() ? "yes" : "no");
      
      return root;
   }
   
   /**
    * This method is called to populate a PSLogger Java object
    * from a PSXLogger XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXLogger
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
      
      String sTemp = tree.getElementData("id");
      try {
         m_id = Integer.parseInt(sTemp);
      } catch (Exception e) {
         Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp) };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
      }
      
      /* get the logging options */
      m_options = 0;
      
      // all logging is off by default
      sTemp = tree.getElementData("logErrors");
      if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
         m_options |= LOG_ERRORS;

      sTemp = tree.getElementData("logServerStartStop");
      if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
         m_options |= LOG_SERVER_START_STOP;

      sTemp = tree.getElementData("logAppStartStop");
      if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
         m_options |= LOG_APP_START_STOP;

      sTemp = tree.getElementData("logExecutionPlan");
      if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
         m_options |= LOG_EXECUTION_PLAN;

      sTemp = tree.getElementData("logAppStatistics");
      if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
         m_options |= LOG_APP_STATISTICS;
      
      sTemp = tree.getElementData("logBasicUserActivity");
      if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
         m_options |= LOG_BASIC_USER_ACTIVITY;
      
      sTemp = tree.getElementData("logDetailedUserActivity");
      if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
         m_options |= LOG_DETAILED_USER_ACTIVITY;
      
      sTemp = tree.getElementData("logFullUserActivity");
      if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
         m_options |= LOG_FULL_USER_ACTIVITY;

      sTemp = tree.getElementData("logMultipleHandlers");
      if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
         m_options |= LOG_MULTIPLE_HANDLERS;
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

      // validate the options bits
      final int all_flags_compliment =
         ~(LOG_ERRORS | LOG_SERVER_START_STOP | LOG_APP_START_STOP |
         LOG_APP_STATISTICS | LOG_BASIC_USER_ACTIVITY |
         LOG_DETAILED_USER_ACTIVITY | LOG_FULL_USER_ACTIVITY | 
         LOG_MULTIPLE_HANDLERS | LOG_EXECUTION_PLAN);

         if (0 != (m_options & all_flags_compliment))
         {
            cxt.validationError(this,
               IPSObjectStoreErrors.LOGGER_OPTIONS_INVALID,
               "" + m_options);
         }

   }

   public boolean equals(Object o)
   {
      if (!(o instanceof PSLogger))
         return false;

      PSLogger other = (PSLogger)o;

      if (m_options != other.m_options)
         return false;

      return true;
   }
   
   /**
    * Generates hash code for the object.
    */   
   @Override
   public int hashCode()
   {
      return m_options;
   }

   /**
    * Operator to OR the provided logger to this logger. Nothing is changed if 
    * the provided logger is <code>null</code>.
    *
    * @param logger the logger we want to OR to this.
    */
   public void or(PSLogger logger)
   {
      if (logger != null)
         m_options |= logger.m_options;
   }

   private int m_options = 0;

   private static final int      LOG_ERRORS                  = 0x0001;
   private static final int      LOG_SERVER_START_STOP      = 0x0002;
   private static final int      LOG_APP_START_STOP         = 0x0004;
   private static final int      LOG_APP_STATISTICS         = 0x0008;
   private static final int      LOG_BASIC_USER_ACTIVITY      = 0x0010;
   private static final int      LOG_DETAILED_USER_ACTIVITY   = 0x0020;
   private static final int      LOG_FULL_USER_ACTIVITY      = 0x0040;
   private static final int      LOG_MULTIPLE_HANDLERS      = 0x0080;
   private static final int      LOG_EXECUTION_PLAN         = 0x0100;
   
   /* public access on this so they may reference each other in fromXml,
    * including legacy classes */
   public static final String   ms_NodeType            = "PSXLogger";
}

