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

import com.percussion.share.data.PSAbstractDataObject;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "statistics")
public class PSSiteStatistics extends PSAbstractDataObject
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private long binary;

    private long css;

    private long linksInternal;

    private long pages;

    private long templates;

    public long getBinary()
    {
        return binary;
    }

    public void setBinary(long binary)
    {
        this.binary = binary;
    }

    public long getCss()
    {
        return css;
    }

    public void setCss(long css)
    {
        this.css = css;
    }

    public long getLinksInternal()
    {
        return linksInternal;
    }

    public void setLinksInternal(long linksInternal)
    {
        this.linksInternal = linksInternal;
    }

    public long getPages()
    {
        return pages;
    }

    public void setPages(long pages)
    {
        this.pages = pages;
    }

    public long getTemplates()
    {
        return templates;
    }

    public void setTemplates(long templates)
    {
        this.templates = templates;
    }
}
