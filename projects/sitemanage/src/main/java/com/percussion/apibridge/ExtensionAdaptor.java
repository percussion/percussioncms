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

package com.percussion.apibridge;

import com.percussion.extension.*;
import com.percussion.extensions.IPSExtensionService;
import com.percussion.rest.extensions.*;
import com.percussion.util.PSSiteManageBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.net.URL;
import java.util.*;

@PSSiteManageBean
public class ExtensionAdaptor implements IExtensionAdaptor {

    private IPSExtensionService extensionService;
    private Log log = LogFactory.getLog(ExtensionAdaptor.class);

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
             List<String> apps = new ArrayList<String>();
             while(it.hasNext()){
               apps.add((String)it.next() );
             }
             ret.setRequiredApplications(apps);

             it = def.getResourceLocations();
             List<String> resources = new ArrayList<String>();
             while(it.hasNext()){
                resources.add(((URL)it.next()).toString());
             }
             ret.setResourceLocations(resources);


            it = def.getSuppliedResources();
            List<String> supplied = new ArrayList<String>();
            while(it.hasNext()){
                resources.add(((URL)it.next()).toString());
            }
            ret.setSuppliedResources(supplied);

            it = def.getRuntimeParameterNames();
            List<ExtensionParameter> runParams = new ArrayList<ExtensionParameter>();
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

        List<Extension> response = new ArrayList<Extension>();
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
