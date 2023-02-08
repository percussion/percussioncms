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

package com.percussion.taxonomy.web.xmlGeneration;

import com.percussion.error.PSExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simpleframework.xml.core.Persister;

import java.io.ByteArrayOutputStream;

public class Test {

    private static final Logger log = LogManager.getLogger(Test.class);

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
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        }
        //System.out.println(baos);
    }
}
