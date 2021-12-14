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

package com.percussion.webdav;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * WebDAV constants.
 */
public interface IPSWebdavConstants {


   //--------------------------
   // WebDAV specific constants
   //--------------------------
      
   /** URI schemes */
   String S_DAV_NAMESPACE        = "DAV:";  // WebDAV namespace URI
   String S_DAV_ALIAS            = "D";     // The alias of the above namespace
   String S_LOCK_TOKEN           = "opaquelocktoken:"; // LOCK feature
   
   /** Methods */
   String M_CONNECT              = "CONNECT";
   String M_COPY                 = "COPY";
   String M_DELETE               = "DELETE";
   String M_GET                  = "GET";
   String M_HEAD                 = "HEAD";
   String M_LOCK                 = "LOCK";
   String M_MKCOL                = "MKCOL";
   String M_MOVE                 = "MOVE";
   String M_OPTIONS              = "OPTIONS";
   String M_POST                 = "POST";
   String M_PROPFIND             = "PROPFIND";
   String M_PROPPATCH            = "PROPPATCH";
   String M_PUT                  = "PUT";
   String M_TRACE                = "TRACE";
   String M_UNLOCK               = "UNLOCK";
   
   /** Headers */
   String H_CACHE_CONTROL        = "Cache-Control";
   String H_DAV                  = "DAV";
   String H_DEPTH                = "Depth";
   String H_DESTINATION          = "Destination";
   String H_IF                   = "If";
   String H_LASTMODIFIED         = "Last-Modified";
   String H_LOCK_TOKEN           = "Lock-Token";
   String H_LOCATION             = "Location";
   String H_OVERWRITE            = "Overwrite";
   String H_STATUS_URI           = "Status-URI";
   String H_TIMEOUT              = "Timeout";
   
   /** XML Elements */
   String E_ACTIVELOCK           = "activelock";
   String E_ALLPROP              = "allprop";
   String E_COLLECTION           = "collection";
   String E_DEPTH                = "depth";
   String E_DST                  = "dst";
   String E_EXCLUSIVE            = "exclusive";
   String E_ERROR                = "error";
   String E_HREF                 = "href";
   String E_KEEPALIVE            = "keepalive";
   String E_LINK                 = "link";
   String E_LOCKDISCOVERY        = "lockdiscovery";
   String E_LOCKENTRY            = "lockentry";
   String E_LOCKINFO             = "lockinfo";
   String E_LOCKSCOPE            = "lockscope";
   String E_LOCKTOKEN            = "locktoken";
   String E_LOCKTYPE             = "locktype";
   String E_MULTISTATUS          = "multistatus";
   String E_OMIT                 = "omit";
   String E_OWNER                = "owner";
   String E_PRINCIPAL            = "principal";
   String E_PROP                 = "prop";
   String E_PROPERTYBEHAVIOR     = "propertybehavior";
   String E_PROPERTYUPDATE       = "propertyupdate";
   String E_PROPFIND             = "propfind";
   String E_PROPNAME             = "propname";
   String E_PROPSTAT             = "propstat";
   String E_REMOVE               = "remove";
   String E_REPORT               = "report";
   String E_RESOURCETYPE         = "resourcetype";
   String E_RESPONSE             = "response";
   String E_RESPONSEDESCRIPTION  = "responsedescription";
   String E_SET                  = "set";
   String E_SHARED               = "shared";
   String E_SOURCE               = "source";
   String E_SRC                  = "src";
   String E_STATUS               = "status";
   String E_SUPPORTEDLOCK        = "supportedlock";
   String E_TIMEOUT              = "timeout";
   String E_WRITE                = "write";
   
   /** Live Properties */
   String P_CREATIONDATE         = "creationdate";
   String P_DISPLAYNAME          = "displayname";
   String P_GETCONTENTLANGUAGE   = "getcontentlanguage";
   String P_GETCONTENTLENGTH     = "getcontentlength";
   String P_GETCONTENTTYPE       = "getcontenttype";
   String P_GETETAG              = "getetag";
   String P_GETLASTMODIFIED      = "getlastmodified";
   String P_LOCKDISCOVERY        = "lockdiscovery";
   String P_RESOURCETYPE         = "resourcetype";
   String P_SOURCE               = "source";
   String P_SUPPORTEDLOCK        = "supportedlock";
   
   String[] WEBDAV_PROPERTIES = new String[] {
      P_CREATIONDATE,
      P_DISPLAYNAME,
      P_GETCONTENTLANGUAGE,
      P_GETCONTENTLENGTH,
      P_GETCONTENTTYPE,
      P_GETETAG,
      P_GETLASTMODIFIED,
      P_LOCKDISCOVERY,
      P_RESOURCETYPE,
      P_SOURCE,
      P_SUPPORTEDLOCK
   };

   List WEBDAV_PROPERTY_LIST = Collections.unmodifiableList(
     Arrays.asList(WEBDAV_PROPERTIES));
       
   //----------------------------
   // Rhythmyx specific constants
   //----------------------------
   
   String GENERIC_MIMETYPE = "application/octet-stream";
   
   /**
    * The namespace prefix for Rhythmyx properties
    */
   String RX_NAMESPACE_PREFIX = "RX";
   
   /**
    * The namespace for Rhythmyx properties
    */
   String RX_NAMESPACE    = "urn:www.percussion.com/webdav";
   
   /**
    * Folder component type constant, used for proxy processor
    */
   String FOLDER_TYPE = "PSFolder";
   
   /**
    * The required field name for all the content type in Rhythmyx
    */
   String SYS_TITLE = "sys_title";
   
   /**
    * The required property names for a defined WebDAV content type
    */   
   String[] REQUIRED_PROPERTIES = new String[] {
      P_GETCONTENTLENGTH,
      P_GETCONTENTTYPE,
   };
   
   List REQUIRED_PROPERTY_LIST = Collections.unmodifiableList(
      Arrays.asList(REQUIRED_PROPERTIES));  
}

