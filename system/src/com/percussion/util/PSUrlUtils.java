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
package com.percussion.util;

import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;
import org.apache.commons.lang.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A utility class that contains methods useful for processing URLs. All
 * methods are static as it is never meant to be instantiated.
 */
public class PSUrlUtils
{
   /**
    * Builds a URL from its parts. The parts are combined in this way:
    * <p>base?p1=v1&p2=v2#anchor</p>
    * <p>where pN is the Nth parameter and vN is the Nth value
    *
    * @param base The root part of the URL. May contain a query string,
    *    and/or anchor. All parameters and the anchor must be url-encoded.
    *    The root part must not be <code>null</code> or empty.
    *
    * @param queryParams A set of Map.Entry objects, each of which contains
    * a param name (String) as the key and the param value (String) as the
    * value. If the key is <code>null</code>, the entry is ignored. The value
    * may be <code>null</code>. If <code>null</code>, no query string is added.
    * The params are added after any params already in the base part.
    *
    * @param anchor The anchor text. Only appended if not <code>null</code>
    *    and not empty. If the base had an anchor, it is replaced by this one.
    *    Handles presence of anchor seperator.
    *
    * @return The fully formed url as a String. Never <code>null</code>. If
    *    the base has an anchor and an anchor is supplied, the supplied anchor
    *    replaces the anchor in the base.
    *
    * @throws IllegalArgumentException if base is <code>null</code>,
    *    empty or only contains query and/or anchor.
    */
   public static String createUrl(String base, Iterator queryParams,
                                  String anchor)
   {
      if ( null == base || base.trim().length() == 0 )
         throw new IllegalArgumentException(
               "Base of URL can't be null or empty." );
      base = base.trim();

      String parsedProtocol = null;
      String parsedHost = null;
      String parsedPort = null;
      String parsedFile = null;
      URL parsedUrl = null;
      try
      {
         parsedUrl = new URL(base);

         parsedProtocol = parsedUrl.getProtocol();
         parsedHost = parsedUrl.getHost();
         parsedPort = String.valueOf(parsedUrl.getPort());
         parsedFile = parsedUrl.getFile();
      }
      catch (MalformedURLException e)
            { } // ignore, we don't have a fully qualified URL
      if (parsedFile != null)
         base = parsedFile;

      int anchorPos = base.lastIndexOf(ANCHOR_SEP);
      String suppliedAnchor = null;
      if (anchor != null)
         suppliedAnchor = PSURLEncoder.encodeQuery(anchor);
      else if (anchorPos >= 0)
         suppliedAnchor = base.substring(anchorPos+1, base.length());
      if (suppliedAnchor != null && suppliedAnchor.trim().length() == 0)
         suppliedAnchor = null;

      int queryPos = base.indexOf(QUERY_SEP);
      if (base.length() == 0 || queryPos == 0 || anchorPos == 0 ||
          anchorPos == queryPos+1)
         throw new IllegalArgumentException( "Base must include root part." );

      String suppliedParams = null;
      if (queryPos >= 0)
      {
         if (anchorPos >= 0)
            suppliedParams = base.substring(queryPos+1, anchorPos);
         else if (queryPos < base.length()-1)
            suppliedParams = base.substring(queryPos+1);
      }

      String baseUrl = base;
      if (queryPos >= 0)
         baseUrl = baseUrl.substring(0, queryPos);
      else if (anchorPos >= 0)
         baseUrl = baseUrl.substring(0, anchorPos);

      StringBuffer finalUrl = new StringBuffer();
      if (parsedProtocol != null && parsedProtocol.length() > 0)
      {
         finalUrl.append(parsedProtocol);
         finalUrl.append(":");
      }
      if (parsedHost != null && parsedHost.length() > 0)
      {
         finalUrl.append("//");
         finalUrl.append(parsedHost);
         if (parsedPort != null && !parsedPort.equals("-1"))
         {
            if (!parsedPort.equals("80") && !parsedPort.equals("443"))
            {
               finalUrl.append(":");
               finalUrl.append(parsedPort);
            }
         }
      }

      finalUrl.append(PSURLEncoder.encodePath(baseUrl));
      if (suppliedParams != null ||
          (queryParams != null && queryParams.hasNext()))
         finalUrl.append(QUERY_SEP);

      if (suppliedParams != null)
      {
         int strLength = suppliedParams.length();
         if (suppliedParams.endsWith(PARAM_SEP))
            --strLength;

         finalUrl.append(suppliedParams.substring(0, strLength));
      }

      if (null != queryParams)
      {
         String delim = "";
         if (queryParams.hasNext() && suppliedParams != null)
            delim = PARAM_SEP;

         while (queryParams.hasNext())
         {
            Map.Entry entry = (Map.Entry) queryParams.next();
            String param = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (null != param)
            {
               finalUrl.append(delim);
               finalUrl.append(PSURLEncoder.encodeQuery(param));
               finalUrl.append(PARAM_VALUE_SEP);
               if (null != value)
                  finalUrl.append(PSURLEncoder.encodePath(value));
               delim = PARAM_SEP;
            }
         }
      }

      if (suppliedAnchor != null && suppliedAnchor.length() > 0)
         finalUrl.append(ANCHOR_SEP + suppliedAnchor);

      return finalUrl.toString();
   }

