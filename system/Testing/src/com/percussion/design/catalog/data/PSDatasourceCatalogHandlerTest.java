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

import com.percussion.conn.PSDesignerConnection;
import com.percussion.design.catalog.PSCataloger;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.design.objectstore.PSObjectStoreTest;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Test class for the {@link PSDatasourceCatalogHandler} class.
 */
@Category(IntegrationTest.class)
public class PSDatasourceCatalogHandlerTest extends PSObjectStoreTest
{
   // see base class
   public PSDatasourceCatalogHandlerTest(String name)
   {
      super(name);
   }
   
   /**
    * Tests cataloging datasources
    * 
    * @throws Exception
    */
   @Test
   public void testCatalog() throws Exception
   {
      PSDesignerConnection conn = makeConnection();
      PSObjectStore os = new PSObjectStore(conn);
      PSCataloger cataloger = new PSCataloger(conn);
      Properties req = new Properties();
      req.setProperty("RequestCategory", "data");
      req.setProperty("RequestType", "Datasource");
      
      Document result = cataloger.catalog(req);
      Element root = result.getDocumentElement();
      assertNotNull(root);
      

      assertEquals(root.getNodeName(), "PSXDatasourceCatalogResults");
      PSXmlTreeWalker tree = new PSXmlTreeWalker(root);
      Element dsEl = tree.getNextElement("datasource", 
         tree.GET_NEXT_ALLOW_CHILDREN);
      assertNotNull(dsEl);
      
      boolean foundRepository = false;
      while (dsEl != null)
      {
         String dsName = tree.getElementData("name", false);
         boolean isRepository = "yes".equals(dsEl.getAttribute("isRepository"));
         if (!foundRepository)
            foundRepository = isRepository;
         assertTrue(!StringUtils.isBlank(dsName));
         PSConnectionDetail detail = os.getConnectionDetail(dsName);
         assertEquals(detail.getDatasourceName(), tree.getElementData(
            "jndiDatasource", false));
         assertEquals(detail.getJdbcUrl(), tree.getElementData("jdbcUrl", 
            false));
         assertEquals(detail.getDatabase(), tree.getElementData("database", 
            false));
         assertEquals(detail.getOrigin(), tree.getElementData("origin", 
            false));
         
         if (isRepository)
            assertEquals(detail, os.getConnectionDetail(null));
         
         tree.setCurrent(dsEl);
         dsEl = tree.getNextElement("datasource", tree.GET_NEXT_ALLOW_SIBLINGS);
      }
   }

}

