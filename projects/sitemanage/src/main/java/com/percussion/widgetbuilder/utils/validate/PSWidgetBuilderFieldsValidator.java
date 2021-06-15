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
package com.percussion.widgetbuilder.utils.validate;

import com.percussion.security.SecureStringUtils;
import com.percussion.utils.security.PSSecurityUtility;
import com.percussion.widgetbuilder.data.PSWidgetBuilderFieldData;
import com.percussion.widgetbuilder.data.PSWidgetBuilderFieldData.FieldType;
import com.percussion.widgetbuilder.data.PSWidgetBuilderFieldsListData;
import com.percussion.widgetbuilder.data.PSWidgetBuilderValidationResult;
import com.percussion.widgetbuilder.data.PSWidgetBuilderValidationResult.ValidationCategory;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author JaySeletz
 *
 */
public class PSWidgetBuilderFieldsValidator
{

    private static final String DUPLICATE_NAME = "Duplicate Name: ";

    private static final String TYPE = "type";

    private static final String LABEL = "label";

    private static final String NAME = "name";

    private static final String CATEGORY = ValidationCategory.CONTENT.name();
    
    private static final String INVALID_VALUE = "Invalid value: ";
    
    public static List<PSWidgetBuilderValidationResult> validate(PSWidgetBuilderFieldsListData fields)
    {
        List<PSWidgetBuilderValidationResult> results = new ArrayList<>();

        Set<String> names = new HashSet<>();
        for (PSWidgetBuilderFieldData field : fields.getFields())
        {
            if (!SecureStringUtils.isValidTableOrColumnName(field.getName())) {
                results.add(new PSWidgetBuilderValidationResult(CATEGORY, NAME, INVALID_VALUE + field.getName()));
            }
            
            if (StringUtils.isBlank(field.getLabel()) || field.getLabel().length() > 50) {
                results.add(new PSWidgetBuilderValidationResult(CATEGORY, LABEL, INVALID_VALUE + field.getLabel()));
            }
            
            if (!isValidType(field.getType())) {
                results.add(new PSWidgetBuilderValidationResult(CATEGORY, TYPE, INVALID_VALUE + field.getType()));
            }
            
            if (names.contains(field.getName()))
            {
                results.add(new PSWidgetBuilderValidationResult(CATEGORY, NAME, DUPLICATE_NAME + field.getName()));
            }
            else 
            {
                names.add(field.getName());
            }            
        }


        return results;
    }

    private static boolean isValidType(String type)
    {
        FieldType[] values = FieldType.values();
        for (FieldType fieldType : values)
        {
            if (fieldType.name().equals(type)) {
                return true;
            }
        }
        
        return false;
    }

}
