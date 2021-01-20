/*[ EmpireHtmlDumper.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.autotest.empire.script;

import java.io.PrintWriter;
import java.io.OutputStream;

import com.quiotix.html.HtmlVisitor;
import com.quiotix.html.HtmlDocument;

/**
 * Simple HtmlDumper which dumps out the document to the specified
 * output stream and ignores all line breaks except in &lt;PRE&gt; tag block
 * if the flag is set to ignore. By default the flag is set to <code>true</code>
 * to ignore line breaks.
 */
public class EmpireHtmlDumper extends HtmlVisitor {

   /**
    * Flag to indicate whether line breaks should be ignored while writting them
    * to output stream or not. Initially set to <code>true</code> to ignore line
    * breaks and can be set to <code>false</code> in constructor.
    **/
   private boolean m_IgnoreLineBreaks  = true;

   /**
    * Holds the element which is written to output stream, to compare it before
    * writting a line break to the output. Initialized in {@link #start()}.
    **/
   private HtmlDocument.HtmlElement m_previousElement;

   /**
    * Flag to indicate whether the element writting is in &lt;PRE&gt; tag block.
    * Set to <code>true</code> when &lt;PRE&gt; tag is encountered and set to
    * <code>false</code> when &lt;/PRE&gt; tag is encountered. Initially set to
    * <code>false</code> in {@link #start()}.
    **/
   private boolean m_inPreBlock;

   /**
    * The output writer to which the HTML elements should be written.
    * Initialized in constructor. 
    **/
   private PrintWriter m_out;

   /**
    * Constructor for html dumper. Ignores all line breaks except in
    * &lt;PRE&gt; tag block.
    *
    * @param os the output stream to which the html document should be written.
    * It should be closed by caller.
    *
    * @throws IllegalArgumentException if <code>os</code> is <code>null</code>.
    **/
   public EmpireHtmlDumper(OutputStream os)
   {
      if(os == null)
         throw new IllegalArgumentException(
            "The stream to which the document should be written may not be null"
            );
      m_out = new PrintWriter(os);
   }

   /**
    * Constructor to set the output stream and the flag to ignore line breaks.
    *
    * @param os the output stream to which the html document should be written.
    * It should be closed by caller.
    * @param ignoreLineBreaks if <code>true</code> ignores line breaks except in
    * &lt;PRE&gt; tag block while writting to stream, otherwise writes the line
    * breaks to stream.
    *
    * @see #EmpireHtmlDumper(OutputStream)
    **/
   public EmpireHtmlDumper(OutputStream os, boolean ignoreLineBreaks)
   {
      this(os);
      m_IgnoreLineBreaks = ignoreLineBreaks;
   }

   /**
    * Initializes data. Invoked before visiting elements for writting.
    **/
   public void start()
   {
      m_previousElement = null;
      m_inPreBlock = false;
   }

   /**
    * Invoked when tag block element is encountered in document. Visits Tag
    * Block element for writting to output stream. Checks whether
    * this element is &lt;PRE&gt; block element and sets the
    * <code>m_inPreBlock</code> to <code>true</code>, so that the text or
    * elements inside this block preserves it's formatting.
    *
    * @param bl the block element in html to write, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>bl</code> is <code>null</code>
    * @see HtmlVisitor#visit(HtmlDocument.TagBlock)
    **/
   public void visit(HtmlDocument.TagBlock bl)
   {
      if(bl == null)
         throw new IllegalArgumentException(
            "The element to be written can not be null.");

      String tagName = bl.startTag.tagName.toUpperCase();
      boolean isPreChild = false;

      if(tagName.equals("PRE"))
         m_inPreBlock = true;
      else
      {
         if(m_inPreBlock)
            isPreChild = true;
      }

      visit(bl.startTag);
      super.visit(bl.body);
      visit(bl.endTag);

      if(m_inPreBlock && !isPreChild)
         m_inPreBlock = false;
   }

