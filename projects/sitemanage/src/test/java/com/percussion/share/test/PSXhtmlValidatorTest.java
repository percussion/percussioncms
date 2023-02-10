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
package com.percussion.share.test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;
import static com.percussion.share.test.PSMatchers.*;
import org.junit.Test;

/**
 * This is a Unit test of unit test code :)
 * @author adamgent
 *
 */
public class PSXhtmlValidatorTest
{
    
    @Test
    public void testValidXhtmlMatcher() throws Exception
    {
        String xhtml = getHtml("test-xhtml-valid.html");
        assertThat(xhtml, is(validXhtml()));
    }
    
    
    @Test
    public void testInValidXhtmlMatcher() throws Exception
    {
        String xhtml = getHtml("test-xhtml-invalid.html");
        assertThat(xhtml, is(not(validXhtml())));
    }
    
    
    private String getHtml(String name) {
        return  PSTestUtils.resourceToString(PSXhtmlValidatorTest.class, name);
    }


}

