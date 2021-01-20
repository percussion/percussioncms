/******************************************************************************
 *
 * [ PSContentListItem.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.fastforward.sfp;

import com.percussion.data.macro.PSMacroUtils;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Represents a single content item/variant in a publishing content list.
 */
public class PSContentListItem implements Comparable
{   
   /**
    * Ctor taking all the attributes of a content list item to be
    * published/previewed
    * 
    * @param contentId ContentId of the item to be published, must not be
    *           <code>null</code>.
    * @param revision Revision of the item to be published, must not be
    *           <code>null</code>. It is important to note that when the item
    *           is serielized as an XML document using
    *           {@link #toXml(Document, IPSRequestContext)}, the revision is
    *           corrected to represent the last public revision if the item is
    *           in "i" state (Quick Edit).
    * @param variantId VariantId of the item to be published, must not be
    *           <code>null</code>.
    * @param variantBase Assembly url for the variant specified by the
    *           variantid, must not be <code>null</code>
    * @param contentTitle content title of the item, may be <code>null</code>
    *           or empty.
    * @param folderId Item's parent fodlerid, may be <code>null</code> or
    *           empty.
    * @param lastModifiedDate item's last modified date, may be
    *           <code>null</code>.
    * @param expiryDate item's expiry, may be <code>null</code>.
    * @param lastModifier last modifer's name, may be <code>null</code> or
    *           empty.
    * @param contentTypeId content typeid of the item, may be <code>null</code>
    *           or empty.
    * @param filenameContext context value, may be <code>null</code> or empty.
    * @param folderPath item's parent folder path, may be <code>null</code> or
    *           empty.
    * @param linkGenerator link generator object, must not be <code>null</code>.
    * @param unpublishValue unpublish attribute value, may be <code>null</code>
    *           or empty.
    * @param protocol the URL protocol to use when creating content URLs, never
    *           <code>null</code> or empty
    * @param host the host name or ip address to use when creating content URLs,
    *           never <code>null</code> or empty
    * @param port the port number to use when creating content URLs
    * @param paramSetToPass Set of names of non-standard HTML parameters to pass
    *           from request context to each content item url in the content
    *           list, may be <code>null</code> or empty.
    * @param getLastPubRev4QEState <code>true</code> if need to get the last 
    *           public revision when invoking 
    *           {@link #toXml(Document, IPSRequestContext)}. This flag will
    *           set to <code>false</code> by 
    *           {@link #setLastPublicRevision(String)}.
    */
   public PSContentListItem( 
      String contentId,
      String revision,
      String variantId,
      String variantBase,
      String contentTitle,
      String folderId,
      Date lastModifiedDate,
      Date expiryDate,
      String lastModifier,
      String contentTypeId,
      String filenameContext,
      String folderPath,
      PSSiteFolderContentListLinkGenerator linkGenerator,
      String unpublishValue, 
      String protocol, String host, int port,
      Set paramSetToPass,
      boolean getLastPubRev4QEState)
   {
      if (contentId == null || contentId.length() < 1)
      {
         throw new IllegalArgumentException(
               "contentId must not be null or empty");
      }
      if (revision == null || revision.length() < 1)
      {
         throw new IllegalArgumentException(
               "revision must not be null or empty");
      }

      if (linkGenerator == null)
      {
         throw new IllegalArgumentException("linkGenerator must not be null");
      }
      if (protocol == null || protocol.trim().length() == 0)
      {
         throw new IllegalArgumentException("protocol must not be null or empty");
      }
      if (host == null || host.trim().length() == 0)
      {
         throw new IllegalArgumentException("host must not be null or empty");
      }
      m_contentId = contentId;
      m_revision = revision;
      m_variantId = variantId;
      m_variantBase = variantBase;
      m_contentTitle = contentTitle;
      m_folderId = folderId;
      m_lastModifiedDate = lastModifiedDate;
      m_expiryDate = expiryDate;
      m_lastModifier = lastModifier;
      m_contentTypeId = contentTypeId;
      m_filenameContext = filenameContext;
      m_folderPath = folderPath;
      m_linkGenerator = linkGenerator;
      m_unpublishValue = unpublishValue;
      m_paramSetToPass = paramSetToPass;
      m_host = host;
      m_port = port;
      m_protocol = protocol;
      m_getLastPubRev4QEState = getLastPubRev4QEState;
   }

