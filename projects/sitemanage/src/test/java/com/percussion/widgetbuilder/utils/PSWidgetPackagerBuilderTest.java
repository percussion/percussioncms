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
