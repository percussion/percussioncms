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
package com.percussion.share.dao;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PSIdMapperTest extends ServletTestCase
{
    @Override
    protected void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
    }
    
    public void testGetGuid() throws Exception
    {
        IPSGuid guid = createGuid();
        
        PSLocator locator = guidManager.makeLocator(guid);
        assertEquals(guid, idMapper.getGuid(locator));
      
        String guidStr = idMapper.getString(guid);
        assertEquals(guid, idMapper.getGuid(guidStr));
    }
    
    public void testGetGuids() throws Exception
    {
        IPSGuid guid1 = createGuid();
        IPSGuid guid2 = createGuid();
        
        List<String> guidStrings = new ArrayList<String>();
        guidStrings.add(idMapper.getString(guid1));
        guidStrings.add(idMapper.getString(guid2));
        
        List<IPSGuid> guids = idMapper.getGuids(guidStrings);
        assertTrue(guids.contains(guid1) && guids.contains(guid2));
    }
        
    public void testGetLocalContentId() throws Exception
    {
        int id1 = idMapper.getLocalContentId();
        assertTrue(id1 > 0);
        int id2 = idMapper.getLocalContentId();
        assertTrue(id2 > id1);
    }
    
    public void testGetLocator() throws Exception
    {
        IPSGuid guid = createGuid();
        
        PSLocator locator = idMapper.getLocator(guid);
        assertEquals(guid, idMapper.getGuid(locator));
        
        String guidStr = idMapper.getString(guid);
        assertEquals(locator, idMapper.getLocator(guidStr));
    }
    
    public void testGetString() throws Exception
    {
        IPSGuid guid = createGuid();
        String guidStr = idMapper.getString(guid);
        assertEquals(guid, idMapper.getGuid(guidStr));
                
        PSLocator locator = guidManager.makeLocator(guid);
        assertEquals(guidStr, idMapper.getString(locator));
    }
    
    public void testGetStrings() throws Exception
    {
        IPSGuid guid1 = createGuid();
        IPSGuid guid2 = createGuid();
        
        List<IPSGuid> guids = new ArrayList<IPSGuid>();
        guids.add(guid1);
        guids.add(guid2);
        
        List<String> guidStrings = idMapper.getStrings(guids);
        assertTrue(guidStrings.contains(idMapper.getString(guid1)) && guidStrings.contains(idMapper.getString(guid2)));
    }
    
    public IPSGuidManager getGuidManager()
    {
        return guidManager;
    }

    public void setGuidManager(IPSGuidManager guidManager)
    {
        this.guidManager = guidManager;
    }

    public IPSIdMapper getIdMapper()
    {
        return idMapper;
    }

    public void setIdMapper(IPSIdMapper idMapper)
    {
        this.idMapper = idMapper;
    }
    
    private IPSGuid createGuid()
    {
        IPSGuid guid = guidManager.createGuid(PSTypeEnum.LEGACY_CONTENT);
        return new PSLegacyGuid(guid.getUUID(), 1);
    }
    
    private IPSIdMapper idMapper;
    private IPSGuidManager guidManager;
     
}
