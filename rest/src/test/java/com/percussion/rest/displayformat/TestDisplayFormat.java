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

package com.percussion.rest.displayformat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class TestDisplayFormat {

    @Test
    public void testToAndFromJson() throws IOException {

        DisplayFormat f = new DisplayFormat();
        f.setDescription("DescriptionTest");
        f.setDisplayName("DisplayNameTest");
        f.setInternalName("InternalNameTest");

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(f);
        System.out.println(json);

        DisplayFormat d2 = new DisplayFormat();
        d2 = mapper.readValue(json, DisplayFormat.class);

        assertEquals("DescriptionTest", d2.getDescription());
        assertEquals("DisplayNameTest", d2.getDisplayName());
        assertEquals("InternalNameTest", d2.getInternalName());

        //TODO:  Finish me - test all the properties
    }
}
