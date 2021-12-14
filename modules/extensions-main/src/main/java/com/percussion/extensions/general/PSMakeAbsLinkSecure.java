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
public class PSMakeAbsLinkSecure extends PSSimpleJavaUdfExtension
   implements IPSUdfProcessor
{
   /**
    * Creates a URL from the supplied parameters and returns it.
    * <p>A URI has the following pieces for purposes of this description
    * (see RFC 2396 for more details):
    * <p>
    * <p>&lt;scheme&gt;://&lt;host&gt;&lt;path-segments&gt;
    *    &lt;resource&gt;?&lt;query&gt;#&lt;fragment&gt;
    * <p>
    * <p>All parts except resource are optional.
    * <p>Five basic forms are allowed for the supplied URI:
    * <ul>
    * <li>Fully qualified (e.g. http://server:9992/Rhythmyx/approot/res.html</li>
    * <li>Partially qualified (e.g. /Rhythmyx/approot/res.html)</li>
    * <li>Relative (e.g. ../myApp/res.html)</li>
    * <li>Resource name only (e.g. res.html)</li>
    * <li>An empty string.</li>
    * </ul>
    * <p>Any of these forms may contain a query and fragment part. Any relative
    * url is assumed to be relative from the orginiating request's app root.
    * <p>If the supplied URL is fully qualified and the protocol is not
    * 'http' or 'https', the supplied URL will be returned, unmodified.
    * Otherwise, any pieces supplied will be substituted.  If the supplied URL
    * is not fully qualified, the missing parts will be added. For a partially
    * qualified name, the http(s) protocol, server and port will be added to the
    * supplied name. For an unqaulified name, these items, plus the Rhythmyx
    * request root and the originating application request root will be added.
    * For a relative name, the http(s) protocol, server, port, and Rhythmyx root
    * will be added, assuming it is relative from the originating
    * requests app root.  For an empty string, all parts of the
    * originating request will be used, substituting the supplied parameters.If
    * the port is 80, no port number will be added to the generated url.
    * <p>
    *
    * <p>Multiple name/value pairs may be specified for the parameters.
    * For example, if the following were supplied as parameters:
    * <ul>
    *   <li>useSecure = no</li>
    *   <li>resource = query1.html</li>
    *   <li>param1 = city</li>
    *   <li>value1 = Boston</li>
    *   <li>param2 = state</li>
    *   <li>value2 = MA</li>
    * </ul>
    * then the following URL would be generated (assuming the request was
    * targeted directly at the Rhythmyx server):
    *    <p>http://rxserver:9992/Rhythmyx/MyApp/query1.html?city=Boston&state=MA
    *    </p>
    *
    *   <p>Note: The resource may contain parameters defined on it,
    *       in which case the supplied parameters will be appended after
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
    *    value</th><tr>
    *   <tr>
    *     <td>1</td>
    *     <td>A flag to indicate whether to use the https protocol if the
    *          originating request used this protocol or if the supplied URL
    *          specified it. If the value is "yes" (case insensitive compare),
    *          and either the original request used a secure channel or the
    *          supplied URL specifies the 'https' protocol, the the resulting
    *          link will use a secure channel, otherwise, the http protocol will
    *          be used. If the value is not "yes", the http protocol will be
    *          used regardless of the protocol used by the original request. See
    *          table below for a list of cases and the resulting values.
    *          </td>
    *     <td>yes</td>
    *     <td>n/a</td>
    *   </tr>
    *   <tr>
    *     <td>2</td>
    *     <td> path to the resource.</td>
    *     <td>no</td>
    *     <td>""</td>
    *   </tr>
    *   <tr>
    *     <td>2 * N + 1</td>
    *     <td>The name of the Nth parameter</td>
    *     <td>no</td>
    *     <td>none</td>
    *   </tr>
    *   <tr>
    *     <td>2 * N + 2</td>
    *     <td>The value of the Nth parameter</td>
    *     <td>no</td>
    *     <td>none</td>
    *   </tr>
    * </table>
    * <p>
    * The table below shows the possible combinations of input parameter
    * values and the resulting protocol and port used:<p>
    * <table border="1">
    * <tr>
    * <th><code>useSecure</code></th><th>Original Request Protocol</th><th>
    *    Supplied URL protocol</th><th>Resulting protocol</th>
    *    <th>Resulting port</th>
    * </tr>
    * <tr>
    * <td>no</td><td>HTTP</td><td>none</td><td>HTTP</td><td>originating
    *    request's port</td>
    * </tr>
    * <tr>
    * <td>no</td><td>HTTPS</td><td>none</td><td>HTTP</td><td>Rhythmyx server's
    *    default port</td>
    * </tr>
    * <tr>
    * <td>yes</td><td>HTTP</td><td>none</td><td>HTTP</td><td>originating
    *    request's port</td>
    * </tr>
    * <tr>
    * <td>yes</td><td>HTTPS</td><td>none</td><td>HTTPS</td><td>originating
    *    request's port</td>
    * </tr>
    * <tr>
    * <td>no</td><td>HTTP</td><td>HTTP</td><td>HTTP</td><td>port from supplied
    *    URL</td>
    * </tr>
    * <tr>
    * <td>no</td><td>HTTPS</td><td>HTTP</td><td>HTTP</td><td>Rhythmyx server's
    *    default port</td>
    * </tr>
    * <tr>
    * <td>no</td><td>HTTP</td><td>HTTPS</td><td>HTTP</td><td>originating
    *    request's port</td>
    * </tr>
    * <tr>
    * <td>no</td><td>HTTPS</td><td>HTTPS</td><td>HTTP</td><td>Rhythmyx server's
    *    default port</td>
    * </tr>
    * <tr>
    * <td>yes</td><td>HTTP</td><td>HTTP</td><td>HTTP</td><td>port from supplied
    *    URL</td>
    * </tr>
    * <tr>
    * <td>yes</td><td>HTTPS</td><td>HTTP</td><td>HTTP</td><td>port from supplied
    *    URL</td>
    * </tr>
    * <tr>
    * <td>yes</td><td>HTTP</td><td>HTTPS</td><td>HTTPS</td><td>port from
    *    supplied URL</td>
    * </tr>
    * <tr>
    * <td>yes</td><td>HTTPS</td><td>HTTPS</td><td>HTTPS</td><td>port from
    *    supplied URL</td>
    * </tr>
    * </table>
    * <p>
    *
    * @param request The current request context. May not be <code>null</code>.
    *
    * @return The absolute URL created from the supplied foundation,
    *    user session information, and supplied parameters and values.
    *    If the resource is <code>null</code>, an empty string will
    *    be returned.  If the suppied base specifies a protocol other than
    *    HTTP or HTTPS, then it is returned unchanged.
    *
    * @throws PSConversionException If any required param is missing.
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      if ( null == params || params.length < 2 || null == params[1])
      {
         return "";
      }

      // useSecure is required
      if (params[0] == null)
         throw new PSConversionException( 0, "useSecure parameter required." );

      boolean useSecure = params[0].toString().trim().equalsIgnoreCase("yes");
      String sourceUrl = params[1].toString().trim();

      // build params map
      HashMap paramMap = new HashMap();
      int paramMaxIndex = params.length - 1;
      for ( int paramIndex = 2;
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

      // create the url
      URL result = null;
      try
      {
         result = PSUrlUtils.createUrl(null, null, sourceUrl,
            paramMap.entrySet().iterator(), null, request, useSecure);
      }
      catch (Throwable t)
      {
         throw new PSConversionException(0, t.toString());
      }

      return result;

   }
}
