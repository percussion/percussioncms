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

package com.percussion.tools.redirect;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.nio.file.Paths;

public class PSRedirectTool {

    public static void main(String[] args)  {
        Options options = new Options();
        options.addOption("h", "help", false, "show usage and options");
        options.addOption("a", "apache", false, "generate redirects in .htaccess format");
        options.addOption("i", "iis", false, "generate redirects in web.config format");
        options.addOption("j", "json", false, "generate redirects in json format");
        options.addOption("n", "nginx", false, "generate redirects in nginx formay");
        options.addOption("s3", false, "generate redirects in S3 json redirect policy format");
        options.addOption("csv", true, "input redirect manager CSV file");
        options.addOption("z", true, "Add absolute url to targets");
        options.addOption("o", false, "the directory to write converted redirects to");

        // create the parser
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            if(line.hasOption("h") || line.getOptions().length==0){
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("perc-redirect-tool", options);
                return;
            }
            String csv = "";

            if(line.hasOption("csv")){
                csv = line.getParsedOptionValue("csv").toString();
                if(csv == null || "".equals(csv.trim())){
                    System.err.println("Error: -csv parameter is required.");
                    return;
                }
            }
            if(line.hasOption("i") || line.hasOption("iis")){
                PSIISRedirectConverter cvt = new PSIISRedirectConverter();
                PSPercussionRedirectEntryList list = new PSPercussionRedirectEntryList(csv);
               int count = cvt.convertRedirects(list, Paths.get("").toAbsolutePath().toString(),null,null,null);
                System.out.println("Wrote " +  count + " redirects to " + Paths.get("").toAbsolutePath() + File.separator + cvt.getFilename());
                return;
            }

            if(line.hasOption("a")){
                PSApacheRedirectConverter cvt = new PSApacheRedirectConverter();
                if(line.hasOption("z")) {
                    cvt.setAbsolutePrefix(line.getOptionValue("z"));
                }

                PSPercussionRedirectEntryList list = new PSPercussionRedirectEntryList(csv);

                int count = cvt.convertRedirects(list, Paths.get("").toAbsolutePath().toString(),null,null,null);
                System.out.println("Wrote " +  count + " redirects to " + Paths.get("").toAbsolutePath() + File.separator + cvt.getFilename());

            }

            if(line.hasOption("j")) {
                PSJSONRedirectConverter cvt = new PSJSONRedirectConverter();
                PSPercussionRedirectEntryList list = new PSPercussionRedirectEntryList(csv);

                int count = cvt.convertRedirects(list, Paths.get("").toAbsolutePath().toString(),"{","}",",");
                System.out.println("Wrote " + count + " redirects to " + Paths.get("").toAbsolutePath() + File.separator + cvt.getFilename());
            }


                if(line.hasOption("n")){
                PSNginxRedirectConverter cvt = new PSNginxRedirectConverter();
                PSPercussionRedirectEntryList list = new PSPercussionRedirectEntryList(csv);

                int count = cvt.convertRedirects(list, Paths.get("").toAbsolutePath().toString(),null,null,null);
                System.out.println("Wrote " +  count + " redirects to " + Paths.get("").toAbsolutePath() + File.separator + cvt.getFilename());

            }

        }
        catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
        }
    }

}
