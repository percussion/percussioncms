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

package com.percussion.design.catalog;

import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;


/**
 * The PSCatalogResultsWalker class is used to simplify processing 
 * the XML results of a catalog request.
 * <p>
 * To use the PSCatalogResultsWalker, perform the catalog request and
 * construct a walker for the resulting XML document. The walker can then
 * be used to search for entries, retrieve a subset of the document, etc.
 * <p>
 * The mechanism for issuing catalog requests is described in the
 * {@link com.percussion.design.catalog com.percussion.catalog} package
 * description. By using the walker, manipulating the XML document directly
 * is not necessary. This greatly simplifies processing results, as XML
 * documents can contain fairly complex structures.
 * The following sample shows how to access back-end columns
 * for a particular table using the walker.
 * <pre><code>
 *    try {
 *       PSCatalogResultsWalker  cataloger   = new PSCataloger( "myserver",
 *                                                              "myid", "mypw");
 *       Properties              props       = new Properties();
 *       
 *       props.put("RequestCategory",  "data");
 *       props.put("RequestType",      "Column");
 *       props.put("DriverName",       "odbc");
 *       props.put("ServerName",       "MyServerDSN");
 *       props.put("TableName",        "mytab");
 *       
 *       Document                xmlDoc   = cataloger.catalog(props);
 *       PSCatalogResultsWalker  walker   = new PSCatalogResultsWalker(xmlDoc);
 *
 *       // Get data from the request info
 *       log.info("Table: {}" , walker.getRequestData("tableName"));
 *
 *       log.info("Column");
 *       log.info("------------------------------");
 *
 *       // now walk all the child objects (Column elements) to
 *       // get the column names
 *       while (walker.nextResultObject("Column")) {
 *          log.info(walker.getResultData("name"));
 *       }
 *    }
 *    catch (Exception e) {
 *       log.error(PSExceptionUtils.getMessageForLog(e));
 *    }
 *
 * </code></pre>
 * Which results in the following output:
 * <pre><code>
 *    Table: mytab
 *    Column
 *    ------------------------------
 *    mycol1
 *    mycol2
 *    mycol3
 * </code></pre>
 *
 * @see        IPSCatalogHandler
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSCatalogResultsWalker extends PSXmlTreeWalker {

   /**
    * Creates a walker for the specified catalog results. Walkers provide
    * a simplified way to traverse results, which can also be done
    * using the XML document.
    *
    * @param   results  the result document returned by a catalog request
    */
   public PSCatalogResultsWalker(Document results)
   {
      super(results);
   }

   /**
    * Get the value of a request field. The information sent with the
    * request is also stored with the result. The request info can be
    * retrieved using this method. Request fileds are catalog
    * request specific.
    *
    * @param   name              the name of the request field to retrieve
    *
    * @return                    the value of the request field
    */
   public java.lang.String getRequestData(java.lang.String name)
   {
        return getElementData(name, true);
   }

   /**
    * Position the walker on the next result object. Results often contain
    * multiple objects, one for each "result row". By traversing the result
    * objects, all the result data can be accessed. Result objects
    * are catalog request specific.
    *
    * @param   name              the name of the result object to retrieve
    *
    * @return                    <code>true</code> if the walker found the
    *                            next result object; <code>false</code> if
    *                            no other objects exist by that name
    */
   public boolean nextResultObject(java.lang.String name)
   {
        return (getNextElement(name, true, true) != null);
   }

   /**
    * Get the value of a result field. Result fileds are catalog
    * request specific.
    * <p>
    * Before accessing a result field, the walker must be positioned
    * on a result object. This is done by calling the
    * {@link #nextResultObject nextResultObject} method.
    *
    * @param   name              the name of the result field to retrieve
    *
    * @return                    the value of the result field
    */
   public java.lang.String getResultData(java.lang.String name)
   {
        return getElementData(name, false);
   }
}

