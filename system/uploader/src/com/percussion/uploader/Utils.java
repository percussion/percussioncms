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
package com.percussion.uploader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A class to contain utility methods and system wide constants. All methods
 * are static. By putting constants here, all programs can more easily have a
 * consistent interface. All property file keys are here. If a program uses a
 * constant, it should use them as documented.
 */
public class Utils
{

   /**
    * Default Rhythmyx server IP address
    */
   public static final String RX_DEFAULT_SERVER_IP = "127.0.0.1";

   /**
    * Default Rhythmyx server port
    */
   public static final int RX_DEFAULT_SERVER_PORT = 9992;

   /**
    * Name of the community cookie
    */
   public static final String COMMUNITY_COOKIE_NAME = "sys_community";

   /**
    * Constant for the "Community" element name
    */
   public static final String EL_COMMUNITY = "Community";

   /**
    * Constant for the "commid" attribute of the "Community" element
    */
   public static final String ATTR_COMMID = "commid";

   /**
    * URL for obtaining the communties
    */
   public static final String RX_COMMUNITY_URL =
      "/Rhythmyx/sys_commSupport/usercommunities.xml";

   /**
    * Default timeout for http requests (in milliseconds).
    */
   public static final int DEFAULT_TIMEOUT_MILLIS = 100000;

   /**
    * This html parameter defines how the uploaded xml document should be
    * treated. The valid expected values for this parameter are 'useValidating',
    * 'useNonValidating' and 'treatAsText'. If the request has multiple xml
    * documents to be uploaded, then this parameter should have values for each
    * xml document delimited by ';'.
    */
   public static final String REQ_XML_DOC_FLAG = "psxmldoc";

   /**
    * The constant to define that the xml document uploaded should be validated.
    */
   public static final String XML_DOC_VALIDATE = "useValidating";

   /**
    * The constant to define that the xml document uploaded should not be
    * validated.
    */
   public static final String XML_DOC_NONVALIDATE = "useNonValidating";

   /**
    * The constant to define that the xml document uploaded should be treated as
    * text. So it should be mapped to a parameter.
    */
   public static final String XML_DOC_AS_TEXT = "treatAsText";

   /* Keys for all possible properties read from the properties file
    * (Default property file - rxuploader.properties)
    */

   /**
    * A properties file key used to specify the JDBC driver class name, fully
    * qualified. Required. Example: oracle.jdbc.OracleDriver
    */
   public static final String DBMS_CLASSNAME_KEY = "DriverClassname";

   /**
    * A properties file key used to specify the host name of the DBMS server.
    * Required. Example: Tds:dbserver:5000
    */
   public static final String DBMS_SERVER_KEY = "DbServer";

   /**
    * A properties file key used to specify the database that contains the
    * tables used by this application. Optional, dependent on the driver used.
    * Highly recommended if available.
    */
   public static final String DBMS_DATABASE_KEY = "DbDatabase";

   /**
    * A properties file key used to specify the owner of the tables used in
    * the application. Optional, dependent on the driver used. Highly recommended
    * if available.
    */
   public static final String DBMS_OWNER_KEY = "DbOwner";

   /**
    * A properties file key used to specify the user Id required to connect to
    * the dbms specified by the other DBMS_... keys. Required.
    */
   public static final String DBMS_USERID_KEY = "DbUserId";

   /**
    * A properties file key used to specify the password required to connect to
    * the DBMS. Optional.
    */
   public static final String DBMS_PASSWORD_KEY = "DbPassword";

   /**
    * A properties file key used to specify the format of the string returned
    * by the getdate() function. If not supplied,DBMS_DEFAULT_DATETIME_FORMAT is
    * used.
    */
   public static final String DBMS_DATETIME_FORMAT_KEY = "DbDateTimeFormat";

   /**
    * A properties file key used to specify the host name of the Rhythmyx server.
    * Required.
    */
   public static final String RX_SERVER_KEY = "RxServer";

   /**
    * A properties file key used to specify the port to use when connecting to
    * the Rhythmyx server. If not supplied, port (the default HTTP port) is used.
    */
   public static final String RX_PORT_KEY = "RxPort";

   /**
    * A properties file key used to specify the login id to use when connecting
    * to the Rhythmyx server. If not supplied, HTTP Basic Authorization header is
    * not set.
    */
   public static final String RX_LOGINID_KEY = "RxLoginId";

