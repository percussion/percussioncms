package com.percussion.rest.templates;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="TemplateFilter")
@JsonRootName(value="TemplateFilter")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder()
@XmlType(propOrder = {})
@Schema(description="Represents a filter that can be used to query available Templates. Filter params with 0 or empty values are ignored.")
public class TemplateFilter {

    @Schema(description="The id of the site")
    private int siteId;
    @Schema(description="The id of the content item")
    private int contentId;

    @Schema(description="The id of the community")
    private int communityId;

    @Schema(description="The long id of the workflow")
    private int workflowId;

    @Schema(description="The id of the content type")
    private int contentTypeId;

    public int getContentTypeId() {
        return contentTypeId;
    }

    public void setContentTypeId(int contentTypeId) {
        this.contentTypeId = contentTypeId;
    }

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public int getContentId() {
        return contentId;
    }

    public void setContentId(int contentId) {
        this.contentId = contentId;
    }

    public int getCommunityId() {
        return communityId;
    }

    public void setCommunityId(int communityId) {
        this.communityId = communityId;
    }

    public int getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(int workflowId) {
        this.workflowId = workflowId;
    }
}
