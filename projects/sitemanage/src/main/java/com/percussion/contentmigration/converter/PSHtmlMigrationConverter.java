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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.contentmigration.converter;

import com.percussion.contentmigration.converters.IPSContentMigrationConverter;

import java.util.HashMap;
import java.util.Map;

public class PSHtmlMigrationConverter implements IPSContentMigrationConverter
{

    private static final String WIDGET_DEF = "percRawHtml";
    private static final String WIDGET_FIELD_HTML = "html";
    private static final String WIDGET_CONTENT_TYPE = "percRawHtmlAsset";

    @Override
    public String getWidgetDefId()
    {
        return WIDGET_DEF;
    }

    @Override
    public Map<String, Object> convert(String source)
    {
        Map<String, Object> fields = new HashMap<>();
        fields.put(WIDGET_FIELD_HTML, source);
        return fields;
    }

    @Override
    public String getWidgetContentType()
    {
        return WIDGET_CONTENT_TYPE;
    }

}
