package com.percussion.rest.templates;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.ArrayList;
import java.util.Collection;

@XmlRootElement(name = "TemplateSummaryList")
@ArraySchema(schema = @Schema(implementation = TemplateSummary.class))
@XmlSeeAlso({TemplateSummary.class})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemplateSummaryList extends ArrayList<TemplateSummary> {
    public TemplateSummaryList(Collection<? extends TemplateSummary> c) {
        super(c);
    }
    public TemplateSummaryList(){}
}
