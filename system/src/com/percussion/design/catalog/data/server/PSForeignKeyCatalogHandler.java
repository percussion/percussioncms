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

import com.percussion.data.PSSqlException;
import com.percussion.design.catalog.IPSCatalogErrors;
import com.percussion.design.catalog.IPSCatalogRequestHandler;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.server.PSRequest;
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * The PSForeignKeyCatalogHandler class implements cataloging of
 * foreign keys. This request type is used to locate the columns which
 * are related to other tables. Foreign key columns usually refer to the
 * primary key of another table. This allows a unique relationship to be
 * defined between the two tables. When a foreign key is defined, values
 * cannot be inserted which do not exist in the table being referenced.
 * <p>
 * The request format is defined in the
 * {@link com.percussion.design.catalog.data.PSForeignKeyCatalogHandler
 *  com.percussion.design.catalog.data.PSForeignKeyCatalogHandler} class.
 *
 */
public class PSForeignKeyCatalogHandler
   extends com.percussion.design.catalog.PSCatalogRequestHandler
   implements IPSCatalogRequestHandler
{
   /**
    * Constructs an instance of this handler.
    */
   public PSForeignKeyCatalogHandler()
   {
      super();
   }


   /* ********  IPSCatalogRequestHandler Interface Implementation ******** */

   /**
    * Get the request type(s) (XML document types) supported by this
    * handler.
    *
    * @return      the supported request type(s)
    */
   public String[] getSupportedRequestTypes()
   {
      return new String[] { "PSXForeignKeyCatalog" };
   }


   /* ************ IPSRequestHandler Interface Implementation ************ */

   /**
    * Process the catalog request. This uses the XML document sent as the input
    * data. The results are written to the specified output stream using the
    * appropriate XML document format.
    * 
    * @param request the request object containing all context data associated
    * with the request
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
         Object[] args =
         {ms_RequestDTD, root.getTagName()};
         createErrorResponse(request, new PSIllegalArgumentException(
            IPSCatalogErrors.REQ_DOC_INVALID_TYPE, args));
         return;
      }

      PSXmlTreeWalker tree = new PSXmlTreeWalker(doc);

      String datasource = tree.getElementData("datasource");
      String table = tree.getElementData("tableName");

      Document retDoc = PSXmlDocumentBuilder.createXmlDocument();

      root = PSXmlDocumentBuilder.createRoot(retDoc,
         "PSXForeignKeyCatalogResults");

      PSXmlDocumentBuilder.addElement(retDoc, root, "datasource",
         datasource == null ? "" : datasource);

      if (table != null)
         PSXmlDocumentBuilder.addElement(retDoc, root, "tableName", table);

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

      Element colNode;
      String impTable, impCol, locCol;
      try
      {
         DatabaseMetaData meta = conn.getMetaData();
         ResultSet rs = meta.getImportedKeys(detail.getDatabase(), 
            detail.getOrigin(), table);
         if (rs != null)
         {
            while (rs.next())
            {
               /* THESE MUST BE READ IN THE CORRECT SEQUENCE !!! */
               impTable = rs.getString(COLNO_IMP_TABLE_NAME);
               impCol = rs.getString(COLNO_IMP_COL_NAME);
               locCol = rs.getString(COLNO_LOCAL_COL_NAME);

               colNode = PSXmlDocumentBuilder.addEmptyElement(retDoc, root,
                  "ForeignKey");

               PSXmlDocumentBuilder.addElement(retDoc, colNode, "name", locCol);

               colNode = PSXmlDocumentBuilder.addEmptyElement(retDoc, colNode,
                  "ExternalColumn");

               PSXmlDocumentBuilder.addElement(retDoc, colNode, "tableName",
                  impTable);

               PSXmlDocumentBuilder.addElement(retDoc, colNode, "columnName",
                  impCol);
            }

            rs.close();
         }
      }
      catch (SQLException e)
      {
         // if this feature is not supported, just return the empty doc
         // otherwise, treat this as a real error
         if (!PSSqlException.hasFeatureNotSupported(e, connInfo))
         {
            createErrorResponse(request, e);
            return;
         }
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

      /* send the result to the caller */
      sendXmlData(request, retDoc);
   }

   /**
    * Shutdown the request handler, freeing any associated resources.
    */
   public void shutdown()
   {
      /* nothing to do here */
   }


   private static final String   ms_RequestCategory   = "data";
   private static final String   ms_RequestType         = "ForeignKey";
   private static final String   ms_RequestDTD         = "PSXForeignKeyCatalog";

   private static final int COLNO_IMP_TABLE_NAME   = 3;
   private static final int COLNO_IMP_COL_NAME      = 4;
   private static final int COLNO_LOCAL_COL_NAME   = 8;
}

