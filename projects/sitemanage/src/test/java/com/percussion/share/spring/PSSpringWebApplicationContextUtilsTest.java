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
package com.percussion.share.spring;

import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.*;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PSSpringWebApplicationContextUtilsTest extends ServletTestCase
{
    
    public void testGetWebApplicationContext() 
    {
        assertNotNull(getWebApplicationContext());
        assertNotNull(getWebApplicationContext()
                .getBean("springWebApplicationContextSetter"));
    }
    

    public void testInjectDependencies() throws Exception 
    {
        ToBeAutoWired a = new ToBeAutoWired();
        injectDependencies(a);
        assertNotNull(a.getSpringWebApplicationContextSetter());
    }
    
    
    public static class ToBeAutoWired {
        private PSSpringWebApplicationContextSetter springWebApplicationContextSetter;

        public PSSpringWebApplicationContextSetter getSpringWebApplicationContextSetter()
        {
            return springWebApplicationContextSetter;
        }

        public void setSpringWebApplicationContextSetter(PSSpringWebApplicationContextSetter springWebApplicationContextSetter)
        {
            this.springWebApplicationContextSetter = springWebApplicationContextSetter;
        }
        
        
    }

}
