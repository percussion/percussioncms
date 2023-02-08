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
