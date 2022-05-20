/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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

package com.percussion.guitools;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class to start the default system browser
 */
public class UTBrowserControl
{
   /**
    * Display a file in the system browser. If you want to display a
    * file, you must include the absolute path name.
    *
    * @param url The target object's url. On Windows, this could be any file or
    * URL. The proper program will be launched. On other systems, only the
    * browser will be launched.
    */
   public static void displayURL(String url)
   {
      String cmd = null;
      try
      {
         /* Fix for bug Rx00-04-0007.
            We used to use rundll32 to execute url.dll:FileProtocolHandler.
            Unfortunately, we found out that this doesn't work properly in
            all versions of shell32.dll. So I created a dll that had an
            interface for rundll32 to call and called ShellExecute, which
            would accomplish the same thing, but was documented. As part of
            this change, I moved the command line to launch the browser from
            being hard-coded in this file to the designer.properties file.
            This will allow us to add an interface to allow the user to change
            it if they desire (esp. for unix). */

         String cmdLine = DEFAULT_BROWSER_CMD_LINE;
         try {
            ResourceBundle res = ResourceHelper.getResources();         
            cmdLine = res.getString( BROWSER_CMD_LINE_KEY );
         }
         catch(MissingResourceException e) {} //we already had the default.

         String [] params =
         {
            url
         };
         cmd = MessageFormat.format( cmdLine, params );

         Process p = Runtime.getRuntime().exec( cmd );
      }
      catch( IOException x )
      {
         // couldn't exec browser
         System.err.println("Could not invoke browser, command=" + cmd);
         System.err.println("Caught: " + x);
      }
   }
   
   /**
    * The name of the key in the properties file that is expected to
    * contain the command line to launch the browser. The value is expected to
    * contain 1 replacement parameter where the URL will go.<p/>
    * The replacement parameter defined by {@link MessageFormat#format(
    * String, Object[]) MessageFormat} should be used.<p/>
    * Example: rundll32 psutil.dll,_launchA@16 {0}
    */
   public static final String BROWSER_CMD_LINE_KEY = "BrowserCmdLine";

   /**
    * If the BROWSER_CMD_LINE_KEY can't be found, this is the cmd line that
    * will be executed. Must contain a replacement parameter where the URL should
    * be placed. The replacement parameter defined by {@link 
    * MessageFormat#format(String, Object[]) MessageFormat} should be used.
    */
   public static final String DEFAULT_BROWSER_CMD_LINE = 
      "rundll32 url.dll,FileProtocolHandler {0}";
   
}
