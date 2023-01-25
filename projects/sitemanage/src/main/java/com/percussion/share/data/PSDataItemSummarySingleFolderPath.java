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
package com.percussion.share.data;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

import static java.util.Arrays.asList;

@XmlRootElement
public abstract class PSDataItemSummarySingleFolderPath extends PSDataItemSummary
{
    private static final long serialVersionUID = 6742796878036917020L;


    public String getFolderPath()
    {
        if(getFolderPaths() != null 
                && ! getFolderPaths().isEmpty()) {
            return getFolderPaths().get(0);
        }
        return null;
    }


    public void setFolderPath(String folderPath)
    {
        if (folderPath != null)
            setFolderPaths(asList(folderPath));
        else
            setFolderPaths(new ArrayList<>());
    }
}
