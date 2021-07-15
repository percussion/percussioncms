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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.widgetbuilder.data;

import com.percussion.services.widgetbuilder.PSWidgetBuilderDefinition;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

@XmlRootElement(name="WidgetBuilderDefinitionData")
public class PSWidgetBuilderDefinitionData  extends PSWidgetBuilderSummaryData
{

    private static final long serialVersionUID = -1L;

    private PSWidgetBuilderFieldsListData fieldsList = new PSWidgetBuilderFieldsListData();
    private String widgetHtml;
    private PSWidgetBuilderResourceListData jsFileList = new PSWidgetBuilderResourceListData();
    private PSWidgetBuilderResourceListData cssFileList = new PSWidgetBuilderResourceListData();


    public PSWidgetBuilderDefinitionData()
    {
        super();
    }

    /**
     * Create from dao
     *
     * @param dao The dao to copy from, not <code>null</code>.
     */
    public PSWidgetBuilderDefinitionData(PSWidgetBuilderDefinition dao)
    {
        super(dao);

        if (!StringUtils.isBlank(dao.getFields())) {
            setFieldsList(PSWidgetBuilderFieldsListData.fromXml(dao.getFields()));
        }

        widgetHtml = dao.getWidgetHtml();

        if (!StringUtils.isBlank(dao.getCssFiles())) {
            setCssFileList(PSWidgetBuilderResourceListData.fromXml(dao.getCssFiles()));
        }

        if (!StringUtils.isBlank(dao.getJsFiles())) {
            setJsFileList(PSWidgetBuilderResourceListData.fromXml(dao.getJsFiles()));
        }
    }

    public static PSWidgetBuilderDefinition createDaoObject(PSWidgetBuilderDefinitionData data)
    {
        PSWidgetBuilderDefinition definition = new PSWidgetBuilderDefinition();
        definition.setAuthor(data.getAuthor());
        definition.setDescription(data.getDescription());
        definition.setLabel(data.getLabel());
        definition.setPrefix(data.getPrefix());
        definition.setPublisherUrl(data.getPublisherUrl());
        definition.setVersion(data.getVersion());
        if(data.getWidgetId()>0) {
            definition.setWidgetBuilderDefinitionId(data.getWidgetId());
        }
        definition.setFields(data.getFieldsList().toXml());
        definition.setJsFiles(data.getJsFileList().toXml());
        definition.setCssFiles(data.getCssFileList().toXml());
        definition.setWidgetHtml(data.getWidgetHtml());
        definition.setResponsive(data.isResponsive());
        definition.setWidgetTrayCustomizedIconPath(data.getWidgetTrayCustomizedIconPath());
        definition.setToolTipMessage(data.getToolTipMessage());
        return definition;
    }

    /**
     * Get the list of fields.
     * @return The fields list, never <code>null</code>.
     */
    public PSWidgetBuilderFieldsListData getFieldsList()
    {
        return fieldsList;
    }

    /**
     * Set the list of fields
     *
     * @param fieldsList The fieldlist, not <code>null</code>.
     */
    public void setFieldsList(PSWidgetBuilderFieldsListData fieldsList)
    {
        Validate.notNull(fieldsList);
        this.fieldsList = fieldsList;
    }


    /**
     * Set the html used to render the widget
     *
     * @param html The html, not be <code>null<code/> or empty.
     */
    public void setWidgetHtml(String widgetHtml)
    {
        Validate.notNull(widgetHtml);
        this.widgetHtml = widgetHtml;
    }

    /**
     * Get the html used to render the widget.
     *
     * @return The html, may be <code>null<code/>, not empty.
     */
    public String getWidgetHtml()
    {
        return widgetHtml;
    }

    /**
     * Get the list of js files
     *
     * @return The list, not <code>null</code>.
     */
    public PSWidgetBuilderResourceListData getJsFileList()
    {
        return jsFileList;
    }

    /**
     * Set the list of js files.
     *
     * @param jsFileList The list, not <code>null</code>.
     */
    public void setJsFileList(PSWidgetBuilderResourceListData jsFileList)
    {
        Validate.notNull(jsFileList);
        this.jsFileList = jsFileList;
    }

    /**
     * Get the list of css files
     *
     * @return The list, not <code>null</code>.
     */
    public PSWidgetBuilderResourceListData getCssFileList()
    {
        return cssFileList;
    }

    /**
     * Set the list of css files
     *
     * @param cssFileList The list, not <code>null</code>.
     */
    public void setCssFileList(PSWidgetBuilderResourceListData cssFileList)
    {
        Validate.notNull(cssFileList);
        this.cssFileList = cssFileList;
    }
}

