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
package com.percussion.utils.tomcat;

import com.percussion.utils.container.IPSConnector;
import com.percussion.utils.container.PSAbstractConnector;
import com.percussion.utils.tools.PSBaseXmlConfigTest;
import com.percussion.utils.xml.PSInvalidXmlException;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.percussion.util.PSResourceUtils.getResourcePath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test case for the {@link PSTomcatUtils} class.
 */
public class PSTomcatUtilsTest extends PSBaseXmlConfigTest
{
   /**
    * Constant for test tomcat server file
    */
   public static final String TEST_TOMCAT_SERVER_FILE =
           getResourcePath(PSTomcatUtilsTest.class,
      "/com/percussion/utils/container/AppServer/server/rx/deploy/jboss-web.deployer/server.xml");

   /**
    * Tests loading and saving tomcat connectors.
    * 
    * @throws Exception if the test fails.
    */
@Test  
public void testLoadHttpConnectors() throws Exception
   {
      // make copy of the configs
      File srcServerDoc = getTempXmlFile();
      File tgtServerDoc = getTempXmlFile();

      // copy source to that file
      File srcFile = new File(TEST_TOMCAT_SERVER_FILE);
      copyXmlFile(srcFile, srcServerDoc);
      
      // try round-trip
      List<IPSConnector> connectors = PSTomcatUtils.loadHttpConnectors(
         srcServerDoc);
      assertEquals(1,connectors.size());


   }

   @Test
   @Ignore("SKIPPED: TODO: testSaveHttpConnectors - Fix Me. This test fails.")
   public void testSaveHttpConnectors() throws Exception {
      // make copy of the configs
      File srcServerDoc = getTempXmlFile();
      File tgtServerDoc = getTempXmlFile();

      // copy source to that file
      File srcFile = new File(TEST_TOMCAT_SERVER_FILE);
      copyXmlFile(srcFile, srcServerDoc);

      // try round-trip
      List<IPSConnector> connectors = PSTomcatUtils.loadHttpConnectors(
              srcServerDoc);
      PSTomcatUtils.saveHttpConnectors(srcServerDoc, connectors);
      List<IPSConnector> connectors1 = PSTomcatUtils.loadHttpConnectors(
              srcServerDoc);
      assertEquals(connectors, connectors1);
      copyXmlFile(srcServerDoc, tgtServerDoc);
      PSTomcatUtils.saveHttpConnectors(tgtServerDoc, connectors1);
      compareXmlDocs(srcServerDoc, tgtServerDoc);

      Path rootPath = Paths.get(".");
      // add a connector
      PSAbstractConnector conn = PSTomcatConnector.getBuilder().setPort(9992).build();
      connectors1.add(conn);
      PSTomcatUtils.saveHttpConnectors(tgtServerDoc, connectors1);
      List<IPSConnector> connectors2 = PSTomcatUtils.loadHttpConnectors(
              tgtServerDoc);
      assertEquals(connectors1, connectors2);
      assertTrue(connectors2.contains(conn));

      // remove a connector
      connectors2.remove(conn);
      PSTomcatUtils.saveHttpConnectors(tgtServerDoc, connectors2);
      connectors2 = PSTomcatUtils.loadHttpConnectors(
              tgtServerDoc);
      assertEquals(connectors, connectors2);
      assertTrue(connectors1.size() - 1 == connectors2.size());

      // check against original doc
      compareXmlDocs(srcServerDoc, tgtServerDoc);
   }

   @Override
   protected String getFilePrefix()
   {
      return "server-";
   }

}

