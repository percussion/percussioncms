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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.percussion.packagemanagement.PSPackageFileEntry.PackageFileStatus;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * @author JaySeletz
 *
 */
public class PSPackageFileTest
{

    @Test
    public void test() throws Exception
    {
        String pkgXmlString = getPackageFileContents("TestPackageFile.xml");
        PSPackageFileList pkgFile = PSPackageFileList.fromXml(pkgXmlString);
        assertNotNull(pkgFile);
        
        List<PSPackageFileEntry> entries = pkgFile.getEntries();
        assertNotNull(entries);
        assertEquals(3, entries.size());
        Iterator<PSPackageFileEntry> iterator = entries.iterator();
        
        PSPackageFileEntry entry = iterator.next();
        assertEquals("perc.PackageInstalled", entry.getPackageName());
        assertEquals(PackageFileStatus.INSTALLED, entry.getStatus());
        
        entry = iterator.next();
        assertEquals("perc.PackageFailed", entry.getPackageName());
        assertEquals(PackageFileStatus.FAILED, entry.getStatus());
        
        entry.setStatus(PackageFileStatus.INSTALLED);
        
        entry = iterator.next();
        assertEquals("perc.PackagePending", entry.getPackageName());
        assertEquals(PackageFileStatus.PENDING, entry.getStatus());
        
        entry.setStatus(PackageFileStatus.INSTALLED);
        
        // round-trip and verify the changes
        PSPackageFileList modPkgFile = PSPackageFileList.fromXml(pkgFile.toXml());
        entries = modPkgFile.getEntries();
        iterator = entries.iterator();
        entry = iterator.next();
        assertEquals("perc.PackageInstalled", entry.getPackageName());
        assertEquals(PackageFileStatus.INSTALLED, entry.getStatus());
        
        entry = iterator.next();
        assertEquals("perc.PackageFailed", entry.getPackageName());
        assertEquals(PackageFileStatus.INSTALLED, entry.getStatus());
        
        entry = iterator.next();
        assertEquals("perc.PackagePending", entry.getPackageName());
        assertEquals(PackageFileStatus.INSTALLED, entry.getStatus());
    }


    private String getPackageFileContents(String name) throws IOException
    {
        return IOUtils.toString(this.getClass().getResourceAsStream(name));
        
    }

}
