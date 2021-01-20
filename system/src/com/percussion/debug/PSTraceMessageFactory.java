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

package com.percussion.debug;

import com.percussion.design.objectstore.PSTraceOption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Factory object used to create instances of each type of tracing message.  
 * Contains a static initializer to initialize the static member m_optionList.
 */
public class PSTraceMessageFactory 
{
   
   /**
    * Creates the appropriate trace message object from the supplied trace flag.
    * 
    * @param typeFlag Flag representing the type of info that will be traced.
    * @return the type of trace info object as specified by the supplied flag
    * @roseuid 39F49E9C000F
    */
   public static IPSTraceMessage getTraceMessage(int typeFlag) 
   {
      IPSTraceMessage message = null;
      switch (typeFlag)
      {
         case APP_HANDLER_PROC_FLAG:
            message = new PSTraceAppHandlerProc(typeFlag);
            break;
         case APP_SECURITY_FLAG:
            message = new PSTraceAppSecurity(typeFlag);
            break;
         case BASIC_REQUEST_INFO_FLAG:
            message = new PSTraceBasicRequest(typeFlag);
            break;
         case CONDITIONAL_EVAL_FLAG:
            message = new PSTraceConditionalEval(typeFlag);
            break;
         case DB_POOL_FLAG:
            message = new PSTraceDBPool(typeFlag);
            break;
         case EXIT_EXEC_FLAG:
            message = new PSTraceExitExecution(typeFlag);
            break;
         case EXIT_PROC_FLAG:
            message = new PSTraceExitProc(typeFlag);
            break;
         case FILE_INFO_FLAG:
            message = new PSTraceFileInfo(typeFlag);
            break;
         case INIT_HTTP_VAR_FLAG:
            message = new PSTraceHtmlCgi(typeFlag);
            break;
         case MAPPER_FLAG:
            message = new PSTraceMapper(typeFlag);
            break;
         case OUTPUT_CONV_FLAG:
            message = new PSTraceOutputConversion(typeFlag);
            break;
         case POST_EXIT_CGI_FLAG:
            message = new PSTraceHtmlCgi(typeFlag);
            break;
         case POST_EXIT_XML_FLAG:
            message = new PSTracePostExitXml(typeFlag);
            break;
         case POST_PREPROC_HTTP_VAR_FLAG:
            message = new PSTraceHtmlCgi(typeFlag);
            break;
         case RESULT_SET:
            message = new PSTraceResultSet(typeFlag);
            break;
         case RESOURCE_HANDLER_FLAG:
            message = new PSTraceResourceHandler(typeFlag);
            break;
         case SESSION_INFO_FLAG:
            message = new PSTraceSessionInfo(typeFlag);
            break;
      }

      if (message == null)
         throw new IllegalArgumentException("Invalid trace flag");

      return message;
   }
   
   /**
    * Returns a list of all possible trace option flags that may be enabled for an 
    * application and their corresponding flag and description using the default 
    * locale.  If list has already been created for the current locale, it is 
    * retreived from the map, otherwise 
    * the specified locale is used to retreive the display name and description for 
    * each option from a resource bundle and a new list is created and stored in the 
    * map before it is returned.
    * 
    * @return the possible flags as a list of PSTraceOption objects.
    * @roseuid 39F46BEC0280
    */
   public static ArrayList getPossibleOptions() 
   {
      return getPossibleOptions(Locale.getDefault());
   }
   
   /**
    * Returns a PSTraceFlag object with all default options enabled.  Currently these 
    * options include: 
    * Basic Request Info
    * App Handler Processing
    * App Security
    * Resource Handler
    * @return the flag with the default options set
    * @roseuid 39F82C6B0177
    */
   public static PSTraceFlag getDefaultOptionsFlag() 
   {
      PSTraceFlag flag = new PSTraceFlag();

      flag.setBit(BASIC_REQUEST_INFO_FLAG);
      flag.setBit(APP_HANDLER_PROC_FLAG);
      flag.setBit(APP_SECURITY_FLAG);
      flag.setBit(RESOURCE_HANDLER_FLAG);

      return flag;
   }
   
