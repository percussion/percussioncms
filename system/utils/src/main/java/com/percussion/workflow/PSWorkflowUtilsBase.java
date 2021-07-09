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

package com.percussion.workflow;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.sql.Date;
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

public class PSWorkflowUtilsBase { /**
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
     * Instantiation of empty properties. File will be loaded later.
     *
     */
    public static Properties properties = null;

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
     * Helper function to compare two role lists
     * @param assignmentTypeList - ArrayList containing assignment types as
     *                             Integers
     *
     * @param roleList -           first role list as ArrayList;
     *
     * @param sRoleList -          the second role list as comma separated list
     *
     * @return                     the assignment type - one of the values
     *                             defined in this file (PSWorkflowUtilsBase.java)
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
        int nAssignmentType = PSWorkflowUtilsBase.ASSIGNMENT_TYPE_NONE;
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
    public static String getTransitionCommentFromHTMLParams(HashMap htmlParams)
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
        return PSWorkflowUtilsBase.properties.getProperty(
                "HTML_PARAM_TRANSITION_COMMENT", PSWorkflowUtilsBase.TRANSITION_COMMENT);
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
                                                        HashMap params)
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
        StringBuffer buf = new StringBuffer();
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
        for (int i = 0; i < Array.getLength(array); i++)
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
        StringBuffer delimitedStringBuffer =
                new StringBuffer(toStringHandleNull(iter.next(), stringForNull));

        while (iter.hasNext())
        {
            delimitedStringBuffer.append(delimeter +
                    toStringHandleNull(iter.next(),
                            stringForNull));
        }
        return delimitedStringBuffer.toString();
    }





    /**
     * Determines if ssl should be used by notification during link generation.
     * @return <code>true</code> if ssl is enabled, <code>false</code> otherwise.
     */
    public static boolean isSSLEnabledForNotification()
    {
        return PSWorkflowUtilsBase.properties.getProperty("RX_SERVER_IS_SSLLINK_NOTIFICATION", "").equalsIgnoreCase("yes");
    }



    /**
     * The logger
     */
    private static final Logger ms_log = LogManager.getLogger(PSWorkflowUtilsBase.class.getName());
}

