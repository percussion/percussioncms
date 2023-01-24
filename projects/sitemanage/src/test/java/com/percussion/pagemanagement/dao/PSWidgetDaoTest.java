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
package com.percussion.pagemanagement.dao;

import com.percussion.pagemanagement.dao.impl.PSWidgetDao;
import com.percussion.pagemanagement.data.PSWidgetDefinition;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PSWidgetDaoTest
{
    
    PSWidgetDao widgetDao;
    

    
    @Before
    public void setup() throws Exception
    {
        widgetDao = new PSWidgetDao();
        widgetDao.setRepositoryDirectory("src/test/resources/widgets");
        
    }
    
    
    @Test
    public void shouldFindWidget() throws Exception
    {
        PSWidgetDefinition widget = widgetDao.find("RawHtmlWidget");
        assertRawHtmlWidget(widget);
        
    }


    @Test
    public void shouldFindAllWidgets() throws Exception
    {
        List<PSWidgetDefinition> widgets = widgetDao.findAll();
        assertEquals(3, widgets.size());
    }
    
    @Test
    public void shouldPoll() throws Exception {
        widgetDao.poll();
        widgetDao.poll();
    }
    
    
    @Test(expected=UnsupportedOperationException.class)
    public void shouldNotSupportDelete() throws Exception
    {
        widgetDao.delete("fail");    
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void shouldNotSupportSave() throws Exception
    {
        PSWidgetDefinition widget = new PSWidgetDefinition();
        widgetDao.save(widget);
        
    }
    

    private void assertRawHtmlWidget(PSWidgetDefinition widget)
    {
        assertEquals("Raw Html Widget", widget.getWidgetPrefs().getTitle());
        assertEquals("PSXRawHtmlWidget", widget.getWidgetPrefs().getContenttypeName());
        assertEquals("my_css", widget.getCssPref().get(0).getName());
    }
    

}