   /**
    * Returns a list of all possible trace option flags that may be enabled for an 
    * application and their corresponding flag and description.  If list has already 
    * been created for the current locale, it is retreived from the map, otherwise 
    * the specified locale is used to retreive the display name and description for 
    * each option from a resource bundle and a new list is created and stored in the 
    * map before it is returned.
    *
    * @param locale the locale to use to retrieve display names and descriptions when
    * constructing the list
    * @return the possible flags as a list of PSTraceOption objects.
    * @roseuid 3A11462E0119
    */
   public static ArrayList getPossibleOptions(Locale locale)
   {
      ArrayList optionList = null;

      synchronized(m_optionLists)
      {
         optionList = (ArrayList)m_optionLists.get(locale);
         if (optionList == null)
         {
            // create the arraylist
            optionList = new ArrayList();

            // get the resource bundle for localization of display names and descriptions
            ResourceBundle bundle = ResourceBundle.getBundle("com.percussion.server.PSStringResources", locale);

            optionList.add(
                 new PSTraceOption(BASIC_REQUEST_INFO_FLAG,
                                    bundle.getString("traceBasicRequestInfo_dispname"),
                                    bundle.getString("traceBasicRequestInfo_desc"),
                                    "traceBasicRequestInfo"));

            optionList.add(
                 new PSTraceOption(INIT_HTTP_VAR_FLAG,
                                    bundle.getString("traceInitHttpVar_dispname"),
                                    bundle.getString("traceInitHttpVar_desc"),
                                    "traceInitHttpVar"));

            optionList.add(
                 new PSTraceOption(FILE_INFO_FLAG,
                                    bundle.getString("traceFileInfo_dispname"),
                                    bundle.getString("traceFileInfo_desc"),
                                    "traceFileInfo"));

            optionList.add(
                 new PSTraceOption(APP_HANDLER_PROC_FLAG,
                                    bundle.getString("traceAppHandlerProc_dispname"),
                                    bundle.getString("traceAppHandlerProc_desc"),
                                    "traceAppHandlerProc"));

            optionList.add(
                 new PSTraceOption(APP_SECURITY_FLAG,
                                    bundle.getString("traceAppSecurity_dispname"),
                                    bundle.getString("traceAppSecurity_desc"),
                                    "traceAppSecurity"));

            optionList.add(
                 new PSTraceOption(POST_PREPROC_HTTP_VAR_FLAG,
                                    bundle.getString("tracePostPreProcHttpVar_dispname"),
                                    bundle.getString("tracePostPreProcHttpVar_desc"),
                                    "tracePostPreProcHttpVar"));

            optionList.add(
                 new PSTraceOption(RESOURCE_HANDLER_FLAG,
                                    bundle.getString("traceResourceHandler_dispname"),
                                    bundle.getString("traceResourceHandler_desc"),
                                    "traceResourceHandler"));

            optionList.add(
                 new PSTraceOption(MAPPER_FLAG,
                                    bundle.getString("traceMapper_dispname"),
                                    bundle.getString("traceMapper_desc"),
                                    "traceMapper"));

            optionList.add(
                 new PSTraceOption(SESSION_INFO_FLAG,
                                    bundle.getString("traceSessionInfo_dispname"),
                                    bundle.getString("traceSessionInfo_desc"),
                                    "traceSessionInfo"));

            optionList.add(
                 new PSTraceOption(DB_POOL_FLAG,
                                    bundle.getString("traceDbPool_dispname"),
                                    bundle.getString("traceDbPool_desc"),
                                    "traceDbPool"));

            optionList.add(
                 new PSTraceOption(EXIT_PROC_FLAG,
                                    bundle.getString("traceExitProc_dispname"),
                                    bundle.getString("traceExitProc_desc"),
                                    "traceExitProc"));

            optionList.add(
                 new PSTraceOption(EXIT_EXEC_FLAG,
                                    bundle.getString("traceExitExec_dispname"),
                                    bundle.getString("traceExitExec_desc"),
                                    "traceExitExec"));

            optionList.add(
                 new PSTraceOption(POST_EXIT_XML_FLAG,
                                    bundle.getString("tracePostExitXml_dispname"),
                                    bundle.getString("tracePostExitXml_desc"),
                                    "tracePostExitXml"));

            optionList.add(
                 new PSTraceOption(POST_EXIT_CGI_FLAG,
                                    bundle.getString("tracePostExitCgi_dispname"),
                                    bundle.getString("tracePostExitCgi_desc"),
                                    "tracePostExitCgi"));

            optionList.add(
                 new PSTraceOption(OUTPUT_CONV_FLAG,
                                    bundle.getString("traceOutputConv_dispname"),
                                    bundle.getString("traceOutputConv_desc"),
                                    "traceOutputConv"));

            optionList.add(
                 new PSTraceOption(RESULT_SET,
                                    bundle.getString("traceResultSet_dispname"),
                                    bundle.getString("traceResultSet_desc"),
                                    "traceResultSet"));

            optionList.add(
                 new PSTraceOption(CONDITIONAL_EVAL_FLAG,
                                    bundle.getString("traceConditionalEval_dispname"),
                                    bundle.getString("traceConditionalEval_desc"),
                                    "traceConditionalEval"));


            // add it to the map
            m_optionLists.put(locale, optionList);

         }
      }

      /* we want to return a copy of the ArrayList so modifications to it
       * wont affect our copy
       */
       ArrayList temp = new ArrayList(optionList.size());
       Iterator i = optionList.iterator();
       while (i.hasNext())
         temp.add(i.next());

      return temp;
   }

