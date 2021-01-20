/******************************************************************************
 *
 * [ RxIAConsoleUtils.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installanywhere;

import com.zerog.ia.api.pub.ConsoleUtils;
import com.zerog.ia.api.pub.CustomCodeConsoleProxy;
import com.zerog.ia.api.pub.PreviousRequestException;

import java.util.Vector;

/**
 * This is a wrapper class for the install platform console utility class which
 * provides io functionality for console installations.
 * 
 * @author peterfrontiero
 */
public class RxIAConsoleUtils
{
   /**
    * Constructor which initializes the {@link ConsoleUtils} object.
    * 
    * @param proxy the console proxy object used to access the console
    * utility class, may not be <code>null</code>.
    */
   public RxIAConsoleUtils(CustomCodeConsoleProxy proxy)
   {
      if (proxy == null)
         throw new IllegalArgumentException("proxy may not be null");
      
      m_consoleUtils = (ConsoleUtils) proxy.getService(ConsoleUtils.class);
   }
   
   /**
    * Creates a console prompt with the given String and returns the value given
    * by the user in response to the console prompt.  Calls
    * {@link ConsoleUtils#promptAndGetValue(java.lang.String)}.
    * 
    * @param arg0 the prompt.
    * 
    * @return user input value.
    * @throws RxIAPreviousRequestException
    */
   public String promptAndGetValue(String arg0) throws
   RxIAPreviousRequestException
   {
      try
      {
         return m_consoleUtils.promptAndGetValue(arg0);
      }
      catch (PreviousRequestException e)
      {
         // User has chosen to go to the previous console
         throw new RxIAPreviousRequestException();
      }
   }

  
   /**
    * Creates a console prompt with the given String and returns the value given
    * by the user in response to the console prompt.  Calls
    * {@link ConsoleUtils#promptAndGetValueWithDefaultValue(java.lang.String,
    *  java.lang.String)}.
    * 
    * @param arg0 the prompt.
    * @param arg1 the default value.
    * 
    * @return user input value.
    * @throws RxIAPreviousRequestException
    */
   public String promptAndGetValueWithDefaultValue(String arg0, String arg1)
   throws RxIAPreviousRequestException
   {
      try
      {
         return m_consoleUtils.promptAndGetValueWithDefaultValue(arg0, arg1);
      }
      catch (PreviousRequestException e)
      {
         // User has chosen to go to the previous console
         throw new RxIAPreviousRequestException();
      }
   }

   /**
    * Creates a console prompt with the given String and returns the value given
    * by the user in response to the console prompt.  Calls
    * {@link ConsoleUtils#promptAndGetValue(java.lang.String, boolean)}.
    * 
    * @param arg0 the prompt.
    * @param arg1 whether or not to require a value from the user 
    * (<code>true</code> requires a value). 
    *
    * @return the user input value.
    * @throws RxIAPreviousRequestException
    */
   public String promptAndGetValue(String arg0, boolean arg1) throws
   RxIAPreviousRequestException
   {
      try
      {
         return m_consoleUtils.promptAndGetValue(arg0, arg1);
      }
      catch (PreviousRequestException e)
      {
         // User has chosen to go to the previous console
         throw new RxIAPreviousRequestException();
      }
   }

   /**
    * Creates a console prompt with the given String and returns the value given
    * by the user in response to the console prompt.  Calls
    * {@link ConsoleUtils#promptAndGetValueWithEchoCharacter(java.lang.String,
    * char)}.
    * 
    * @param arg0 the prompt.
    * @param arg1 the character that is displayed when the user types in a
    * character. 
    *
    * @return the user input value.
    * @throws RxIAPreviousRequestException
    */
   public String promptAndGetValueWithEchoCharacter(String arg0, char arg1)
   throws RxIAPreviousRequestException
   {
      try
      {
         return m_consoleUtils.promptAndGetValueWithEchoCharacter(arg0, arg1);
      }
      catch (PreviousRequestException e)
      {
         // User has chosen to go to the previous console
         throw new RxIAPreviousRequestException();
      }
   }

