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
