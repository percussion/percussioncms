/******************************************************************************
 *
 * [ RxVLayoutManager.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.concurrent.ConcurrentHashMap;

/**
    RxVLayoutManager is a LayoutManager that arranges components in a vertical
    (or horizontal) stack aligning them at right, left or centered, and/or
    filling them to take up any extra vertical or horizontal space.
    Arrangement tags are provided by using the add(tag,component) form to
    add components to the container.
   
    The tag consists of one or more of the following, with the two forms
    applying to horizontal or vertical dimension.
   
  <pre>
    The tag supplied to the add method is string that is a combination of any of
    the following options:
    
    Positioning: "CENTER" | "LEFT" | "LEFTTOP" | "RIGHT" | "RIGHTBOTTOM"
    
    Sizing:
     "WIDE"    or "TALL"     : filled to use all available space.
     "WIDE*#"  or "TALL*#"   : filled but weighted by the number #.
     "FILL" (or "FILL*#")    : filled in both directions.
     "WIDTH=#" or "HEIGHT=#" : given explicit width|height
    
    Margins:  "FLUSH"        : margins are not added around this component.
    
    Example: 
    Create a Vertical Stack Layout aligned at the LEFT TOP and with a 5 v gap:
    
    Container c = getContentPane();
    c.setLayout(new RxVLayoutManager());
    
    c.add("LeftTopWide", new Label("say hello to V layout"));
    
    TextField comp = new TextField();
    c.add("LeftTopWide", comp);
  </pre>
*/
public class RxVLayoutManager implements LayoutManager
{
   /** 
    * Default Ctor.
    * Creates a vertical stack layout, each comp. will be aligned at the
    * left top with a default v gap.
    */
   public RxVLayoutManager()
   {
   }

   /**
    * Create a RxVLayoutManager with the given orientation.
    * @param orientation VERTICAL or HORIZONTAL.
    */
   public RxVLayoutManager(int orientation)
   {
      if (orientation != VERTICAL && orientation != HORIZONTAL)
         throw new IllegalArgumentException("unsupported orientation");
               
      m_orientation = orientation;
   }

   /**
    * Create a RxVLayoutManager with the given m_orientation and space 
    * between components. 
    * @param orientation VERTICAL or HORIZONTAL.
    * @param margin v or h gap, must be >= 0.
    */
   public RxVLayoutManager(int orientation, int margin)
   {
      if (orientation != VERTICAL && orientation != HORIZONTAL)
         throw new IllegalArgumentException("unsupported orientation");
         
      if (margin < 0)
         throw new IllegalArgumentException("margin may not be < 0");
         
      m_orientation = orientation;
      m_margin = margin;
   }

