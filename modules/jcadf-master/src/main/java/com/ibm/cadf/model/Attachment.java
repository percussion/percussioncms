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

public class Attachment extends com.ibm.cadf.model.CADFType
{

    private static final long serialVersionUID = 1L;

    private String typeURI;

    private String content;

    private String name;

    private String contentType;

    public Attachment(String contentType, String content, String name) throws CADFException
    {
        super();
        this.contentType = contentType;
        this.content = content;
        this.name = name;
    }

    public String getTypeURI()
    {
        return typeURI;
    }

    public void setTypeURI(String typeURI)
    {
        this.typeURI = typeURI;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getContentType()
    {
        return contentType;
    }

    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    @Override
    public boolean isValid() throws CADFException
    {
        // Validation to ensure Attachment required attributes are set.

        boolean missingMandatoryField = false;
        StringBuffer missingMadatoryFields = new StringBuffer();
        if (StringUtils.isEmpty(contentType))
        {
            missingMandatoryField = true;
            missingMadatoryFields.append("contentType");
        }

        if (StringUtils.isEmpty(content))
        {
            if (missingMandatoryField)
            {
                missingMadatoryFields.append(",");
            }
            else
            {
                missingMandatoryField = true;
            }
            missingMadatoryFields.append("content");
        }

        if (StringUtils.isEmpty(name))
        {
            if (missingMandatoryField)
            {
                missingMadatoryFields.append(",");
            }
            else
            {
                missingMandatoryField = true;
            }
            missingMadatoryFields.append("name");
        }

        // Validation to ensure FederatedCredential required attributes are set.
        if (!missingMandatoryField)
            return true;
        else
            throw new CADFException(MessageFormat.format(Messages.MISSING_MANDATORY_FIELDS,
                                                         missingMadatoryFields.toString()));

    }

}
