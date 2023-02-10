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

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author JaySeletz
 *
 */
@XmlRootElement(name="PackageFileEntry")
public class PSPackageFileEntry
{
    private String packageName;
    private PackageFileStatus status;
    
    public String getPackageName()
    {
        return packageName;
    }
    
    public void setPackageName(String packageName)
    {
        this.packageName = packageName;
    }
    
    public PackageFileStatus getStatus()
    {
        return status;
    }
    
    public void setStatus(PackageFileStatus status)
    {
        this.status = status;
    }
    
    public enum PackageFileStatus
    {
        FAILED,
        INSTALLED,
		REVERT,
        UNINSTALL,
        PENDING
    }
}
