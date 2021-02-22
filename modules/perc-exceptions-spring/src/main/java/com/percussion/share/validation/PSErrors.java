/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.share.validation;

import com.fasterxml.jackson.annotation.JsonRootName;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlRootElement;
import java.text.MessageFormat;
import java.util.List;


/**
 * A data object that represents global errors.
 * <p>
 * The object some what mirrors the Spring validation framework Errors object
 * and its children. The big difference is that this object is safe to serialize
 * with JAXB but the spring errors object is not.
 * <p>
 * The object is safe to serialize with JAXB.
 * 
 * @see PSValidationErrors
 * @author adamgent
 *
 */
@XmlRootElement(name = "Errors")
@JsonRootName(value="Errors")
public class PSErrors {
    
    private PSObjectError globalError;

    
    public PSObjectError getGlobalError() {
        return globalError;
    }

    
    public void setGlobalError(PSObjectError globalError) {
        this.globalError = globalError;
    }

    /**
     * See Spring's ObjectError
     *  
     * @author adamgent
     *
     */
    @XmlRootElement(name="Error")
    @JsonRootName(value="Error")
    public static class PSObjectError {
        
        private String code;
        private String defaultMessage;
        private List<String> arguments;
        private PSErrorCause cause;

        
        public PSObjectError() {
            super();
        }
        
        /**
         * Parameters for the error message.
         * The parameters are just like 
         * {@link MessageFormat} parameters.
         * @return maybe <code>null</code>.
         */
        public List<String> getArguments()
        {
            return arguments;
        }

        public void setArguments(List<String> arguments)
        {
            this.arguments = arguments;
        }

        /**
         * The default message if localization is not done.
         * @return maybe <code>null</code>.
         */
        public String getDefaultMessage() {
            return defaultMessage;
        }

        
        public void setDefaultMessage(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }

        
        public String getCode() {
            return code;
        }

        
        public void setCode(String code) {
            this.code = code;
        }
        
        


        public PSErrorCause getCause()
        {
            return cause;
        }


        public void setCause(PSErrorCause cause)
        {
            this.cause = cause;
        }


        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
        }
        
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
    
    
}
