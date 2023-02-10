/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        }

        PSProfanityFilter.profanity = Arrays.asList(StringUtils.split(profanityWords, ","));
    }
    
}
