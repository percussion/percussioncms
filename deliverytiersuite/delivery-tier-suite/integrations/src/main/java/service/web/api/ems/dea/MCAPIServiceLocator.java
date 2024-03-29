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

/**
 * MCAPIServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package service.web.api.ems.dea;

public class MCAPIServiceLocator extends org.apache.axis.client.Service implements MCAPIService {

    public MCAPIServiceLocator() {
    }


    public MCAPIServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public MCAPIServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for MCAPIServiceSoap
    private java.lang.String MCAPIServiceSoap_address = "https://dhemsdev.csudh.edu/MCAPI/MCAPIService.asmx";

    public java.lang.String getMCAPIServiceSoapAddress() {
        return MCAPIServiceSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String MCAPIServiceSoapWSDDServiceName = "MCAPIServiceSoap";

    public java.lang.String getMCAPIServiceSoapWSDDServiceName() {
        return MCAPIServiceSoapWSDDServiceName;
    }

    public void setMCAPIServiceSoapWSDDServiceName(java.lang.String name) {
        MCAPIServiceSoapWSDDServiceName = name;
    }

    public MCAPIServiceSoap getMCAPIServiceSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(MCAPIServiceSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getMCAPIServiceSoap(endpoint);
    }

    public MCAPIServiceSoap getMCAPIServiceSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
           MCAPIServiceSoapStub _stub = new MCAPIServiceSoapStub(portAddress, this);
            _stub.setPortName(getMCAPIServiceSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setMCAPIServiceSoapEndpointAddress(java.lang.String address) {
        MCAPIServiceSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (MCAPIServiceSoap.class.isAssignableFrom(serviceEndpointInterface)) {
                MCAPIServiceSoapStub _stub = new MCAPIServiceSoapStub(new java.net.URL(MCAPIServiceSoap_address), this);
                _stub.setPortName(getMCAPIServiceSoapWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("MCAPIServiceSoap".equals(inputPortName)) {
            return getMCAPIServiceSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://DEA.Web.Service.MasterCalendar.API/", "MCAPIService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://DEA.Web.Service.MasterCalendar.API/", "MCAPIServiceSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("MCAPIServiceSoap".equals(portName)) {
            setMCAPIServiceSoapEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
