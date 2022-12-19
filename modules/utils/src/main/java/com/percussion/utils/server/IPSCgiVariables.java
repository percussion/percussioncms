/*
 * Copyright 1999-2022 Percussion Software, Inc.
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

package com.percussion.utils.server;


/**
 * The IPSCgiVariables interface is provided as a convenient mechanism
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

