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
package com.percussion.cms.objectstore.client;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSProcessingStatistics;
import com.percussion.cms.objectstore.PSProcessorCommon;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.IPSRemoteRequester;
import com.percussion.util.PSUrlUtils;
import com.percussion.util.PSXMLDomUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;



/**
 * This class contains the know how to implement the necessary processing
 * when operating in a different JVM than the server. It uses http requests
 * to accomplish its work. All requests are performed on behalf of
 * the user specified in the config params.
 * <p>Generally, this is designed for use by remote clients such as the
 * workbench.
 *
 * @author Paul Howard
 * @version 1.0
 */
public class PSRemoteProcessor extends PSProcessorCommon
{
   /**
    * Creates a processor that can fulfill database operation requests Remotely
    * from the Rhythmyx server. Implementors
    * should not instantiate this class directly but should use the {@link
    * com.percussion.cms.objectstore.PSProcessorProxy PSProcessorProxy} class.
    * <p>See {@link PSServerProcessor#PSServerProcessor(Map) base class}
    * for further details. (Note, parameters (except ssl) are the same as
    * those of the {@link com.percussion.conn.PSDesignerConnection
    * PSDesignerConnection} class.
    * <table border="1">
    *    <tr>
    *       <th>Key</th>
    *       <th>Value</th>
    *    </tr>
    *    <tr>
    *       <td>hostName</td>
    *       <td>The name of the Rx server machine, required.</td>
    *    </tr>
    *    <tr>
    *       <td>port</td>
    *       <td>The port the Rx server is listening on. If not provided,
    *          9992 is used for non-ssl and 9443 for ssl.</td>
    *    </tr>
    *    <tr>
    *       <td>loginId</td>
    *       <td>The user name to use when connecting. If empty all connections
    *          will be made anonymously.</td>
    *    </tr>
    *    <tr>
    *       <td>loginPw</td>
    *       <td>The password to use when connecting. If not provided, "" is
    *          used. Must be unencrypted.</td>
    *    </tr>
    *    <tr>
    *       <td>useSSL</td>
    *       <td>A flag to indicate whether the connection should be encrypted.
    *          If 'true', then uses an SSL socket for communication. Any other
    *          value, or absence of the property and the connection will be
    *          made without SSL. If <code>true</code>, the supplied port
    *          must accept ssl connection requests.</td>
    *    </tr>
    *    <tr>
    *       <td>serverRoot</td>
    *       <td>The server's request root. If not supplied, Rhythmyx is used.
    *          </td>
    *    </tr>
    * </table>
    * <p>See {@link PSServerProcessor#PSServerProcessor(Map) base class}
    * for further details.
    *
    * @param connInfo Never <code>null</code>. All work is performed as the user
    *    identified with these connection parameters. See description for
    *    required and optional parameters. All property names are
    *    case-sensitive. The needed props are read and stored locally. If any
    *    required props are missing or empty or any integral props can't be
    *    parsed, an IAE is thrown.
    */
   public PSRemoteProcessor(IPSRemoteRequester conn, Map procConfig)
   {
      super(procConfig);
      if (null == conn)
      {
         throw new IllegalArgumentException(
               "Connection information must be supplied.");
      }
      
      m_conn = conn;
   }

   /**
    * See base class for details.
    * <li>For each entry in ids, create N html parameters whose name is the name
    *    of the entry key. The value of each instance should be the value of
    *    one of the entries in the associated collection.</li>
    * <li>Generate an http/s request to the resource specified in
    *    loadResource.</li>
    */
   protected Document doLoad(String resourceName, Map ids)
      throws PSCmsException
   {
      String path = "";
      try
      {
         Map params = new HashMap();
         Iterator pairs = ids.keySet().iterator();
         while (pairs.hasNext())
         {
            String keyPartName = (String) pairs.next();
            String[] idSet = (String[]) ids.get(keyPartName);
            if ( idSet.length > 1 )
            {
               ArrayList l = new ArrayList();
               for (int i=0; i < idSet.length; i++)
                  l.add(idSet[i]);
               params.put(keyPartName, l);
            }
            else
               params.put( keyPartName, idSet[0]);
         }

         return m_conn.getDocument(resourceName, params);
      }
      catch (IOException ioe)
      {
         String[] args =
         {
            "request url: " + path,
            ioe.getLocalizedMessage()
         };
         throw new PSCmsException(IPSCmsErrors.COMM_ERROR_WITH_SERVER, args);
      }
      catch (SAXException se)
      {
         String[] args =
         {
            "request url: " + path,
            se.getLocalizedMessage()
         };
         throw new PSCmsException(IPSCmsErrors.SAX_PROCESSING_EXCEPTION, args);
      }
   }

