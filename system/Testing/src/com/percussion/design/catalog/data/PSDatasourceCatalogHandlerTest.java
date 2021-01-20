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

