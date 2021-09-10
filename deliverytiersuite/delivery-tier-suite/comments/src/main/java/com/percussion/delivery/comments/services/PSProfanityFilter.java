/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.delivery.comments.services;

import com.percussion.error.PSExceptionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Profanity filter to find if text contains profanity.
 */
public class PSProfanityFilter 
{

    private static final Logger log = LogManager.getLogger(PSProfanityFilter.class);

    /*
     * Comma separated word list of profanity words.
     */
    private File profanityFile = null;
    
    /*
     * List of profanity
     */
    static List<String> profanity;

    
    private static String PROFANITY_FILE_CONF = "/conf/perc/profanity.txt";
    private static String PROFANITY_FILE_WEBAPPS = "/webapps/profanity.txt";
    private static String PROFANITY_FILE_CP = "/profanity.txt";
    
    /***
     * Initializes the Profanity filter from a file.  Will first try to load it from conf/perc.
     * If it is not found, will load it from the old webapps location.  If that is not found,
     * will load it from the class path resource.
     */
    public PSProfanityFilter()
    {
    	
    	profanityFile = new File(System.getProperty("catalina.base") + PROFANITY_FILE_CONF);
        if(!profanityFile.exists()){
        	profanityFile = new File(System.getProperty("catalina.base") + PROFANITY_FILE_WEBAPPS);
        }
        
        setProfanity();
    }
    
    public PSProfanityFilter(File fileSample)
    {
        profanityFile = fileSample;
        setProfanity();
    }
    
    /**
     * Check to see if any of the words in the profanity list exist in given text.
     * Once a match result returns true.  If a match is never found returns false.
     * 
     * @param text - test to check for profanity.
     * 
     * @return true if profanity is found else false
     */
    public boolean containsProfanity(String text)
    {
        for(String word:profanity)
        {
            Pattern p = Pattern.compile("(?i)\\b" + word.toLowerCase().trim() + "\\b");
            Matcher m = p.matcher(text.toLowerCase());
            
            if (m.find())
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Read in profanityFile and create array list of profanity.
     */
    private synchronized void setProfanity()
    {
        String profanityWords = "";
        try
        {
        	if(profanityFile.exists()){
        		log.info("Initializing the Comment Profanity Filter from file: {}",profanityFile.getAbsolutePath());
        		profanityWords = FileUtils.readFileToString(profanityFile, StandardCharsets.UTF_8);
	        }else{
	        	log.info("Initializing the Comment Profanity Filter from default resource file.");        		
	        	profanityWords = new Scanner(PSProfanityFilter.class.getResourceAsStream(PROFANITY_FILE_CP)).useDelimiter("\\A").next();
	        }
        }
        catch (IOException e)
        {
            log.error("Error initializing the Comment Profanity Filter. Error: {}",
                    PSExceptionUtils.getMessageForLog(e));
            log.debug(e);
        }

        PSProfanityFilter.profanity = Arrays.asList(StringUtils.split(profanityWords, ","));
    }
    
}
