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

import com.percussion.pagemanagement.dao.impl.PSResourceDefinitionGroupDao;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSResourceDefinition;
import com.percussion.share.IPSSitemanageConstants;

public class PSResourceDefinitionGroupDaoTest
{
    
    private PSResourceDefinitionGroupDao dao;
    

    
    @Before
    public void setup() throws Exception
    {
        dao = new PSResourceDefinitionGroupDao();
        //dao.setRepositoryDirectory("src/main/resources/Rhythmyx/rxconfig/Resources");
        dao.setRepositoryDirectory("src/test/resources/resourceDefinitions");
        
    }
    
    
    @Test
    public void shouldFindGroup() throws Exception
    {
        PSResourceDefinitionGroup widget = dao.find("percSystem");
        assertResourceGroup(widget);
        
    }
    
    @Test
    public void shouldFindResource() throws Exception
    {
        PSResourceDefinition resource = dao.findResource("percSystem.page");
        assertNotNull(resource);
        
        PSResourceDefinition resourceXml = dao.findResource("percSystem.pageXml");
        assertNotNull(resourceXml);
    }
    
    @Test
    public void shouldFindAllResources() throws Exception
    {
        List<PSResourceDefinition> resources = dao.findAllResources();
        assertEquals(7, resources.size());
    }
    
    @Test
    public void shouldFindDeps() throws Exception
    {
        PSResourceDefinition resource = dao.findResource("percSystem.blah_css");
        assertTrue(resource.getDependencies().size() > 0);
    }
    
    @Test
    public void shouldFindAssetResourceForContentType() throws Exception
    {
        assertNotNull(dao.findAssetResourceForType("percPage"));
    }


    @Test
    public void shouldFindAllGroups() throws Exception
    {
        List<PSResourceDefinitionGroup> widgets = dao.findAll();
        assertEquals(1, widgets.size());
    }
    
    @Test
    public void shouldPoll() throws Exception {
        dao.poll();
        dao.poll();
    }
    
    
    @Test(expected=UnsupportedOperationException.class)
    public void shouldNotSupportDelete() throws Exception
    {
        dao.delete("fail");    
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void shouldNotSupportSave() throws Exception
    {
        PSResourceDefinitionGroup widget = new PSResourceDefinitionGroup();
        dao.save(widget);
        
    }
    

    private void assertResourceGroup(PSResourceDefinitionGroup rdg)
    {
        assertEquals(IPSSitemanageConstants.PLAIN_BASE_TEMPLATE_NAME, rdg.getAssetResources().get(0).getLegacyTemplate());
        assertEquals("percPage", rdg.getAssetResources().get(0).getContentType());
    }
    

}
