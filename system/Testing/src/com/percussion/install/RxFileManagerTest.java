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
package com.percussion.install;

import com.percussion.utils.container.PSJettyConnectorsTest;
import com.percussion.utils.testing.UnitTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author JaySeletz
 *
 */
@Category(UnitTest.class)
public class RxFileManagerTest
{

   @Rule
   public TemporaryFolder temporaryFolder = new TemporaryFolder();

   
   /**
    * Test method for {@link com.percussion.install.RxFileManager#isCM1Dir(java.lang.String)}.
    */
   @Test
   public void testIsCM1Dir() throws IOException {

      Path root = temporaryFolder.getRoot().toPath();

      InputStream srcInstallProps = PSJettyConnectorsTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/installation.properties");
      InputStream srcLoginConf = PSJettyConnectorsTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/login.conf");
      InputStream srcPercDsXML= PSJettyConnectorsTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/perc-ds.xml");
      InputStream srcPercDsProperties= PSJettyConnectorsTest.class.getResourceAsStream("/com/percussion/utils/container/jetty/base/etc/perc-ds-derby.properties");

      temporaryFolder.newFolder("jetty","base","etc");
      temporaryFolder.newFolder("ObjectStore");

      Files.copy(srcInstallProps,root.resolve("jetty/base/etc/installation.properties"));
      Files.copy(srcLoginConf,root.resolve("jetty/base/etc/login.conf"));
      Files.copy(srcPercDsXML,root.resolve("jetty/base/etc/perc-ds.xml"));
      Files.copy(srcPercDsProperties,root.resolve("jetty/base/etc/perc-ds.properties"));

      System.setProperty("rxdeploydir",root.toAbsolutePath().toString());

      assertTrue(RxFileManager.isCM1Dir(root.toAbsolutePath().toString()));
      assertFalse(RxFileManager.isCM1Dir(root.toAbsolutePath().toString() + "/garbagein"));
   }

}
