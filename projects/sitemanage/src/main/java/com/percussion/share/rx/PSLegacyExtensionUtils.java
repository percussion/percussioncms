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
package com.percussion.share.rx;

import static org.apache.commons.lang.Validate.notNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.server.IPSRequestContext;

/**
 * 
 * Utility methods for working with legacy Percussion CM System extensions.
 * Many methods convert parameter objects like {@link IPSRequestContext}
 * into a {@link Map}.
 * Once you convert parameters to a Map you can take advantage of 
 * <a href="http://commons.apache.org/collections/api/org/apache/commons/collections/MapUtils.html">
 * Apache Commons MapUtils</a>
 * Or Apache Commons BeanUtils.
 * 
 * @author adamgent
 *
 */
public class PSLegacyExtensionUtils
{
    /**
     * Adds the parameters specified by the extension def and contained in the parameters
     * array to the given map.
     * @param paramMap never <code>null</code>.
     * @param def never <code>null</code>.
     * @param params never <code>null</code>.
     */
    public static void addParameters(Map<String, String> paramMap, IPSExtensionDef def, Object[] params) {
        addParameters(paramMap, getParameterNames(def), params);
    }
    
    /**
     * Adds object parameters to a map.
     * @param paramMap self never <code>null</code>.
     * @param parameterNames the keys of the map
     * @param parameters the values of the map.
     */
    public static void addParameters(Map<String, String> paramMap, List<String> parameterNames, Object[] parameters) {
        notNull(paramMap);
        notNull(parameterNames);
        notNull(parameters);
        for (int i = 0; i < parameters.length; i++) {
            if (parameterNames.size() > i) {
                paramMap.put(parameterNames.get(i), parameters[i].toString());
            }
        }
    }
    
    /**
     * Add parameters from request context.
     * @param paramMap never <code>null</code>.
     * @param request never <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    public static void addParameters(Map<String, String> paramMap, IPSRequestContext request) {
        notNull(paramMap);
        notNull(request);
        Iterator<Entry<String, ?>> iterator = request.getParametersIterator();
        while(iterator.hasNext()) {
            String p = iterator.next().getKey();
            paramMap.put(p, request.getParameter(p));
        }
    }
    
    /**
     * Get parameter names from the request.
     * @param request never <code>null</code>.
     * @return never <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    public static List<String> getParameterNames(IPSRequestContext request) {
        notNull(request);
        Iterator<Entry<String, ?>> iterator = request.getParametersIterator();
        List<String> parameterNames = new ArrayList<>();
        while(iterator.hasNext()) {
            String p = iterator.next().getKey();
            parameterNames.add(p);
        }
        return parameterNames;
    }
    
    /**
     * Gets the parameters names from an extension definition.
     * @param extensionDef never <code>null</code>.
     * @return never <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    public static List<String> getParameterNames(IPSExtensionDef extensionDef) {
        notNull(extensionDef);
        List<String> rvalue = new ArrayList<>();
        Iterator<String> it = extensionDef.getRuntimeParameterNames();
        CollectionUtils.addAll(rvalue, it);
        return rvalue;
    }
    

}

