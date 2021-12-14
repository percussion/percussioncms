/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.security.xml;

/**
 * Allows for the setting of XML security options.
 */
public class PSXmlSecurityOptions {
    public PSXmlSecurityOptions(boolean enableExternalEntities, boolean enableDtdDeclarations, boolean enableExternalDtdReferences, boolean enableSecureProcessing, boolean enableExternalParameterEntities, boolean enableValidation) {
        this.enableExternalEntities = enableExternalEntities;
        this.enableDtdDeclarations = enableDtdDeclarations;
        this.enableExternalDtdReferences = enableExternalDtdReferences;
        this.enableSecureProcessing = enableSecureProcessing;
        this.enableExternalParameterEntities = enableExternalParameterEntities;
        this.enableValidation = enableValidation;
    }

    private boolean enableExternalEntities;
    private boolean enableDtdDeclarations;
    private boolean enableExternalDtdReferences;
    private boolean enableSecureProcessing;
    private boolean enableExternalParameterEntities;
    private boolean enableValidation;


    private String[] allowedPathsForDtdDeclarations;
    private String[] allowedPathsForExternalEntities;
    private String[] allowedPathsForImports;
    private String[] allowedPathsForIncludes;

    public boolean isEnableExternalEntities() {
        return enableExternalEntities;
    }

    public void setEnableExternalEntities(boolean enableExternalEntities) {
        this.enableExternalEntities = enableExternalEntities;
    }

    public boolean isEnableDtdDeclarations() {
        return enableDtdDeclarations;
    }

    public void setEnableDtdDeclarations(boolean enableDtdDeclarations) {
        this.enableDtdDeclarations = enableDtdDeclarations;
    }

    public boolean isEnableExternalDtdReferences() {
        return enableExternalDtdReferences;
    }

    public void setEnableExternalDtdReferences(boolean enableExternalDtdReferences) {
        this.enableExternalDtdReferences = enableExternalDtdReferences;
    }

    public boolean isEnableSecureProcessing() {
        return enableSecureProcessing;
    }

    public void setEnableSecureProcessing(boolean enableSecureProcessing) {
        this.enableSecureProcessing = enableSecureProcessing;
    }

    public boolean isEnableExternalParameterEntities() {
        return enableExternalParameterEntities;
    }

    public void setEnableExternalParameterEntities(boolean enableExternalParameterEntities) {
        this.enableExternalParameterEntities = enableExternalParameterEntities;
    }

    public boolean isEnableValidation() {
        return enableValidation;
    }

    public void setEnableValidation(boolean enableValidation) {
        this.enableValidation = enableValidation;
    }

    public String[] getAllowedPathsForDtdDeclarations() {
        return allowedPathsForDtdDeclarations;
    }

    public void setAllowedPathsForDtdDeclarations(String[] allowedPathsForDtdDeclarations) {
        this.allowedPathsForDtdDeclarations = allowedPathsForDtdDeclarations;
    }

    public String[] getAllowedPathsForExternalEntities() {
        return allowedPathsForExternalEntities;
    }

    public void setAllowedPathsForExternalEntities(String[] allowedPathsForExternalEntities) {
        this.allowedPathsForExternalEntities = allowedPathsForExternalEntities;
    }

    public String[] getAllowedPathsForImports() {
        return allowedPathsForImports;
    }

    public void setAllowedPathsForImports(String[] allowedPathsForImports) {
        this.allowedPathsForImports = allowedPathsForImports;
    }

    public String[] getAllowedPathsForIncludes() {
        return allowedPathsForIncludes;
    }

    public void setAllowedPathsForIncludes(String[] allowedPathsForIncludes) {
        this.allowedPathsForIncludes = allowedPathsForIncludes;
    }
}
