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
package com.percussion.pagemanagement.assembler.impl;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.services.assembly.data.PSTemplateBinding;
import org.xml.sax.SAXException;

import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateBinding;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.xml.PSInvalidXmlException;

/**
 * 
 * A decorator around the real assembly template to enhance performance
 * with cloning and memory foot print.
 * The template,bindings, and assembler properties are not wrapped.
 * 
 * @author adamgent
 *
 */
public class PSProxyAssemblyTemplate implements IPSAssemblyTemplate, Cloneable
{

    private static final long serialVersionUID = 1L;

    private PSAssemblyTemplate assemblyTemplate;
    
    private String template;
    private List<PSTemplateBinding> bindings = new Vector<>();
    private String assembler;
    
    private String name;

    /**
     * Decorator pattern constructor. Wrapped not copied.
     * @param assemblyTemplate never <code>null</code>.
     */
    public PSProxyAssemblyTemplate(PSAssemblyTemplate assemblyTemplate)
    {
        super();
        this.assemblyTemplate = assemblyTemplate;
        this.setTemplate(assemblyTemplate.getTemplate());
        this.setBindings(assemblyTemplate.getBindings());
        this.setAssembler(assemblyTemplate.getAssembler());
        this.name = assemblyTemplate.getName();
    }

    
    public String getAssembler()
    {
        return assembler;
    }


    public void setAssembler(String assembler)
    {
        this.assembler = assembler;
    }


    public String getTemplate()
    {
        return template;
    }

    public void setTemplate(String template)
    {
        this.template = template;
    }

    public List<PSTemplateBinding> getBindings()
    {
        return bindings;
    }

    public void setBindings(List<PSTemplateBinding> bindings)
    {
        this.bindings = bindings;
    }

    public void addBinding(PSTemplateBinding binding)
    {
        getBindings().add(binding);
    }

    public void addSlot(IPSTemplateSlot arg0)
    {
        assemblyTemplate.addSlot(arg0);
    }

    @Override
    public Object clone()
    {
        try
        {
            PSProxyAssemblyTemplate t = (PSProxyAssemblyTemplate) super.clone();
            t.setBindings(new Vector<>(t.getBindings()));
            return t;
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void fromXML(String arg0) throws IOException, SAXException, PSInvalidXmlException
    {
        assemblyTemplate.fromXML(arg0);
    }

    public AAType getActiveAssemblyType()
    {
        return assemblyTemplate.getActiveAssemblyType();
    }

    public String getAssemblyUrl()
    {
        return assemblyTemplate.getAssemblyUrl();
    }

    public String getCharset()
    {
        return assemblyTemplate.getCharset();
    }

    public String getDescription()
    {
        return assemblyTemplate.getDescription();
    }

    public IPSGuid getGlobalTemplate()
    {
        return assemblyTemplate.getGlobalTemplate();
    }

    public GlobalTemplateUsage getGlobalTemplateUsage()
    {
        return assemblyTemplate.getGlobalTemplateUsage();
    }

    public IPSGuid getGUID()
    {
        return assemblyTemplate.getGUID();
    }

    public String getLabel()
    {
        return assemblyTemplate.getLabel();
    }

    public String getLocationPrefix()
    {
        return assemblyTemplate.getLocationPrefix();
    }

    public String getLocationSuffix()
    {
        return assemblyTemplate.getLocationSuffix();
    }

    public String getMimeType()
    {
        return assemblyTemplate.getMimeType();
    }

    public String getName()
    {
        return this.name;
    }

    public OutputFormat getOutputFormat()
    {
        return assemblyTemplate.getOutputFormat();
    }

    public PublishWhen getPublishWhen()
    {
        return assemblyTemplate.getPublishWhen();
    }

    public Set<IPSTemplateSlot> getSlots()
    {
        return assemblyTemplate.getSlots();
    }

    public String getStyleSheetPath()
    {
        return assemblyTemplate.getStyleSheetPath();
    }

    public TemplateType getTemplateType()
    {
        return assemblyTemplate.getTemplateType();
    }

    public boolean isVariant()
    {
        return assemblyTemplate.isVariant();
    }

    public void removeBinding(PSTemplateBinding arg0)
    {
        assemblyTemplate.removeBinding(arg0);
    }

    public void removeSlot(IPSTemplateSlot arg0)
    {
        assemblyTemplate.removeSlot(arg0);
    }

    public void setActiveAssemblyType(AAType arg0)
    {
        assemblyTemplate.setActiveAssemblyType(arg0);
    }

    public void setAssemblyUrl(String arg0)
    {
        assemblyTemplate.setAssemblyUrl(arg0);
    }

    public void setCharset(String arg0)
    {
        assemblyTemplate.setCharset(arg0);
    }

    public void setDescription(String arg0)
    {
        assemblyTemplate.setDescription(arg0);
    }

    public void setGlobalTemplate(IPSGuid arg0)
    {
        assemblyTemplate.setGlobalTemplate(arg0);
    }

    public void setGlobalTemplateUsage(GlobalTemplateUsage arg0)
    {
        assemblyTemplate.setGlobalTemplateUsage(arg0);
    }

    public void setGUID(IPSGuid arg0) throws IllegalStateException
    {
        throw new IllegalArgumentException("Cannot change guid of base PSAssemblyTemplate Entity");
    }

    public void setLabel(String arg0)
    {
        assemblyTemplate.setLabel(arg0);
    }

    public void setLocationPrefix(String arg0)
    {
        assemblyTemplate.setLocationPrefix(arg0);
    }

    public void setLocationSuffix(String arg0)
    {
        assemblyTemplate.setLocationSuffix(arg0);
    }

    public void setMimeType(String arg0)
    {
        assemblyTemplate.setMimeType(arg0);
    }

    public void setName(String arg0)
    {
        this.name = arg0;
    }

    public void setOutputFormat(OutputFormat arg0)
    {
        assemblyTemplate.setOutputFormat(arg0);
    }

    public void setPublishWhen(PublishWhen arg0)
    {
        assemblyTemplate.setPublishWhen(arg0);
    }

    public void setSlots(Set<IPSTemplateSlot> arg0)
    {
        assemblyTemplate.setSlots(arg0);
    }

    public void setStyleSheetPath(String arg0)
    {
        assemblyTemplate.setStyleSheetPath(arg0);
    }

    public void setTemplateType(TemplateType arg0)
    {
        assemblyTemplate.setTemplateType(arg0);
    }

    public String toXML() throws IOException, SAXException
    {
        return assemblyTemplate.toXML();
    }
    
    

}
