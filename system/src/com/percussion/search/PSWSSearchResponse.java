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

package com.percussion.search;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is an object representation of search results returned by 
 * web services search.  This class does not implement all functionality 
 * requried to support the schema defined by the WSDL for such results; it only 
 * provides the functionality required to process search results for the content 
 * explorer applet.
 */
public class PSWSSearchResponse
{
   /**
    * Construct this object from its xml representation.  If 
    * <code>curUser</code> is supplied, then the <code>sys_revision</code>
    * field will be calculated based on the current user if it is not found in 
    * the results.  
    * @param doc The document returned by a web services search request.  May 
    * not be <code>null</code>.
    * @param curUser Optionally supplied to calculate the 
    * <code>sys_revision</code> field value for each row in the results.  If not
    * <code>null</code> or empty, then it is compared against the value of the 
    * <code>sys_contentcheckoutusername</code> field, and if they match, the 
    * value of <code>sys_tiprevision</code> is used, otherwise 
    * the value of <code>sys_currentrevision</code> is used.
    * @throws PSUnknownNodeTypeException if an element in the source document is 
    * invalid.
    */
   public PSWSSearchResponse(Document doc, String curUser)
         throws PSUnknownNodeTypeException
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      
      // walk each row
      int firstFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
      int nextFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
      
      PSXmlTreeWalker tree = new PSXmlTreeWalker(doc);
      Element searchResponse = doc.getDocumentElement();
      if (searchResponse == null)
      {
         throw new IllegalArgumentException("Missing root element");
      }
      String truncated = searchResponse.getAttribute(ATTR_TRUNCATED);
      m_truncated = truncated != null && truncated.equalsIgnoreCase("true");
      
