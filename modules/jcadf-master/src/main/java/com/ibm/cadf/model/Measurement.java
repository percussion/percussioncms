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

import org.apache.commons.lang3.StringUtils;

import com.ibm.cadf.exception.CADFException;

public class Measurement extends CADFType
{
    private static final long serialVersionUID = 1L;

    private String result;

    private Metric metric;

    private String metricId;

    private Resource calculatedBy;

    public Measurement(String result, Metric metric, String metricId) throws CADFException
    {
        super();
        this.result = result;
        this.metric = metric;
        this.metricId = metricId;
    }

    public String getResult()
    {
        return result;
    }

    public void setResult(String result)
    {
        this.result = result;
    }

    public Metric getMetric()
    {
        return metric;
    }

    public void setMetric(Metric metric)
    {
        this.metric = metric;
    }

    public String getMetricId()
    {
        return metricId;
    }

    public void setMetricId(String metricId)
    {
        this.metricId = metricId;
    }

    public Resource getCalculatedBy()
    {
        return calculatedBy;
    }

    public void setCalculatedBy(Resource calculatedBy)
    {
        this.calculatedBy = calculatedBy;
    }

    @Override
    public boolean isValid()
    {
        return StringUtils.isNotEmpty(result) && (metric != null ^ StringUtils.isNotEmpty(metricId));
    }

}