   /**
    * Adds the specified component to the layout in a specific arrangement.
    * 
    * see 
    * {@link LayoutManager#addLayoutComponent(
    * java.lang.String, java.awt.Component)} for details.
    */
   public void addLayoutComponent(String tag, Component comp)
   {
      tag = tag.toUpperCase().trim();
      int hcode = CODE_CENTER, vcode = CODE_CENTER, harg = 0, varg = 0;
      int i, l = tag.length(), n;
      for (i = 0; i < l;)
      {
         if (tag.startsWith(CENTER, i))
         {
            i += 6;
         }
         else if (tag.startsWith(LEFT, i))
         {
            i += 4;
            hcode |= CODE_FRONT;
         }
         else if (tag.startsWith(TOP, i))
         {
            i += 3;
            vcode |= CODE_FRONT;
         }
         else if (tag.startsWith(RIGHT, i))
         {
            i += 5;
            hcode |= CODE_BACK;
         }
         else if (tag.startsWith(BOTTOM, i))
         {
            i += 6;
            vcode |= CODE_BACK;
         }
         else if (tag.startsWith(WIDE, i))
         {
            i += 4;
            hcode |= CODE_FILL;
            if (tag.startsWith("*", i))
            {
               i++;
               n = countDigits(tag, i);
               harg = parseIntArgument(tag, i, n);
               i += n;
            }
            else
               harg = 1;
         }
         else if (tag.startsWith(TALL, i))
         {
            i += 4;
            vcode |= CODE_FILL;
            if (tag.startsWith("*", i))
            {
               i++;
               n = countDigits(tag, i);
               varg = parseIntArgument(tag, i, n);
               i += n;
            }
            else
               varg = 1;
         }
         else if (tag.startsWith(FILL, i))
         {
            i += 4;
            hcode |= CODE_FILL;
            vcode |= CODE_FILL;
            if (tag.startsWith("*", i))
            {
               i++;
               n = countDigits(tag, i);
               harg = varg = parseIntArgument(tag, i, n);
               i += n;
            }
            else
               harg = varg = 1;
         }
         else if (tag.startsWith(WIDTH, i))
         {
            i += 5;
            hcode |= CODE_ABS;
            if (tag.startsWith("=", i))
            {
               i++;
               n = countDigits(tag, i);
               harg = parseIntArgument(tag, i, n);
               i += n;
            }
            else
            {
               harg = -1;
               break;
            }
         }
         else if (tag.startsWith(HEIGHT, i))
         {
            i += 6;
            vcode |= CODE_ABS;
            if (tag.startsWith("=", i))
            {
               i++;
               n = countDigits(tag, i);
               varg = parseIntArgument(tag, i, n);
               i += n;
            }
            else
            {
               varg = -1;
               break;
            }
         }
         else if (tag.startsWith(FLUSH, i))
         {
            i += 5;
            hcode |= CODE_FLUSH;
            vcode |= CODE_FLUSH;
         }
         else
         {
            harg = -1;
            break;
         }

         while ((i < l) && Character.isWhitespace(tag.charAt(i)))
            i++;
      }
      
      if ((harg == -1) || (varg == -1))
         throw new IllegalArgumentException("RxVLayoutManager: invalid tag: "
          + tag + " harg: " + harg + " varg: " + varg);
            
      else
      {
         int codes[] = { hcode, vcode, harg, varg };
         m_codeTable.put(comp, codes);
      }
   }

   /**
    * Calculates the number of digits in a given tag string.
    * 
    * @param tag the number of digits in this string will be counted, asssumed
    * not <code>null</code>.
    * @param i this is the index from which to start counting digits in
    * <code>tag</code>.
    * 
    * @return the number of digits in the given tag, starting at and including
    * the character at i.
    */
   private int countDigits(String tag, int i)
   {
      int l = tag.length();
      int j = i;
            
      while ((j < l) && Character.isDigit(tag.charAt(j)))
         j++;
      
      return j - i;
   }

   /**
    * Converts the character(s) of the given tag starting at the given index
    * into an int.
    *  
    * @param tag a character from this string will be converted to an int,
    * assumed not <code>null</code>.
    * @param i the beginning index of the numeric characters.
    * @param n the length of the numeric character substring.
    *
    * @return an int representation of the numeric character(s), -1 if the
    * character(s) could not be converted. 
    */
   private int parseIntArgument(String tag, int i, int n)
   {
      int num = -1;
      try
      {
         num = Integer.parseInt(tag.substring(i, i + n));
      }
      catch (Exception e)
      {
      }
      return num;
   }

   /**
    *  Remove the specified component from the layout.
    */
   public void removeLayoutComponent(Component comp)
   {
      m_codeTable.remove(comp);
   }

   /**
    * Accessor for a component's layout positioning.
    * 
    * @param comp the component, assumed not <code>null</code>.
    * 
    * @return the codes representing a component's positioning in the layout or
    * the default codes if none were found.
    */
   private int[] getCode(Component comp)
   {
      int code[] = m_codeTable.get(comp);
      return (code == null ? m_defaultCodes : code);
   }

   /**
    * Determines if a component in the layout is set to fill both horizontally
    * and vertically.
    * 
    * @param comp the component, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the component is filled in both directions
    * (horizontal, vertical), <code>false</code> otherwise.
    */
   @SuppressWarnings("unused")
   private boolean stretches(Component comp)
   {
      int c[] = getCode(comp);
      return c[m_orientation] == CODE_FILL;
   }