   /**
    * Creates a console prompt with the given String.
    * Calls {@link ConsoleUtils#enterToContinue(java.lang.String)}.
    * 
    * @param arg0 the prompt.
    * @throws RxIAPreviousRequestException
    */
   public void enterToContinue(String arg0) throws RxIAPreviousRequestException
   {
      try
      {
         m_consoleUtils.enterToContinue(arg0);
      }
      catch (PreviousRequestException e)
      {
         // User has chosen to go to the previous console
         throw new RxIAPreviousRequestException();
      }
   }

   /**
    * Creates a console prompt with default 'enter to continue' text.
    * Calls {@link ConsoleUtils#enterToContinue()}.
    * 
    * @throws RxIAPreviousRequestException
    */
   public void enterToContinue() throws RxIAPreviousRequestException
   {
      try
      {
         m_consoleUtils.enterToContinue();
      }
      catch (PreviousRequestException e)
      {
         // User has chosen to go to the previous console
         throw new RxIAPreviousRequestException();
      }
   }

   /**
    * Convenience method calls {@link #enterToContinuePrevDisabled(String)}.
    */
   public void enterToContinuePrevDisabled()
   {
      enterToContinuePrevDisabled(null);
   }
   
   /**
    * This method prompts using one of the <code>enterToContinue<code> methods
    * but does not allow navigation to the previous console.  Only [ENTER] and
    * [quit] are allowed.
    * 
    * @param arg0 the prompt to be displayed.  If <code>null</code> a default
    * prompt is used.
    */
   public void enterToContinuePrevDisabled(String arg0)
   {
      Exception e;
      do
      {
         e = null;
         try
         {
            if (arg0 == null)
               enterToContinue();
            else
               enterToContinue(arg0);
         }
         catch (RxIAPreviousRequestException pre)
         {
            //Don't allow the user to go to previous console
            e = pre;
         }
      }
      while (e != null);      
   }
   
   /**
    * Constructs a console prompt that requests the user to choose from one of
    * two supplied options.  Calls {@link ConsoleUtils#promptAndBilateralChoice(
    * java.lang.String, java.lang.String, java.lang.String)}.
    * 
    * @param arg0 the prompt.
    * @param arg1 the first option.
    * @param arg2 the second option.
    * 
    * @return the user selection.
    * @throws RxIAPreviousRequestException
    */
   public String promptAndBilateralChoice(String arg0, String arg1, String arg2)
   throws RxIAPreviousRequestException
   {
      try
      {
         return m_consoleUtils.promptAndBilateralChoice(arg0, arg1, arg2);
      }
      catch (PreviousRequestException e)
      {
         // User has chosen to go to the previous console
         throw new RxIAPreviousRequestException();
      }
   }

   /**
    * Constructs a console prompt that requests the user to make a 'yes' or 'no'
    * choice based on the query presented.  Calls
    * {@link ConsoleUtils#promptAndYesNoChoice(java.lang.String)}.
    * 
    * @param arg0 the prompt.
    * 
    * @return a boolean value representing the user's input.  <code>true</code>
    * is 'yes'. 
    * @throws RxIAPreviousRequestException
    */
   public boolean promptAndYesNoChoice(String arg0) throws
   RxIAPreviousRequestException
   {
      try
      {
         return m_consoleUtils.promptAndYesNoChoice(arg0);
      }
      catch (PreviousRequestException e)
      {
         // User has chosen to go to the previous console
         throw new RxIAPreviousRequestException();
      }
   }

   /**
    * Constructs a console prompt that requests the user to make a 'yes' or 'no'
    * choice based on the default 'is this correct text' prompt.  Calls
    * {@link ConsoleUtils#isThisCorrectPrompt()}.
    * 
    * @return <code>true</code> for a user response of 'yes', <code>false</code>
    * for 'no'.
    * @throws RxIAPreviousRequestException
    */
   public boolean isThisCorrectPrompt() throws RxIAPreviousRequestException
   {
      try
      {
         return m_consoleUtils.isThisCorrectPrompt();
      }
      catch (PreviousRequestException e)
      {
         // User has chosen to go to the previous console
         throw new RxIAPreviousRequestException();
      }
   }