   /**
    * A properties file key used to specify the password to use when connecting
    * to the Rhythmyx server. If login id is not supplied then this key is not read.
    * If login id is supplied and password is not supplied, empty password is used.
    */
   public static final String RX_PWD_KEY = "RxPassword";

   /**
    * A properties file key used to specify if the password specified in the
    * properties file is encrytped.
    */
   public static final String RX_PWD_ENCRYPTED_KEY = "RxPwdEncrypted";

   /**
    * A properties file key used to specify the community to use when connecting to
    * the Rhythmyx server. If supplied, a cookie "sys_community" with value as
    * specified is set in the request.
    */
   public static final String RX_COMMUNITY_KEY = "RxCommunity";

   /**
    * A properties file key used to specify if debugging output should be
    * enabled.
    */
   public static final String RX_DEBUG_KEY = "RxDebug";

   /**
    * A properties file key used to specify the file where log messages will
    * be written.
    */
   public static final String RX_LOGFILE_KEY = "RxLogFile";

   /**
    * A properties file key used to specify the extra html parameters which
    * will be appended to the URL.
    */
   public static final String RX_EXTRAPARAMS_KEY = "RxExtraParams";

   /**
    * A properties file key used to specify the action type name used
    * in the Rx application. For example, the default for inserting is INSERT.
    * If not supplied, RX_DEFAULT_ACTIONTYPE is used.
    */
   public static final String RX_ACTIONTYPE_KEY = "UpdateActionType";

   /**
    * A properties file key used to get the HTML parameter that
    * contains the update action to take on a submission to the Rx server. If
    * not supplied, RX_DEFAULT_HTML_ACTION_PARAM is used.
    */
   public static final String RX_HTML_ACTION_PARAM_KEY = "HTMLActionParam";

   /**
    * A properties file key used to get the HTML parameter that
    * specifies the name used to select a document during publishing.
    * &apos;id&apos; is the recommended value. If not supplied
    * RX_DEFAULT_HTML_ID_PARAM is used.
    */
   public static final String RX_HTML_ID_PARAM_KEY = "HtmlIdParamName";

   /**
    * The default format string that specifies how the date will be returned from the
    * result set via getString when the getdate() function is used. This can
    * be overridden in the properties file.
    */
   public static final String DBMS_DEFAULT_DATETIME_FORMAT =
      "yyyy-MM-dd HH:mm:ss.SSS";

   /**
    * The default name of the property file. This can be overridden by a
    * cmd line option (PROPERTYFILE_OPTION).
    */
   public static final String DEFAULT_PROPERTY_FILENAME = "rxuploader.properties";

   /**
    * The name of the stored procedure that will be used to return the next key
    * number. Example: "GetNextNumber"
    */
   public static final String DBMS_NUMBER_GENERATOR_KEY = "NumberGenerator";

   /**
    * The default update action type used when submitting Rx requests if one is
    * not specified in the properties file.
    */
   public static final String RX_DEFAULT_ACTIONTYPE = "INSERT";

   /**
    * The default HTML parameter name that specifies the action type, if one is
    * not specified in the properties file.
    */
   public static final String RX_DEFAULT_HTML_ACTION_PARAM = "DBActionType";

   /**
    * The default HTML parameter that will contain the content Id when the
    * publisher is making a request.
    */
   public static final String RX_DEFAULT_HTML_ID_PARAM = "id";

   /* Error codes returned from main. */
   public static final int SUCCESS                 = 0;
   public static final int ERROR_MISSING_PROPERTY  = -1;
   public static final int ERROR_IO                = -2;
   public static final int ERROR_SQL               = -3;
   public static final int ERROR_CLASS_NOT_FOUND   = -4;
   public static final int ERROR_PARSE             = -5;
   public static final int ERROR_UNKNOWN           = -6;

   public static final int HTTP_STATUS_OK = 200;

   /**
    * The (lowercase) character used on the command line to pass in a
    * filename for the program&apos;s property file.
    */
   public static final Character PROPERTYFILE_OPTION = new Character( 'p' );
   /**
    * The (lowercase) character used on the command line to pass in a
    * filename for the program&apos;s logging file.
    */
   public static final Character LOGFILE_OPTION = new Character( 'l' );

   /**
    * The (lowercase) character used on the command line to pass in a
    * loginid for login into Rhythmyx server.
    */
   public static final Character LOGINID_OPTION = new Character( 'u' );

