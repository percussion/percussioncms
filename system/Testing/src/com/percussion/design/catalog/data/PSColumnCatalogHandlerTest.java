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

