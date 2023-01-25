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

