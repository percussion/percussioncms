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
package com.percussion.widgetbuilder.utils.xform;

import com.percussion.pagemanagement.data.PSResourceDefinitionGroup;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSFileResource;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSFileResource.PSFileResourceType;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSResourceDependency;
import com.percussion.share.dao.PSSerializerUtils;
import com.percussion.widgetbuilder.utils.IPSWidgetFileTransformer;
import com.percussion.widgetbuilder.utils.PSWidgetPackageBuilderException;
import com.percussion.widgetbuilder.utils.PSWidgetPackageSpec;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Add resource entries for js and css files specified by the supplied package spec
 * 
 * @author JaySeletz
 *
 */
public class PSResourceFileTransformer implements IPSWidgetFileTransformer
{
    @Override
    public Reader transformFile(File file, Reader reader, PSWidgetPackageSpec packageSpec)
            throws PSWidgetPackageBuilderException
    {
        // TODO Auto-generated method stub
        try
        {
            PSResourceDefinitionGroup group = PSSerializerUtils.unmarshal(IOUtils.toString(reader), PSResourceDefinitionGroup.class);
            List<PSFileResource> files = group.getFileResources();
            Set<String> fileIds = new HashSet<>();
            String cssDepId = null;
            for (String path : packageSpec.getCssFiles())
            {
            	PSFileResource fres = createFileResource(packageSpec, PSFileResourceType.css, path, fileIds, cssDepId);
            	cssDepId = packageSpec.getFullWidgetName() + "." + fres.getId();
            	files.add(fres);
            }
            String jsDepId = "percSystem.jquery";
            for (String path : packageSpec.getJsFiles())
            {
            	PSFileResource fres = createFileResource(packageSpec, PSFileResourceType.javascript, path, fileIds, jsDepId);
            	jsDepId = packageSpec.getFullWidgetName() + "." + fres.getId();
            	files.add(fres);
            }
            
            return new StringReader(PSSerializerUtils.marshal(group));
        }
        catch (Exception e)
        {
            throw new PSWidgetPackageBuilderException("Failed to transform resource definition file: " + file.getName(), e);
        }
    }

    private PSFileResource createFileResource(PSWidgetPackageSpec packageSpec, PSFileResourceType type, String path,
            Set<String> fileIds, String dependeeId)
    {
        PSFileResource cssFile = new PSFileResource();
        cssFile.setFile(path);
        cssFile.setId(getFileId(path, packageSpec, fileIds));
        cssFile.setType(type);
        if(dependeeId != null)
        {
            List<PSResourceDependency> deps = new ArrayList<>();
            PSResourceDependency dep = new PSResourceDependency();
            dep.setDependeeId(dependeeId);
            deps.add(dep);
            cssFile.setDependencies(deps);
        }
        return cssFile;
    }

    /**
     * Generate unique file id based on package and path
     * 
     * @param path
     * @param packageSpec
     * @param fileIds Set of ids already generated to ensure uniqueness
     * 
     * @return
     */
    private String getFileId(String path, PSWidgetPackageSpec packageSpec, Set<String> fileIds)
    {
        String fileName = StringUtils.replace(StringUtils.substringAfterLast(path, "/"), ".", "-");
        String id = packageSpec.getPrefix() + "-" + packageSpec.getWidgetName() + "-" + fileName;
        String base = id;
        int suffix = 1;
        while (fileIds.contains(id))
        {
            id = base + "-" + suffix++;
        }
        
        fileIds.add(id);
        return id;
    }

    @Override
    public boolean handleFile(File file)
    {
        File parent = file.getParentFile();
        if (parent == null)
            return false;
        
        return parent.getName().startsWith("sys__UserDependency--rxconfig_Resources_");
    }

    @Override
    public File transformPath(File file, PSWidgetPackageSpec packageSpec) throws PSWidgetPackageBuilderException
    {
        return file;
    }

}
