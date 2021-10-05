/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.workflow;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.extension.PSExtensionException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.security.PSEncryptProperties;
import com.percussion.security.PSEncryptor;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSConsole;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowException;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.tools.PSURIEncoder;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSProperties;
import com.percussion.util.PSRelationshipUtils;
import com.percussion.util.PSUrlUtils;
import com.percussion.utils.exceptions.PSORMException;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.io.PathUtils;
import com.percussion.utils.jdbc.PSConnectionHelper;
import com.percussion.utils.string.PSStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.naming.NamingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * This class is a place holder for several global constants or variables that
 * need to be evaluated only once, and also contains static functions used
 * for workflow.
 */
@SuppressWarnings("unchecked")
public class PSWorkFlowUtils
{
   /**
    * Role delimiter (in role list)
    */
   public static final String ROLE_DELIMITER = ",";

   /**
    * Adhoc user list delimiter (in HTML parameter)
    */
   public static final String ADHOC_USER_LIST_DELIMITER = ";";

   /**
    * Adhoc user list separator.  The user list is passed as
    * &lt;rolename&gt;<code>ADHOC_USER_ROLE_TYPE_SEP</code>&lt;adhoctype&gt;
    * .
    */
   public static final String ADHOC_USER_ROLE_TYPE_SEP = ":";

   /**
    * Delimiter for workflow actions in database column
    * TRANSITIONS.TRANSITIONACTIONS
    */
   public static final String WORKFLOW_ACTION_DELIMITER = ",";

   /**
    * Delimiter for data base <CODE>String</CODE>s containing email recipients
    */
   public static final String EMAIL_STRING_DELIMITER = ",";

   /**
    * Separator for <CODE>String</CODE>s used to pass email recipients
    * to mail programs.
    */
   public static final String EMAIL_STRING_SEPARATOR = ", ";

   /**
    * Name of the properties file.
    */
   public static final String FILE_PROPERTIES = "rxworkflow.properties";

   /**
    * Default name for storing the new state id when transition takes place.
    */
   public static String DEFAULT_NEWSTATEID_NAME = "newstateid";

   /**
    * Default name for storing the transition id.
    */
   public static String DEFAULT_TRANSITIONID_NAME = "transitionid";
   
   /**
    * Default key for the default workflow property.
    */
   private static String DEFAULT_WORKFLOW_KEY = "DEFAULT_WORKFLOW";

   /**
    * Installer Options
    *   Table works as followed :
    *             Yes  No   Ignore    No Action
    *             ===  ==  ========  ===========
    *    CREATE    1    2     3          0
    *    ALTER     1    2     3          0
    *    DELETE    1    2     3          0
    *
    */
   public static final int CREATE_NONE = 0 ;
   public static final int CREATE_YES  = 1 ;
   public static final int CREATE_NO   = 2 ;
   public static final int CREATE_NOT  = 3 ;
   public static final int ALTER_NONE  = 0 ;
   public static final int ALTER_YES   = 1 ;
   public static final int ALTER_NO    = 2 ;
   public static final int ALTER_NOT   = 3 ;
   public static final int DELETE_NONE = 0 ;
   public static final int DELETE_YES  = 1 ;
   public static final int DELETE_NO   = 2 ;
   public static final int DELETE_NOT  = 3 ;

   /**
    * Error Messages ref write option to Disk.
    */
   public static final int SUCCESS            = 0 ;
   public static final int WRITE_ERRROR       = 1 ;
   public static final int SECURITY_FAILURE   = 2 ;
   public static final int FILE_ALREADY_EXIST = 3 ;
   public static final int FILE_NOT_FOUND     = 4 ;
   public static final int UNKNOWN_FAILURE    = 5 ;

   /**
    * Flags for writing to disk
    * CREATE_NEW_OVERWRITE = Create new file. Overwrite the old file.
    * CREATE_NEW_OLD = Create new, rename old file with ".old" ext if exists
    * APPEND_CREATE = Append to file. Create file if doesn't exist
    * APPEND = Append to existing file. Fail if doen't exist.
    *
    */
   public static final int CREATE_NEW_OVERWRITE = 0 ;
   public static final int CREATE_NEW_OLD       = 1 ;
   public static final int APPEND_CREATE        = 2 ;
   public static final int APPEND               = 3 ;

   public static final int TABLE_NO_ACTION      = 0 ;
   public static final int TABLE_CREATE_YES     = 1 ;
   public static final int TABLE_CREATE_NO      = 2 ;
   public static final int TABLE_ALTER_YES      = 4 ;
   public static final int TABLE_ALTER_NO       = 5 ;

   /**
    * Preset file name which the old data is stored in if system
    * fails to insert them back into the new tables.
    *
    */
   public static String PS_OLD_DATA_FILENAME =
      new String("ps_org_tbl_data.xml");
   public static String PS_OLD_DTD_FILENAME =
      new String("ps_org_tbl_def.xml");

   /**
    * error constants
    */
   public static final int ERROR_INVALID_NMBER_OF_PARAMETERS  = 7006;
   public static final int ERROR_INVALID_PARAMETER_TYPE       = 7003;
   public static final int ERROR_AUTHORIZATION_FAILURE        = 1203;

   /**
    * The string used as the start of field tokens.
    */
   public static final String FIELD_TOKEN_START = "${";

   /**
    * The string used as the end of field tokens.
    */
   public static final String FIELD_TOKEN_END = "}";

   /**
    * Constant for the name of the workflow property specifying the extension to use for notification link generation.
    */
   public static final String NOTIFICATION_LINK_GEN_EXIT_PROP = "MAIL_NOTIFY_LINK_GEN_EXIT";

   public static final String NOTIFICATION_ENABLE = "NOTIFICATION_ENABLE";


   /**
    * Default user name when workflow action is triggered by a server action.
    */
   public static final String RXSERVER = "rxserver";

   /**
    * String used to denote the name of the HTML page
    * for a navOn.
    */
   public static final String PERCNAVON = "percNavon.html";

   /**
    * Constant for the name of the workflow property specifying the format to
    * use for dates in messages.
    */
   public  static final String DATE_FORMAT_PROP = "NOTIFICATION_DATE_FORMAT";

   /**
    * Instantiation of empty properties. File will be loaded later.
    *
    */
   public static Properties properties = null;

   /**
    * Constant for the propery name which is used to create the
    * {@link #WORKFLOW_COMMENT_TOKEN}. The value of this constant is
    * "wfcomment".
    */
   public static final String WORKFLOW_COMMENT_PROP = "wfcomment";

   /**
    * Constant for the token name which is replaced by the user's comment for
    * the current transition. The value of this constant is "$wfcomment". The
    * runtime value of this token is the transition comment entered by the
    * user.
    */
   public static final String WORKFLOW_COMMENT_TOKEN = "$" +
           WORKFLOW_COMMENT_PROP;

   /**
    * Constant for the property name which is used to create the
    * {@link #WORKFLOW_LINK_TOKEN}. The value of this constant is
    * "wflink".
    */
   public static final String WORKFLOW_LINK_PROP = "wflink";

   /**
    * Constant for the token name which is replaced by the content URL for
    * the current item. The value of this constant is "$wflink". The
    * runtime value of this token is the content URL of the item being
    * transitioned.
    */
   public static final String WORKFLOW_LINK_TOKEN = "$"
           + WORKFLOW_LINK_PROP;

   /**
    * Tokens which can be used in the notification mail subject or body.
    * Currently only the tokens (<code>WORKFLOW_COMMENT_TOKEN</code>,
    * <code>WORKFLOW_LINK_TOKEN</code>) are supported.
    */
   public static final String[] MAIL_TOKENS = {WORKFLOW_COMMENT_TOKEN, WORKFLOW_LINK_TOKEN};

   /**
    *
    * AssignmentType:
    *  -1 - User is not listed in this content adhoc users context.
    * 0 - Not in workflow - Either the user is not in any role, or all of his
    *       roles  are not listed in the workflow definition
    * 1 - None - User is in a role, but the role is not assigned for this
    *       state
    * 2 - Reader - User is in a role assigned as Reader
    * 3 - Assignee - User is assigned to transition the document
    *   4 - Admin - User is an administrator in this Workflow.  Administrators
    *       have all rights
    *
    */
   public static final int ASSIGNMENT_TYPE_NOT_IN_ADHOC_USERS_CONTEXT = -1;
   public static final int ASSIGNMENT_TYPE_NOT_IN_WORKFLOW = 0;
   public static final int ASSIGNMENT_TYPE_NONE = 1;
   public static final int ASSIGNMENT_TYPE_READER = 2;
   public static final int ASSIGNMENT_TYPE_ASSIGNEE = 3;
   public static final int ASSIGNMENT_TYPE_ADMIN = 4;

   /**
    * HTML Parameter name that stores the current user's assignment type.
    * authenticateUser exit adds this to the list and performTransition may
    * modify, if required.
    *
    */
   public static final String ASSIGNMENT_TYPE_CURRENT_USER =
                              "assignmenttypecurrentuser";
   /**
    *
    * Document check out status values
    * 0 - Not checked-out by anybody
    * 1 - Checked out by current user
    * 2 - Checked out by some body else
    *
    */
   public static final int CHECKOUT_STATUS_NONE = 0;
   public static final int CHECKOUT_STATUS_CURRENT_USER = 1;
   public static final int CHECKOUT_STATUS_OTHER = 2;

