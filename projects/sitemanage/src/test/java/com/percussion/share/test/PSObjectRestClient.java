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
package com.percussion.share.test;

import com.percussion.share.dao.PSSerializerUtils;
import com.percussion.share.validation.PSErrorCause;
import com.percussion.share.validation.PSErrors;
import com.percussion.share.validation.PSValidationErrors;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.STAXEventReader;

/**
 * 
 * A Rest client that handles un/marshalling of objects
 * from responses and to requests.
 * <p>
 * The serialization is done through JAXB.
 * 
 * @author adamgent
 *
 */
public class PSObjectRestClient extends PSRestClient
{
    
    public PSObjectRestClient()
    {
        super();
        getRequestHeaders().put("Accept", "text/xml");
    }

    public PSObjectRestClient(String baseUrl)
    {
       this();
       setUrl(baseUrl);
    }
    
    public void switchCommunity(Integer id)
    {
        Map<String,String> params = new HashMap<String, String>();
        params.put("sys_community", id.toString());
        POST("/Rhythmyx/sys_welcome/login.html", params.entrySet());
    }

    public void login(String user, String password)
    {
        Map<String,String> params = new HashMap<String, String>();
        params.put("j_username", user);
        params.put("j_password", password);
        POST("/Rhythmyx/login?sys_redirect=http%3a%2f%2flocalhost%3a9992%2fRhythmyx%2ftest%2fsearch.jsp", params.entrySet());
    }

    public void delete(String path) {
        try
        {
            DELETE(path);
        }
        catch (RestClientException e)
        {
            this.handleException(e);
        }
    }
    
    protected <T, R> List<R> deleteObjectFromPathAndGetObjects(String path, Class<R> responseType)
    {
        try
        {
            return objectsFromResponseBody(deleteObjectFromPath(path), responseType);
        }
        catch (RestClientException e)
        {
            return this.<List<R>>handleException(e);
        }
    }
    
    protected <T> String deleteObjectFromPath(String path)
    {
        try
        {
            return DELETE(path);
        }
        catch (RestClientException e)
        {
            return this.<String>handleException(e);
        }
    }
    

    protected <T> T getObjectFromPath(String path, Class<T> type)
    {
        try
        {
            return objectFromResponseBody(GET(path), type);
        }
        catch (RestClientException e) {
            return this.<T>handleException(e);
        }
    }

    protected <T> List<T> getObjectsFromPath(String path, Class<T> type)
    {
        try
        {
            return objectsFromResponseBody(GET(path), type);
        }
        catch (RestClientException e)
        {
            return this.<List<T>>handleException(e);
        }
    }
    
    
    private <T> T handleException(RestClientException e) {
        if (e.getStatus() == 400)
            throw new DataValidationRestClientException(e);
        else if (e.getStatus() == 500)
            throw new DataRestClientException(e);
        else
            throw e;  
    }
    
    protected <T> String postObjectToPath(String path, T object)
    {
        try
        {
            return POST(path, objectToRequestBody(object));
        }
        catch (RestClientException e)
        {
            return this.<String>handleException(e);
        }
    }
    
    protected <T> String putObjectToPath(String path, T object)
    {
        try
        {
            return PUT(path, objectToRequestBody(object));
        }
        catch (RestClientException e)
        {
            return this.<String>handleException(e);
        }
    }
    
    protected <T, R> R putObjectToPath(String path, T object, Class<R> responseType)
    {
        try
        {
            return objectFromResponseBody(putObjectToPath(path, object), responseType);
        }
        catch (RestClientException e)
        {
            return this.<R>handleException(e);
        }
    }

    protected <T, R> List<R> postObjectToPathAndGetObjects(String path, T object, Class<R> responseType)
    {

        try
        {
            return objectsFromResponseBody(postObjectToPath(path, object), responseType);
        }
        catch (RestClientException e)
        {
            return this.<List<R>>handleException(e);
        }
    }
    
    protected <T, R> R postObjectToPath(String path, T object, Class<R> responseType)
    {

        try
        {
            return objectFromResponseBody(postObjectToPath(path, object), responseType);
        }
        catch (RestClientException e)
        {
            return this.<R>handleException(e);
        }
    }
    

    protected <T> String objectToRequestBody(T data)
    {
        StringWriter sw = new StringWriter();
        JAXB.marshal(data, sw);
        return sw.getBuffer().toString();
    }

    @SuppressWarnings("unchecked")
    protected <T> T objectFromResponseBody(String response, Class<T> type)
    {
        try
        {
            JAXBContext context = JAXBContext.newInstance(type);
            Unmarshaller u =  context.createUnmarshaller();
            return (T) u.unmarshal(new StringReader(response));
        }
        catch (JAXBException e)
        {
            throw new DataRestClientMarshalException("Error unmarshaling", e);
        }
    }
    
