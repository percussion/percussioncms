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

package com.percussion.cas;

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.extensions.general.PSMakeIntLink;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This exit is used to produce database publisher documents that conform to
 * the sys_DatabasePublisher.dtd out of input documents conforming to the
 * markup.dtd.
 */
public class PSDatabasePublisher implements IPSResultDocumentProcessor
{

   private static final Logger log = LogManager.getLogger(PSDatabasePublisher.class);

   // see IPSResultDocumentProcessor#canModifyStyleSheet()
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   // see IPSExtensionDef#init(IPSExtensionDef, File)
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      ms_fullExtensionName = extensionDef.getRef().toString();
      if (ms_paramCount == NOT_INITIALIZED)
      {
         ms_paramCount = 0;

         Iterator iter = extensionDef.getRuntimeParameterNames();
         while (iter.hasNext())
         {
            iter.next();
            ms_paramCount++;
         }
      }
   }

   // see IPSResultDocumentProcessor#processResultDocument(Object[], IPSRequestContext, Document)
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document doc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      Document resultDoc = null;
      try
      {
         String action = REPLACE_ACTION;
         String aliasLookup = null;
         if (params != null)
         {
            /*
             * The default action is 'replace'. This can be overwritten with an
             * optional first parameter provided.
             */
            if (params.length > 0 && params[0] != null &&
               params[0].toString().length() > 0)
               action = params[0].toString().toLowerCase();
            validateAction(action);

            // did they provid an aliasmap
            if (params.length > 1 && params[1] != null &&
               params[1].toString().length() > 0)
            {
               Object[] args =
               {
                  params[1].toString()
               };
               PSMakeIntLink link = new PSMakeIntLink();
               aliasLookup = link.processUdf(args, request).toString();
            }
         }

         // get alias map if provided
         m_aliasMap = getAliasMap(lookup(request, aliasLookup));

         // create a new result document
         resultDoc = PSXmlDocumentBuilder.createXmlDocument();

         // get the required parent table element
         NodeList databases = doc.getElementsByTagName(DATABASE_ELEM);
         if (databases == null || databases.getLength() == 0)
            throw new PSExtensionProcessingException(0,
               "No database element found");
         Element database = (Element) databases.item(0);

         // create the datapublisher element
         Element dataPublisher = resultDoc.createElement(DATAPUBLISHER_ELEM);
         dataPublisher.setAttribute(DBNAME_ATTR,
            database.getAttribute(DBNAME_ATTR));
         dataPublisher.setAttribute(DRIVERTYPE_ATTR,
            database.getAttribute(DRIVERTYPE_ATTR));
         dataPublisher.setAttribute(RESOURCENAME_ATTR,
            database.getAttribute(RESOURCENAME_ATTR));
         dataPublisher.setAttribute(ORIGIN_ATTR,
            database.getAttribute(ORIGIN_ATTR));
         resultDoc.appendChild(dataPublisher);

         // lookup the table definition and add it to the datapublisher
         NodeList tabledefs = doc.getElementsByTagName(TABLEDEFSET_ELEM);
         if (tabledefs == null || tabledefs.getLength() == 0)
            throw new PSExtensionProcessingException(0,
               "No tabledefset element found");
         Element tabledef = (Element) tabledefs.item(0);
         PSXmlDocumentBuilder.copyTree(resultDoc,
            dataPublisher,
            lookup(request, tabledef.getAttribute(LOOKUP_ATTR)),
            true);

         // create the table dataset for the parent and add it to the datapublisher
         Element tableDataSet = resultDoc.createElement(TABLEDATASET_ELEM);
         dataPublisher.appendChild(tableDataSet);

         // append all childtable datasets to the parenttable dataset
         tableDataSet.appendChild(createParentTableData(request, doc,
            resultDoc, action));

         return resultDoc;
      }
      catch (Throwable t)
      {
         log.error(t.getMessage());
         log.debug(t.getMessage(), t);
         throw new PSExtensionProcessingException(0, t.getLocalizedMessage());
      }
      finally
      {
         return resultDoc;
      }
   }

   /**
    * Get the alias map out of the provided element.
    *
    * @param aliases an element that contains all specified aliases, might be
    *    <code>null</code>.
    * @return a map of alias String objects as key and name String objects as
    *    value, never <code>null</code>, might be empty.
    */
   private Map getAliasMap(Element aliases)
   {
      Map aliasMap = new HashMap();
      if (aliases != null)
      {
         NodeList nodes = aliases.getElementsByTagName(ALIAS_ELEM);
         for (int i=0; i<nodes.getLength(); i++)
         {
            Element elem = (Element) nodes.item(i);
            aliasMap.put(elem.getAttribute(NAME_ATTR),
               elem.getAttribute(FOR_ATTR));
         }
      }

      return aliasMap;
   }

   /**
    * Gets the replacement value for the specified alias.
    *
    * @param alias the alias we want the replacement value for, assumed not
    *    <code>null</code> or empty.
    * @return the replacement value found for the supplied alias or the alias
    *    if not found, never <code>null</code> or empty.
    */
   private String resolveAlias(String alias)
   {
      if (m_aliasMap != null && m_aliasMap.containsKey(alias))
         return (String) m_aliasMap.get(alias);

      return alias;
   }

   /**
    * Creates the parent <table> element and all its children and childrens
    * children.
    *
    * @param request the request context to for internal lookups, assumed not
    *    <code>null</code>.
    * @param source the source document, assumed not <code>null</code>.
    * @param target the target document, assumed not <code>null</code>.
    * @param action the row action string, assumed not <code>null</code> or
    *    empty.
    * @return the <table> element to added to the <tabledefset> element, never
    *    <code>null</code>.
    * @throws PSExtensionProcessingException for all required elements not found
    *    and any processing error.
    */
   private Element createParentTableData(IPSRequestContext request,
      Document source, Document target, String action)
      throws PSExtensionProcessingException
   {
      NodeList tables = source.getElementsByTagName(TABLE_ELEM);
      if (tables == null || tables.getLength() == 0)
         throw new PSExtensionProcessingException(0,
            "No parent table element found.");
      Element table = (Element) tables.item(0);

      Element tableElem = target.createElement(TABLE_ELEM);
      tableElem.setAttribute(NAME_ATTR,
         resolveAlias(table.getAttribute(NAME_ATTR)));

      // loop over target rows
      NodeList targetRows = table.getChildNodes();
      for (int i=0; i<targetRows.getLength(); i++)
      {
         if (!(targetRows.item(i) instanceof Element))
            continue;
         Element tagetRow = (Element)targetRows.item(i);
         NodeList columns = tagetRow.getChildNodes();
         if (columns.getLength() > 0)
         {
            // parent table row
            Element row = target.createElement(ROW_ELEM);
            row.setAttribute(ACTION_ATTR, action);
            tableElem.appendChild(row);

            // create all columns
            for (int j=0; j<columns.getLength(); j++)
            {
               if (!(columns.item(j) instanceof Element))
                  continue;
               Node column = columns.item(j);
               addColumn(column, target, row);
            }

            // create all children
            for (int j=0; j<columns.getLength(); j++)
            {
               if (!(columns.item(j) instanceof Element))
                  continue;
               Node column = columns.item(j);
               if (column.getNodeName().equals(CHILDREN_ELEM))
               {
                  Node kid = columns.item(j);
                  NodeList childTables = kid.getChildNodes();
                  for (int k=0; k<childTables.getLength(); k++)
                  {
                     if (!(childTables.item(k) instanceof Element))
                        continue;
                     Element child = (Element) childTables.item(k);
                     addChild(request, source, target, action, child, row);
                  }
               }
            }
         }
      }

      return tableElem;
   }

   /**
    * Adds all <childtable> elements recursive to the specified destination
    * element.
    *
    * @param request the request context to for internal lookups, assumed not
    *    <code>null</code>.
    * @param source the source document, assumed not <code>null</code>.
    * @param target the target document, assumed not <code>null</code>.
    * @param action the row action string, assumed not <code>null</code> or
    *    empty.
    * @param dest the destination element to which to append the childtables,
    *    assumed not <code>null</code>.
    * @return the <table> element to added to the <tabledefset> element, never
    *    <code>null</code>.
    * @throws PSExtensionProcessingException for all requred elements not found
    *    and any processing error.
    */
   private void addChild(IPSRequestContext request, Document source,
      Document target, String action, Element child, Element dest)
      throws PSExtensionProcessingException
   {
      Element childElem = lookup(request, child.getAttribute(LOOKUP_ATTR));
      if (childElem == null)
         return;

      // craet the childtable element
      Element childtable = target.createElement(CHILDTABLE_ELEM);
      dest.appendChild(childtable);

      Element table = (Element) childElem.getFirstChild();
      if (table == null || table.getAttribute(NAME_ATTR).length() == 0)
         throw new PSExtensionProcessingException(0,
            "No child table element found.");
      childtable.setAttribute(NAME_ATTR,
         resolveAlias(table.getAttribute(NAME_ATTR)));

      NodeList rows = getRows(table.getChildNodes());
      if (rows != null)
      {
         for (int i=0; i<rows.getLength(); i++)
         {
            Node rowNode = rows.item(i);

            Element row = target.createElement(ROW_ELEM);
            row.setAttribute(ACTION_ATTR, action);
            childtable.appendChild(row);

            // 1st add all columns
            NodeList columns = rowNode.getChildNodes();
            for (int j=0; j<columns.getLength(); j++)
            {
               Node columnNode = columns.item(j);
               if (!columnNode.getNodeName().equals(CHILDREN_ELEM))
               {
                  if (columnNode.hasChildNodes())
                  {
                     NodeList test = columnNode.getChildNodes();
                     for (int k=0; k<test.getLength(); k++)
                        addColumn(columnNode, target, row);
                  }
                  else
                     addColumn(columnNode, target, row);
               }
            }

            // then add all children
            for (int j=0; j<columns.getLength(); j++)
            {
               Node columnNode = columns.item(j);
               if (columnNode.getNodeName().equals(CHILDREN_ELEM))
               {
                  Element kid = (Element) columnNode.getFirstChild();
                  addChild(request, source, target, action, kid, row);
               }
            }
         }
      }
   }

   /**
    * Creates a new column element for the provided column node and append it
    * to the supplied row.
    *
    * @param column the column node for which this creates a new column
    *    element, assumed not <code>null</code>.
    * @param target the target document, assumed not <code>null</code>.
    * @param row the row element to which the newly created column will be
    *    appended, assumed not <code>null</code>.
    */
   private void addColumn(Node column, Document target, Element row)
   {
      Element columnElem = target.createElement(COLUMN_ELEM);
      if (!column.getNodeName().equals(CHILDREN_ELEM))
      {
         columnElem.setAttribute(NAME_ATTR,
            resolveAlias(column.getNodeName()));
         
         if(column.hasChildNodes())
         {
            PSXmlDocumentBuilder.copyTree(target,
               columnElem,
               column.getFirstChild(),
               true);
         }
         row.appendChild(columnElem);

         // add all supplied column attributes
         NamedNodeMap attributes = column.getAttributes();
         if (attributes != null)
         {
            for (int i=0; i<attributes.getLength(); i++)
            {
               Node attribute = attributes.item(i);
               if (attribute instanceof Attr)
               {
                  Attr attr = (Attr) attribute;
                  columnElem.setAttribute(attr.getName(), attr.getValue());
               }
            }
         }
      }
   }

   /**
    * The markup of the supplied node list is checked for <rowid> markup. If
    * <rowid> markup is provided, row list is one level deeper, otherwise
    * the provided list is already the row list.
    *
    * @param rows a list of nodes to be tested if it already is the row list or
    *    if we have to go one level deeper. Might be <code>null</code> or empty.
    * @return a NodeList of rows, might be <code>null</code> or empty.
    */
   private NodeList getRows(NodeList rows)
   {
      if (rows != null && rows.getLength() > 0)
      {
         Node row = rows.item(0);
         if (row.hasChildNodes())
         {
            Node child = row.getFirstChild();
            Node rowid = child.getFirstChild();
            if (!(rowid instanceof Text))
               return row.getChildNodes();
         }
      }

      return rows;
   }

   /**
    * Makes an internal lookup request for the supplided resource and returns
    * its document element.
    *
    * @param request the request context to use for internal requests, assumed
    *    not <code>null</code>.
    * @param resource the resource to lookup, assumed not <code>null</code>.
    * @return the document element of the lookup result, never
    *    <code>null</code>.
    * @throws PSExtensionProcessingException if the internal lookup fails.
    */
   private Element lookup(IPSRequestContext request,
      String resource) throws PSExtensionProcessingException
   {
      Element result = null;
      try
      {
         IPSInternalRequest ir = request.getInternalRequest(resource);
         if (ir == null)
            throw new PSExtensionProcessingException(0,
               "No lookup request handler found for: " + resource);

         Document resultDoc = ir.getResultDoc();
         result = resultDoc.getDocumentElement();
      }
      catch (PSInternalRequestCallException e)
      {
         throw new PSExtensionProcessingException(ms_fullExtensionName, e);
      }
      finally
      {
         return result;
      }
   }

   /**
    * Validates that we know the supplied action.
    *
    * @param action the actio to validate, assumed not <code>null</code>.
    * @throws PSExtensionProcessingException if the supplied action is invalid.
    */
   private void validateAction(String action)
      throws PSExtensionProcessingException
   {
      if (action.equals(INSERT_ACTION) || action.equals(UPDATE_ACTION) ||
         action.equals(DELETE_ACTION) || action.equals(REPLACE_ACTION))
         return;

      throw new PSExtensionProcessingException(0,
         "Invalid action specified: " + action);
   }

   /**
    * The fully qualified name of this extension.
    */
   static private String ms_fullExtensionName = "";

   /**
    * The number of parameters provided for this exit. Initially set to
    * NOT_INITIALIZED to reflect that the #init(IPSExtensionDef, File) method
    * has not been called yet. Is set during the first call to #init(
    * IPSExtensionDef, File).
    */
   public static int ms_paramCount = NOT_INITIALIZED;

   /**
    * A map of alias String objects as key and name String objects as value.
    * Initialized if <code>processResultDocument</code>, never <code>null</code>
    * after that, might be empty.
    */
   private Map m_aliasMap = null;

   /**
    * All constants following define elements, attributes or attribute values
    * from the sys_DatabasePublisher.dtd.
    */
   private static final String DATAPUBLISHER_ELEM = "datapublisher";
   private static final String TABLEDEFSET_ELEM = "tabledefset";
   private static final String TABLEDATASET_ELEM = "tabledataset";
   private static final String TABLE_ELEM = "table";
   private static final String ROW_ELEM = "row";
   private static final String COLUMN_ELEM = "column";
   private static final String CHILDTABLE_ELEM = "childtable";
   private static final String DATABASE_ELEM = "database";
   private static final String CHILDREN_ELEM = "children";
   private static final String ALIAS_ELEM = "alias";

   private static final String DBNAME_ATTR = "dbname";
   private static final String DRIVERTYPE_ATTR = "drivertype";
   private static final String RESOURCENAME_ATTR = "resourceName";
   private static final String ORIGIN_ATTR = "origin";
   private static final String NAME_ATTR = "name";
   private static final String FOR_ATTR = "for";
   private static final String ACTION_ATTR = "action";
   private static final String LOOKUP_ATTR = "lookup";

   private static final String INSERT_ACTION = "n";
   private static final String UPDATE_ACTION = "u";
   private static final String DELETE_ACTION = "d";
   private static final String REPLACE_ACTION = "r";
}

