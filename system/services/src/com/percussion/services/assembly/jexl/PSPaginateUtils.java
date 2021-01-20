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
package com.percussion.services.assembly.jexl;

import static com.percussion.utils.xml.PSSaxHelper.canBeSelfClosedElement;
import static com.percussion.utils.xml.PSSaxHelper.newSAXParser;

import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.filter.PSFilterException;
import com.percussion.util.IPSHtmlParameters;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Jexl methods for paginating content items.
 * 
 * @author dougrand
 */
public class PSPaginateUtils extends PSJexlUtilBase
{
   /**
    * @param property
    * @return count of pages present
    * @throws RepositoryException
    * @throws ValueFormatException
    * @throws SAXException
    */
   @IPSJexlMethod(description = "Count the number of pages present in the property. If blank, 0 is returned.", params = { @IPSJexlParam(name = "content", description = "the input string") })
   public Number fieldContentPageCount(Property property)
      throws ValueFormatException, RepositoryException, SAXException
   {
      if (property == null)
      {
         throw new IllegalArgumentException("property may not be null");
      }
      try
      {
         String content = property.getString();
         if (StringUtils.isBlank(content))
         {
            return 0;
         }
         HTMLPageCounter counter = new HTMLPageCounter();
         return counter.getCount(new InputSource(new StringReader(
               xmlWrap(content))));
      }
      catch (IOException e)
      {
         // this should never happen as the source is a string
         throw new RuntimeException("Unexpected IOException: " + e.toString());
      }
   }

   /**
    * Page extractor
    * 
    * @param property
    * @param page
    * @return the specific contents for the page
    * @throws Exception
    */
   @IPSJexlMethod(description = "Extract the specified page from the content held in the property. The content must be well-formed XML. The returned content is well-formed XML.", params = {
         @IPSJexlParam(name = "property", description = "the content to be paged"),
         @IPSJexlParam(name = "page", description = "The page number, where 1 is the first page. If the content has less pages than the requested page, an empty string is returned.") })
   public String getFieldPage(Property property, Number page) throws Exception
   {
      if (property == null)
      {
         throw new IllegalArgumentException("property may not be null");
      }
      if (page == null)
      {
         throw new IllegalArgumentException("page may not be null");
      }
      if (page.intValue() < 1)
      {
         throw new IllegalArgumentException("page must be a positive number");
      }

      String content = property.getString();
      HTMLPaginator pager = new HTMLPaginator();
      String result = pager.getHTMLPage(new InputSource(new StringReader(
            xmlWrap(content))), page.intValue());
      return xmlUnwrap(result);
   }

   private final String WRAP_TAG_NAME = "rx_wrap_tag";
   
   /**
    * The opening tag used by {@link #xmlWrap(String)}.
    */
   private final String START_TAG = "<" + WRAP_TAG_NAME + ">";

   /**
    * The closing tag used by {@link #xmlWrap(String)}. A proper closing tag
    * for {@link #START_TAG}.
    */
   private final String END_TAG = "</" + WRAP_TAG_NAME + ">";

   /**
    * Adds the supplied content as a child of another element and returns the
    * result. The {@link #START_TAG} and {@link #END_TAG}s are used as wrapping
    * tags.
    * 
    * @param content Anything allowed.
    * @return The supplied content wrapped in a pair of tags. <code>null</code>
    *         behaves like the empty string.
    */
   private String xmlWrap(String content)
   {
      if (content == null)
         content = StringUtils.EMPTY;
      return START_TAG + content + END_TAG;
   }

   /**
    * Undoes what {@link #xmlWrap(String)} did.
    * 
    * @param wrapped Anything allowed.
    * @return If <code>null</code>, empty or doesn't start with a wrap tag,
    *         then returns the supplied string w/o change. Otherwise, the tags
    *         added by the wrapper are removed and the resulting string
    *         returned.
    */
   private String xmlUnwrap(String wrapped)
   {
      if (StringUtils.isBlank(wrapped) || !wrapped.startsWith(START_TAG))
         return wrapped;

      return wrapped.substring(START_TAG.length(), wrapped.length()
            - END_TAG.length());
   }

