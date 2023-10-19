package com.percussion.rest.templates;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents a lightweight summary of a template.
 */
@XmlRootElement(name="TemplateSummary")
@JsonRootName(value="TemplateSummary")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder()
@XmlType(propOrder = {})
@Schema(description = "Represents a summary of a Template")
public class TemplateSummary {

    @Schema(description = "The numeric template id")
    private int templateId;

    @Schema(description = "The system unique name of the template")
    private String templateName;

    @Schema(description = "The user friendly label for the template")
    private String templateLabel;

    @Schema(description = "A brief description of the template.")
    private String templateDescription;

    public int getTemplateId() {
        return templateId;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateLabel() {
        return templateLabel;
    }

    public void setTemplateLabel(String templateLabel) {
        this.templateLabel = templateLabel;
    }

    public String getTemplateDescription() {
        return templateDescription;
    }

    public void setTemplateDescription(String templateDescription) {
        this.templateDescription = templateDescription;
    }
}
