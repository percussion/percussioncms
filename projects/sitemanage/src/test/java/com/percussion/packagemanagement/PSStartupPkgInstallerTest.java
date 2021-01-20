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
package com.percussion.packagemanagement;

import static org.junit.Assert.*;

import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.server.IPSPackageInstaller;
import com.percussion.rx.services.deployer.IPSPackageUninstaller;
import com.percussion.maintenance.service.IPSMaintenanceManager;
import com.percussion.maintenance.service.impl.PSMaintenanceManager;
import com.percussion.packagemanagement.PSPackageFileEntry.PackageFileStatus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.percussion.rx.services.deployer.PSUninstallMessage;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author JaySeletz
 *
 */
@Category(IntegrationTest.class)
public class PSStartupPkgInstallerTest
{

    private static final String TEMP_FILE_PREFIX = "perc.testPackageFileList";

    /**
     * @author JaySeletz
     *
     */
    private final class MockPackageInstaller implements IPSPackageInstaller
    {
        int count = 0;
        int errorIndex = -1;

        /**
         * @param i
         */
        public MockPackageInstaller(int i)
        {
            errorIndex = i;
        }

        @Override
        public void installPackage(File packageFile)
        {
            count++;
            if (count == errorIndex)
                throw new RuntimeException("Failing 3rd package for test");
        }
        
		@Override
		public void installPackage(File packageFile,
				boolean shouldValidateVersion) throws PSDeployException {
            count++;
            if (count == errorIndex)
                throw new RuntimeException("Failing 3rd package for test");
			
		}

        public int getCount()
        {
            return count;
        }
        
    }

    private final class MockPackageUninstaller implements IPSPackageUninstaller{

        @Override
        public List<PSUninstallMessage> uninstallPackages(String packageNames)
        {
            return Collections.emptyList();
        }

        @Override
        public List<PSUninstallMessage> uninstallPackages(String packageNames,
                boolean isRevertEntry) {
            return Collections.emptyList();
        }
    }




    private static final String PKG_FILE_LIST_XML = "TestStartupPkgInstaller.xml";
    private static final String PKG_FILE_UNINSTALLER_LIST_XML = "TestStartupPkgUninstaller.xml";

    @Test
    public void test() throws Exception
    {
        PSStartupPkgInstaller pkgInstaller = new PSStartupPkgInstaller();
        IPSMaintenanceManager maintMgr = new PSMaintenanceManager();
        pkgInstaller.setMaintenanceManager(maintMgr);
        String packageFileListPath = createPackageFileList(TEMP_FILE_PREFIX, PKG_FILE_LIST_XML);
        PSPackageFileList pkgFileList = PSPackageFileList.fromXml(IOUtils.toString(this.getClass().getResourceAsStream(PKG_FILE_LIST_XML)));
        File packageDir = createTestPackageFiles(pkgFileList);

        pkgInstaller.setPackageDir(packageDir);
        pkgInstaller.setPackageFileListPath(packageFileListPath);
        pkgInstaller.setPackageInstaller(new MockPackageInstaller(3));

        pkgInstaller.installPackages();
        assertFalse(maintMgr.isWorkInProgress());
        assertTrue(maintMgr.hasFailures());

        // now check the results
        String xmlContents = IOUtils.toString(new FileInputStream(packageFileListPath));
        PSPackageFileList pkgFileListResults = PSPackageFileList.fromXml(xmlContents);
        List<PSPackageFileEntry> entries = pkgFileListResults.getEntries();
        assertNotNull(entries);
        assertEquals(4, entries.size());
        assertEquals(PackageFileStatus.INSTALLED, entries.get(0).getStatus());
        assertEquals(PackageFileStatus.INSTALLED, entries.get(1).getStatus());
        assertEquals(PackageFileStatus.FAILED, entries.get(2).getStatus());
        assertEquals(PackageFileStatus.PENDING, entries.get(3).getStatus());

        // test re-run with no failures
        maintMgr = new PSMaintenanceManager();
        pkgInstaller.setMaintenanceManager(maintMgr);
        MockPackageInstaller installer = new MockPackageInstaller(-1);
        pkgInstaller.setPackageInstaller(installer);
        pkgInstaller.installPackages();
        assertFalse(maintMgr.isWorkInProgress());
        assertFalse(maintMgr.hasFailures());
        assertEquals(2, installer.getCount());

        // test re-run with no work to do
        installer = new MockPackageInstaller(-1);
        pkgInstaller.setPackageInstaller(installer);
        pkgInstaller.installPackages();
        assertFalse(maintMgr.isWorkInProgress());
        assertFalse(maintMgr.hasFailures());
        assertEquals(0, installer.getCount());

        // test run w/bad xml
        installer = new MockPackageInstaller(-1);
        pkgInstaller.setPackageInstaller(installer);
        pkgInstaller.setPackageFileListPath(packageFileListPath + ".bad");
        pkgInstaller.installPackages();
        assertFalse(maintMgr.isWorkInProgress());
        assertTrue(maintMgr.hasFailures());
    }

    @Test
    public void testUninstall() throws Exception
    {
        PSStartupPkgInstaller pkgUninstaller = new PSStartupPkgInstaller();

        //set maintenance manager
        IPSMaintenanceManager maintMgr = new PSMaintenanceManager();
        pkgUninstaller.setMaintenanceManager(maintMgr);

        // Set our uninstaller to a mock.
        pkgUninstaller.setPackageUninstaller(new MockPackageUninstaller());

        //Obtain our packages
        String uninstallPackagesPath = createPackageFileList(TEMP_FILE_PREFIX, PKG_FILE_UNINSTALLER_LIST_XML);
        pkgUninstaller.setPackageFileListPath(uninstallPackagesPath);

        //Do uninstall process
        pkgUninstaller.uninstallPackages();

        assertFalse(maintMgr.isWorkInProgress());


        // now check the results
        String xmlContents = IOUtils.toString(new FileInputStream(uninstallPackagesPath));
        PSPackageFileList pkgFileListResults = PSPackageFileList.fromXml(xmlContents);

        List<PSPackageFileEntry> entries = pkgFileListResults.getEntries();
        assertNotNull(entries);
        assertEquals(2, entries.size());

        assertEquals(PackageFileStatus.PENDING, entries.get(0).getStatus());
        assertEquals(PackageFileStatus.PENDING, entries.get(1).getStatus());

        assertTrue(!maintMgr.isWorkInProgress());
    }

    private String createPackageFileList(String tempFile, String resourceList) throws IOException
    {
        File pkgFileList = File.createTempFile(tempFile, ".xml");
        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream(pkgFileList);
            IOUtils.copy(this.getClass().getResourceAsStream(resourceList), out);
            return pkgFileList.getPath();
        }
        finally
        {
            IOUtils.closeQuietly(out);
        }
    }

    private File createTestPackageFiles(PSPackageFileList pkgFileList) throws IOException
    {
        File dir = FileUtils.getTempDirectory();

        for (PSPackageFileEntry entry : pkgFileList.getEntries())
        {
            File file = new File(dir, entry.getPackageName() + ".ppkg");
            file.createNewFile();
        }

        return dir;
    }

}
