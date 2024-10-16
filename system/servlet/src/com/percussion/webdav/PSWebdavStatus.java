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
package com.percussion.webdav;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This class contains WebDAV status code.
 */
public class PSWebdavStatus
{
   /**
    * Get status text from a given status code.
    * 
    * @param statusCode THe status code.
    * 
    * @return The status text of the given code. It is never <code>null</code>,
    *    but may be empty if cannot find the status code.
    */
   public static String getStatusText(int statusCode)
   {
     Integer key = new Integer(statusCode);
     if(!ms_mapStatusCodes.containsKey(key))
         return "";
     else
         return (String)ms_mapStatusCodes.get(key);
   }

   /**
    * Adds the code and its corresponding text to the internal map.
    * 
    * @param code The status code.
    * 
    * @param text The status text corresponding to the status code, assume it
    *    is not <code>null</code>.
    */
   private static void addStatusCodeMap(int code, String text)
   {
     ms_mapStatusCodes.put(new Integer(code), text);
   }
   
   /**
    * The status code map, it maps the status code (as the key in 
    * <code>Integer</code>) to the status text (as the value in 
    * <code>String</code>).
    */
   private static ConcurrentHashMap ms_mapStatusCodes = new ConcurrentHashMap();
   
   // The status codes
   public static final int SC_CONTINUE = 100;
   public static final int SC_SWITCHING_PROTOCOLS = 101;
   public static final int SC_PROCESSING = 102;
   public static final int SC_OK = 200;
   public static final int SC_CREATED = 201;
   public static final int SC_ACCEPTED = 202;
   public static final int SC_NON_AUTHORITATIVE_INFORMATION = 203;
   public static final int SC_NO_CONTENT = 204;
   public static final int SC_RESET_CONTENT = 205;
   public static final int SC_PARTIAL_CONTENT = 206;
   public static final int SC_MULTI_STATUS = 207;
   public static final int SC_MULTIPLE_CHOICES = 300;
   public static final int SC_MOVED_PERMANENTLY = 301;
   public static final int SC_MOVED_TEMPORARILY = 302;
   public static final int SC_SEE_OTHER = 303;
   public static final int SC_NOT_MODIFIED = 304;
   public static final int SC_USE_PROXY = 305;
   public static final int SC_BAD_REQUEST = 400;
   public static final int SC_UNAUTHORIZED = 401;
   public static final int SC_PAYMENT_REQUIRED = 402;
   public static final int SC_FORBIDDEN = 403;
   public static final int SC_NOT_FOUND = 404;
   public static final int SC_METHOD_NOT_ALLOWED = 405;
   public static final int SC_NOT_ACCEPTABLE = 406;
   public static final int SC_PROXY_AUTHENTICATION_REQUIRED = 407;
   public static final int SC_REQUEST_TIMEOUT = 408;
   public static final int SC_CONFLICT = 409;
   public static final int SC_GONE = 410;
   public static final int SC_LENGTH_REQUIRED = 411;
   public static final int SC_PRECONDITION_FAILED = 412;
   public static final int SC_REQUEST_TOO_LONG = 413;
   public static final int SC_UNSUPPORTED_MEDIA_TYPE = 415;
   public static final int SC_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
   public static final int SC_EXPECTATION_FAILED = 417;
   public static final int SC_INSUFFICIENT_SPACE_ON_RESOURCE = 419;
   public static final int SC_METHOD_FAILURE = 420;
   public static final int SC_UNPROCESSABLE_ENTITY = 422;
   public static final int SC_LOCKED = 423;
   public static final int SC_FAILED_DEPENDENCY = 424;
   public static final int SC_INTERNAL_SERVER_ERROR = 500;
   public static final int SC_NOT_IMPLEMENTED = 501;
   public static final int SC_BAD_GATEWAY = 502;
   public static final int SC_SERVICE_UNAVAILABLE = 503;
   public static final int SC_GATEWAY_TIMEOUT = 504;
   public static final int SC_HTTP_VERSION_NOT_SUPPORTED = 505;
   public static final int SC_INSUFFICIENT_STORAGE = 507;

   // the status code & text map
   static
   {
      addStatusCodeMap(SC_OK,                     "OK");
      addStatusCodeMap(SC_CREATED,                "Created");
      addStatusCodeMap(SC_ACCEPTED,               "Accepted");
      addStatusCodeMap(SC_NO_CONTENT,             "No Content");
      addStatusCodeMap(SC_MOVED_PERMANENTLY,      "Moved Permanently");
      addStatusCodeMap(SC_MOVED_TEMPORARILY,      "Moved Temporarily");
      addStatusCodeMap(SC_NOT_MODIFIED,           "Not Modified");
      addStatusCodeMap(SC_BAD_REQUEST,            "Bad Request");
      addStatusCodeMap(SC_UNAUTHORIZED,           "Unauthorized");
      addStatusCodeMap(SC_FORBIDDEN,              "Forbidden");
      addStatusCodeMap(SC_NOT_FOUND,              "Not Found");
      addStatusCodeMap(SC_INTERNAL_SERVER_ERROR,  "Internal Server Error");
      addStatusCodeMap(SC_NOT_IMPLEMENTED,        "Not Implemented");
      addStatusCodeMap(SC_BAD_GATEWAY,            "Bad Gateway");
      addStatusCodeMap(SC_SERVICE_UNAVAILABLE,    "Service Unavailable");
      addStatusCodeMap(SC_CONTINUE,               "Continue");
      addStatusCodeMap(SC_METHOD_NOT_ALLOWED,     "Method Not Allowed");
      addStatusCodeMap(SC_CONFLICT,               "Conflict");
      addStatusCodeMap(SC_PRECONDITION_FAILED,    "Precondition Failed");
      addStatusCodeMap(SC_REQUEST_TOO_LONG,       "Request Too Long");
      addStatusCodeMap(SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type");
      addStatusCodeMap(SC_SWITCHING_PROTOCOLS,    "Switching Protocols");
      addStatusCodeMap(SC_RESET_CONTENT,          "Reset Content");
      addStatusCodeMap(SC_GATEWAY_TIMEOUT,        "Gateway Timeout");
      addStatusCodeMap(SC_PROCESSING,             "Processing");
      addStatusCodeMap(SC_MULTI_STATUS,           "Multi-Status");
      addStatusCodeMap(SC_UNPROCESSABLE_ENTITY,   "Unprocessable Entity");
      addStatusCodeMap(SC_METHOD_FAILURE,         "Method Failure");
      addStatusCodeMap(SC_LOCKED,                 "Locked");
      addStatusCodeMap(SC_INSUFFICIENT_STORAGE,   "Insufficient Storage");
      addStatusCodeMap(SC_FAILED_DEPENDENCY,      "Failed Dependency");
      addStatusCodeMap(SC_HTTP_VERSION_NOT_SUPPORTED, "Http Version Not Supported");
      addStatusCodeMap(SC_INSUFFICIENT_SPACE_ON_RESOURCE, "Insufficient Space On Resource");
      addStatusCodeMap(SC_NON_AUTHORITATIVE_INFORMATION, "Non Authoritative Information");
   }
}
