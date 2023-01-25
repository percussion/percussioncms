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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="CategoryConfig")
public class PSCategoryConfiguration
{
    private Tree tree;
    
    @XmlElement(name="tree")
    public Tree getTree()
    {
        return tree;
    }
    public void setTree(Tree tree)
    {
        this.tree = tree;
    }

    public static class Tree 
    {
        private String url;

        @XmlAttribute(name="url")
        public String getUrl()
        {
            return url;
        }
        public void setUrl(String url)
        {
            this.url = url;
        }
        
    }

}
