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
package com.percussion.design.objectstore;

import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * A PSRevisionHistory object defines a list containing information about
 * the initial and latest revision associated with a component or document.
 */
public class PSRevisionHistory extends PSComponent
{
   /**
    * Constructs a new revision history object.
    *
    * @author      chad loder
    * 
    * @version 1.0 1999/7/8
    * 
    */
   public PSRevisionHistory()
   {
      m_revs = new ArrayList();
   }

   /**
    * Sets the lastest revision information the revision history, incrementing
    * the current Minor Version number by 1.<BR>
    * The date should be later than any dates already in the history. <BR>
    * If the date is <CODE>null</CODE>, it will default to the current system
    * time. 
    * 
    * @param    agent        The agent responsible for the change. Can be
    *                        <CODE>null</CODE>.
    *
    * @param    description  A description of the change. Can be
    *                        <CODE>null</CODE>.
    *
    * @param    time         The time of the change. If <CODE>null</CODE>, uses
    *                        the current system time.
    * 
    */
   public void setRevision(String agent, String description, Date time)
   {
      setRevision(new PSRevisionEntry(agent,
                                      description,
                                      time,
                                      getLatestMajorVersion(),
                                      getLatestMinorVersion() + 1));
   }

   /**
    * Sets the lastest revision information the revision history. <BR>
    * Will be a no-op if the supplied version is not greater than or equal
    * to the current latest version.
    *
    * @param    rev    PSRevisionEntry entry for the latest revision
    *                  The lastest revision information will only be reset if
    *                  rev's version is greater than or equal to  the current 
    *                  latest version. <BR> 
    *                  This is true if either:
    *                  <ul>
    *                  <li> rev's Major Version > latest Major Version </li>
    *                  <li> or rev's Major Version = latest Major Version and
    *                       Minor Version >= latest Minor Version
    * 
    */
   public void setRevision(PSRevisionEntry rev)
   {
      // The first revision added is always the latest
      if (m_revs.size() == 0)
      {
         m_revs.add(rev);
         m_latestEntry = rev;
      }
      else
      {
         /*
          * Check if rev is really the latest. First compare Major versions,
          * then compare minor ones if there is a Major version tie.
          */

         if ((rev.getMajorVersion() > getLatestMajorVersion()) ||
             ((rev.getMajorVersion() == getLatestMajorVersion()) &&
              (rev.getMinorVersion() >= getLatestMinorVersion())))
         {
            // This revision is now latest
            m_latestEntry = rev;

            /*
             * If there is only one entry, add this one, otherwise replace
             * the second entry. We keep the original one, which is used by
             * the application summary.
             */
            if (m_revs.size() >= 2)
            {
               m_revs.set(1, rev);
            }
            else
            {            
               m_revs.add(rev);
            }

            // Do nothing if rev was not the latest
         }
      }
   }
   
   /**
    * Gets the latest major version in this history.
    *
    * @author      chadloder
    * 
    * @version 1.3 1999/07/12
    * 
    * @return      int
    */
   public int getLatestMajorVersion()
   {
      if (m_latestEntry == null)
      {
         return 1;
      }
      else
      {
         return m_latestEntry.getMajorVersion();
      }
   }

   /**
    * Gets the latest minor version in this history.
    *
    * @author      chadloder
    * 
    * @version 1.3 1999/07/12
    * 
    * @return      int
    */
   public int getLatestMinorVersion()
   {
      if (m_latestEntry == null)
      {
         return -1;
      }
      else
      {
         return m_latestEntry.getMinorVersion();
      }
   }

   /**
    * Gets the latest version as a string with the format <CODE>M.m</CODE>,
    * where <CODE>M</CODE> is the major version and <CODE>m</CODE> is
    * the minor.
    *
    * @author      chadloder
    * 
    * @version 1.3 1999/07/12
    * 
    * @return      String
    */
   public String getLatestVersion()
   {
      return getLatestMajorVersion() + "." + getLatestMinorVersion();
   }

