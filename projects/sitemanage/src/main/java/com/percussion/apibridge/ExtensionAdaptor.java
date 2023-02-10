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

package com.percussion.apibridge;

import com.percussion.extension.*;
import com.percussion.extensions.IPSExtensionService;
import com.percussion.rest.extensions.*;
import com.percussion.util.PSSiteManageBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.net.URL;
import java.util.*;

@PSSiteManageBean
public class ExtensionAdaptor implements IExtensionAdaptor {

    private IPSExtensionService extensionService;
    private Logger log = LogManager.getLogger(ExtensionAdaptor.class);


    @Autowired
    public ExtensionAdaptor(IPSExtensionService extensionService){
        this.extensionService = extensionService;
    }

    private Extension copyExtensionRef(PSExtensionRef ref){
        Extension ret = new Extension();
        ret.setCategory(ref.getCategory());
        ret.setContext(ref.getContext());
        ret.setExtensionName(ret.getExtensionName());
        ret.setHandlerName(ref.getHandlerName());

        try {
            IPSExtensionDef def = extensionService.getExtensionDef(ref);

            ret.setDeprecated(def.isDeprecated());
            ret.setJexlExtension(def.isJexlExtension());
            ret.setVersion(def.getVersion());

            //copy interfaces
            Iterator it = def.getInterfaces();
            List interfaces = new ArrayList();
            it.forEachRemaining(interfaces::add);
            ret.setSupportedInterfaces(interfaces);

            //Init params
            it = def.getInitParameterNames();
            Map<String,String> initParams = new HashMap();
            while(it.hasNext()){
                String name = (String)it.next();
                initParams.put(name,def.getInitParameter(name));
            }
            ret.setInitParameters(initParams);
            Map<String, ExtensionMethod> methods = new HashMap();
             it = def.getMethods();
             while(it.hasNext()) {
                 PSExtensionMethod defMethod = (PSExtensionMethod) it.next();
                 ExtensionMethod meth = new ExtensionMethod();
                 meth.setName(defMethod.getName());
                 meth.setDescription(defMethod.getDescription());

                 Iterator itParams = defMethod.getParameters();
                 List<ExtensionParameter> methParams = new ArrayList();
                 while (itParams.hasNext()) {
                     ExtensionParameter ep = new ExtensionParameter();
                     PSExtensionMethodParam emp = (PSExtensionMethodParam) itParams.next();
                     ep.setDataType(emp.getType());
                     ep.setDescription(emp.getDescription());
                     ep.setName(emp.getName());
                     methParams.add(ep);
                 }
                 meth.setParameters(methParams);
                 methods.put(defMethod.getName(), meth);
             }
             ret.setMethods(methods);
             it = def.getRequiredApplications();
             List<String> apps = new ArrayList<>();
             while(it.hasNext()){
               apps.add((String)it.next() );
             }
             ret.setRequiredApplications(apps);

             it = def.getResourceLocations();
             List<String> resources = new ArrayList<>();
             while(it.hasNext()){
                resources.add(((URL)it.next()).toString());
             }
             ret.setResourceLocations(resources);


            it = def.getSuppliedResources();
            List<String> supplied = new ArrayList<>();
            while(it.hasNext()){
                resources.add(((URL)it.next()).toString());
            }
            ret.setSuppliedResources(supplied);

            it = def.getRuntimeParameterNames();
            List<ExtensionParameter> runParams = new ArrayList<>();
            while(it.hasNext()){
                String name = (String)it.next();
                ExtensionParameter runP = new ExtensionParameter();
                IPSExtensionParamDef defParam = def.getRuntimeParameter(name);
                runP.setName(defParam.getName());
                runP.setDescription(defParam.getDescription());
                runP.setDataType(defParam.getDataType());
                runParams.add(runP);
            }
            ret.setRuntimeParameters(runParams);
        }finally{
            return ret;
        }
    }

    /***
     * Gets all extensions based on the specified ExtensionFilterOptions
     * @param baseURI
     * @param filter An ExtensionFilterOptions configured with the target filters
     * @return A list of Extensions.
     */
    @Override
    public List<Extension> getExtensions(URI baseURI, ExtensionFilterOptions filter) {

        List<Extension> response = new ArrayList<>();
        try {
            Iterator it = extensionService.getExtensionNames(filter.getHandlerNamePattern(), filter.getContext(), filter.getInterfacePattern(), filter.getExtensionNamePattern());

            while(it.hasNext()){
                PSExtensionRef ref = (PSExtensionRef)it.next();
                response.add(copyExtensionRef(ref));
            }

        } catch (PSExtensionException e) {
            log.error("Error getting getExtensionNames", e);
        } finally{
            return response;
        }
    }
}
