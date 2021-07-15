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
package com.percussion.packagemanagement;

import com.percussion.packagemanagement.PSPackageFileEntry.PackageFileStatus;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.test.PSServletTestCase;
import com.percussion.utils.request.PSRequestInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.experimental.categories.Category;

/**
 * @author JaySeletz
 *
 */
@Category(IntegrationTest.class)
public class PSServerStartupPkgInstallerTest extends PSServletTestCase
{
    private PSStartupPkgInstaller pkgInstaller;
    private static File pkgFile = new File("rxconfig/Installer/InstallPackages.xml");
    private static File bakFile = new File("rxconfig/Installer/InstallPackages.bak");
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        pkgInstaller = (PSStartupPkgInstaller) getBean("startupPackageInstaller");
        PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_USER, "Admin");
        PSSecurityFilter.authenticate(request, response, "Admin", "demo");
        
        preparePackageList();
    }
    
    
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        restorePackageList();
    }


    private void restorePackageList()
    {
        try
        {
            FileUtils.copyFile(bakFile, pkgFile);
        }
        catch (IOException e)
        {
            System.out.println("Failed to restore backup of " + pkgFile.getPath() + ": " + e.getLocalizedMessage());
        }
    }



    private void preparePackageList() throws IOException
    {
        PSPackageFileList fileList = backupCurrentList();
        boolean done = false;
        for (PSPackageFileEntry entry : fileList.getEntries())
        {
            if (!done)
            {
                entry.setStatus(PackageFileStatus.PENDING);
                done = true;
            }
            else
                entry.setStatus(PackageFileStatus.INSTALLED);
        }
        
        FileUtils.writeStringToFile(pkgFile, fileList.toXml(), StandardCharsets.UTF_8);
    }

    private PSPackageFileList backupCurrentList() throws IOException
    {
        FileUtils.copyFile(pkgFile, bakFile);
        return PSPackageFileList.fromXml(IOUtils.toString(new FileInputStream(pkgFile)));
    }



    public void testPkgInstall() throws Exception
    {
        pkgInstaller.installPackages();
        validatePackageList();
    }


    private void validatePackageList() throws FileNotFoundException, IOException
    {
        PSPackageFileList packageList = PSPackageFileList.fromXml(IOUtils.toString(new FileInputStream(pkgFile)));
        for (PSPackageFileEntry entry : packageList.getEntries())
        {
            assertEquals(PackageFileStatus.INSTALLED, entry.getStatus());
        }
    }
}
