/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.sitemanage.data;

import org.jdom2.CDATA;
import org.jdom2.Element;
import org.jsoup.nodes.Document;

public class PSPageContent
{
    private String title;

    private String path;

    private String beforeBodyClose;

    private String afterBodyStart;

    private String headContent;

    private String bodyContent;
    
    private String description = "";

    private Document sourceDocument;

    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @return the path
     */
    public String getPath()
    {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    /**
     * @return the beforeBodyClose
     */
    public String getBeforeBodyClose()
    {
        return beforeBodyClose;
    }

    /**
     * @param beforeBodyClose the beforeBodyClose to set
     */
    public void setBeforeBodyClose(String beforeBodyClose)
    {
        this.beforeBodyClose = beforeBodyClose;
    }

    /**
     * @return the afterBodyStart
     */
    public String getAfterBodyStart()
    {
        return afterBodyStart;
    }

    /**
     * @param afterBodyStart the afterBodyStart to set
     */
    public void setAfterBodyStart(String afterBodyStart)
    {
        this.afterBodyStart = afterBodyStart;
    }

    /**
     * @return the headContent
     */
    public String getHeadContent()
    {
        return headContent;
    }

    /**
     * @param headContent the headContent to set
     */
    public void setHeadContent(String headContent)
    {
        this.headContent = headContent;
    }

    /**
     * @return the bodyContent
     */
    public String getBodyContent()
    {
        return bodyContent;
    }

    /**
     * @param bodyContent the bodyContent to set
     */
    public void setBodyContent(String bodyContent)
    {
        this.bodyContent = bodyContent;
    }

    /**
     * @param sourceDocument the sourceDocument to set
     */
    public void setSourceDocument(Document sourceDocument)
    {
        this.sourceDocument = sourceDocument;
    }

    /**
     * @return the sourceDocument
     */
    public Document getSourceDocument()
    {
        return sourceDocument;
    }


    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
    
    public Element toXml()
    {
        Element elem = new Element("SiteDetails");
        elem.addContent(createElem("Path", path, false));
        elem.addContent(createElem("Title", title, false));
        elem.addContent(createElem("HeadContent", headContent, false));
        elem.addContent(createElem("AfterBodyStart", afterBodyStart, false));
        elem.addContent(createElem("BeforeBodyClose", beforeBodyClose, false));
        elem.addContent(createElem("BodyContent", bodyContent, false));
        elem.addContent(createElem("Description", description, false));
        return elem;
    }

    private Element createElem(String name, String value, boolean encloseCdata)
    {
        Element elem = new Element(name);
        if (encloseCdata)
            elem.addContent(new CDATA(value));
        else
            elem.setText(value);
        return elem;
    }

}
