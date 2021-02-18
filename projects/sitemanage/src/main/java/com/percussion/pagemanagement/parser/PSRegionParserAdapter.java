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
package com.percussion.pagemanagement.parser;

import com.percussion.pagemanagement.data.PSAbstractRegion;
import com.percussion.pagemanagement.data.PSRegionCode;
import com.percussion.pagemanagement.parser.IPSRegionParser.IPSRegionParserRegionFactory;

public abstract class PSRegionParserAdapter<REGION extends PSAbstractRegion, CODE extends PSRegionCode>
        implements
            IPSRegionParserRegionFactory<REGION, CODE>,
            IPSRegionParser<REGION, CODE>
{
    PSRegionParser<REGION, CODE> parser = new PSRegionParser<>(this);

    public PSParsedRegionTree<REGION, CODE> parse(String text)
    {
        return parser.parse(text);
    }

}