   /**
    * Flushes the output stream. Invoked when end of elements reached in HTML
    * document.
    **/
   public void finish()
   {
      m_out.flush();
   }

   /**
    * Prints/Writes Tag element with its attributes. Invoked when tag element is
    * encountered in document.
    *
    * @param t the tag element in html to write, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>t</code> is <code>null</code>
    * @see HtmlVisitor#visit(HtmlDocument.Tag)
    **/
   public void visit(HtmlDocument.Tag t)
   {
      visitElement(t);
   }

   /**
    * Prints/Writes End Tag element. Invoked when end tag element is encountered
    * in document.
    *
    * @param t the end tag element in html to write,may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>t</code> is <code>null</code>
    * @see HtmlVisitor#visit(HtmlDocument.EndTag)
    **/
   public void visit(HtmlDocument.EndTag t)
   {
      visitElement(t);
   }

   /**
    * Prints/Writes Comment element. Invoked when Comment element is encountered
    * in document.
    *
    * @param c the comment element in html to write,may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>c</code> is <code>null</code>
    * @see HtmlVisitor#visit(HtmlDocument.Comment)
    **/
   public void visit(HtmlDocument.Comment c)
   {
      visitElement(c);
   }

   /**
    * Prints/Writes Text element. Invoked when Text element is encountered
    * in document.
    *
    * @param t the text element in html to write, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>t</code> is <code>null</code>
    * @see HtmlVisitor#visit(HtmlDocument.Text)
    **/
   public void visit(HtmlDocument.Text t)
   {
      visitElement(t);
   }

   /**
    * Prints/Writes Annotation element. Invoked when Annotation element is
    * encountered in document.
    *
    * @param a the annotation tag element in html to write, may not be
    * <code>null</code>
    *
    * @throws IllegalArgumentException if <code>a</code> is <code>null</code>
    * @see HtmlVisitor#visit(HtmlDocument.Annotation)
    **/
   public void visit(HtmlDocument.Annotation a)
   {
      visitElement(a);
   }

   /**
    * Utility method to visit any html element. Keeps the element which will be
    * used while interpreting 'Newline' element for writting to stream and
    * writes it to output stream. Used by all elements except 'Newline' element.
    *
    * @param e the html element to write, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>e</code> is <code>null</code>
    **/
   private void visitElement(HtmlDocument.HtmlElement e)
   {
      if(e == null)
         throw new IllegalArgumentException(
            "The element to be written can not be null.");

      m_previousElement = e;
      m_out.print(e);
   }

   /**
    * Prints/Writes new line if this element is encountered in &lt;PRE&gt;
    * block or the flag <code>m_IgnoreLineBreaks</code> is set to
    * <code>false</code>.
    * <br>
    * If this element is not with in the the &lt;PRE&gt; block and the previous
    * element of this 'Newline' element is 'Text' element then prints a single
    * space irrespective of <code>m_IgnoreLineBreaks</code> flag.
    * <br>
    * The reason for above cause is browser interprets the following html
    * element <br>
    * &lt;b&gt;<br>
    * I am<br>
    * testing<br>
    * new line<br>
    * &lt;/b&gt;<br> as 'I am testing new line'. If we take out the new line in
    * this case, this results in to 'I amtestingnew line' which is wrong.
    * Invoked when 'Newline' element is encountered.
    *
    * @param n the html new line element to write, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>n</code> is <code>null</code>
    * @see HtmlVisitor#visit(HtmlDocument.Newline)
    **/
   public void visit(HtmlDocument.Newline n)
   {
      if(n == null)
         throw new IllegalArgumentException(
            "The new line element to be written can not be null.");

      if (m_inPreBlock)
         m_out.println();
      else if (m_previousElement instanceof HtmlDocument.Text)
         m_out.print(" ");
      else if(!m_IgnoreLineBreaks)
         m_out.println();

      m_previousElement = n;
   }
}

