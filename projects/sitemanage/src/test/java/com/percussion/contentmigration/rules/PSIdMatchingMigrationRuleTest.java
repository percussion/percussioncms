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
package com.percussion.contentmigration.rules;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PSIdMatchingMigrationRuleTest
{

    private Document sourceDoc = null;

    private Document targetDoc = null;

    private PSIdMatchingMigrationRule matchingMigrationRule;

    @Before
    public void setup() throws IOException
    {
        sourceDoc = Jsoup.parse(new File("src/test/resources/contentmigration/sourceDoc.html"), "UTF-8");
        targetDoc = Jsoup.parse(new File("src/test/resources/contentmigration/targetDoc.html"), "UTF-8");
    }

    @Test
    public void testMatchingMigrationRule()
    {
        matchingMigrationRule = new PSIdMatchingMigrationRule();
        String content = matchingMigrationRule.findMatchingContent("1", sourceDoc, targetDoc);
        Elements elems = targetDoc.select("#perc-content");
        Element elem = elems.get(0);
        Assert.assertNotNull(content);
        Assert.assertEquals(elem.html(), content);
    }
    
    @Test
    public void testNotMatchingMigrationRule()
    {
        matchingMigrationRule = new PSIdMatchingMigrationRule();
        String content = matchingMigrationRule.findMatchingContent("2", sourceDoc, targetDoc);
        Elements elems = targetDoc.select("#perc-content");
        Element elem = elems.get(0);
        Assert.assertNull(content);
        Assert.assertNotSame(elem.html(), content);
    }
}
