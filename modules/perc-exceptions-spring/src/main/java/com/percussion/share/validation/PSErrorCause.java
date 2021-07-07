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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.percussion.utils.security.PSSecurityUtility;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A JAXB serializable Exception wrapper.
 * @author adamgent
 *
 */

@XmlRootElement(name = "ErrorCause")
public class PSErrorCause
{
	
	private static final Logger log = LogManager.getLogger(PSErrorCause.class);
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
        this.message = PSSecurityUtility.sanitizeStringForHTML(message);
    }




    public String getLocalizedMessage()
    {
        return localizedMessage;
    }


    public void setLocalizedMessage(String localizedMessage)
    {
        this.localizedMessage = PSSecurityUtility.sanitizeStringForHTML(localizedMessage);
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