   /**
    * @param item
    * @param slotName
    * @param itemsPerPage
    * @param pageNumber
    * @param params
    * @return
    * @throws Exception
    */
   @IPSJexlMethod(description = "Extract the given page for the contents of the given slot. The returned snipped are disabled for Active Assembly, e.g. $sys.activeAssembly = false.", params = {
         @IPSJexlParam(name = "item", description = "the assembly item, not the node"),
         @IPSJexlParam(name = "slotName", description = "the name of the slot to expand"),
         @IPSJexlParam(name = "itemsPerPage", description = "the count of items per rendered page"),
         @IPSJexlParam(name = "pageNumber", description = "the page to extract where 1 is the first page"),
         @IPSJexlParam(name = "params", description = "optional parameters to pass to the slot") }, returns = "a list of assembly results for the slot")
   public List<IPSAssemblyResult> getSlotPage(IPSAssemblyItem item,
         String slotName, int itemsPerPage, Number pageNumber,
         Map<String, Object> params) throws Exception
   {
      int pageZeroBased = pageNumber.intValue() - 1;
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      int start = itemsPerPage * pageZeroBased;
      List<IPSAssemblyItem> items = getSlotContents(item, slotName, params);
      int end = Math.min(start + itemsPerPage, items.size());
      List<IPSAssemblyItem> pagedItems = items.subList(start, end);
      
      // Disable Active Assembly for the paged slot items. This has to be done
      // before assemble the items, so that the binding "$sys.activeAssembly"
      // will be set to "false" in the assembled results.
      for (IPSAssemblyItem pitem : pagedItems)
      {
         pitem.setParameterValue(IPSHtmlParameters.SYS_FORAASLOT, Boolean
               .toString(false));
      }
      
      return asm.assemble(pagedItems);
   }

   /**
    * Get the contents of the slot, to be used either for counting or for
    * returning as "pages"
    * 
    * @param item the assembly item, never <code>null</code>.
    * @param slotName the slot name, never <code>null</code> or empty
    * @param params the parameters, may be <code>null</code>
    * @return the list of assembly items in the slot, never <code>null</code>
    *         but could be empty.
    * @throws PSAssemblyException
    * @throws PSFilterException
    * @throws RepositoryException
    */
   private List<IPSAssemblyItem> getSlotContents(IPSAssemblyItem item,
         String slotName, Map<String, Object> params)
      throws PSAssemblyException, PSFilterException, RepositoryException
   {
      if (item == null)
      {
         throw new IllegalArgumentException("item may not be null");
      }
      if (StringUtils.isBlank(slotName))
      {
         throw new IllegalArgumentException(
               "slotName may not be null or empty");
      }
      PSAssemblerUtils asmutils = new PSAssemblerUtils();
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      IPSTemplateSlot slot = safeSlotGet(asm, slotName);
      return asmutils.getSlotItems(item, slot, params);
   }

   /**
    * Get the slot, throwing appropriate exceptions for bad data, missing slots,
    * etc.
    * 
    * @param asm the assembly service, assumed never <code>null</code>.
    * @param slotName the slot name, never <code>null</code> or empty
    * @return the slot, never <code>null</code>.
    * @throws PSAssemblyException
    */
   private IPSTemplateSlot safeSlotGet(IPSAssemblyService asm, String slotName)
      throws PSAssemblyException
   {
      if (StringUtils.isBlank(slotName))
      {
         throw new IllegalArgumentException(
               "slotName may not be null or empty");
      }
      return asm.findSlotByName(slotName);
   }

   /**
    * 
    * @param item
    * @param slotName
    * @param itemsPerPage
    * @return
    * @throws RepositoryException
    * @throws PSFilterException
    * @throws PSAssemblyException
    */
   @IPSJexlMethod(description = "Calculate the page count for the contents of the given slot", params = {
         @IPSJexlParam(name = "item", description = "the assembly item, not the node"),
         @IPSJexlParam(name = "slotName", description = "the name of the slot to expand"),
         @IPSJexlParam(name = "itemsPerPage", description = "the count of items per rendered page. It must be greater than 0."),
         @IPSJexlParam(name = "params", description = "optional parameters to pass to the slot") }, returns = "how many pages worth of items are in the slot")
   public Number slotContentPageCount(IPSAssemblyItem item, String slotName,
         int itemsPerPage, Map<String, Object> params)
      throws PSAssemblyException, PSFilterException, RepositoryException
   {
      if (itemsPerPage <= 0)
         throw new IllegalArgumentException("itemsPerPage must be > 0.");
      
      List<IPSAssemblyItem> items = getSlotContents(item, slotName, params);
      if (items.size() == 0)
      {
         return 1;
      }
      else
      {
         int count = items.size() / itemsPerPage;
         return (items.size() % itemsPerPage) == 0 ? count : count + 1;
      }
   }

   /**
    * The name of the processing instruction used as the page break marker.
    */
   private static final String PAGE_BREAK_TEXT = "pageBreak";