      Element row = tree.getNextElement(PSSearchResultRow.XML_NODE_NAME,
            firstFlag);
      while (row != null)
      {
         IPSSearchResultRow resRow = createRow(row, curUser);
         m_rowList.add(resRow);
         tree.setCurrent(row);
         row = tree.getNextElement(PSSearchResultRow.XML_NODE_NAME, nextFlag);
      }
   }

   /**
    * Creates a search result from the specified element.
    * 
    * @param rowElem the element contains the search result of an item, not <code>null</code>.
    * @param curUser current user, may be <code>null</code> or empty.
    * 
    * @return the created search result, not <code>null</code>.
    * 
    * @throws PSUnknownNodeTypeException if the element does not contain a search result.
    */
   public IPSSearchResultRow createRow(Element rowElem, String curUser) throws PSUnknownNodeTypeException
   {
       notNull(rowElem, "rowElem");

       PSSearchResultRow resRow = new PSSearchResultRow(rowElem);
       
       // determine the revision and append the field if a request is supplied
       if (curUser != null && curUser.trim().length() > 0)
       {
          String curRev = resRow.getColumnValue("sys_currentrevision");
          String tipRev = resRow.getColumnValue("sys_tiprevision");
          String checkOutUser = resRow.getColumnValue(
             "sys_contentcheckoutusername");
          
          String sysRev;
          if (checkOutUser.trim().length() == 0 || 
             checkOutUser.equalsIgnoreCase(curUser))
          {
             sysRev = tipRev;
          }
          else
             sysRev = curRev;
            
          if(resRow.hasColumn(IPSHtmlParameters.SYS_REVISION))
          {
             resRow.setColumnValue(IPSHtmlParameters.SYS_REVISION, sysRev);
          }
          else
          {
             PSSearchResultColumn field = new PSSearchResultColumn(
                   IPSHtmlParameters.SYS_REVISION, sysRev, sysRev);
             resRow.addColumn(field);
          }
       }
       return resRow;   
   }
   
   /**
    * Construct this object from its xml representation.  
    * @param doc The document returned by a web services search request.  May 
    * not be <code>null</code>.
    * @throws PSUnknownNodeTypeException if an element in the source document is 
    * invalid.
    */
   public PSWSSearchResponse(Document doc) throws PSUnknownNodeTypeException
   {
      this(doc, null);
   }
   
   /**
    * Construct an empty set of search results
    */
   public PSWSSearchResponse()
   {
   }
   
   /**
    * Appends a <code>Result</code> element for each row in the results to the
    * root element of the supplied document.
    * 
    * @param responseDoc The document to which the result rows are appended,
    * may not be <code>null</code>.
    */
   public void appendSearchResponseResults(Document responseDoc)
   {
      if (responseDoc == null)
         throw new IllegalArgumentException("responseDoc may not be null");
         
      Element root = responseDoc.getDocumentElement();
      
      Iterator<IPSSearchResultRow> rows = m_rowList.iterator();
      while (rows.hasNext())
      {
         PSSearchResultRow row = (PSSearchResultRow)rows.next();
         root.appendChild(row.toXml(responseDoc));
      }
   }
 
   /**
    * Returns an iterator over zero or more <code>PSSearchResultRow</code>
    * objects, each representing a row of results.
    * 
    * @return An iterator over zero or more <code>IPSSearchResultRow</code>
    *         objects, each representing a row of results.
    */
   public Iterator<IPSSearchResultRow> getRows()
   {
      return m_rowList.iterator();
   }
 
   /**
    * Add the specified row to the search result.
    * 
    * @param row the qppended row, not <code>null</code>.
    */
   public void addRow(IPSSearchResultRow row)
   {
       notNull(row, "row");

       m_rowList.add(row);
   }
   
   /**
    * Returns a cloned row list as <code>List</code> object. Modification
    * of the cloned list has no effect on this object or its members.
    * 
    * @return the modifiable row list, never <code>null</code> may be empty.
    */
   public List<IPSSearchResultRow> getRowList()
   {
      List<IPSSearchResultRow> list = 
         new ArrayList<IPSSearchResultRow>(m_rowList.size());
      Iterator<IPSSearchResultRow> iter = m_rowList.iterator();
      while (iter.hasNext())
      {
         IPSSearchResultRow element = iter.next();
         list.add(element.cloneRow());
      }
      return list;
   }

   /**
    * Set the supplied rows on these results, replacing existing rows.
    * 
    * @param rows A list of zero or more <code>PSSearchResultRow</code>
    * objects, each representing a row of result data. See {@link #getRows()}
    * for more info. May not be <code>null</code>.
    */
   public void setRows(List<IPSSearchResultRow> rows)
   {
      if (rows == null)
      {
         throw new IllegalArgumentException("rows must not be null");
      }
      
      try
      {
         rows.toArray(new IPSSearchResultRow[rows.size()]);
      }
      catch (Exception e)
      {
         throw new IllegalArgumentException(
               "rows must contain PSSearchResultColumn objects");
      }

      m_rowList.clear();
      m_rowList.addAll(rows);
   }
   
   /**
    * Cycle through the rows and add the given column if it does not already
    * exist in the rows. If the column is already there then this call has no
    * effect.
    * 
    * @param columnName the column name, never <code>null</code> or empty
    * @param defaultValue the initial value, may be <code>null</code> or
    *           empty.
    */
   public void addColumn(String columnName, String defaultValue)
   {
      if (columnName == null || StringUtils.isBlank(columnName))
      {
         throw new IllegalArgumentException("columnName may not be null or empty");
      }
      for(IPSSearchResultRow row : m_rowList)
      {
         PSSearchResultRow rr = (PSSearchResultRow) row;
         if (!rr.hasColumn(columnName))
         {
            PSSearchResultColumn col = new PSSearchResultColumn(columnName,
                  null, defaultValue);
            rr.addColumn(col);
         }
      }
   }
 
   
   /**
    * Is this result truncated?
    * @return <code>true</code> if this result could have had more rows. 
    */
   public boolean isTruncated()
   {
      return m_truncated;
   }
   
   /**
    * Set new truncated state
    * @param truncated new value for truncated
    */
   public void setTruncated(boolean truncated)
   {
      m_truncated = truncated;
   }
 
   /**
    * List of row data for these results. Never <code>null</code>, data is
    * initialized in the ctor. List contains a {@link PSSearchResultRow}object
    * for each row.
    */
   private List<IPSSearchResultRow> m_rowList = 
      new ArrayList<IPSSearchResultRow>();

   /**
    * Is this response truncated, i.e. were there more search responses that
    * could have been returned.
    */
   private boolean m_truncated = false;
   
   // xml constant(s) from sys_SearchParameters.xsd
   private static final String ATTR_TRUNCATED = "truncated";

}
