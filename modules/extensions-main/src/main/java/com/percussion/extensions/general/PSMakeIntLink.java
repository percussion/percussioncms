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

package com.percussion.extensions.general;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSUrlUtils;

import java.net.URL;
import java.util.HashMap;

/**
 * This class implements the UDF processor interface so it can be used as a
 * Rhythmyx function. See {@link #processUdf(Object[], IPSRequestContext)
 * processUdf} for a description.
 */
public class PSMakeIntLink extends PSSimpleJavaUdfExtension
      implements IPSUdfProcessor
{
   /**
    * Creates a URL from the supplied parameters and returns it with the
    * sessionid attached as one of the html parameters.  The server and port
    * are set to the Rhythmyx server's address and port.
    * <p>A URI has the following pieces for purposes of this description
    * (see RFC 2396 for more details):
    * <p>
    * <p>&lt;scheme&gt;://&lt;host&gt;&lt;path-segments&gt;
    *    &lt;resource&gt;?&lt;query&gt;#&lt;fragment&gt;
    * <p>
    * <p>All parts except resource are optional.
    * <p>Five basic forms are allowed for the supplied URI:
    * <ul>
    * <li>Fully qualified (e.g. http://server:9992/Rhythmyx/aproot/res.html</li>
    * <li>Partially qualified (e.g. /Rhythmyx/approot/res.html)</li>
    * <li>Relative (e.g. ../myApp/res.html)</li>
    * <li>Resource name only (e.g. res.html)</li>
    * <li>An empty string.</li>
    * </ul>
    * <p>Any of these forms may contain a query and fragment part. Any relative
    * url is assumed to be relative from the orginiating request's app root.
    * <p>If the supplied URL is fully qualified and the protocol is not 'http',
    * or 'https', the supplied URL will be returned, unmodified. Otherwise, any
    * pieces supplied will be substituted.  If the supplied URL is
    * not fully qualified, the missing parts will be added. For a partially
    * qualified name, the http protocol, server and port will be added to the
    * supplied name. For an unqualified name, these items, plus the Rhythmyx
    * request root and the originating application request root will be added.
    * For a relative name, the http protocol, server, port, and Rhythmyx root
    * will be added, assuming it is relative from the originating
    * requests app root. For an empty string, the app root of the
    * originating request will be used, substituting the supplied parameters.
    * Regardless of whether the originating request is using https, or if the
    * supplied URL is fully qualified and specifies 'https', the resulting URL
    * will always use 'http'.
    *
    * <p>Multiple name/value pairs may be specified for the parameters.
    * For example, if the following were supplied as parameters:
    * <ul>
    *   <li>resource = query1.html</li>
    *   <li>param1 = city</li>
    *   <li>value1 = Boston</li>
    *   <li>param2 = state</li>
    *   <li>value2 = MA</li>
    * </ul>
    * and the session identifier was <code>sessionid</code>,
    * then the following URL would be generated (the params do not
    * necessarily appear in the order presented):
    *   <p>http://127.0.0.1:9992/Rhythmyx/MyApp/query1.html?
    *       pssessionid=sessionid&city=Boston&state=MA</p>
    *
    *   <p>Note: The resource may contain parameters defined on it,
    *       in which case the session id will be appended after
    *       the last parameter defined therein.
    *
    *
    * @param params An array with elements as defined below. The array
    * is processed from beginning to end. As soon as the first <code>null</code>
    * parameter is encountered (<code>null</code> values allowed), processing
    * of the parameters will stop.
    *
    * <table border="1">
    *   <tr><th>Param #</th><th>Description</th><th>Required?</th><th>default
    *     value</th><tr>
    *   <tr>
    *     <td>1</td>
    *     <td> path to the resource.</td>
    *     <td>no</td>
    *     <td>""</td>
    *   </tr>
    *   <tr>
    *     <td>2 * N</td>
    *     <td>The name of the Nth parameter</td>
    *     <td>no</td>
    *     <td>none</td>
    *   </tr>
    *   <tr>
    *     <td>2 * N + 1</td>
    *     <td>The value of the Nth parameter</td>
    *     <td>no</td>
    *     <td>none</td>
    *   </tr>
    * </table>
    *
    * @param request The current request context.
    *
    * @return The absolute URL created from the supplied foundation,
    *    user session information, and supplied parameters and values.
    *    If the resource is <code>null</code>, an empty string will
    *    be returned.
    *
    * @throws PSConversionException If the url cannot be constructed.
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
         throws PSConversionException
   {
      if ( null == params || params.length < 1 || null == params[0])
      {
         return "";
      }

      String sourceUrl = params[0].toString().trim();

      // build params map
      HashMap paramMap = new HashMap();

      int paramMaxIndex = params.length - 1;
      for ( int paramIndex = 1;
            paramIndex < paramMaxIndex && null != params[paramIndex]
            && params[paramIndex].toString().trim().length() > 0;
            paramIndex+=2 )
      {

         int valIndex = paramIndex+1;
         Object o = params[valIndex];
         if (o != null)
            o = o.toString();
         paramMap.put(params[paramIndex].toString(), o);
      }  

      // create the url, passing the rx server's address and port
      URL result = null;
      try
      {
         result =
               PSUrlUtils.createUrl("127.0.0.1",
               new Integer(request.getServerListenerPort()), sourceUrl,
               paramMap.entrySet().iterator(), null, request, false);
         /*FIXME - This function used to return the url with atleast one parameter
         called pssessionid. Now the pssessionid parameter has been removed. If there
         are no parameters then the url will be returned without ? at the end.
         Some document calls in xsl files assumed ? to be present and appended parameters
         without checking for it. Those document calls will fail now.
         The following line add ? at the end of the url if it does not exist.
         This needs to be removed and the document calls in the xsls and js functions
         needs to be fixed properly.
         */
         if (result != null && result.toString().indexOf('?') == -1)
            result = new URL(result.toString() + "?");
      }
      catch (Throwable t)
      {
         throw new PSConversionException(0, t.toString());
      }
       
      return result;

   }
}