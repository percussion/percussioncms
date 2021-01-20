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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.test.soap;

import com.percussion.util.PSCharSets; 
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;   
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL; 
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;

import org.apache.soap.Body;
import org.apache.soap.Constants;
import org.apache.soap.Envelope;
import org.apache.soap.Fault;
import org.apache.soap.Header;

import org.apache.soap.SOAPException;
import org.apache.soap.encoding.SOAPMappingRegistry;
import org.apache.soap.messaging.Message;
import org.apache.soap.rpc.Response;
import org.apache.soap.rpc.SOAPContext;
import org.apache.soap.util.xml.XMLParserUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Generic wrapper class to make soap requests and process its response.
 */
public class PSSoapRequest
{
   /** 
    * Default constructor for derived classes.
    */
   protected PSSoapRequest()
   {
   }
   
   /**
    * Constructs this soap request object.
    * 
    * @param reqUrl the url of the soap request listener, may not be <code>null
    * </code> or empty and must be a valid url.
    * @param msgEnv the message envelope to send, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSSoapRequest(String reqUrl, Envelope msgEnv)
   {
      setRequestContent(reqUrl, msgEnv);
   }

   /**
    * Sets the request url and envelope.
    * 
    * @param reqUrl the url of the soap request listener, may not be <code>null
    * </code> or empty and must be a valid url.
    * @param msgEnv the message envelope to send, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */   
   protected void setRequestContent(String reqUrl, Envelope msgEnv)
   {
      if(reqUrl == null || reqUrl.trim().length() == 0)
         throw new IllegalArgumentException("reqUrl may not be null or empty.");
         
      try 
      {
         m_soapReqListenerURL = new URL(reqUrl);
      }
      catch(MalformedURLException ex)
      {
         throw new IllegalArgumentException("reqUrl is malformed.");
      }
      
      if(msgEnv == null)
         throw new IllegalArgumentException("msgEnv may not be null.");
         
      m_reqEnvelope = msgEnv;
   }
   
   /**
    * Checks whether the request made has generated a fault. Should be called 
    * after calling {@link #sendRequest()} to make sure response is valid.
    * 
    * @return <code>true</code> if the request generated a fault, otherwise
    * <code>false</code>.
    * 
    * @throws IllegalStateException if <code>sendRequest()</code> has not been 
    * called.
    */
   public boolean generatedFault()
   {
      checkRequestSent();
      
      return m_fault != null;
   }
   
   /**
    * Gets the request fault detail. Should be called only if a call to {@link 
    * #generatedFault()} returns <code>true</code> to know the fault reason.
    * 
    * @return the fault detail, may be <code>null</code> if the request has not
    * generated a fault.
    * 
    * @throws IllegalStateException if <code>sendRequest()</code> has not been 
    * called.
    */
   public String getFaultDetail()
   {    
      checkRequestSent();
      
      if(generatedFault())
         return m_fault.toString();
      else
         return null;
   }
   
   /**
    * Checks whether request is made yet or not. If the request is not made 
    * response might have not initialized, so all methods that deal with the
    * response should call this method to make sure it is a valid call.
    * 
    * @throws IllegalStateException if <code>sendRequest()</code> has not been 
    * called.
    */
   protected void checkRequestSent()
   {
      if(m_fault == null && m_respBody == null)
         throw new IllegalStateException("request is not yet sent");
   }
   
   /**
    * Gets the character set that should be used to convert the response content
    * bytes into characters.
    * 
    * @return the character set, never <code>null</code> or empty.
    * 
    * @throws IllegalStateException if <code>sendRequest()</code> has not been 
    * called.
    */
   public String getCharacterSet()
   {
      checkRequestSent();
      return PSCharSets.rxStdEnc();
   }

   /**
    * Gets the response header.
    *
    * @return the header, may be <code>null</code> if the response don't have
    * any header entries.
    */
   public Header getResponseHeader()
   {
      checkRequestSent();
      
      return m_respHeader;
   }