   /**
    * All tag types that are stored on the tag stack
    */
   enum TagType
   {
      BEGIN_TAG,
      END_TAG,
      UNKNOWN_TAG
   };

   /**
    * The caller provides an XML document that contains 0 or more page break
    * markers and asks for 1 of the pages back. The returned document is a
    * properly formed XML document with content from the original document that
    * resided between the (n-1)th and nth page break markers, where n is the
    * requested page #. Tags that open before the requested page are
    * automatically opened and tags that closed after that page are
    * automatically closed.
    * <p>
    * The content is parsed and the elements are pushed on a stack with
    * notations about what page they were opened and closed in. When parsing is
    * complete, the stack is popped and the desired page is reconstructed, with
    * all tags properly opened and closed.
    * <p>
    * All entities are converted. Comments are stripped.
    * <p>
    * This code was derived from a PSO implementation of pagination.
    * 
    * @author paulhoward
    */
   private class HTMLPaginator extends DefaultHandler2
   {
      
      /**
       * Simple class to group some data that is tracked for each element found
       * in an XML document.
       */
      private class HtmlTag
      {
         public HtmlTag(String name, String text, int openedInPage,
               int closedInPage, TagType tagType)
         {
            mi_tagName = name;
            mi_tagText = text;
            mi_openedInPage = openedInPage;
            mi_closedInPage = closedInPage;
            mi_tagType = tagType;
         }

         public String mi_tagName = "";

         public String mi_tagText = "";

         public int mi_openedInPage = 0; // Page number where the tag was
                                          // opened

         public int mi_closedInPage = 0; // Page number where the tag was
                                          // closed
         
         public TagType mi_tagType;
      }

      /**
       * Logger for this class
       */
      private final Log log = LogFactory.getLog(HTMLPaginator.class);

      /**
       * Counter of how many page breaks have been seen so far + 1.
       */
      private int mi_numPages = 1;

      /**
       * Contains the resulting page. Contents not valid until
       * {@link #endDocument()} has returned.
       */
      private String mi_resultHTML = "";

      /**
       * Stores data about each element found in the processed document. Never
       * <code>null</code>.
       */
      private Stack<HtmlTag> mi_htmlTags = new Stack<HtmlTag>();

      /**
       * The page that was requested. Set only by
       * {@link #getHTMLPage(InputSource, int)}.
       */
      private int mi_currentPage;

      /**
       * This flag indicates whether the parser is currently processing data
       * within a CDATA section or not.
       */
      private boolean mi_inCdata;

      /**
       * Parses the supplied content, searching for page break tags (processing
       * instructions whose name is {@link PSPaginateUtils#PAGE_BREAK_TEXT}.)
       * The resulting page has all tags properly matched for the page.
       * 
       * @param in The source of the xml compliant content. Assumed not
       *        <code>null</code>.
       * @param page Which page within the content to retrieve. Pages start at
       *        1. Assumed >= 1.
       * @return The content of the requested page, which is well-formed XML. If
       *         a page number greater than the number of pages in the source is
       *         requested, an empty string is returned. If a SAX parser can't
       *         be acquired, the empty string is returned and the error is
       *         logged.
       * @throws SAXException If the content is not well formed XML.
       * @throws IOException If any problems reading from the supplied source.
       */
      public String getHTMLPage(InputSource in, int page)
         throws SAXException, IOException
      {
         setResultHTML("");
         mi_currentPage = page;
         mi_htmlTags.clear();
         mi_numPages = 1;

         try
         {
            SAXParser saxParser = newSAXParser(this);
            saxParser.parse(in, this);
            return getResultHTML();
         }
         catch (ParserConfigurationException p)
         {
            log.error("Paginator-Couldn't acquire SAX parser: "
                        + p.toString());
            return "";
         }
      }

      // ===========================================================
      // SAX DocumentHandler methods
      // ===========================================================
      
      
      @Override
      public void startDocument()
      {
         setResultHTML("");
      }

      @Override
      public void endDocument()
      {
         // build the page from the 'rear' to the 'front'
         while (!mi_htmlTags.empty())
         {
            HtmlTag h = mi_htmlTags.pop();

            /*
             * First condition is for text nodes. Second is for all the tags
             * opened before or during current page or closed during or after
             * current page
             */
            if ((h.mi_tagName.equals("") && h.mi_closedInPage == mi_currentPage)
                  || (h.mi_openedInPage <= mi_currentPage && h.mi_closedInPage >= mi_currentPage))
            {
               if (canBeSelfClosedTag(h, mi_htmlTags))
               {
                  HtmlTag beginTag = mi_htmlTags.pop();
                  setResultHTML(getSelfClosedTagText(beginTag) + getResultHTML());
               }
               else
               {
                  setResultHTML(h.mi_tagText + getResultHTML());
               }
            }
         }
      }

