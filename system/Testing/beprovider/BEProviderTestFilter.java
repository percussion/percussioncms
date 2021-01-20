/*[ BEProviderTestFilter.java ]************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

import com.percussion.extension.PSDefaultExtension;

public class BEProviderTestFilter extends PSDefaultExtension
   implements com.percussion.security.IPSPasswordFilter
{
   public static void main(String[] args)
   {
      BEProviderTestFilter filter = new BEProviderTestFilter();

      for (int i = 0; i < args.length; i++)
      {
         System.out.println(args[i]+"-->"+filter.encrypt(args[i]));
      }
   }

   public String encrypt(String pw)
   {
      char[] chars = new char[pw.length()];
      pw.getChars(0,pw.length(),chars,0);
      char[] reverseChars = new char[pw.length()];
      for (int i = 1; i <= chars.length; i++)
      {
         reverseChars[i - 1] = chars[chars.length - i];
      }
      return new String(reverseChars);
   }
}
