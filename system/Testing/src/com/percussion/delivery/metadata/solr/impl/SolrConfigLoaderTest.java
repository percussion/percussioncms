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

package com.percussion.delivery.metadata.solr.impl;

import com.percussion.share.dao.PSSerializerUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;

import com.percussion.utils.testing.IntegrationTest;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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
      stream = new ByteArrayInputStream(testString.getBytes("UTF-8"));
      PSSolrConfig config2 = PSSerializerUtils.unmarshalWithValidation(stream, PSSolrConfig.class);
      assertEquals(false,config2.getSolrServer().get(0).isCleanAllOnFullPublish());

   }
}
