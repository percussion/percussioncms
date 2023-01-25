
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
 *         &lt;element name="BuildingID" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="RoomTypeID" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="FloorID" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="WebProcessTemplates" type="{http://DEA.EMS.API.Web.Service/}ArrayOfInt" minOccurs="0"/&gt;
 *         &lt;element name="StartDateTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="EndDateTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
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
    "buildingID",
    "roomTypeID",
    "floorID",
    "webProcessTemplates",
    "startDateTime",
    "endDateTime"
})
@XmlRootElement(name = "GetRoomsAvailable2")
public class GetRoomsAvailable2 {

    @XmlElement(name = "UserName")
    protected String userName;
    @XmlElement(name = "Password")
    protected String password;
    @XmlElement(name = "BuildingID")
    protected int buildingID;
    @XmlElement(name = "RoomTypeID")
    protected int roomTypeID;
    @XmlElement(name = "FloorID")
    protected int floorID;
    @XmlElement(name = "WebProcessTemplates")
    protected ArrayOfInt webProcessTemplates;
    @XmlElement(name = "StartDateTime", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar startDateTime;
    @XmlElement(name = "EndDateTime", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar endDateTime;

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
     * Gets the value of the roomTypeID property.
     * 
     */
    public int getRoomTypeID() {
        return roomTypeID;
    }

    /**
     * Sets the value of the roomTypeID property.
     * 
     */
    public void setRoomTypeID(int value) {
        this.roomTypeID = value;
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
     * Gets the value of the webProcessTemplates property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfInt }
     *     
     */
    public ArrayOfInt getWebProcessTemplates() {
        return webProcessTemplates;
    }

    /**
     * Sets the value of the webProcessTemplates property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfInt }
     *     
     */
    public void setWebProcessTemplates(ArrayOfInt value) {
        this.webProcessTemplates = value;
    }

    /**
     * Gets the value of the startDateTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getStartDateTime() {
        return startDateTime;
    }

    /**
     * Sets the value of the startDateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setStartDateTime(XMLGregorianCalendar value) {
        this.startDateTime = value;
    }

    /**
     * Gets the value of the endDateTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getEndDateTime() {
        return endDateTime;
    }

    /**
     * Sets the value of the endDateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setEndDateTime(XMLGregorianCalendar value) {
        this.endDateTime = value;
    }

}
