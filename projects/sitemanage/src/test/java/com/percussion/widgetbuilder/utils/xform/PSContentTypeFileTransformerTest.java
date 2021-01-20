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

import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.design.objectstore.PSControlMeta;
import com.percussion.widgetbuilder.data.PSWidgetBuilderFieldData;
import com.percussion.widgetbuilder.data.PSWidgetBuilderFieldData.FieldType;
import com.percussion.widgetbuilder.utils.PSWidgetPackageSpec;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * @author JaySeletz
 *
 */
public class PSContentTypeFileTransformerTest
{
    private static PSWidgetPackageSpec packageSpec;
    
    @BeforeClass
    public static void beforeClass()
    {
        packageSpec = setupPackageSpec();
    }

    
    @Test
    public void testHandleFile() throws Exception
    {
        PSContentTypeFileTransformer xform = new PSContentTypeFileTransformer(new PSControlMgr());
        
        assertTrue(xform.handleFile(new File("/test/myWidget.schemaDef.contentType")));
        assertTrue(xform.handleFile(new File("/test/myWidget.itemDef.contentType")));
        assertFalse(xform.handleFile(new File("/test/myWidget.nodeDef.contentType")));
        assertFalse(xform.handleFile(new File("/test/myWidget.xml")));
    }
    
    @Test
    public void testTransformSchemaFile() throws Exception
    {
        PSContentTypeFileTransformer xform = new PSContentTypeFileTransformer(new PSControlMgr());
        File file = new File("/test/myWidget.schemaDef.contentType");
        Reader resultReader = new InputStreamReader(this.getClass().getResourceAsStream("transformSchema.xml"));
        Reader expectedReader = new InputStreamReader(this.getClass().getResourceAsStream("expectedSchema.xml"));
        
        String expected = PSXmlDocumentBuilder.toString(xform.getSchema(expectedReader).toXml(PSXmlDocumentBuilder.createXmlDocument()));
        String result = IOUtils.toString(xform.transformFile(file, resultReader, packageSpec));
        assertEquals(expected, result);
    }
    
    @Test
    public void testTransformItemDefFile() throws Exception
    {
        PSContentTypeFileTransformer xform = new PSContentTypeFileTransformer(new PSControlMgr());
        File file = new File("/test/myWidget.itemDef.contentType");
        Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("transformItemDef.xml"));
        
        PSItemDefinition expectedItemDef = new PSItemDefinition(PSXmlDocumentBuilder.createXmlDocument(this.getClass().getResourceAsStream("expectedItemDef.xml"), false).getDocumentElement());
        String expected = PSXmlDocumentBuilder.toString(expectedItemDef.toXml(PSXmlDocumentBuilder.createXmlDocument()));
        String result = IOUtils.toString(xform.transformFile(file, reader, packageSpec));
       
        assertEquals(expected, result);
    }

    public static PSWidgetPackageSpec setupPackageSpec()
    {
        PSWidgetPackageSpec spec = new PSWidgetPackageSpec("pre", "url", "MyWidget", "", "1.0.0", "3.2.1");

        List<PSWidgetBuilderFieldData> fields = new ArrayList<PSWidgetBuilderFieldData>();
        PSWidgetBuilderFieldData field;
        field = new PSWidgetBuilderFieldData();
        field.setName("Author");
        field.setLabel(field.getName());
        field.setType(FieldType.TEXT.name());
        fields.add(field);
        
        field = new PSWidgetBuilderFieldData();
        field.setName("ContentDate");
        field.setLabel("Content Date");
        field.setType(FieldType.DATE.name());
        fields.add(field);
        
        field = new PSWidgetBuilderFieldData();
        field.setName("ContentAbstract");
        field.setLabel("Content Abstract");
        field.setType(FieldType.TEXT_AREA.name());
        fields.add(field);
        
        field = new PSWidgetBuilderFieldData();
        field.setName("Article");
        field.setLabel(field.getName());
        field.setType(FieldType.RICH_TEXT.name());
        fields.add(field);
        
        field = new PSWidgetBuilderFieldData();
        field.setName("ImageField");
        field.setLabel(field.getName());
        field.setType(FieldType.IMAGE.name());
        fields.add(field);
        
        spec.setFields(fields);
        
        return spec;
    }
    
    private class PSControlMgr implements IPSControlManager
    {
        PSControlMeta ctrlMeta;
        public PSControlMgr() throws Exception
        {
            ctrlMeta = new PSControlMeta((Element) PSXmlDocumentBuilder.createXmlDocument(this.getClass().getResourceAsStream("controlMeta.xml"), false).getDocumentElement().getElementsByTagName("psxctl:ControlMeta").item(0));
        }
        /* (non-Javadoc)
         * @see com.percussion.widgetbuilder.utils.xform.IPSControlManager#getControl(java.lang.String)
         */
        @Override
        public PSControlMeta getControl(String name)
        {
            return ctrlMeta;
        }
        
    }
}
