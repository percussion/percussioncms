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
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.testing.PSClientTestCase;

import com.percussion.utils.testing.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PSColumnCatalogHandlerTest extends PSClientTestCase
{

   public PSColumnCatalogHandlerTest(String name)
   {
      super(name);
   }

   @Test
   public void testOracle() throws Exception
   {
      java.util.Properties props = getConnectionProps(CONN_TYPE_RXSERVER);

      PSDesignerConnection conn = new PSDesignerConnection(props);
      PSCataloger cataloger = new PSCataloger(conn);

      PSBackEndTable tab = new PSBackEndTable("EMPL");
      tab.setDataSource(null);
      tab.setTable("ALL_TAB_COLUMNS");


      PSCatalogedColumn[] cols = PSColumnCatalogHandler.getCatalog(
         cataloger, tab,
         "SCOTT", cataloger.prepareCredentials("SCOTT", "TIGER"));
      for (int i = 0; i < cols.length; i++)
      {
         System.out.println("col " + i + ": " + cols[i]);
      }
   }

}

