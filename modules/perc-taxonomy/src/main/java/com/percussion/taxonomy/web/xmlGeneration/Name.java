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

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Text;

public class Name {

    @Attribute(required = false)
    public String onclick;
	
    @Attribute(required = false)
    public String href;
    
    @Text(data = true)
    public String text;

    public Name() {
    }

    public Name(String text) {
        this.text = text;
    }

    public Name(String text, String link) {
        this.text = text;
        this.href = link;
    }
    
    public Name(String text, String link, String onclick) {
        this.text = text;
        this.href = link;
        this.onclick = onclick;
    }
}
