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
package com.percussion.deployer.server;

import com.percussion.services.pkginfo.IPSPkgInfoService;
import com.percussion.services.pkginfo.PSPkgInfoServiceLocator;
import com.percussion.services.pkginfo.data.PSPkgInfo;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.apache.commons.lang.time.DateUtils;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

/**
 * @author JaySeletz
 *
 */
@Category(IntegrationTest.class)
public class PSLocalDeployerClientTest extends ServletTestCase
{
    
    private static final String PKG_DIR = "Packages/Percussion";
    private static final String TEST_PKG = "perc.Test";
    private static final String PKG_EXT = ".ppkg";
    
    public void test() throws Exception
    {
        PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_USER, "Admin");
        PSSecurityFilter.authenticate(request, response, "Admin", "demo");
        
        try
        {
            PSLocalDeployerClient client = new PSLocalDeployerClient();
            Date started = new Date();
            
            client.installPackage(new File(PKG_DIR, TEST_PKG + PKG_EXT));
            checkPackageIsInstalled(TEST_PKG, started);

        }
        catch (Exception e)
        {
            fail("Exception thrown: " + e.getLocalizedMessage());
        }
    }

    private void checkPackageIsInstalled(String pkgName, Date started) throws Exception
    {
        IPSPkgInfoService svc = PSPkgInfoServiceLocator.getPkgInfoService();
        PSPkgInfo info = svc.findPkgInfo(pkgName);
        assertNotNull("Package info not found: " + pkgName, info);
        assertTrue("Package not successfully installed: " + pkgName, info.isSuccessfullyInstalled());
        Date startDate = DateUtils.truncate(started, Calendar.SECOND);
        Date installDate = DateUtils.truncate(info.getLastActionDate(), Calendar.SECOND);
        assertFalse("Package install date before test run: " + pkgName, startDate.after(installDate));
    }
}
