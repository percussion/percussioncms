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
package com.percussion.share.validation;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.security.SecureStringUtils;
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
            this.defaultMessage = SecureStringUtils.sanitizeStringForHTML(
                    defaultMessage);
        }

        
        public String getCode() {
            return code;
        }

        
        public void setCode(String code) {
            this.code = SecureStringUtils.sanitizeStringForHTML(code);
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
            final StringBuffer sb = new StringBuffer("PSObjectError{");
            sb.append("code='").append(code).append('\'');
            sb.append(", defaultMessage='").append(defaultMessage).append('\'');
            sb.append(", arguments=").append(arguments);
            sb.append(", cause=").append(cause);
            sb.append('}');
            return sb.toString();
        }
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PSErrors{");
        sb.append("globalError=").append(globalError);
        sb.append('}');
        return sb.toString();
    }
}
