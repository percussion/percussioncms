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

package com.percussion.taxonomy.web.xmlGeneration;

import org.simpleframework.xml.core.Persister;
import java.io.ByteArrayOutputStream;

public class Test {

    public static void main(String[] args) {
        new Test();
    }

    public Test() {
        RootTag r = new RootTag();

        Value v1 = new Value(1, "abcd");
        
        Attr at1 = new Attr();
        at1.name = "Abbreviation";
        at1.langID = 1;
        at1.addValue(v1);
        
        Item i1 = new Item();
        i1.id = "" + 22;
        i1.parent_id = "" + 33;
        i1.state = "Awesome";
        i1.title = "Bananas";
        i1.addAttribute(at1);

        Item i2 = new Item();
        i2.id = "" + 11;
        i2.parent_id = "" + 99;
        i2.state = "1337 Goodness";
        i2.title = "Pineapples";

        Item i3 = new Item();
        i3.id = "" + 44;
        i3.parent_id = "" + 66;
        i3.state = "Angry Beavers";
        i3.title = "Mangos";

        Content c1 = new Content();
        c1.name = new Name("Bob's Big Boy");
        i1.content = c1;

        Content c2 = new Content();
        c2.name = new Name("Shoney's");
        i2.content = c2;

        Content c3 = new Content();
        c3.name = new Name("Golden Corral");
        i3.content = c3;

        r.addItem(i1);
        r.addItem(i2);
        r.addItem(i3);

        Persister serializer = new Persister();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            serializer.write(r, baos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(baos);
    }
}