   /**
    * Convenience method that calls {@link #createUrl(String, Integer, String,
    * Iterator, String, IPSRequestContext, boolean) createUrl(host, port,
    * partialUrl, queryParams, anchor, request, true)}
    *
    * @throws MalformedURLException if any of the pieces can't be converted
    *    to a URL.
    *
    * @see #createUrl(String, Integer, String,
    * Iterator, String, IPSRequestContext, boolean)
    *
    */
   public static URL createUrl(String host, Integer port, String partialUrl,
                               Iterator queryParams, String anchor,
                               IPSRequestContext request)
         throws MalformedURLException
   {
      return createUrl(host, port, partialUrl, queryParams, anchor, request,
                       true);
   }

   /**
    * Creates a URL from the supplied parameters and returns it.  Will
    * optionally create the url to use the HTTPS protocol if the supplied
    * request context resulted from an HTTPS request.
    * <p>A URI has the following pieces for purposes of this description
    * (see RFC 2396 for more details):
    * <p>
    * <p>&lt;scheme&gt;://&lt;host&gt;&lt;path-segments&gt;
    *    &lt;resource&gt;?&lt;query&gt;#&lt;fragment&gt;
    * <p>
    * <p>All parts except resource are optional.
    * <p>Five basic forms are allowed for the supplied URI:
    * <ul>
    * <li>Fully qualified (e.g.
    * http://server:9992/Rhythmyx/approot/res.html</li>
    * <li>Partially qualified (e.g. /Rhythmyx/approot/res.html)</li>
    * <li>Relative (e.g. ../myApp/res.html)</li>
    * <li>Resource name only (e.g. res.html)</li>
    * <li>An empty string.</li>
    * </ul>
    * <p>Any of these forms may contain a query and fragment part.  Any relative
    * url is assumed to be relative from the orginating request's app root.
    * <p>
    * If the supplied URL is fully qualified and the protocol is not 'http(s)',
    * the supplied URL will be returned, unmodified. Otherwise, any pieces
    * supplied will be used in the result.  If the supplied URL is
    * not fully qualified, the missing parts will be added as follows:
    * <ul>
    * <li>For a partially
    * qualified name, the http(s) protocol, server and port will be added to the
    * supplied name.</li>
    * <li>For an unqualified name, those same items plus the Rhythmyx
    * request root and the originating application request root will be
    * added.</li>
    * <li>For a relative name, original request protocol (http or https),
    * server, port, and Rhythmyx root will be added, assuming it is relative
    * to the origination application's request root.</li>
    * <li>For an empty string, all parts of the originating request will be
    * used, substituting the supplied parameters. If the port is 80 and http
    * is being used, no port number will be added to the generated url. If
    * server and port are not provided, they will come from the current
    * session, subject to the value of the <code>useSecure</code>
    * parameter (see that parameter's documentation for more info).</li>
    *</ul>
    * @param host The hostname to use.  May be <code>null</code>.  If not
    * specified, the hostname of the originating request will be used.
    *
    * @param port The port to use.  May be <code>null</code>.  If not specified,
    * the port of the originating request will be used, subject to the value
    * of the <code>useSecure</code> parameter (see that parameter's
    * documentation for more info).  If specified, it will always be used
    * regardless of the value of other parameters.
    *
    * @param partialUrl As much or as little of the URL as you wish to specify.
    * Supplied pieces will be filled in as specified above. If a <code>null
    * </code> or empty string is provided, then the request url that generated
    * the current request (up through the request page) will be used. If a
    * relative URL is specified, it is assumed to be relative to the originating
    * request app root. Any query parameters in the url must be url-encoded
    * and are retained.
    *
    * @param queryParams A set of Map.Entry objects, each of which contains
    * a param name (String) as the key and the param value (String) as the
    * value. If the key is <code>null</code>, the entry is ignored. The value
    * may be <code>null</code>. If <code>null</code>, no query string is added.
    * The params are added after any params already in the base part.

    * @param anchor The anchor text. Only appended if not <code>null</code>
    * and not empty. If the base had an anchor, it is replaced by this one.
    * Handles presence of anchor seperator.
    *
    * @param request The request context of the originating request.  May not
    * be <code>null</code>.
    *
    * @param allowHttps Indicates whether to allow the https protocol.
    * If <code>true</code>, then https will be used if either
    * the originating request used https or if the supplied
    * <code>partialUrl</code> specifies the https protocol.  The following table
    * shows the resulting protocol used based on combinations of input
    * parameter values.  Note: if the <code>port</code> parameter is not
    * <code>null</code>, that value will be used regardless of the other
    * parameter values.
    * <p>
    * The table below shows the resulting protocol from various combinations of
    * parameter values.
    * <table border="1">
    * <tr>
    * <th><code>allowHttps</code></th><th>Original Request Protocol</th><th>
    *    <code>partialUrl</code> protocol</th><th>Resulting protocol</th>
    *    <th>Notes</th>
    * </tr>
    * <tr>
    * <td>false</td><td>HTTP</td><td>none</td><td>HTTP</td><td>&nbsp;</td>
    * </tr>
    * <tr>
    * <td>false</td><td>HTTPS</td><td>none</td><td>HTTP</td><td>use Rx server's
    *    default port</td>
    * </tr>
    * <tr>
    * <td>true</td><td>HTTP</td><td>none</td><td>HTTP</td><td>&nbsp;</td>
    * </tr>
    * <tr>
    * <td>true</td><td>HTTPS</td><td>none</td><td>HTTPS</td><td>&nbsp;</td>
    * </tr>
    * <tr>
    * <td>false</td><td>HTTP</td><td>HTTP</td><td>HTTP</td><td>&nbsp;</td>
    * </tr>
    * <tr>
    * <td>false</td><td>HTTPS</td><td>HTTP</td><td>HTTP</td><td>use Rx server's
    *    default port</td>
    * </tr>
    * <tr>
    * <td>false</td><td>HTTP</td><td>HTTPS</td><td>HTTP</td><td>use originating
    *    request's port</td>
    * </tr>
    * <tr>
    * <td>false</td><td>HTTPS</td><td>HTTPS</td><td>HTTP</td><td>use Rx server's
    *    default port</td>
    * </tr>
    * <tr>
    * <td>true</td><td>HTTP</td><td>HTTP</td><td>HTTP</td><td>&nbsp;</td>
    * </tr>
    * <tr>
    * <td>true</td><td>HTTPS</td><td>HTTP</td><td>HTTP</td><td><code>partialUrl
    *    </code> value overrides originating request protocol</td>
    * </tr>
    * <tr>
    * <td>true</td><td>HTTP</td><td>HTTPS</td><td>HTTPS</td><td><code>partialUrl
    *    </code> value overrides originating request protocol</td>
    * </tr>
    * <tr>
    * <td>true</td><td>HTTPS</td><td>HTTPS</td><td>HTTPS</td><td>&nbsp;</td>
    * </tr>
    * </table>
    *
    * @return The fully formed URL; never <code>null</code>. If the base
    * has an anchor and an anchor is supplied, the supplied anchor
    * replaces the anchor in the base.  If the protocol specified is not http
    * or https, then the supplied partialUrl is returned instead.
    *
    * @throws IllegalArgumentException if request is <code>null</code>.
    *
    * @throws MalformedURLException if any of the pieces can't be converted
    *    to a URL.
    */
   public static URL createUrl(String host, Integer port, String partialUrl,
                               Iterator queryParams, String anchor,
                               IPSRequestContext request,
                               boolean allowHttps)
         throws MalformedURLException
   {
      // TODO Switch to using a regular expression to determine if a protocol has
      // been supplied by <code>partialUrl</code> when we move to JDK 1.4

      // validate params
      if (request == null)
         throw new IllegalArgumentException("request may not be null");

      if (partialUrl == null)
         partialUrl = "";
      partialUrl = partialUrl.trim();

      boolean passedHost = false;
      boolean passedPort = false;

      URL parsedUrl = null;
      if (partialUrl != null && partialUrl.trim().length() > 0)
      {
         /*
         * Need to determine if the supplied URL starts with a protocol and
         * only try to construct a URL object in this case.  Need a URL object
         * to return if a protocol other than http or https is used.  Need a
         * URL object if protocol is http or https to easily parse the supplied
         * string.  RFC 1808 gives the following parsing rule ("scheme" means
         * protocol in this case):
         *
         * "If the parse string contains a colon ":" after the first character
         * and before any characters not allowed as part of a scheme name
         * (i.e., any not an alphanumeric, plus "+", period ".", or
         * hyphen "-"), the <scheme> of the URL is the substring of characters
         * up to but not including the first colon.
         *
         * When we move to jdk 1.4, we can use a regular expression to do this.
         * For now, we use the simple rule that if there is a colon ":", and it
         * appears before any forward slash "/" and the query string if one is
         * supplied (begun by a "?"), then the String begins with the protocol.
         *
         * TODO: once we move to jdk1.4, the following regular expression will
         * grep the string for the protocol: "^[A-Za-z0-9\+\.\-]+:"
          */
         int colonPos = partialUrl.indexOf(PROTOCOL_SEP);
         if (colonPos > 0)
         {
            int slashPos = partialUrl.indexOf(PATH_SEP);
            int queryPos = partialUrl.indexOf(QUERY_SEP);
            int firstBadChar = ((slashPos != -1) && (slashPos < queryPos)) ?
                               slashPos : queryPos;
            if (colonPos < firstBadChar || firstBadChar == -1)
            {
               try
               {
                  parsedUrl = new URL( partialUrl );
                  if (!DEFAULT_PROTOCOL.equalsIgnoreCase(
                        parsedUrl.getProtocol()) &&
                        !SECURE_PROTOCOL.equalsIgnoreCase(
                        parsedUrl.getProtocol()))
                     return parsedUrl;
               }
               catch ( MalformedURLException e )
                     { } // ignore, we don't have a fully qualified URL
            }
         }
      }


      // see what we need
      boolean wasHttps = false;
      String origProtocol = request.getOriginalProtocol();
      if (origProtocol.equalsIgnoreCase("https"))
         wasHttps = true;

      if (host != null && host.trim().length() > 0)
      {
         passedHost = true;
         host = host.trim();
         if (host.equals("127.0.0.1") || host.equalsIgnoreCase("localhost"))
         {
            /*
              This is to try to prevent bugs such as this one:
              Rx-03-06-0087 - "Content Explorer doesn't work under SSL".

              Since the localhost was passed in, there is no reason that we
              should use SSL. So, in this case we want to make it to always use
              regular http protocol and regular server port.
            */
            wasHttps = false;
            allowHttps = false;
            port = new Integer(PSServer.getListenerPort());
         }
      }
      else
      {
         String tempHostName = PSServer.getProperty("publicCmsHostname");
         if (StringUtils.isNotBlank(tempHostName)) {
            host = tempHostName;
         }
         else {
             // TODO: Find out if this property would make more sense: PSServer.getServerName();
            host = request.getOriginalHost();
         }
      }

      /* if switching from https to http, use server's default port, not the
      * one from the request.  If they passed a port, that's what we'll use.
       */
      int iPort;
      if (port != null)
      {
         iPort = port.intValue();
         passedPort = true;
      }
      else if ((!wasHttps) || (wasHttps && !allowHttps))
      {
         iPort = PSServer.getListenerPort();
      }
      else
      {
         int sslListenerPort = PSServer.getSslListenerPort();
         iPort = sslListenerPort <= 0 ? request.getOriginalPort() : sslListenerPort;
      }

      // process the partial url
      String suppliedAnchor = null;
      int pos = partialUrl.lastIndexOf(ANCHOR_SEP);
      if (pos >= 0)
      {
         suppliedAnchor = partialUrl.substring(pos+1);
         partialUrl = partialUrl.substring(0, pos);
      }

      String suppliedParams = null;
      pos = partialUrl.lastIndexOf(QUERY_SEP);
      if (pos >= 0)
      {
         suppliedParams = partialUrl.substring(pos);
         partialUrl = partialUrl.substring(0, pos);
      }

      /* see if it is relative - if so strip off any "../" that correspond to
      * levels of the originating request app root to get the new approot.
      * This does not ensure that the resulting request will not refer to
      * something outside of the Rhythmyx server.
      *
      * For example, if the originating request was:
      * //server:port/Rhythmyx/Approot/resource.htm
      * and we are passed:
      * ../App2/resource.htm
      * the result will be:
      * //server:port/Rhythmyx/App2/resource.htm
      *
      * However, if we are passed:
      * ../../App2/resource.htm
      * the result will be:
      * //server:port/Rhythmyx/../App2/resource.htm
      *
       */
      boolean hasAppRoot = false;
      pos = partialUrl.indexOf(RELATIVE_PREFIX);
      if (pos == 0)
      {
         hasAppRoot = true;
         // get the number of pieces in the app root
         StringTokenizer tok = new StringTokenizer(request.getRequestRoot(),
               PATH_SEP);
         // token count includes RxRoot and the resource
         int numEl = tok.countTokens() - 1;
         /* walk the url and remove each "../" that can correspond to part of
         * the app root - this can be all but the resource itself
          */
         int relLen = RELATIVE_PREFIX.length();
         for (int i = 0; i < numEl; i++)
         {
            if (partialUrl.substring(pos, relLen).equals(
                  RELATIVE_PREFIX))
         {
            pos += relLen;
         }
         if ((pos + relLen) > partialUrl.length())
            break;
         }

         // in case we were just passed "../"
         if (pos >= partialUrl.length())
            partialUrl = "";
         else
            partialUrl = partialUrl.substring(pos);
      }

      // now build our pieces
      String parsedProtocol = null;
      String parsedHost = null;
      String parsedPort = null;
      String parsedFile = null;

      // see if fully qualified
      PSServerConfiguration serverConfig = PSServer.getServerConfiguration();
      if (parsedUrl != null)
      {
         parsedProtocol = parsedUrl.getProtocol();
         parsedHost = parsedUrl.getHost();
         int p = parsedUrl.getPort();
         if ( p != -1 )
            parsedPort = String.valueOf(p);
         parsedFile = parsedUrl.getFile();
         /*
          * This will include the supplied params also - chop them. These will
          * be added later anyway.
          */
         int index = parsedFile.indexOf(QUERY_SEP);
         if(index!=-1)
            parsedFile = parsedFile.substring(0,index);
      }
      else
      {
         if (partialUrl.length() == 0)
         {
            // we've got nothing, use the originating data
            parsedFile = request.getRequestFileURL();
         }
         else if (partialUrl.charAt(0) == PATH_SEP.charAt(0))
         {
            // it's absolute, just use it
            parsedFile = partialUrl;
         }
         else
         {
            // we have a relative url
            if (!hasAppRoot)
            {
               // prepend full request root
               parsedFile = request.getRequestRoot() + PATH_SEP + partialUrl;
            }
            else
            {
               // this means we have approot/resource - prepend server root
               parsedFile = serverConfig.getRequestRoot() + PATH_SEP +
                            partialUrl;
            }
         }
      }


      // should have all the pieces, put it together
      StringBuffer finalUrl = new StringBuffer();
      boolean isSecure = wasHttps && allowHttps;
      if (parsedProtocol != null && parsedProtocol.equalsIgnoreCase(
            SECURE_PROTOCOL))
      {
         if (allowHttps)
         {
            // they've specified https via the url
            isSecure = true;
         }
         else
         {
            /* url was fully qualified as https, so we need to replace the port
            * we parsed.  If they specified a port, that will still be used.
             */
            isSecure = false;
            if (!wasHttps)
               parsedPort = String.valueOf(request.getOriginalPort());
            else
               parsedPort = String.valueOf(PSServer.getListenerPort());
         }
      }

      if (isSecure)
         finalUrl.append(SECURE_PROTOCOL);
      else
         finalUrl.append(DEFAULT_PROTOCOL);

      finalUrl.append("://");

      // add host
      if (passedHost || parsedHost == null)
         finalUrl.append(host);
      else
         finalUrl.append(parsedHost);

      // add port
      if (passedPort || parsedPort == null)
      {
         if ((iPort != 80 &&  iPort != 443) || isSecure)
         {
            finalUrl.append(':');
            finalUrl.append(iPort);
         }
      }
      else
      {
         if ((!parsedPort.equals("80") && !parsedPort.equals("443")) || isSecure)
         {
            finalUrl.append(':');
            finalUrl.append(parsedPort);
         }
      }

      // add file (rxroot/app/resource) - check if leading "/" is required
      if (!(parsedFile.length() > 0 && parsedFile.charAt(0) ==
            PATH_SEP.charAt(0)))
      {
         finalUrl.append(PATH_SEP);
      }
      finalUrl.append(PSURLEncoder.encodePath(parsedFile));

      //append the suplied parameters back
      String sep = null;
      if (suppliedParams != null)
      {
         finalUrl.append(suppliedParams);
         sep = PARAM_SEP;
      }
      else
         sep = QUERY_SEP;

      // add params
      if (queryParams != null && queryParams.hasNext())
      {
         while (queryParams.hasNext())
         {
            Map.Entry entry = (Map.Entry)queryParams.next();
            String param = (String)entry.getKey();
            String value = entry.getValue() == null ? null : entry.getValue()
                  .toString();

            if (null != param)
            {
               finalUrl.append(sep);
               finalUrl.append(PSURLEncoder.encodeQuery(param));
               finalUrl.append(PARAM_VALUE_SEP);
               if (null != value)
                  finalUrl.append(PSURLEncoder.encodePath(value));
               sep = PARAM_SEP;
            }
         }
      }

      // add anchor
      if (anchor != null)
      {
         finalUrl.append(ANCHOR_SEP);
         finalUrl.append(PSURLEncoder.encodeQuery(anchor));
      }
      else if (suppliedAnchor != null)
      {
         finalUrl.append(ANCHOR_SEP);
         finalUrl.append(suppliedAnchor);
      }

      return new URL(finalUrl.toString());
   }

