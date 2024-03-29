
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
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="ReservationID" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="BookingID" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="WebUserID" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="WebTemplateID" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="ReservationSourceID" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="BillingReference" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
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
    "reservationID",
    "bookingID",
    "webUserID",
    "webTemplateID",
    "reservationSourceID",
    "billingReference"
})
@XmlRootElement(name = "UpdateReservation")
public class UpdateReservation {

    @XmlElement(name = "UserName")
    protected String userName;
    @XmlElement(name = "Password")
    protected String password;
    @XmlElement(name = "ReservationID")
    protected int reservationID;
    @XmlElement(name = "BookingID")
    protected int bookingID;
    @XmlElement(name = "WebUserID")
    protected int webUserID;
    @XmlElement(name = "WebTemplateID")
    protected int webTemplateID;
    @XmlElement(name = "ReservationSourceID")
    protected int reservationSourceID;
    @XmlElement(name = "BillingReference")
    protected String billingReference;

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
     * Gets the value of the reservationID property.
     * 
     */
    public int getReservationID() {
        return reservationID;
    }

    /**
     * Sets the value of the reservationID property.
     * 
     */
    public void setReservationID(int value) {
        this.reservationID = value;
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
     * Gets the value of the webTemplateID property.
     * 
     */
    public int getWebTemplateID() {
        return webTemplateID;
    }

    /**
     * Sets the value of the webTemplateID property.
     * 
     */
    public void setWebTemplateID(int value) {
        this.webTemplateID = value;
    }

    /**
     * Gets the value of the reservationSourceID property.
     * 
     */
    public int getReservationSourceID() {
        return reservationSourceID;
    }

    /**
     * Sets the value of the reservationSourceID property.
     * 
     */
    public void setReservationSourceID(int value) {
        this.reservationSourceID = value;
    }

    /**
     * Gets the value of the billingReference property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBillingReference() {
        return billingReference;
    }

    /**
     * Sets the value of the billingReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBillingReference(String value) {
        this.billingReference = value;
    }

}
