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
package com.percussion.utils.data;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author miltonpividori
 *
 */
@XmlRootElement(name = "PrivateKeys")
public class PSPrivateKeysResponse
{
    private List<String> keyNames;

    public PSPrivateKeysResponse()
    {
        
    }
    
    public PSPrivateKeysResponse(List<String> keyNames)
    {
        this.keyNames = keyNames;
    }

    public List<String> getKeyNames()
    {
        return keyNames;
    }

    public void setKeyNames(List<String> keyNames)
    {
        this.keyNames = keyNames;
    }
}
