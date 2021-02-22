/*
 * Copyright 2016 IBM Corp.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.ibm.cadf.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ibm.cadf.exception.CADFException;

public class Metric extends CADFType
{

    private static final long serialVersionUID = 1L;

    private String metricId;

    private String unit;

    private String name;

    private Map<String, String> annotations;

    public Metric(String metricId, String unit, String name) throws CADFException
    {
        super();
        this.metricId = metricId;
        this.unit = unit;
        this.name = name;
    }

    public String getMetricId()
    {
        return metricId;
    }

    public void setMetricId(String metricId)
    {
        this.metricId = metricId;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void addAnnotation(String key, String value)
    {
        if (this.annotations == null)
        {
            this.annotations = new HashMap<>();
        }
        this.annotations.put(key, value);
    }

    @Override
    public boolean isValid()
    {
        return StringUtils.isNotEmpty(metricId) && StringUtils.isNotEmpty(unit);
    }
}
