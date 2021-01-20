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
package com.percussion.fastforward.sfp;

import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSUrlUtils;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a publishing content list.
 */
public class PSContentList
{
   /**
    * Ctor that takes the publish context and delivery type. Calls
    * {@link #PSContentList(String, String, int, int, int)}with default values
    * (-1) for the last three parameters.
    * 
    * @param context
    * @param deliveryType
    */
   public PSContentList(String context, String deliveryType)
   {
      m_context = context;
      m_deliveryType = deliveryType;
      m_maxRowsPerPage = -1;
      m_maxPages = -1;
      m_maxDisplayedPageLinks = -1;
   }

   /**
    * Ctor that takes the publish context, delivery type, number of maximum rows
    * per page, number of maximum pages in the content list and number of
    * maximum displayable links.
    * 
    * @param context publish context allows <code>null</code> or empty.
    * @param deliveryType delivery type string, may be <code>null</code> or
    *           empty.
    * @param maxRowsPerPage number of maximum rows per page, -1 for unlimited
    *           rows per page.
    * @param maxPages number of maximum pages in the content list, -1 to specify
    *           no limit.
    * @param maxDisplayedPageLinks number of maximum displayable links, -1 to
    *           specify no limit.
    */
   public PSContentList(String context, String deliveryType,
         int maxRowsPerPage, int maxPages, int maxDisplayedPageLinks)
   {
      m_context = context;
      if (m_context == null)
         m_context = "";
      m_deliveryType = deliveryType;
      if (m_deliveryType == null)
         m_deliveryType = "";
      m_maxRowsPerPage = maxRowsPerPage;
      m_maxPages = maxPages;
      m_maxDisplayedPageLinks = maxDisplayedPageLinks;
   }

   /**
    * Get the content list document with one page full of items. If maximum rows
    * per page specified is -1, the page will have all items.
    * 
    * @param indexOfFirstItem index of the forst item in the page, must be
    *           greater than equal to 0.
    * @param request request context object, must not be <code>null</code>.
    * @return XMl document with items as described above, never
    *         <code>null</code>.
    */
   public Document getPage(int indexOfFirstItem, IPSRequestContext request)
   {
      String sys_publicationid =
         request.getParameter(IPSHtmlParameters.SYS_PUBLICATIONID);
      if (indexOfFirstItem < 0)
      {
         throw new IllegalArgumentException(
               "indexOfFirstItem can not be negative");
      }
      
      Document listDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element contentList = PSXmlDocumentBuilder.createRoot(listDoc,
            IPSDTDPublisherEdition.ELEM_CONTENTLIST);
      contentList.setAttribute(IPSDTDPublisherEdition.ATTR_CONTEXT, m_context);
      contentList.setAttribute(IPSDTDPublisherEdition.ATTR_DELIVERYTYPE,
            m_deliveryType);

      // determine if pagination is necessary
      if (m_maxRowsPerPage > -1)
      {
         // paginate when m_maxRowsPerPage is assigned
         log.debug("outputting paged content list XML with " + m_maxRowsPerPage
               + " rows per page, " + "starting at item " + indexOfFirstItem);
         int indexOfLastItem = indexOfFirstItem + m_maxRowsPerPage - 1;
         for (int i = indexOfFirstItem - 1; i < m_list.size()
               && i < indexOfLastItem; i++)
         {
            PSContentListItem listItem = (PSContentListItem) m_list.get(i);
            Element elem = listItem.toXml(listDoc, request);
            if(sys_publicationid==null || !listItem.isContentUrlNull())
               contentList.appendChild(elem);
         }
         addResultPagerXml(indexOfFirstItem, indexOfLastItem, listDoc, request);
      }
      else
      {
         // do not paginate when m_maxRowsPerPage == -1
         /*
          * NOTE: indexOfFirstItem is IGNORED when m_maxRowsPerPage == -1; the
          * entire content list is output
          */
         log.debug("outputting non-paged content list XML");
         for (Iterator i = m_list.iterator(); i.hasNext();)
         {
            PSContentListItem listItem = (PSContentListItem) i.next();
            Element elem = listItem.toXml(listDoc, request);
            if(sys_publicationid==null || !listItem.isContentUrlNull())
                contentList.appendChild(elem);
         }
      }
      return listDoc;
   }

