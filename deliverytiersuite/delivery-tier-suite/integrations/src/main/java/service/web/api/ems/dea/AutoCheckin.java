
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
 *         &lt;element name="EmailAddress" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="WebUserID" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="UDFID" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="UDFValue" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="BuildingID" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="RoomID" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="FloorID" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="BookingDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="EventType" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
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
    "emailAddress",
    "webUserID",
    "udfid",
    "udfValue",
    "buildingID",
    "roomID",
    "floorID",
    "bookingDate",
    "eventType"
})
@XmlRootElement(name = "AutoCheckin")
public class AutoCheckin {

    @XmlElement(name = "UserName")
    protected String userName;
    @XmlElement(name = "Password")
    protected String password;
    @XmlElement(name = "EmailAddress")
    protected String emailAddress;
    @XmlElement(name = "WebUserID")
    protected int webUserID;
    @XmlElement(name = "UDFID")
    protected int udfid;
    @XmlElement(name = "UDFValue")
    protected String udfValue;
    @XmlElement(name = "BuildingID")
    protected int buildingID;
    @XmlElement(name = "RoomID")
    protected int roomID;
    @XmlElement(name = "FloorID")
    protected int floorID;
    @XmlElement(name = "BookingDate", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar bookingDate;
    @XmlElement(name = "EventType")
    protected int eventType;

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
     * Gets the value of the emailAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Sets the value of the emailAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmailAddress(String value) {
        this.emailAddress = value;
    }

    /**
     * Gets the value of the webUserID property.
     * 
     */
    public int getWebUserID() {
        return webUserID;
    }

    /**
     * Sets the value of the webUserID property.
     * 
     */
    public void setWebUserID(int value) {
        this.webUserID = value;
    }

    /**
     * Gets the value of the udfid property.
     * 
     */
    public int getUDFID() {
        return udfid;
    }

    /**
     * Sets the value of the udfid property.
     * 
     */
    public void setUDFID(int value) {
        this.udfid = value;
    }

    /**
     * Gets the value of the udfValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUDFValue() {
        return udfValue;
    }

    /**
     * Sets the value of the udfValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUDFValue(String value) {
        this.udfValue = value;
    }

    /**
     * Gets the value of the buildingID property.
     * 
     */
    public int getBuildingID() {
        return buildingID;
    }

    /**
     * Sets the value of the buildingID property.
     * 
     */
    public void setBuildingID(int value) {
        this.buildingID = value;
    }

    /**
     * Gets the value of the roomID property.
     * 
     */
    public int getRoomID() {
        return roomID;
    }

    /**
     * Sets the value of the roomID property.
     * 
     */
    public void setRoomID(int value) {
        this.roomID = value;
    }

    /**
     * Gets the value of the floorID property.
     * 
     */
    public int getFloorID() {
        return floorID;
    }

    /**
     * Sets the value of the floorID property.
     * 
     */
    public void setFloorID(int value) {
        this.floorID = value;
    }

    /**
     * Gets the value of the bookingDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getBookingDate() {
        return bookingDate;
    }

    /**
     * Sets the value of the bookingDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setBookingDate(XMLGregorianCalendar value) {
        this.bookingDate = value;
    }

    /**
     * Gets the value of the eventType property.
     * 
     */
    public int getEventType() {
        return eventType;
    }

    /**
     * Sets the value of the eventType property.
     * 
     */
    public void setEventType(int value) {
        this.eventType = value;
    }

}
