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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.pagemanagement.service;

import static com.percussion.pagemanagement.service.impl.PSResourceDefinitionUtils.sortByDependencies;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSFileResource;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSResourceDefinition;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSResourceDependency;
import com.percussion.pagemanagement.service.impl.PSResourceDefinitionUtils;

public class PSResourceDefinitionUtilsTest
{
    private List<PSResourceDefinition> resources;
    private List<PSResourceDefinition> actual;
    private List<PSResourceDefinition> expected;
    private PSResourceDefinition a;
    private PSResourceDefinition b;
    private PSResourceDefinition c;
    private PSResourceDefinition d;
    private PSResourceDefinition e;
    private PSResourceDefinition f;
    
    @Before
    public void setUp() {
        a = createResource("a", (String[]) null);
        b = createResource("b", "a");
        c = createResource("c", "b", "a");
        d = createResource("d", "c", "f");
        e = createResource("e", "d", "b");
        f = createResource("f", "e");
        
        resources = new ArrayList<PSResourceDefinition>(asList(b,d,c,a,e));
        expected = asList(a,b,c,d,e);
    }
    
    
    @Test
    public void testDepOrder() throws Exception
    {
        actual = sortByDependencies(resources);
        assertEquals("Expected to sort",expected,actual);
    }
    
    @Test(expected=PSResourceDefinitionUtils.PSResourceDefinitionDependencyCycleException.class)
    public void testCycle() throws Exception
    {
        resources.add(f);
        actual = sortByDependencies(resources);
    }
    
    
    public PSFileResource createResource(String id, String ... depIds) {
        PSFileResource r = new PSFileResource();        
        if (depIds != null)
            r.setDependencies(createDeps(depIds));
        r.setUniqueId(id);
        return r;
    }
    
    public List<PSResourceDependency> createDeps(String ...ids) {
        List<PSResourceDependency> deps = new ArrayList<PSResourceDependency>();
        for (String id : ids) {
            PSResourceDependency d = new PSResourceDependency();
            d.setDependeeId(id);
            deps.add(d);
        }
        return deps;
    }
    
    

}

