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

import com.percussion.content.IPSMimeContentTypes;
import com.percussion.debug.PSDebugLogHandler;
import com.percussion.debug.PSTraceMessageFactory;
import com.percussion.design.catalog.PSCatalogException;
import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSDataMapper;
import com.percussion.design.objectstore.PSDataMapping;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSPipe;
import com.percussion.design.objectstore.PSRequestLink;
import com.percussion.design.objectstore.PSRequestor;
import com.percussion.design.objectstore.PSResultPage;
import com.percussion.design.objectstore.PSResultPageSet;
import com.percussion.design.objectstore.PSResultPager;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.server.IPSHttpErrors;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestStatistics;
import com.percussion.server.PSResponse;
import com.percussion.util.PSCollection;
import com.percussion.util.PSIteratorUtils;
import com.percussion.xml.PSDtdAttribute;
import com.percussion.xml.PSDtdDataElement;
import com.percussion.xml.PSDtdElement;
import com.percussion.xml.PSDtdElementEntry;
import com.percussion.xml.PSDtdNode;
import com.percussion.xml.PSDtdNodeList;
import com.percussion.xml.PSDtdTree;
import com.percussion.xml.PSDtdTreeVisitor;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.Format;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

/**
 * The PSResultSetXmlConverter class implements the IPSResultSetConverter
 * interface, and provides conversion to an XML document.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSResultSetXmlConverter implements IPSResultSetConverter {
   /**
    * Construct a result set to XML converter.
    *
    * @param      app   the application containing the data set
    *
    * @param      def   the data set definition
    *
    * @exception  PSNotFoundException  if the application or request is not found
    *
    * @exception  PSCatalogException   if catalog processing error occurs
    */
   public PSResultSetXmlConverter(PSApplicationHandler app, PSDataSet def
      )
      throws PSNotFoundException,
         PSCatalogException,
         PSExtensionException
   {
      super();

      /* Hang on to the requestor for encoding and content information */
      m_requestor = def.getRequestor();

      m_appHandler = app;
      m_ds = def;

      /* we need to use the DTD to determine the doc type, elements, etc. */
      m_DTD = def.getPageDataTank().getSchemaSource();
      if (m_DTD != null) {
         try {
            m_DTD = m_appHandler.getLocalizedURL(m_DTD);
         } catch (java.net.MalformedURLException e) {
            throw new IllegalArgumentException("page tank bad schema URL" +
               m_appHandler.getName() + " " + def.getName() + " " +
               def.getPageDataTank().getSchemaSource() );
         }

         m_DTDTree = new PSDtdTree(m_DTD);
      }

      /* now copy over the column/xml field mapping info */
      m_DataMappings = new HashMap();

      PSPipe pipe = def.getPipe();
      PSDataMapper maps = pipe.getDataMapper();
      for (int j = 0; j < maps.size(); j++)
         addXmlNode((PSDataMapping) maps.get(j));

      m_allowEmptyDoc = maps.allowsEmptyDocReturn();

      /* Default values which indicate no paging is to be performed */
      m_maxRowsPerPage = 0;
      m_maxPages = 0;
      m_maxPageLinks = 0;

      /* Get pagination related information */
      PSResultPager pager = def.getResultPager();

      if (pager != null)
      {
         if (pager.getMaxRowsPerPage() < 1)
         {
            /* can't really page this scenario, just one unlimited page,
               no navigation links - same as default scenario */
         }
         else
         {
            m_maxRowsPerPage  = pager.getMaxRowsPerPage();
            m_maxPages        = pager.getMaxPages();
            m_maxPageLinks    = pager.getMaxPageLinks();

            /* The link generators associated with a paged result set,
               the previous link
               the next link
               a direct page (indexed) link
            */
            m_prevLink  = new PSPagedRequestLinkGenerator(app, def,
                              PSPagedRequestLinkGenerator.RPL_TYPE_PREV);

            m_nextLink  = new PSPagedRequestLinkGenerator(app, def,
                              PSPagedRequestLinkGenerator.RPL_TYPE_NEXT);

            m_indexLink = new PSPagedRequestLinkGenerator(app, def,
                              PSPagedRequestLinkGenerator.RPL_TYPE_INDEXED);
         }
      }

      /* once the column mapping is done, we can now do the
       * style sheet and request link generation. The request link
       * generation must happen after column mapping, so don't move
       * this up!
       */

      m_StyleSheetEvaluators = null;   /* default to no style sheet */

      PSResultPageSet resPageSet = def.getOutputResultPages();
      if (resPageSet != null)
         m_resultPageSet = resPageSet;
      PSCollection      pages;
      if (  ((pages = m_resultPageSet.getResultPages()) != null) &&
            (pages.size() != 0) ) { /* we're not using a style sheet! */
         PSResultPage            page;
         PSCollection            links;
         PSRequestLink           link;
         PSRequestLinkGenerator  genLink;

         // allocate the appropriate arrays
         m_StyleSheetEvaluators = new PSSetStyleSheetEvaluator[pages.size()];

         // Hash map for field/generators
         // We assume that validation happened in PSDataSet so
         // all link generator types will correspond to one xml field
         HashMap linkGenerators = new HashMap();

         for (int i = 0; i < pages.size(); i++) {
            // now build the style sheet object
            page = (PSResultPage)pages.get(i);

            try {
               m_StyleSheetEvaluators[i] = new PSSetStyleSheetEvaluator(
                  m_appHandler.getLocalizedURL(page.getStyleSheet()),
                  page,
                  page.getConditionals());
            } catch (java.net.MalformedURLException e) {
               throw new IllegalArgumentException("style sheet bad URL" +
                  m_appHandler.getName() + " " + def.getName() + " " +
                  page.getStyleSheet() );

            }

            // and the corresponding link generators
            links = page.getRequestLinks();
            if ((links != null) && (links.size() > 0))
            {
               for (int j = 0; j < links.size(); j++) {
                  link = (PSRequestLink)links.get(j);

                  if ((link != null) && !link.isLinkTypeNone())
                  {
                     if (linkGenerators.get(link.getXmlField()) == null)
                     {
                        genLink =  new PSRequestLinkGenerator(app, def, link);

                        linkGenerators.put(link.getXmlField(), genLink);

                        String xmlField = genLink.getXmlFieldName();
                        PSXmlNode node = (PSXmlNode)m_DataMappings.get(xmlField);
                        if (node != null)
                        {
                           // this is an error!!!
                           throw new IllegalArgumentException("XML var link and mapping " +
                                 xmlField);
                        }

                        // now we'll create the XML node and store it as the
                        // link generator object
                        /**
                         * The <code>addXmlNode</code> is used for the generic
                         * functionality. It is not meant to add the new node
                         * to the data mappings, so we must remove it afterwards.
                         */
                        node = addXmlNode(xmlField, genLink, null, null);
                        m_DataMappings.remove(xmlField);
                     }
                  }
               }
            }
         }
      }

      // sort the node list based upon the DTD definition
      sortNodesForDTD();

      // setup the collapse key columns
      initCollapseKeys();

      // and our final step, prepare the extensions
       m_resultDocExtensions = new Vector(3);
      /* not now uses a vector for extensions */
      PSDataHandler.loadExtensions(
         m_appHandler,
         pipe.getResultDataExtensions(),
         IPSResultDocumentProcessor.class.getName(),
         m_resultDocExtensions);
   }

   /* ********** IPSResultSetConverter Interface Implementation ********** */

   /**
    * See {@link IPSResultSetConverter#convert(PSExecutionData,
    * IPSResultSetDataFilter) base class} for full details. More specifically,
    * this class performs the following steps during conversion:
    * <ol>
    * <li>verify reqUrl is supported</li>
    * <li>create the XML document</li>
    * <li>initialize the output with the appropriate header info
    * (content type, etc.)</li>
    * <li>set the XML data on the Response</li>
    * </ol>
    *
    *
    * @param      data   the execution data associated with this request.
    *                    This includes all context data, result sets, etc.
    *
    * @exception  PSConversionException if the conversion fails
    *
    * @exception  PSUnsupportedConversionException
    *             if conversion to the format required by the
    *             specified request URL is not supported
    */
   public void convert(PSExecutionData data, IPSResultSetDataFilter filter)
      throws PSConversionException, PSUnsupportedConversionException
   {
      /* if there's more than one result set on the stack, we're
       * in trouble!!! we must have missed a join.
       */
      java.util.Stack stack = data.getResultSetStack();
      if (stack.size() > 1)
         throw new PSConversionException(
            IPSDataErrors.CANNOT_CONVERT_MULTIPLE_RESULT_SETS, new Integer(stack.size()));
      else if (stack.size() == 0)
         throw new PSConversionException(IPSDataErrors.NO_DATA_FOR_CONVERSION);

      PSRequest request = data.getRequest();

      /* verify reqUrl is supported */
      if ( (request.getRequestPageType() != PSRequest.PAGE_TYPE_XML) &&
           (request.getRequestPageType() != PSRequest.PAGE_TYPE_TEXT) ) {
         String pageExt = request.getRequestPageExtension();
         if (pageExt == null)
            pageExt = "";
         throw new PSUnsupportedConversionException(
            IPSDataErrors.XML_CONV_EXT_NOT_SUPPORTED, pageExt);
      }

      /* create the XML document */
      Document doc = createXmlDocument(data, filter, true);

      /* build the response object */
      PSResponse resp = request.getResponse();
      if (resp == null) {  /* this should never happen! */
         throw new PSConversionException(IPSDataErrors.NO_RESPONSE_OBJECT);
      }

      if (doc == null) {
         resp.setStatus(IPSHttpErrors.HTTP_NOT_FOUND);
      }
      else {
         String contentHeader = request.getContentHeaderOverride();
         if (contentHeader == null)
         {
            String mimeType;
            if (request.getRequestPageType() == PSRequest.PAGE_TYPE_TEXT)
               mimeType = IPSMimeContentTypes.MIME_TYPE_TEXT_PLAIN;
            else
               mimeType = IPSMimeContentTypes.MIME_TYPE_TEXT_XML;

            String encodeString = "";
            String encoding = m_requestor.getCharacterEncoding();

            if ( encoding != null && encoding.length() > 0 )
            {
               encodeString = "; charset=" + encoding;
            }
            contentHeader = mimeType + encodeString;
         }
         resp.setContent(doc, contentHeader);
      }
   }

   /**
    * Get the default MIME type of this converter.
    *
    * @return              the default MIME type
    */
   public String getDefaultMimeType()
   {
      return IPSMimeContentTypes.MIME_TYPE_TEXT_XML;
   }


   /**
    * Evaluate any result page conditionals to determine the index of
    * the result page to use.
    *
    * @param   data        the execution data associated with this request.
    *                      This includes all context data, result sets, etc.
    *
    * @return              the 0-based index of the result page or -1
    */
   public int getResultPageIndex(PSExecutionData data)
   {
      int index = -1;

      for (int i = 0; m_StyleSheetEvaluators!=null && i < m_StyleSheetEvaluators.length; i++) {
         try {
            if (m_StyleSheetEvaluators[i].isMatch(data)) {
               /* Now we need to find the best result page,
                  where the best result page is one which explicitly
                  defines the extensions it accepts  */
               if (!m_StyleSheetEvaluators[i].hasExplicitExtensionList())
               {
                  index = i;
               } else
               {
                  data.setResultPageIndex(i);
                  return i;
               }
            }
         } catch (com.percussion.error.PSEvaluationException e) {
            // we obviously don't have a match!

         }
      }

      if (index != -1) {
         /* Use the result page which accepts all, as no specific
            result page was found */
         data.setResultPageIndex(index);
      }

      return index;
   }

   /**
    * Generate the results for this request.
    *
    * @param   data        the execution data associated with this request.
    *                      This includes all context data, result sets, etc.
    *
    * @exception  PSConversionException if the conversion fails
    *
    * @exception  PSUnsupportedConversionException
    *              if conversion to the format required by the
    *              specified request URL is not supported
    */
   public void generateResults(PSExecutionData data)
      throws PSConversionException, PSUnsupportedConversionException
   {
      /* simply call convert */
      convert(data, null);
   }
   
   /**
    * Get the character encoding value of the dataset requestor.
    * @return the character encoding value, May be <code>null</code> if
    * no requestor exists.
    */
   public String getRequestorCharacterEncoding()
   {
      return m_requestor == null ? null : m_requestor.getCharacterEncoding();
   }


   /* *********************  Protected Implementation ******************** */

   /**
    * Determine whether the request URL is supported by this converter. The request URL may
    * contain an extension. When it does, this is used in defining the
    * output which will be returned.
    *
    * @param   reqUrl      the URL which was specified when making this
    *                                         request
    *
    * @return             <code>true</code> if conversion is supported,
    *                                         <code>false</code> otherwise
    */
   protected boolean isSupported(String reqPageURL)
   {
      /* check the URL to see if it matches the XML conversion rules */
      if (reqPageURL == null)
         return false;

      reqPageURL = reqPageURL.toLowerCase();
      if (reqPageURL.endsWith(".xml") || reqPageURL.endsWith(".txt"))
         return true;

      return false;
   }

   /**
    * Build the XML document from the specified ResultSet.
    *
    * @param data the execution data associated with this request. This includes
    *    all context data, result sets, etc.
    *
    * @param filter See {@link convert(PSExecutionData, IPSResultSetDataFilter)
    *    here} for description.
    *
    * @param   fixupURL    <code>true</code> to convert a file based URL to
    *                      a remote accessible (http) URL
    *
    * @return              the generated document. If an extension processes
    *                      the document, it may return <code>null</code>
    *                      as its output, meaning the caller should act as
    *                      though the request was not processed.
    *
    * @exception  PSConversionException if a conversion error occurs
    */
   protected Document createXmlDocument(PSExecutionData data,
         IPSResultSetDataFilter filter, boolean fixupURL)
      throws PSConversionException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      PSRequest request = data.getRequest();

      PSXmlNodeData[] dataStruct = createXmlDataNodes();

      // setup tracing
      PSDebugLogHandler dh = (PSDebugLogHandler)data.getLogHandler();
      int traceResourceHandlerFlag = PSTraceMessageFactory.RESOURCE_HANDLER_FLAG;
      int traceResultSetFlag = PSTraceMessageFactory.RESULT_SET;
      ArrayList traceRows = new ArrayList();

      /* use the mapper(s) to convert from the result set data to the
       * XML tree. Be sure to order items correctly, grouping related
       * objects, etc.
       */
      ResultSet rs = null;
      try {
         rs = (ResultSet)data.getResultSetStack().pop();

         ResultSetMetaData meta = rs.getMetaData();
         String[] cols = new String[meta.getColumnCount()];

         for (int i = 0; i < cols.length; i++) {
            /* JDBC apparently returns "" for all but getColumnName.
             * This is a problem as we can't distinguish between a column
             * of the same name in two tables. Don't see anything we can
             * do about it at this time.
             */

            /* WE ARE LOWERCASING BACK-END COLUMN NAMES TO GUARANTEE
             * A MATCH
             *
             * This may be a problem for some systems, which are case
             * sensitive, but the chances of someone using the same
             * column name, with only case as the differentiator,
             * should hopefully be VERY slim.
             *
             * we are also creating column names as colX for columns
             * with no name. This usually occurs when a formula is executed
             * on a column without setting an alias on it.
             */
            String col = meta.getColumnName(i+1);
            if ((col == null) || (col.length() == 0))
               cols[i] = "col" + String.valueOf(i+1);
            else
               cols[i] = col.toLowerCase();
         }

         /* Paging variables */
         int pagerFirstRow = 0;
         int curRowNumber  = 0;

         if (m_maxRowsPerPage > 0)
         {
            pagerFirstRow = data.getFirstRowNumber();
            for (int i = 1; i < pagerFirstRow; i++)
            {
               if (rs.next())
                  curRowNumber++;
               else
                  break;
            }
         }
         PSFilterResultSetWrapper frsw =
            new PSFilterResultSetWrapper(data, rs, filter);

         PSRowDataBuffer dataBuf = new PSRowDataBuffer(frsw);
         Object[] curRow = dataBuf.getCurrentRow();

         // let the execution data know what result set/row we're on
         data.setCurrentResultSetMetaData(meta);
         data.setCurrentResultRowData(curRow);

         // trace page processing
         if ((m_maxRowsPerPage > 0) &&
            dh.isTraceEnabled(traceResourceHandlerFlag))
         {
            int curpage = ((curRowNumber) / m_maxRowsPerPage) + 1;
            Object[] args = {new Integer(m_maxRowsPerPage) , new Integer(curpage),
                              "traceResourceHandler_pageResults"};
            dh.printTrace(traceResourceHandlerFlag, args);

            // replace message format string, the first 2 args will be ignored
            args[2]="traceResourceHandler_mapResults";
            dh.printTrace(traceResourceHandlerFlag, args);
         }

         int rowsSelected = 0;   // this is our row selected counter

         boolean gotOne = false;

         PSXmlNode node;
         while (dataBuf.readRow()) {
            gotOne = true;
            processNewRow(data, doc, dataStruct);

            /* increment the rows selected counter */
            rowsSelected++;
            curRowNumber++;

            // trace rows processed
            if ((dh.isTraceEnabled(traceResultSetFlag)) && (curRow != null))
            {
               //save the row for trace output
               Object[] traceRow = new Object[curRow.length];
               for(int i=0; i< curRow.length; i++)
                  traceRow[i] = curRow[i];
               traceRows.add(traceRow);
            }

            if (dh.isTraceEnabled(traceResourceHandlerFlag))
            {
               if (((rowsSelected / 10) > 0) && (rowsSelected % 10 == 0))
               {
                  Object[] args = {new Integer(rowsSelected),
                     "traceResourceHandler_mapResultsCount"};
                  dh.printTrace(traceResourceHandlerFlag, args);
               }
            }


            if (m_maxRowsPerPage > 0)
            {
               /* Break if a page read in, or we have reached the maximum
                  defined row limit (the max page #  * the max row #) */
               if (rowsSelected >= m_maxRowsPerPage)
                  break;
               if ((m_maxPages > 0) &&
                  (curRowNumber >= m_maxRowsPerPage * m_maxPages))
                  break;
            }
         }

         // trace total rows processed and resultset
         if (dh.isTraceEnabled(traceResultSetFlag))
         {
            Object[] resultArgs = {cols, traceRows};
            dh.printTrace(traceResultSetFlag, resultArgs);
         }
         if (dh.isTraceEnabled(traceResourceHandlerFlag))
         {
            Object[] args = {new Integer(rowsSelected),
               "traceResourceHandler_mapResultsTotal"};
            dh.printTrace(traceResourceHandlerFlag, args);
         }

         /* If we didn't get any rows (null result set) and we are
            set to not allow empty documents to be returned, mimic
            one row containing all null values for processing purposes
         */
         if (!gotOne && !m_allowEmptyDoc)
         {
            data.setForceRowOnNullResultSet(true);
            processNewRow(data, doc, dataStruct);
         }

         /*
         * If we are paging, count out the rest of the rows we may need to
         * know how many forward page links to add, and add the page links
         * (forwards and backwards, as needed.)
         */

         if (m_maxRowsPerPage > 0)
         {
            // calculate current page
            int curPage = ((pagerFirstRow + m_maxRowsPerPage) - 1)
                        / m_maxRowsPerPage;

            // If there's a maximum number of page links, stop when we've seen
            // that we have enough rows ahead of us
            int maxPagesAhead = Integer.MAX_VALUE;
            int maxRowsAhead = Integer.MAX_VALUE;

            if (m_maxPageLinks > 0 || m_maxPageLinks < 0)
            {
               //max number of links could be set to -1, which is a not likely
               //setting, but still a valid one that we need to handle
               int maxPageLinks = Integer.MAX_VALUE;
               if (m_maxPageLinks > 0)
                  maxPageLinks = m_maxPageLinks;

               if (curPage < maxPageLinks / 2)
               {
                  // more links ahead than behind
                  maxPagesAhead = maxPageLinks - curPage;
               }
               else
               {
                  // Make current page the half-way point in the link set
                  // (or higher, if we're near the end.)
                  maxPagesAhead = (maxPageLinks / 2);
               }

               //make sure that we don't exceed a hard MaxPages limit if any.
               if (m_maxPages == 1)
                  maxPagesAhead = 0;
               else if (m_maxPages > 1)
                  maxPagesAhead = m_maxPages - curPage;

               maxRowsAhead = (maxPagesAhead * m_maxRowsPerPage) - 1;

               if (m_maxRowsPerPage == 1)
                  maxRowsAhead += 1;

               if (m_maxPages <= 0 && m_maxPageLinks < 0 && maxRowsAhead < 0)
                  maxRowsAhead = Integer.MAX_VALUE;

               while (--maxRowsAhead >= 0 && dataBuf.skipRow())
               {
                  curRowNumber++;
               }
            }

            int numPages = curRowNumber / m_maxRowsPerPage;
            /* Partial page? */
            if (curRowNumber % m_maxRowsPerPage > 0)
               numPages++;

            /* Now set and add the page links, if needed (if we have pages) */
            Element root = doc.getDocumentElement();
            if (numPages > 1)
            {
               /* If we are after the starting point, put a previous page */
               if (pagerFirstRow > 1)
               {
                  PSXmlDocumentBuilder.addElement(doc, root, m_prevLink.getXmlFieldName(),
                     m_prevLink.getURL(data, (pagerFirstRow - m_maxRowsPerPage)));
               } else
                  pagerFirstRow = 1;


               /* If the next page start will not exceed our result set or
                  limits, add its link */
               if ((pagerFirstRow + m_maxRowsPerPage) <= curRowNumber)
               {
                  PSXmlDocumentBuilder.addElement(doc, root, m_nextLink.getXmlFieldName(),
                     m_nextLink.getURL(data, (pagerFirstRow + m_maxRowsPerPage)));
               }


               // We limit the displayed page links by m_maxPageLinks and
               // adjust the links shown relative to the current page
               // we are on.
               int start = 0;
               int end = 0;
               int halfwayMark = m_maxPageLinks/2;

               m_maxPageLinks = m_maxPageLinks < 1 ? numPages : m_maxPageLinks;

               // Adjust the starting point if we are within the halfwayMark
               // value to the total number of pages.
               if(numPages - ((curPage-halfwayMark)-1) < m_maxPageLinks)
               {
                  start = Math.max((numPages-m_maxPageLinks),0);

               // Adjust the starting point if we are past the
               // halfway point
               }else if( curPage>halfwayMark)
               {
                  start = Math.max((curPage-halfwayMark)-1,0);
               }

               // Calculate the ending offset
               end = (start+m_maxPageLinks) < numPages ?
                        (start+m_maxPageLinks)-1 : numPages-1;

               /* Add the indexed links here */
               Element currentElement = null;
               for (int i = start; i <= end; i++)
               {
                  currentElement = PSXmlDocumentBuilder.addElement(
                     doc, root, m_indexLink.getXmlFieldName(),
                     m_indexLink.getURL(data, (1 + (i * m_maxRowsPerPage))));
                  currentElement.setAttribute("pagenum", Integer.toString(i+1));

               }
            }
         }

         /* update the statistics with the number of rows selected */
         PSRequestStatistics stats = null;
         if (request != null) {
            stats = request.getStatistics();
            if (stats != null)
               stats.incrementRowsSelected(rowsSelected);
         }
      } catch (Throwable t) {
         Object[] args = { request.getUserSessionId(), t.toString() };
         throw new PSConversionException(IPSDataErrors.XML_CONV_EXCEPTION, args);
      } finally {
         if (rs != null) {
            try { rs.close(); }
            catch (SQLException e) { /* don't need this, ignore the error */ }
         }
      }

      // trace initial doc for post exit xml
      if (dh.isTraceEnabled(PSTraceMessageFactory.POST_EXIT_XML_FLAG))
      {
         Object[] traceArgs ={doc};
         dh.printTrace(
            PSTraceMessageFactory.POST_EXIT_XML_FLAG, traceArgs);
      }

      // Release the Execution Data now.  (This assumes that no more execution
      // steps will be made after this point.  This ought to be true.
      // The PSExecutionData could verify this, but doesn't currently.)
      data.release();

      // now we can run the result doc extensions
      try
        {
       if( m_resultDocExtensions != null )
       {
           int size=m_resultDocExtensions.size();
          if( size > 0 )
          {
              for (int  i =0; i<size;i++)
              {
                PSExtensionRunner proc = (PSExtensionRunner)m_resultDocExtensions.elementAt(i);
                doc = proc.processResultDoc(data, doc);

            // we treat this as a special condition meaning file not found
            // as noted in the comments for this method
            if (doc == null)
               break;
           }
          }
        }
      } catch (com.percussion.error.PSException e) {
         throw new PSConversionException(e.getErrorCode(), e.getErrorArguments());
      }

      if (dh.isTraceEnabled(PSTraceMessageFactory.POST_EXIT_CGI_FLAG))
         m_appHandler.getLogHandler().printTrace(
            PSTraceMessageFactory.POST_EXIT_CGI_FLAG,
            request);


      if (doc != null)
      {
         /* Check the doc for a ProcessingInstruction named
          *    "xml-stylesheet" apply the app-defined stylesheet
          *    if one is not found (extensions override app settings)
          */
         if (!styleSheetInDoc(doc))
         {
            /*
            *   run the conditionals to set the style sheet and
            *   xml fields can now be used to determine the style sheet.
            */
            /* Need to add document to data, if we are allowing xml */
            data.setInputDocumentWalker(new PSXmlTreeWalker(doc));

            applyStylesheetConditions(data, doc, fixupURL);

            /* keep walker around for future xml field needs */
//          data.setInputDocumentWalker(null);
         }
      }

      return doc;
   }

   private boolean isNodeStyleSheetProcessingInstruction(Node node)
   {
      if (node instanceof ProcessingInstruction)
      {
         if (((ProcessingInstruction) node).getTarget().equals("xml-stylesheet"))
         {
            return true;
         }
      }

      return false;
   }

   /**
    * Check the supplied node for a stylesheet processing instruction, and if
    * not found, recursively checks all child nodes.
    *
    * @param node The node to check, may be <code>null</code>.
    *
    * @return <code>true</code> if a stylesheet processing instruction is found,
    * <code>false</code> if not or if the supplied node is <code>null</code>.
    */
   private boolean processNodes(Node node)
   {
      if (node == null)
         return false;

      if (isNodeStyleSheetProcessingInstruction(node))
         return true;
      else
      {
         if (node.hasChildNodes())
         {
            NodeList kids = node.getChildNodes();
            Node kid;
            int i = 0;
            while ((kid = kids.item(i++)) != null)
            {
               boolean kidHasStyleSheet;
               kidHasStyleSheet = processNodes(kid);
               if (kidHasStyleSheet)
                  return true;
            }
         }
      }
      return false;
   }

   /* Determine whether the document contains a stylesheet processing instruction. */
   private boolean styleSheetInDoc(Document doc)
   {
      return processNodes(doc);
   }

   /**
    * Apply the style sheet conditions to the XML document. The
    * stylesheet processing instruction will be set in the XML document
    * and the link generators associated with the stylesheet will be
    * returned.
    */
   private void applyStylesheetConditions(PSExecutionData data, Document doc, boolean fixupURL)
   {
      PSRequest request = data.getRequest();

      String reqExt = request.getRequestPageExtension().toLowerCase();

      /* Make IE to show XML as is without any XSL applyed for TXT extension
       * if there is no 'txt' mime type specified for the requested resource
       */
      if (m_requestor.getMimeType("txt") == null && reqExt.equals(".txt"))
         return;

      /* run the conditionals to determine which style sheet is in use.
       * If no conditions are met, use our default style sheet.
       */
      java.net.URL   styleSheetURL  = null;
      if (m_StyleSheetEvaluators != null) {
         int index = data.getResultPageIndex();
         if ((index == -1) || (index >= m_StyleSheetEvaluators.length))
            index = getResultPageIndex(data);

         if ((index != -1) && (index < m_StyleSheetEvaluators.length))
         {
            // this is a flag that the cacher already did this check for us
            styleSheetURL = m_StyleSheetEvaluators[index].getStyleSheet();
         }
      }

      if (styleSheetURL != null) {
         String urlText, ssType;

         /* make this "remote ready" by converting FILE URL to HTTP URL */
         if (fixupURL)
            urlText = m_appHandler.getExternalURLString(styleSheetURL);
         else
            urlText = styleSheetURL.toExternalForm();

         int extPos = urlText.lastIndexOf('.');
         if (extPos == -1)
            ssType = "xsl";   // assume it's XSL by default
         else
            ssType = urlText.substring(extPos+1).toLowerCase();

         ProcessingInstruction pi = doc.createProcessingInstruction(
            "xml-stylesheet", ("type=\"text/" + ssType + "\" href=\"" + urlText + "\""));
         doc.insertBefore(pi, doc.getDocumentElement());
      }

      return;
   }

   private PSXmlNode addXmlNode(PSDataMapping map)
   {
      String xmlField = map.getXmlField();
      IPSBackEndMapping beMap = map.getBackEndMapping();
      IPSDataExtractor dataExtract;

      dataExtract = PSDataExtractorFactory.createReplacementValueExtractor(
         (IPSReplacementValue)beMap);

      return addXmlNode(xmlField, dataExtract, map.getConditionals(),
         map.getTextFormatter());
   }

   private PSXmlNode addXmlNode(String xmlField, IPSDataExtractor dataExtract,
      PSCollection conditionals, Format textFormatter)
   {
      ArrayList names = new ArrayList();
      int pos;
      int lastPos = 0;
      for (; (pos = xmlField.indexOf('/', lastPos)) != -1; )
      {
         /* If the first char is a slash char, skip it from the name.
          * For instance, /Manufacturer and Manufacturer should both
          * resolve to Manufacturer
          */
         if (pos != 0)
            names.add(xmlField.substring(lastPos, pos));

         lastPos = pos + 1;
      }

      String elementName = xmlField.substring(lastPos);
      names.add(elementName);
      int nodeCount = names.size();

      PSXmlNode xmlNode = null;
      PSXmlNode parentNode = null;
      String parentField;
      for (pos = 0; pos < nodeCount; pos++)
      {
         if (pos == 0)
         {
            parentField = null;
            xmlField = (String) names.get(pos);
         }
         else
         {
            parentField = xmlField;
            xmlField += "/" + (String) names.get(pos);
         }

         // is this a data node?
         boolean isDataNode = (pos == (nodeCount - 1));

         xmlNode = (PSXmlNode) m_DataMappings.get(xmlField);
         if (xmlNode != null)
         {
            boolean checkChildren = false;
            if (xmlNode.isForData())
            {  // treating prior data node as parent
               // if the data node we're writing is not an attribute, that's
               // definitely a problem
               if (!isDataNode)
               {
                  if (elementName.charAt(0) != '@')
                     throw new IllegalArgumentException("XML parent mapping not supported " +
                           xmlField);

                  checkChildren = true;   // need to check kids now
               }
            }
            else if (isDataNode)    // treating a prior parent as a data node
               checkChildren = true;

            if (checkChildren)
            {
               // search all children to verify they're all attributes
               ArrayList kids = xmlNode.getChildren();
               int kidCount = kids.size();
               for (int i = 0; i < kidCount; i++)
               {
                  PSXmlNode xNode = (PSXmlNode) kids.get(i);
                  if (xNode.isForData() && !xNode.isAttribute())
                  {
                     throw new IllegalArgumentException("XML parent mapping not supported " +
                           xmlField);
                  }
               }
            }

            // this was previously a parent, convert it to a data node now
            if (isDataNode)
            {
               if (conditionals != null && conditionals.size() > 0)
               {
                  xmlNode.addConditionalNode(new PSXmlNode(
                     (String) names.get(pos), xmlNode.m_nodeId, xmlNode.getParent(),
                     dataExtract, conditionals, textFormatter, false));
               }
               else
                  xmlNode.setDataSource(dataExtract);
            }

            parentNode = xmlNode;

            continue;   // otherwise, we're ok so move on to the next one
         }

         // create the new node
         xmlNode = new PSXmlNode(
            (String) names.get(pos), m_nodeCount++, parentNode,
            (isDataNode ? dataExtract : null),
            (isDataNode ? conditionals : null),
            (isDataNode ? textFormatter : null));

         // and add it to the hash map
         m_DataMappings.put(xmlField, xmlNode);

         if (pos == 0)
         {  // this is the root, set it as such
            if (m_xmlRoot != null)
            {
               // two roots are not allowed!!!
               throw new IllegalArgumentException("XML two root elements " +
                     m_xmlRoot.getName() + " " + (String)names.get(0));
            }
            m_xmlRoot = xmlNode;
         }

         parentNode = xmlNode;   // it's a parent if there are more nodes
      }

      return xmlNode;
   }

   private synchronized PSXmlNodeData[] createXmlDataNodes()
   {
      if (m_nodeCount == 0)
         return null;

      PSXmlNodeData[] dataStruct = new PSXmlNodeData[m_nodeCount];
      for (int i = 0; i < m_nodeCount; i++) {
         dataStruct[i] = new PSXmlNodeData();
      }

      return dataStruct;
   }

   private void processNewRow(PSExecutionData data, Document doc, PSXmlNodeData[] dataStruct)
      throws com.percussion.data.PSDataExtractionException
   {
      if (dataStruct == null)
         return;

      /* if we're collapsing on the specified columns, see if we have new
       * data values, in which case we must clear all entries in the tree
       * under the changed element (even at the same level, in some cases).
       * If not, we must simply clear the non-collapsed nodes.
       */
      if ((m_clearOnRowset != null) || ((m_collapseKeys != null) && (m_collapseKeys.length != 0)))
      {
         if (m_clearOnRowset != null)
         {
            for (int i = 0; i < m_clearOnRowset.size(); i++)
            {
               PSXmlNode node = (PSXmlNode) m_clearOnRowset.get(i);
               node.clearChildElementData(dataStruct);
            }
         }

         if ((m_collapseKeys != null) && (m_collapseKeys.length != 0))
         {
            for (int i = 0; i < m_collapseKeys.length; i++)
            {
               PSXmlCollapseNode collapseNode = m_collapseKeys[i];
               if (collapseNode.needsClear(dataStruct, data))
               {
                  collapseNode.clear(dataStruct);
               }
            }
         }
      }
      else {   // reset all the data nodes in this case
         m_xmlRoot.clearElementDataTopDown(dataStruct);
      }

      // now recursively process the tree adding the data
      storeRowData(m_xmlRoot, data, doc, dataStruct);
   }

   private void storeRowData(PSXmlNode node, PSExecutionData data, Document doc,
      PSXmlNodeData[] dataStruct) throws com.percussion.data.PSDataExtractionException
   {
      if ((node == null) || (dataStruct == null))
         return;

      ArrayList kids = node.getChildren();
      int size = kids.size();
      for (int i = 0; i < size; i++)
      {
         PSXmlNode xNode = (PSXmlNode)kids.get(i);
         if (xNode.isCollapseKey() && xNode.isSet(dataStruct))
         {  // this means the key did not change
            // we don't need to waste time trying to set it
            continue;
         }
         if (xNode.isForData())
            xNode.setValue(data, doc, dataStruct);

         // also store the data for its kids
         storeRowData(xNode, data, doc, dataStruct);
      }
   }

   private void initCollapseKeys()
   {
      /* now let's see if we're collapsing records based upon the DTD
       * definition. to make collapsing as painless as possible, we're
       * following a strategy of collapse groups. The problem we face is
       * that different branches of the XML tree may be their own collapse
       * group. We want to allow groupings at the various levels in the tree.
       * to accomplish this, we're following a numbering scheme which
       * dictates what we'll reset when the collapse keys change. The lower
       * the number, the more nodes it effects. We will take the indent
       * level into consideration (eg, root = 0, first level child = 1, etc.)
       * and the parent id of the branch under which we're clearing. This
       * is important as we may run into cases where there are two distinct
       * branches at the same level. If we're only effecting one of them,
       * we don't want to clear the other.
       */
      ArrayList collapseKeys = new ArrayList();
      addCollapseKeys(m_xmlRoot, collapseKeys);
      if (collapseKeys.size() > 0 ){
         m_collapseKeys = new PSXmlCollapseNode[collapseKeys.size()];
         for (int i = 0; i < collapseKeys.size(); i++){
            m_collapseKeys[i] = (PSXmlCollapseNode) collapseKeys.get(i);
         }
      }
   }

   private void addCollapseKeys(PSXmlNode node, ArrayList collapseKeys)
   {
      // check the DTD def to see what we're collapsing on.
   if( node != null )
   {
       if( node.isForData())
       {
         int occurs = node.getMaxOccurrences();
           if ((occurs == PSDtdNode.OCCURS_OPTIONAL) || (occurs == PSDtdNode.OCCURS_ONCE))
        {
               evaluateNodeForCollapse(node, collapseKeys);
            }
       }
     ArrayList kids = node.getChildren();
       int size = kids.size();
      for (int i = 0; i < size; i++)
         addCollapseKeys((PSXmlNode)kids.get(i), collapseKeys);
   }
  }
   void evaluateNodeForCollapse(PSXmlNode node, ArrayList collapseKeys)
   {
     if( node != null )
    {
      boolean isCollapseKey = false;
      PSXmlCollapseNode collapseNode = null;
        PSXmlNode curAncestor = node.getParent();
        while (curAncestor != null)
        {
           int pOccurrence = curAncestor.getMaxOccurrences();
           if ((pOccurrence == PSDtdNode.OCCURS_ONCE) || (pOccurrence == PSDtdNode.OCCURS_OPTIONAL))
           {
            curAncestor = curAncestor.getParent();
         }
        else
         {
            if (curAncestor.requestCollapsePrivilege(node))
              {
                  isCollapseKey = true;
               collapseNode = new PSXmlCollapseNode();
               node.setCollapseKey(true);
               collapseNode.setCollapseNode(node);
               collapseNode.setClearNode(curAncestor);
              }
               break;
            }
      }
      if (collapseNode != null)
        {
         collapseKeys.add(collapseNode);
        // if we're logging the execution plan, store the DTD info
          m_appHandler.getLogHandler().write(new com.percussion.log.PSLogExecutionPlan(
                     m_appHandler.getId(), IPSDataErrors.EXEC_PLAN_LOG_COLLAPSED_XML_FIELD,
                  new Object[] { node.getFullName() }));
       }
    }
   }

   private void sortNodesForDTD()
   {
      if (m_DTDTree != null) {
         // if we have the DTD, let's try to sort the elements the way
         // they're defined in the DTD
         new PSXmlNodeSorter(m_xmlRoot, m_DTDTree);
      }
   }

   /**
    * Get the encoding for the specified request page.
    *
    * @arg  index the offset of the request page in the page set
    *
    * @return the character encoding desired or <code>null</code>
    *          if no encoding has been set for this resource, or
    *          if no request page resource for the data exists.
    */
   public String getEncodingForRequestPage(int index)
   {
      PSResultPage page = getResultPage(index);
      if (page == null)
         return null;     

      return page.getCharacterEncoding();
   }

   /**
    * Get the MIME type for the specified request page.
    *
    * @arg  index the offset of the request page in the page set
    *
    * @return the mime type desired or <code>null</code>
    *          if no encoding has been set for this resource, or
    *          if no request page resource for the data exists.
    */
   public String getMimeTypeForRequestPage(int index, PSExecutionData data)
   {
      PSResultPage page = getResultPage(index);
      if (page == null)
         return null;

      IPSReplacementValue val = page.getMimeType();
      if (val == null)
         return null;

      try
      {
         IPSDataExtractor extractor =
            PSDataExtractorFactory.createReplacementValueExtractor(val);

         if (extractor != null)
         {
            Object mimeType = extractor.extract(data);
            if (mimeType != null)
               return mimeType.toString();
         }
      } catch (IllegalArgumentException e)
      {
         // This should never happen!
      }  catch (PSDataExtractionException extrE)
      {
         // uhoh!
      }

      return null;
   }
   
   /**
    * Returns the indexed result page from the result page set.
    * 
    * @param index the index of the requested result page
    * @return the result page or <code>null</code> if it does
    * not exist.
    */
   public PSResultPage getResultPage(int index)
   {
      if (index < 0)
         return null;

      if (m_resultPageSet == null)
         return null;

      PSCollection pages = m_resultPageSet.getResultPages();
      if (pages == null)
         return null;

      if (index > pages.size())
         return null;
      
      return (PSResultPage)pages.get(index);
   }
   
   /**
    * Returns flag indicating if the XHTML compliant namespace cleanup
    * is allowed to run for the result page specified.
    * 
    * @param index the index of the requested result page
    * @return <code>true</code> if the namespace cleanup is allowed to 
    * run for this result page.
    */
   public boolean isNamespaceCleanupAllowedForResultPage(int index)
   {
      PSResultPage page = getResultPage(index);
      if (page == null)
         return false; 
      return page.allowNamespaceCleanup();
   }

   public static String getMimeTypeForRequestor(PSRequestor requestor,
         String extension, PSExecutionData data)
   {
      try
      {
         IPSDataExtractor extractor =
            PSDataExtractorFactory.createReplacementValueExtractor(requestor.getMimeType(extension));

         Object obj = null;

         if (extractor != null)
            obj = extractor.extract(data);
         if (obj != null)
            return obj.toString();
      } catch (IllegalArgumentException e)
      {
         // This should never happen!
      }  catch (PSDataExtractionException extrE)
      {
         // uhoh!
      }

      return null;
   }

   class PSXmlNodeSorter implements PSDtdTreeVisitor
   {
      public PSXmlNodeSorter(PSXmlNode xmlRoot, PSDtdTree dtdTree)
      {
         super();
         m_dtdTree = dtdTree;
         m_xmlRoot = xmlRoot;

         if ((m_xmlRoot != null) && (m_dtdTree != null))
         {
            PSDtdElementEntry root = m_dtdTree.getRoot();
            if (root != null)
               root.acceptVisitor(this, null);
         }
      }


      /////////////////////// PSDtdTreeVisitor implementation ////////////////////

      public Object visit(PSDtdNode node, Object data)
      {
         node.childrenAccept(this, data);
         return null;
      }

      public Object visit(PSDtdElementEntry node, Object data)
      {
         PSDtdElement el = node.getElement();

         String name = el.getName();
         PSXmlNode curNode = null;

         // the data is stored as an object[] containing the parent
         // PSXmlNode in [0] and the child index in [1]
         PSXmlNode parentNode = (data == null) ? null : ((PSXmlNode)((Object[])data)[0]);

         // only need to do sorting on non-root nodes
         if (parentNode == null)
         {  // not much to do here, we're sorted just fine
            curNode = m_xmlRoot; // mark it as found
         }
         else
         {
            int nodeNo = ((Integer)((Object[])data)[1]).intValue();
            ArrayList kids = parentNode.getChildren();
            int size = kids.size();
            for (int i = nodeNo; i < size; i++)
            {
               PSXmlNode xNode = (PSXmlNode)kids.get(i);
               if (xNode.getName().equals(name))
               {
                  if (i != nodeNo)
                     nodeSwap(kids, i /* fromIndex */, nodeNo /* toIndex */);
                  ((Object[])data)[1] = new Integer(nodeNo+1);
                  curNode = xNode;  // mark it as found
                  break;   // found it, stop searching
               }
            }
         }

         // if we didn't find the node, we can stop searching. we store
         // all parents for a given node. if we haven't found this element,
         // we don't have any of its kids either
         if (curNode == null)
            return null;

         int onChild = 0;
         ArrayList kids = curNode.getChildren();
         int size = kids.size();

         // process the attributes
         int attrCount = el.getNumAttributes();
         for (int i = 0; i < attrCount; i++)
         {
            PSDtdAttribute attr = el.getAttribute(i);
            String attrName = attr.getName();
            for (int j = onChild; j < size; j++)
            {
               PSXmlNode xNode = (PSXmlNode)kids.get(j);
               if (xNode.getName().equals(attrName))
               {
                  if (j != onChild)
                     nodeSwap(kids, j /* fromIndex */, onChild /* toIndex */);
                  onChild++;
                  break;
               }
            }
         }

         // and the children
         Object[] childData = { curNode, new Integer(onChild) };
         node.childrenAccept(this, childData);

         return null;
      }

      public Object visit(PSDtdNodeList node, Object data)
      {
         node.childrenAccept(this, data);
         return null;
      }

      public Object visit(PSDtdDataElement node, Object data)
      {
         node.childrenAccept(this, data);
         return null;
      }

      private void nodeSwap(ArrayList list, int fromIndex, int toIndex)
      {
         PSXmlNode fromNode = (PSXmlNode)list.get(fromIndex);
         PSXmlNode toNode   = (PSXmlNode)list.get(toIndex);

         list.set(fromIndex, toNode);
         list.set(toIndex,   fromNode);
      }

      PSXmlNode m_xmlRoot;
      PSDtdTree m_dtdTree;
   }

   class PSXmlNode
   {
      /**
       * See {@link #PSXmlNode(String, int, PSXmlNode, IPSDataExtractor, PSCollection, Format, boolean)}
       * this (nodeName, nodeId, parent, dataSource, conditionals, textFormatter, true)
       * for a description.
       */
      PSXmlNode(String nodeName, int nodeId, PSXmlNode parent,
         IPSDataExtractor dataSource, PSCollection conditionals,
         Format textFormatter)
      {
         this(nodeName, nodeId, parent, dataSource, conditionals,
            textFormatter, true);
      }

      /**
       * Constructs a new xml node as used while processing each row.
       *
       * @param nodeName the simple node name, assumed not <code>null</code>.
       * @param nodeId the node identifier, a unique number accross all nodes.
       * @param parent the parent node, is <code>null</code> if this is the
       *    parent.
       * @param dataSource the data source, may be <code>null</code>.
       * @param conditionals the conditionals for this node, may be
       *    <code>null</code>.
       * @param textFormatter a text formatter to be used to format the output,
       *    may be <code>null</code>.
       * @param addAsChild <code>true</code> specifies to add this node as
       *    a child to the parent, <code>false</code> otherwise.
       */
      PSXmlNode(String nodeName, int nodeId, PSXmlNode parent,
         IPSDataExtractor dataSource, PSCollection conditionals,
         Format textFormatter, boolean addAsChild)
      {
         if (nodeName.startsWith("@"))
         {
            m_flags = FLAG_IS_ATTRIBUTE;
            m_nodeName = nodeName.substring(1);
         }
         else
         {
            m_flags = 0;
            m_nodeName = nodeName;
         }

         m_nodeId       = nodeId;
         m_parent       = parent;
         m_children     = new ArrayList();
         m_dataSource   = dataSource;
         m_textFormatter = textFormatter;

         // if this column is conditionally mapped, create a conditional
         // evaluator for this
         if ((conditionals != null) && (conditionals.size() != 0))
         {
            try
            {
               m_conditionChecker =
                  new PSConditionalEvaluator(conditionals);
            }
            catch (IllegalArgumentException e)
            {
               throw new IllegalArgumentException(e.getLocalizedMessage());
            }
         }
         else
            m_conditionChecker = null;

         // let's see what the occurrence counter is for this object
         if ((m_DTDTree != null) && (m_parent != null))
         {
            String immediateParent = m_parent.m_nodeName;
            if ((m_flags & FLAG_IS_ATTRIBUTE) == FLAG_IS_ATTRIBUTE)
            {  /* attributes are special cases. We need to get the attribute
                * node from its parent. We'll assume it occurs optionally,
                * then try to find the real setting from the parent.
                */
               m_maxOccurrences = PSDtdNode.OCCURS_OPTIONAL;
               PSDtdElement el = m_DTDTree.getElement(immediateParent);
               if (el != null)
               {
                  PSDtdAttribute attr = el.getAttribute(m_nodeName);
                  if (attr != null)
                  {
                     int occurs = attr.getOccurrence();
                     if (occurs != PSDtdAttribute.IMPLIED)
                     {  /* this may be FIXED, NOFIXED or REQUIRED.
                         * in all of these cases, it must occur once
                         */
                        m_maxOccurrences = PSDtdNode.OCCURS_ONCE;
                     }
                  }
               }
            }
            else
            {
               m_maxOccurrences=m_DTDTree.getMaxOccurrenceSetting(nodeName, immediateParent);
            }

            String occurString = "-undefined-";
            switch (m_maxOccurrences) {
               case PSDtdNode.OCCURS_ANY:
                  occurString = "ZERO OR MORE";
                  break;
               case PSDtdNode.OCCURS_ATLEASTONCE:
                  occurString = "ONE OR MORE";
                  break;
               case PSDtdNode.OCCURS_ONCE:
                  occurString = "ONE";
                  break;
               case PSDtdNode.OCCURS_OPTIONAL:
                  occurString = "ZERO OR ONE";
                  break;
               case PSDtdNode.OCCURS_UNKNOWN:
                  occurString = "UNKNOWN";
                  break;
            }

            // if we're logging the execution plan, store the DTD info
            m_appHandler.getLogHandler().write(new com.percussion.log.PSLogExecutionPlan(
                  m_appHandler.getId(), IPSDataErrors.EXEC_PLAN_LOG_DTD_OCCURS,
                  new Object[] { getFullName(), occurString}));
         }
         else if (m_parent == null)
         {  // the root node can only occur once
            m_maxOccurrences = PSDtdNode.OCCURS_ONCE;
         }
         else if ((m_flags & FLAG_IS_ATTRIBUTE) == FLAG_IS_ATTRIBUTE)
         {  // safe to assume the attribute is 0 or 1
            m_maxOccurrences = PSDtdNode.OCCURS_OPTIONAL;
         }
         else
         {  // for anything else, we really have no clue
            m_maxOccurrences = PSDtdNode.OCCURS_UNKNOWN;
         }

         // we made it this far, let's add it to our parent's child list now
         if (addAsChild && m_parent != null)
            m_parent.m_children.add(this);
      }

      ArrayList getChildren()
      {
         return m_children;
      }

      PSXmlNode getParent()
      {
         return m_parent;
      }

      void setDataSource(IPSDataExtractor dataSource)
      {
         m_dataSource = dataSource;
      }

      boolean isForData()
      {
         return (m_dataSource != null);
      }

      boolean doesXmlNodeContainMultipleOccurrenceElement()
      {
         if (m_children == null)
            return false;
         else
         {
            /* If anything under me repeats, then allow the collapse */
            for (int i = 0; i < m_children.size(); i++)
            {
               PSXmlNode curChild = (PSXmlNode) m_children.get(i);
               if ((curChild.m_maxOccurrences != PSDtdNode.OCCURS_ONCE)
                  && (curChild.m_maxOccurrences != PSDtdNode.OCCURS_OPTIONAL))
               {
                     return true;
               }
               else
               {
                  if (curChild.doesXmlNodeContainMultipleOccurrenceElement())
                     return true;
               }
            }

            /* This node contains all singletons! */
            /* No collapse nodes will be set, this node should be
               set to auto-clear before each row is processed */
            return false;
         }
      }

      /* A node is asking to be a collapse key for us */
      boolean requestCollapsePrivilege(PSXmlNode node)
      {
         if (node == null)
         {  // if this is null, there's not much to do
            return false;
         }
         else if (node.getParent() == null)
         {  // the root is non-repeating, which is what null parent implies
            return false;
         }
         else if ((m_flags & FLAG_CHECKED_NODEKIDS) == FLAG_CHECKED_NODEKIDS) { // We've already checked, return result
            return ((m_flags & FLAG_COLLAPSEABLE_NODE) == FLAG_COLLAPSEABLE_NODE);
         }
         else if ((m_maxOccurrences == PSDtdNode.OCCURS_ONCE) ||
                  (m_maxOccurrences == PSDtdNode.OCCURS_OPTIONAL))
         {  // if we're non-repeating, the node can't collapse us
            return false;
         }
         else
         {
            if (! ((m_flags & FLAG_CHECKED_NODEKIDS) == FLAG_CHECKED_NODEKIDS) )
            {
               m_flags |= FLAG_CHECKED_NODEKIDS;
               if (!doesXmlNodeContainMultipleOccurrenceElement())
               {
                  /* We must flag ourselves to always answer correctly,
                     and we must set add ourselves to the list of nodes
                     to be cleared prior to processing a single row */
                  /* We are not collapsable, as processrow will
                     clear us, so all requesting children are to be denied */
                  m_flags &= m_flags & ~FLAG_COLLAPSEABLE_NODE;

                  if (m_clearOnRowset == null)
                     m_clearOnRowset = new ArrayList();

                  m_clearOnRowset.add(this);
               } else
                  m_flags |= FLAG_COLLAPSEABLE_NODE;
            }
         }
         return ((m_flags & FLAG_COLLAPSEABLE_NODE) == FLAG_COLLAPSEABLE_NODE);
      }

      /**
       * Set the output value for this xml node in the supplied document for the
       * current execution data.
       *
       * @param data the execution data from which to extract the nodes value,
       *    assumed not <code>null</code>.
       * @param doc the document in which to set the xml node value, assumed
       *    not <code>null</code>.
       * @param dataStructs an array of node data objects of already processed
       *    nodes, assumed not <code>null</code>.
       * @return <code>true</code> if this node has conditionals and setting
       *    its value was processed because the conditionals evaluated to
       *    <code>true</code>, <code>false</code> otherwise.
       * @throws PSDataExtractionException if the data extraction fails.
       */
      void setValue(PSExecutionData data, Document doc,
         PSXmlNodeData[] dataStructs) throws PSDataExtractionException
      {
         boolean processed = setConditionalValue(data, doc, dataStructs);
         if (!processed)
         {
            Iterator conditionalNodes = getConditionalNodes();
            while (!processed && conditionalNodes.hasNext())
            {
               PSXmlNode conditionalNode =
                  (PSXmlNode) conditionalNodes.next();

               processed = conditionalNode.setConditionalValue(
                  data, doc, dataStructs);
            }
         }
      }

      /**
       * Set the output value for this xml node in the supplied document for the
       * current execution data.
       *
       * @param data the execution data from which to extract the nodes value,
       *    assumed not <code>null</code>.
       * @param doc the document in which to set the xml node value, assumed
       *    not <code>null</code>.
       * @param dataStructs an array of node data objects of already processed
       *    nodes, assumed not <code>null</code>.
       * @return <code>true</code> if this node has conditionals and setting
       *    its value was processed because the conditionals evaluated to
       *    <code>true</code>, <code>false</code> otherwise.
       * @throws PSDataExtractionException if the data extraction fails.
       */
      private boolean setConditionalValue(PSExecutionData data, Document doc,
         PSXmlNodeData[] dataStructs) throws PSDataExtractionException
      {
         Element     elementNode;
         PSXmlNode   parent;

         PSDebugLogHandler dh = (PSDebugLogHandler)data.getLogHandler();
         boolean traceEnabled =
            dh.isTraceEnabled(PSTraceMessageFactory.MAPPER_FLAG);

         if (traceEnabled && m_dataSource != null)
         {
            // need to construct full node name
            String fullName = m_nodeName;
            PSXmlNode nextParent = m_parent;

            // walk up parents to prepend each name
            while (nextParent != null)
            {
               fullName = nextParent.getName() + "/" + fullName;
               nextParent = nextParent.getParent();
            }

            // use the first value from data extractor
            String dataName;
            IPSReplacementValue[] values = m_dataSource.getSource();
            if (values != null && values.length > 0)
               dataName = values[0].getValueDisplayText();
            else
               dataName = "??";  // there should always be at least one

            Object[] args = {dataName, fullName};
            dh.printTrace(PSTraceMessageFactory.MAPPER_FLAG, args);
         }


         // if we're trying to set data but this is a conditional and
         // our condition was not met, ignore the set call
         if ((m_conditionChecker != null) && !m_conditionChecker.isMatch(data))
         {
            // trace skipped mappings
            if (traceEnabled)
            {
               Object[] args = {false};
               dh.printTrace(PSTraceMessageFactory.MAPPER_FLAG, args);
            }

            return false;
         }

         // trace mapping used if we have data
         if (traceEnabled && (m_dataSource != null))
         {
            Object[] args = {true};
            dh.printTrace(PSTraceMessageFactory.MAPPER_FLAG, args);
         }

         PSXmlNodeData parentData
            = (m_parent == null) ? null : dataStructs[m_parent.m_nodeId];
         PSXmlNodeData nodeData = dataStructs[m_nodeId];
         if ((m_flags & FLAG_IS_ATTRIBUTE) == FLAG_IS_ATTRIBUTE)
         {
            // need to set the parents attribute
            if (parentData.m_nodeElement == null)
            {
               m_parent.setValue(data, doc, dataStructs);
            }

            String curValue = computeValue(data).toString();
            nodeData.m_curValue = curValue;
            if (parentData.m_nodeElement != null)
               parentData.m_nodeElement.setAttribute(m_nodeName, curValue);
         }
         else
         {
            Object curValue = null;
            if (m_dataSource != null) {
               curValue = computeValue(data);
               if ((nodeData.m_curValue != null) &&
                  ( (m_maxOccurrences == PSDtdNode.OCCURS_ONCE) ||
                    (m_maxOccurrences == PSDtdNode.OCCURS_OPTIONAL) ) &&
                  (!isCollapseKey()))
               {
                  /* Get outta here, we haven't changed and
                     aren't supposed to be multiple! */
                  if (nodeData.m_curValue.equals(curValue.toString()))
                     return true;
               }
            }

            if (curValue instanceof Element)
            {
               elementNode = (Element) curValue;
               if (!elementNode.getTagName().equals(m_nodeName))
                  throw new PSDataExtractionException(0,
                     "The value element name must match the mapped element name.");

               // if this has no parent, it must be the root element
               if (m_parent == null)
               {
                  doc.appendChild(doc.importNode(elementNode, true));
               }
               else
               {
                  if (parentData.m_nodeElement == null)
                     m_parent.setValue(data, doc, dataStructs);

                  parentData.m_nodeElement.appendChild(
                     doc.importNode(elementNode, true));
               }
            }
            else
            {
            elementNode = doc.createElement(m_nodeName);

            // if this has no parent, it must be the root element
            if (m_parent == null) {
               doc.appendChild(elementNode);
            }
            else {
               // if the parent node doesn't exist, we must create it
               if (parentData.m_nodeElement == null)
               {
                  m_parent.setValue(data, doc, dataStructs);
               }

               parentData.m_nodeElement.appendChild(elementNode);
            }

            if (m_dataSource != null) {
                  nodeData.m_curValue = curValue.toString();
               Text textNode = doc.createTextNode(
                     PSXmlDocumentBuilder.normalize(curValue.toString()));
               elementNode.appendChild(textNode);
            }
            }

            // set this even on data elements as attributes may be
            // children of data elements
            nodeData.m_nodeElement = elementNode;
         }

         return true;
      }

      void clearElementData(PSXmlNodeData[] dataStructs)
      {
         PSXmlNode startClearing = this;

         for ( PSXmlNode cur = this;
               cur != null;
               cur = cur.m_parent)
         {
            // if this is not the root node, clear it out
            // only one root is permitted per document, which is why we
            // must skip the root node (causes a DOM exception if we do
            // it twice
            // now also skip this clear if this node is max once.
            startClearing = cur;
            if ( (cur.m_maxOccurrences != PSDtdNode.OCCURS_ONCE) &&
                 (cur.m_maxOccurrences != PSDtdNode.OCCURS_OPTIONAL) )
            {
               break;
            }
         }

         startClearing.clearChildElementData(dataStructs);
      }

      void clearElementDataTopDown(PSXmlNodeData[] dataStructs)
      {
         if ( (this.m_maxOccurrences == PSDtdNode.OCCURS_ONCE) ||
              (this.m_maxOccurrences == PSDtdNode.OCCURS_OPTIONAL) )
         {
            int size = m_children.size();
            for (int i = 0; i < size; i++)
            {
               ((PSXmlNode)m_children.get(i)).clearElementDataTopDown(dataStructs);
            }
         } else
         {
            this.clearChildElementData(dataStructs);
         }
      }

      void clearChildElementData(PSXmlNodeData[] dataStructs)
      {
         if (m_parent != null)
         {
            PSXmlNodeData node = dataStructs[m_nodeId];
            node.m_curValue = null;
            node.m_nodeElement = null;
         }

         // go through this node and all its children to clear them out
         int size = m_children.size();
         for (int i = 0; i < size; i++)
         {
            ((PSXmlNode)m_children.get(i)).clearChildElementData(dataStructs);
         }
      }

      /**
       * Get the value currently stored with this node.
       */
      Object getValue(PSXmlNodeData[] dataStructs)
      {
         Object curValue = dataStructs[m_nodeId].m_curValue;

         return (curValue == null) ? "" : curValue;
      }

      /**
       * Compute the current value for this node without storing it
       * internally.
       *
       * @param data the execution data from which to compute the value,
       *    assumed not <code>null</code>.
       * @return Object the computed object, never <code>null</code>.
       */
      Object computeValue(PSExecutionData data)
         throws com.percussion.data.PSDataExtractionException
      {
         if (m_dataSource == null)
            return "";

         Object o = m_dataSource.extract(data, null);
         if (o == null)
            return "";
         else if (m_textFormatter == null)
         {
            if (o instanceof Document)
               return ((Document) o).getDocumentElement();

            return o;
         }

         return m_textFormatter.format(o);
      }

      boolean hasChanged(PSXmlNodeData[] dataStructs, PSExecutionData data)
         throws com.percussion.data.PSDataExtractionException
      {
         // if this field is set conditionally and
         // our condition was not met, we can consider it unchanged
         if ((m_conditionChecker != null) && !m_conditionChecker.isMatch(data))
            return false;

         Object newValue = computeValue(data);
         Object oldValue = getValue(dataStructs);
         if (newValue instanceof Element)
         {
            if (!(oldValue instanceof Element))
               return true;

            Element newElement = (Element) newValue;
            Element oldElement = (Element) oldValue;
            return !oldElement.getTagName().equals(newElement.getTagName());
         }

         return !oldValue.toString().equals(newValue.toString());
      }

      boolean isSet(PSXmlNodeData[] dataStructs)
      {
         return (dataStructs[m_nodeId].m_nodeElement != null);
      }

      boolean areAllParentsSet(PSXmlNodeData[] dataStructs)
      {
         if (m_parent == null)
            return true;

         if (dataStructs[m_parent.m_nodeId].m_nodeElement == null)
            return false;

         return m_parent.areAllParentsSet(dataStructs);
      }

      boolean isCollapseKey()
      {
         return (m_flags & FLAG_IS_COLLAPSE_KEY) == FLAG_IS_COLLAPSE_KEY;
      }

      boolean isAttribute()
      {
         return (m_flags & FLAG_IS_ATTRIBUTE) == FLAG_IS_ATTRIBUTE;
      }

      void setCollapseKey(boolean enable)
      {
         if (enable)
            m_flags |= FLAG_IS_COLLAPSE_KEY;
         else
            m_flags &= ~FLAG_IS_COLLAPSE_KEY;
      }

      String getName()
      {
         return m_nodeName;
      }

      String getFullName()
      {
         if (m_parent == null)
            return m_nodeName;
         return m_parent.getFullName() + "/" + m_nodeName;
      }

      int getMaxOccurrences()
      {
         return m_maxOccurrences;
      }

      /**
       * Adds a new conditional node to the internal list of conditional xml
       * nodes.
       *
       * @param node to node to be added, not <code>null</code>, must contain
       *    conditions.
       */
      public void addConditionalNode(PSXmlNode node)
      {
         if (node == null)
            throw new IllegalArgumentException(
               "conditional node cannot be null");

         if (node.m_conditionChecker == null)
            throw new IllegalArgumentException(
               "conditional node must contain conditions");

         if (m_conditionalNodes == null)
            m_conditionalNodes = new ArrayList();

         m_conditionalNodes.add(node);
      }

      /**
       * Get all conditional nodes.
       *
       * @return an <code>Iterator</code> over <code>PSXmlNode</code> objects
       *    representing all conditional nodes, never <code>null</code>, may
       *    be empty.
       */
      public Iterator getConditionalNodes()
      {
         if (m_conditionalNodes == null)
            return PSIteratorUtils.emptyIterator();

         return m_conditionalNodes.iterator();
      }

      java.lang.String           m_nodeName;
      int                        m_nodeId;
      PSXmlNode                  m_parent;
      ArrayList                  m_children;
      java.lang.String           m_curValue;
      IPSDataExtractor           m_dataSource;
      PSConditionalEvaluator  m_conditionChecker;
      int                        m_maxOccurrences;
      int                        m_flags;
      java.text.Format           m_textFormatter;

      /**
       * A list of conditional xml nodes, initialized in the first call to
       * <code>addConditionalNode(PSXmlNode)</code>. May be <code>null</code>
       * if there are no conditional nodes. The order of this list is the
       * order in which the nodes will be processed.
       */
      private List m_conditionalNodes = null;
   }

   private static final int   FLAG_IS_COLLAPSE_KEY    = 0x0001;
   private static final int   FLAG_IS_ATTRIBUTE       = 0x0002;
   private static final int   FLAG_CHECKED_NODEKIDS   = 0x0004;
   private static final int   FLAG_COLLAPSEABLE_NODE  = 0x0008;

   // this is just a light weight storage class we build to manage the data
   // for the handling of a single request. We avoid synchronization using
   // this model, which speeds up overal processing time
   class PSXmlNodeData
   {
      PSXmlNodeData()
      {
         super();
      }

      Object m_curValue = null;
      Element m_nodeElement = null;
   }

   class PSXmlCollapseNode
   {
      PSXmlCollapseNode()
      {
         super();
      }

      void setClearNode(PSXmlNode node)
      {
         m_clearNode = node;
      }

      void setCollapseNode(PSXmlNode node)
      {
         node.setCollapseKey(true);
         m_collapseNode = node;
      }

      /* Clear the Nodes associated with this collapse node */
      void clear(PSXmlNodeData[] dataStructs)
      {
         if (m_clearNode != null)
               m_clearNode.clearChildElementData(dataStructs);
      }

      boolean needsClear(PSXmlNodeData[] dataStructs, PSExecutionData data)
         throws com.percussion.data.PSDataExtractionException
      {
         if (m_collapseNode != null)
            return m_collapseNode.hasChanged(dataStructs, data);
         else
            return false;
      }

      PSXmlNode      m_collapseNode;
      PSXmlNode      m_clearNode = null;
   }

   protected PSRequestor m_requestor;

   /**
    * Result page set. Initialized to empty. May be reinitialized in the ctor.
    * Never <code>null</code> may be empty.
    */
   protected PSResultPageSet m_resultPageSet = new PSResultPageSet();

   private PSApplicationHandler           m_appHandler;
   private PSDataSet                      m_ds;
   private PSXmlNode[][]                  m_RequestLinks;
   private PSSetStyleSheetEvaluator[]  m_StyleSheetEvaluators;
   private java.net.URL                   m_DTD;

   /**
    * A map with all data mappings, the key is the fully qualified XML name
    * while the value is a <code>PSXmlNode</code>.
    */
   private HashMap m_DataMappings;

   private Vector m_resultDocExtensions=null;

   private PSRequestLinkGenerator[]       m_pagerLinkGenerators;

   private PSDtdTree                      m_DTDTree;
   private PSXmlNode                      m_xmlRoot;
   private int                            m_nodeCount = 0;
   private ArrayList                      m_clearOnRowset;
   private PSXmlCollapseNode[]            m_collapseKeys;

   /** Does this converter consider a result set to indicate no Xml data? */
   private boolean                        m_allowEmptyDoc = false;

   private int                      m_maxRowsPerPage;
   private int                      m_maxPages;
   /** Used to determine the maximum amount of page links displayed */
   private int                      m_maxPageLinks;
   private PSPagedRequestLinkGenerator    m_nextLink = null;
   private PSPagedRequestLinkGenerator    m_prevLink = null;
   private PSPagedRequestLinkGenerator    m_indexLink = null;

   /**
    * Attribute in the request that indicates which row in the ResultSet
    * will be used as the first row on the page.
    */
   static public final String    FIRST_QUERY_INDEX_PARAMETER_NAME = "psfirst";
}

