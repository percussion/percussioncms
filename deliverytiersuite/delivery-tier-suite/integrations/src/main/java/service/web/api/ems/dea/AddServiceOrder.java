
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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package service.web.api.ems.dea;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="UserName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Password" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CategoryID" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="BookingID" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="TimeStart" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="TimeEnd" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="ServiceTypeID" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="StateID" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="EstimatedCount" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="GuaranteedCount" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="ActualCount" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "userName",
    "password",
    "categoryID",
    "bookingID",
    "timeStart",
    "timeEnd",
    "serviceTypeID",
    "stateID",
    "estimatedCount",
    "guaranteedCount",
    "actualCount"
})
@XmlRootElement(name = "AddServiceOrder")
public class AddServiceOrder {

    @XmlElement(name = "UserName")
    protected String userName;
    @XmlElement(name = "Password")
    protected String password;
    @XmlElement(name = "CategoryID")
    protected int categoryID;
    @XmlElement(name = "BookingID")
    protected int bookingID;
    @XmlElement(name = "TimeStart", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar timeStart;
    @XmlElement(name = "TimeEnd", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar timeEnd;
    @XmlElement(name = "ServiceTypeID")
    protected int serviceTypeID;
    @XmlElement(name = "StateID")
    protected int stateID;
    @XmlElement(name = "EstimatedCount")
    protected int estimatedCount;
    @XmlElement(name = "GuaranteedCount")
    protected int guaranteedCount;
    @XmlElement(name = "ActualCount")
    protected int actualCount;

    /**
     * Gets the value of the userName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the value of the userName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserName(String value) {
        this.userName = value;
    }

    /**
     * Gets the value of the password property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the value of the password property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassword(String value) {
        this.password = value;
    }

    /**
     * Gets the value of the categoryID property.
     * 
     */
    public int getCategoryID() {
        return categoryID;
    }

    /**
     * Sets the value of the categoryID property.
     * 
     */
    public void setCategoryID(int value) {
        this.categoryID = value;
    }

    /**
     * Gets the value of the bookingID property.
     * 
     */
    public int getBookingID() {
        return bookingID;
    }

    /**
     * Sets the value of the bookingID property.
     * 
     */
    public void setBookingID(int value) {
        this.bookingID = value;
    }

    /**
     * Gets the value of the timeStart property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTimeStart() {
        return timeStart;
    }

    /**
     * Sets the value of the timeStart property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTimeStart(XMLGregorianCalendar value) {
        this.timeStart = value;
    }

    /**
     * Gets the value of the timeEnd property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTimeEnd() {
        return timeEnd;
    }

    /**
     * Sets the value of the timeEnd property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTimeEnd(XMLGregorianCalendar value) {
        this.timeEnd = value;
    }

    /**
     * Gets the value of the serviceTypeID property.
     * 
     */
    public int getServiceTypeID() {
        return serviceTypeID;
    }

    /**
     * Sets the value of the serviceTypeID property.
     * 
     */
    public void setServiceTypeID(int value) {
        this.serviceTypeID = value;
    }

    /**
     * Gets the value of the stateID property.
     * 
     */
    public int getStateID() {
        return stateID;
    }

    /**
     * Sets the value of the stateID property.
     * 
     */
    public void setStateID(int value) {
        this.stateID = value;
    }

    /**
     * Gets the value of the estimatedCount property.
     * 
     */
    public int getEstimatedCount() {
        return estimatedCount;
    }

    /**
     * Sets the value of the estimatedCount property.
     * 
     */
    public void setEstimatedCount(int value) {
        this.estimatedCount = value;
    }

    /**
     * Gets the value of the guaranteedCount property.
     * 
     */
    public int getGuaranteedCount() {
        return guaranteedCount;
    }

    /**
     * Sets the value of the guaranteedCount property.
     * 
     */
    public void setGuaranteedCount(int value) {
        this.guaranteedCount = value;
    }

    /**
     * Gets the value of the actualCount property.
     * 
     */
    public int getActualCount() {
        return actualCount;
    }

    /**
     * Sets the value of the actualCount property.
     * 
     */
    public void setActualCount(int value) {
        this.actualCount = value;
    }

}
