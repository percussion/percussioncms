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

package com.percussion.rest.templates;

import com.percussion.rest.Guid;
import io.swagger.annotations.ApiModel;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@XmlRootElement(name = "Template")
@ApiModel(value = "Template", description = "Represents an assembly Template")
public class Template {
    /**
     * The template's id
     */
    private Guid id;

    /**
     * The hibernate object version
     */
    private Integer version;

    /**
     * The template name
     */
    private String name;

    /**
     * The template displayed label
     */
    private String label;

    /**
     * The location prefix
     */
    private String locationPrefix;

    /**
     * The location suffix
     */
    private String locationSuffix;

    /**
     * The assembler
     */
    private String assembler;

    /**
     * The assembly url - may reference the template content item or the
     * application depending on the kind of assembler
     */
    private String assemblyUrl;

    /**
     * The stylesheet name
     */
    private String styleSheet;

    /**
     * The aa type
     */
    private int aaType;

    /**
     * The output format
     */
    private int outputFormat;

    /**
     * Publish when value
     */
    private Character publishWhen; //= PublishWhen.Unspecified.getValue();

    private Integer templateType; //= new Integer(TemplateType.Shared.ordinal());

    private String description;

    private String template;

    private String mimeType;

    private String charset;


    private List<TemplateBinding> bindings = new ArrayList<>();

    private Set<TemplateSlot> slots = new HashSet<>();


    private Integer globalTemplateUsage;// = new Integer(GlobalTemplateUsage.None.ordinal());

    private Long globalTemplate;

    public Guid getId() {
        return id;
    }

    public void setId(Guid id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLocationPrefix() {
        return locationPrefix;
    }

    public void setLocationPrefix(String locationPrefix) {
        this.locationPrefix = locationPrefix;
    }

    public String getLocationSuffix() {
        return locationSuffix;
    }

    public void setLocationSuffix(String locationSuffix) {
        this.locationSuffix = locationSuffix;
    }

    public String getAssembler() {
        return assembler;
    }

    public void setAssembler(String assembler) {
        this.assembler = assembler;
    }

    public String getAssemblyUrl() {
        return assemblyUrl;
    }

    public void setAssemblyUrl(String assemblyUrl) {
        this.assemblyUrl = assemblyUrl;
    }

    public String getStyleSheet() {
        return styleSheet;
    }

    public void setStyleSheet(String styleSheet) {
        this.styleSheet = styleSheet;
    }

    public int getAaType() {
        return aaType;
    }

    public void setAaType(int aaType) {
        this.aaType = aaType;
    }

    public int getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(int outputFormat) {
        this.outputFormat = outputFormat;
    }

    public Character getPublishWhen() {
        return publishWhen;
    }

    public void setPublishWhen(Character publishWhen) {
        this.publishWhen = publishWhen;
    }

    public Integer getTemplateType() {
        return templateType;
    }

    public void setTemplateType(Integer templateType) {
        this.templateType = templateType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public List<TemplateBinding> getBindings() {
        return bindings;
    }

    public void setBindings(List<TemplateBinding> bindings) {
        this.bindings = bindings;
    }

    public Set<TemplateSlot> getSlots() {
        return slots;
    }

    public void setSlots(Set<TemplateSlot> slots) {
        this.slots = slots;
    }

    public Integer getGlobalTemplateUsage() {
        return globalTemplateUsage;
    }

    public void setGlobalTemplateUsage(Integer globalTemplateUsage) {
        this.globalTemplateUsage = globalTemplateUsage;
    }

    public Long getGlobalTemplate() {
        return globalTemplate;
    }

    public void setGlobalTemplate(Long globalTemplate) {
        this.globalTemplate = globalTemplate;
    }

    public Template(){}
}
