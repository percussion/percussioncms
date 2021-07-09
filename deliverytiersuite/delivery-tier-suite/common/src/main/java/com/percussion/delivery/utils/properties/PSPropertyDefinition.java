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
package com.percussion.delivery.utils.properties;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines the definition for a Property. 
 * 
 * @author natechadwick
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "property", propOrder = {
    "enumValue"
})
public class PSPropertyDefinition {
	
	        @XmlElement(name = "EnumValue")
	        private List<EnumValue> enumValue;
	        @XmlAttribute(required = true)
	        private String name;
	        @XmlAttribute(name = "display_name")
	        private String displayName;
	        @XmlAttribute(name = "default_value")
	        private String defaultValue;
	        @XmlAttribute
	        private String required;
	        @XmlAttribute
	        private String datatype;
	        @XmlAttribute(name="max_length")
	        private int maxLength;
	        @XmlAttribute(name= "validation_regex")
	        private String validationRegEx;
	        @XmlAttribute(name="validation_message")
	        private String validationMessage;
	        @XmlAttribute(name="help_text")
	        private String helpText;
	        @XmlAttribute(name="display_regex")
	        private String displayRegEx;
	        @XmlAttribute(name="property_value")
	        private Object propertyValue;
	        
	       

	        /**
	         * Gets the value of the enumValue property.
	         * 
	         * <p>
	         * This accessor method returns a reference to the live list,
	         * not a snapshot. Therefore any modification you make to the
	         * returned list will be present inside the JAXB object.
	         * This is why there is not a <CODE>set</CODE> method for the enumValue property.
	         * 
	         * <p>
	         * For example, to add a new item, do as follows:
	         * <pre>
	         *    getEnumValue().add(newItem);
	         * </pre>
	         * 
	         * 
	         * <p>
	         * Objects of the following type(s) are allowed in the list
	         * 
	         * @return never <code>null</code>.
	         * 
	         * 
	         */
	        public List<EnumValue> getEnumValue() {
	            if (enumValue == null) {
	                enumValue = new ArrayList<>();
	            }
	            return this.enumValue;
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
	         * Gets the value of the displayName property.
	         * 
	         * @return
	         *     possible object is
	         *     {@link String }
	         *     
	         */
	        public String getDisplayName() {
	            return displayName;
	        }

	        /**
	         * Sets the value of the displayName property.
	         * 
	         * @param value
	         *     allowed object is
	         *     {@link String }
	         *     
	         */
	        public void setDisplayName(String value) {
	            this.displayName = value;
	        }

	        /**
	         * Gets the value of the defaultValue property.
	         * 
	         * @return default value, it may <code>null</code> if not defined.
	         *     
	         */
	        public String getDefaultValue() {
	            return defaultValue;
	        }

	        /**
	         * Sets the value of the defaultValue property.
	         * 
	         * @param value
	         *     allowed object is
	         *     {@link String }
	         *     
	         */
	        public void setDefaultValue(String value) {
	            this.defaultValue = value;
	        }

	        /**
	         * Gets the value of the required property.
	         * 
	         * @return
	         *     possible object is
	         *     {@link String }
	         *     
	         */
	        public String getRequired() {
	            return required;
	        }

	        /**
	         * Sets the value of the required property.
	         * 
	         * @param value
	         *     allowed object is
	         *     {@link String }
	         *     
	         */
	        public void setRequired(String value) {
	            this.required = value;
	        }

	        /**
	         * Gets the value of the datatype property.
	         * 
	         * @return
	         *     possible object is
	         *     {@link String }
	         *     
	         */
	        public String getDatatype() {
	            if (datatype == null) {
	                return "string";
	            }
	            return datatype;
	        }

	        /**
	         * Sets the value of the datatype property.
	         * 
	         * @param value
	         *     allowed object is
	         *     {@link String }
	         *     
	         */
	        public void setDatatype(String value) {
	            this.datatype = value;
	        }


	        /**
	         * Returns the maximum length that this
	         * property can contain.
			 * @return the maxLength
			 */
			public int getMaxLength() {
				return maxLength;
			}

			/**
			 * Sets the maximum length that this property
			 * can contain.
			 * 
			 * @param maxLength the maxLength to set
			 */
			public void setMaxLength(int maxLength) {
				this.maxLength = maxLength;
			}


			/**
			 * Returns a regular expression that can be used to validate
			 * data input for this property.  May be null.
			 * 
			 * @return the validationRegEx
			 */
			public String getValidationRegEx() {
				return validationRegEx;
			}

			/**
			 * Sets the Validation regular expression that can be used
			 * to validate input for this property. 
			 * 
			 * @param validationRegEx the validationRegEx to set
			 */
			public void setValidationRegEx(String validationRegEx) {
				this.validationRegEx = validationRegEx;
			}


			/**
			 * Returns the help text to be displayed for this property
			 * May be null. 
			 * 
			 * @return the helpText
			 */
			public String getHelpText() {
				return helpText;
			}

			/**
			 * Sets the help text to be displayed for this property.
			 * 
			 * @param helpText the helpText to set
			 */
			public void setHelpText(String helpText) {
				this.helpText = helpText;
			}


			/**
			 * Returns a Regular Expression that can be used to 
			 * format the display of the property. May be null. 
			 * 
			 * @return the displayRegEx
			 */
			public String getDisplayRegEx() {
				return displayRegEx;
			}

			/**
			 * Sets a Regular Expression that can be used to 
			 * format the display of this property. 
			 * 
			 * @param displayRegEx the displayRegEx to set
			 */
			public void setDisplayRegEx(String displayRegEx) {
				this.displayRegEx = displayRegEx;
			}


			/**
			 * @return the validationMessage
			 */
			public String getValidationMessage() {
				return validationMessage;
			}

			/**
			 * @param validationMessage the validationMessage to set
			 */
			public void setValidationMessage(String validationMessage) {
				this.validationMessage = validationMessage;
			}


			/**
			 * When populated contains the current value
			 * if any for this property.
			 * 
			 * @return the propertyValue
			 */
			public Object getPropertyValue() {
				return propertyValue;
			}

			/**
			 * Sets the Value for this property. 
			 * 
			 * @param propertyValue the propertyValue to set
			 */
			public void setPropertyValue(Object propertyValue) {
				this.propertyValue = propertyValue;
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
	         *       &lt;attribute name="value" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
	         *       &lt;attribute name="display_value" type="{http://www.w3.org/2001/XMLSchema}string" />
	         *     &lt;/restriction>
	         *   &lt;/complexContent>
	         * &lt;/complexType>
	         * </pre>
	         * 
	         * 
	         */
	        @XmlAccessorType(XmlAccessType.FIELD)
	        @XmlType(name = "")
	        public static class EnumValue {

	            @XmlAttribute(required = true)
	            protected String value;
	            @XmlAttribute(name = "display_value")
	            protected String displayValue;

	            /**
	             * Gets the value of the value property.
	             * 
	             * @return
	             *     possible object is
	             *     {@link String }
	             *     
	             */
	            public String getValue() {
	                return value;
	            }

	            /**
	             * Sets the value of the value property.
	             * 
	             * @param value
	             *     allowed object is
	             *     {@link String }
	             *     
	             */
	            public void setValue(String value) {
	                this.value = value;
	            }

	            /**
	             * Gets the value of the displayValue property.
	             * 
	             * @return
	             *     possible object is
	             *     {@link String }
	             *     
	             */
	            public String getDisplayValue() {
	                return displayValue;
	            }

	            /**
	             * Sets the value of the displayValue property.
	             * 
	             * @param value
	             *     allowed object is
	             *     {@link String }
	             *     
	             */
	            public void setDisplayValue(String value) {
	                this.displayValue = value;
	            }

	        }
	 }


