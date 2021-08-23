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

package com.percussion.rest.extensions;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "Extension")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Represents an Extension")
public class Extension {

    @Schema(name = "handlerName", required = false, description = "The extension handler name")
    private String handlerName;

    @Schema(name="context", required = false, description = "The extension context")
    private String context;

    @Schema(name="extensionName",  required = false, description = "The extension name")
    private String extensionName;

    @Schema(name="category",  required = false, description = "The Category of the extension")
    private String category = "";

    @Schema(name="fqn",  required = false, description = "The fully qualified name for the extension")
    private String fqn;

    @Schema(name="version",  description="The version of the extension")
    private long version;

    @Schema(name="deprecated", description="When true, this extension has been deprecated.")
    private boolean deprecated;

    @Schema(name="restoreRequestParamsOnError",  description="When true if an error occurs on processing the original request parameters will be restored.")
    private boolean restoreRequestParamsOnError;

    @Schema(name="jexlExtension",  description="When true, this extension is a Jexl extension.")
    private boolean jexlExtension;

    @Schema(name="suppliedResources",  description = "A list of urls pointing to resources supplied by the Extension")
    private List<String> suppliedResources;

    @Schema(name="resourceLocations",  description = "A list of urls pointing to resources locations the Extension")
    private List<String> resourceLocations;

    @Schema(name="supportedInterfaces",  description = "A list of Java interfaces supported by this Extension")
    private List<String> supportedInterfaces;

    @Schema(name="runtimeParameters",  description = "A list of ExtensionParameter objects required by the extension")
    private List<ExtensionParameter> runtimeParameters;

    @Schema(name="initParameters",  description = "A map of key value pairs indicating the initParameters used to initialize the Extension")
    private Map<String,String> initParameters;

    @Schema(name="requiredApplications", description = "A list of Extension names that this Extension depends on to function correctly")
    private List<String> requiredApplications;

    @Schema(name="methods", description = "A list of ExtensionMethods provided by this extension")
    private Map<String, ExtensionMethod> methods;

    public Extension(){}

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getExtensionName() {
        return extensionName;
    }

    public void setExtensionName(String extensionName) {
        this.extensionName = extensionName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    public String getFqn() {
        return fqn;
    }

    public void setFqn(String fqn){
        this.fqn = fqn;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public boolean isRestoreRequestParamsOnError() {
        return restoreRequestParamsOnError;
    }

    public void setRestoreRequestParamsOnError(boolean restoreRequestParamsOnError) {
        this.restoreRequestParamsOnError = restoreRequestParamsOnError;
    }

    public boolean isJexlExtension() {
        return jexlExtension;
    }

    public void setJexlExtension(boolean jexlExtension) {
        this.jexlExtension = jexlExtension;
    }

    public List<String> getSuppliedResources() {
        return suppliedResources;
    }

    public void setSuppliedResources(List<String> suppliedResources) {
        this.suppliedResources = suppliedResources;
    }

    public List<String> getResourceLocations() {
        return resourceLocations;
    }

    public void setResourceLocations(List<String> resourceLocations) {
        this.resourceLocations = resourceLocations;
    }

    public List<String> getSupportedInterfaces() {
        return supportedInterfaces;
    }

    public void setSupportedInterfaces(List<String> supportedInterfaces) {
        this.supportedInterfaces = supportedInterfaces;
    }

    public List<ExtensionParameter> getRuntimeParameters() {
        return runtimeParameters;
    }

    public void setRuntimeParameters(List<ExtensionParameter> runtimeParameters) {
        this.runtimeParameters = runtimeParameters;
    }

    public Map<String, String> getInitParameters() {
        return initParameters;
    }

    public void setInitParameters(Map<String, String> initParameters) {
        this.initParameters = initParameters;
    }

    public List<String> getRequiredApplications() {
        return requiredApplications;
    }

    public void setRequiredApplications(List<String> requiredApplications) {
        this.requiredApplications = requiredApplications;
    }

    public Map<String, ExtensionMethod> getMethods() {
        return methods;
    }

    public void setMethods(Map<String, ExtensionMethod> methods) {
        this.methods = methods;
    }
}