   /**
    * Sends the request and parses the response.
    *
    * @throws SOAPException if an error happens sending the request.
    * @throws SAXException if an error happens parsing the response document.
    * @throws IOException if any other io error happens.
    */
   public void sendRequest() throws SOAPException, SAXException, IOException
   {
      Message msg = new Message ();    
      //second argument actionURI is ignored because actual action uri in the
      //envelope is processed.    
      msg.send (m_soapReqListenerURL, "", m_reqEnvelope);

      BufferedReader rdr = msg.getSOAPTransport().receive();
      Document respEnvDoc;      
      try 
      {
         DocumentBuilder xdb = XMLParserUtils.getXMLDocBuilder();
         respEnvDoc = xdb.parse (new InputSource(rdr) );
      }
      catch(SAXException e)
      {
         throw e;
      }
      finally 
      {
         rdr.close();
      }
      
      SOAPContext respContext = msg.getResponseSOAPContext();
      Envelope respEnv = Envelope.unmarshall(
         respEnvDoc.getDocumentElement(), respContext);
      
      Iterator bodyEntries = respEnv.getBody().getBodyEntries().iterator();
      if(bodyEntries.hasNext())
      {
         Element childEl = (Element)bodyEntries.next();
         if(childEl.getLocalName().equals(Constants.ELEM_FAULT))
         {
            SOAPMappingRegistry soapRegistry = SOAPMappingRegistry.
               getBaseRegistry(Constants.NS_URI_CURRENT_SCHEMA_XSI);
            Response resp = Response.extractFromEnvelope(
               respEnv, soapRegistry, respContext);
            m_fault = resp.getFault();
         }
         else 
         {         
            m_respBody = respEnv.getBody();
            m_respHeader = respEnv.getHeader();
         }
      }
   }

   /**
    * Gets the response content stream.
    *
    * @return  the response input stream, never <code>null</code>
    * @throws IllegalStateException if the request is not yet made.
    * @throws IOException if an error happens getting input stream from response
    * document.
    */
   public InputStream getResponseContent() throws IOException
   {
      checkRequestSent();

      ByteArrayOutputStream outStream = new ByteArrayOutputStream();  
      OutputStreamWriter writer = new OutputStreamWriter(outStream, 
         getCharacterSet());
      if(!m_respBody.getBodyEntries().isEmpty())
      {
         Iterator bodyEntries = m_respBody.getBodyEntries().iterator();
         while(bodyEntries.hasNext())
            PSXmlDocumentBuilder.write((Element)bodyEntries.next(), writer);      
      }
      ByteArrayInputStream inStream = new ByteArrayInputStream(
         outStream.toByteArray());
      outStream.close();
      
      return inStream;
   }  

   /**
    * The soap request listener url, initialized to <code>null</code>, modified
    * through calls to <code>setRequestContent(String, Envelope)</code>. Never
    * <code>null</code> after that.
    */
   private URL m_soapReqListenerURL = null;
   
   /**
    * The soap request envelope, initialized to <code>null</code>, modified
    * through calls to <code>setRequestContent(String, Envelope)</code>. Never
    * <code>null</code> after that.
    */
   private Envelope m_reqEnvelope = null;
   
   /**
    * The soap response body, initialized to <code>null</code> and modified 
    * through calls to <code>sendRequest()</code>. If the request does not 
    * generate a soap fault, then it will not be <code>null</code>.
    */
   protected Body m_respBody = null; 
   
   /**
    * The soap response header, initialized to <code>null</code> and modified 
    * through calls to <code>sendRequest()</code>. May be <code>null</code> if 
    * the request generated a soap fault or response did not have a header.
    */
   private Header m_respHeader = null; 
   
   /**
    * The fault response, initialized to <code>null</code> and modified 
    * through calls to <code>sendRequest()</code>. May be <code>null</code> if 
    * the request is successful (response is not an envelope with soap fault).
    */
   private Fault m_fault = null;
}
