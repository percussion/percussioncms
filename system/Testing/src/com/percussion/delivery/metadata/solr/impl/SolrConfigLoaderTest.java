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

package com.percussion.delivery.metadata.solr.impl;

import com.percussion.share.dao.PSSerializerUtils;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class SolrConfigLoaderTest
{
   @Test
   public void testLoadConfig() throws Exception
   {    
      PSSolrConfig config = new PSSolrConfig();
      SolrServer server = new SolrServer();
      server.setSolrHost("url");
      server.setServerType("STAGING");
      server.addSiteEntry("sitename");
      server.addMetaMapEntry("key","value");
   
      config.setSolrServer(Collections.singletonList(server));
      
      String configString = SolrConfigLoader.toXml(config);
      System.out.println(configString);
      InputStream stream;
     
      String testString = "<SolrConfig><SolrServer><serverType>STAGING</serverType><solrHost>url</solrHost><cleanAllOnFullPublish>false</cleanAllOnFullPublish><metadataMap><entry value=\"value\" key=\"key\"/></metadataMap><enabledSites><site>sitename</site></enabledSites></SolrServer></SolrConfig>";
      stream = new ByteArrayInputStream(testString.getBytes(StandardCharsets.UTF_8));
      PSSolrConfig config2 = PSSerializerUtils.unmarshalWithValidation(stream, PSSolrConfig.class);
      assertEquals(false,config2.getSolrServer().get(0).isCleanAllOnFullPublish());

   }
}
