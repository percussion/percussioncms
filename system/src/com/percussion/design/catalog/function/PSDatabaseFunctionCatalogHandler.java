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

package com.percussion.design.catalog.function;

import com.percussion.conn.PSServerException;
import com.percussion.design.catalog.IPSCatalogHandler;
import com.percussion.design.catalog.PSCataloger;
import com.percussion.extension.PSDatabaseFunctionDef;
import com.percussion.extension.PSDatabaseFunctionManager;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements cataloging of database functions installed on the
 * server.
 * <p>
 * Database functions catalog requests are sent to the server
 * using the PSXDatabaseFunctionCatalog XML document. Its definition
 * is as follows:
 * <pre><code>
 *
 *  &lt;!ELEMENT PSXDatabaseFunctionCatalog (datasource?)&gt;
 *  &lt;!ELEMENT datasource (#PCDATA)&gt;
 *
 * <pre><code>
 *
 * The PSXDatabaseFunctionCatalogResults XML document is sent
 * as the response. Its definition is as follows:
 * <pre><code>
 *
 *  &lt;!ELEMENT PSXDatabaseFunctionCatalogResults (PSXDatabaseFunctionDef*)&gt;
 *
 * <pre><code>
 *
 * See "DatabaseFunctionDefs.dtd" for the DTD of the "PSXDatabaseFunctionDef"
 * element.
 */
public class PSDatabaseFunctionCatalogHandler implements IPSCatalogHandler
{
   /**
    * Format the catalog request based upon the specified
    * request information. The request information for this
    * request type is:
    *
    * <table border="1">
    * <tr>
    *      <th>Key</th>
    *      <th>Value</th>
    *      <th>Required</th>
    * </tr>
    * <tr>
    *      <td>RequestCategory</td>
    *      <td>function</td>
    *      <td>yes</td>
    * </tr>
    * <tr>
    *      <td>RequestType</td>
    *      <td>DatabaseFunction</td>
    *      <td>yes</td>
    * </tr>
    * 
    * <tr>
    *      <td>datasource</td>
    *      <td>The name of the datasource, may be ommited to use the 
    *      repository datasource</td>
    *      <td>no</td>
    * </tr>
    * </table>
    *
    * @param req contains request information, may not be <code>null</code>,
    * should contain the value of the following keys:
    * "RequestCategory", "RequestType" and "Datasource"
    *
    * @return an XML document containing the appropriate the database functions
    * serialized in XML format, never <code>null</code>
    */
   public Document formatRequest(Properties req)
   {
      String sTemp = (String) req.get(REQ_CATEGORY_KEY);
      if ((sTemp == null) || (!(REQ_CATEGORY_VALUE.equalsIgnoreCase(sTemp))))
      {
         throw new IllegalArgumentException(
            "req category invalid: null or REQ_CATEGORY_VALUE");
      }

      sTemp = (String) req.get(REQ_TYPE_KEY);
      if ((sTemp == null) || (!(REQ_TYPE_VALUE.equalsIgnoreCase(sTemp))))
      {
         throw new IllegalArgumentException(
            "req type invalid: null or REQ_TYPE_VALUE");
      }

      String datasource = (String) req.get(DATASOURCE_KEY);

      Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(reqDoc, NODE_NAME);
      if (datasource != null)
         PSXmlDocumentBuilder.addElement(reqDoc, root, EL_DATASOURCE, 
            datasource);

      return reqDoc;
   }


   /**
    * Returns the database functions installed on the server.
    * This is a convenience method which makes a call to the
    * <code>catalog</code> of the specified cataloger (<code>cataloger</code>)
    * using the appropriate properties for this request type.
    *
    * @param cataloger a cataloger containing a connection to the Rhythmyx
    * server which will be used for catalogging database functions, may not be
    * <code>null</code>
    *
    * @param datasource the datasource from which the database functions are to
    * be obtained. Database functions are database/driver specific. May be
    * be <code>null</code> or empty to use the repository.
    *
    * @return the database functions installed on the server,
    * never <code>null</code>, may be empty
    *
    * @throws PSServerException if the server is not responding.
    * @throws PSAuthenticationFailedException if the credentials specified
    *         for the server connection are invalid.
    * @throws PSAuthorizationException if the user does not have designer or
    *         administrator access to the server.
    * @throws IOException if a communication error occurs while processing
    *         the request
    */
   public static PSDatabaseFunctionDef[] getDatabaseFunctions(
      PSCataloger cataloger, String datasource)
      throws PSServerException,PSAuthenticationFailedException,
            PSAuthorizationException, IOException
   {
      if (cataloger == null)
         throw new IllegalArgumentException("cataloger may not be null");

      if (datasource == null)
         datasource = "";

      PSDatabaseFunctionDef[] dbFuncDefs = null;

      // create the properties
      Properties req = new Properties();

      req.put(REQ_CATEGORY_KEY, REQ_CATEGORY_VALUE);
      req.put(REQ_TYPE_KEY, REQ_TYPE_VALUE);
      req.put(DATASOURCE_KEY, datasource);

      // perform the catalog request
      Document doc = null;
      try
      {
         doc = cataloger.catalog(req);
      }
      catch (IllegalArgumentException e)
      {
         throw new PSServerException(e);
      }

      /* store the database functions in a list.
       * The returned XML tree contains the standard root node
       * (which we can ignore) then each database function
       * (where each database function is a child of the root, but siblings to
       * each other). To walk the tree we can get the root node
       * create a walker for it. We can get the first child to get
       * the first extension then iterate siblings to get
       * all subsequent extensions.
       */
      Element root = doc.getDocumentElement();
      if (root != null)
      {
         List list = new ArrayList();
         PSXmlTreeWalker walker = new PSXmlTreeWalker(doc);
         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
            PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
         int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
            PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         Element el = walker.getNextElement(
            PSDatabaseFunctionDef.getNodeName(), firstFlags);
         while (el != null)
         {
            try
            {
               list.add(new PSDatabaseFunctionDef(
                  PSDatabaseFunctionManager.FUNCTION_TYPE_SYSTEM, el, true));
               el = walker.getNextElement(
                  PSDatabaseFunctionDef.getNodeName(), nextFlags);
            }
            catch (Exception ex)
            {
               throw new PSServerException(ex);
            }
         }

         // and convert the list to an array
         final int size = list.size();
         dbFuncDefs = new PSDatabaseFunctionDef[size];
         list.toArray(dbFuncDefs);
      }
      else
      {
         // create an empty one
         dbFuncDefs = new PSDatabaseFunctionDef[0];
      }

      return dbFuncDefs;
   }

   /**
    * Constant for the "RequestCategory" key for the properties object
    * specified in the <code>formatRequest()</code> method
    */
   public static final String REQ_CATEGORY_KEY = "RequestCategory";

   /**
    * Constant for the value of the "RequestCategory" key for the properties
    * object specified in the <code>formatRequest()</code> method
    */
   public static final String REQ_CATEGORY_VALUE = "function";

   /**
    * Constant for the "RequestType" key for the properties object
    * specified in the <code>formatRequest()</code> method
    */
   public static final String REQ_TYPE_KEY = "RequestType";

   /**
    * Constant for the value of the "RequestType" key for the properties
    * object specified in the <code>formatRequest()</code> method
    */
   public static final String REQ_TYPE_VALUE = "DatabaseFunction";

   /**
    * Constant for the "driver" key for the properties object
    * specified in the <code>formatRequest()</code> method
    */
   public static final String DATASOURCE_KEY = "datasource";

   // Constants for XML element and attribute names
   public static final String NODE_NAME = "PSXDatabaseFunctionCatalog";
   public static final String EL_DATASOURCE = "Datasource";
}

