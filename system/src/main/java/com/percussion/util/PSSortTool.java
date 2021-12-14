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
package com.percussion.util;

import java.util.AbstractList;
import java.util.Collections;
import java.util.Comparator;

/**
 *   Tools for sorting arrays and Vectors. Empirically, I have found array
 *   sorting to be almost twice as fast as vector sorting. MergeSort is also
 *   faster than quicksort. For large Vectors, it is often faster to convert
 *   the vector to an array, sort it, then copy it back into the vector
 *   (no, I'm not kidding!). Experiment.
 */
public class PSSortTool
{
   /**
    * The implementation of this method was found to have problems;
    * as a part of a bug fix Rx-03-05-0642 replaced the whole body
    * with one call: Collections.sort(list, comper).
    * @param list to sort, never <code> null </code>. 
    * @param comper comparator, never <code> null </code>.
    */
    public static void QuickSort(AbstractList list, Comparator comper)
    {
       if (list== null)
         throw new IllegalArgumentException("list may not be null");
         
       if (comper== null)
         throw new IllegalArgumentException("comper may not be null");
         
       
         
      Collections.sort(list, comper);
    }
   
   /**
    * Iterative (non-recursive) QuickSort with median-of-three
    * partitioning to avoid slow behavior on mostly-sorted lists.
    */
    public static void QuickSort(Object[] list, Comparator comper)
    {
      class StackItem
      {
         public int left;
         public int right;
      }

        // create stack
        final int stackSize = 32;
        StackItem [] stack = new StackItem [stackSize];

        for (int n = 0; n < 32; ++n)
            stack[n] = new StackItem();

        int stackPtr = 0;

        // size of minimum partition to median-of-three
        final int Threshold = 7;

        // sizes of left and right partitions
        int lsize, rsize;

        // create working indexes
        int l, r, mid, scanl, scanr, pivot;

        // set initial values
        l = 0;
        r = list.length - 1;

        Object a, b, c;
    
        // main loop
        while (true)
        {
            while (r > l)
            {
                if ((r - l) > Threshold)
                {
                    // "median-of-three" partitioning
                    mid = (l + r) / 2;
    
               a = list[l];
               b = list[mid];
               c = list[r];

                    // three-sort left, middle, and right elements
                    if (comper.compare(b, a) < 0)
                    {
                        list[mid] = a;
                        list[l] = b;
                  a = b;
                  b = list[mid];
                    }

                    if (comper.compare(c, a) < 0)
                    {
                        list[r] = a;
                        list[l] = c;
                  a = c;
                  c = list[r];
                    }

                    // three-sort left, middle, and right elements
                    if (comper.compare(c, b) < 0)
                    {
                        list[mid] = c;
                        list[r] = b;
                    }

                    // set-up for partitioning
                    pivot = r - 1;
    
                    a        = list[mid];
                    list[mid]   = list[pivot];
                    list[pivot] = a;
    
                    scanl = l + 1;
                    scanr = r - 2;
                }
                else
                {
                    // set-up for partitioning
                    pivot = r;
                    scanl = l;
                    scanr = r - 1;
                }
    
            b = list[pivot];
                for (;;)
                {
                    // scan from left for element >= to pivot
                    while ((comper.compare(list[scanl],b) < 0) && (scanl < r))
                        ++scanl;
    
                    // scan from right for element <= to pivot
                    while ((comper.compare(list[pivot],list[scanr]) < 0) && (scanr > l))
                        --scanr;
    
                    // if scans have met, exit inner loop
                    if (scanl >= scanr)
                        break;
    
                    // exchange elements
                    a  = list[scanl];
                    list[scanl] = list[scanr];
                    list[scanr] = a;
    
                    if (scanl < r)
                        ++scanl;
    
                    if (scanr > l)
                        --scanr;
                }
    
                // exchange final element
                a  = list[scanl];
                list[scanl] = list[pivot];
                list[pivot] = a;
    
                // place largest partition on stack
                lsize = scanl - l;
                rsize = r - scanl;
    
                if (lsize > rsize)
                {
                    if (lsize != 1)
                    {
                        ++stackPtr;
    
/*         
                        if (stackPtr == stackSize)
                            throw some kind of error;
*/    
                        stack[stackPtr].left  = l;
                        stack[stackPtr].right = scanl - 1;
                    }
    
                    if (rsize != 0)
                        l = scanl + 1;
                    else
                        break;
                }
                else
                {
                    if (rsize != 1)
                    {
                        ++stackPtr;
/*    
                        if (stackPtr == stackSize)
                      throw some kind of error;
*/  
                        stack[stackPtr].left  = scanl + 1;
                        stack[stackPtr].right = r;
                    }
    
                    if (lsize != 0)
                        r = scanl - 1;
                    else
                        break;
                }
            }
    
            // iterate with values from stack
            if (stackPtr != 0)
            {
                l = stack[stackPtr].left;
                r = stack[stackPtr].right;
    
                --stackPtr;
            }
            else
                break;
        }
    }

   public static void MergeSort(Object[] a, Comparator c)
   {
      Object aux[] = (Object[])a.clone();
      mergeSort(aux, a, 0, a.length, c);      
   }

   private static void mergeSort(Object src[], Object dest[],
      int low, int high, Comparator c)
   {
      int length = high - low;
      
      // Insertion sort on smallest arrays
      if (length < 7)
      {
         Object temp;
         for (int i=low; i < high; i++)
            for (int j=i; j>low && c.compare(dest[j-1], dest[j])>0; j--)
         {
            temp = dest[j];
            dest[j] = dest[j-1];
            dest[j-1] = temp;
         }
         return;
      }
      
      // Recursively sort halves of dest into src
      int mid = (low + high)/2;
      mergeSort(dest, src, low, mid, c);
      mergeSort(dest, src, mid, high, c);
      
      // If list is already sorted, just copy from src to dest.  This is an
      // optimization that results in faster sorts for nearly ordered lists.
      if (c.compare(src[mid-1], src[mid]) <= 0)
      {
         System.arraycopy(src, low, dest, low, length);
         return;
      }
      
      // Merge sorted halves (now in src) into dest
      for(int i = low, p = low, q = mid; i < high; i++)
      {
         if (q>=high || p<mid && c.compare(src[p], src[q]) <= 0)
            dest[i] = src[p++];
         else
            dest[i] = src[q++];
      }
   }
}
