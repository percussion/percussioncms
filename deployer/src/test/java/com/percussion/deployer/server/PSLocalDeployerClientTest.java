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
