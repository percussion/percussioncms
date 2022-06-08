/******************************************************************************
 *
 * [ PSSortXmlExtension.java ]
 *
 * COPYRIGHT (c) 1999 - 2011 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.extensions;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.DOMReader;
import org.dom4j.io.DOMWriter;
import org.w3c.dom.Document;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static java.util.Collections.sort;

/**
 * Sorts based on the Locale ({@link Collator}) on the XML Elements under the root node.
 * The text used for sorting is a child element of the elements being sorted (ie the
 * root nodes grand children and the node being sorted is the parent).
 * 
 * If there is no node for sorting text than an empty string will be used.
 * 
 * The first parameter is the name of the element to base sorting on.
 * 
 * @author adamgent
 *
 */
public class PSSortXmlExtension implements IPSResultDocumentProcessor
{

   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
   }

   public boolean canModifyStyleSheet()
   {
      return false;
   }

   @SuppressWarnings("unchecked")
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      final String path = ((IPSReplacementValue) params[0]).getValueText();
      final boolean descending = params.length < 2 ? 
            false :  Boolean.parseBoolean(((IPSReplacementValue) params[1]).getValueText());
      DOMReader reader = new DOMReader();
      org.dom4j.Document doc4j = reader.read(resultDoc);
      List<Element> elements = doc4j.getRootElement().elements();
      Locale loc = request.getPreferredLocale();
      loc = loc == null ? Locale.getDefault() : loc;
      final Locale locale = loc;
      Comparator<Element> c = new Comparator<Element>()
      {
         Collator collator = Collator.getInstance(locale);
         public int compare(Element o1, Element o2)
         {
            Element e1 = o1.element(path);
            Element e2 = o2.element(path);
            String s1 = e1 == null ? "" : e1.getTextTrim();
            String s2 = e2 == null ? "" : e2.getTextTrim();
            return collator.compare(s1, s2);
         }
      };
      if (descending)
         c = Collections.reverseOrder(c);
      
      List<Element> newElements = new ArrayList<Element>(elements);
      sort(newElements, c);
      for (Element e : elements) {
         e.detach();
      }
      doc4j.getRootElement().clearContent();
      doc4j.getRootElement().elements().addAll(newElements);
      
      DOMWriter writer = new DOMWriter();
      try
      {
         return writer.write(doc4j);
      }
      catch (DocumentException e)
      {
         throw new RuntimeException(e);
      }
   }
   

}