   /**
    * Create an instance of the class from its XML representation. The XML
    * representation is in the format below:
    * <pre><code>
    * &lt;!ELEMENT contentitem (title?, contenturl, delivery, modifydate, 
    *                            modifyuser, expiredate, contenttype )>
    * &lt;!ELEMENT contenttype (#PCDATA)>
    * &lt;!ELEMENT expiredate (#PCDATA)>
    * &lt;!ELEMENT modifyuser (#PCDATA)>
    * &lt;!ELEMENT modifydate (#PCDATA)>
    * &lt;!ELEMENT location (#PCDATA)>
    * &lt;!ELEMENT delivery (location )>
    * &lt;!ELEMENT contenturl (#PCDATA)>
    * &lt;!ELEMENT title (#PCDATA)>
    * &lt;!ATTLIST  contentitem variantid CDATA #REQUIRED>
    * &lt;!ATTLIST  contentitem contentid CDATA #REQUIRED>
    * &lt;!ATTLIST  contentitem unpublish CDATA #REQUIRED>
    * &lt;!ATTLIST  contentitem revision CDATA #REQUIRED>
    * </code></pre>
    * Note, must not call {@link #isContentUrlNull()} from this instance, since
    * that data is undetermined. The "title" element is an optional element.
    * 
    * @param src the XML representcation of the instance, never 
    *    <code>null</code>. It must conform with described above, which is
    *    specified in {@link #fromXml(Element)}.
    * 
    * @throws PSUnknownNodeTypeException if the src does not conform with 
    *    dtd described above.
    */
   public PSContentListItem(Element src) throws PSUnknownNodeTypeException
   {
      fromXml(src);
   }
   
   /**
    * Set the instance according to the supplied XML representation. 
    * 
    * @param src the XML representation of the instance, 
    *    assumed not <code>null</code>, it must conform with the dtd described
    *    in {@link #PSContentListItem(Element)}.
    * 
    * @throws PSUnknownNodeTypeException if the src does not conform with the 
    *    dtd described in {@link #PSContentListItem(Element)}.
    */
   private void fromXml(Element src) throws PSUnknownNodeTypeException
   {
      if (src == null)
         throw new IllegalArgumentException("src must not be null");
      
      PSXMLDomUtil.checkNode(src, IPSDTDPublisherEdition.ELEM_CONTENTITEM);
      m_contentId = PSXMLDomUtil.checkAttribute(src,
            IPSDTDPublisherEdition.ATTR_CONTENTID, true);
      m_revision = PSXMLDomUtil.checkAttribute(src,
            IPSDTDPublisherEdition.ATTR_REVISION, true);
      m_variantId = PSXMLDomUtil.checkAttribute(src,
            IPSDTDPublisherEdition.ATTR_REVISION, false);
      m_unpublishValue = PSXMLDomUtil.checkAttribute(src,
            IPSDTDPublisherEdition.ATTR_UNPUBLISH, false);
      
      // the title is not stored in RXSITEITEMS, so this is option
      Element childElem = PSXMLDomUtil.getFirstElementChild(src);
      if (PSXMLDomUtil.getUnqualifiedNodeName(childElem).equalsIgnoreCase(
            IPSDTDPublisherEdition.ELEM_CONTENTTITLE))
      {
         m_contentTitle = PSXMLDomUtil.getElementData(childElem);
         childElem = PSXMLDomUtil.getNextElementSibling(childElem,
               IPSDTDPublisherEdition.ELEM_CONTENTURL);
      }
      else
      {
         m_contentTitle = "";
      }
      
      m_contenturl = PSXMLDomUtil.getElementData(childElem);

      Element deliveryElem = PSXMLDomUtil.getNextElementSibling(childElem,
            IPSDTDPublisherEdition.ELEM_DELIVERY);
      Element locationElem = PSXMLDomUtil.getFirstElementChild(deliveryElem,
            IPSDTDPublisherEdition.ELEM_LOCATION);
      m_deliveryLocation = PSXMLDomUtil.getElementData(locationElem);
      
      Element modifyDateElem = PSXMLDomUtil.getNextElementSibling(deliveryElem,
            IPSDTDPublisherEdition.ELEM_MODIFYDATE);
      String modifyDate = PSXMLDomUtil.getElementData(modifyDateElem);
      SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);
      if (modifyDate.trim().length() > 0)
      {
         try
         {
            m_lastModifiedDate = dateFormat.parse(modifyDate);
         }
         catch (ParseException e)
         {
            // ignore the bad date
            m_lastModifiedDate = null;
         }
      }
      Element userElem = PSXMLDomUtil.getNextElementSibling(modifyDateElem,
            IPSDTDPublisherEdition.ELEM_MODIFYUSER);
      m_lastModifier = PSXMLDomUtil.getElementData(userElem);
      
