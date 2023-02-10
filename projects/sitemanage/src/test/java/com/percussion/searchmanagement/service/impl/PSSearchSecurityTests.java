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
        PSSearchRestService svc = new PSSearchRestService(null,null, null);

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
