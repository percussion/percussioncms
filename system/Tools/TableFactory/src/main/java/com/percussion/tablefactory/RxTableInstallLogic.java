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

package com.percussion.tablefactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

/**
* This class is a place holder for several global constants or variables that
* need to be evaluated only once.
*
*/
public class RxTableInstallLogic
{

   private static final Logger log = LogManager.getLogger(RxTableInstallLogic.class);

   /**
   * Role delimiter (in role list)
   */
   private static final String ROLE_DELIMITER = ",";

   /**
   * Name of the properties file.
   */
   public static final String FILE_PROPERTIES = "rxworkflow.properties";

   /**
   * Debug flag, when set to true additonal logging is done.
   */
   public static boolean m_bDebug = false;

   /**
   * Name fo the file for logging. This typycally specified in the Properties
   * file
   *
   */
   public static String m_sLogFile = null;

   /**
   * Default name for storing the new state id when transition takes place.
   */
   public static String DEFAULT_NEWSTATEID_NAME = "newstateid";

   /**
   * Default name for storing the transition id.
   */
   public static String DEFAULT_TRANSITIONID_NAME = "transitionid";

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
   public static final int CREATE_NODROP  = 3 ;
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
   public static String PS_OLD_DATA_FILENAME = new String("ps_org_tbl_data.xml");
   public static String PS_OLD_DTD_FILENAME = new String("ps_org_tbl_def.xml");

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
   public static Properties properties = new Properties();

   /**
   *
   * AssignmentType:
   *   0 - Not in workflow - Either the user is not in any role, or all of his roles
   *      are not listed in the workflow definition
   *   1 - None - User is in a role, but the role is not assigned for this state
   *   2 - Reader - User is in a role assigned as Reader
   *   3 - Assignee - User is assigned to transition the document
   *  4 - Admin - User is an administrator in this Workflow.  Administrators have
   *      all rights
   *
   */
   public static final int ASSIGNMENT_TYPE_NOT_IN_WORKFLOW = 0;
   public static final int ASSIGNMENT_TYPE_NONE = 1;
   public static final int ASSIGNMENT_TYPE_READER = 2;
   public static final int ASSIGNMENT_TYPE_ASSIGNEE = 3;
   public static final int ASSIGNMENT_TYPE_ADMIN = 4;

   /**
   *
   * HTML Parameter name that stores the current user's assignment type.
   * authenticateUser exit adds this to the list and performTransition may modify,
   * if required.
   *
   */
   public static final String ASSIGNMENT_TYPE_CURRENT_USER =
                                                   "assignmenttypecurrentuser";
   /**
   *
   * Document check out status values
   *   0 - Not checked-out by anybody
   *   1 - Checked out by current user
   *   2 - Checked out by some body else
   *
   */
   public static final int CHECKOUT_STATUS_NONE = 0;
   public static final int CHECKOUT_STATUS_CURRENT_USER = 1;
   public static final int CHECKOUT_STATUS_OTHER = 2;

   /**
   *
   * HTML Parameter name that stores the current document's checkout status.
   * authenticateUser exit adds this to the list and performTransition may modify,
   * if required.
   *
   */
   public static final String CHECKOUT_STATUS_CURRENT_DOCUMENT =
                                                "checkoutstatuscurrentdocument";
   /**
   *
   * HTML Parameter name that stores the current document's checkout user name.
   * authenticateUser exit adds this to the list and performTransition may modify,
   * if required.
   *
   */
   public static final String CHECKOUT_USER_NAME = "checkoutusername";

   /**
   *
   * HTML Parameter name that stores the content status history id after writing
   * the history.
   *
   */
   public static final String HTML_PARAM_CONTENTSTATUSHISTORYID = "contentstatushistoryid";
                                                /*
   /**
   * Ad-hoc options:
   *  0 - No Ad-Hoc
   *   1 - Ad-Hoc enabled
   *   2 - Anonymous Ad-Hoc
   *
   */
   public static final int ADHOC_DISABLED = 0;
   public static final int ADHOC_ENABLED = 1;
   public static final int ADHOC_ANONYMOUS = 2;

   public static final String SNEWSTATEIDKEY = "sNewStateIDKey";

   public static final String ACTION_TRIGGER_NAME = "ACTION_TRIGGER_NAME";
   public static final String DEFAULT_ACTION_TRIGGER_NAME = "WFAction";

   public static final String REQUEST_NAME = "REQUEST_NAME";
   public static final String DEFAULT_REQUEST_NAME = "request";

   public static final String ACTION_LIST_ELEMENT_NAME = "ACTION_LIST_ELEMENT_NAME";
   public static final String DEFAULT_ACTION_LIST_ELEMENT_NAME = "ActionList";

   public static final String ACTION_ELEMENT_NAME = "ACTION_ELEMENT_NAME";
   public static final String DEFAULT_ACTION_ELEMENT_NAME = "Action";

   public static final String CONTENT_HISTORY_REQUEST = "CONTENT_HISTORY_REQUEST";
   public static final String HISTORY_ACTION_ELEMENT_NAME = "HistoryAction";
   public static final String VIEW_ACTION_ELEMENT_NAME = "ViewAction";
   public static final String EDIT_ACTION_ELEMENT_NAME = "EditAction";
   public static final String CHECKINOUT_ACTION_ELEMENT_NAME = "CheckInOutAction";