      Element expireDateElem = PSXMLDomUtil.getNextElementSibling(userElem,
            IPSDTDPublisherEdition.ELEM_EXPIREDATE);
      String expireDate = PSXMLDomUtil.getElementData(expireDateElem);
      if (expireDate.trim().length() > 0)
      {
         try
         {
            m_expiryDate = dateFormat.parse(expireDate);
         }
         catch (ParseException e)
         {
            // ignore the bad date
            m_expiryDate = null;
         }
      }
      
      Element contentTypeElem = PSXMLDomUtil.getNextElementSibling(
            expireDateElem, IPSDTDPublisherEdition.ELEM_CONTENTTYPE);
      m_contentTypeId = PSXMLDomUtil.getElementData(contentTypeElem);
   }
   
   /**
    * Generates a XML representation of the content item to be published,
    * conforming to the Rhythmyx contentlist DTD. A request context is required
    * because the assembler URL and delivery location path will be generated.
    *
    * @param doc the XML document that will generate the content item element,
    * must not be <code>null</code>.
    * @param request the current request context, used to generate assembler URL
    * and delivery location path, must not be <code>null</code>.
    * @return a XML representation of the content item to be published,
    * conforming to the Rhythmyx contentlist DTD.  Never <code>null</code>.
    */
   public Element toXml(Document doc, IPSRequestContext request)
   {
      if (doc == null)
         throw new IllegalArgumentException("XML document may not be null");
      if (request == null)
         throw new IllegalArgumentException("request context may not be null");

      log.debug("generating contentitem XML for id=" + m_contentId);
      Element itemXml =
         doc.createElement(IPSDTDPublisherEdition.ELEM_CONTENTITEM);
      itemXml.setAttribute(IPSDTDPublisherEdition.ATTR_CONTENTID, m_contentId);
      itemXml.setAttribute(IPSDTDPublisherEdition.ATTR_REVISION, m_revision);
      if (m_variantId != null)
         itemXml.setAttribute(
            IPSDTDPublisherEdition.ATTR_VARIANTID,
            m_variantId);
      else
         itemXml.setAttribute(IPSDTDPublisherEdition.ATTR_VARIANTID, "0");
      itemXml.setAttribute(
         IPSDTDPublisherEdition.ATTR_UNPUBLISH,
         m_unpublishValue);

      Element titleElem = PSXmlDocumentBuilder.addElement(
         doc,
         itemXml,
         IPSDTDPublisherEdition.ELEM_CONTENTTITLE,
         m_contentTitle);

      // add the "contenturl" element
      if (m_contenturl != null) // got it from #fromXml(), just use it.
      {
         PSXmlDocumentBuilder.addElement(
               doc,
               titleElem,
               IPSDTDPublisherEdition.ELEM_CONTENTURL,
               m_contenturl);         
      }
      // generate content URL to the assembler resource
      else
      {
         Element contentUrlXml =
            doc.createElement(IPSDTDPublisherEdition.ELEM_CONTENTURL);
         itemXml.appendChild(contentUrlXml);
         if (m_variantId != null && m_variantId.trim().length() > 0)
         {
            // if the variant is known, build an internal link to its assembler
            //First try correcting the last public revision 
            String revError = null;
            String correctedRevision = m_revision;
            try
            {
               //Correct revision only if the valid flag is "i"
               if (m_getLastPubRev4QEState)
               {
                  char[] validFlags = new char[]{CONTENTVALID_FLAG_I};
                  correctedRevision = PSMacroUtils.correctRevisionForFlags(request,
                        m_contentId, validFlags, m_revision);
               }
               if (correctedRevision.equals("-1"))
               {
                  revError = "This item was never public "
                        + "but in workflow state with contentvalid="
                        + CONTENTVALID_FLAG_I;
               }
               else
               {
                  itemXml.setAttribute(IPSDTDPublisherEdition.ATTR_REVISION,
                        correctedRevision);
               }
            }
            catch (PSException e)
            {
               //failed to correct the revision
               revError = e.getMessage();
            }
            if (revError != null)
            {
               makeNullContentUrl(doc, request, contentUrlXml, revError);
            }
            else
            {
            URL assemblerURL = m_linkGenerator.generateAssemblerLink(
                  m_contentId, correctedRevision, m_variantId, m_variantBase,
                  m_folderId, request, m_protocol, m_host, m_port, m_paramSetToPass);
               contentUrlXml.appendChild(doc.createTextNode(assemblerURL
                     .toString()));
            }
         }
         else
         {
            // if variant is unknown, include a comment in the content list to aid
            // debugging
            String comment = "content URL suppressed because no publishable "
                  + "variants for this item";
            makeNullContentUrl(doc, request, contentUrlXml, comment);
         }
      }

      Element deliveryXml =
         doc.createElement(IPSDTDPublisherEdition.ELEM_DELIVERY);
      itemXml.appendChild(deliveryXml);
      
      // add the "location" element
      if (m_deliveryLocation != null) // got the value from fromXml() method.
      {
         PSXmlDocumentBuilder.addElement(
               doc,
               deliveryXml,
               IPSDTDPublisherEdition.ELEM_LOCATION,
               m_deliveryLocation);         
      }
      else                    // create the "location" element on the fly 
      {
         Element locationXml =
            doc.createElement(IPSDTDPublisherEdition.ELEM_LOCATION);
         deliveryXml.appendChild(locationXml);
   
         /* Use the registered location scheme generator to populate the delivery
            location, unless there is a problem, in which case populate the delivery
            location with a comment explaining the problem */
         Node locationXmlValue;
         if (m_variantId == null)
         {
            log.debug("No publishable variants for item id=" + m_contentId);
            locationXmlValue =
               doc.createComment(
                  "location suppressed because no publishable variants for this item");
         }
         else
         {
            String filepath = getDeliveryLocation(request);
            if (filepath.length() > 0)
            {
               /* the delivery location is a combination of the folder path build
                  so far, plus filename generated by the scheme generator */
               locationXmlValue = doc.createTextNode(filepath);
            }
            else
            {
               log.warn(
                  "location scheme generator errored or returned empty string");
               locationXmlValue =
                  doc.createComment(
                     "location scheme generator errored or returned an empty string");
            }
         }
         locationXml.appendChild(locationXmlValue);
      }

      /* include last modified date in the datetime format needed by incremental
         publishing filter */
      SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_PATTERN);
      if (m_lastModifiedDate != null)
         PSXmlDocumentBuilder.addElement(
            doc,
            itemXml,
            IPSDTDPublisherEdition.ELEM_MODIFYDATE,
            format.format(m_lastModifiedDate));

      PSXmlDocumentBuilder.addElement(
            doc,
            itemXml,
            IPSDTDPublisherEdition.ELEM_MODIFYUSER,
            m_lastModifier);

      if (m_expiryDate != null)
         PSXmlDocumentBuilder.addElement(
            doc,
            itemXml,
            IPSDTDPublisherEdition.ELEM_EXPIREDATE,
            format.format(m_expiryDate));

      PSXmlDocumentBuilder.addElement(
         doc,
         itemXml,
         IPSDTDPublisherEdition.ELEM_CONTENTTYPE,
         m_contentTypeId);

      return itemXml;
   }

   /**
    * Helper method to Make a null content url and append as an XML comment node
    * to the element node supplied.
    * 
    * @param doc the parentXML document, assumed not <code>null</code>.  
    * @param request request context, assumed not <code>null</code>
    * @param contentUrlXml DOM element to which the comment is added
    * @param comment XML comment to be added, assumed not <code>null</code>.
    */
   private void makeNullContentUrl(Document doc, IPSRequestContext request,
         Element contentUrlXml, String comment)
   {
      URL nullURL = m_linkGenerator.generateNullLink(request);
      contentUrlXml.appendChild(doc.createTextNode(nullURL.toString()));
      contentUrlXml.appendChild(doc.createComment(comment));
      m_nullUrl = true;
   }

   /**
    * @return contentid of the item, never <code>null</code> or empty.
    */
   public String getContentId()
   {
      return m_contentId;
   }

   /**
    * Set the revision to the supplied new revision, which should be the last
    * public revision of the item.
    * 
    * @param revision
    *    the last public revision, never <code>null</code> or empty.
    */
   public void setLastPublicRevision(String revision)
   {
      if (revision == null || revision.trim().length() == 0)
         throw new IllegalArgumentException("revision may not be null or empty");
      
      m_revision = revision;
      m_getLastPubRev4QEState = false;
   }
   
   /**
    * @return variantid of the item, never <code>null</code> or empty.
    */
   public String getVariantId()
   {
      return m_variantId;
   }

   /**
    * @return Last modified date of the item, may be <code>null</code>.
    */
   public Date getLastModifiedDate()
   {
      return m_lastModifiedDate;
   }

   /**
    * @return Unpublish attribute of the item, may be <code>null</code> or
    *         empty.
    */
   public String getUnpublishValue()
   {
      return m_unpublishValue;
   }

   /**
    * @return If the Url is null then return <code>true</code> 
    *         otherwise <code>false</code>.
    */
   public boolean isContentUrlNull()
   {
      if (m_contenturl != null)
         throw new IllegalStateException("This instance is created from XML, the m_nullUrl data is unknown.");
         
      return m_nullUrl;
   }

   /**
    * Get last modified date as string.
    * @return last modifed date as string, may be <code>null</code>. If not
    *         <code>null</code>, it will be formatted as per the pattern
    *         {@link #DATE_FORMAT_PATTERN}.
    */
   public String getLastModifiedDateAsString()
   {
      if (m_lastModifiedDate != null)
      {
         SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_PATTERN);
         return format.format(m_lastModifiedDate);
      }
      else
         return null;
   }

   /**
    * Get delivery location using the link generator.
    * 
    * @param request request context object, must not be <code>null</code>.
    * @return Deliverty location as generated by the link generator supplied to
    *         the constructor.
    * @see PSSiteFolderContentListLinkGenerator#generatePubLocation(String,
    *      String, String, String, String, IPSRequestContext)
    */
   public String getDeliveryLocation(IPSRequestContext request)
   {
      if (request == null)
      {
         throw new IllegalArgumentException("request must not be null");
      }
      if (m_deliveryLocation == null)
      {
         // generate and store the delivery location
         m_deliveryLocation =
            m_linkGenerator.generatePubLocation(
               m_contentId,
               m_revision,
               m_variantId,
               m_filenameContext,
               m_folderPath,
               m_folderId,
               request);
      }
      return m_deliveryLocation;
   }

   // Implements compareTo(Object) interface
   public int compareTo(Object other)
   {
      if (! (other instanceof PSContentListItem))
         return -1;

      PSContentListItem otherObj = (PSContentListItem) other;
      
      return getCompareKey().compareTo(otherObj.getCompareKey());
   }
   
   /**
    * Returns the compare key, see {@link #m_compareKey} for detail.
    *
    * @return the compare key, never <code>null</code>.
    */
   private String getCompareKey()
   {
      if (m_compareKey == null)
         m_compareKey = m_contentId + "," + m_variantId;
      return m_compareKey;
   }
   
   /**
    * The key used to compare 2 different <code>PSContentListItem</code> 
    * objects. This is used by <code>PSContentList</code> to sort
    * the items by contentId and its variantId
    */
   private String m_compareKey = null;
   
   /**
    * Protocol for the HTTP request to be constructed for the content URL. 
    * Never <code>null</code> or empty after construction. Never modified
    * after construction.
    */
   private String m_protocol;

   /**
    * Host ip address or name for the HTTP request to be constructed for the
    * content URL. Never <code>null</code> or empty after construction. Never
    * modified after construction.
    */
   private String m_host;

   /**
    * Port number for the HTTP request to be constructed for the content URL.
    * Never modified after construction.
    */
   private int m_port;
   
   /**
    * Constant for the pattern used to format the last modified and expiration
    * dates.
    */
   private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";

   //Attributes of each item that is part of the content list for publishing. 
   private String m_contentId;
   private String m_revision;
   private String m_variantId;
   private String m_variantBase;
   private String m_contentTitle;
   private String m_folderId;
   private Date m_lastModifiedDate;
   private Date m_expiryDate;
   private String m_lastModifier;
   private String m_contentTypeId;
   private String m_filenameContext;
   private String m_folderPath;
   private String m_unpublishValue;
   private String m_deliveryLocation = null;
   private PSSiteFolderContentListLinkGenerator m_linkGenerator;
   private boolean m_nullUrl;
   
   /**
    * The data of "contenturl" element. It may be <code>null</code> if did not
    * set by the ctor. It is only set by {@link #fromXml(Element)}.
    */
   private String m_contenturl = null;
   
   /**
    * String constant for content valid flag "i", indicates the item is in
    * Quick-Edit state.
    */
   public static final char CONTENTVALID_FLAG_I = 'i';

   /**
    * Set of optional parameter names that need to be appended to the contenturl 
    * for the item. Could be initialized in the ctor, may be <code>null</code> 
    * or empty. 
    */
   private Set m_paramSetToPass = null;


   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   private Logger log = Logger.getLogger(getClass());
   
   /**
    * This is used to determine whether it needs get the last public revision
    * if the current item is in quick edit state. Initialized by ctor and set
    * to <code>false</code> by {@link #setLastPublicRevision(String)}
    */
   private boolean m_getLastPubRev4QEState;
}
