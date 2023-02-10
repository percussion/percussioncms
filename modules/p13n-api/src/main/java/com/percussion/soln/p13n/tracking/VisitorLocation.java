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

package com.percussion.soln.p13n.tracking;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.percussion.soln.p13n.tracking.location.IVisitorLocationService;


/**
 * Contains the environment data of the visitor.
 * This is updated on every request if needed.
 * With some minor exceptions such as the TTL, this object should not be mutated outside of the 
 * {@link IVisitorLocationService}.
 * @author adamgent
 *
 */
public class VisitorLocation implements Serializable, Cloneable {
    
    private long id;
    private String countryShort;
    private String countryLong;
    private String region;
    private String city;
    private Float latitude;
    private Float longitude;
    private String zipCode;
    private String ISP;
    private String domainName;
    private String netSpeed;
    private transient volatile AtomicInteger timeToLive = new AtomicInteger(1);
    
    
    /**
     * For serializers.
     * Please use {@link #VisitorLocation(Integer)}.
     */
    public VisitorLocation() {
        super();
    }

    /**
     * The visitor location service should create
     * locations with this constructor.
     * 
     * @param timeToLive how many times the visitor location can be accessed before its invalid. 
     * If <em><code>null</code> indicates infinite TTL.</em> 
     */
    public VisitorLocation(Integer timeToLive) {
        super();
        if (timeToLive == null)
            this.timeToLive = null;
        else
            this.timeToLive.set(timeToLive);
    }
    
    /**
     * Internal id for possible future serialization or custom use.
     * @return <code>null</code> for now.
     */
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getCountryShort() {
        return countryShort;
    }
    public void setCountryShort(String countryShort) {
        this.countryShort = countryShort;
    }
    public String getCountryLong() {
        return countryLong;
    }
    public void setCountryLong(String countryLong) {
        this.countryLong = countryLong;
    }
    public String getRegion() {
        return region;
    }
    public void setRegion(String region) {
        this.region = region;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    /**
     * Geo-location coordinate.
     * @return maybe <code>null</code>.
     */
    public Float getLatitude() {
        return latitude;
    }
    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }
    /**
     * Geo-location coordinate
     * @return maybe <code>null</code>.
     */
    public Float getLongitude() {
        return longitude;
    }
    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }
    public String getZipCode() {
        return zipCode;
    }
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    public String getISP() {
        return ISP;
    }
    public void setISP(String isp) {
        ISP = isp;
    }
    public String getDomainName() {
        return domainName;
    }
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }
    public String getNetSpeed() {
        return netSpeed;
    }
    public void setNetSpeed(String netSpeed) {
        this.netSpeed = netSpeed;
    }
    
    /**
     * How long before this instance is invalid.
     * This is not an actual date/calendar time but rather a 
     * counter that is decremented every time the location is
     * accessed by a tracking request.
     *  
     * @return <code>null</code> indicates an infinite TTL.
     * @see #decrementTimeToLive(int)
     */
    @XmlTransient
    public Number getTimeToLive() {
        return timeToLive;
    }
    /**
     * For serializers. Please use {@link #decrementTimeToLive(int)}.
     * @param number
     */
    @XmlTransient
    public void setTimeToLive(Number number) {
        if (number != null)
            timeToLive = new AtomicInteger(number.intValue());
        else
            timeToLive = null;
    }
    
    /**
     * Atomically decreases the TTL.
     * @param i never <code>null</code>.
     * @return new TTL maybe <code>null</code>.
     */
    public Number decrementTimeToLive(int i) {
        AtomicInteger ttl = timeToLive;
        if (ttl == null) return null;
        return ttl.addAndGet(i * -1);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("VisitorLocation{");
        sb.append("id=").append(id);
        sb.append(", countryShort='").append(countryShort).append('\'');
        sb.append(", countryLong='").append(countryLong).append('\'');
        sb.append(", region='").append(region).append('\'');
        sb.append(", city='").append(city).append('\'');
        sb.append(", latitude=").append(latitude);
        sb.append(", longitude=").append(longitude);
        sb.append(", zipCode='").append(zipCode).append('\'');
        sb.append(", ISP='").append(ISP).append('\'');
        sb.append(", domainName='").append(domainName).append('\'');
        sb.append(", netSpeed='").append(netSpeed).append('\'');
        sb.append(", timeToLive=").append(timeToLive);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
    
    @Override
    protected VisitorLocation clone()  {
        try {
            return (VisitorLocation) super.clone();
        } catch (CloneNotSupportedException e) {
            //Should not happen.
            throw new RuntimeException(e);
        }
    }

    /**
     * Safe to serialize
     */
    private static final long serialVersionUID = 6709583725907842454L;


}