   public static final String TRIGGER_CHECK_IN = "TRIGGER_CHECK_IN";
   public static final String DEFAULT_TRIGGER_CHECK_IN = "CheckIn";

   public static final String TRIGGER_CHECK_OUT = "TRIGGER_CHECK_OUT";
   public static final String DEFAULT_TRIGGER_CHECK_OUT = "CheckOut";

   public static final String CHECKINOUT_CONDITION_IGNORE = "ignore";
   public static final String CHECKINOUT_CONDITION_CHECKIN = "checkin";
   public static final String CHECKINOUT_CONDITION_CHECKOUT = "checkout";

   private static ResourceBundle m_ResourceBundle = null;

   static
   {
      try
      {
         properties.load(new FileInputStream(FILE_PROPERTIES));
      }
      catch(FileNotFoundException e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
      catch(IOException e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }

      String temp = properties.getProperty("DEBUG", "false");
      if(null != temp && temp.equalsIgnoreCase("true"))
         m_bDebug = true;

      m_sLogFile = properties.getProperty("LOGFILE");
      if(null != m_sLogFile)
         m_sLogFile = m_sLogFile.trim();
   }

   /**
   * Helper function to compare two role lists
   * @param assignmentTypeList - ArrayList containing assignment types as Integers
   *
   * @param roleList - first role list as ArrayList;
   *
   * @param sRoleList - the second role list as comma separated list
   *
   * @return the assignment type - -ne of the values defined in this file (RxTableInstallLogic.java)
   *
   */
   public static int compareRoleList(ArrayList assignmentTypeList, ArrayList roleList, String sRoleList)
   {
      StringTokenizer sTokenizer = new StringTokenizer(sRoleList, ROLE_DELIMITER);
      String sRole = "";
      String sToken = "";
      boolean bPresent = false;
      int nAssignMentType = RxTableInstallLogic.ASSIGNMENT_TYPE_NONE;
      Integer index;
      while(sTokenizer.hasMoreElements())
      {
         sToken = sTokenizer.nextToken().trim();
         for(int i=0; i<roleList.size(); i++)
         {
            sRole = roleList.get(i).toString();
            if(sRole.trim().equalsIgnoreCase(sToken))
            {
               if(false == bPresent)
                  bPresent = true;
               index = (Integer)assignmentTypeList.get(i);
               if(index.intValue() > nAssignMentType)
                  nAssignMentType = index.intValue();
            }
         }
      }
      return nAssignMentType;
   }




   /**
    * Determine whether the user is an administrator. <BR>
    * The user is an administrator if the Workflow admin name is the user's
    * name or is one of the user's roles.
    *
    * @param sAdminName  The name of the workflow administrator
    * @param sUserName   The user's name, cannot be <CODE>null<\CODE>
    * @param sRoleList   A comma-delimited list of the user's roles.
    * @return            <CODE>true<\CODE> if the user is an administrator,
    *                    <CODE>false<\CODE> if not.
    * @throws            <CODE>IllegalArgumentException<\CODE> if the user
    *                    name is <CODE>null<\CODE>or empty.
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

      if(null == sUserName || 0 == sUserName.trim().length())
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


      if(null == sRoleList || 0 == sRoleList.trim().length())
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
      Integer index;
      while(sTokenizer.hasMoreElements())
      {
         sToken = sTokenizer.nextToken().trim();
         if(sAdminName.equalsIgnoreCase(sToken))
         {
            bAdmin = true;
            break;
         }
      }

      return (bAdmin);
   }


   /**
   * Helper function that returns user name after last comma.
   * This is usefule when we need to extract actual user name out of the user name that is obtainable
   * by user contex/username. This has of the form: host1,host2,host3,host4,username.
   *
   * @author Rammohan Vangapalli
   * @version 1.0
   * @since 2.0
   *
   */
   public static String filterUserName(String sUserName)
   {
      String sResult = "";
      StringTokenizer sTokenizer = new StringTokenizer(sUserName, ROLE_DELIMITER);

      while(sTokenizer.hasMoreElements())
      {
         sResult = sTokenizer.nextToken().trim();
         if(-1 != sResult.indexOf('*') || -1 != sResult.indexOf('.'))
            continue;
         break;
      }
      return sResult;
      /*
      int nLoc = sUserName.lastIndexOf(',');
      if(-1 == nLoc)
         return sUserName;
      return (sUserName.length() > nLoc+2) ? sUserName.substring(nLoc+2).trim() : null;
      */
   }

   /**
   * All the error and other messages are stored in a resource file. This static
   * method returns (instantiates if required) the resource bundle.
   */
   public static ResourceBundle getResourceBundle()
   {
      /* load the resources first. this will throw an exception if we can't
      find them */
      if (null == m_ResourceBundle)
         m_ResourceBundle = ResourceBundle.getBundle( "com.percussion.cms.workflow."
            + "RxTableInstallLogicResources", Locale.getDefault());

      return m_ResourceBundle;
   }

}
