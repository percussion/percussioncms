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
