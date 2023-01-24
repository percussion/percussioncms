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