   /**
    * Computes the size dimensions for the specified container.
    * 
    * @param parent the container, assumed not <code>null</code>.
    * @param preferred if <code>true</code>, for components set to fill both
    * horizontally and vertically, the preferred size will be used, otherwise
    * the minimum size will be used.
    * 
    * @return a <code>Dimension</code> which represents the size of the 
    * container, never <code>null</code>.
    */
   private Dimension computeLayoutSize(Container parent, boolean preferred)
   {
      Insets in = parent.getInsets();
      int inW = in.left + in.right, inH = in.top + in.bottom;
      int n = parent.getComponentCount();
      
      if (m_orientation == HORIZONTAL)
      {
         int maxH = 0, totW = 0, m;
         for (int i = 0; i < n; i++)
         {
            Component comp = parent.getComponent(i);
            if (comp.isVisible())
            {
               int code = getCode(comp)[m_orientation];
               m = ((code & CODE_FLUSH) == 0 ? m_margin : 0);
               Dimension d =
                  (preferred
                     && ((code & SIZEMASK) == CODE_FILL)
                        ? comp.getPreferredSize()
                        : comp.getMinimumSize());
               maxH = Math.max(maxH, d.height + 2 * m);
               totW += d.width + 2 * m;
            }
         }
         
         return new Dimension(totW + inW , maxH + inH );
      }
      else
      {
         int maxW = 0, totH = 0, m;
         for (int i = 0; i < n; i++)
         {
            Component comp = parent.getComponent(i);
            if (comp.isVisible())
            {
               int code = getCode(comp)[m_orientation];
               m = ((code & CODE_FLUSH) == 0 ? m_margin : 0);
               Dimension d =
                  (preferred
                     && ((code & SIZEMASK) == CODE_FILL)
                        ? comp.getPreferredSize()
                        : comp.getMinimumSize());
               maxW = Math.max(maxW, d.width + 2 * m);
               totH += d.height + 2 * m;
            }
         }
         
         return new Dimension(maxW + inW , totH + inH );
      }
   }

   /** Calculate the minimum size dimensions for the specififed container.*/
   public Dimension minimumLayoutSize(Container parent)
   {
      return computeLayoutSize(parent, false);
   }

   /** Calculate the preferred size dimensions for the specififed container.*/
   public Dimension preferredLayoutSize(Container parent)
   {
      return computeLayoutSize(parent, true);
   }

