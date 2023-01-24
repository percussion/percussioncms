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

package com.percussion.patch.test;

import com.percussion.linkmanagement.service.IPSManagedLinkService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PSSaveAssetsMainetanceProcessUT {

    @Test
    public void testTarget(){
        Document doc = Jsoup.parseBodyFragment("<p>This is <a href=\"#\" target=\"_blank\"/>");
        Elements targetAnchors = doc.select(IPSManagedLinkService.A_HREF + "a[target=\"_blank\"]"
                + ":not(a[rel=\"noopener noreferrer\"])");

        assertFalse(targetAnchors.isEmpty());

        doc = Jsoup.parseBodyFragment("<p>This is <a href=\"#\" target=\"_blank\" rel=\"noopener noreferrer\" />");
        targetAnchors = doc.select(IPSManagedLinkService.A_HREF + "a[target=\"_blank\"]"
                + ":not(a[rel=\"noopener noreferrer\"])");

        assertTrue(targetAnchors.isEmpty());

    }
}
