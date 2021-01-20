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
package com.percussion.deployer.catalog;

import com.percussion.deployer.objectstore.IPSDeployComponent;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The result set that represents the catalog request results. This is useful
 * to represent the results data in tabular form. 
 */
public class PSCatalogResultSet implements IPSDeployComponent
{ 
   /**
    * Constructs the result set with <code>null</code> column meta data and 
    * empty result set.
    */
   public PSCatalogResultSet()
   {      
   } 
   
   /**
    * Constructs the object with the specified column metadata.
    * 
    * @param columns the column meta data of the results, may not be 
    * <code>null</code> or empty. The values in the array should not be 
    * <code>null</code>
    * 
    * @throws IllegalArgumentException if the objects in the results list is not
    * of type <code>PSCatalogResult</code>
    */
   public PSCatalogResultSet(PSCatalogResultColumn[] columns)
   {
      if(columns == null || columns.length == 0)
         throw new IllegalArgumentException(
            "columns may not be null or empty.");
            
      for(int i=0; i<columns.length; i++)
      {
         if(columns[i] == null)
            throw new IllegalArgumentException(
               "the objects in columns array may not be null");
      }         
      m_columns = columns;
   }   
   
   /**
    * Constructs the object from the supplied element.
    * 
    * @param sourceNode the element to construct the object from, may not be 
    * <code>null</code>. See {@link #toXml(Document)} for format of XML. 
    * 
    * @throws PSUnknownNodeTypeException if the document does not have elements
    * or attributes expected by this class or its children.
    * @throws IllegalArgumentException if doc is <code>null</code>
    */
   public PSCatalogResultSet(Element sourceNode)
      throws PSUnknownNodeTypeException   
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");   
         
