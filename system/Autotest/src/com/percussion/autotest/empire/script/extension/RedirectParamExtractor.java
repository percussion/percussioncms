/*[ RedirectParamExtractor.java ]**********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.autotest.empire.script.extension;

import com.percussion.autotest.empire.script.IResponseExit;
import com.percussion.autotest.empire.script.IExecutionContext;
import com.percussion.autotest.empire.script.ScriptTestErrorException;

import com.percussion.test.http.HttpHeaders;

import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * This class is used to extract an html parameter from the Location header on
 * a redirect (Http result code 302), and set it in a script macro.  It should
 * only be used in the expect block of a request that will result in a 302.
 */
public class RedirectParamExtractor implements IResponseExit
{
   /**
    * This method will perform the extraction of a parameter, adding a marcro
    * for each to the ctx.  It uses the first param found.  Expected params are:
    * <table>
    * <th>
    * <td>Param name</td><td>Param value</td>
    * </th>
    * <tr>
    * <td>htmlParam</td><td>The name of the html param to extract</td>
    * </tr>
    * <tr>
    * <td>macroName</td><td>The name of the macro to set with the htmlParam's
    *    value</td>
    * </tr>
    * </table>
    *
    * The input stream is not modified in any way, and is simply returned.  See
    * {@link IResponseExit#processResponse(Properties, IExecutionContext,
    * HttpHeaders, InputStream) IResponseExit.processResponse()} for info
    * regarding this method's parameters, return value, and exceptions.
    *
    * @thows ScriptTestErrorException If the the Location header is not found,
    * or if any of the expected params are missing or invalid.
    */
   public InputStream processResponse( Properties params,
         IExecutionContext ctx, HttpHeaders headers, InputStream input )
      throws ScriptTestErrorException
   {
      // get the params
      String htmlParam = params.getProperty(HTML_PARAM_NAME);
      String macroName = params.getProperty(MACRO_PARAM_NAME);

      // Find the location header
      String location = headers.getHeader(LOCATION_CMD_HEADER);

      String strParamValue = "";

      if (htmlParam == null || htmlParam.trim().length() == 0)
         throw new ScriptTestErrorException("No htmlParam provided.");

      if (macroName == null || macroName.trim().length() == 0)
         throw new ScriptTestErrorException("No macroName provided.");

      if (location == null || location.trim().length() == 0)
         throw new ScriptTestErrorException("No Location header found.");


      // parse it to find the content id param
      int pos = location.indexOf('?');
      if (pos != -1 && location.length() > pos)
      {
         // lose the anchor, if any
         int endPos = location.indexOf('#', pos);
         endPos = (endPos == -1 ? location.length() : endPos);
         String paramsString = location.substring(pos + 1, endPos);

         // we have the params, now find ours
         StringTokenizer tok = new StringTokenizer(paramsString, "&");
         while (tok.hasMoreTokens())
         {
            String paramString = tok.nextToken();
            int equalPos = paramString.indexOf('=');
            if (equalPos != -1 && paramString.substring(0, equalPos).equals(
               htmlParam))
            {
               if (paramString.length() > equalPos)
                  strParamValue = paramString.substring(equalPos + 1);
               break;
            }
         }

         ctx.setMacro(macroName, strParamValue);
      }
      else
      {
         throw new ScriptTestErrorException(
            "Unable to locate query string in Location header.");
      }

      return input;
   }

   /** The macroName property name */
   public static final String MACRO_PARAM_NAME = "macroName";

   /** The htmlParam property name */
   public static final String HTML_PARAM_NAME = "htmlParam";

   /** The redirect location header */
   private static final String LOCATION_CMD_HEADER = "Location";

}