   /**
    * Constructs a numbered vertical list of choices from the Vector of Strings
    * provided in the parameters to this method.  Calls
    * {@link ConsoleUtils#createChoiceList(java.util.Vector)}.
    * 
    * @param arg0 set of choices as a Vector.
    * 
    * @return an int[] representing a numerical mapping of the list number 
    * choices to the actual index of the corresponding item in the supplied
    * Vector of Strings.
    */
   public int[] createChoiceList(Vector arg0)
   {
      return m_consoleUtils.createChoiceList(arg0);
   }

   /**
    * Constructs a numbered vertical list of choices from the Vector of Strings
    * provided in the parameters to this method.  Calls
    * {@link ConsoleUtils#createChoiceList(java.util.Vector, int)}.
    * 
    * @param arg0 set of choices as a Vector.
    * @param arg1 index of the default choice in the given Vector.
    * 
    * @return an int[] representing a numerical mapping of the list number 
    * choices to the actual index of the corresponding item in the supplied
    * Vector of Strings.
    */
   public int[] createChoiceList(Vector arg0, int arg1)
   {
      return m_consoleUtils.createChoiceList(arg0, arg1);
   }

   /**
    * Constructs a numbered vertical list of choices from the array of Strings
    * provided in the parameters to this method.  Calls
    * {@link ConsoleUtils#createChoiceList(java.lang.String[])}.
    * 
    * @param arg0 set of choices as String array.
    * 
    * @return an int[] representing a numerical mapping of the list number 
    * choices to the actual index of the corresponding item in the supplied
    * array of Strings.
    */
   public int[] createChoiceList(String[] arg0)
   {
      return m_consoleUtils.createChoiceList(arg0);
   }

   /**
    * Constructs a numbered vertical list of choices from the array of Strings
    * provided in the parameters to this method.  Calls
    * {@link ConsoleUtils#createChoiceList(java.lang.String[], int)}.
    * 
    * @param arg0 set of choices as String array.
    * @param arg1 index of the default choice in the given array.
    * 
    * @return an int[] representing a numerical mapping of the list number 
    * choices to the actual index of the corresponding item in the supplied
    * array of Strings.
    */
   public int[] createChoiceList(String[] arg0, int arg1)
   {
      return m_consoleUtils.createChoiceList(arg0, arg1);
   }

   /**
    * Constructs a numbered vertical list of choices from the array of Strings
    * provided in the parameters to this method.  Calls
    * {@link ConsoleUtils#createChoiceList(java.lang.String[], int[])}.
    * 
    * @param arg0 set of choices as String array.
    * @param arg1 array of indexes of the default choices in the given array.
    * 
    * @return an int[] representing a numerical mapping of the list number 
    * choices to the actual index of the corresponding item in the supplied
    * array of Strings.
    */
   public int[] createChoiceList(String[] arg0, int[] arg1)
   {
      return m_consoleUtils.createChoiceList(arg0, arg1);
   }

   /**
    * Constructs a numbered vertical list of choices from the Vector of Strings
    * provided in the parameters to this method.  Calls
    * {@link ConsoleUtils#createChoiceListAndGetValue(java.lang.String,
    * java.util.Vector)}.
    * 
    * @param arg0 the prompt.
    * @param arg1 set of choices as a Vector.
    * 
    * @return an int representing the index of the chosen item in the supplied
    * Vector of Strings. 
    * @throws RxIAPreviousRequestException 
    */
   public int createChoiceListAndGetValue(String arg0, Vector arg1) throws
   RxIAPreviousRequestException
   {
      try
      {
         return m_consoleUtils.createChoiceListAndGetValue(arg0, arg1);
      }
      catch (PreviousRequestException e)
      {
         // User has chosen to go to the previous console
         throw new RxIAPreviousRequestException();
      }
   }      

   /**
    * Constructs a numbered vertical list of choices from the Vector of Strings
    * provided in the parameters to this method.  Calls
    * {@link ConsoleUtils#createChoiceListAndGetValue(java.lang.String,
    * java.util.Vector, int)}.
    * 
    * @param arg0 the prompt.
    * @param arg1 set of choices as Vector.
    * @param arg2 index of the default choice in the Vector of choices.
    * 
    * @return an int representing the index of the chosen item in the supplied
    * Vector of Strings. 
    * @throws RxIAPreviousRequestException 
    */
   public int createChoiceListAndGetValue(String arg0, Vector arg1, int arg2)
   throws RxIAPreviousRequestException
   {
      try
      {
         return m_consoleUtils.createChoiceListAndGetValue(arg0, arg1, arg2);
      }
      catch (PreviousRequestException e)
      {
         // User has chosen to go to the previous console
         throw new RxIAPreviousRequestException();
      }
   }

