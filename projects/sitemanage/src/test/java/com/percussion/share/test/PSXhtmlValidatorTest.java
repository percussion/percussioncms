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

