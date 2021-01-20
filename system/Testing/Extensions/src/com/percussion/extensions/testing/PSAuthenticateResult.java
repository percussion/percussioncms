/******************************************************************************
 *
 * [ PSAuthenticateResult.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.extensions.testing;

import com.percussion.design.objectstore.PSSubject;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This exit is used for authentication tests. It returns the authenticated 
 * subject with all attributes and roles in the returned result document. The
 * DTD is like this:
 * &lt;!ELEMENT AuthenticatedSubject (PSXSubject)&gt;
 * &lt;!ATTLIST AuthenticatedSubject
 *    roles CDATA #REQUIRED
 * &gt;
 */
public class PSAuthenticateResult extends PSDefaultExtension
   implements IPSResultDocumentProcessor
{
   /**
    * This exit does whether take exit nor HTML parameters.
    * 
    * @see IPSResultDocumentProcessor#processResultDocument(Object[], 
    *    IPSRequestContext, Document)
    */
   public Document processResultDocument(
         @SuppressWarnings("unused") Object[] params,
         IPSRequestContext request,
         @SuppressWarnings("unused") Document resultDoc)
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = doc.createElement("AuthenticatedSubject");
      doc.appendChild(root);

      String roleNames = "";
      Iterator roles = request.getSubjectRoles().iterator();
      while (roles.hasNext())
      {
         String role = (String) roles.next();
         if (roleNames.length() > 0)
            roleNames += ", ";
         roleNames += role;
      }
      if (roleNames.length() > 0)
         root.setAttribute("roles", roleNames);
      
      List subjects = request.getSubjectGlobalAttributes(
         request.getUserName(), 0, null, null, true);

      if (!subjects.isEmpty())
      {
         // there is always just one authenticated subject
         PSSubject subject = (PSSubject) subjects.get(0);
         root.appendChild(subject.toXml(doc));
      }
      
      return doc;
   }
   
   /* (non-Javadoc)
    * @see IPSResultDocumentProcessor#canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }
}
