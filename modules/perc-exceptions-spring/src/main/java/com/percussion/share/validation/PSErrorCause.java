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

import com.percussion.security.SecureStringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

/**
 * A JAXB serializable Exception wrapper.
 * @author adamgent
 *
 */

@XmlRootElement(name = "ErrorCause")
public class PSErrorCause
{
	
	private static final Logger log = LogManager.getLogger("Server");
    private PSErrorCause errorCause;
    private List<PSErrorCauseElement> errorCauseStackTrace;
    private StackTraceElement[] stackTrace;
    
    private String message;
    private String localizedMessage;
    private Throwable cause;

    public PSErrorCause()
    {
        super();
    }

    public PSErrorCause(Throwable cause)
    {
        init(cause,false);
    }
    
    protected void init(Throwable t,boolean sendErrorStackToClient)
    {
        setLocalizedMessage(t.getLocalizedMessage());
        setMessage(t.getMessage());

        if (sendErrorStackToClient=true)
        {
            setStackTrace(t.getStackTrace());
        }

        setCause(t.getCause());
    }
    




    public PSErrorCause getErrorCause()
    {
        return errorCause;
    }


    public void setErrorCause(PSErrorCause cause)
    {
        this.errorCause = cause;
    }
    
    
    

    @XmlTransient
    public Throwable getCause()
    {
        return cause;
    }




    public void setCause(Throwable cause)
    {
        this.cause = cause;
        if (cause != null) {
            setErrorCause(new PSErrorCause(cause));
        }
    }




    public List<PSErrorCauseElement> getErrorCauseStackTrace()
    {
        return errorCauseStackTrace;
    }


    public void setErrorCauseStackTrace(List<PSErrorCauseElement> errorCauseStackTrace)
    {
        this.errorCauseStackTrace = errorCauseStackTrace;
    }

    

    @XmlTransient
    public StackTraceElement[] getStackTrace()
    {
        List<StackTraceElement> stack = new ArrayList<>();
        if (getErrorCauseStackTrace() != null) {
            for(PSErrorCauseElement element : getErrorCauseStackTrace()) {
                stack.add(element.getStackTraceElement());
            }
        }
        if (stackTrace != null) {
            CollectionUtils.addAll(stack, stackTrace);
        }
        return stack.toArray(new StackTraceElement[] {});
    }


    public void setStackTrace(StackTraceElement[] stackTrace)
    {
        this.stackTrace = stackTrace;
        if (stackTrace != null) {
            errorCauseStackTrace = new ArrayList<>();
            for(StackTraceElement st : stackTrace) {
                errorCauseStackTrace.add(new PSErrorCauseElement(st));
            }
        }
    }


    public String getMessage()
    {
        return message;
    }




    public void setMessage(String message)
    {
        this.message = SecureStringUtils.sanitizeStringForHTML(message);
    }




    public String getLocalizedMessage()
    {
        return localizedMessage;
    }


    public void setLocalizedMessage(String localizedMessage)
    {
        this.localizedMessage = SecureStringUtils.sanitizeStringForHTML(localizedMessage);
    }


    public static class PSErrorCauseElement {

        private String className;
        private String fileName;
        private int lineNumber;
        private String methodName;
        

        
        
        public PSErrorCauseElement()
        {
            super();
        }
        
        public StackTraceElement getStackTraceElement() {
            return new StackTraceElement(getClassName(), getMethodName(), getFileName(), getLineNumber());
        }

        public PSErrorCauseElement(StackTraceElement element)
        {
            setClassName(element.getClassName());
            setFileName(element.getFileName());
            setLineNumber(element.getLineNumber());
            setMethodName(element.getMethodName());
        }
        
 
        @XmlAttribute
        public String getClassName()
        {
            return className;
        }


        public void setClassName(String className)
        {
            this.className = className;
        }


 
        @XmlAttribute
        public String getFileName()
        {
            return fileName;
        }


        public void setFileName(String fileName)
        {
            this.fileName = fileName;
        }


        @XmlAttribute
        public int getLineNumber()
        {
            return lineNumber;
        }


        public void setLineNumber(int lineNumber)
        {
            this.lineNumber = lineNumber;
        }


        @XmlAttribute 
        public String getMethodName()
        {
            return methodName;
        }


        public void setMethodName(String methodName)
        {
            this.methodName = methodName;
        }


        
    }
    
    
    /**
     * 
     */
    private static final long serialVersionUID = -3237445850903443415L;

}
