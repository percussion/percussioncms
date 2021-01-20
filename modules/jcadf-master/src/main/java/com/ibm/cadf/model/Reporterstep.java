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

import com.ibm.cadf.exception.CADFException;
import com.ibm.cadf.util.TimeStampUtils;

public class Reporterstep extends CADFType
{

    private static final long serialVersionUID = 1L;

    private String role;

    private Resource reporter;

    private String reporterId;

    private String reporterTime;

    public Reporterstep(String role, Resource reporter, String reporterId, String reporterTime) throws CADFException
    {
        super();
        this.role = role;
        this.reporter = reporter;
        this.reporterId = reporterId;
        this.reporterTime = reporterTime;
    }

    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
    }

    public Resource getReporter()
    {
        return reporter;
    }

    public void setReporter(Resource reporter)
    {
        this.reporter = reporter;
    }

    public String getReporterId()
    {
        return reporterId;
    }

    public void setReporterId(String reporterId)
    {
        this.reporterId = reporterId;
    }

    public String getReporterTime()
    {
        return reporterTime;
    }

    public void setReporterTime(String reporterTime)
    {
        this.reporterTime = reporterTime;
    }

    @Override
    public boolean isValid()
    {
        return isValidReporterRoles(role) && TimeStampUtils.isValid(reporterTime);
    }
}
