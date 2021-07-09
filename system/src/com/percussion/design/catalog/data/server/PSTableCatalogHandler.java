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

package com.percussion.design.catalog.data.server;

import com.percussion.design.catalog.IPSCatalogErrors;
import com.percussion.design.catalog.IPSCatalogRequestHandler;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.server.PSRequest;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.jdbc.IPSConnectionInfo;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionHelper;
import com.percussion.utils.jdbc.PSConnectionInfo;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * The PSTableCatalogHandler class implements cataloging of 
 * tables. This request type is used to locate the tables in the
 * specified back-end database.
 * <p>
 * The request format is defined in the
 * {@link com.percussion.design.catalog.data.PSTableCatalogHandler
 *  com.percussion.design.catalog.data.PSTableCatalogHandler} class.
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSTableCatalogHandler
   extends com.percussion.design.catalog.PSCatalogRequestHandler
   implements IPSCatalogRequestHandler
{
   /**
    * Constructs an instance of this handler.
    */
   public PSTableCatalogHandler()
   {
      super();
   }
   
   
   /* ********  IPSCatalogRequestHandler Interface Implementation ******** */
   
   /**
    * Get the request type(s) (XML document types) supported by this
    * handler.
    * 
    * @return       the supported request type(s)
    */
   public String[] getSupportedRequestTypes()
   {
      return new String[] { "PSXTableCatalog" };
   }
   
   
   /* ************ IPSRequestHandler Interface Implementation ************ */
   
   /**
    * Process the catalog request. This uses the XML document sent as the
    * input data. The results are written to the specified output
    * stream using the appropriate XML document format.
    * 
    * @param   request    the request object containing all context
    *                  data associated with the request
    */
   public void processRequest(PSRequest request)
   {
      Document doc = request.getInputDocument();
      Element root = null;
      if ((doc == null) || ((root = doc.getDocumentElement()) == null))
      {
         Object[] args =
         {ms_RequestCategory, ms_RequestType, ms_RequestDTD};
         createErrorResponse(request, new PSIllegalArgumentException(
            IPSCatalogErrors.REQ_DOC_MISSING, args));
         return;
      }

      /* verify this is the appropriate request type */
      if (!ms_RequestDTD.equals(root.getTagName()))
      {
         Object[] args = {ms_RequestDTD, root.getTagName()};
         createErrorResponse(request, new PSIllegalArgumentException(
            IPSCatalogErrors.REQ_DOC_INVALID_TYPE, args));
         return;
      }

      PSXmlTreeWalker tree = new PSXmlTreeWalker(doc);

      String datasource = tree.getElementData("datasource");
      java.lang.String filter = tree.getElementData("filter");

      /* parse the tableType string and build an array from it */
      Vector<String> vTypes = new Vector<String>();
      while (tree.getNextElement("tableType", true, false) != null)
      {
         vTypes.addElement(tree.getElementData((Element) tree.getCurrent()));
      }
      String[] tableTypes = null;
      if (vTypes.size() != 0)
      {
         tableTypes = new String[vTypes.size()];
         vTypes.copyInto(tableTypes);
      }

      Document retDoc = PSXmlDocumentBuilder.createXmlDocument();

      root = PSXmlDocumentBuilder.createRoot(retDoc, "PSXTableCatalogResults");

      PSXmlDocumentBuilder.addElement(retDoc, root, "datasource",
         datasource == null ? "" : datasource);

      if (filter != null)
         PSXmlDocumentBuilder.addElement(retDoc, root, "filter", filter);

      if (tableTypes != null)
      {
         for (int i = 0; i < tableTypes.length; i++)
            PSXmlDocumentBuilder.addElement(retDoc, root, "tableType",
               tableTypes[i]);
      }

      Element tableNode;
      String retName, retType;

      String searchTable = "%";
      if ((filter != null) && (filter.length() != 0))
         searchTable = filter;

      Connection conn;
      PSConnectionDetail detail;
      IPSConnectionInfo connInfo = new PSConnectionInfo(datasource);
      try
      {
         conn = PSConnectionHelper.getDbConnection(connInfo);
         detail = PSConnectionHelper.getConnectionDetail(connInfo);
      }
      catch (Exception e)
      {
         createErrorResponse(request, e);
         return;
      }

      try
      {
         DatabaseMetaData meta = conn.getMetaData();
         ResultSet rs = meta.getTables(detail.getDatabase(), detail.getOrigin(), 
            searchTable, tableTypes);
         if (rs != null)
         {
            while (rs.next())
            {
               retName = rs.getString(COLNO_TABLE_NAME);
               if (skipTable(retName, detail))
               {
                  continue;
               }
               retType = rs.getString(COLNO_TABLE_TYPE);

               tableNode = PSXmlDocumentBuilder.addEmptyElement(retDoc, root,
                  "Table");
               tableNode.setAttribute("type", retType);

               PSXmlDocumentBuilder.addElement(retDoc, tableNode, "name",
                  retName);
            }

            rs.close();
         }

         /* send the result to the caller */
         sendXmlData(request, retDoc);
      }
      catch (SQLException e)
      {
         // if this feature is not supported, we really can't use the driver
         // in this case, we will always treat it as an error
         createErrorResponse(request, e);
      }
      finally
      {
         try
         {
            conn.close();
         }
         catch (SQLException e)
         {
         }
      }
   }

   /**
    * Indicates whether this DB object should be skipped from the results
    * returned by the handler.
    * @param name the db object name.
    * @param detail connection detail.
    */
   private boolean skipTable(String name, PSConnectionDetail detail)
   {
      if (PSSqlHelper.isOracle(detail.getDriver()))
      {
         return isOracleRecycleBinObject(name);
      }
      return false;
   }

   /**
    * <p>Returns <code>true</code> if the object name indicates that this is an
    * Oracle recycle bin object.
    * Oracle documentation defines these names as following:</p>
    * <pre>BIN$<em>globalUID</em>$<em>version
    * </pre>
    * <p>where:</p>
    * <ul>
    * <li><em><code>globalUID</code></em> is a globally unique, 24 character
    * long identifier generated for the object.</li>
    * <li><em><code>version</code></em> is a version number assigned by the database</li>
    * </ul>
    * <p>The recycle bin name of an object is always 30 characters long.</p>
    * <p>Note that the <em><code>globalUID</code></em> used in the recycle bin
    * name is not readily correlated with any externally visible piece of
    * information about the object or the database.
    * </p>
    */
   boolean isOracleRecycleBinObject(String name)
   {
      return name.length() == 30
            && name.startsWith("BIN$")
            && name.charAt(28) == '$';
   }


   /**
    * Shutdown the request handler, freeing any associated resources.
    */
   public void shutdown()
   {
      /* nothing to do here */
   }

   private static final String   ms_RequestCategory   = "data";
   private static final String   ms_RequestType         = "Table";
   private static final String   ms_RequestDTD         = "PSXTableCatalog";

   private static final int COLNO_TABLE_NAME      = 3;
   private static final int COLNO_TABLE_TYPE      = 4;
}
