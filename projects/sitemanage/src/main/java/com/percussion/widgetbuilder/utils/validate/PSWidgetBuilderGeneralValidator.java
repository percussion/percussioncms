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

import com.percussion.server.PSServer;
import com.percussion.widgetbuilder.data.PSWidgetBuilderDefinitionData;
import com.percussion.widgetbuilder.data.PSWidgetBuilderValidationResult;
import com.percussion.widgetbuilder.data.PSWidgetBuilderValidationResult.ValidationCategory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

/**
 * @author JaySeletz
 *
 */
public class PSWidgetBuilderGeneralValidator
{

    private static final String DESCRIPTION = "description";

    private static final String DUPLICATE_NAME = "There is already a widget with this name";

    private static final String INVALID_FORMAT = "Invalid format, must be of form a.b.c, where a, b and c are numbers";

    private static final String VERSION = "version";

    private static final String PUBLISHER_URL = "publisherUrl";

    private static final String PREFIX = "prefix";

    private static final String LABEL = "label";

    private static final String REQUIRED_MSG = "This is a required field.";
    
    private static final String INVALID_CHARS = "This field contains invalid characters.";
    
    private static final String MAX_CHARS = "Exceeded maximum number of characters allowed: ";

    private static final String AUTHOR = "author";

    private static final String WIDGETTRAYCUSTOMIZEDICONPATH = "widgetTrayCustomizedIconPath";

    private static final String TOOLTIPMESSAGE = "toolTipMessage";

    private static final String INVALID_PATH_MSG = "Provided image does not exist or the Icon path is invalid";

    private static final String INVALID_CHARS_MSG = "This field cannot contains special characters";

    private static String CATEGORY = ValidationCategory.GENERAL.name();
    
    /**
     * Validates the general data
     *  
     * @param definition The definition to validate, not <code>null</code>.
     * @param existing A list of existing definitions, used to ensure unique widget names.
     * 
     * @return A list of results, not <code>null</code>, may be empty.
     */
    public static List<PSWidgetBuilderValidationResult> validate(PSWidgetBuilderDefinitionData definition, List<PSWidgetBuilderDefinitionData> existing)
    {
        List<PSWidgetBuilderValidationResult> results = new ArrayList<>();
        
        if (StringUtils.isBlank(definition.getAuthor())) {
            results.add(new PSWidgetBuilderValidationResult(CATEGORY, AUTHOR, REQUIRED_MSG));
        }
        else {
            validateMaxChars(definition.getAuthor(), 100, CATEGORY, AUTHOR, results);
        }
        
        if (StringUtils.isBlank(definition.getLabel())) {
            results.add(new PSWidgetBuilderValidationResult(CATEGORY, LABEL, REQUIRED_MSG));
        }
        else if (!isValidLabel(definition.getLabel())) {
            results.add(new PSWidgetBuilderValidationResult(CATEGORY, LABEL, INVALID_CHARS));
        }
        else if (isDuplicateName(definition, existing)) {
            results.add(new PSWidgetBuilderValidationResult(CATEGORY, LABEL, DUPLICATE_NAME));
        }
        else {
            validateMaxChars(definition.getLabel(), 100, CATEGORY, LABEL, results);
        }
        
        
        if (StringUtils.isBlank(definition.getPrefix())) {
            results.add(new PSWidgetBuilderValidationResult(CATEGORY, PREFIX, REQUIRED_MSG));
        }
        else if (!isValidLabel(definition.getPrefix())) {
            results.add(new PSWidgetBuilderValidationResult(CATEGORY, PREFIX, INVALID_CHARS));
        }
        else {
            validateMaxChars(definition.getPrefix(), 100, CATEGORY, PREFIX, results);
        }
        
        if (StringUtils.isBlank(definition.getPublisherUrl())) {
            results.add(new PSWidgetBuilderValidationResult(CATEGORY, PUBLISHER_URL, REQUIRED_MSG));
        }
        else {
            validateMaxChars(definition.getPublisherUrl(), 100, CATEGORY, PUBLISHER_URL, results);
        }
        
        if (StringUtils.isBlank(definition.getVersion())) {
            results.add(new PSWidgetBuilderValidationResult(CATEGORY, VERSION, REQUIRED_MSG));
        }
        else if (validateMaxChars(definition.getVersion(), 50, CATEGORY, VERSION, results))
        {
            if (!isValidFormat(definition.getVersion())) {
                results.add(new PSWidgetBuilderValidationResult(CATEGORY, VERSION, INVALID_FORMAT));
            }
        }
        
        if (definition.getDescription() != null) {
            validateMaxChars(definition.getDescription(), 1024, CATEGORY, DESCRIPTION, results);
        }

        if(StringUtils.isNotBlank(definition.getWidgetTrayCustomizedIconPath())){
            definition.setWidgetTrayCustomizedIconPath(definition.getWidgetTrayCustomizedIconPath().replaceAll("\\\\","/"));
            //validate the path
            File imagePath = new File(PSServer.getRxDir(), definition.getWidgetTrayCustomizedIconPath());
            if (!imagePath.exists())
            {
                results.add(new PSWidgetBuilderValidationResult(CATEGORY, WIDGETTRAYCUSTOMIZEDICONPATH, INVALID_PATH_MSG));
            }
        }
        if(StringUtils.isNotBlank(definition.getToolTipMessage())){
            String myRegex = "[@#$%&*()_+-=|<>?{}'\"\\[\\]~\\\\-]";
            validateForSpecialChars(definition.getToolTipMessage(), myRegex, CATEGORY, TOOLTIPMESSAGE, INVALID_CHARS_MSG , results);
        }
        return results;
    }



