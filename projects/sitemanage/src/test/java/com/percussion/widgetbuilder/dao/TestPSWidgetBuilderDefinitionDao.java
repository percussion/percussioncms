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

package com.percussion.widgetbuilder.dao;


import com.percussion.services.widgetbuilder.IPSWidgetBuilderDefinitionDao;
import com.percussion.services.widgetbuilder.PSWidgetBuilderDefinition;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.List;

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
    public void testDao() throws IPSGenericDao.SaveException {
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