   /**
    * Constructs a numbered vertical list of choices from the array of Strings
    * provided in the parameters to this method.  Calls
    * {@link ConsoleUtils#createChoiceListAndGetValue(java.lang.String,
    * java.lang.String[])}.
    * 
    * @param arg0 the prompt.
    * @param arg1 the set of choices as String array.
    * 
    * @return an int representing the actual index of the user selection in the
    * array of Strings.
    * @throws RxIAPreviousRequestException 
    */
   public int createChoiceListAndGetValue(String arg0, String[] arg1) throws
   RxIAPreviousRequestException
   {
      try
      {
         return m_consoleUtils.createChoiceListAndGetValue(arg0, arg1);
      }
      catch (PreviousRequestException e)
      {
         // User has chosen to go to the previous console
         throw new RxIAPreviousRequestException();
      }
   }

   /**
    * Constructs a numbered vertical list of choices from the array of Strings
    * provided in the parameters to this method.  Calls
    * {@link ConsoleUtils#createChoiceListAndGetValue(java.lang.String,
    * java.lang.String[], int)}.
    * 
    * @param arg0 the prompt.
    * @param arg1 the set of choices as array of Strings.
    * @param arg2 index in the choices array of the default choice.
    * 
    * @return index of the user selection in the array of Strings.
    * @throws RxIAPreviousRequestException
    */
   public int createChoiceListAndGetValue(String arg0, String[] arg1, int arg2)
   throws RxIAPreviousRequestException 
   {
      try
      {
         return m_consoleUtils.createChoiceListAndGetValue(arg0, arg1, arg2);
      }
      catch (PreviousRequestException e)
      {
         // User has chosen to go to the previous console
         throw new RxIAPreviousRequestException();
      }
   }

   /**
    * Constructs a numbered vertical list of choices from the Vector of Strings
    * provided in the parameters to this method.  Calls 
    * {@link ConsoleUtils#createChoiceListAndGetMultipleValues(
    * java.lang.String, java.util.Vector)}.
    * 
    * @param arg0 the prompt.
    * @param arg1 set of choices as Vector.
    * 
    * @return int array of the user selections.
    * @throws RxIAPreviousRequestException
    */
   public int[] createChoiceListAndGetMultipleValues(String arg0, Vector arg1)
   throws RxIAPreviousRequestException
   {
      try
      {
         return m_consoleUtils.createChoiceListAndGetMultipleValues(arg0, arg1);
      }
      catch (PreviousRequestException e)
      {
         // User has chosen to go to the previous console
         throw new RxIAPreviousRequestException();
      }
   }

   /**
    * Constructs a numbered vertical list of choices from the array of Strings
    * provided in the parameters to this method.  Calls
    * {@link ConsoleUtils#createChoiceListAndGetMultipleValues(
    * java.lang.String, java.lang.String[])}.
    * 
    * @param arg0 the prompt.
    * @param arg1 the set of choices as String array.
    * 
    * @return an int[] representing the index(es) of the chosen item(s) in the
    * supplied array of Strings. 
    * @throws RxIAPreviousRequestException
    */
   public int[] createChoiceListAndGetMultipleValues(String arg0, String[] arg1)
   throws RxIAPreviousRequestException
   {
      try
      {
         return m_consoleUtils.createChoiceListAndGetMultipleValues(arg0, arg1);
      }
      catch (PreviousRequestException e)
      {
         // User has chosen to go to the previous console
         throw new RxIAPreviousRequestException();
      }
   }