    /**
     * @param value
     * @param max 
     * @param category
     * @param name
     * @param results
     */
    private static boolean validateMaxChars(String value, int max, String category, String name,
            List<PSWidgetBuilderValidationResult> results)
    {
        if (value.length() > max)
        {
            results.add(new PSWidgetBuilderValidationResult(category, name, MAX_CHARS + max));
            return false;
        }
        
        return true;
    }

    /**
     * @param val
     * @param regex
     * @param category
     * @param name
     * @param msg
     * @param results
     */
    private static boolean validateForSpecialChars(String val, String regex ,String category, String name, String msg ,
                                            List<PSWidgetBuilderValidationResult> results)
    {
        if (StringUtils.isNotBlank(val))
        {
            Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            // added to validate the special characters not used in the fields
            Matcher m = p.matcher(val);
            boolean hasInvalidChars = m.find();
            if (hasInvalidChars) {
                results.add(new PSWidgetBuilderValidationResult(category, name, msg));
                return false;
            }

        }

        return true;
    }


    private static boolean isValidLabel(String label)
    {
        boolean isValid = StringUtils.isAlphanumeric(label);
        if (isValid)
        {
            isValid = !StringUtils.isNumeric(StringUtils.left(label, 1));
        }
        
        return isValid;
    }


    private static boolean isDuplicateName(PSWidgetBuilderDefinitionData definition,
            List<PSWidgetBuilderDefinitionData> existing)
    {
        // if an existing widget, no need to check
        long id = NumberUtils.toLong(definition.getId(), 0);
        if (id > 0) {
            return false;
        }
        
        for (PSWidgetBuilderDefinitionData def : existing)
        {
            if (def.getPrefix().equalsIgnoreCase(definition.getPrefix()) && def.getLabel().equalsIgnoreCase(definition.getLabel())) {
                return true;
            }
        }
        
        return false;
    }


    private static boolean isValidFormat(String format)
    {
        boolean isValidFormat = true;
        String[] parts = format.split("\\.");
        if (parts.length != 3) {
            isValidFormat = false;
        }
        else
        {
            for (int i = 0; i < parts.length; i++)
            {
                if (!NumberUtils.isDigits(parts[i])) {
                    isValidFormat = false;
                }
            }
        }
        
        return isValidFormat;
    }

}
