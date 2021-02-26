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

package com.percussion.design.server;

import com.percussion.content.IPSMimeContentTypes;
import com.percussion.design.catalog.IPSCatalogErrors;
import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.security.IPSDecryptor;
import com.percussion.security.IPSKey;
import com.percussion.security.IPSSecretKey;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthenticationRequiredException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSEncryptionKeyFactory;
import com.percussion.security.PSEncryptor;
import com.percussion.security.ToDoVulnerability;
import com.percussion.server.IPSRequestHandler;
import com.percussion.server.PSRequest;
import com.percussion.server.PSResponse;
import com.percussion.server.PSServer;
import com.percussion.server.PSUserSession;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSBase64Decoder;
import com.percussion.util.PSCharSets;
import com.percussion.utils.io.PathUtils;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.security.deprecated.PSLegacyEncrypter;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;


/**
 * The PSDesignerConnectionHandler class is used to open and close
 * sessions on behalf of the designer client.
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSDesignerConnectionHandler implements IPSRequestHandler
{
   /**
    * Create a handler for designer connections.
    */
   public PSDesignerConnectionHandler()
   {
      super();   // no magic here!
   }

   /* ************ IPSRequestHandler Interface Implementation ************ */

   /**
    * Process a designer connect/disconnect request.
    * This uses the input context information and data.
    * The results are written to the specified output
    * stream using the appropriate XML document format.
    *
    * @param   request      the request object containing all context
    *                        data associated with the request
    */
   public void processRequest(PSRequest request)
   {
      Document respDoc = null;
      try {
         /* check the XML document type to determine the type of catalog
          * request we must perform
          */
         Document reqDoc = request.getInputDocument();
         if (reqDoc == null) {
            throw new PSIllegalArgumentException(
               IPSCatalogErrors.REQ_DOC_MISSING_GENERIC);
         }

         Element root = reqDoc.getDocumentElement();
         if (root == null) {
            throw new PSIllegalArgumentException(
               IPSCatalogErrors.REQ_DOC_ROOT_MISSING_GENERIC);
         }

         /* locate the catalog handler from our hash table */
         String reqType = root.getTagName();
         if (ms_openRoot.equals(reqType))
            respDoc = openSession(request);
         else if (ms_closeRoot.equals(reqType))
            respDoc = closeSession(request);
         else {
            Object[] args = { ms_openRoot + ", " + ms_closeRoot, reqType };
            throw new PSIllegalArgumentException(
               IPSCatalogErrors.REQ_DOC_INVALID_TYPE, args);
         }
      }
      catch (Exception e)
      {
         respDoc = com.percussion.error.PSErrorHandler.fillErrorResponse(e);
      }

      PSResponse response = request.getResponse();
      response.setContent(respDoc,
            IPSMimeContentTypes.MIME_TYPE_TEXT_XML
            + "; charset=" + PSCharSets.rxStdEnc());
   }

   /**
    * Shutdown the request handler, freeing any associated resources.
    */
   public void shutdown()
   {   // nothing to do here

   }

   /**
    * Opens a session to the server. Access levels may not be checked.
    *
    */
   private Document openSession(PSRequest request)
      throws PSAuthorizationException,
         PSAuthenticationRequiredException,
         PSAuthenticationFailedException
   {
      PSXmlTreeWalker w = new PSXmlTreeWalker(
         request.getInputDocument().getDocumentElement());

      String loginId = w.getElementData("loginid");
      String loginPw = w.getElementData("loginpw");
      String sessId =  w.getElementData("sessid");
      String encrypted = w.getElementData("encrypted");
      boolean isEncrypted = (encrypted != null && encrypted.equalsIgnoreCase("yes"));
      String locale = w.getElementData("locale");

      if (loginPw == null)
         loginPw = "";

      if (loginPw == null || loginPw.length() == 0)
         loginPw = "";
      else if (isEncrypted) { // fix bug #Rx-99-12-0016
         try {
            loginPw = PSEncryptor.getInstance("AES",
                    PathUtils.getRxDir(null).getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
            ).decrypt(loginPw);
         } catch (PSEncryptionException e) {
            loginPw = eatLasagna(loginId, sessId, loginPw);
         }


      }
      else
         loginPw = PSBase64Decoder.decode(loginPw);

      java.util.HashMap params = request.getParameters();
      if (params == null) {
         params = new java.util.HashMap();
         request.setParameters(params);
      }

      params.put("loginid", loginId);
      params.put("loginpw", loginPw);

      boolean freeSession = true;
      try {
         try {
            // first see if they're granted design access (more common type)
            PSServer.checkAccessLevel(
               request, PSAclEntry.SACE_ACCESS_DESIGN);
            freeSession = false;
         } catch (PSAuthenticationRequiredException e2) {
            // do the have admin access? if not, this will also throw an
            // authorization exception, which is ok at this point
            PSServer.checkAccessLevel(
               request, PSAclEntry.SACE_ADMINISTER_SERVER);
         }
      }
      finally
      {
         if (freeSession)
            com.percussion.server.PSUserSessionManager.releaseUserSession(
               request.getUserSession());
      }

      // if we've made it here, we have the appropriate access
      if(null != locale && locale.length() > 0)
         request.getUserSession().
            setPrivateObject(IPSHtmlParameters.SYS_LANG, locale);
      
        String jsessionid = (String) PSRequestInfo.getRequestInfo(
           PSRequestInfo.KEY_JSESSIONID);      
      
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(
         respDoc, ms_openRoot + "Response");
      root.setAttribute("sessid", request.getUserSessionId());
        root.setAttribute("jsessid", jsessionid);
      root.setAttribute("loginid", loginId);

      /* need to keep setting old version attribute in case old workbench
         connects to newer server */
      root.setAttribute("version", ms_version.getVersion());
      root.appendChild(ms_version.toXml(respDoc));

      return respDoc;
   }

   private Document closeSession(PSRequest request)
   {   // an empty response doc means success
      PSUserSession sess = request.getUserSession();
      if (sess != null)
         com.percussion.server.PSUserSessionManager.releaseUserSession(sess);

      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(respDoc, ms_closeRoot + "Response");
      return respDoc;
   }

   @ToDoVulnerability
   @Deprecated
   private String eatLasagna(String uid, String sessId, String str)
   {
      if ((str == null) || (str.equals("")))
         return "";

      int partone = PSLegacyEncrypter.getInstance(PathUtils.getRxDir(null).getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
      ).OLD_SECURITY_KEY().hashCode();
      int parttwo;
      if (uid == null || uid.equals(""))
         parttwo = PSLegacyEncrypter.getInstance(PathUtils.getRxDir(null).getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
         ).OLD_SECURITY_KEY2().hashCode();
      else
         parttwo = uid.hashCode();

      partone /= 7;
      parttwo /= 13;

      try {
         int padLen = 0;
         ByteArrayOutputStream bOut = new ByteArrayOutputStream();
         com.percussion.util.PSBase64Decoder.decode(
            new ByteArrayInputStream(str.getBytes(PSCharSets.rxJavaEnc())), bOut);

         IPSKey key = PSEncryptionKeyFactory.getKeyGenerator(PSEncryptionKeyFactory.DES_ALGORITHM);
         if ((key != null) && (key instanceof IPSSecretKey))
         {
            IPSSecretKey secretKey = (IPSSecretKey)key;
            byte[] baOuter = new byte[8];
            for (int i = 0; i < 4; i++)
               baOuter[i] = (byte)((partone >> i) & 0xFF);
            for (int i = 4; i < 8; i++)
               baOuter[i] = (byte)((parttwo >> (i-4)) & 0xFF);

            secretKey.setSecret(baOuter);
            IPSDecryptor decr = secretKey.getDecryptor();

            ByteArrayOutputStream bOut2 = new ByteArrayOutputStream();
            decr.decrypt(new ByteArrayInputStream(bOut.toByteArray()), bOut2);
            byte[] bTemp = bOut2.toByteArray();

            byte[] baInner = new byte[8];
            System.arraycopy(bTemp, 0, baInner, 0, 4);
            System.arraycopy(bTemp, bTemp.length - 4, baInner, 4, 4);
            int innerDataLength = bTemp.length - 8;

            for (int i = 0; i < 8; i++)
               baInner[i] ^= (byte) ((1 << i) & innerDataLength);

            padLen = baInner[0];

            secretKey.setSecret(baInner);
            bOut = new ByteArrayOutputStream();
            decr.decrypt(
               new ByteArrayInputStream(bTemp, 4, innerDataLength), bOut);
         }

         String ret = bOut.toString(PSCharSets.rxJavaEnc());
         // pad must be between 1 and 7 bytes, fix for bug id Rx-99-11-0049
         if ((padLen > 0) & (padLen  < 8))
            ret = ret.substring(0, ret.length() - padLen);

         return ret;
      } catch (Exception e) {
         // we were returning null which caused a decryption error downstream
         // now we return ""
         return "";
      }
   }


   static final String UTIL_PKG_NAME = "com.percussion.util";
   static com.percussion.util.PSFormatVersion ms_version = new com.percussion.util.PSFormatVersion(UTIL_PKG_NAME);

   private static final String   ms_openRoot      = "PSXDesignOpen";
   private static final String   ms_closeRoot   = "PSXDesignClose";
}

