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

package com.percussion.sitemanage.servlet;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * A mutable http servlet request.
 */
public class PSServletRequestWrapper extends HttpServletRequestWrapper
{
    private Map<String, String[]> wrappedparams = new LinkedHashMap<>();

    /**
     * @param request
     */
    public PSServletRequestWrapper(HttpServletRequest request)
    {
        super(request);
        wrappedparams.putAll(super.getParameterMap());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
     */
    @Override
    public String getParameter(String name)
    {
        String[] p = getParameterValues(name);
        if (p == null || p.length == 0)
            return null;
        return p[0];
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequestWrapper#getParameterMap()
     */
    @Override
    public Map getParameterMap()
    {
        return Collections.unmodifiableMap(getMergedParameters());
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequestWrapper#getParameterNames()
     */
    @Override
    public Enumeration getParameterNames()
    {
        return Collections.enumeration(getMergedParameters().keySet());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.servlet.ServletRequestWrapper#getParameterValues(java.lang.
     * String)
     */
    @Override
    public String[] getParameterValues(String name)
    {
        return getMergedParameters().get(name);
    }

    /**
     * @param params the params to set
     */
    public void setParameterMap(Map<String, String[]> params)
    {
        this.wrappedparams = params;
    }

    /**
     * Merge the superclass and local parameters together, the local
     * parameters overwrite the super parameters.
     * 
     * @return merged parameter map, never <code>null</code>, may be empty.
     */
    private Map<String, String[]> getMergedParameters()
    {
        Map<String, String[]> mergedparams = new LinkedHashMap<>(super.getParameterMap());
        mergedparams.putAll(wrappedparams);
        return mergedparams;
    }

}