   /**
    * Add result page XML for navigating to next or previous pages of the
    * content list. This XML will conform to the standard result page XML
    * produced by A Rhythmyx resource when specified with rows per page etc.
    * 
    * @param indexOfFirstItem index of the first item, if greater than 1, a link
    *           will be generated to go to previous page of items.
    * @param indexOfLastItem index of the last item of the content list, if less
    *           that total number of items in the list, link will be generated
    *           to go to next page of items.
    * @param listDoc the content list XML document to which the paging block is
    *           added, must not be <code>null</code>
    * @param request request context object must not be <code>null</code>.
    */
   private void addResultPagerXml(int indexOfFirstItem, int indexOfLastItem,
         Document listDoc, IPSRequestContext request)
   {
      if (listDoc == null)
      {
         throw new IllegalArgumentException("listDoc must not be null");
      }
      if (request == null)
      {
         throw new IllegalArgumentException("request must not be null");
      }
      
      Element contentList = listDoc.getDocumentElement();
      /*
       * Get all the parameters from the current request and add them as URL
       * parameters to the link, making sure to assign the correct value to
       * "psfirst"
       */
      HashMap paramMap = new HashMap(10);
      Iterator paramIter = request.getParametersIterator();
      while (paramIter.hasNext())
      {
         Map.Entry map = (Map.Entry) paramIter.next();
         paramMap.put(map.getKey(), map.getValue());
      }
      if (indexOfFirstItem > 1)
      {
         // generate a previous link
         int psfirst = indexOfFirstItem - m_maxRowsPerPage;
         paramMap.put("psfirst", String.valueOf(psfirst));
         try
         {
            // null/empty parameters to createUrl will use current request
            // values
            URL prevLink = PSUrlUtils.createUrl(null, null, "", paramMap
                  .entrySet().iterator(), null, request);
            PSXmlDocumentBuilder.addElement(listDoc, contentList,
                  RPL_XML_FIELDNAME_PREV, prevLink.toString());
         }
         catch (MalformedURLException e)
         {
            // this exception should never be thrown
            e.printStackTrace();
            log.error(getClass().getName(), e);
         }
      }
      if (indexOfLastItem < size())
      {
         // generate a next link
         int psfirst = indexOfFirstItem + m_maxRowsPerPage;
         paramMap.put("psfirst", String.valueOf(psfirst));

         try
         {
            // null/empty parameters to createUrl will use current request
            // values
            URL nextLink = PSUrlUtils.createUrl(null, null, "", paramMap
                  .entrySet().iterator(), null, request);
            PSXmlDocumentBuilder.addElement(listDoc, contentList,
                  RPL_XML_FIELDNAME_NEXT, nextLink.toString());
         }
         catch (MalformedURLException e)
         {
            // this exception should never be thrown
            e.printStackTrace();
            log.error(getClass().getName(), e);
         }
      }
      // todo: generate index page links
   }

   /**
    * Clear the content list.
    */
   public void clear()
   {
      m_list.clear();
   }

   /**
    * Add item to the content list at the end.
    * 
    * @param item content item, must not be <code>null</code>.
    */
   public void addItem(PSContentListItem item)
   {
      if (item == null)
      {
         throw new IllegalArgumentException("item must not be null");
      }
      m_list.add(item);
   }

   /**
    * @return the size of the content list.
    */
   public int size()
   {
      return m_list.size();
   }

   /**
    * Sorts the content items by the contentId and variantId
    */
   public void sort()
   {
      PSContentListItem[] items = new PSContentListItem[size()];
      m_list.toArray(items);
      Arrays.sort(items);
      m_list.clear();
      for (int i=0; i<items.length; i++)
         m_list.add(items[i]);
   }
   
   /**
    * List of all content items, never <code>null</code>.
    */
   private List m_list = new ArrayList();

   /**
    * Publish context value, initialized in the ctor, never <code>null</code>,
    * may be empty.
    */
   private String m_context;

   /**
    * Delivery type string, initialized in the ctor, never <code>null</code>,
    * may be empty.
    */
   private String m_deliveryType;

   /**
    * Maximum number of rows per page, may be initialized in the ctor and
    * default is -1 (unlimited).
    */
   private int m_maxRowsPerPage = -1;

   /**
    * Maximum number of pages in the content list, may be initialized in the
    * ctor and default is -1 (unlimited).
    */
   private int m_maxPages = -1;

   /**
    * Maximum number of displayable page links in the content list, may be 
    * initialized in the ctor and default is -1 (unlimited).
    */
   private int m_maxDisplayedPageLinks = -1;

   // constants reproduced from obfuscated PSPagedRequestLinkGenerator
   private static final String RPL_XML_FIELDNAME_NONE = "PSXErrorPage";
   private static final String RPL_XML_FIELDNAME_PREV = "PSXPrevPage";
   private static final String RPL_XML_FIELDNAME_NEXT = "PSXNextPage";
   private static final String RPL_XML_FIELDNAME_INDEXED = "PSXIndexPage";

   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   private Logger log = Logger.getLogger(getClass());
}
