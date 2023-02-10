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

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.share.data.PSAbstractDataObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class contains the structure of the doc type information for templates. The object is
 * composed of a selected doc type, and a list of
 * {@link PSMetadataDocTypeOptions}.
 * 
 * @author leonardohildt
 * 
 */
@XmlRootElement(name = "DocType")
@JsonRootName("DocType")
public class PSMetadataDocType extends PSAbstractDataObject
{
    private static final long serialVersionUID = 1L;

    private String selected = "";

    private List<PSMetadataDocTypeOptions> options = new ArrayList<>();

    /**
     * Constructor for new doc type. The default doc type set is html5.
     * Used when a new template is added into the system.
     */
    public PSMetadataDocType()
    {
        super();
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(String selected)
    {
        this.selected = selected;
    }

    /**
     * @return the selected type
     */
    public String getSelected()
    {
        return selected;
    }

    /**
     * @return the doc type options, may be empty but never <code>null</code>
     */
    public List<PSMetadataDocTypeOptions> getOptions()
    {
        return this.options;
    }

    /**
     * @param options the doc type options to set, may be empty but never <code>null</code>
     */
    public void setOptions(List<PSMetadataDocTypeOptions> options)
    {
        this.options = options;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSMetadataDocType)) return false;
        PSMetadataDocType that = (PSMetadataDocType) o;
        return Objects.equals(getSelected(), that.getSelected()) && Objects.equals(getOptions(), that.getOptions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSelected(), getOptions());
    }
}
