/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.design.catalog.data.server;

import com.percussion.error.PSSqlException;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * The PSUniqueKeyCatalogHandler class implements cataloging of
 * unique keys. This request type is used to locate the column
 * combinations which can be used to uniquely identify rows in
 * the specified back-end table.
 * <p>
 * The request format is defined in the
 * {@link com.percussion.design.catalog.data.PSUniqueKeyCatalogHandler
 *  com.percussion.design.catalog.data.PSUniqueKeyCatalogHandler} class.
 */
public class PSUniqueKeyCatalogHandler
   extends com.percussion.design.catalog.PSCatalogRequestHandler
   implements IPSCatalogRequestHandler
{
   /**
    * Constructs an instance of this handler.
    */
   public PSUniqueKeyCatalogHandler()
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
      return new String[] { "PSXUniqueKeyCatalog" };
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
         Object[] args = {ms_RequestCategory, ms_RequestType, ms_RequestDTD};
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
      String table = tree.getElementData("tableName");

      Document retDoc = PSXmlDocumentBuilder.createXmlDocument();

      root = PSXmlDocumentBuilder.createRoot(retDoc,
         "PSXUniqueKeyCatalogResults");

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

      try
      {
         DatabaseMetaData meta;
         Element keyNode = null;
         ResultSet rs;

         /*
          * some drivers, like MS Access, support index lookups but not primary
          * key lookups. To allow unique keys to be located even when this is
          * the case, we've broken the code up into multiple try blocks which
          * check if the SQLException which is thrown is due to the feature not
          * being supported. This fixes bug id GBOD-4BSHCY
          */

         try
         {
            meta = conn.getMetaData();
         }
         catch (java.sql.SQLException e)
         {
            // any SQL exception is serious here
            createErrorResponse(request, e);
            return;
         }

         try
         {
            boolean bFirstRow = true;
            rs = meta.getPrimaryKeys(detail.getDatabase(), detail.getOrigin(), 
               table);
            if (rs != null)
            {
               while (rs.next())
               {
                  if (bFirstRow)
                  {
                     keyNode = PSXmlDocumentBuilder.addEmptyElement(retDoc,
                        root, "UniqueKey");
                     keyNode.setAttribute("type", "primaryKey");
                     bFirstRow = false;
                  }

                  PSXmlDocumentBuilder.addElement(retDoc, keyNode, "name", rs
                     .getString(COLNO_PKEY_COL_NAME));
               }
               rs.close();
            }
         }
         catch (SQLException e)
         {
            // unless this feature is not supported, treat this as an error
            if (!PSSqlException.hasFeatureNotSupported(e, connInfo))
            {
               createErrorResponse(request, e);
               return;
            }
         }

         try
         {
            String indexName, colName;
            String lastIndex = "noSuchIndexNamePossible";

            rs = meta.getIndexInfo(detail.getDatabase(), detail.getOrigin(), 
               table, true, true);
            if (rs != null)
            {
               while (rs.next())
               {
                  /* THESE MUST BE READ IN THE CORRECT SEQUENCE !!! */
                  indexName = rs.getString(COLNO_INDEX_NAME);
                  if (rs.getShort(COLNO_INDEX_TYPE) == DatabaseMetaData.tableIndexStatistic)
                     continue; /* not a real index, move on */
                  colName = rs.getString(COLNO_INDEX_COL_NAME);

                  if (!lastIndex.equals(indexName))
                  {
                     keyNode = PSXmlDocumentBuilder.addEmptyElement(retDoc,
                        root, "UniqueKey");
                     keyNode.setAttribute("type", "index");
                     lastIndex = indexName;
                  }

                  PSXmlDocumentBuilder.addElement(retDoc, keyNode, "name",
                     colName);
               }
               rs.close();
            }
         }
         catch (java.sql.SQLException e)
         {
            // unless this feature is not supported, treat this as an error
            if (!PSSqlException.hasFeatureNotSupported(e, connInfo))
            {
               createErrorResponse(request, e);
               return;
            }
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
   private static final String   ms_RequestType         = "UniqueKey";
   private static final String   ms_RequestDTD         = "PSXUniqueKeyCatalog";

   private static final int COLNO_PKEY_COL_NAME      = 4;

   private static final int COLNO_INDEX_NAME         = 6;
   private static final int COLNO_INDEX_TYPE         = 7;
   private static final int COLNO_INDEX_COL_NAME   = 9;
}