   /**
    * Gets the initial revision entry.
    *
    * @return      The first PSRevisionEntry in the history, or
    *              <CODE>null</CODE> if the history list is empty.
    */
   public PSRevisionEntry getInitialRevision()
   {
      if (m_revs.size() == 0)
      {
         return null;
      }
      else
      {
         return (PSRevisionEntry)m_revs.get(0);
      }
   }
      
   /**
    * Gets the latest revision entry.
    *
    * @return      PSRevisionEntry
    */
   public PSRevisionEntry getLatestRevision()
   {
      return m_latestEntry;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSRevisionHistory)) return false;
      if (!super.equals(o)) return false;
      PSRevisionHistory that = (PSRevisionHistory) o;
      return Objects.equals(m_latestEntry, that.m_latestEntry) &&
              Objects.equals(m_revs, that.m_revs);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_latestEntry, m_revs);
   }

   /**
    * Gets the revision entries in order from earliest to latest.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/7/8
    * 
    * @return   PSRevisionEntry[]
    */
   private PSRevisionEntry[] getEntries()
   {
      return (PSRevisionEntry[])(m_revs.toArray
                                 (new PSRevisionEntry[m_revs.size()]));
   }
   
   /* **************  IPSComponent Interface Implementation ************** */
        
   /**
    * This method is called to create a PSXRevisionHistory XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *      &lt;!--
    *              PSXRevisionHistory defines a list of changes associated with 
    *              a component or document.
    *      --&gt;
    *      &lt;!ELEMENT PSXRevisionHistory (PSXRevisionEntry*)&gt;
    *
    *      &gt;
    * </code></pre>
    *
    * @return        the newly created PSXRevisionHistory XML element node
    */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(ms_nodeType);
      PSRevisionEntry[] revs = getEntries();
      for (int i = 0; i < revs.length; i++)
      {
         root.appendChild(revs[i].toXml(doc));
      }
                
      return root;
   }
        
   /**
    * This method is called to populate a PSRevisionHistory Java object
    * from a PSXRevisionHistory XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @throws   PSUnknownNodeTypeException if the XML element node is not
    *              of type PSXRevisionHistory
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc, 
                       java.util.ArrayList parentComponents)
         throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_nodeType);
      }
                
      if (!ms_nodeType.equals (sourceNode.getNodeName()))
      {
         Object[] args = { ms_nodeType, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
                
      PSXmlTreeWalker walker = new PSXmlTreeWalker(sourceNode);

      String curNodeType = PSRevisionEntry.ms_nodeType;
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
      firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      nextFlags  |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      for (     Element curNode = walker.getNextElement(curNodeType,
                                                        firstFlags);
                curNode != null;
                curNode = walker.getNextElement(curNodeType, nextFlags))
      {
         PSRevisionEntry rev = new PSRevisionEntry();
         rev.fromXml(curNode, parentDoc, parentComponents);
         setRevision(rev);
      }
   }
        
   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSValidationException, but the
    * implementation must not directly throw any exceptions. Instead, it
    * should register any errors with the validation context, which will
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * rethrown).
    * 
    * @param       cxt The validation context.
    * 
    * @throws      PSValidationException According to the implementation of the
    * validation context (on warnings and/or errors).
    */
   public void validate(IPSValidationContext cxt) throws PSValidationException
   {
      if (!cxt.startValidation(this, null))
         return;

      cxt.pushParent(this);
      try
      {
         PSRevisionEntry[] revs = getEntries();
         for (int i = 0; i < revs.length; i++)
         {
            PSRevisionEntry entry = revs[i];
            entry.validate(cxt);
         }
      }
      finally
      {
         cxt.popParent();
      }
   }

   /** the latest version in the history */
   private PSRevisionEntry m_latestEntry = null;

   /** the revision entries. never null (but can have 0 entries) */
   private List m_revs;

   static final String ms_nodeType = "PSXRevisionHistory";
}
