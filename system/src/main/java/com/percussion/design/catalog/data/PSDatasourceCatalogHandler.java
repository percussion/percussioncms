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

package com.percussion.design.catalog.data;

import com.percussion.design.catalog.IPSCatalogHandler;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Properties;

/**
 * This class provides for the cataloging of active server datasources. 
 * Datasource catalog requests are sent to the server using the 
 * PSXDatasourceCatalog XML document. Its definition is as follows:
 * 
 * <pre>
 * <code>
 *  
 *    &lt;!ELEMENT PSXDatasourceCatalog EMPTY&gt;
 *    &lt;!ATTLIST PSXDatasourceCatalog
 *        includeDbPubOnlySources CDATA #REQUIRED
 *    &gt;
 *
 *  
 * </code>
 * </pre>
 * 
 * The PSXDataspirceCatalogResults XML document is sent as the response. Its
 * definition is as follows:
 * 
 * <pre>
 * <code>
 * 
 *    &lt;!ELEMENT PSXDatasourceCatalogResults (datasource+)&gt;
 *  
 *    &lt;!--
 *       Each active datasource is returned as a datasource element.  The
 *       isRepository element will be &quot;yes&quot; for exactly one of the 
 *       returned datasources, indicating this datasource represents the CMS 
 *       Repository. 
 *    --&gt;
 *    &lt;!ELEMENT datasource (name, jndiDatasource, jdbcUrl, database, 
 *       origin, isRepository)&gt;
 *  
 *    &lt;!--
 *       The name by which the datasource should be referenced. 
 *     --&gt;
 *    &lt;!ELEMENT name (#PCDATA)&gt;
 *    
 *    &lt;!--
 *       The name of the JNDI datsource used by this datasource. 
 *     --&gt;
 *    &lt;!ELEMENT jndiDatasource (#PCDATA)&gt;
 *    
 *    &lt;!--
 *       The jdbcUrl used by this datasource. 
 *     --&gt;
 *    &lt;!ELEMENT jdbcUrl (#PCDATA)&gt;
 *    
 *    &lt;!--
 *       The database used by this datasource, if any. 
 *     --&gt;
 *    &lt;!ELEMENT database (#PCDATA)&gt;
 *    
 *    &lt;!--
 *       The origin or schema used by this datasource. 
 *     --&gt;
 *    &lt;!ELEMENT origin (#PCDATA)&gt;
 *    
 *    &lt;!--
 *       Determine if this is the repository datasource.   
 *     --&gt;
 *    &lt;!ELEMENT isRepository (#PCDATA)&gt;
 *    
 *    &lt;!--
 *       JDBC Driver.   
 *     --&gt;
 *    &lt;!ELEMENT driver (#PCDATA)&gt;
 *    
 *    &lt;!--
 *       Indicates that this source is only used for DB publishing.
 *       Value: yes or no.   
 *     --&gt;
 *    &lt;!ELEMENT isDbPubOnly (#PCDATA)&gt;
 *  
 * </code>
 * </pre>
 */
public class PSDatasourceCatalogHandler implements IPSCatalogHandler
{
   /**
    * Default ctor
    */
   public PSDatasourceCatalogHandler()
   {

   }

   /**
    * Format the catalog request based upon the specified request
    * information. The request information for this request type is:
    * <table border="2">
    *   <tr><th>Key</th>
    *       <th>Value</th>
    *       <th>Required</th></tr>
    *   <tr><td>RequestCategory</td>
    *       <td>data</td>
    *       <td>yes</td></tr>
    *   <tr><td>RequestType</td>
    *       <td>Datasource</td>
    *       <td>yes</td></tr>
    *   <tr><td>IncludeDbPubSources</td>
    *       <td>true or false</td>
    *       <td>no</td></tr>    
    * </table>
    * 
    * @param req The request information, may not be <code>null</code> and must
    * contain the expected request properties.
    * 
    * @return The formatted document, never <code>null</code>.
    */
   public Document formatRequest(Properties req)
   {
      String sTemp = (String)req.get("RequestCategory");
      if ( (sTemp == null) || !"data".equalsIgnoreCase(sTemp) ) {
         throw new IllegalArgumentException("req category invalid");
      }

      sTemp = (String)req.get("RequestType");
      if ( (sTemp == null) || !"Datasource".equalsIgnoreCase(sTemp) ) {
         throw new IllegalArgumentException("req type invalid");
      }
      
      sTemp = (String)req.get("IncludeDbPubSources");
      String includeDbPubSources = "true";
      if((sTemp == null) || !sTemp.equalsIgnoreCase("true"))
      {
         includeDbPubSources = "false";
      }
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = 
         PSXmlDocumentBuilder.createRoot(doc, "PSXDatasourceCatalog");
      root.setAttribute("includeDbPubOnlySources", includeDbPubSources);
      return doc;
   }

   
   
}