   // test
   public static void main(String[] args)
   {
      String base = "test/base.htm?par1=val1&par2=val2#anchor";
      String anchor = "a";
      LinkedHashMap map = new LinkedHashMap(0,1,false);
      map.put("p=1", "v:+1");
      map.put("p2", "v2");
      map.put("p3", null);
      map.put(null, "v1");
      System.out.println("...createUrl= " + createUrl(base,
            map.entrySet().iterator(), anchor));
      System.out.println("\n\n");

      anchor = "";
      System.out.println("...createUrl= " + createUrl(base,
            map.entrySet().iterator(), anchor));
      System.out.println("\n\n");

      anchor = null;
      System.out.println("...createUrl= " + createUrl(base,
            map.entrySet().iterator(), anchor));
      System.out.println("\n\n");

      base = "test/base.htm?par1=val1&par2=val2";
      System.out.println("...createUrl= " + createUrl(base,
            map.entrySet().iterator(), anchor));
      System.out.println("\n\n");

      base = "test/base.htm";
      System.out.println("...createUrl= " + createUrl(base,
            map.entrySet().iterator(), anchor));
      System.out.println("\n\n");

      anchor = "a";
      base = "test/base.htm?par1=val1&par2=val2";
      System.out.println("...createUrl= " + createUrl(base,
            map.entrySet().iterator(), anchor));
      System.out.println("\n\n");

      base = "test/base.htm";
      System.out.println("...createUrl= " + createUrl(base,
            map.entrySet().iterator(), anchor));
      System.out.println("\n\n");

      base = "http://server:9992/te&t/base.htm";
      System.out.println("...createUrl= " + createUrl(base,
            map.entrySet().iterator(), anchor));
      System.out.println("\n\n");

      base = "http://server/te=t/base.htm";
      System.out.println("...createUrl= " + createUrl(base,
            map.entrySet().iterator(), anchor));
      System.out.println("\n\n");

      base = "file:test/base.htm";
      System.out.println("...createUrl= " + createUrl(base,
            map.entrySet().iterator(), anchor));
      System.out.println("\n\n");

      base = "file:/test/base.htm";
      System.out.println("...createUrl= " + createUrl(base,
            map.entrySet().iterator(), anchor));
      System.out.println("\n\n");

      base = "file:/test/base.htm";
      System.out.println("...createUrl= " + createUrl(base,
            PSIteratorUtils.emptyIterator(), null));
      System.out.println("\n\n");

      base = "file:/test/base.htm?";
      System.out.println("...createUrl= " + createUrl(base,
            map.entrySet().iterator(), anchor));
      System.out.println("\n\n");

      base = "file:/test/base.htm#";
      System.out.println("...createUrl= " + createUrl(base,
            map.entrySet().iterator(), anchor));
      System.out.println("\n\n");

      base = "file:/test/base.htm#anchorText";
      System.out.println("...createUrl= " + createUrl(base,
            map.entrySet().iterator(), anchor));
      System.out.println("\n\n");

      System.exit(0);
   }
   
