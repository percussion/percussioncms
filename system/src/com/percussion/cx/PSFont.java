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
package com.percussion.cx;

import com.percussion.cx.error.IPSContentExplorerErrors;
import com.percussion.cx.error.PSContentExplorerException;
import com.percussion.util.PSXMLDomUtil;

import java.awt.Font;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * This class provides a way of constructing the Java Font object from the
 * XML document and to get XML document from the Java Font object. The DTD used
 * by this class is defined in the file sys_Font.dtd.
 */
public class PSFont implements IPSClientObjects
{
   static Logger log = Logger.getLogger(PSFont.class);
   
   /**
    * Default constructor, this is needed so that an instance of this class can
    * be created by reflection.  {@link #fromXml(Element) fromXml(Element)}
    * should be called immediately after calling this constructor in order
    * to create a valid object.
    *
    * @param sourceNode   the XML element node from which to populate.  Must not
    * be <code>null</code>.
    * @throws PSContentExplorerException if the XML element node does not
    * represent a type supported by this class.
    */
   public PSFont(Element sourceNode)
   {
      try
      {
         fromXml(sourceNode);
      }
      catch(PSContentExplorerException e)
      {
         log.error(e);
      }
   }

   /**
    * Constructs this object from the supplied font.
    *
    * @param font the font, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if font is <code>null</code>
    */
   public PSFont(Font font)
   {
      setFont(font);
   }

   /** @see IPSClientObjects */
   public void fromXml(Element sourceNode) throws PSContentExplorerException
   {
      if(sourceNode == null)
         throw new IllegalArgumentException("optionsElement must not be null");
      try
      {
         // validate the root element:
         PSXMLDomUtil.checkNode(sourceNode, ELEM_FONT);
         Element tempEle = null;

         // get and set style:
         tempEle = PSXMLDomUtil.getFirstElementChild(sourceNode, ELEM_STYLE);
         setStyle(getStyle(PSXMLDomUtil.getElementData(tempEle)));

         // get and set size:
         tempEle = PSXMLDomUtil.getNextElementSibling(tempEle, ELEM_SIZE);
         setSize(Integer.parseInt(PSXMLDomUtil.getElementData(tempEle)));

         // get and set face:
         tempEle = PSXMLDomUtil.getNextElementSibling(tempEle, ELEM_FACE);
         setFace(PSXMLDomUtil.getElementData(tempEle));
      }
      catch (Exception e)
      {
         throw new PSContentExplorerException(
            IPSContentExplorerErrors.MISC_PROCESSING_OPTIONS_ERROR,
            e.getLocalizedMessage());

      }
   }

   /** @see IPSClientObjects  */
   public Element toXml(Document doc)
   {
      // create root and its attributes
      Element root = doc.createElement(ELEM_FONT);
      doc.appendChild(root);

      // create temp text node:
      Text valueTextNode = null;
      Element temp = null;

      // create style node:
      temp = doc.createElement(ELEM_STYLE);
      // add value:
      valueTextNode = doc.createTextNode(getStyleAsString(getStyle()));
      temp.appendChild(valueTextNode);
      root.appendChild(temp);

      // create size node:
      temp = doc.createElement(ELEM_SIZE);
      // add value:
      valueTextNode = doc.createTextNode("" + getSize());
      temp.appendChild(valueTextNode);
      root.appendChild(temp);

      // create color node:
      temp = doc.createElement(ELEM_FACE);
      // add value:
      valueTextNode = doc.createTextNode(getFace());
      temp.appendChild(valueTextNode);
      root.appendChild(temp);

      return root;
   }

   /**
    * Indicates whether some other object is "equal to" this one.
    * Overrides the method in {@link Object.equals(Object) Object} and adheres
    * to that contract.
    * @param obj the reference object with which to compare.
    * @return <code>true</code> if this object is the same as the
    * <code>obj</code> argument; <code>false</code> otherwise. If
    * <code>null</code> supplied or obj is not an instance of this class,
    * <code>false</code> is returned.
    */
   public boolean equals(Object obj)
   {
      if(obj == null || !(getClass().isInstance(obj)))
         return false;

      PSFont comp = (PSFont) obj;

      if(m_style != comp.m_style)
         return false;
      if(m_size != comp.m_size)
         return false;
      if(!m_face.equals(comp.m_face))
         return false;

      return true;
   }

   /**
    * Overridden to fulfill contract of this method as described in
    * {@link Object#hashCode() Object}.
    *
    * @return A hash code value for this object
    */
   public int hashCode()
   {
      int hash = 0;
      // super is abtract, don't call
      hash += m_style;
      hash += m_size;
      hash += m_face.hashCode();

      return hash;
   }

   /**
    * The face used by this font.
    *
    * @return may be empty if {@link #fromXml(Document) fromXml(Document)}
    * hasn't been called, never <code>null</code>
    */
   public String getFace()
   {
      return m_face;
   }

