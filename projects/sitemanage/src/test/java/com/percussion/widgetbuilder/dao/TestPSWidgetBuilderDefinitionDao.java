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

package com.percussion.widgetbuilder.dao;



import com.percussion.services.widgetbuilder.IPSWidgetBuilderDefinitionDao;
import com.percussion.services.widgetbuilder.PSWidgetBuilderDefinition;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;

import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class TestPSWidgetBuilderDefinitionDao extends ServletTestCase
{
    private IPSWidgetBuilderDefinitionDao dao;

    public void setDao(IPSWidgetBuilderDefinitionDao dao)
    {
        this.dao = dao;
    }
    
    
    @Override
    protected void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
    }
    
    @Test
    public void testDao()
    {
        List<PSWidgetBuilderDefinition> previousDefinitions = dao.getAll();
        
        PSWidgetBuilderDefinition definition = new PSWidgetBuilderDefinition();
        assertEquals("Instantiated id is not equal to -1", -1, definition.getWidgetBuilderDefinitionId());
        
        dao.save(definition);
        
        assertFalse("No Id assigned during persist", (-1 == definition.getWidgetBuilderDefinitionId()));
        
        definition.setDescription("a description");
        definition.setLabel("a label");
        definition.setPrefix("perc");
        definition.setPublisherUrl("http://www.percussion.com");
        definition.setVersion("42");
        definition.setFields("this is some field data");
        definition.setWidgetHtml("<p>here is some html with a <b>$field</b> in it</p>");
        definition.setResponsive(true);
        dao.save(definition);
        
        PSWidgetBuilderDefinition comparisonDefinition = dao.find(definition.getWidgetBuilderDefinitionId());
        assertTrue(definition.getDescription().equals(comparisonDefinition.getDescription()));
        assertTrue(definition.getLabel().equals(comparisonDefinition.getLabel()));
        assertTrue(definition.getPrefix().equals(comparisonDefinition.getPrefix()));
        assertTrue(definition.getPublisherUrl().equals(comparisonDefinition.getPublisherUrl()));
        assertTrue(definition.getVersion().equals(comparisonDefinition.getVersion()));
        assertEquals(definition.isResponsive(), comparisonDefinition.isResponsive());
        dao.delete(definition.getWidgetBuilderDefinitionId());
        
        assertTrue(null == dao.find(definition.getWidgetBuilderDefinitionId()));
        
        dao.save(definition);
        PSWidgetBuilderDefinition definition2 = new PSWidgetBuilderDefinition();
        definition2.setDescription("a description");
        definition2.setLabel("a label");
        definition2.setPrefix("perc");
        definition2.setPublisherUrl("http://www.percussion.com");
        definition2.setVersion("42");
        dao.save(definition2);
        
        List<PSWidgetBuilderDefinition> definitions = dao.getAll();
        assertTrue(2 + previousDefinitions.size() == definitions.size());
        
        dao.delete(definition.getWidgetBuilderDefinitionId());
        dao.delete(definition2.getWidgetBuilderDefinitionId());
        
        definitions = dao.getAll();
        assertTrue(previousDefinitions.size() == definitions.size());
    }

}
