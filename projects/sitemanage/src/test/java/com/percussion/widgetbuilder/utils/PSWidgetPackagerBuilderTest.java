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
package com.percussion.widgetbuilder.utils;

import com.percussion.server.PSServer;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.widgetbuilder.utils.xform.PSContentTypeFileTransformerTest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.cactus.ServletTestCase;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author JaySeletz
 *
 */
@Category(IntegrationTest.class)
public class PSWidgetPackagerBuilderTest extends ServletTestCase
{

    @Test
    public void test() throws Exception
    {
        File srcFile = new File(PSServer.getRxDir(), "sys_resources/widgetbuilder/percWidgetTemplate.zip");
        assertTrue(srcFile.exists());
        File tmpDir = new File(FileUtils.getTempDirectory(), this.getClass().getName());
        File tgtDir = new File(tmpDir, "packages"); 
        tgtDir.mkdirs();
        
        File result = null;
        try
        {
            PSWidgetPackageBuilder builder = new PSWidgetPackageBuilder(srcFile, tmpDir);
            PSWidgetPackageSpec spec = new PSWidgetPackageSpec("test", "www.test.com", "Custom Widget 2", "a 2nd test widget", "1.0.0", "3.1.0");
            spec.setResponsive(true);
            spec.setFields(PSContentTypeFileTransformerTest.setupPackageSpec().getFields());
            spec.setWidgetHtml("<div>$field</div>");
            List<String> files = new ArrayList<String>();
            files.add("/web_resources/preMyWidget/foo/bar.css");
            files.add("/web_resources/preMyWidget/foo/bar2.css");
            files.add("http://foo.com/bar.css");
            spec.setCssFiles(files);
            
            files = new ArrayList<String>();
            files.add("/web_resources/preMyWidget/foo/bar.js");
            files.add("/web_resources/preMyWidget/foo/bar2.js");
            files.add("http://foo.com/bar.js"); 
            spec.setJsFiles(files);
            
            result = builder.generatePackage(tgtDir, spec);
            assertTrue(result.exists());
            assertEquals(tgtDir, result.getParentFile());
            assertEquals(result.getName(), spec.getPackageName() + ".ppkg");
        }
        finally
        {
            FileUtils.deleteQuietly(result);
        }
    }
}
