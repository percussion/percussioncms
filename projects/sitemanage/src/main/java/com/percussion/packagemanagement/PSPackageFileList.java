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
package com.percussion.packagemanagement;

import com.percussion.share.dao.PSSerializerUtils;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author JaySeletz
 *
 */
@XmlRootElement(name="PackageFileList")
public class PSPackageFileList
{
    private List<PSPackageFileEntry> entries;
    
    @XmlElement(name="PackageFileEntry")
    public List<PSPackageFileEntry> getEntries()
    {
        return entries;
    }
    public void setEntries(List<PSPackageFileEntry> entries)
    {
        this.entries = entries;
    }
    
    public static PSPackageFileList fromXml(String xmlString)
    {
        return PSSerializerUtils.unmarshal(xmlString, PSPackageFileList.class);
    }
    
    public String toXml()
    {
        return PSSerializerUtils.marshal(this);
    }
}
