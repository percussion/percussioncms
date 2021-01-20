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

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;

import com.ibm.cadf.Messages;
import com.ibm.cadf.exception.CADFException;

public class Credential extends CADFType
{

    private static final long serialVersionUID = 1L;

    private String type;

    private String token;

    public Credential(String token) throws CADFException
    {
        super();
        this.token = token;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    @Override
    public boolean isValid() throws CADFException
    {
        // Validation to ensure Credential required attributes are set.
        if (StringUtils.isNotEmpty(this.token))
            return true;
        else
            throw new CADFException(MessageFormat.format(Messages.MISSING_MANDATORY_FIELDS, "token"));
    }

}
