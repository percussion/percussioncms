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