   /**
    * Sets the style to be used with this font.
    *
    * @param the style to use.  Must be one of the values in the
    * {@link #getStyle() getStyle()} description
    */
   private void setStyle(int style)
   {
      if(style == Font.PLAIN || style == Font.BOLD || style == Font.ITALIC ||
         style == (Font.BOLD | Font.ITALIC) )
      {
         m_style = style;
      }
      else
         throw new IllegalArgumentException("style is not valid");
   }

   /**
    * Returns the style used with this class.  The return values corresponds
    * to the styles listed {@link java.awt.Font#PLAIN, plain},
    * {@link java.awt.Font#BOLD, bold}, {@link java.awt.Font#ITALIC, italic}
    * or ITALIC|BOLD
    *
    * @return the style of this class.
    */
   public int getStyle()
   {
      return m_style;
   }

   /**
    * Sets the size to be used with this font.
    *
    * @param the size, must be > 0.
    */
   private void setSize(int size)
   {
      if(size <= 0)
         throw new IllegalArgumentException("size must be > 0");

      m_size = size;

   }

   /**
    * Gets the style int based on the string value.
    *
    * @param styleAsString assumed not <code>null</code>.
    * @return one of the possible style values.  Defaults to
    * {@link #TEXT_STYLE_PLAIN TEXT_STYLE_PLAIN}
    * @see #getStyle()
    */
   private int getStyle(String styleAsString)
   {
      // default:
      int theStyle = Font.PLAIN;
      if (styleAsString.equalsIgnoreCase(TEXT_STYLE_BOLD))
         theStyle = Font.BOLD;

      if (styleAsString.equalsIgnoreCase(TEXT_STYLE_ITALIC))
         theStyle = Font.ITALIC;

      if (styleAsString.equalsIgnoreCase(TEXT_STYLE_BOLDITALIC))
         theStyle = Font.BOLD|Font.ITALIC;

      return theStyle;
   }

   /**
    * Gets the string represenation of the value from the int value.
    * @param the int value of the style.
    * @return the string value of the style.
    */
   private String getStyleAsString(int style)
   {
      // default:
      String theStyle = TEXT_STYLE_PLAIN;

      if(style == Font.BOLD)
         theStyle = TEXT_STYLE_BOLD;

      if(style == Font.ITALIC)
         theStyle = TEXT_STYLE_ITALIC;

      if(style == (Font.BOLD|Font.ITALIC))
         theStyle = TEXT_STYLE_BOLDITALIC;

      return theStyle;
   }

   /**
    * The size of this font.
    *
    * @return  > 0.
    */
   public int getSize()
   {
      return m_size;
   }

   /**
    * Creates a <code>java.awt.Font</code> object from the values in this class.
    * This value is not owned by this object and any modifications to it will
    * not be referenced by this class.
    *
    * @return never <code>null</code>.
    */
   public Font getFont()
   {
      return new Font(getFace(), getStyle(), getSize());
   }

   /**
    * Populates the face, style and size from a <code>java.awt.Font</code>
    * object.
    *
    * @param the font from which to populate this object, must not be
    * <code>null</code>.
    */
   public void setFont(Font font)
   {
      if(font == null)
         throw new IllegalArgumentException("font must not be null");

      setFace(font.getName());
      setStyle(font.getStyle());
      setSize(font.getSize());
   }

   /**
    * Sets the face of this font.
    *
    * @param the name of the font-face to use.  Must not be <code>null</code>
    * or empty.
    */
   private void setFace(String face)
   {
      if(face  == null || face.trim().length() == 0)
         throw new IllegalArgumentException(
            "face must not be null or empty.");

      m_face = face;
   }

   /**
    * The style to be used with this font, initialized by
    * <code>setStyle(int)</code>, may be changed.
    */
   private int m_style = 0;

   /**
    * The size of this font, initialized by <code>setSize(int)</code>, may be
    * changed.
    */
   private int m_size = -1;

   /**
    * The face of this font, initialized by <code>setFace(String)</code>, may be
    * changed, never <code>null</code>.
    */
   private String m_face = "";

   /**
    * The possible values for the style <code>TEXT</code> element.
    */
   private static final String TEXT_STYLE_PLAIN = "plain";
   private static final String TEXT_STYLE_BOLD = "bold";
   private static final String TEXT_STYLE_ITALIC = "italic";
   private static final String TEXT_STYLE_BOLDITALIC = "bolditalic";

   /**
    * Name of the Root element of the XML document representing a font.
    */
   private static final String ELEM_FONT = "PSXFont";

   /**
    * Name of the element representing the face name of the font.
    */
   private static final String ELEM_FACE = "Face";

   /**
    * Name of the element representing the style of the font.
    */
   private static final String ELEM_STYLE = "Style";

   /**
    * Name of the element representing the size of the font.
    */
   private static final String ELEM_SIZE = "Size";
}