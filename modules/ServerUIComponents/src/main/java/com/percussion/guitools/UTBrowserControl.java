/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