   /**
    * Constructs a numbered vertical list of choices from the array of Strings
    * provided in the parameters to this method.  Calls
    * ConsoleUtils#createChoiceListAndGetMultipleValuesWithDefaults(
    * java.lang.String, java.util.Vector, int[])}.
    * 
    * @param arg0 the user prompt.
    * @param arg1 set of choices as Vector.
    * @param arg2 the default choice values.
    *  
    * @return an int[] representing the index(es) of the chosen item(s) in the
    * supplied array of Strings. 
    * @throws RxIAPreviousRequestException 
    */
   public int[] createChoiceListAndGetMultipleValuesWithDefaults(String arg0,
         Vector arg1, int[] arg2) throws RxIAPreviousRequestException
   {
      try
      {
         return m_consoleUtils.createChoiceListAndGetMultipleValuesWithDefaults(
               arg0, arg1, arg2);
      }
      catch (PreviousRequestException e)
      {
         // User has chosen to go to the previous console
         throw new RxIAPreviousRequestException();
      }
   }

   /**
    * Prints a String without terminating the line at the end of the String.
    * Calls {@link ConsoleUtils#wprint(java.lang.String)}.
    * 
    * @param arg0 the String to print.
    */
   public void wprint(String arg0)
   {
      m_consoleUtils.wprint(arg0);
   }

   /**
    * Prints a String without terminating the line at the end of the String.
    * Calls {@link ConsoleUtils#wprint(java.lang.String, int)}.
    * 
    * @param arg0 the String to print.
    * @param arg1 the the width in characters at which wrapping should occur.
    */
   public void wprint(String arg0, int arg1)
   {
      m_consoleUtils.wprint(arg0, arg1);
   }

   /**
    * Prints a String and then terminates the line.
    * Calls {@link ConsoleUtils#wprintln(java.lang.String)}.
    * 
    * @param arg0 the String to print.
    */
   public void wprintln(String arg0)
   {
      m_consoleUtils.wprintln(arg0);
   }

   /**
    * Prints a String and then terminates the line.  Calls
    * {@link ConsoleUtils#wprintln(java.lang.String, int)}.
    *
    * @param arg0 the String to print.
    * @param arg1 the width in characters at which wrapping should occur.
    */
   public void wprintln(String arg0, int arg1)
   {
      m_consoleUtils.wprintln(arg0, arg1);      
   }

   /**
    * Prints a String and then terminates the line.  Calls
    * {@link ConsoleUtils#wprintlnWithBreaks(java.lang.String)}.
    * 
    * @param arg0 the String to print.
    * 
    * @throws RxIAPreviousRequestException
    */
   public void wprintlnWithBreaks(String arg0) throws
   RxIAPreviousRequestException
   {
      try
      {
         m_consoleUtils.wprintlnWithBreaks(arg0);
      }
      catch (PreviousRequestException e)
      {
         // User has chosen to go to the previous console
         throw new RxIAPreviousRequestException();
      }
   }

   /**
    * Prints a String and then terminates the line.  Calls
    * {@link ConsoleUtils#wprintlnWithBreaks(java.lang.String, java.lang.String,
    * int, int)}.
    * 
    * @param arg0 the String to print.
    * @param arg1 the prompt.
    * @param arg2 the width in characters at which wrapping should occur.
    * @param arg3 the height in lines at which the output should pause and wait
    * for the user.
    * 
    * @throws RxIAPreviousRequestException
    */
   public void wprintlnWithBreaks(String arg0, String arg1, int arg2, int arg3)
   throws RxIAPreviousRequestException
   {
      try
      {
         m_consoleUtils.wprintlnWithBreaks(arg0, arg1, arg2, arg3);
      }
      catch (PreviousRequestException e)
      {
         // User has chosen to go to the previous console 
         throw new RxIAPreviousRequestException();
      }
   }
   
   /**
    * Prints a new line in the console.
    */
   public void newline()
   {
      m_consoleUtils.wprintln(" ");
   }
   
   /**
    * The console utility class provided by InstallAnywhere.  Initialized in
    * ctor, never <code>null</code> after that.
    */
   private ConsoleUtils m_consoleUtils;

   public String promptAndGetSensitiveInformation(String arg0)
      throws RxIAPreviousRequestException
      {
         try
         {
            //Don't mess with this, you have to assign the return variable
            String returnVal = m_consoleUtils.promptAndGetSensitiveInformation(arg0);
            return returnVal;
         }
         catch (PreviousRequestException e)
         {
            // User has chosen to go to the previous console
            throw new RxIAPreviousRequestException();
         }
   }
}
