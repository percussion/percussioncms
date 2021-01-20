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

package com.percussion.extension;

import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSURLEncoder;

import java.io.File;

import org.w3c.dom.Document;



/**
 * The PSSetCookieExtension class implements extension handling for the
 * setCookie simple action extension. This extension associates a cookie with the
 * results to be returned to the requestor. Several attributes are associated
 * with the cookie:
 * <table border="1">
 * <tr><th>Parameter</th><th>Description</th></tr>
 * <tr><td>PSXName</td>
 *     <td>(required) the name of the cookie</td></tr>
 * <tr><td>PSXValue</td>
 *     <td>(required) the value of the cookie</td></tr>
 * <tr><td>PSXExpires</td>
 *     <td>(optional) the date the cookie expires. Use 'M/d/yyyy h:mm a' as the date format
 *          (date or time may be omitted).</td></tr>
 * <tr><td>PSXDomain</td>
 *     <td>(optional) the domain name of the host from which the URI is accessed.
 *          For instance, to set a cookie for any web server in the
 *          percussion.com domain, set the domain name to:
 *          <code>percussion.com</code>  To set a cookie for
 *          www.percussion.com, the domain name can be set to the
 *          full server name: <code>www.percussion.com</code></td></tr>
 * <tr><td>PSXPath</td>
 *     <td>(optional) only sends the cookie when accessing a URI under the
 *          specified path on the host. This includes the path and
 *          all descendents. For instance, using "/" matches all URI
 *          specifications on the host.</td></tr>
 * <tr><td>PSXSecure</td>
 *     <td>(optional) this is a boolean value. When set to "1", the cookie will
 *          only be sent when a secure (SSL) connection has been
 *          established. When set to "0" or "", any connection type
 *          is acceptable.</td></tr>
 * </table>
 * A sample usage is:
 * <table border="1">
 * <tr><th>Parameter</th><th>Value</th></tr>
 * <tr><td>PSXName</td>    <td>"MySessId2"</td></tr>
 * <tr><td>PSXValue</td>   <td>"1001"</td></tr>
 * <tr><td>PSXExpires</td> <td>"12/31/1999 11:59 p"</td></tr>
 * <tr><td>PSXDomain</td>   <td>"www.percussion.com"</td></tr>
 * <tr><td>PSXPath</td>    <td>"/"</td></tr>
 * <tr><td>PSXSecure</td>   <td>"1"</td></tr>
 * </table>
 * This will associate the cookie named MySessId2 with all
 * requests on the www.percussion.com web server. The cookie will
 * only be sent over secure (SSL) connections. It has a value of
 * 1001 and will expire on December 31, 1999 at 11:59:00 pm.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSSetCookieExtension implements IPSResultDocumentProcessor
{
   /* *************  IPSExtension Interface Implementation ************* */

   /**
    * No-op
    */
   public void init(IPSExtensionDef def, File codeRoot)
      throws PSExtensionException
   {}


   /* *******  IPSResultDocumentProcessor Interface Implementation ******* */
   /**
    * Return false, this extension can not modify the style sheet.
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * Associates a cookie with this response. The result XML document is
    * not modified in any way.
    *
    * @param      params      the parameters needed for this extension are
    *                         shown in the table below
    * <P>
    * <table border="1">
    * <tr><th>Parameter</th><th>Description</th></tr>
    * <tr><td>PSXName</td>
    *     <td>(required) the name of the cookie</td></tr>
    * <tr><td>PSXValue</td>
    *     <td>(required) the value of the cookie</td></tr>
    * <tr><td>PSXExpires</td>
    *     <td>(optional) the date the cookie expires. Use 'M/d/yyyy h:mm a' as
    *          the date format (date or time may be omitted).</td></tr>
    * <tr><td>PSXDomain</td>
    *     <td>(optional) the domain name of the host from which the URI is
    *          accessed. For instance, to set a cookie for any web server in the
    *          percussion.com domain, set the domain name to:
    *          <code>percussion.com</code>  To set a cookie for
    *          www.percussion.com, the domain name can be set to the
    *          full server name: <code>www.percussion.com</code></td></tr>
    * <tr><td>PSXPath</td>
    *     <td>(optional) only sends the cookie when accessing a URI under the
    *          specified path on the host. This includes the path and
    *          all descendents. For instance, using "/" matches all URI
    *          specifications on the host.</td></tr>
    * <tr><td>PSXSecure</td>
    *     <td>(optional) this is a boolean value. When set to "1", the cookie
    *          will only be sent when a secure (SSL) connection has been
    *          established. When set to "0" or "", any connection type
    *          is acceptable.</td></tr>
    * </table>
    *
    * @param      rc          the context of the request associated with this extension
    *
    * @param      resultDoc   the result XML document
    *
    * @return                     <code>resultDoc</code> is always returned
    *
    * @throws  PSParameterMismatchException  if the parameter number is incorrect
    * @throws  PSExtensionProcessingException      if the first two parameters are <code>null</code>
    */
   public Document processResultDocument(Object[] params, IPSRequestContext rc,
      Document resultDoc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      int size = (params == null) ? 0 : params.length;
      if (size != 6){ // six parameters are required
         throw new PSParameterMismatchException( 6, size );
      }

      if ((params[0] == null) || (params[1] == null)){
         String msg = "params[0] and [1] must not be null to call processResultDocument";
         IllegalArgumentException ex = new IllegalArgumentException(msg);
         throw new PSExtensionProcessingException( getClass().getName(), ex);
      }

      try {
         /* format of the cookie is:
           *
           * <name>=<value>[;path=<path>][;domain=<domain>][;expires=<expires>][;secure]
           *
           * We store name as the key, so value is simply <value>[;....]
           */
         String cookieName = params[0].toString();
         StringBuffer cookieValue = new StringBuffer();

         // start with the value
         cookieValue.append(PSURLEncoder.encodeQuery(params[1].toString()));

         // tack on expires if it was specified
         Object o = params[2];
         String sTemp = (o == null) ? "" : o.toString();
         if (sTemp.length() > 0) {
            cookieValue.append(";expires=");
            cookieValue.append(sTemp);
         }

         // tack on path if it was specified
         o = params[3];
         sTemp = (o == null) ? "" : o.toString();
         if (sTemp.length() > 0) {
            cookieValue.append(";domain=");
            cookieValue.append(sTemp);
         }

         // tack on path if it was specified
         o = params[4];
         sTemp = (o == null) ? "" : o.toString();
         if (sTemp.length() > 0) {
            cookieValue.append(";path=");
            cookieValue.append(sTemp);
         }

         // tack on path if it was specified
         o = params[5];
         sTemp = (o == null) ? "" : o.toString();
         if ("1".equals(sTemp)) {
            cookieValue.append(";secure");
         }

         // modify the response headers by adding the specified cookie
         rc.getResponseCookies().put(cookieName, cookieValue.toString());

         // and return the result document untouched
         return resultDoc;
      } catch (Exception e) {
         throw new PSExtensionProcessingException( getClass().getName(), e);
      }
   }
}

