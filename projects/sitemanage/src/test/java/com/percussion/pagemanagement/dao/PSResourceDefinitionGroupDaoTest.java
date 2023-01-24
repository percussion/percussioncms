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