   //see interface for description
   protected int doDelete(String resourceName, Map ids)
      throws PSCmsException
   {
      Element root = null;
      try
      {
         Map params = new HashMap();
         Iterator pairs = ids.keySet().iterator();
         while (pairs.hasNext())
         {
            String keyPartName = (String) pairs.next();
            String[] idSet = (String[]) ids.get(keyPartName);
            if ( idSet.length > 1 )
            {
               ArrayList l = new ArrayList();
               for (int i=0; i < idSet.length; i++)
                  l.add(idSet[i]);
               params.put(keyPartName, l);
            }
            else
               params.put( keyPartName, idSet[0]);
         }
         Document doc = m_conn.getDocument(resourceName, params);
         if (null == doc || null == doc.getDocumentElement())
         {
            throw new PSCmsException(IPSObjectStoreErrors.XML_ELEMENT_NULL,
               "PSXExecStatistics");
         }

         PSProcessingStatistics stats = new PSProcessingStatistics(root);
         return stats.getDeletedCount();
      }
      catch (IOException ioe)
      {
         String[] args =
         {
            "request partial url: " + resourceName,
            ioe.getLocalizedMessage()
         };
         throw new PSCmsException(IPSCmsErrors.COMM_ERROR_WITH_SERVER, args);
      }
      catch (SAXException se)
      {
         String[] args =
         {
            "request partial url: " + resourceName,
            se.getLocalizedMessage()
         };
         throw new PSCmsException(IPSCmsErrors.SAX_PROCESSING_EXCEPTION, args);
      }
      catch (PSUnknownNodeTypeException unte)
      {
         throw new PSCmsException(unte.getErrorCode(),
               unte.getErrorArguments());
      }
   }


   //see base class
   protected int[] doAllocateIds(String lookup, int count)
      throws PSCmsException
   {
      String number = "";
      //used if exception occurs
      String errPath = "";
      try
      {
         Map params = new HashMap();
         params.put("sys_lookupkey", lookup);
         params.put("sys_idcount", ""+count);
         /*resource returns doc of form
            <PSXIdGenerator key="lookup" firstId="100", count="count or less">*/
         //String path = "/" + m_serverRoot + "/sys_psxCms/idgen.xml";
         errPath = PSUrlUtils.createUrl("/sys_psxCms/idgen.xml", 
            params.entrySet().iterator(),
               null);
         Document doc = m_conn.getDocument("sys_psxCms/idgen.xml", params);
         String nodeName = "PSXIdGenerator";
         if (null == doc || null == doc.getDocumentElement())
         {
            throw new PSCmsException(IPSObjectStoreErrors.XML_ELEMENT_NULL,
               nodeName);
         }

         Element root = doc.getDocumentElement();
         PSXMLDomUtil.checkNode(root, nodeName);

         number = root.getAttribute("firstId");
         int firstId = Integer.parseInt(number);
         number = root.getAttribute("count");
         int returnedIds = Integer.parseInt(number);
         int[] result = new int[count];
         for (int i=0; i < returnedIds; i++)
         {
            result[i] = firstId+i;
         }
         return result;
      }
      catch (NumberFormatException nfe)
      {
         throw new PSCmsException(1000,
               "Bad number returned by id generator app: '" + number + "'.");
      }
      catch (IOException ioe)
      {
         String[] args =
         {
            "request url: " + errPath,
            ioe.getLocalizedMessage()
         };
         throw new PSCmsException(IPSCmsErrors.COMM_ERROR_WITH_SERVER, args);
      }
      catch (SAXException se)
      {
         String[] args =
         {
            "request url: " + errPath,
            se.getLocalizedMessage()
         };
         throw new PSCmsException(IPSCmsErrors.SAX_PROCESSING_EXCEPTION, args);
      }
      catch (PSUnknownNodeTypeException unte)
      {
         throw new PSCmsException(unte.getErrorCode(),
               unte.getErrorArguments());
      }
   }


   //see base class for description
   protected PSProcessingStatistics doSave(String resourceName, Document input)
      throws PSCmsException
   {
      Element root = null;
      //String path = "/" + m_serverRoot + "/" + resourceName;
      try
      {
         Document doc = m_conn.sendUpdate(resourceName, input);

         if (null == doc)
            ;//todo: throw or return all 0's?

         root = doc.getDocumentElement();
         if (null == root)
            ;//todo: throw or return all 0's?

         return new PSProcessingStatistics(doc);
      }
      catch (IOException ioe)
      {
         String[] args =
         {
            "request url (posting xml): " + resourceName,
            ioe.getLocalizedMessage()
         };
         throw new PSCmsException(IPSCmsErrors.COMM_ERROR_WITH_SERVER, args);
      }
      catch (SAXException se)
      {
         String[] args =
         {
            "request url (posting xml): " + resourceName,
            se.getLocalizedMessage()
         };
         throw new PSCmsException(IPSCmsErrors.SAX_PROCESSING_EXCEPTION, args);
      }
      catch (PSUnknownNodeTypeException unte)
      {
         throw new PSCmsException(unte.getErrorCode(),
               unte.getErrorArguments());
      }
   }


   /**
    * Object used to make the requests to the server. Never <code>null</code>
    * after construction.
    */
   private IPSRemoteRequester m_conn = null;

}