   /** Lays out the specified container. */
   public void layoutContainer(Container parent)
   {
      int along = m_orientation, across = (m_orientation + 1) % 2;
      int n = parent.getComponentCount();
      Insets in = parent.getInsets();
      Dimension sz = parent.getSize();
      final int SLACK = 10;
      int W = sz.width - in.left - in.right - SLACK;
      int H = sz.height - in.top - in.bottom - SLACK;
      int L = (m_orientation == HORIZONTAL ? W : H); // total running Length
      int D = (m_orientation == HORIZONTAL ? H : W); // sideways Depth.

      // First pass: find visible components, record min. sizes,
      // find out how much leftover space there is.
      int nFills = 0, nRubber = 0;
      int sum = 0, prev;
      prev = CODE_FRONT;
      int codes[][] = new int[n][];
      int sizes[][] = new int[n][2];
      for (int i = 0; i < n; i++)
      { // determine # of fills & remaining space.
         Component comp = parent.getComponent(i);
         if (comp.isVisible())
         {
            Dimension d = comp.getMinimumSize();
            int code[] = getCode(comp);
            int size[] = sizes[i];
            codes[i] = code;
            size[0] = d.width;
            size[1] = d.height;
            int l = size[along], c = code[along];
            switch (c & SIZEMASK)
            {
               case CODE_FILL :
                  nFills += code[along + 2];
                  break;
               case CODE_ABS :
                  sum += code[along + 2];
                  break;
               default :
                  sum += l;
                  break;
            }
            switch (c & POSMASK)
            {
               case CODE_CENTER :
                  nRubber++;
                  break;
               case CODE_BACK :
                  if (prev != CODE_BACK)
                     nRubber++;
                  break;
            }
            if ((c & CODE_FLUSH) == 0)
               sum += 2 * m_margin;
            prev = (c & POSMASK);
         }
      }
      if (prev == CODE_CENTER)
         nRubber++;
      // Divide up the leftover space among filled components (if any)
      // else as filler between centered or justified components.
      int rubber =
         ((nFills != 0)
            || (nRubber == 0) ? 0 : Math.max(0, (L - sum) / nRubber)),
         fill = (nFills == 0 ? 0 : Math.max(0, (L - sum) / nFills));

      // Second pass: layout the components.
      int r = (m_orientation == HORIZONTAL ? in.left : in.top),
      
      // running pos.
      s0 = (m_orientation == HORIZONTAL ? in.top : in.left), // side pos.
      s, l, d, m;
      
      prev = CODE_FRONT;
      for (int i = 0; i < n; i++)
      {
         int code[] = codes[i];
         int size[] = sizes[i];
         if (code != null)
         {
            int c = code[along], ca = code[across];
            m = ((c & CODE_FLUSH) == 0 ? m_margin : 0);
            r += m;
            s = s0 + m;
            l = size[along];
            d = size[across];
            switch (c & SIZEMASK)
            {
               case CODE_FILL :
                  if (fill > 0)
                     l = fill * code[along + 2];
                  break;
               case CODE_ABS :
                  l = code[along + 2];
                  break;
            }
            switch (c & POSMASK)
            {
               case CODE_CENTER :
                  r += rubber;
                  break;
               case CODE_BACK :
                  if (prev != CODE_BACK)
                     r += rubber;
                  break;
            }
            prev = (c & POSMASK);
            switch (ca & SIZEMASK)
            {
               case CODE_FILL :
                  d = D - 2 * m;
                  break;
               case CODE_ABS :
                  d = code[across + 2];
                  break;
            }
            switch (ca & POSMASK)
            {
               case CODE_BACK :
                  s += D - d;
                  break;
               case CODE_CENTER :
                  s += (D - d) / 2;
                  break;
            }
            Component comp = parent.getComponent(i);
            
            if (m_orientation == HORIZONTAL)
               comp.setBounds(r + SLACK / 2, s, l, d);
            else
               comp.setBounds(s + SLACK / 2, r, d, l);
               
            r += l + m;
         }
      }
   }

   /** The orientation constant for horizontal layouts. */
   public static final int HORIZONTAL = 0;
   /** The orientation constant for vertical layouts. */
   public static final int VERTICAL = 1;

   private int m_defaultCodes[] = { CODE_CENTER, CODE_CENTER, 0, 0 };
   private int m_orientation = VERTICAL;
   private int m_margin = 1;
   private ConcurrentHashMap<Component, int[]> m_codeTable =
      new ConcurrentHashMap<>();

   /* Layout codes */
   private static final int CODE_CENTER = 0;
   private static final int CODE_FRONT = 1;
   private static final int CODE_BACK = 2;
   private static final int CODE_FILL = 4;
   private static final int CODE_ABS = 8;
   private static final int CODE_FLUSH = 16;

   private static final int POSMASK = 0x03;
   private static final int SIZEMASK = 0x0C;
   
   /**
    * Constant for center positioning tag option.
    */
   public static final String CENTER = "CENTER";
   
   /**
    * Constant for left positioning tag option.
    */
   public static final String LEFT = "LEFT";
   
   /**
    * Constant for top positioning tag option.
    */
   public static final String TOP = "TOP";
   
   /**
    * Constant for right positioning tag option.
    */
   public static final String RIGHT = "RIGHT";
   
   /**
    * Constant for bottom positioning tag option.
    */
   public static final String BOTTOM = "BOTTOM";
   
   /**
    * Constant for width sizing tag option.
    */
   public static final String WIDTH = "WIDTH";
   
   /**
    * Constant for height sizing tag option.
    */
   public static final String HEIGHT = "HEIGHT";
   
   /**
    * Constant for tall sizing tag option.
    */
   public static final String TALL = "TALL";
   
   /**
    * Constant for wide sizing tag option.
    */
   public static final String WIDE = "WIDE";
   
   /**
    * Constant for fill sizing tag option.
    */
   public static final String FILL = "FILL";
   
   /**
    * Constant for flush margin tag option.
    */
   public static final String FLUSH = "FLUSH";
}
