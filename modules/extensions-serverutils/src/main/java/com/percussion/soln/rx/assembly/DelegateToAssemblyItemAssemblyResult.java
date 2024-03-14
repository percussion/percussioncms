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

package com.percussion.soln.rx.assembly;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.jcr.Node;

import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.impl.nav.PSNavHelper;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.PSFilterException;
import com.percussion.utils.guid.IPSGuid;

public abstract class DelegateToAssemblyItemAssemblyResult implements IPSAssemblyResult {
    
    /**
     * Safe to serialize
     */
    private static final long serialVersionUID = 1L;
    
    private IPSAssemblyItem assemblyItem;
    
    public IPSAssemblyItem getAssemblyItem() {
        return assemblyItem;
    }

    public void setAssemblyItem(IPSAssemblyItem assemblyItem) {
        this.assemblyItem = assemblyItem;
    }
    
    
    /*
     * 
     * DELEGATE METHODS
     * 
     * 
     */
    public Map<String, Object> getBindings() {
        return assemblyItem.getBindings();
    }

    public IPSAssemblyItem getCloneParentItem() {
        return assemblyItem.getCloneParentItem();
    }

    public IPSItemFilter getFilter() throws PSFilterException {
        return assemblyItem.getFilter();
    }

    public int getFolderId() {
        return assemblyItem.getFolderId();
    }

    public IPSGuid getId() {
        return assemblyItem.getId();
    }

    public long getJobId() {
        return assemblyItem.getJobId();
    }

    public Node getNode() {
        return assemblyItem.getNode();
    }

    public Map<String, String[]> getParameters() {
        return assemblyItem.getParameters();
    }

    public String getParameterValue(String name, String defaultvalue) {
        return assemblyItem.getParameterValue(name, defaultvalue);
    }

    public String[] getParameterValues(String name, String[] defaultvalues) {
        return assemblyItem.getParameterValues(name, defaultvalues);
    }

    public String getPath() {
        return assemblyItem.getPath();
    }


    public long getReferenceId() {
        return assemblyItem.getReferenceId();
    }

    public IPSGuid getSiteId() {
        return assemblyItem.getSiteId();
    }

    public IPSAssemblyTemplate getTemplate() {
        return assemblyItem.getTemplate();
    }

    public String getUserName() {
        return assemblyItem.getUserName();
    }

    public Map<String, String> getVariables() {
        return assemblyItem.getVariables();
    }

    public boolean hasNode() {
        return assemblyItem.hasNode();
    }

    public boolean hasParameter(String name) {
        return assemblyItem.hasParameter(name);
    }

    public boolean isDebug() {
        return assemblyItem.isDebug();
    }

    public boolean isPublish() {
        return assemblyItem.isPublish();
    }

    public void normalize() throws PSAssemblyException {
        assemblyItem.normalize();
    }

    public void removeParameterValue(String name) {
        assemblyItem.removeParameterValue(name);
    }

    public void setBindings(Map<String, Object> bindings) {
        assemblyItem.setBindings(bindings);
    }

    public void setDebug(boolean isDebug) {
        assemblyItem.setDebug(isDebug);
    }

    public void setFilter(IPSItemFilter filter) {
        assemblyItem.setFilter(filter);
    }

    public void setFolderId(int folderId) {
        assemblyItem.setFolderId(folderId);
    }

    public void setId(IPSGuid id) {
        assemblyItem.setId(id);
    }

    public void setJobId(long jobId) {
        assemblyItem.setJobId(jobId);
    }

    public void setNode(Node node) {
        assemblyItem.setNode(node);
    }

    public void setParameters(Map<String, String[]> parameters) {
        assemblyItem.setParameters(parameters);
    }

    public void setParameterValue(String name, String value) {
        assemblyItem.setParameterValue(name, value);
    }

    public void setPath(String path) {
        assemblyItem.setPath(path);
    }

    public void setPublish(boolean pub) {
        assemblyItem.setPublish(pub);
    }

    public void setReferenceId(int referenceId) {
        assemblyItem.setReferenceId(referenceId);
    }

    public void setSiteId(IPSGuid siteid) {
        assemblyItem.setSiteId(siteid);
    }

    public void setTemplate(IPSAssemblyTemplate template) {
        assemblyItem.setTemplate(template);
    }

    public void setUserName(String userName) {
        assemblyItem.setUserName(userName);
    }

    public void setVariables(Map<String, String> variables) {
        assemblyItem.setVariables(variables);
    }

    public Object clone() {
        return assemblyItem.clone();
    }


    public Integer getPage() {
        return assemblyItem.getPage();
    }

    public Long getParentPageReferenceId() {
        return assemblyItem.getParentPageReferenceId();
    }

    public void setPage(Integer page) {
        assemblyItem.setPage(page);
    }

    public void setParentPageReferenceId(long refid) {
        assemblyItem.setParentPageReferenceId(refid);
    }

    public void setReferenceId(long referenceId) {
        assemblyItem.setReferenceId(referenceId);
    }

    public IPSGuid getOriginalTemplateGuid() {
        return assemblyItem.getOriginalTemplateGuid();
    }

    public Long getUnpublishRefId() {
        return assemblyItem.getUnpublishRefId();
    }
    
    /*
     * Bens added method.
     */
    public abstract void clearResults();

    public String getAssemblyUrl() {
        return assemblyItem.getAssemblyUrl();
    }

    public int getContext() {
        return assemblyItem.getContext();
    }

    public int getDeliveryContext() {
        return assemblyItem.getDeliveryContext();
    }

    public IPSGuid getDeliveryContextId() {
        return assemblyItem.getDeliveryContextId();
    }

    public String getDeliveryPath() {
        return assemblyItem.getDeliveryPath();
    }

    public String getDeliveryType() {
        return assemblyItem.getDeliveryType();
    }

    public int getElapsed() {
        return assemblyItem.getElapsed();
    }

    public PSNavHelper getNavHelper() {
        return assemblyItem.getNavHelper();
    }

    public IPSAssemblyItem pageClone() {
        return assemblyItem.pageClone();
    }

    public void removeParameter(String arg0) {
        assemblyItem.removeParameter(arg0);
    }

    public void setAssemblyUrl(String arg0) {
        assemblyItem.setAssemblyUrl(arg0);
    }

    public void setDeliveryContext(int arg0) {
        assemblyItem.setDeliveryContext(arg0);
    }

    public void setDeliveryPath(String arg0) {
        assemblyItem.setDeliveryPath(arg0);
    }

    public void setDeliveryType(String arg0) {
        assemblyItem.setDeliveryType(arg0);
    }

    public void setElapsed(int arg0) {
        assemblyItem.setElapsed(arg0);
    }

    public void setMimeType(String arg0) {
        assemblyItem.setMimeType(arg0);
    }

    public void setOriginalTemplateGuid(IPSGuid arg0) {
        assemblyItem.setOriginalTemplateGuid(arg0);
    }

    public void setPaginated(boolean paginate) {
        assemblyItem.setPaginated(paginate);
    }

    public void setResultData(byte[] data) throws IOException {
        assemblyItem.setResultData(data);
    }

    public void setResultStream(InputStream arg0) throws IOException {
        assemblyItem.setResultStream(arg0);
    }

    
    public void setStatus(Status status) {
        assemblyItem.setStatus(status);
    }

    public void setUnpublishRefId(Long arg0) {
        assemblyItem.setUnpublishRefId(arg0);
    }


}