      /**
       * Gets the self closed (empty) tag of the specified tag.
       * 
       * @param h the tag, it must be a begin tag, assumed not <code>null</code>.
       * 
       * @return the text of the self closed empty tag, never <code>null</code>.
       */
      private String getSelfClosedTagText(HtmlTag h)
      {
         int closeTag = h.mi_tagText.length() - 1;
         if (h.mi_tagType == TagType.BEGIN_TAG && (!(h.mi_tagText.charAt(closeTag) == '>')))
            throw new IllegalStateException("Expecting a begin tag ends with '>' character.");

         // some tag, such as "br" must be self close with " />"
         return h.mi_tagText.substring(0, closeTag) + " />";
      }

      /**
       * Determines if the specified tag and the next tag on the stack can be replaced 
       * with one self closed tag.
       * 
       * @param h the tag, assumed not <code>null</code>.
       * @param tagStack the stack that contains the remaining elements, assumed not <code>.
       * 
       * @return <code>true</code> if the tag is an end of empty tag.
       */
      private boolean canBeSelfClosedTag(HtmlTag h, Stack<HtmlTag> tagStack)
      {
         if (h.mi_tagType != TagType.END_TAG)
            return false;

         // WRAP_TAG_NAME cannot be self closed tag
         if (tagStack.empty() || WRAP_TAG_NAME.equals(h.mi_tagName)
               || (!canBeSelfClosedElement(h.mi_tagName)))
            return false;
         
         HtmlTag preTag = tagStack.peek();
         return preTag.mi_tagType == TagType.BEGIN_TAG 
            && preTag.mi_openedInPage == h.mi_closedInPage
            && preTag.mi_tagName.equalsIgnoreCase(h.mi_tagName);
      }
      
      @Override
      public void processingInstruction(String target,
            @SuppressWarnings("unused")
            String data)
      {
         if (target.equals(PAGE_BREAK_TEXT))
            mi_numPages++;
         else
         {
            addNewHTMLTag("", "<?" + target + " " + data + "?>", mi_numPages,
                  mi_numPages);
         }
      }

      @Override
      public void startElement(@SuppressWarnings("unused")
      String namespaceURI, String lName, String qName, Attributes attrs)
      {
         // Look for tag and attributes
         String eName = lName;
         eName = qName;
         String resTag = "<" + eName;
         if (attrs != null)
         {
            for (int i = 0; i < attrs.getLength(); i++)
            {
               String aName = attrs.getLocalName(i); // Attr name
               if ("".equals(aName))
                  aName = attrs.getQName(i);
               // Set text with attributes
               resTag += " " + aName + "=\""
                     + xmlEscapeAttribute(attrs.getValue(i)) + "\"";
            }
         }
         resTag += ">";

         // Save in stack
         addNewHTMLTag(eName, resTag, mi_numPages, 0, TagType.BEGIN_TAG);
      }

      /**
       * Calls {@link #xmlEscapeText(String)} and then converts the following
       * additional char: <code>quot</code>.
       * 
       * @param value Anything ok.
       * 
       * @return The original string, with reserved chars replaced by their
       *         entity.
       */
      private String xmlEscapeAttribute(String value)
      {
         String result = xmlEscapeText(value);
         result = result.replace("\"", "&quot;");
         return result;
      }

      /**
       * Just like {@link #xmlEscapeAttribute(String)}, except only
       * <code>amp</code> and <code>lt</code> are processed.
       * 
       * @param value Anything ok.
       * @return The orginal string, with reserved chars replaced by their
       *         entity.
       */
      private String xmlEscapeText(String value)
      {
         if (StringUtils.isBlank(value))
            return value;
         String result = value;
         result = result.replace("&", "&amp;");
         result = result.replace("<", "&lt;");
         return result;
      }