   /**
    * Flag constant representing the Basic Request Infomation trace option.  Includes the type of request (POST or GET) and the complete URL and HTTP version
    */
   public static final int BASIC_REQUEST_INFO_FLAG = 0x0001;

   /**
    * Flag constant representing the Initial HTML/CGI trace option.
    * Includes:
    * All HTML parameters in the request (either query params or post params) and their values (before any exit processing).  
    * All CGI variables and their values(before any exit processing).
    */
   public static final int INIT_HTTP_VAR_FLAG = 0x0002;
   
   /**
    * Flag constant representing the File Infomation trace option:  
    * If a post includes one or more files, the name, mime type and length of each one.  
    * Is file treated as XML or single value
    */
   public static final int FILE_INFO_FLAG = 0x0004;
   
   /**
    * Flag constant representing the aPP HANDLER pROCESSING trace option.  Handler used for requestDataset (resource) used (request name and dataset name)
    */
   public static final int APP_HANDLER_PROC_FLAG = 0x0008;
   
   /**
    * Flag constant representing the App Security trace option:
    * ACL entry name
    * Access allowed
    * Was user required to authenticate.  If so: 
    * User ID(s)
    * Security provider(s)/instance(s) used for authentication
    */
   public static final int APP_SECURITY_FLAG = 0x0010;
   
   /**
    * Flag constant representing the HTML/CGI Post PreProc exit trace option:
    * HTML params after all exits are run
    * CGI variables after all exits are run
    */
   public static final int POST_PREPROC_HTTP_VAR_FLAG = 0x0020;
   
   /**
    * Flag constant representing the Resource Handler trace option:
    * Validation processing:
    * For each step in the plan - what is it and what are results
    * 
    * Query: All data associated with the result set returned by the current query. Binary data is displayed in hex.
    * 
    * Update: For each row - action taken (Insert, update, etc). If not skipped, the values bound to each column.
    */
   public static final int RESOURCE_HANDLER_FLAG = 0x0040;
   
   /**
    * Flag constant representing the Mapper trace option:
    * Each mapping that was skipped (query only)
    * For each UDF:  Value of each input param (by doing a toString on it) in the form 'param=value'. The value returned (by doing a toString on it) in the form 'return=value'
    */
   public static final int MAPPER_FLAG = 0x0080;
   
   /**
    * Flag constant representing the Session Info trace option:
    * Are sessions enabled
    * Was an existing session found
    * Session ID
    * All User context values associated w/ the current request.
    */
   public static final int SESSION_INFO_FLAG = 0x0100;
   
   /**
    * Flag constant representing the DB Pool trace option:
    * When connection requested, did it come from pool or was a new connection made. For each new connection attempt, all info used (except pw) and whether successful or failed.
    */
   public static final int DB_POOL_FLAG = 0x0200;
   
   /**
    * Flag constant representing the Exit Processing trace option:
    * Type of exit
    * Name of each exit executed
    * For each exit: Value of each input param (by doing a toString on it) in the form 'param=value'.
    */
   public static final int EXIT_PROC_FLAG = 0x0400;
   
   /**
    * Flag constant representing the Exit Execution trace option:
    * Java extensions can write trace statements to the log.
    */
   public static final int EXIT_EXEC_FLAG = 0x0800;
   
   /**
    * Flag constant representing the Post Exit XML Doc trace option:
    * Prints out the entire XML document sent to the first exit and returned from each successive exit.
    */
   public static final int POST_EXIT_XML_FLAG = 0x1000;
   
   /**
    * Flag constant representing the Post EXIT CGI  trace option:
    * CGI variables after all exits are run.
    */
   public static final int POST_EXIT_CGI_FLAG = 0x2000;
   
   /**
    * Flag constant representing the Output Conversion trace option:
    * Is conversion being done
    * Type of conversion
    * If HTML, display XSL URL
    */
   public static final int OUTPUT_CONV_FLAG = 0x4000;
   
   /**
    * Flag constant representing the Conditional Evaluation trace option:
    * Any conditional evaluation for selection (i.e. resource selection, mapper, etc.)Prints out each operand, the operator, and result for every condition checked.
    */
   public static final int CONDITIONAL_EVAL_FLAG = 0x10000;
   
   /**
    * Flag constant representing the Heterogeneous Join trace option:
    * Prints out result set before and after each join.  Binary data is displayed in hex format.
    */
   public static final int RESULT_SET = 0x8000;
   
   /**
    * Map of ArrayLists of TraceOptions, each one built using a particular Locale.
    * The Locale is the key to the Map.  Map is built as lists are requested for each 
    * Locale.
    */
   private static HashMap m_optionLists = new HashMap();

}