   /**
    *
    * HTML Parameter name that stores the current document's checkout status.
    * authenticateUser exit adds this to the list and performTransition may
    * modify, if required.
    *
    */
   public static final String
         CHECKOUT_STATUS_CURRENT_DOCUMENT = "checkoutstatuscurrentdocument";
   /**
    *
    * HTML Parameter name that stores the current document's checkout user .
    * name authenticateUser exit adds this to the list and performTransition
    * may modify, if required.
    *
    */
   public static final String CHECKOUT_USER_NAME = "checkoutusername";

   /**
    *
    * HTML Parameter name that stores the content status history id after
    * writing the history.
    *
    */
   public static final String
          HTML_PARAM_CONTENTSTATUSHISTORYID = "contentstatushistoryid";

   /**
    *
    * Default HTML Parameter name used to access the transition comment if
    * a value is not assigned to property HTML_PARAM_TRANSITION_COMMENT
    * in the property file PSWorkFlowUtilsResources.properties.
    * This is used by the exits performTransition and updateHistory.
    */
   public static final String TRANSITION_COMMENT = "commenttext";

   /**
    * Ad-hoc options:
    *   0 - No Ad-Hoc
    * 1 - Ad-Hoc enabled
    * 2 - Anonymous Ad-Hoc
    */

   public static final int ADHOC_DISABLED = 0;
   public static final int ADHOC_ENABLED = 1;
   public static final int ADHOC_ANONYMOUS = 2;

   public static final String SNEWSTATEIDKEY = "sNewStateIDKey";

   public static final String ACTION_TRIGGER_NAME = "ACTION_TRIGGER_NAME";
   public static final String DEFAULT_ACTION_TRIGGER_NAME = "WFAction";

   public static final String REQUEST_NAME = "REQUEST_NAME";
   public static final String DEFAULT_REQUEST_NAME = "request";

   public static final String
                       ACTION_LIST_ELEMENT_NAME = "ACTION_LIST_ELEMENT_NAME";
   public static final String DEFAULT_ACTION_LIST_ELEMENT_NAME = "ActionList";

   public static final String ACTION_ELEMENT_NAME = "ACTION_ELEMENT_NAME";
   public static final String DEFAULT_ACTION_ELEMENT_NAME = "Action";

   public static final String
                          CONTENT_HISTORY_REQUEST = "CONTENT_HISTORY_REQUEST";
   public static final String HISTORY_ACTION_ELEMENT_NAME = "HistoryAction";
   public static final String VIEW_ACTION_ELEMENT_NAME = "ViewAction";
   public static final String EDIT_ACTION_ELEMENT_NAME = "EditAction";
   public static final String
                          CHECKINOUT_ACTION_ELEMENT_NAME = "CheckInOutAction";

   public static final String TRIGGER_CHECK_IN = "TRIGGER_CHECK_IN";
   public static final String DEFAULT_TRIGGER_CHECK_IN = "CheckIn";

   public static final String TRIGGER_CHECK_OUT = "TRIGGER_CHECK_OUT";
   public static final String DEFAULT_TRIGGER_CHECK_OUT = "CheckOut";

   public static final String TRIGGER_FORCE_CHECK_IN = "TRIGGER_FORCE_CHECK_IN";
   public static final String DEFAULT_TRIGGER_FORCE_CHECK_IN = "forcecheckin";

   public static final String CHECKINOUT_CONDITION_IGNORE = "ignore";
   public static final String CHECKINOUT_CONDITION_CHECKIN = "checkin";
   public static final String CHECKINOUT_CONDITION_CHECKOUT = "checkout";

   private volatile static ResourceBundle m_ResourceBundle = null;

   /** Name of the user global attribute for the email address */
   public static final String USER_EMAIL_ATTRIBUTE = "sys_email";
   
   /**
    * An optional property that stores the atribute name for the attribute 
    * that holds the user's email address.
    */
   public static final String USER_EMAIL_ATTRIBUTE_PROPERTY = 
      "MAIL_ATTRIBUTE_NAME";

   /**
    * Constant for the name of the entry that reperesents workflow's
    * name/value pair.
    */
   private static final String ENTRY_NAME = "workflow_config_base_dir";

   /**
    * Constant for the directory containing workflow configs.
    * Assumed to be relative to the Rx directory.
    */
   public static final String WORKFLOW_DIR = "rxconfig/Workflow";
   /**
    * Debug flag, set to <CODE>true</CODE> when running tests for which the
    * server is not used, else <CODE>false</CODE>.
    */
   public static boolean m_bTestWithoutServer = false;

   /**
    * Debug flag, when set to <CODE>true</CODE> trace message output is sent to
    * the PSConsole.
    */
   public static boolean m_bPSConsoleTraceMessages = false;

   /**
    * Debug flag, when set to <CODE>true</CODE> stack trace output is sent to
    * the PSConsole.
    */
   public static boolean m_bPSConsoleStackTrace = false;

   /**
    * Debug flag, when set to <CODE>true</CODE> trace message output is sent to
    * System.out.
    */
   public static boolean m_bSystemOutTraceMessages = false;

   /**
    * Debug flag, when set to <CODE>true</CODE> stack trace output is sent to
    *  System.out.
    */
   public static boolean m_bSystemOutStackTrace = false;

   /**
    * Used to maintain name of rxworkflow.properties file.
    */
   private static final String WORKFLOW_PROPS_FILE_NAME = "rxworkflow.properties";

   /**
    * Used to maintain location of rxworkflow.properties file.
    */
   private static final String WORKFLOW_PROPS_PATH = "rxconfig/Workflow";

   /**
    * Props that will be encrypted in the
    * rxworkflow.properties file.
    */
   private static String[] encryptProps = null;

   static
   {
      encryptProps = new String[] {
              "SMTP_PASSWORD"
      };
      // Encrypt rxworkflow.properties
      encryptWorkflowProps();

      loadProperties();

      /*
       * Initialize properties used for debugging.
       * TODO: use get/set methods that don't require restarting the server
       */
      String temp = properties.getProperty("TESTWITHOUTSERVER", "false");
      if (null != temp && temp.trim().equalsIgnoreCase("true"))
      {
         m_bTestWithoutServer = true;
      }

      temp = properties.getProperty("PSCONSOLETRACEMESSAGES", "false");
      if (null != temp && temp.trim().equalsIgnoreCase("true"))
      {
         m_bPSConsoleTraceMessages = true;
      }

      temp = properties.getProperty("PSCONSOLESTACKTRACE", "false");
      if (null != temp && temp.trim().equalsIgnoreCase("true"))
      {
         m_bPSConsoleStackTrace = true;
      }

      temp = properties.getProperty("SYSTEMOUTTRACEMESSAGES", "false");
      if (null != temp && temp.trim().equalsIgnoreCase("true"))
      {
         m_bSystemOutTraceMessages = true;
      }

      temp = properties.getProperty("SYSTEMOUTSTACKTRACE", "false");
      if (null != temp && temp.trim().equalsIgnoreCase("true"))
      {
         m_bSystemOutStackTrace = true;
      }
   }
   
