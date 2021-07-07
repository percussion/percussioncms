/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.searchmanagement.service.impl;

import com.percussion.searchmanagement.data.PSSearchCriteria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PSSearchSecurityTests {

    private static final Logger log = LogManager.getLogger(PSSearchSecurityTests.class);

    @Test
    public void testCriteriaValidation(){
        PSSearchCriteria badCriteria = new PSSearchCriteria();
        PSSearchRestService svc = new PSSearchRestService(null,null);

        badCriteria.setQuery("<Script>alert();</Script> AND g=1");
        log.info("Bad Query: {}", badCriteria.getQuery());
        svc.sanitizeCriteria(badCriteria);
        log.info("Scrubbed Query: {}", badCriteria.getQuery());

        assertNotEquals("<Script>alert();</Script> AND g=1",badCriteria.getQuery());

        HashMap<String,String> fields = new HashMap<>();

        fields.put("key1", "<Script>alert();</Script> ");
        fields.put("key2", "Some data. Yay!!!");
        fields.put("key3", "");
       badCriteria.setSearchFields(fields);

       svc.sanitizeCriteria(badCriteria);
       log.info("Scrubbed Field 0: {}", badCriteria.getSearchFields().values().toArray()[0]);
       log.info("Scrubbed Field 1: {}", badCriteria.getSearchFields().values().toArray()[1]);
       log.info("Scrubbed Field 2: {}", badCriteria.getSearchFields().values().toArray()[2]);

        assertNotEquals("<Script>alert();</Script> ", badCriteria.getSearchFields().values().toArray()[0]);
        assertEquals("Some data. Yay!!!", badCriteria.getSearchFields().values().toArray()[1]);
        assertEquals("", badCriteria.getSearchFields().values().toArray()[2]);

        //Sort
       badCriteria.setSortColumn("IN VALID <script>alert();</script> COLUMN NAME");

       log.info("Bad Sort Column: {}",badCriteria.getSortColumn());
       svc.sanitizeCriteria(badCriteria);
       log.info("Scrubbed Sort Column: {}",badCriteria.getSortColumn());

       assertNotEquals("IN VALID <script>alert();</script> COLUMN NAME",
               badCriteria.getSortColumn());

        assertFalse(badCriteria.getSortColumn().contains("<"));
        assertFalse(badCriteria.getSortColumn().contains(">"));
        assertFalse(badCriteria.getSortColumn().contains("/"));
        assertFalse(badCriteria.getSortColumn().contains(" "));

        badCriteria.setFolderPath("//Sites/www.mysite.com/test/index.html");
        svc.sanitizeCriteria(badCriteria);
        assertNotNull(badCriteria.getFolderPath());

        badCriteria.setFolderPath("//Sites/www.mysite.edu/test/<script>alert()</script>/test");
        log.info("Bad folder path: {}" ,badCriteria.getFolderPath());
        svc.sanitizeCriteria(badCriteria);
        log.info("Scrubbed folder path: {}" ,badCriteria.getFolderPath());
        assertNull(badCriteria.getFolderPath());
    }

}
