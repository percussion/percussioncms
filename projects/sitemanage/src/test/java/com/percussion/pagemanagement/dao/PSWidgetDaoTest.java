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
package com.percussion.pagemanagement.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.percussion.pagemanagement.dao.impl.PSWidgetDao;
import com.percussion.pagemanagement.data.PSWidgetDefinition;

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
        assertEquals(2, widgets.size());
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
