
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
 *         &lt;element name="A_0" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="A_1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="A_2" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="A_3" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="A_4" type="{http://DEA.EMS.API.Web.Service/}ArrayOfInt" minOccurs="0"/&gt;
 *         &lt;element name="A_5" type="{http://DEA.EMS.API.Web.Service/}ArrayOfInt" minOccurs="0"/&gt;
 *         &lt;element name="A_6" type="{http://DEA.EMS.API.Web.Service/}ArrayOfInt" minOccurs="0"/&gt;
 *         &lt;element name="A_7" type="{http://DEA.EMS.API.Web.Service/}ArrayOfInt" minOccurs="0"/&gt;
 *         &lt;element name="A_8" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
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
    "a0",
    "a1",
    "a2",
    "a3",
    "a4",
    "a5",
    "a6",
    "a7",
    "a8"
})
@XmlRootElement(name = "GetBookingHistory")
public class GetBookingHistory {

    @XmlElement(name = "A_0")
    protected String a0;
    @XmlElement(name = "A_1")
    protected String a1;
    @XmlElement(name = "A_2", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar a2;
    @XmlElement(name = "A_3", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar a3;
    @XmlElement(name = "A_4")
    protected ArrayOfInt a4;
    @XmlElement(name = "A_5")
    protected ArrayOfInt a5;
    @XmlElement(name = "A_6")
    protected ArrayOfInt a6;
    @XmlElement(name = "A_7")
    protected ArrayOfInt a7;
    @XmlElement(name = "A_8")
    protected boolean a8;

    /**
     * Gets the value of the a0 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getA0() {
        return a0;
    }

    /**
     * Sets the value of the a0 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setA0(String value) {
        this.a0 = value;
    }

    /**
     * Gets the value of the a1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getA1() {
        return a1;
    }

    /**
     * Sets the value of the a1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setA1(String value) {
        this.a1 = value;
    }

    /**
     * Gets the value of the a2 property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getA2() {
        return a2;
    }

    /**
     * Sets the value of the a2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setA2(XMLGregorianCalendar value) {
        this.a2 = value;
    }

    /**
     * Gets the value of the a3 property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getA3() {
        return a3;
    }

    /**
     * Sets the value of the a3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setA3(XMLGregorianCalendar value) {
        this.a3 = value;
    }

    /**
     * Gets the value of the a4 property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfInt }
     *     
     */
    public ArrayOfInt getA4() {
        return a4;
    }

    /**
     * Sets the value of the a4 property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfInt }
     *     
     */
    public void setA4(ArrayOfInt value) {
        this.a4 = value;
    }

    /**
     * Gets the value of the a5 property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfInt }
     *     
     */
    public ArrayOfInt getA5() {
        return a5;
    }

    /**
     * Sets the value of the a5 property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfInt }
     *     
     */
    public void setA5(ArrayOfInt value) {
        this.a5 = value;
    }

    /**
     * Gets the value of the a6 property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfInt }
     *     
     */
    public ArrayOfInt getA6() {
        return a6;
    }

    /**
     * Sets the value of the a6 property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfInt }
     *     
     */
    public void setA6(ArrayOfInt value) {
        this.a6 = value;
    }

    /**
     * Gets the value of the a7 property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfInt }
     *     
     */
    public ArrayOfInt getA7() {
        return a7;
    }

    /**
     * Sets the value of the a7 property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfInt }
     *     
     */
    public void setA7(ArrayOfInt value) {
        this.a7 = value;
    }

    /**
     * Gets the value of the a8 property.
     * 
     */
    public boolean isA8() {
        return a8;
    }

    /**
     * Sets the value of the a8 property.
     * 
     */
    public void setA8(boolean value) {
        this.a8 = value;
    }

}
