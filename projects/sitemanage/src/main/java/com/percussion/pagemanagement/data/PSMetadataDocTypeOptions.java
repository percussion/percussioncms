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
package com.percussion.pagemanagement.data;

import com.percussion.share.data.PSAbstractDataObject;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

/**
 * This class contains the structure of the doc type options for templates. The
 * object is composed of a option name and the value for it.
 * 
 * @author leonardohildt
 * 
 */
@XmlRootElement(name = "Options")
public class PSMetadataDocTypeOptions extends PSAbstractDataObject
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String option;

    private String value;

    /**
     * Constructor
     */
    public PSMetadataDocTypeOptions()
    {
        super();
    }

    /**
     * @param option
     * @param value
     */
    public PSMetadataDocTypeOptions(String option, String value)
    {
        this.option = option;
        this.value = value;
    }

    /**
     * @return the option name
     */
    public String getOption()
    {
        return option;
    }

    /**
     * @param option the option name to set
     */
    public void setOption(String option)
    {
        this.option = option;
    }

    /**
     * @return the value
     */
    public String getValue()
    {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value)
    {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSMetadataDocTypeOptions)) return false;
        PSMetadataDocTypeOptions that = (PSMetadataDocTypeOptions) o;
        return Objects.equals(getOption(), that.getOption()) && Objects.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOption(), getValue());
    }
}