   /**
    * Parse the url parameter value for the supplied name from the provided url.
    * 
    * @param url the url from which to parse the parameter value, not
    *    <code>null</code> or empty.
    * @param name the parameter name for which to parse the value from the 
    *    supplied url, not <code>null</code> or empty.
    * @return the parameter value, may be <code>null</code> if not found.
    */
   public static String getUrlParameterValue(String url, String name)
   {
      if (url == null || url.trim().length() == 0)
         throw new IllegalArgumentException("url cannot be null or empty");

      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name cannot be null or empty");

      int pos = url.indexOf(name);
      if (pos != -1)
      {
         int startPos = url.indexOf(PARAM_VALUE_SEP, pos);
         if (startPos != -1)
         {
            startPos += PARAM_VALUE_SEP.length();
            int endPos = url.indexOf(PARAM_SEP, startPos);
            if (endPos == -1)
               return url.substring(startPos);
            else
               return url.substring(startPos, endPos);
         }
      }
      
      return null;
   }

   /**
    * Replace the url parameter value for the named parameter with the supplied
    * value. Does nothing to the url if the parameter is not found.
    * 
    * @param url the url in which to replace the supplied parameter value,
    *    not <code>null</code> or empty.
    * @param name the name of the parameter for which to replace the value,
    *    not <code>null</code> or empty.
    * @param value the new parameter value, not <code>null</code>, may be 
    *    empty.
    * @return the url with the replaced parameter, never <code>null</code>, 
    *    may be empty.
    */
   public static String replaceUrlParameterValue(String url, String name, 
      String value)
   {
      if (url == null || url.trim().length() == 0)
         throw new IllegalArgumentException("url cannot be null or empty");

      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name cannot be null or empty");

      if (value == null)
         throw new IllegalArgumentException("value cannot be null");
      
      int pos = url.indexOf(name);
      if (pos != -1)
      {
         int startPos = url.indexOf(PARAM_VALUE_SEP, pos);
         if (startPos != -1)
         {
            startPos += PARAM_VALUE_SEP.length();
            int endPos = url.indexOf(PARAM_SEP, startPos);
            if (endPos == -1)
               return url.substring(0, startPos) + value;
            else
               return url.substring(0, startPos) + value + 
                  url.substring(endPos);
         }
      }
      
      return url;
   }

   /**
    * Private so it can never be instantiated.
    */
   private PSUrlUtils() {}

   /**
    * The character that seperates the base part of the URL from the query
    * string.
    */
   private static final String QUERY_SEP = "?";

   /**
    * The character that seperates each param/value pairing from each other.
    */
   private static final String PARAM_SEP = "&";

   /**
    * The character that seperates the anchor from the rest of the URL.
    */
   private static final String ANCHOR_SEP = "#";

   /**
    * The character that seperates the param from the value.
    */
   private static final String PARAM_VALUE_SEP = "=";

   /**
    * The character that separates part of the base url
    */
   private static final String PATH_SEP = "/";

   /**
    * The string that indicates a relative url.
    */
   private static final String RELATIVE_PREFIX = "../";

   /**
    * The default protocol, http.
    */
   private static final String DEFAULT_PROTOCOL="http";

   /**
    * The secure protocol, https.
    */
   private static final String SECURE_PROTOCOL="https";

   /**
    * The string that follows the protocol portion of a url.
    */
   private static final String PROTOCOL_SEP = ":";
}
