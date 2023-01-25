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
package com.percussion.sitemanage.data;

import com.percussion.pathmanagement.data.PSItemByWfStateRequest;

import javax.xml.bind.annotation.XmlRootElement;

import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;

/**
 * Request object used for validating a source and destination folder for copy.  Both folder paths are required.  The
 * destination folder may not be empty.
 */
@XmlRootElement(name = "ValidateCopyFoldersRequest")
public class PSValidateCopyFoldersRequest extends PSItemByWfStateRequest
{
    public String getSrcFolder()
    {
        return srcFolder;
    }

    public void setSrcFolder(String srcFolder)
    {
        this.srcFolder = srcFolder;
    }

    public String getDestFolder()
    {
        return destFolder;
    }

    public void setDestFolder(String destFolder)
    {
        this.destFolder = destFolder;
    }

    @NotNull
    private String srcFolder;
    
    @NotNull
    @NotEmpty
    private String destFolder;

}