      fromXml(sourceNode);
   }
   
   /**
    * Restores this object's state from its XML representation.  See
    * {@link #toXml(Document)} for format of XML.  See 
    * {@link IPSDeployComponent#fromXml(Element)} for more info on method
    * signature.
    */
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");
         
      //make sure we got the correct root node tag
      if (false == XML_NODE_NAME.equals (sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);         
      int firstFlags = PSXmlTreeWalker.GET_NEXT_RESET_CURRENT |
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_RESET_CURRENT |
         PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;         
      
      m_columns = null;   
      Element columnMeta = tree.getNextElement(XML_COLUMN_META,
         firstFlags);
      if(columnMeta != null)
      {
         Element resultColumn = tree.getNextElement(
            PSCatalogResultColumn.XML_NODE_NAME, firstFlags);
         if(resultColumn == null)
         {
            Object[] args =
            { XML_COLUMN_META, PSCatalogResultColumn.XML_NODE_NAME, "null" };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         List catalogResultColumns = new ArrayList();
         while(resultColumn != null)
         {
            catalogResultColumns.add(new PSCatalogResultColumn(resultColumn));
            resultColumn = tree.getNextElement(
               PSCatalogResultColumn.XML_NODE_NAME, nextFlags);
         }
         m_columns = (PSCatalogResultColumn[])catalogResultColumns.toArray(
            new PSCatalogResultColumn[catalogResultColumns.size()]);
         tree.setCurrent(sourceNode);               
      }      
      
      m_results.clear();      
      Element catalogResult = tree.getNextElement(PSCatalogResult.XML_NODE_NAME,
         firstFlags);            
         
      while(catalogResult != null)
      {  
         PSCatalogResult result = new PSCatalogResult(catalogResult);
         if(validateResultToAdd(result))
            m_results.add(result);                     
         else
         {
            Object[] args =
            { XML_NODE_NAME, PSCatalogResult.XML_NODE_NAME,  
               "invalid column data" };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         catalogResult = tree.getNextElement(PSCatalogResult.XML_NODE_NAME,
            nextFlags);
      }
   }
   
   /**
    * Serializes this object's state to its XML representation.  Format is:
    * 
    * <pre><code>
    *    %lt;!--
    *       Please see {@link PSCatalogResult#toXml(Document) toXml} for the 
    *       format of the <code>PSXCatalogResult</code>
    *    -->
    *    %lt;!ELEMENT PSXCatalogResultSet(PSXCatalogResultColumnMeta?, 
    *       PSXCatalogResult*))>
    *    %li;!--
    *    The definition of the result columns. Please see {@link 
    *    PSCatalogResultColumn#toXml(Document) toXml} for the format of the
    *    <code>PSXCatalogResultColumn</code>
    *    -->  
    *    %lt;!ELEMENT PSXCatalogResultColumnMeta (PSXCatalogResultColumn+)>
    * </code></pre>
    * 
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if(doc == null)
         throw new IllegalArgumentException("doc may not be null");
         
      Element root = doc.createElement(XML_NODE_NAME);
      if(m_columns != null)
      {
         Element columnMeta = PSXmlDocumentBuilder.addEmptyElement(
            doc, root, XML_COLUMN_META);
         for(int i=0; i<m_columns.length; i++)
            columnMeta.appendChild(m_columns[i].toXml(doc));         
      }
      Iterator results = getResults();
      while(results.hasNext())
      {
         PSCatalogResult result = (PSCatalogResult)results.next();
         root.appendChild(result.toXml(doc));
      }
      
      return root;
   }
   
   //IPSDeployComponent interface implementation 
   public void copyFrom(IPSDeployComponent obj)
   {
      if(!(obj instanceof PSCatalogResultSet))
         throw new IllegalArgumentException(
            "obj must be an instance of PSCatalogResultSet");
            
      PSCatalogResultSet set = (PSCatalogResultSet)obj;      
      int numColumns = set.m_columns.length;
      System.arraycopy(set.m_columns, 0, m_columns, 0, numColumns);
                  
      m_results.clear();
      m_results.addAll(set.m_results);
   }
   
   //IPSDeployComponent interface implementation
   public int hashCode()
   {
      int hashCode = 0; 
      if(m_columns != null)
      {
         for(int i=0; i<m_columns.length; i++)
         {
            hashCode += m_columns[i].hashCode();
         }
      }
      return hashCode + m_results.hashCode();
   }
   
   //IPSDeployComponent interface implementation
   public boolean equals(Object obj)
   {
      if(!(obj instanceof PSCatalogResultSet))
         return false;
      
      PSCatalogResultSet set = (PSCatalogResultSet)obj;            
      boolean equals = true;

      if(m_columns != null && set.m_columns != null)      
      {
         if(m_columns.length != set.m_columns.length)
            equals = false;
         else
         {
            for(int i=0; i<m_columns.length; i++)
            {
               equals = m_columns[i].equals(set.m_columns[i]);               
               if(!equals)
                  break;
            }
         }
      }
      else
      {
         equals = (m_columns == null && set.m_columns == null);
      }
      if(equals)
         equals = m_results.equals(set.m_results);         

      return equals;      
   }
   
   /**
    * Validates and adds the result to this set. Please see {@link 
    * #validateResultToAdd(PSCatalogResult) validate} for more information on
    * how the result is validated.
    * 
    * @param result the result to be added, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if the result is invalid to add.
    */
   public void addResult(PSCatalogResult result)
   {     
      if(validateResultToAdd(result))
         m_results.add(result);
      else
         throw new IllegalArgumentException(
            "result is invalid to add to this set.");
   }
   
   /**
    * Validates that the result can be added to this result set. Does the 
    * following for validation.
    * <ol>
    * <li>Makes sure that either both(resultset and result) support column data
    * or do not support column data.</li>
    * <li>If both support columns, the column data in the result must be 
    * supported by the corresponding column metadata definition in the resultset
    * testing in the order.
    * </li>
    * 
    * @param result the result to add, may not be <code>null</code>
    * 
    * @return <code>true</code> if it can be added, otherwise <code>false</code>
    */
   public boolean validateResultToAdd(PSCatalogResult result)
   {
      if(result == null)
         throw new IllegalArgumentException("result may not be null.");
         
      PSCatalogResultColumn[] columnMeta = getColumns();
      boolean canAdd = true;
      if( (columnMeta == null && result.getColumns().length != 0) ||
         (columnMeta != null && columnMeta.length != 
            result.getColumns().length) )
      {
         canAdd = false;
      }
      else if(columnMeta != null)
      {
         Object[] columns = result.getColumns();
         for(int i=0; i<columnMeta.length; i++)
         {
            int type = columnMeta[i].getType();
            canAdd = PSCatalogResultColumn.validateObjectType(type, columns[i]);
            if(!canAdd)
               break;
         }         
      }
         
      return canAdd;
   }
   
   /**
    * Gets the catalog results.
    * 
    * @return the results, never <code>null</code> may be empty.
    */
   public Iterator getResults()
   {
      return m_results.iterator();
   }
   
   /**
    * The column metadata of this result set if it has results with multiple
    * columns.
    * 
    * @return the column metadata, may be <code>null</code>
    */
   public PSCatalogResultColumn[] getColumns()
   {
      return m_columns;
   }   
   
   /**
    * The column metadata of the resultset, initialized if this resultset 
    * represents data with multiple columns, <code>null</code> otherwise.
    */
   private PSCatalogResultColumn[] m_columns = null;
   
   /**
    * The list of <code>PSCatalogResult</code> objects, initialized to empty 
    * list and gets updated with results.
    */
   private List m_results = new ArrayList();
   
   private static final String XML_NODE_NAME = "PSXCatalogResultSet";
   private static final String XML_COLUMN_META = "PSXCatalogResultColumnMeta";   
}
