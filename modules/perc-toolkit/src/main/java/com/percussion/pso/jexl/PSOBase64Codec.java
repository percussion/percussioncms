/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.jexl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import org.apache.commons.codec.binary.Base64;
import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.utils.tools.PSCopyStream;

/**
 * 
 *
 * @author DavidBenua
 *
 */
public class PSOBase64Codec extends PSJexlUtilBase implements IPSJexlExpression
{
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(PSOBase64Codec.class);

   /**
    * 
    */
   public PSOBase64Codec()
   {
      super();
   }

   /**
    * Base64 encode a binary property
    * @param jcrProperty the property to encode
    * @return a base 64 encoded String representing the property value.
    * @throws ValueFormatException
    * @throws RepositoryException
    * @throws IOException 
    * @throws UnsupportedEncodingException 
    */
   @IPSJexlMethod(description="Encode a binary property", 
         params={
        @IPSJexlParam(name="source", description="binary property to encode")})
   public String encode(Property jcrProperty) throws ValueFormatException, RepositoryException, UnsupportedEncodingException, IOException
   {
      if(jcrProperty == null)
      {
         log.debug("Property is null, no base64 encoding is possible" ); 
         return null; 
      }
      return encode(jcrProperty.getStream()); 
   }

   /**
    * Base64 encode a binary stream.  
    * @param stream the byte stream to be encoded.
    * @return a base 64 encoded String representing the binary value. 
    * @throws IOException 
    * @throws UnsupportedEncodingException 
    */
   @IPSJexlMethod(description="Encode a binary stream", 
         params={
        @IPSJexlParam(name="source", description="binary stream to encode")})
   public String encode(InputStream stream) throws UnsupportedEncodingException, IOException
   {
      return encode(streamToBytes(stream));
   }
   
   /**
    * Copy a binary stream into a byte array.
    * @param stream the binary stream to be copied
    * @return the byte array. Will be <code>null</code> if the 
    * stream is <code>null</code>. 
    * @throws IOException if a memory error occurs. 
    */
   @IPSJexlMethod(description="copy a binary stream to a byte array", 
         params={
        @IPSJexlParam(name="stream", description="binary stream to copy")})
   public byte[] streamToBytes(InputStream stream) throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
      try
      {
         if(stream == null)
         {
            log.debug("No Stream found, cannot base64 encode ");
            return null;
         }
         PSCopyStream.copyStream(stream, baos);
      } catch (IOException ex)
      {
        // should never happen unless we run out of memory
        log.error("Unexpected exception " + ex.getMessage(), ex);
        throw ex;
      } 
      return baos.toByteArray();
   }
   
   /**
    * Base 54 encode a byte array
    * @param bytes the byte array to be encoded. 
    * @return a base64 encoded String representing the binary value. 
    * @throws UnsupportedEncodingException if an error occurs. ASCII 
    * should always be supported, so this exception is impossible.  
    */
   @IPSJexlMethod(description="Encode a binary byte array", 
         params={
        @IPSJexlParam(name="source", description="binary byte array to encode")})
   public String encode(byte[] bytes) throws UnsupportedEncodingException
   {
      byte[] out = Base64.encodeBase64(bytes);
      try
      {
         return new String(out,"ASCII"); //base64 strings are ASCII only
      } catch (UnsupportedEncodingException ex)
      {
         //ASCII is always supported, this should never happen 
         log.error("Unsupported Encoding " + ex.getMessage(), ex);
         throw ex; 
      }
      
   }
}
