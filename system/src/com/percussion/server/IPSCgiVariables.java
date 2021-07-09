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

package com.percussion.server;


/**
 * The IPSCgiVariables inteface is provided as a convenient mechanism
 * for accessing the names of the pre-defined CGI variables.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public interface IPSCgiVariables {
   /**
    * The server software version.
    */
   public static final String CGI_SERVER_SOFTWARE      = "SERVER-SOFTWARE";

   /**
    * The "extra" info sent with the request.
    */
   public static final String CGI_PATH_INFO            = "PATH-INFO";

   /**
    * The path with virtual-physical mappings applied.
    */
   public static final String CGI_PATH_TRANSLATED      = "PATH-TRANSLATED";

   /**
    * The virtual path of the executing script.
    */
   public static final String CGI_SCRIPT_NAME         = "SCRIPT-NAME";

   /**
    * The type of authentication performed.
    */
   public static final String CGI_AUTH_TYPE            = "AUTH-TYPE";

   /**
    * The user name submitted for authentication.
    */
   public static final String CGI_AUTH_USER_NAME      = "AUTH-USER";

   /**
    * The password submitted for authentication.
    */
   public static final String CGI_AUTH_PASSWORD         = "AUTH-PASSWORD";

   /**
    * The type of content sent with the request.
    */
   public static final String CGI_CONTENT_TYPE         = "Content-Type";

   /**
    * The length of the data (content) sent with the request.
    */
   public static final String CGI_CONTENT_LENGTH      = "Content-Length";

   /**
    * The MIME types supported by the client.
    */
   public static final String CGI_MIME_TYPES            = "Accept";

   /**
    * The type of software used to make the request.
    */
   public static final String CGI_REQUESTOR_SOFTWARE   = "User-Agent";

   /**
    * The key size used for the secure connection.
    */
   public static final String CGI_HTTPS_KEYSIZE         = "HTTPS-KEYSIZE";
   
   /**
    * The E2 specific request type variable.
    */
   public static final String   CGI_PS_REQUEST_TYPE = "PS-Request-Type";

   /**
    * The E2 specific application name variable.
    */
   public static final String   CGI_PS_APP_NAME         = "PS-Application";

   /**
    * The E2 specific data set name variable.
    */
   public static final String   CGI_PS_DATA_SET_NAME      = "PS-DataSet";

   /**
    * The filter sets this to "1" to indicate that a request came from
    * the filter.
    */
   public static final String REQ_FILTERED_CGIVAR = "PS-Relative-Req";

   /**
    * The HTTP/1.0 Pragma header which may contain:
    *
    */
   public static final String CGI_HTTP_PRAGMA         = "HTTP-PRAGMA";

   /**
    * The HTTP/1.1 Cache-Control header which may contain:
    *
    */
   public static final String CGI_HTTP_CACHE_CONTROL   = "HTTP-CACHE-CONTROL";

   /**
    * When SSL certificates are used to authenticate, this is the certificate
    * of the user making the request.
    */
   public static final String CGI_CERT_SUBJECT   = "CERT-SUBJECT";

   /**
    * When SSL certificates are used to authenticate, this is the certificate
    * of the entity who issued the certificate to the user making the
    * request.
    */
   public static final String CGI_CERT_ISSUER   = "CERT-ISSUER";
}

