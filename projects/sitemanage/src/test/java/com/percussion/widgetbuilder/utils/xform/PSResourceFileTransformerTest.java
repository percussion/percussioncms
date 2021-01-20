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