   /**
    * Load the properties of the rxworkflow.properties file. 
    */
   private synchronized static void loadProperties()
   {
      try
      {
         properties = new Properties();
         
         //get the configuration
         File confFile = PSProperties.getConfig(ENTRY_NAME, FILE_PROPERTIES,
            PSServer.getRxFile(WORKFLOW_DIR));

         properties.load(new FileInputStream(confFile));
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

   /*  ********* Workflow Utility Methods ******** */

   /**
    * Outputs a message to trace file and PSConsole or output stream, depending
    * on workflow properties and application tracing options.
    * 
    * @param request <CODE>null</CODE> for no system trace file output else
    * the request context used to output message via
    * {@link #printWorkflowMessage(IPSRequestContext, String, boolean, boolean)}
    * .
    * The message will be added to the trace file only if tracing is enabled for
    * the application and for the exit tracing option.
    * 
    * @param message message to output
    * 
    * <ul>
    * Output is sent to:
    * <li>trace file if request is non <CODE>null</CODE> and application and
    * exit tracing is enabled</li>
    * <li>{@link System#out} if workflow property "SYSTEMOUTTRACEMESSAGES" is
    * "true"</li>
    * <li>{@link PSConsole} if workflow property "PSCONSOLETRACEMESSAGES" is
    * "true"</li>
    * </ul>
    */
   public static void printWorkflowMessage(IPSRequestContext request,
                                           String message)
   {
      // TODO: use get/set methods that don't require restarting the server
      boolean useSysOut = m_bSystemOutTraceMessages;
      boolean usePSConsole = m_bPSConsoleTraceMessages;
      printWorkflowMessage(request,
                           message,
                           useSysOut,
                           usePSConsole);
   }

   /**
    * Outputs a message to trace file and PSConsole or system output stream,
    * depending on input arguments and application tracing options.
    *
    * @param request      <CODE>null</CODE> for no system trace file output
    *                     else the request context used to output message via
    *                     {@link IPSRequestContext#printTraceMessage(String)}.
    *                     The message will be added to the trace file only if
    *                     tracing is enabled for the application and for the
    *                     exit tracing option.
    *
    * @param message      message to output
    *
    * @param useSysOut    if <CODE>true</CODE> send message to
    *                     {@link System#out} else don't
    *
    * @param usePSConsole if <CODE>true</CODE>, send message to
    *                     {@link PSConsole} else don't.
    *
    */
   public static void printWorkflowMessage(IPSRequestContext request,
                                           String message,
                                           boolean useSysOut,
                                           boolean usePSConsole)
   {
      if (null != request)
      {
         request.printTraceMessage(message);
      }
      if (usePSConsole)
      {
         PSConsole.printMsg("Workflow", message);
      }
      if (useSysOut)
      {
         System.out.println(message);
      }
   }

   /**
    * Prints a stack trace to trace file and PSConsole or system output stream,
    * depending on workflow properties and application tracing options.
    * 
    * @param request <CODE>null</CODE> for no system trace file output else
    * the request context used to output message via
    * {@link #printWorkflowMessage(IPSRequestContext, String, boolean, boolean)}.
    * The message will be added to the trace file only if tracing is enabled for
    * the application and for the exit tracing option.
    * 
    * @param throwable exception for which stack trace should be output.
    * 
    * <ul>
    * Output is sent to:
    * <li>trace file if request is non <CODE>null</CODE> and application and
    * exit tracing is enabled</li>
    * <li>{@link System#out} if workflow property "SYSTEMOUTSTACKTRACE" is
    * "true"</li>
    * <li>{@link PSConsole} if workflow property "PSCONSOLESTACKTRACE" is
    * "true"</li>
    * </ul>
    */

   public static void printWorkflowException(IPSRequestContext request,
                                             Throwable throwable)
   {
      // TODO: use get/set methods that don't require restarting the server
      boolean useSysOut = m_bSystemOutStackTrace;
      boolean usePSConsole = m_bPSConsoleStackTrace;
      printWorkflowException(request,
                             throwable,
                             useSysOut,
                             usePSConsole);
   }

   /**
    * Prints a stack trace to trace file and PSConsole or output stream,
    * depending on input arguments and application tracing options.
    *
    * @param request      the request context for the extension used for
    *                     if <CODE>null</CODE>, do not send stack trace to
    *                     application trace file.
    *
    * @param throwable    exception for which stack trace should be output.
    *                     Also output to tracing
    *                     file if request is not <CODE>null</CODE>, and
    *                     extension execution tracing is on in the application.
    *
    * @param useSysOut    if <CODE>true</CODE> send message to
    *                     {@link System#out} else don't
    *
    * @param usePSConsole if <CODE>true</CODE>, send message to
    *                     {@link PSConsole} else don't.
    */
   public static void printWorkflowException(IPSRequestContext request,
                                             Throwable throwable,
                                             boolean useSysOut,
                                             boolean usePSConsole)
   {
      if (throwable == null)
      {
         throw new IllegalArgumentException("throwable cannot be null");
      }

      String stackTrace = stackTraceString(throwable);

      if (null != request)
      {
         request.printTraceMessage(stackTrace);
      }
      if (usePSConsole)
      {
         PSConsole.printMsg("Workflow", throwable);
      }
      if (useSysOut)
      {
         System.out.println(stackTrace);
      }
   }

   /**
    * Helper function to compare two role lists
    * @param assignmentTypeList - ArrayList containing assignment types as
    *                             Integers
    *
    * @param roleList -           first role list as ArrayList;
    *
    * @param sRoleList -          the second role list as comma separated list
    *
    * @return                     the assignment type - one of the values
    *                             defined in this file (PSWorkFlowUtils.java)
    */
   public static int compareRoleList(ArrayList assignmentTypeList,
                                     ArrayList roleList,
                                     String sRoleList)
   {
      StringTokenizer sTokenizer = new StringTokenizer(sRoleList,
                                                       ROLE_DELIMITER);
      String sRole = "";
      String sToken = "";
      boolean bPresent = false;
      int nAssignmentType = PSWorkFlowUtils.ASSIGNMENT_TYPE_NONE;
      Integer index;
      while(sTokenizer.hasMoreElements())
      {
         sToken = sTokenizer.nextToken().trim();
         for(int i=0; i<roleList.size(); i++)
         {
            sRole = roleList.get(i).toString();
            if (sRole.trim().equalsIgnoreCase(sToken))
            {
               if (false == bPresent)
                  bPresent = true;
               index = (Integer)assignmentTypeList.get(i);
               if (index.intValue() > nAssignmentType)
                  nAssignmentType = index.intValue();
            }
         }
      }
      return nAssignmentType;
   }

   /**
    * Determine if two role lists have a common role.
    *
    * @param roleList -           first role list as <CODE>List</CODE>;
    *
    * @param sRoleList -          the second role list as comma separated list
    *
    * @return                     <CODE>true</CODE> if there is a common role,
    *                             else <CODE>false</CODE>
    */
   public static boolean compareRoleList( List roleList,
                                          String sRoleList)
   {
      StringTokenizer sTokenizer = new StringTokenizer(sRoleList,
                                                       ROLE_DELIMITER);
      String sRole = "";
      String sToken = "";
      boolean bFound = false;
      while(sTokenizer.hasMoreElements())
      {
         sToken = sTokenizer.nextToken().trim();
         for(int i=0; i<roleList.size(); i++)
         {
            sRole = roleList.get(i).toString();
            if (sRole.trim().equalsIgnoreCase(sToken))
            {
               bFound = true;
               break;
            }
         }
      }
      return bFound;
   }

   /**
    * Determine whether the user is an administrator. <BR>
    * The user is an administrator if the Workflow admin name is the user's
    * name or is one of the user's roles.
    *
    * @param sAdminName  The name of the workflow administrator
    * @param sUserName   The user's name, cannot be <CODE>null</CODE>
    * @param sRoleList   A comma-delimited list of the user's roles.
    * @return            <CODE>true</CODE> if the user is an administrator,
    *                    <CODE>false</CODE> if not.
    */
   public static boolean isAdmin(String sAdminName,
                                 String sUserName,
                                 String sRoleList)
   {
      // If the admin name is null or empty, the person is not an admin
      if (null == sAdminName || 0 == sAdminName.trim().length())
      {
         return (false);
      }

      sAdminName = sAdminName.trim();

      if (null == sUserName || 0 == sUserName.trim().length())
      {
         throw new IllegalArgumentException("User name cannot be empty.");
      }

      /*
       * If the user name is the same as the Workflow admin name,
       * the user is an administrator
       */
      if (sUserName.trim().equalsIgnoreCase(sAdminName))
      {
         return (true);
      }


      if (null == sRoleList || 0 == sRoleList.trim().length())
      {
         return (false);
      }

      /*
       * If the Workflow admin name is one of the user's roles,
       * the user is an administrator.
       */

      StringTokenizer sTokenizer = new StringTokenizer(sRoleList,
                                                       ROLE_DELIMITER);
      String sToken = "";
      boolean bAdmin = false;
      while(sTokenizer.hasMoreElements())
      {
         sToken = sTokenizer.nextToken().trim();
         if (sAdminName.equalsIgnoreCase(sToken))
         {
            bAdmin = true;
            break;
         }
      }

      return (bAdmin);
   }


   /**
    * Takes the assignment type based on the workflow rules and modifies it
    * based on the user's login community and item's community. If they are same
    * then returns ASSIGNMENT_TYPE_NONE if input assignment type is
    * ASSIGNMENT_TYPE_NONE otherwise returns ASSIGNMENT_TYPE_READER if input
    * assignment type is equal or greater than ASSIGNMENT_TYPE_READER.
    * Returns assignment type un changed if items community and users community
    * are same.
    *
    * @param assignmentType  assignment type from workflow rules
    * @param itemCommunity   items community
    * @param usersCommunity   users community
    * @return  assignmentType
    * @throws            IllegalArgumentException if the user
    *                    name is <CODE>null</CODE>or empty.
    */
   public static int modifyAssignmentType(int assignmentType,
                                 int itemCommunity,
                                 int usersCommunity)
   {
      if((itemCommunity != usersCommunity) &&
         (assignmentType > ASSIGNMENT_TYPE_READER))
         assignmentType = ASSIGNMENT_TYPE_READER;

      return assignmentType;
   }


   /**
    * Helper function that returns user name after last comma.
    * This is useful when we need to extract actual user name out of the user
    * name that is obtainable by user contex/username. This has of the form:
    * host1,host2,host3,host4,username.
    *
    * @deprecated This method was written for the IP Security Provider,
    * which is no longer supported.
    *
    * @author Rammohan Vangapalli
    * @version 1.0
    * @since 2.0
    *
    */
   public static String filterUserName(String sUserName)
   {
      /*
      Vitaly, Dec 9 2002: made this method a 'noop' for the following reasons:
      1. The IP based authentication method it was designed for is now obsolete
      2. It screws up a user name, which has one or more commas in it,
         ie: "Lee, Christo" -> would be returned by this method as "Lee"
      3. This method is used by several workflow classes, so the 'noop' approach
         minimizes the number of changes needed to fix the above problem
      4. Fixes: RX-02-11-0151
      */
      return sUserName;
   }

   /**
    * Gets the descriptive comment for this transition from the HTML Parameter
    * hash map.
    *
    * @param   htmlParams hash map containing the HTML parameters for this
    *          request
    *
    * @return  the descriptive comment for this transition or
    *          <CODE>null</CODE> if there is no descriptive comment in the
    *          HTML Parameter hash map, or the comment is empty or consists
    *          entirely of whitespace.
    *          May not be more than 255 characters.
    */
   public static String getTransitionCommentFromHTMLParams(Map<String,Object> htmlParams)
   {
      String paramName = getTransitionCommentParamName();
      
      String transitionComment = (String)htmlParams.get(paramName);

      if (null != transitionComment)
      {
         transitionComment = transitionComment.trim();
         if (0 == transitionComment.length())
         {
            transitionComment = null;
         }
      }

      return transitionComment;
   }

   /**
    * Get the name of the html param to use for transition comments.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   private static String getTransitionCommentParamName()
   {
      return PSWorkFlowUtils.properties.getProperty(
         "HTML_PARAM_TRANSITION_COMMENT", PSWorkFlowUtils.TRANSITION_COMMENT);
   }
   
   /**
    * Sets the descriptive comment for this transition in the HTML Parameter
    * hash map.
    *
    * @param comment The comment, ignored if <code>null</code>, may be empty, 
    * not more than 255 characters.
    * @param params map containing the HTML parameters for this request, may not
    * be <code>null</code>.
    */
   public static void setTransitionCommentInHTMLParams(String comment, 
      Map<String, Object> params)
   {
      if (comment != null && comment.length() > 255)
         throw new IllegalArgumentException(
            "comment may not exceed 255 characters");
      
      if (params == null)
         throw new IllegalArgumentException("params may not be null");
      
      if (comment != null)
         params.put(getTransitionCommentParamName(), comment);
   }   

   /**
    * All the error and other messages are stored in a resource file. This
    * static method returns (instantiates if required) the resource bundle.
    */
   public static ResourceBundle getResourceBundle()
   {
      /*
       * load the resources first. this will throw an exception if we can't
       * find them
       */
      if (null == m_ResourceBundle)
         m_ResourceBundle =
               ResourceBundle.getBundle( "com.percussion.workflow."
            + "PSWorkFlowUtilsResources", Locale.getDefault());

      return m_ResourceBundle;
   }

   /**
    * Gets an iterator over all the installed workflow action extensions.
    *
    * @return   A non-null Iterator over 0 or more non-null PSExtensionRefs of
    *           workflow action extensions (extensions that implement
    *           <CODE>IPSWorkflowAction</CODE>
    * @throws   PSExtensionException if an error occurs
    */
   public static Iterator getWorkflowActionExtensionRefs()
      throws PSExtensionException
   {
      return  PSServer.getExtensionManager(null).
            getExtensionNames("Java",            // Handler
                              null,              // Context
                              // Interface
                              "com.percussion.extension.IPSWorkflowAction",
                              null);             // Extension Name

   }

   /**
    * Convenience method that calls
    * {@link #getContentItemURL(int, int, IPSRequestContext, boolean)
    * getContentItemURL(contentID, revisionID, request, false)}
    */
   static public String getContentItemURL(int contentID,
                                          int revisionID,
                                          IPSRequestContext request)
      throws   PSAuthenticationFailedException,
      PSInternalRequestCallException,
      PSAuthorizationException
   {
      return getContentItemURL(contentID, revisionID, request, false);
   }

   /**
    * Gets the URL to preview the content item.
    * Starting from version 4.5 the url generated will be the search url which
    * when clicked bring the search results with only that content item. Earlier
    * clicking the url was opening content editor.
    *
    * @param contentID          ID of the content item
    * @param revisionID         revision ID of the content item
    * @param request  connection to back-end database
    * @param overrideCommunity  If <code>true</code>, the community id of the
    *                           item is appended using the
    *                           sys_overridecommunityid parameter, which will
    *                           switch the user to the item's community when
    *                           they use the url.  Otherwise, it is not.
    *
    * @return Unescaped URL String to preview the content item,
    * <code>null</code> if it could not be generated
    *
    * @throws IllegalArgumentException if the request object is null, the
    *  content ID is invalid, or the revision ID is invalid
    * @throws PSAuthenticationFailedException if thrown from internal request
    * @throws PSInternalRequestCallException if thrown from internal request
    * @throws PSAuthorizationException if thrown from internal request
    */

   static public String getContentItemURL(int contentID,
                                          int revisionID,
                                          IPSRequestContext request,
                                          boolean overrideCommunity)
      throws   PSAuthenticationFailedException,
      PSInternalRequestCallException,
      PSAuthorizationException
   {
      if (request == null)
      {
         throw new IllegalArgumentException(
            "Request object must not be empty");
      }


      String contentid = "";
      String editURLString = null;
      String host = null;
      String portString = null;
      Integer port = null;
      URL editURL = null;
      try
      {
         contentid = Integer.toString(contentID);
      }
      catch(Exception e)
      {
         throw new IllegalArgumentException(
            "Invalid contentID value supplied");
      }

      try
      {
         Integer.toString(revisionID);
      }
      catch(Exception e)
      {
         throw new IllegalArgumentException(
            "Invalid revisionID value supplied");
      }

      host = PSWorkFlowUtils.properties.getProperty(
         "RX_SERVER_HOST_NOTIFICATION");

      if (null != host)
      {
         host = host.trim();

         if (0 == host.length())
         {
            host = null;
         }
      }
      portString = PSWorkFlowUtils.properties.getProperty(
         "RX_SERVER_PORT_NOTIFICATION");

      try
      {
         if (null != portString)
         {
            port = Integer.valueOf(portString);
         }
      }
      catch (NumberFormatException e)
      {
         //We ignore port string if we can't parse it
         port = null;
      }



      if(PSServer.isRequestBehindProxy(null)){
         port = Integer.valueOf(PSServer.getProperty("proxyPort", ""+port));
         host = PSServer.getProperty("publicCmsHostname",host);
      }

      String linkUrlComponent = PSWorkFlowUtils.properties
            .getProperty("MAIL_NOTIFY_LINK_COMPONENT_NAME");
      if (linkUrlComponent != null)
      {
         linkUrlComponent = linkUrlComponent.trim();
         if (linkUrlComponent.length() < 1)
            linkUrlComponent = null;
      }
      linkUrlComponent = (linkUrlComponent == null)
            ? "ca_search"
            : linkUrlComponent; 

      try
      {
         HashMap newHtmlParams;
         IPSInternalRequest iReq;
         Document doc;

         // get content status if adding community
         String communityId = null;
         if (overrideCommunity)
         {
            newHtmlParams = new HashMap();
            newHtmlParams.put(IPSHtmlParameters.SYS_CONTENTID, contentid);
            String resource = IPSConstants.EDITOR_SUPPORT_APPNAME +
               "/contentstatus";
            iReq = request.getInternalRequest(resource,
               newHtmlParams, false);
            if (iReq != null)
            {
               doc = iReq.getResultDoc();
               Element rootStatusEl = doc.getDocumentElement();
               if (rootStatusEl != null)
               {
                  Element statusEl = (Element)rootStatusEl.getFirstChild();
                  if (statusEl != null)
                     communityId = statusEl.getAttribute("communityId");
               }
            }
         }

         //search component is currently using "statusid" for "sys_contentid"
         //parameter
         newHtmlParams = new HashMap();
         newHtmlParams.put("sys_contentid",contentid);
         newHtmlParams.put("sys_componentname",linkUrlComponent);
         newHtmlParams.put("sys_pagename","ca_search");
         newHtmlParams.put("sys_sortparam","title");
         if (communityId != null)
            newHtmlParams.put(IPSHtmlParameters.SYS_OVERRIDE_COMMUNITYID,
               communityId);

         iReq = request.getInternalRequest(
            "sys_ComponentSupport/componentabslink", newHtmlParams, false);

         doc = null;
         if (iReq != null)
         {
            doc = iReq.getResultDoc();
            editURLString = "";
            NodeList nl = null;
            nl = doc.getElementsByTagName("url");
            if(null != nl && nl.getLength() >= 1)
            {
               Element elem = (Element)nl.item(0);
               if(null != elem)
               {
                  Node temp = elem.getFirstChild();
                  if(null != temp && Node.TEXT_NODE == temp.getNodeType())
                  {
                     editURLString = ((Text)temp).getData().trim();
                  }
               }
            }
            editURL = PSUrlUtils.createUrl(host,
                                           port,
                                           editURLString,
                                           null, // queryParams,
                                           null, // anchor
                                           request);

            //If we have to use SSL modify the URL to use https
             if(PSServer.isRequestBehindProxy(null)){
                 String proxyScheme  = PSServer.getProperty("proxyScheme",editURL.getProtocol());
                         editURL = new URL(proxyScheme, editURL.getHost(), editURL.getPort(),
                                 editURL.getFile());

             }else{
                 if (isSSLEnabledForNotification())
                 {
                     editURL = new URL("https", editURL.getHost(), editURL.getPort(),
                             editURL.getFile());
                 }
             }


            editURLString = editURL.toString();
            editURLString = PSURIEncoder.unescape(editURLString);
            editURLString = editURLString.replace(' ', '+');
            
            return editURLString;
         }
      }
      catch (MalformedURLException e)
      {
         // Ignore and return a null url
      }

      return null;
   }

   /*  ********* String Methods ********* */

   /**
    * Create a list of the substrings of a string, using a specified delimiter.
    * Surrounding whitespace is trimmed from each substring. Consecutive
    * occurrences of the delimiter are ignored.
    *
    * @param inString   the string to be tokenized (divided into substrings
    *                   separated by a delimiter.)
    * @param delimeter  The delimeter used to tokenize the string.
    *                   if <CODE>null</CODE> whitespace is used as a delimeter
    * @return           the list of substrings; will contain no elements if
    *                   the <CODE>inString</CODE> consists entirely of
    *                   delimiters.
    * @throws           IllegalArgumentException if <CODE>inString</CODE> is
    *                   <CODE>null</CODE> or empty
    */
   public static List<String> tokenizeString(String inString,
                                     String delimeter )
   {
      if ( null == inString || inString.length() == 0 )
      {
         throw new IllegalArgumentException("Input string is null or empty.");
      }
      String token = "";
      ArrayList<String> l = new ArrayList<String>();
      StringTokenizer toker;
      if ( null == delimeter)
      {
         // delimiter is white space
         toker = new StringTokenizer( inString);
      }
      else
      {
         toker = new StringTokenizer( inString, delimeter );
      }
      while ( toker.hasMoreTokens())
      {
         token = toker.nextToken().trim();
         l.add(token);
      }
      return l;
   }

   /**
    * Creates a <CODE>String</CODE> for the stack trace of a
    * <CODE>throwable</CODE>.
    *
    * @param throwable Throwable for which stack trace is desired
    *                  never <CODE>null</CODE>
    * @return          <CODE>String</CODE> containing the stack trace
    * @throws IllegalArgumentException if the input is <CODE>null</CODE>
    */
   public static String stackTraceString(Throwable throwable)
   {
      if (null == throwable )
      {
          throw new IllegalArgumentException(
             "Throwable may not be null or empty.");
      }
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      throwable.printStackTrace(pw);
      return sw.toString();
   }

   /*  ********* Date and Calender Methods ********* */
   /**
    * Produces a string giving date information down to the millisecond in the
    * format mm/dd/yyyy hh:mm:ss:milli.
    *
    * @param date  date to be turned into a string
    * @return      string giving date information down to the millisecond
    */
   public static String DateString(Date date)
   {
      if (null == date)
      {
         return "";
      }

      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      return DateString(calendar);
   }

   /**
    * Produces a string giving date information down to the millisecond in the
    * format mm/dd/yyyy hh:mm:ss:milli.
    *
    * @param calendar  calendar specifying date to be turned into a string
    * @return          string giving date information down to the millisecond
    */
   public static String DateString(Calendar calendar)
   {
      if (null == calendar)
      {
          return "";
      }
      StringBuilder buf = new StringBuilder();
      buf.append((calendar.get(Calendar.MONTH) + 1)+ "/" );
      buf.append(calendar.get(Calendar.DAY_OF_MONTH)  + "/");
      buf.append(calendar.get(Calendar.YEAR) + " ");
      buf.append(calendar.get(Calendar.HOUR) + ":");
      buf.append(calendar.get(Calendar.MINUTE) + ":");
      buf.append(calendar.get(Calendar.SECOND) + ":");
      buf.append(calendar.get(Calendar.MILLISECOND));
      return buf.toString();
   }

   /**
    * Convert a Timestamp to a <CODE>java.sql.Date</CODE>, passing through a
    * <CODE>null</CODE>  value.
    *
    * @param timestamp  The time value to convert to a
    *                   <CODE>java.sql.Date</CODE> or <CODE>null</CODE> if no
    *                   corresponding time exists.
    * @return  corresponding <CODE>java.sql.Date</CODE> or <CODE>null</CODE>
    */
   public static Date sqlDateFromTimestamp(Timestamp timestamp)
   {
      return (null == timestamp )  ? null : new Date(timestamp.getTime());
   }


   /**
    * Convert a <CODE>java.util.Date</CODE> to aTimestamp to passing through a
    * <CODE>null</CODE>  value.
    *
    * @param date  The<CODE>java.util.Date</CODE to convert to a Timestamp
    *              or <CODE>null</CODE> if the date is <CODE>null</CODE>
    * @return      corresponding Timestamp or <CODE>null</CODE>
    */
   public static Timestamp timestampFromDate(java.util.Date date)
   {
      return (null == date )  ? null : new Timestamp(date.getTime());
   }

   /**
    * Convert a Calendar time to a <CODE>java.sql.Date</CODE>, passing through
    * a <CODE>null</CODE> value.
    *
    * @param calendar  The calendar with the time value to convert to a
    *                   <CODE>java.sql.Date</CODE> or <CODE>null</CODE> if no
    *                   corresponding time exists.
    * @return  corresponding <CODE>java.sql.Date</CODE> or <CODE>null</CODE>
    */
   public static Date sqlDateFromCalendar(Calendar calendar)
   {
         /*
          * This code uses the constructor uses the constructor
          * java.sql.Date(long Date).  Calendar.getTime() is a java.util.Date
          *  and java.util.Date.getTime() is a long
          */
      return (null == calendar )  ? null :
            new Date(calendar.getTime().getTime());
   }

   /**
    * Return a Calendar with time equal to to a <CODE>java.util.Date</CODE>,
    * passing through a <CODE>null</CODE> a value, possibly making use of
    * an existing Calendar
    *
    * @param calendar  The calendar for which the time value to convert to a
    *                   <CODE>java.util.Date</CODE> or <CODE>null</CODE> if no
    * @param date      The <CODE>java.util.Date</CODE> to which the Calendar
    *                  time should be set,  or <CODE>null</CODE> if no
    *                   corresponding time exists.
    * @return          <CODE>Calendar</CODE> with time set to the given
    *                  <CODE>java.util.Date</CODE> or <CODE>null</CODE>
    */
   public static Calendar calendarFromDate(Calendar calendar,
                                           java.util.Date date)
   {
      if (null == date)
      {
         return null;
      }

      if (null == calendar)
      {
         calendar =  Calendar.getInstance();
      }

      calendar.setTime(date);
      return calendar;
   }


   /**
    * Find the difference in milliseconds between the time values of two
    * calenders.
    *
    * @param calendar1  calendar with time to subtract from,
    *                   never <CODE>null</CODE>
    * @param calendar2  calendar with time to be subtracted
    *                   never <CODE>null</CODE>
    * @return  difference in milliseconds between the time values
    */
   public static long timeDiffMillis(Calendar calendar1,
                                     Calendar calendar2)
   {
      long time1 = 0;
      long time2 = 0;

      if (null == calendar1)
      {
         throw new IllegalArgumentException("Calender may not be null.");
      }

      if (null == calendar2)
      {
         throw new IllegalArgumentException("Calender may not be null.");
      }

      time1 = calendar1.getTime().getTime();
      time2 =  calendar2.getTime().getTime();

      return time1 - time2;
   }

   /**
    * Find the difference in seconds between the time values of two
    * calenders.
    *
    * @param calendar1  calendar with time to subtract from,
    *                   never <CODE>null</CODE>
    * @param calendar2  calendar with time to be subtracted
    *                   never <CODE>null</CODE>
    * @return  difference in seconds between the time values
    */
   public static long timeDiffSecs(Calendar calendar1,
                                   Calendar calendar2)
   {

      if (null == calendar1)
      {
         throw new IllegalArgumentException("Calender may not be null.");
      }

      if (null == calendar2)
      {
         throw new IllegalArgumentException("Calender may not be null.");
      }

      return timeDiffMillis(calendar1, calendar2)/1000;
   }


   /**
    * Find the difference in seconds between the time values of two
    * calenders.
    *
    * @param date1  time to subtract from, never <CODE>null</CODE>
    * @param date2  time to be subtracted, never <CODE>null</CODE>
    * @return  difference in seconds between the time values
    */
   public static long timeDiffSecs(java.util.Date date1,
                                   java.util.Date date2)
   {
      long time1 = 0;
      long time2 = 0;

      if (null == date1)
      {
         throw new IllegalArgumentException("Date may not be null.");
      }

      if (null == date2)
      {
         throw new IllegalArgumentException("Date may not be null.");
      }

      time1 = date1.getTime();
      time2 = date2.getTime();

      return (time1 - time2)/1000;
   }

   /**
    * Return a Calendar with time equal to to a <CODE>java.util.Date</CODE>,
    * incremented by a time interval (in minutes), possibly making use of
    * an existing Calendar
    *
    * @param calendar  The calendar for which the time value to convert to a
    *                   <CODE>java.util.Date</CODE> or <CODE>null</CODE> if no
    * @param date      The <CODE>java.util.Date</CODE> to which the Calendar
    *                  time should be set. may not be <CODE>null</CODE>
    * @param interval  Time interval in minutes by which date should be
    *                  incremented.
    * @return          <CODE>Calendar</CODE> with the desired time set.
    * @throws IllegalArgumentException if the date is <CODE>null</CODE>.
    *
    */
   public static Calendar incrementCalendar(Calendar calendar,
                                            java.util.Date date,
                                            int interval)
   {
      if (null == date)
      {
         throw new IllegalArgumentException("The date may not be null.)");
      }

      calendar = calendarFromDate(calendar, date);
      calendar.add(Calendar.MINUTE, interval);

      return calendar;
   }


   /**
    * Return a date with time equal to to a <CODE>java.sql.Date</CODE>,
    * incremented by a time interval (in minutes)
    *
    * @param date      The <CODE>java.sql.Date</CODE> to which the Calendar
    *                  time should be set. may not be <CODE>null</CODE>
    * @param interval  Time interval in minutes by which date should be
    *                  incremented.
    * @return          <CODE>java.sql.Date</CODE> with the desired time set.
    * @throws IllegalArgumentException if the date is <CODE>null</CODE>.
    *
    */
   public static Date incrementDate(Date date,
                                    int interval)
   {
      if (null == date)
      {
         throw new IllegalArgumentException("The date may not be null.");
      }
      Calendar calendar = null;

      calendar = incrementCalendar(calendar, date, interval);

      return  sqlDateFromCalendar(calendar);
   }


   /**
    * Return a date with time equal to to a <CODE>java.util.Date</CODE>,
    * incremented by a time interval (in minutes)
    *
    * @param date      The <CODE>java.util.Date</CODE> to which the Calendar
    *                  time should be set. may not be <CODE>null</CODE>
    * @param interval  Time interval in minutes by which date should be
    *                  incremented.
    * @return          <CODE>java.util.Date</CODE> with the desired time set.
    * @throws IllegalArgumentException if the date is <CODE>null</CODE>.
    *
    */
   public static java.util.Date incrementDate(java.util.Date date,
                                              int interval)
   {
      if (null == date)
      {
         throw new IllegalArgumentException("The date may not be null.");
      }
      Calendar calendar = null;

      calendar = incrementCalendar(calendar, date, interval);

      return  calendar.getTime();
   }

   /**
    * Trim a non-null string, and return <CODE>null</CODE> if the trimmed
    * string is empty.
    *
    * @param string  string to be trimmed. May be <CODE>null</CODE>
    *
    * @return   <CODE>null</CODE> if the input string is <CODE>null</CODE>,
    *           or is empty after trimming.
    *           Otherwise, return the trimmed string.
    */
   public static String trimmedOrNullString(String string)
   {
      String trimmedString = (null == string) ? null : string.trim();

      return ((null == trimmedString ) || trimmedString.length() == 0)
              ? null : trimmedString;
   }

   /**
    * Trim a non-null string, and return an empty string if the input string is
    * empty.
    *
    * @param string  string to be trimmed. May be <CODE>null</CODE>
    *
    * @return   an empty string ("") if the input string is <CODE>null</CODE>
    */
   public static String trimmedOrEmptyString(String string)
   {
      if (null == string)
      {
         return "";
      }
      else
      {
         return string.trim();
      }
   }

   /*  ********* List Methods ********* */
   /**
    * Create a list from an array of objects with primitives replaced by
    * wrapper classes (e.g. int replaced by Integer)
    *
    * @param array  array of objects
    * @return       correponding list, with primitives replaced by wrapper
    *               clases, empty list if input array is <CODE>null</CODE>
    */
   public static List arrayToList(Object array)
   {
      List newList = new ArrayList();
      if (null == array)
      {
         return newList;
      }
      for (int i=0; i < Array.getLength(array); i++)
      {
         newList.add(Array.get(array, i));
      }
      return newList;
   }

   /**
    * Creates a lower case version of a list of strings.
    *
    * @param inputList  list of strings,can be <code>null</code>
    * @return           list with items in lower case, or <CODE>null</CODE> if
    *                   original list was <CODE>null</CODE>.
    */
   static List lowerCaseList(List inputList)
   {
      if (null == inputList)
      {
          return null;
      }

      List newList = new ArrayList();

      Iterator iter = inputList.iterator();
      String item = "";

      while (iter.hasNext())
      {
         item = (String) iter.next();
         if (null != item)
         {
            newList.add(item.toLowerCase());
         }
         else
         {
            newList.add(item);
         }
      }
      return newList;
   }

   /**
    * Create a list by applying a boolean map to the members of a list and
    * retaining only items which map to <CODE>true</CODE>.
    *
    * @param inputList  list of <CODE>Objects</CODE>, can be <code>null</code>
    * @param map        map with <CODE>Boolean</CODE> values,
    *                   <CODE>true</CODE> if objects with the corresponding
    *                   key should be retained, else <CODE>false</CODE>
    * @return           list resulting from application of the map to the list,
    *                   retaining only items which map to <CODE>true</CODE>.
    *                   Return <CODE>null</CODE> if the
    *                   original list was <CODE>null</CODE> or the map was
    *                   <CODE>null</CODE> or empty.
    */
   public static List filterList(List inputList, Map map)
   {
      if (null == inputList ||
          null == map || map.isEmpty())
      {
          return null;
      }
      List filteredList = new ArrayList();
      if (inputList.isEmpty())
      {
          return filteredList;
      }
      Iterator iter = inputList.iterator();
      Object key = null;
      Boolean val = null;

      while (iter.hasNext())
      {
         key = iter.next();
         if (null != key)
         {
            val = (Boolean) map.get(key);
            if (null != val && val.booleanValue())
            {
               filteredList.add(key);
            }
         }
      }
      return filteredList;
   }

   /**
    * This is a utility method that was put in place because of the way roles
    * are dealt with for transitions.  In some places the role name is used,
    * in others the role id is used.  A has been used throughout with the id
    * as the key and the role name as the value.  There are places where a role
    * id is needed but all that is available is the map and the name, so this
    * method provides a solution for that.
    *
    * @param roleMap a map with its key being an <code>Integer</code> and the
    * value being a <code>String</code>. Must not be <code>null</code>.
    * @param value the value whose id is needed.
    * @return the key if found.  -1 if not found.
    */
   public static int getRoleIdFromMap(Map roleMap, String value)
   {
      if(roleMap == null || value == null || value.trim().length() ==0)
         throw new IllegalArgumentException(
            "arguments must not be null or empty");


      int theKey = -1;
      Collection c = roleMap.values();
      Iterator it = c.iterator();

      Set keySet = roleMap.keySet();
      Object key[] = keySet.toArray();

      int i = 0;
      while(it.hasNext())
      {

         String theValue = (String)it.next();
         if(theValue.equals(value))
         {
            theKey = ((Integer)key[i]).intValue();
            break;
         }
         i++;
      }

      return theKey;
   }

   /**
    * Create a list by applying a map to the members of a list and discarding
    * items which are not keys in the map, or which map to <CODE>null</CODE>.
    *
    * @param inputList  list of <CODE>Objects</CODE>
    * @param map        map to be applied to objects
    * @return           list resulting from application of the map to the list,
    *                   discarding <CODE>null</CODE>s
    *                   Return <CODE>null</CODE> if the
    *                   original list was <CODE>null</CODE> or the map was
    *                   <CODE>null</CODE> or empty.
    */
   public static <V,K> List<V> applyMapList(List<K> inputList, Map<K,V> map)
   {
      if (null == inputList ||
          null == map || map.isEmpty())
      {
          return null;
      }
      List<V> newList = new ArrayList<V>();
      if (inputList.isEmpty())
      {
          return newList;
      }
      Iterator<K> iter = inputList.iterator();
      K key = null;
      V val = null;

      while (iter.hasNext())
      {
         key = iter.next();
         if (null != key)
         {
            val =  map.get(key);
            if (null != val)
            {
               newList.add(val);
            }
         }
      }
      return newList;
   }

   /**
    * Create a list that contains only strings from an existing list which are
    * unique under case-insensitive comparision, retaining the first occurance
    * of any item, and discarding <CODE>null</CODE> strings. Leading and
    * trailing whitespace is trimmed from strings.
    * to <CODE>null</CODE>.
    *
    * @param inputList  list of <CODE>Strings</CODE>
    * @return           list that contains only strings from the input list
    * which are unique under case-insensitive comparision, retaining the first
    * occurance of any item, and discarding <CODE>null</CODE> strings.
    * Returns <CODE>null</CODE> if the original list is null.
    */
   public static List caseInsensitiveUniqueList(List inputList)
   {
      HashMap localMap = new HashMap();
      if (null == inputList)
      {
          return null;
      }

      if (inputList.isEmpty())
      {
          return new ArrayList();
      }
      Iterator iter = inputList.iterator();
      String key = null;
      String val = null;

      /*
       * Create a map with the lower cased strings as keys, and the strings as
       * values. This map collects the unique strings.
       */
      while (iter.hasNext())
      {
         val = (String) iter.next();
         if (null == val)
         {
            continue;
         }
         val = val.trim();
         key = val.toLowerCase();

         if (localMap.containsKey(key))
         {
            continue;
         }
         localMap.put(key, val);
      }

      return new ArrayList(localMap.values());
   }

   /**
    * Intersect 2 lists, returning a list consisting of all elements of the
    * first list that are contained in the second list, or <CODE>null</CODE>
    * if either list is empty.
    *
    * @param list1  first list
    * @param list2  second list
    * @return       a list consisting of all elements of the
    * first list that are contained in the second list, or <CODE>null</CODE>
    * if either list is empty.
    */
   public static List intersectLists(List list1, List list2)
   {
      if (null == list1 || null == list2)
      {
         return null;
      }
      List newList = new ArrayList();
      if (list1.isEmpty() || list2.isEmpty())
      {
         return newList;
      }

      Iterator iter = list1.iterator();
      Object item = null;

      while (iter.hasNext())
      {
         item = iter.next();
         if (list2.contains(item))
         {
            newList.add(item);
         }
      }
      return newList;
   }

   /**
    * Trim a non-null string, and return <CODE>null</CODE> if the trimmed
    * string is empty.
    *
    * @param obj  string to be trimmed. May be <CODE>null</CODE>
    *
    * @return   <CODE>null</CODE> if the input string is <CODE>null</CODE>,
    *           or is empty after trimming.
    *           Otherwise, return the trimmed string.
    */
   public static String toStringHandleNull(Object obj, String stringForNull)
   {
      return (null == obj) ? stringForNull : obj.toString();
   }


   /**
    * Convenience method. Calls
    * {@link #listToDelimitedString(List,String,String)
    * listToDelimitedString(list, delimeter, "")}
    */
   public static String listToDelimitedString (List list,
                                               String delimeter)
   {
      return listToDelimitedString(list, delimeter, "");
   }


   /**
    * Create a string by concatenating the string representations of the
    * elements of an array list, separating the substrings by a delimeter.
    *
    * @param  list       the array list from which the string will be created
    * @param  delimeter  the delimeter used to separate the substrings
    *                    can be empty ("").
    * @throws IllegalArgumentException if list is <CODE>null</CODE> or
    * empty.
    */
   public static String listToDelimitedString (List list,
                                               String delimeter,
                                               String stringForNull)
   {

      // Null or empty lists are not allowed
      if ( null == list || list.size() == 0 )
      {
         throw new IllegalArgumentException(
            "List to be delimited may not be null or empty.");
      }

      // If there is only one element, no delimeter is needed.
      if (list.size() == 1 )
      {
         return list.get(0).toString();
      }

      Iterator iter = list.iterator();


      // To get delimiters between the substrings, put in the first substring
      // and thereafter append delimeter + substring.
      StringBuilder delimitedStringBuilder =
            new StringBuilder(toStringHandleNull(iter.next(), stringForNull));

      while (iter.hasNext())
      {
         delimitedStringBuilder.append(delimeter +
                                      toStringHandleNull(iter.next(),
                                                         stringForNull));
      }
      return delimitedStringBuilder.toString();
   }

   /**
    * Tests if the currently processed item was transitioned into a public
    * workflow state. The request parameters <code>sys_contentid</code> and
    * <code>IPSConstants.DEFAULT_NEWSTATEID_NAME</code> are required.
    *
    * @param request the request to operate on, not <code>null</code>.
    * @param params the request parameters if <code>inherit</code> if
    *    <code>false</code>, additional parameters if <code>inherit</code> is
    *    <code>true</code>, may be <code>null</code> or empty.
    * @param inherit <code>true</code> to inherit the parameters from the
    *    supplied request, <code>false</code> otherwise.
    * @return <code>true</code> if the current item was transitioned into a
    *    public state, <code>false</code> otherwise.
    * @throws PSInternalRequestCallException if any error occurs processing
    *    the internal request call.
    * @throws PSNotFoundException if a required resource cannot be found.
    */
   public static boolean isPublicState(IPSRequestContext request, Map params,
      boolean inherit)
      throws PSInternalRequestCallException, PSNotFoundException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");

      String resource = PSRelationshipUtils.SYS_PSXRELATIONSHIPSUPPORT + "/" +
         PSRelationshipUtils.GET_WORKFLOWSTATUS;

      IPSInternalRequest ir = request.getInternalRequest(resource, params,
         inherit);
      if (ir != null)
      {
         Document doc = ir.getResultDoc();
         Element elem = doc.getDocumentElement();
         if (elem != null)
         {
            return elem.getAttribute("isPublic").equalsIgnoreCase("y")
               || elem.getAttribute("isPublic").equalsIgnoreCase("i");
         }

         return false;
      }
      else
      {
         Object[] args =
         {
            resource,
            "No request handler found."
         };
         throw new PSNotFoundException(
            IPSServerErrors.MISSING_INTERNAL_REQUEST_RESOURCE, args);
      }
   }
   
   /**
    * Tests if the currently processed item is in the public state.
    * The request parameter <code>sys_contentid</code> is required.
    *
    * @param request the request to operate on, not <code>null</code>.
    * @throws PSInternalRequestCallException if any error occurs processing
    *    the internal request call.
    * @throws PSNotFoundException if a required resource cannot be found.
    */
   public static boolean isInPublicState(IPSRequestContext request)
      throws PSInternalRequestCallException, PSNotFoundException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");
         
      if (request.getParameter(IPSHtmlParameters.SYS_CONTENTID)==null)
         throw new IllegalArgumentException("sys_contentid cannot be null");

      String resource = PSRelationshipUtils.SYS_PSXRELATIONSHIPSUPPORT + "/" +
         PSRelationshipUtils.GET_CURRENTSTATE;

      IPSInternalRequest ir = request.getInternalRequest(resource, null,
         true);
         
      if (ir != null)
      {
         Document doc = ir.getResultDoc();
         Element elem = doc.getDocumentElement();
         if (elem != null)
            return elem.getAttribute("isPublic").equalsIgnoreCase("y")
               || elem.getAttribute("isPublic").equalsIgnoreCase("i");

         return false;
      }
      else
      {
         Object[] args =
         {
            resource,
            "No request handler found."
         };
         throw new PSNotFoundException(
            IPSServerErrors.MISSING_INTERNAL_REQUEST_RESOURCE, args);
      }
   }

