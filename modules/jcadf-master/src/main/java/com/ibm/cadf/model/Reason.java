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

public class Reason extends CADFType
{

    private static final long serialVersionUID = 1L;

    private String reasonType;

    private String reasonCode;

    private String policyType;

    private String policyId;

    public Reason(String reasonType, String reasonCode, String policyType, String policyId) throws CADFException
    {
        super();
        this.reasonType = reasonType;
        this.reasonCode = reasonCode;
        this.policyType = policyType;
        this.policyId = policyId;
    }

    public String getReasonType()
    {
        return reasonType;
    }

    public void setReasonType(String reasonType)
    {
        this.reasonType = reasonType;
    }

    public String getReasonCode()
    {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode)
    {
        this.reasonCode = reasonCode;
    }

    public String getPolicyType()
    {
        return policyType;
    }

    public void setPolicyType(String policyType)
    {
        this.policyType = policyType;
    }

    public String getPolicyId()
    {
        return policyId;
    }

    public void setPolicyId(String policyId)
    {
        this.policyId = policyId;
    }

    @Override
    public boolean isValid()
    {
        return (StringUtils.isNotEmpty(reasonType) && StringUtils.isNotEmpty(reasonCode))
               || (StringUtils.isNotEmpty(policyType) && StringUtils.isNotEmpty(policyId));
    }

}
