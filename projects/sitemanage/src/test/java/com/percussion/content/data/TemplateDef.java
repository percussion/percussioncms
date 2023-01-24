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

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-661 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.08.24 at 01:24:36 PM EDT 
//


package com.percussion.content.data;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="BodyMarkup" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CssOverride" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AdditionalHeadContent" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AfterBodyStart" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="BeforeBodyClose" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RegionDef" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="parentRegion" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element ref="{}Widget" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="label" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="siteName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="baseTemplateName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "bodyMarkup",
    "cssOverride",
    "additionalHeadContent",
    "afterBodyStart",
    "beforeBodyClose",
    "regionDef",
    "widget"
})
@XmlRootElement(name = "TemplateDef")
public class TemplateDef {

    @XmlElement(name = "BodyMarkup")
    protected String bodyMarkup;
    @XmlElement(name = "CssOverride")
    protected String cssOverride;
    @XmlElement(name = "AdditionalHeadContent")
    protected String additionalHeadContent;
    @XmlElement(name = "AfterBodyStart")
    protected String afterBodyStart;
    @XmlElement(name = "BeforeBodyClose")
    protected String beforeBodyClose;
    @XmlElement(name = "RegionDef")
    protected TemplateDef.RegionDef regionDef;
    @XmlElement(name = "Widget")
    protected List<Widget> widget;
    @XmlAttribute
    protected String name;
    @XmlAttribute
    protected String label;
    @XmlAttribute
    protected String siteName;
    @XmlAttribute(required = true)
    protected String baseTemplateName;

    /**
     * Gets the value of the bodyMarkup property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBodyMarkup() {
        return bodyMarkup;
    }

    /**
     * Sets the value of the bodyMarkup property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBodyMarkup(String value) {
        this.bodyMarkup = value;
    }

    /**
     * Gets the value of the cssOverride property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCssOverride() {
        return cssOverride;
    }

    /**
     * Sets the value of the cssOverride property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCssOverride(String value) {
        this.cssOverride = value;
    }

    /**
     * Gets the value of the additionalHeadContent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdditionalHeadContent() {
        return additionalHeadContent;
    }

    /**
     * Sets the value of the additionalHeadContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdditionalHeadContent(String value) {
        this.additionalHeadContent = value;
    }

    /**
     * Gets the value of the afterBodyStart property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAfterBodyStart() {
        return afterBodyStart;
    }

    /**
     * Sets the value of the afterBodyStart property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAfterBodyStart(String value) {
        this.afterBodyStart = value;
    }

    /**
     * Gets the value of the beforeBodyClose property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBeforeBodyClose() {
        return beforeBodyClose;
    }

    /**
     * Sets the value of the beforeBodyClose property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBeforeBodyClose(String value) {
        this.beforeBodyClose = value;
    }

    /**
     * Gets the value of the regionDef property.
     * 
     * @return
     *     possible object is
     *     {@link TemplateDef.RegionDef }
     *     
     */
    public TemplateDef.RegionDef getRegionDef() {
        return regionDef;
    }

    /**
     * Sets the value of the regionDef property.
     * 
     * @param value
     *     allowed object is
     *     {@link TemplateDef.RegionDef }
     *     
     */
    public void setRegionDef(TemplateDef.RegionDef value) {
        this.regionDef = value;
    }

    /**
     * Gets the value of the widget property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the widget property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWidget().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Widget }
     * 
     * 
     */
    public List<Widget> getWidget() {
        if (widget == null) {
            widget = new ArrayList<Widget>();
        }
        return this.widget;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the label property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabel(String value) {
        this.label = value;
    }

    /**
     * Gets the value of the siteName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSiteName() {
        return siteName;
    }

    /**
     * Sets the value of the siteName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSiteName(String value) {
        this.siteName = value;
    }

    /**
     * Gets the value of the baseTemplateName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBaseTemplateName() {
        return baseTemplateName;
    }

    /**
     * Sets the value of the baseTemplateName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBaseTemplateName(String value) {
        this.baseTemplateName = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="parentRegion" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class RegionDef {

        @XmlAttribute(required = true)
        protected String name;
        @XmlAttribute
        protected String parentRegion;

        /**
         * Gets the value of the name property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the value of the name property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setName(String value) {
            this.name = value;
        }

        /**
         * Gets the value of the parentRegion property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getParentRegion() {
            return parentRegion;
        }

        /**
         * Sets the value of the parentRegion property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setParentRegion(String value) {
            this.parentRegion = value;
        }

    }

}