   /**
    * The (lowercase) character used on the command line to pass in a
    * password for login into Rhythmyx server.
    */
   public static final Character LOGINPWD_OPTION = new Character( 'a' );

   /**
    * The (lowercase) character used on the command line to pass in a
    * community name or id.
    */
   public static final Character COMMUNITYID_OPTION = new Character( 'c' );

   /**
    * Causes the program to emit additional debugging messages to the
    * logger&apos;s stream. No params allowed.
    */
   public static final Character DEBUG_OPTION = new Character( 'd' );

   /**
    * Specify the request URL. The required param is the URL.
    */
   public static final Character REQUESTURL_OPTION = new Character( 'r' );

   /**
    * Brings up a usage message and exits. If present, all other options are
    * ignored.
    */
   public static final Character HELP_OPTION = new Character( 'h' );

   /**
    * Obtains the list of source URLs from a file instead of the command line
    **/
   public static final Character FILE_OPTION = new Character('f');
   
   /**
    * Specify the timeout to use when making the POST request. Timeout is
    * in seconds.
    */
   public static final Character TIMEOUT_OPTION = new Character('t');

   /**
    * Every option that doesn't take a param must appear in this list. If an
    * option doesn't appear in this list, it is expected to take exactly 1
    * param.
    */
   private static final Map OPTION_COUNT_ZERO = new HashMap();

   static
   {
      OPTION_COUNT_ZERO.put( DEBUG_OPTION, null );
      OPTION_COUNT_ZERO.put( HELP_OPTION, null );
   }

  /**
    * Parses the supplied command line params into a map, with all options as keys
    * (Character objects) and any option params as values (String objects) of
    * their respective key. The format of the command line is expected to be
    *    <p>program [options] [file1, file2 ...]
    * <p>The following formats for params are allowed:
    * <ul>
    *   <li>A - must be used to indicate an option. There can be no
    *     space between this char and the option char.</li>
    *   <li>Options are exactly 1 character long. Multiple characters after an
    *     option indicator will be interpreted as multiple options. If any
    *     param is present, it will be associated w/ the last option in the list
    *     </li>
    *   <li>Options can have 0 or 1 parameter.</li>
    *   <li>0 or more files can be specified. Each file can include wildcards.</li>
    *   <li>There must be a space between options and params</li>
    * </ul>
    * <p>The end result is that the OS will expand all wildcards and the incoming
    * array will contain entries for options, option params and all files at
    * the end.<p/>
    * Examples:
    *   <p>PublishHtml -c foo -bd</p>
    *   <p>Will result in:</p>
    *   <table>
    *     <tr>
    *       <th>Option</th><th>Value</th>
    *       <td>c</td><td>foo</td>
    *       <td>b</td><td></td>
    *       <td>d</td><td></td>
    *     </tr>
    *   </table>
    *   <p>PublishHtml foo -b /d</p>
    *   <p>Will result in:</p>
    *   <table>
    *     <tr>
    *       <th>Option</th><th>Value</th>
    *       <td></td><td>foo</td>
    *       <td>b</td><td></td>
    *       <td>d</td><td></td>
    *     </tr>
    *   </table>
    *
    * @param params An array containing options/flags, option parameters and
    * filenames, as defined above.
    *
    * @param lowercaseOptions If <code>true</code>, all option characters are
    * lowercased before they are placed in the map.
    *
    * @param filenames If not null, all filenames found in the array will be
    * added to this list.
    *
    * @return A map that contains each option as a key (just the letter) and
    * each option&apos;s parameter as a value. Special keys are used to map an
    * optionless parameter. If there are no parameters, null is returned.
    *
    * @throws IllegalArgumentException If the supplied params contain
    * more than 1 parameter for any option.
    *
    * Note: This method could be enhanced by passing in the zero option list.
    */
   public static Map parseCmdParams( String [] params, boolean lowercaseOptions,
      ArrayList filenames )
   {
      if ( null == params || params.length == 0 )
         return null;
      HashMap options = new HashMap();
      for ( int i = 0; i < params.length; ++i )
      {
         /* I expect Java or the OS to strip leading/trailling white space
            from all params. */
         String tmp = params[i];
         char indicator = tmp.charAt(0);
         if ( indicator == '-' )
         {
            if ( lowercaseOptions )
               tmp = tmp.toLowerCase();
            // got 1 or more options
            int optionCt = tmp.length()-1;   // subtract 1 for the indicator
            int j;
            for ( j = 1; j < optionCt; ++j )
            {
               options.put( new Character( tmp.charAt(j)), null );
               if ( !isZeroOption( tmp.charAt(j)) )
                  throw new IllegalArgumentException( "Invalid command line:" +
                      " the '" + tmp.charAt(j) + "' option requires a parameter." );
            }
            // see if there is a following param
            String param = null;
            if ( i + 1 < params.length && !isZeroOption( tmp.charAt(j)))
            {
               String p = params[i+1];
               indicator = p.charAt(0);
               if ( indicator != '-' )
               {
                  // it's a param
                  param = p;
                  // increment to loop counter to bypass this used up param
                  ++i;
               }
               else
               {
                  throw new IllegalArgumentException( "Invalid command line:" +
                      " the '" + tmp.charAt(j) + "' option requires a parameter." );
               }
            }
            options.put( new Character( tmp.charAt(j)), param );
         }
         else
         {
            int k;
            // the rest of the params are file names, copy them to the list
            for ( k = i; k < params.length; ++k )
               filenames.add(params[k]);
            // set counter to finish loop
            i = k;
         }
      }
      return options;
   }


