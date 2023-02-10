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

package com.percussion.soln.p13n.tracking.data;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.p13n.tracking.data.VisitorProfileResourceRepository;


public class VisitorProfileResourceRepositoryTest {
    VisitorProfileResourceRepository repo;
    Resource readResource;
    Resource writeResource;
    VisitorProfile a;
    VisitorProfile b;
    File writeFile;
    @Before
    public void setUp() {
        repo = new VisitorProfileResourceRepository();
        String basePath = getClass().getPackage().getName().replaceAll("\\.","/")  + "/";
        writeFile = new File("build/.null/repo.xml");
        String readPath = basePath + getClass().getSimpleName()+ ".xml";
        //assertEquals(readPath, "test/percussion/soln/p13n/tracking/data/VisitorProfileResourceRepositoryTest.xml");
        readResource = new ClassPathResource(readPath);
        writeResource = new FileSystemResource(writeFile);
        
        a = new VisitorProfile();
        a.setId(1L);
        a.setUserId("a");
        b = new VisitorProfile();
        b.setId(2L);
        b.setUserId("b");
    }
    
    @Test
    public void testSave() {
        repo.setResource(writeResource);
        repo.addProfile(a);
        repo.addProfile(b);
        repo.save();
    }
    
    @Test
    public void testLoad() {
        repo.setResource(readResource);
        repo.load();
        assertEquals("Should have A and B profile", asList(a,b), asList(repo.getProfileByUserId("a"), repo.getProfileByUserId("b")));
    }
    
    

}
