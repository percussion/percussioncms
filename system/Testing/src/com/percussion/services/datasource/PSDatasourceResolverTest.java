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
package com.percussion.services.datasource;

import com.percussion.utils.jdbc.IPSDatasourceConfig;
import com.percussion.utils.jdbc.PSDatasourceConfig;
import com.percussion.utils.jdbc.PSDatasourceResolver;
import com.percussion.utils.tools.PSTestUtils;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * Test class for the {@link PSDatasourceResolver}
 */
public class PSDatasourceResolverTest extends TestCase
{
   /**
    * Tests setting and getting values
    * 
    * @throws Exception if the test fails
    */
   public void testSetters() throws Exception
   {
      PSDatasourceResolver dsr = new PSDatasourceResolver();
      String dsName = "ds";
      PSDatasourceConfig cfg = new PSDatasourceConfig("ds", "jndids", "origin", 
         "db");
      PSDatasourceConfig cfg2 = new PSDatasourceConfig("ds2", "jndids2", 
         "origin2", "db2");
      List<IPSDatasourceConfig> dsList = new ArrayList<IPSDatasourceConfig>();
      
      PSTestUtils.testSetter(dsr, "RepositoryDatasource", dsName + "test", 
         false);
      PSTestUtils.testSetter(dsr, "RepositoryDatasource", null, 
         true);
      PSTestUtils.testSetter(dsr, "RepositoryDatasource", null, 
         true);

      
      PSTestUtils.testSetter(dsr, "DatasourceConfigurations", null, 
         List.class, true);
      
      PSTestUtils.testSetter(dsr, "DatasourceConfigurations", dsList, 
         List.class, true);
      
      dsList.add(cfg);
      PSTestUtils.testSetter(dsr, "DatasourceConfigurations", dsList, 
         List.class, false);
      
      dsList.add(cfg2);
      PSTestUtils.testSetter(dsr, "DatasourceConfigurations", dsList, 
         List.class, false);

   }
   
   /**
    * Test instantiating and serializing from xml
    * 
    * @throws Exception if the test fails
    */
   public void testXml() throws Exception
   {
      PSDatasourceResolver dsr = new PSDatasourceResolver();
      PSDatasourceConfig cfg = new PSDatasourceConfig("ds", "jndids", "origin", 
         "db");
      PSDatasourceConfig cfg2 = new PSDatasourceConfig("ds2", "jndids2",
         "origin2", "db2");
      List<IPSDatasourceConfig> dsList = new ArrayList<>();
      dsList.add(cfg);
      dsList.add(cfg2);
      dsr.setDatasourceConfigurations(dsList);
      dsr.setRepositoryDatasource(cfg.getName());
      
      PSDatasourceResolver dsr2 = new PSDatasourceResolver();
      dsr2.fromXml(dsr.toXml(PSXmlDocumentBuilder.createXmlDocument()));
      
      assertEquals(dsr.getRepositoryDatasource(), 
         dsr2.getRepositoryDatasource());
      assertEquals(dsr.getDatasourceConfigurations(), 
         dsr2.getDatasourceConfigurations());
   }
}

