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
package com.percussion.widgetbuilder.utils;

import com.percussion.utils.IPSTokenResolver;
import com.percussion.widgetbuilder.data.PSWidgetBuilderFieldData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

/**
 * Used to resolve tokens in widget package files
 *  
 * @author JaySeletz
 *
 */
public class PSWidgetPackageResolver implements IPSTokenResolver
{

    private Map<String, String> tokenMap;
    private Set<String> optionalTokens;
    private PSWidgetPackageSpec packageSpec;
    private static List<IPSBindingGenerator> bindingGenerators = new ArrayList<>();
    
    static
    {
        bindingGenerators.add(new PSPageFieldValueGenerator());
    	bindingGenerators.add(new PSFileFieldValueGenerator());
        bindingGenerators.add(new PSImageFieldValueGenerator());
        bindingGenerators.add(new PSBasicFieldValueGenerator()); // this must be last, as it will accept all fields
    }
    
    /**
     * @param packageSpec
     */
    public PSWidgetPackageResolver(PSWidgetPackageSpec packageSpec)
    {
        Validate.notNull(packageSpec);

        tokenMap = new HashMap<>();
        tokenMap.put("WIDGET_PKG_NAME", packageSpec.getPackageName());
        tokenMap.put("PROPERCASE_WIDGET_NAME", packageSpec.getFullWidgetName());
        tokenMap.put("WIDGET_VERSION", packageSpec.getWidgetVersion());
        tokenMap.put("WIDGET_TITLE", packageSpec.getTitle());
        tokenMap.put("WIDGET_DESCRIPTION", packageSpec.getDescription());
        tokenMap.put("WIDGET_AUTHOR", packageSpec.getAuthorUrl());
        tokenMap.put("WIDGET_AUTHOR_URL", packageSpec.getAuthorUrl());
        tokenMap.put("UPPERCASE_WIDGET_NAME", packageSpec.getFullWidgetName().toUpperCase());
        tokenMap.put("CM1_VERSION", packageSpec.getCm1Version());
        tokenMap.put("WIDGET_HTML", packageSpec.getWidgetHtml()); 
        tokenMap.put("FIELD_BINDINGS", generateFieldBindings(packageSpec.getFields()));
        tokenMap.put("IS_RESPONSIVE", Boolean.toString(packageSpec.isResponsive()));
        optionalTokens = new HashSet<>();
        optionalTokens.add("WIDGET_DESCRIPTION");
        String defaultToolTipMessage="This widget is showing sample content";
        String defaultIconPath="/rx_resources/widgets/"+packageSpec.getFullWidgetName()+"/images/"+packageSpec.getFullWidgetName()+"Icon.png";
        if(StringUtils.isNotBlank(packageSpec.getTooTipMessage())){
            defaultToolTipMessage=packageSpec.getTooTipMessage();
        }
        if(StringUtils.isNotBlank(packageSpec.getWidgetTrayCustomizedIconPath())){
            defaultIconPath=packageSpec.getWidgetTrayCustomizedIconPath();
        }
        tokenMap.put("WIDGET_TOOLTIP_MESSAGE", defaultToolTipMessage);
        tokenMap.put("WIDGET_TRAY_CUSTOMIZED_ICON_PATH", defaultIconPath);
        this.packageSpec = packageSpec;
    }
    
    private String generateFieldBindings(List<PSWidgetBuilderFieldData> fields)
    {
        if (fields == null)
            return "";
        
        String result = "";
        for (PSWidgetBuilderFieldData field : fields)
        {
            result += generateBinding(field);
        }
        
        return result;
    }

    public String generateBinding(PSWidgetBuilderFieldData field)
    {
        for (IPSBindingGenerator generator : bindingGenerators)
        {
            if (generator.accept(field))
            {
                return generator.generateBinding(field);
            }
        }
        throw new RuntimeException("No binding generator found for field with type: " + field.getType());
    }

    @Override
    public String resolveToken(String tokenName)
    {
        String tokenVal = tokenMap.get(tokenName);
        
        // if not found, see if the token has been added to the packageSpec.
        if (tokenVal == null)
            tokenVal = packageSpec.getResolverTokenMap().get(tokenName);
        
        if (StringUtils.isBlank(tokenVal))
        {
            if (optionalTokens.contains(tokenName))
                tokenVal = " ";
            else
                throw new IllegalStateException("Null or empty value for token: " + tokenName);
        }
        return tokenVal;
    }

}
