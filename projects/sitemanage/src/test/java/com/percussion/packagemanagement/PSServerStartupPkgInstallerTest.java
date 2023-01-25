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
