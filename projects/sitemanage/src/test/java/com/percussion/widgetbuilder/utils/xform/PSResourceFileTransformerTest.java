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
package com.percussion.widgetbuilder.utils.xform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.percussion.pagemanagement.data.PSResourceDefinitionGroup;
import com.percussion.share.dao.PSSerializerUtils;
import com.percussion.widgetbuilder.utils.PSWidgetPackageSpec;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * @author JaySeletz
 *
 */
public class PSResourceFileTransformerTest
{

    /**
     * Test method for {@link com.percussion.widgetbuilder.utils.xform.PSResourceFileTransformer#transformFile(java.io.File, java.io.Reader, com.percussion.widgetbuilder.utils.PSWidgetPackageSpec)}.
     */
    @Test
    public void testTransformFile() throws Exception
    {
        PSWidgetPackageSpec packageSpec = new PSWidgetPackageSpec("pre", "url", "MyWidget", "", "1.0.0", "3.2.1");
        List<String> files = new ArrayList<String>();
        files.add("/web_resources/preMyWidget/foo/bar.css");
        files.add("/web_resources/preMyWidget/foo/bar2.css");
        files.add("http://foo.com/bar.css");
        packageSpec.setCssFiles(files);
        
        files = new ArrayList<String>();
        files.add("/web_resources/preMyWidget/foo/bar.js");
        files.add("/web_resources/preMyWidget/foo/bar2.js");
        files.add("http://foo.com/bar.js"); 
        packageSpec.setJsFiles(files);
        
        PSResourceFileTransformer xform = new PSResourceFileTransformer();
        Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("transformResources.xml"));
        File file = new File("sys__UserDependency--rxconfig_Resources_preMyWidget/preMyWidget.xml");
        
        String expected = IOUtils.toString(this.getClass().getResourceAsStream("expectedResources.xml"));
        expected = PSSerializerUtils.marshal(PSSerializerUtils.unmarshal(expected, PSResourceDefinitionGroup.class));
        String result = IOUtils.toString(xform.transformFile(file, reader, packageSpec));  

        assertEquals(expected, result);
    }

    /**
     * Test method for {@link com.percussion.widgetbuilder.utils.xform.PSResourceFileTransformer#handleFile(java.io.File)}.
     */
    @Test
    public void testHandleFile()
    {
        PSResourceFileTransformer xform = new PSResourceFileTransformer();
        assertFalse(xform.handleFile(new File("testWidget.xml")));
        assertFalse(xform.handleFile(new File("sys__UserDependency--rxconfig_Widgets_mywidget/testWidget.xml")));
        assertTrue(xform.handleFile(new File("sys__UserDependency--rxconfig_Resources_mywidget/testWidget.xml")));
    }

    /**
     * Test method for {@link com.percussion.widgetbuilder.utils.xform.PSResourceFileTransformer#transformPath(java.io.File, com.percussion.widgetbuilder.utils.PSWidgetPackageSpec)}.
     */
    @Test
    public void testTransformPath() throws Exception
    {
        File test = new File("/a/b/c");
        PSResourceFileTransformer xform = new PSResourceFileTransformer();
        assertEquals(test, xform.transformPath(test, null));
    }

}
