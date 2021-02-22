/*
 * Copyright 2016 IBM Corp.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.ibm.cadf.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ibm.cadf.exception.CADFException;
import com.ibm.cadf.util.Constants;

public class Resource extends CADFType
{

    private static final long serialVersionUID = 1L;

    private String id;

    private String typeURI;

    private String name;

    private String domain;

    private Credential credential;

    private Host host;

    private String ref;

    private Geolocation geolocation;

    private String geolocationId;

    private List<EndPoint> addresses;

    private List<Attachment> attachments;

    public Resource()
    {

    }

    public Resource(String id) throws CADFException
    {
        super();
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getTypeURI()
    {
        return typeURI;
    }

    public void setTypeURI(String typeURI)
    {
        this.typeURI = typeURI;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDomain()
    {
        return domain;
    }

    public void setDomain(String domain)
    {
        this.domain = domain;
    }

    public Credential getCredential()
    {
        return credential;
    }

    public void setCredential(Credential credential)
    {
        this.credential = credential;
    }

    public Host getHost()
    {
        return host;
    }

    public void setHost(Host host)
    {
        this.host = host;
    }

    public String getRef()
    {
        return ref;
    }

    public void setRef(String ref)
    {
        this.ref = ref;
    }

    public Geolocation getGeolocation()
    {
        return geolocation;
    }

    public void setGeolocation(Geolocation geolocation)
    {
        this.geolocation = geolocation;
    }

    public String getGeolocationId()
    {
        return geolocationId;
    }

    public void setGeolocationId(String geolocationId)
    {
        this.geolocationId = geolocationId;
    }

    public void addAddress(EndPoint endpoint)
    {
        if (addresses == null)
        {
            addresses = new ArrayList<>();
        }
        addresses.add(endpoint);
    }

    public void addAttachment(Attachment attachment)
    {
        if (attachments == null)
        {
            attachments = new ArrayList<>();
        }
        attachments.add(attachment);
    }

    public List<EndPoint> getAddresses()
    {
        return addresses;
    }

    public List<Attachment> getAttachments()
    {
        return attachments;
    }

    @Override
    public boolean isValid()
    {
        return (StringUtils.isNotEmpty(id) && (StringUtils.isNotEmpty(typeURI) || (id.equals(Constants.TARGET) || id.equals(Constants.INITIATOR))));
    }

}