    public <T> String objectToJson(T object) {
        try
        {
            return PSSerializerUtils.getJsonXmlFromObject(object);
        }
        catch (Exception e)
        {
            throw new DataRestClientMarshalException("Error converting to JSON", e);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> List<T> objectsFromResponseBody(String response, Class<T> type)
    {
        //JAXB can't handle lists that well with out help.
        //CXF has the help built in.
        try {
            STAXEventReader reader = new STAXEventReader();
            StringReader sr = new StringReader(response);
            Document doc = reader.readDocument(sr);
            List<Element> es = doc.getRootElement().elements();
            ArrayList<T> list = new ArrayList<T>();
            for (Element e : es) {
                list.add(objectFromResponseBody(e.asXML(), type));
            }
            return list;
        } 
        catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public static class DataValidationRestClientException extends DataRestClientException {
    
            private static final long serialVersionUID = 1L;
            private PSValidationErrors errors;
            
            public DataValidationRestClientException(RestClientException cause) {
                super(cause);
            }
    
            @Override
            protected void setErrorResponse(String response) {
                setErrors(fromXml(response));
            }
            
            @Override
            public PSValidationErrors getErrors() {
                return errors;
            }
    
            
            public void setErrors(PSValidationErrors errors) {
                this.errors = errors;
            }
            
            @Override
            protected PSValidationErrors fromXml(String xml) {
                try
                {
                    return JAXB.unmarshal(new StringReader(xml), PSValidationErrors.class);
                }
                catch (Exception e)
                {
                    log.error("Failed to get errors object from xml");
                }
                return new PSValidationErrors();
            }
            
            /**
             * The log instance to use for this class, never <code>null</code>.
             */
            private static final Logger log = LogManager.getLogger(DataValidationRestClientException.class);
        }
    
    public static class DataRestClientException extends RestClientException {
        
        private static final long serialVersionUID = 1L;
        private PSErrors errors;
        private RestClientException restClientException;

        public DataRestClientException(RestClientException cause) {
            super(cause);
            restClientException = cause;
            if (cause.getResponseBody() != null) {
                setErrorResponse(cause.getResponseBody());
            }
            else {
                fillInStackTrace();
            }
        }
        
        public DataRestClientException(DataRestClientException parent, PSErrorCause ec) {
            restClientException = parent.restClientException;
            init(ec);
        }
        
        protected void init(PSErrorCause ec) {
            setStackTrace(ec.getStackTrace());
            PSErrorCause child = ec.getErrorCause();
            initCause(child);
        }
        
        protected void initCause(PSErrorCause child) {
            if (child != null)
                initCause(new DataRestClientException(this,child));
            else if(restClientException != null)
                initCause(restClientException);
        }
        
        protected void setStackTrace(PSErrorCause ec) {
            if (ec != null)
                setStackTrace(ec.getStackTrace());
        }
        
        
        @Override
        public String getMessage()
        {
            if(hasException())  {
                return errors.getGlobalError().getCause().getMessage();
            }
            return super.getMessage();
        }

        private boolean hasException() {
            return (errors != null 
                    && errors.getGlobalError() != null 
                    && errors.getGlobalError().getCause() != null);
        }

        protected void setErrorResponse(String response) {
            setErrors(fromXml(response));
        }
        
        public PSErrors getErrors() {
            return errors;
        }

        protected void setErrors(PSErrors errors) {
            this.errors = errors;
            if(hasException()) {
                setStackTrace(errors.getGlobalError().getCause());
                initCause(errors.getGlobalError().getCause().getErrorCause());
            }
        }
        
        protected PSErrors fromXml(String xml) {
            try
            {
                return JAXB.unmarshal(new StringReader(xml), PSErrors.class);
            }
            catch (Exception e)
            {
                log.error("Failed to get errors object from xml");
            }
            return new PSErrors();
        }



        /**
         * The log instance to use for this class, never <code>null</code>.
         */
        private static final Logger log = LogManager.getLogger(DataRestClientException.class);
        
    }
    
    
    public static class DataRestClientMarshalException extends RuntimeException
    {

        private static final long serialVersionUID = 1L;

        public DataRestClientMarshalException(String message)
        {
            super(message);
        }

        public DataRestClientMarshalException(String message, Throwable cause)
        {
            super(message, cause);
        }

        public DataRestClientMarshalException(Throwable cause)
        {
            super(cause);
        }

    }



}