   /**
    * Checks the passed in option character against the list of options that
    * are specified to have no parameters. If a match is found, <code>true</code>
    * is returned.
    *
    * @param option One of the ...._OPTION characters.
    *
    * @return <code>true</code> if the option does not require a parameter.
    */
   private static boolean isZeroOption( char option )
   {
      return OPTION_COUNT_ZERO.containsKey( new Character( option ));
   }

   /**
    * Set the CTRL-C handler (only available on Sun's JDK 1.2. If it can't be
    * set, a message is logged to the screen.
    */
   public static void setInterruptHandler()
   {
      try
      {
         //JDK12InterruptHandler.enable();
      }
      catch (Throwable t)
      {
         /* ExceptionInInitializerError or ClassNotFoundException */
         System.out.println( "Couldn't install CTRL-C handler: " +
            t.getLocalizedMessage());
      }
   }


   /**
    * Parse an HTTP params string which consists of 0 or more
    * attribute=value pairs separated by semicolons. Unlimited whitespace
    * is allowed between tokens. Values can also be quoted, which means
    * that special characters (such as = and ;) should be ignored
    * between the quote delimiters.
    *
    * The params will be stored in the map as LCASE(name) -> value.
    */
   public static int parseHttpParamsString(String paramStr, Map params)
   {
      final String str = paramStr.trim();
      final int strLen = str.length();

      int semiPos = 0;
      int numParams = 0;

      while (semiPos >= 0 && semiPos < strLen)
      {
         if (semiPos == 0)
            semiPos = -1; // special case for first param

         int nextSemiPos = str.indexOf(';', semiPos + 1);
         if (nextSemiPos < 0)
            nextSemiPos = strLen;

         String param = str.substring(semiPos + 1, nextSemiPos).trim();

         // must have at least one char in the attribute, an equals sign,
         // and at least one char in the value, making the shortest
         // possible param ("a=b") length 3.
         if (param.length() < 3)
            throw new IllegalArgumentException( "Invalid param string (too short): " + param );

         int eqPos = param.indexOf('=');
         if (eqPos < 2 || eqPos == (param.length() - 1))
            throw new IllegalArgumentException( "Invalid param string (misplaced =): " + param );

         String attribute = param.substring(0, eqPos).trim();
         String value = param.substring(eqPos + 1, param.length()).trim();

         // ignore delimiters within quoted strings
         char start = value.charAt(0);
         char end = value.charAt(value.length() - 1);
         while (start == '"' && end != '"')
         {
            int quotePos = str.indexOf('"', nextSemiPos + 1);
            if (quotePos < 0)
               throw new IllegalArgumentException( "Invalid param string (missing closing doublequote): " + param );
            nextSemiPos = str.indexOf(';', quotePos + 1);
            param = str.substring(semiPos + 1, nextSemiPos).trim();
            value = param.substring(eqPos + 1, param.length()).trim();
            end = value.charAt(value.length() - 1);
         }

         params.put(attribute.toLowerCase(), value);
         numParams++;
         // advance to the next parameter
         semiPos = nextSemiPos;
      }

      return numParams;
   }

}