      @Override
      public void endElement(@SuppressWarnings("unused")
      String namespaceURI, @SuppressWarnings("unused")
      String sName, String qName)
      {
         // Page default for closing tag
         int openedInPage = mi_numPages;

         // Look for the most recent not-yet-closed tag inserted.
         ListIterator<HtmlTag> it = mi_htmlTags.listIterator(mi_htmlTags
               .size());

         while (it.hasPrevious())
         {
            HtmlTag h = it.previous();

            // Check if is already closed
            if (h.mi_closedInPage == 0) // Not yet closed
            {
               h.mi_closedInPage = mi_numPages;
               // Save the value for the closing tag
               openedInPage = h.mi_openedInPage;
               break;
            }
         }

         // Closing tag
         addNewHTMLTag(qName, "</" + qName + ">", openedInPage, mi_numPages, TagType.END_TAG);
      }

      @Override
      public void characters(char buf[], int offset, int len)
      {
         // Text nodes
         String s = new String(buf, offset, len);
         addNewHTMLTag("", mi_inCdata ? s : xmlEscapeText(s), mi_numPages,
               mi_numPages);
      }

      /**
       * Creates a container to hold the supplied data and pushes it on the
       * local stack ({@link #mi_htmlTags}).
       * 
       * @param tagName The name of the element. Supply empty for text nodes.
       * @param tagText The value of the node.
       * @param openedInPage What page within the document did this tag start.
       * @param closedInPage What page within the document did this tag end.
       */
      private void addNewHTMLTag(String tagName, String tagText,
            int openedInPage, int closedInPage)
      {
         addNewHTMLTag(tagName, tagText, openedInPage, closedInPage, TagType.UNKNOWN_TAG);
      }

      /**
       * Same as {@link #addNewHTMLTag(String, String, int, int)}, but this method
       * has tag type parameter.
       * @param tagType the type of the tag, assumed not <code>null</code>.
       */
      private void addNewHTMLTag(String tagName, String tagText,
            int openedInPage, int closedInPage, TagType tagType)
      {
         // Add a new tag to the stack
         HtmlTag h1 = new HtmlTag(tagName, tagText, openedInPage, closedInPage, tagType);
         mi_htmlTags.push(h1);         
      }

      /**
       * @return Returns the resultHTML, never <code>null</code>.
       */
      private String getResultHTML()
      {
         return mi_resultHTML;
      }

      /**
       * Sets the HTML result.
       * 
       * @param res The resultHTML to set.
       */
      private void setResultHTML(String res)
      {
         if (res == null)
            res = StringUtils.EMPTY;
         mi_resultHTML = res;
      }

      @Override
      public void comment(char[] ch, int start, int length)
      {
         String s = "<!--" + new String(ch, start, length) + "-->";
         addNewHTMLTag("", s, mi_numPages, mi_numPages);
      }

      @Override
      public void endCDATA()
      {
         mi_inCdata = false;
         addNewHTMLTag("", "]]>", mi_numPages, mi_numPages);
      }

      @Override
      public void endEntity(String name) throws SAXException
      {
         super.endEntity(name);
      }

      @Override
      public void startCDATA()
      {
         mi_inCdata = true;
         addNewHTMLTag("", "<![CDATA[", mi_numPages, mi_numPages);
      }

      @Override
      public void startEntity(String name) throws SAXException
      {
         super.startEntity(name);
      }
   }

   /**
    * Similar to {@link HTMLPaginator}, but only handles the page break. Counts
    * the number of page breaks and returns the number of pages.
    * <p>
    * This code was derived from a PSO implementation of pagination.
    * 
    * @author paulhoward
    */
   private class HTMLPageCounter extends DefaultHandler
   {
      /**
       * Logger for this class
       */
      private final Log log = LogFactory.getLog(HTMLPageCounter.class);

      /**
       * Stores the number of page breaks currently encountered +1.
       */
      private int numPages = 1;

      /**
       * Counts the number of page breaks in the supplied content.
       * 
       * @param in
       * @return The number of page breaks found + 1. If a sax parser cannot be
       *         obtained, 0 is returned and a message is logged.
       * 
       * @throws SAXException If the content is not well-formed XML.
       * @throws IOException If the supplied stream can't be read.
       */
      public int getCount(InputSource in) throws SAXException, IOException
      {
         // Reset
         numPages = 1;

         try
         {
            SAXParser saxParser = newSAXParser(null);
            saxParser.parse(in, this);
            return numPages;
         }
         catch (ParserConfigurationException p)
         {
            log.error("PageCounter-Couldn't acquire SAX parser: "
                  + p.toString());
            return 0;
         }
      }

      // ===========================================================
      // SAX DocumentHandler methods
      // ===========================================================

      @Override
      public void processingInstruction(String target,
            @SuppressWarnings("unused")
            String data)
      {
         if (target.equals(PAGE_BREAK_TEXT))
            numPages++;
      }
   }
}