   /**
    * Tests if the supplied contentid is in the public state.
    * The request parameter <code>sys_contentid</code> is required.
    *
    * @param request the request to operate on, not <code>null</code>.
    * @param contentid to test for public state, must be a valid contentid.
    * @throws PSInternalRequestCallException if any error occurs processing
    *    the internal request call.
    * @throws PSNotFoundException if a required resource cannot be found.
    */
   public static boolean isInPublicState(IPSRequestContext request, int contentid)
      throws PSInternalRequestCallException, PSNotFoundException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");
      
      if (contentid < 1)
         throw new IllegalArgumentException("contentid must be valid");

      String resource = PSRelationshipUtils.SYS_PSXRELATIONSHIPSUPPORT + "/" +
         PSRelationshipUtils.GET_CURRENTSTATE;

      Map extraParams = new HashMap();
      extraParams.put(IPSHtmlParameters.SYS_CONTENTID, "" + contentid);
      
      IPSInternalRequest ir = request.getInternalRequest(resource, extraParams,
         false);
      
      if (ir != null)
      {
         Document doc = ir.getResultDoc();
         Element elem = doc.getDocumentElement();
         if (elem != null)
            return elem.getAttribute("isPublic").equalsIgnoreCase("y");

         return false;
      }
      else
      {
         Object[] args =
         {
            resource,
            "No request handler found."
         };
         throw new PSNotFoundException(
            IPSServerErrors.MISSING_INTERNAL_REQUEST_RESOURCE, args);
      }
   }

   /**
    * Tests whether or not the workflow action of the supplied request is
    * a transition to a public state.
    *
    * @param request the request to test, not <code>null</code>.
    * @return <code>true</code> if the workflow action is a transition to a
    *    public state, <code>false</code> otherwise.
    * @throws PSInternalRequestCallException if any error occurs processing
    *    the internal request call.
    * @throws PSNotFoundException if a required resource cannot be found.
    */
   public static boolean toPublicState(IPSRequestContext request)
      throws PSInternalRequestCallException, PSNotFoundException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");

      String resource = PSRelationshipUtils.SYS_PSXRELATIONSHIPSUPPORT + "/" +
         PSRelationshipUtils.GET_REQUESTEDTRANSITION;

      IPSInternalRequest ir = request.getInternalRequest(resource);
      if (ir != null)
      {
         Document doc = ir.getResultDoc();
         Element elem = doc.getDocumentElement();
         if (elem != null)
         {
            Map params = new HashMap();
            params.put(IPSHtmlParameters.SYS_WORKFLOWID,
            elem.getAttribute("wfId"));
            params.put(IPSConstants.DEFAULT_NEWSTATEID_NAME,
               elem.getAttribute("toStateId"));

            return PSWorkFlowUtils.isPublicState(request, params, false);
         }

         return false;
      }
      else
      {
         Object[] args =
         {
            resource,
            "No request handler found."
         };
         throw new PSNotFoundException(
            IPSServerErrors.MISSING_INTERNAL_REQUEST_RESOURCE, args);
      }
   }
   
   /**
    * Get the property of the specified property.
    * @param name the name of the property, never <code>null</code>.
    * @return the value of the property, may be <code>null</code> if the
    *    property does not exist.
    */
   public static String getProperty(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null.");

      if (properties == null)
         loadProperties();
      return PSWorkFlowUtils.properties.getProperty(name);
   }
   
   /**
    * Set the property value of the specified key.
    * 
    * @param key the key of the property, never <code>null</code>.
    * @param value the name of the property, never <code>null</code>.
    */
   public static void setProperty(String key, String value)
   {
      if (StringUtils.isBlank(key) || StringUtils.isBlank(value))
         throw new IllegalArgumentException("key and/or value may not be null.");
      
      properties.setProperty(key, value);
      
      storeProperty();
   }
   
   /**
    * Stores the property file.
    * 
    */
   private static synchronized void storeProperty()
   {
      try
      {
         File confFile = PSProperties.getConfig(ENTRY_NAME, FILE_PROPERTIES,
               PSServer.getRxFile(WORKFLOW_DIR));
         
         properties.store(new FileOutputStream(confFile), null);
      }
      catch (FileNotFoundException e)
      {
         printWorkflowMessage(null, "Error saving the workflow properties file. Cannot found the file.");
      }
      catch (IOException e)
      {
         printWorkflowMessage(null, "Error saving the workflow properties file.");
      }
   }
   
   /**
    * Determines if ssl should be used by notification during link generation.
    * @return <code>true</code> if ssl is enabled, <code>false</code> otherwise.
    */
   public static boolean isSSLEnabledForNotification()
   {
      return PSWorkFlowUtils.properties.getProperty("RX_SERVER_IS_SSLLINK_NOTIFICATION", "").equalsIgnoreCase("yes");
   }
   
   /**
    * Reads the default workflow value from the rxworkflow.properties file.
    * 
    * @return the name of the default workflow. May be empty or <code>null</code>
    */
   public static String getDefaultWorkflowProperty()
   {
      return getProperty(DEFAULT_WORKFLOW_KEY);
   }
   
   /**
    * Saves the default workflow value on the rxworkflow.properties file.
    * 
    * @param workflowName the name of the workflow, never empty or <code>null</code>.
    */
   public static void setDefaultWorkflowName(String workflowName) throws com.percussion.services.error.PSNotFoundException {
      if (StringUtils.isBlank(workflowName))
         throw new IllegalArgumentException("worklfow name may not be null.");
      
      loadDefaultWorkflow(workflowName);
      setProperty(DEFAULT_WORKFLOW_KEY, workflowName);
   }

   /**
    * Loads the specified default workflow by name.
    * 
    * @param wfName the name of the (default) workflow in question, assumed not blank.
    * 
    * @return the workflow, never <code>null</code>.
    */
   private static PSWorkflow loadDefaultWorkflow(String wfName) throws com.percussion.services.error.PSNotFoundException {
      IPSWorkflowService wfService = PSWorkflowServiceLocator.getWorkflowService();
      List<PSWorkflow> wfs = wfService.findWorkflowsByName(wfName);
      if (!wfs.isEmpty())
      {
         return wfs.get(0);
      }
      
      String errorMsg = "Cannot find default workflow name: \"" + wfName + "\"";
      ms_log.error(errorMsg);
      throw new com.percussion.services.error.PSNotFoundException(errorMsg);
   }

   /**
    * Encrypts the rxworkflow.properties file with
    * any property added to the <code>encryptProps</code>
    * array.
    */
   private static void encryptWorkflowProps() {
      Collection<String> props = new ArrayList<String>();
      for (String prop : encryptProps) {
         props.add(prop);
      }
      File workflowProps = new File(PathUtils.getRxDir(null),
              WORKFLOW_PROPS_PATH + "/" + WORKFLOW_PROPS_FILE_NAME);
      PSEncryptProperties.encryptFile(workflowProps, props,PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR));
   }

   /**
    * Get allowed transitions for the specified user and item.
    * @param contentId The id of the item to get transitions for.
    * @param userName The name of the user to check for, may not be
    * <code>null</code> or empty.
    * @param roles A list of the user's roles, not <code>null</code>, may be
    * empty.
    * @param commId The user's current community.
    *
    * @return A list of allowed transition objects, never <code>null</code>,
    * may be empty.
    *
    * @throws NamingException If there is a problem obtaining a database
    * connection.
    * @throws PSEntryNotFoundException If the content status entry for the
    * specified item cannot be located.
    * @throws PSORMException If there are errors loading the workflow definition.
    * @throws SQLException If there are any other errors accessing the database.
    */
   public static List<PSTransitionInfo> getAllowedTransitions(int contentId,
                                                              String userName, List<String> roles, int commId)
           throws NamingException, SQLException, PSEntryNotFoundException,
           PSORMException
   {
      if (StringUtils.isBlank(userName))
         throw new IllegalArgumentException(
                 "userName may not be null or empty");
      if (roles == null)
         throw new IllegalArgumentException("roles may not be null");

      Connection conn = null;
      PSContentStatusContext csc = null;
      try
      {
         conn = PSConnectionHelper.getDbConnection(null);
         csc = new PSContentStatusContext(conn,
                 contentId);
         csc.close();

         if (csc.getCommunityID() != commId)
            return new ArrayList<PSTransitionInfo>();

         String roleNames = PSStringUtils.listToString(roles, ",");
         return getAllowedTransitions(csc, conn, userName, isAdmin(csc,
                 userName, roleNames), roleNames);
      }
      finally
      {
         if (conn != null)
         {
            try
            {
               conn.close();
            }
            catch (SQLException e)
            {
            }
         }
      }
   }

   /**
    * Get allowed transitions for the specified user and item.
    *
    * @param csc The content status context of the item, assumed not
    * <code>null</code>.
    * @param conn The connection to use, assumed not <code>null</code>.
    * @param userName The name of the user, assumed not <code>null</code> or
    * empty.
    * @param isAdmin <code>true</code> if the user has workflow administrator
    * priveleges, <code>false</code> otherwise.
    * @param actorRoles A comma-delimieted list of the user's roles, assumed not
    * <code>null</code>, may be empty.
    *
    * @return A list of allowed transition objects, never <code>null</code>,
    * may be empty.
    *
    * @throws SQLException If there are errors accessing the database.
    */
   @SuppressWarnings("unchecked")
   public static List<PSTransitionInfo> getAllowedTransitions(
           PSContentStatusContext csc, Connection conn, String userName,
           boolean isAdmin, String actorRoles) throws SQLException
   {
      PSTransitionsContext tc = null;
      List<PSTransitionInfo> results = new ArrayList<PSTransitionInfo>();

      try
      {
         tc = new PSTransitionsContext(csc.getWorkflowID(),
                 conn,
                 csc.getContentStateID());

         // before going further.. should we?
         // first check if the user has acted, if so just return
         PSContentApprovalsContext cac =
                 new PSContentApprovalsContext(
                         csc.getWorkflowID(),
                         conn,
                         csc.getContentID(),
                         tc);

         if (cac.hasUserActed(userName))
            return results;

         while(true)
         {
            boolean isDisabled = false;
            // Don't show buttons for aging transitions
            if (!tc.isAgingTransition())
            {
               /*
                * Transition required roles are ignored for an administrator,
                * otherwise check whether user acts in one of them
                */
               if (!isAdmin)
               {
                  List transitionRequiredRoles = tc.getTransitionRoles();

                  if (null != transitionRequiredRoles &&
                          transitionRequiredRoles.size() > 0)
                  {
                     isDisabled =
                             !PSWorkFlowUtils.compareRoleList(
                                     transitionRequiredRoles,
                                     actorRoles);

                     // check to see if we have acted in all available roles
                     if (isDisabled || hasRolesActed(cac, tc, actorRoles))
                     {
                        // if we have already acted in all the available roles
                        // for this user, don't add the transitions that are
                        // not available for this user, move to the next trans
                        if (false == tc.moveNext())
                           break;

                        continue;
                     }
                  }
               }

               results.add(new PSTransitionInfo(tc.getTransitionID(),
                       tc.getTransitionLabel(), tc.getTransitionActionTrigger(),
                       tc.getTransitionToStateID(), tc.getTransitionComment(),
                       isDisabled));
            }

            if(false == tc.moveNext())
               break;
         }
      }
      catch(PSEntryNotFoundException e)
      {
         // ignore
      }
      finally
      {
         if (tc != null)
            tc.close();
      }

      return results;
   }


   /**
    * Checks to see if all the roles specified for the user that match with the
    * list of roles supplied on the transition context.
    * @param cac The current approvals context, assumed not <code>null</code>.
    * @param tc The current transtions context, assumed not <code>null</code>.
    * @param roleList The comma delimited list of user role names, assumed not
    * <code>null</code> or empty.
    *
    * @return <code>false</code> if at least 1 role that exists in the
    * transition context has not been acted, <code>true</code> otherwise.
    */
   private static boolean hasRolesActed(PSContentApprovalsContext cac,
                                        PSTransitionsContext tc, String roleList)
   {
      // next get the roles list, check if all roles have been acted upon
      StringTokenizer tkn =
              new StringTokenizer(roleList, PSWorkFlowUtils.ROLE_DELIMITER);

      while (tkn.hasMoreTokens())
      {
         String role = tkn.nextToken().trim();
         int roleId =
                 PSWorkFlowUtils.getRoleIdFromMap(
                         tc.getTransitionRoleNameIdMap(),
                         role);

         // role not found in the list of roles for the specified transition
         // just ignore this one and continue checking the list of roles
         if (roleId == -1)
            continue;

         // if we find a role we have not acted upon, return false
         if (!cac.hasRoleActed(roleId))
            return false;
      }
      return true;
   }

   /**
    * Determine if the specified user is a workflow administrator.
    *
    * @param csc The content status context to use, assumed not
    * <code>null</code> and to have been closed already.
    * @param userName The The user to check, assumed not <code>null</code> or
    * empty.
    * @param roleNameList A comma delimited list of the user's roles, assumed
    * not <code>null</code>.
    *
    * @return <code>true</code> if the user is an admin, <code>false</code>
    * if not.
    *
    * @throws SQLException If there are any other database errors.
    */
   public static boolean isAdmin(PSContentStatusContext csc, String userName,
                                  String roleNameList) throws SQLException
   {
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      IPSWorkflowAppsContext wac = cms.loadWorkflowAppContext(
              csc.getWorkflowID());
      String sAdminName = wac.getWorkFlowAdministrator();
      // Check whether the user is Workflow admin
      boolean isAdmin = PSWorkFlowUtils.isAdmin(sAdminName,
              userName,
              roleNameList);
      return isAdmin;
   }

   public static boolean isPublic(int cid) {
      IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager();
      PSComponentSummary sum = mgr.loadComponentSummary(cid);
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();

      int wfId = sum.getWorkflowAppId();
      int stateId = sum.getContentStateId();

      IPSWorkflowService wfSvc = PSWorkflowServiceLocator.getWorkflowService();
      IPSGuid stateGuid = gmgr.makeGuid(stateId, PSTypeEnum.WORKFLOW_STATE);
      IPSGuid wfGuid = gmgr.makeGuid(wfId, PSTypeEnum.WORKFLOW);
      try {
         return wfSvc.isPublic(stateGuid, wfGuid);
      } catch (PSWorkflowException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * The logger
    */
   private static final Logger ms_log = LogManager.getLogger(PSWorkFlowUtils.class.getName());
}

