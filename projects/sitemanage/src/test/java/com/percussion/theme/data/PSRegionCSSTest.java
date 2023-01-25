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

package com.percussion.theme.data;

import com.percussion.share.data.PSDataObjectTestCase;
import com.percussion.theme.data.PSRegionCSS.Property;

import java.util.ArrayList;
import java.util.List;

public class PSRegionCSSTest extends PSDataObjectTestCase<PSRegionCSS>
{

    @Override
    public PSRegionCSS getObject() throws Exception
    {
        PSRegionCSS rule = new PSRegionCSS("container", "header");
        List<Property> properties = new ArrayList<Property>();

        Property property = new Property("font-size", "12px");
        properties.add(property);
        property = new Property("border", "2px");
        properties.add(property);
        property = new Property("margin", "5px");
        properties.add(property);

        rule.setProperties(properties);

        return rule;
    }

}
