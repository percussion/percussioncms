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

import com.ibm.cadf.exception.CADFException;

public class Geolocation extends CADFType
{

    private static final long serialVersionUID = 1L;

    private String id;

    private String latitude;

    private String longitude;

    private String elevation;

    private String accuracy;

    private String city;

    private String state;

    private String regionICANN;

    public Geolocation(String id, String latitude, String longitude, String elevation, String accuracy, String city,
                    String state, String regionICANN) throws CADFException
    {
        super();
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.accuracy = accuracy;
        this.city = city;
        this.state = state;
        this.regionICANN = regionICANN;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getLatitude()
    {
        return latitude;
    }

    public void setLatitude(String latitude)
    {
        this.latitude = latitude;
    }

    public String getLongitude()
    {
        return longitude;
    }

    public void setLongitude(String longitude)
    {
        this.longitude = longitude;
    }

    public String getElevation()
    {
        return elevation;
    }

    public void setElevation(String elevation)
    {
        this.elevation = elevation;
    }

    public String getAccuracy()
    {
        return accuracy;
    }

    public void setAccuracy(String accuracy)
    {
        this.accuracy = accuracy;
    }

    public String getCity()
    {
        return city;
    }

    public void setCity(String city)
    {
        this.city = city;
    }

    public String getState()
    {
        return state;
    }

    public void setState(String state)
    {
        this.state = state;
    }

    public String getRegionICANN()
    {
        return regionICANN;
    }

    public void setRegionICANN(String regionICANN)
    {
        this.regionICANN = regionICANN;
    }

    @Override
    public boolean isValid()
    {
        return true;
    }

}
