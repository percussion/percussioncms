/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.data;

import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSRequestor;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.util.PSURLEncoder;

import java.util.Iterator;
import java.util.Map;

/**
 * The PSPagedRequestLinkGenerator 
 * allows the current url to be modified with additional paging 
 * link information to create page links.
 * 
 * @author      David Gennaco
 * @version      1.0
 * @since      1.0
 */
public class PSPagedRequestLinkGenerator
{
   /**
    * Construct a paged request link generator. This creates URL links in the
    * XML document being processed based upon the current location, type
    * of page reference, and query starting index.  All existing html 
    * parameters should be maintained.
    *
    *   The type of page references are previous, next, and indexed links.
    *
    * @param      app            the application containing the data set
    *                              we will be linking to
    *
    * @param      ds               the data set information
    *
    *   @param      pageLinkType   the type of page link (next, prev, index)
    *
    * @exception   PSNotFoundException   if the target data set does not exist
    */
   public PSPagedRequestLinkGenerator(   PSApplicationHandler app, 
                                       PSDataSet ds, int pageLinkType)
   {
      PSRequestor requestor = ds.getRequestor();

      m_requestURL = PSServer.makeRequestRoot(app.getRequestRoot());
      if (m_requestURL == null)
         m_requestURL = "";
      m_requestURL += "/" + requestor.getRequestPage();

      m_requestURL = PSURLEncoder.encodePath(m_requestURL);

      // if they're overriding the MIME type, don't set the extension
      m_useExtension = (requestor.getOutputMimeType() == null);

      m_type = pageLinkType;
   }

   /**
    *   Return the Xml field name for this page link type
    *
    * @return      the Xml field name for this type of page link
    */
   public String getXmlFieldName()
   {
      return getXmlFieldName(m_type);
   }

   /**
    *   Return the Xml field name for the specified page link type
    *
    * @param      type      the pagelink type
    *
    * @return      the Xml field name for this type of page link
    */
   public static String getXmlFieldName(int type)
   {
      switch (type)
      {
         case RPL_TYPE_PREV:
            return RPL_XML_FIELDNAME_PREV;
         case RPL_TYPE_NEXT:
            return RPL_XML_FIELDNAME_NEXT;
         case RPL_TYPE_INDEXED:
            return RPL_XML_FIELDNAME_INDEXED;
         default:
            return RPL_XML_FIELDNAME_NONE;
      }
   }

   /**
    *   Return the address of the page link.
    *
    * @param   data      the associated execution data
    *
    * @param   startAt   The row to start this page with
    *
    * @return            The URL string associated with this page link
    */
   public String getURL(PSExecutionData data, int startAt)
   {
      PSRequest request = data.getRequest();
      
      Map<String, Object> params = request.getParameters();

      StringBuilder buf = new StringBuilder(128);
      char nextParamMarker = '?';

      String reqExtension;
      if (m_useExtension)
         reqExtension = request.getRequestPageExtension();
      else
         reqExtension = "";

      /* if the request was submitted through a hook, make that the
       * base of the URL
       */
      String baseURL = request.getHookURL();
      if ((baseURL != null) && (baseURL.length() != 0)) {
         nextParamMarker = '&';
         buf.append(PSURLEncoder.encodePath(baseURL));
         buf.append("?");
         buf.append(PSRequest.REQ_URL_PARAM);
         buf.append("=");
         buf.append(m_requestURL); // already URL encoded
         buf.append(PSURLEncoder.encodeQuery(reqExtension));
      }
      else {
         buf.append(m_requestURL);
         buf.append(PSURLEncoder.encodePath(reqExtension));
      }

      // Add all html parameters here then the psfirst link...
      if (params != null)
      {
         Iterator i = params.keySet().iterator();
         while (i.hasNext()) {
            String key = (String) i.next();
            if (key.equalsIgnoreCase(PSResultSetXmlConverter.FIRST_QUERY_INDEX_PARAMETER_NAME))
               continue;   // ignore this, we'll do it later
            else if (key.equalsIgnoreCase("psrequrl"))
               continue;   // we've already set this (first step)

            buf.append(nextParamMarker);
            nextParamMarker = '&';

            String val = (String) params.get(key);
            buf.append(PSURLEncoder.encodeQuery(key) + "=" + 
               ((val==null) ? "" : PSURLEncoder.encodeQuery(val)));
         }
      }

      // final step, tack on psfirst
      buf.append(nextParamMarker);
      buf.append(PSResultSetXmlConverter.FIRST_QUERY_INDEX_PARAMETER_NAME +
         "=" + startAt);

      return buf.toString();
   }


   int m_type;

   /** the URL encoded request root */
   private   String               m_requestURL;

   private   boolean               m_useExtension;

   /**
    *  Page link type identifiers
    */
   public static final int RPL_TYPE_NONE      =    0;
   public static final int RPL_TYPE_PREV      =    1;
   public static final int RPL_TYPE_NEXT      =   2;
   public static final int RPL_TYPE_INDEXED   =   3;

   /**
    *  Page link type xml names
    */
   public static final String RPL_XML_FIELDNAME_NONE      =    "PSXErrorPage";
   public static final String RPL_XML_FIELDNAME_PREV      =    "PSXPrevPage";
   public static final String RPL_XML_FIELDNAME_NEXT      =   "PSXNextPage";
   public static final String RPL_XML_FIELDNAME_INDEXED   =   "PSXIndexPage";
}
