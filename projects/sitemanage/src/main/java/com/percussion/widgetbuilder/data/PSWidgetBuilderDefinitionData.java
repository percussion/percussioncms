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

package com.percussion.widgetbuilder.data;

import com.percussion.services.widgetbuilder.PSWidgetBuilderDefinition;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import java.util.Objects;

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
     * @param widgetHtml The html, not be <code>null<code/> or empty.
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

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PSWidgetBuilderDefinitionData{");
        sb.append("fieldsList=").append(fieldsList);
        sb.append(", widgetHtml='").append(widgetHtml).append('\'');
        sb.append(", jsFileList=").append(jsFileList);
        sb.append(", cssFileList=").append(cssFileList);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSWidgetBuilderDefinitionData)) return false;
        PSWidgetBuilderDefinitionData that = (PSWidgetBuilderDefinitionData) o;
        return Objects.equals(getFieldsList(), that.getFieldsList()) && Objects.equals(getWidgetHtml(), that.getWidgetHtml()) && Objects.equals(getJsFileList(), that.getJsFileList()) && Objects.equals(getCssFileList(), that.getCssFileList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFieldsList(), getWidgetHtml(), getJsFileList(), getCssFileList());
    }
}

