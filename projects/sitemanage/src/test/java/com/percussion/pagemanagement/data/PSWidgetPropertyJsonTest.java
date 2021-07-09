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
package com.percussion.pagemanagement.data;

import static com.percussion.share.dao.PSSerializerUtils.*;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class PSWidgetPropertyJsonTest
{

    private String number = "234";
    private String string = "'hello'";
    private String list = "['a','b','c']";
    private String empty = "";
    
    @Test
    public void testJson() throws Exception
    {
        log.debug(getObjectFromJson(list));
        log.debug(getObjectFromJson(number));
        log.debug(getObjectFromJson(string));
        log.debug(getJsonFromObject(42));
        log.debug(getJsonFromObject("42"));
        
        String trueJason = getJsonFromObject(Boolean.TRUE);
        Object trueObject = getObjectFromJson(trueJason);
        assertTrue(trueObject instanceof Boolean);
        assertTrue((Boolean)trueObject);
    }
    
    @Test
    public void testEmptyJsonString() throws Exception
    {
        Object o = getObjectFromJson(empty);
        assertThat(o, nullValue());
    }
    
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSWidgetPropertyJsonTest.class);
}
