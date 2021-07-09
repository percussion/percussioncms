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

package com.percussion.data;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSContentItemStatus;
import com.percussion.error.PSException;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.util.IPSHtmlParameters;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * Extracts the content item status associated with the current execution
 * context.
 */
public class PSContentItemStatusExtractor extends PSDataExtractor
{
   /**
    * Creates a new <code>IPSReplacementValue</code> extractor for the supplied
    * content item status.
    *
    * @param source the content item status to construct the
    *    IPSReplacementValue for, may be <code>null</code>.
    */
   public PSContentItemStatusExtractor(PSContentItemStatus source)
   {
      super(source);
   }

   /**
    * Extract the content item status source from the supplied execution data.
    *
    * @param data the execution data to extract the status from, may be
    *    <code>null</code>.
    * @return the extracted content item status as <code>String</code>, may
    *    be <code>null</code>.
    */
   public Object extract(PSExecutionData data) throws PSDataExtractionException
   {
      return extract(data, null);
   }

   /**
    * Extract the content item status source from the supplied execution data.
    *
    * @param data the execution data to extract the status from, may be
    *    <code>null</code>.
    * @param defaultVal the default value to be returned if the source
    *    could not be extracted, may be <code>null</code>.
    * @return the extracted content item status as <code>String</code> or the
    *    supplied default value if the status cannot be found, may
    *    be <code>null</code>.
    */
   public Object extract(PSExecutionData data, Object defValue)
      throws PSDataExtractionException
   {
      if (data != null)
      {
         IPSReplacementValue source = getSingleSource();
         if (source != null)
         {
            String name = source.getValueText();
            Document doc = getContentStatus(data);
            return extract(name, doc, defValue);
         }
      }

      return defValue;
   }

   /**
    * Extracts the requested content item status from the supplied document.
    *
    * @param name the content item status name, assumed not <code>null</code>
    *    and in the form of "table.column".
    * @param doc the document to extract the status from, may be
    *    <code>null</code>, conforms to the sys_ContentItemStatus.dtd.
    * @param defValue the default to return is the requested content item
    *    status was not found, may be <code>null</code>.
    * @return the content item status information or th edefault value if not
    *    found, may be <code>null</code>.
    */
   private Object extract(String name, Document doc, Object defValue)
   {
      if (doc == null)
         return defValue;

      int pos = name.indexOf(".");
      String table = name.substring(0, pos);
      String column = name.substring(pos+1);

      // first get the element for the requested table
      Element tableElem = null;
      Element root = doc.getDocumentElement();
      if (root == null)
         return defValue;

      NodeList nodes = root.getChildNodes();
      for (int i=0; i<nodes.getLength(); i++)
      {
         Element elem = (Element) nodes.item(i);
         if (elem.getTagName().equalsIgnoreCase(table))
         {
            tableElem = elem;
            break;
         }
      }

      if (tableElem == null)
         return defValue;

      // no get the requested column from the table element found
      NamedNodeMap attrs = tableElem.getAttributes();
      for (int i=0; i<attrs.getLength(); i++)
      {
         Attr attr = (Attr) attrs.item(i);
         if (attr.getName().equalsIgnoreCase(column))
            return attr.getValue();
      }

      return defValue;
   }

   /**
    * Makes an internal request to get the content item status information.
    * Note: As an optimization measure the result document is cached
    * in the request. 
    * 
    * @param data the execution context to operator on, may be <code>null</code>.
    * @return a document conforming to sys_ContentItemStatus.dtd or
    *    <code>null</code> if not found for supplied execution data.
    * @throws PSDataExtractionException if anythig goes wrong doing the
    *    lookup.
    */
   private Document getContentStatus(PSExecutionData data)
      throws PSDataExtractionException
   {
      try
      {
         PSRequest req = data.getRequest();
         String contentid = req.getParameter(IPSHtmlParameters.SYS_CONTENTID);
         if (contentid == null || contentid.trim().length() == 0)
            return null;

         Document doc = (Document)req.getContentItemStatus(contentid);         
         if (doc != null)
            return doc; //has already been cached
                  
         Map params = new HashMap();
         params.put(IPSHtmlParameters.SYS_CONTENTID, contentid);            
         
         PSInternalRequest rh = PSServer.getInternalRequest(
            GET_CONTENTITEMSTATUS_RESOURCE, req, params, false);
         
         if (rh != null)
         {
            doc = rh.getResultDoc();
            
            //cache the result doc in the user session
            req.setContentItemStatus(contentid, doc);
            
            return doc;
         }
            
         return null;
      }
      catch (PSException e)
      {
         throw new PSDataExtractionException(e.getErrorCode(), 
            e.getErrorArguments());
      }
   }
   
  
   /**
    * The object support application used to get the content item status 
    * information.
    */
   private static final String SYS_PSXOBJECTSUPPORT = "sys_psxObjectSupport";
   
   /**
    * The resource used to get the content item status information.
    */
   private static final String GET_CONTENTITEMSTATUS = "getContentItemStatus";
   
   /**
    * The full resource name used to get the content item status information.
    */
   private static final String GET_CONTENTITEMSTATUS_RESOURCE = 
      SYS_PSXOBJECTSUPPORT + "/" + GET_CONTENTITEMSTATUS;
}
