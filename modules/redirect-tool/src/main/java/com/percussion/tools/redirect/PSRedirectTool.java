/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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
        options.addOption("n", "nginx", false, "generate redirects in nginx formay");
        options.addOption("s3", false, "generate redirects in S3 json redirect policy format");
        options.addOption("csv", true, "input redirect manager CSV file");
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
               int count = cvt.convertRedirects(list, Paths.get("").toAbsolutePath().toString());
                System.out.println("Wrote " +  count + " redirects to " + Paths.get("").toAbsolutePath() + File.separator + cvt.getFilename());
                return;
            }

            if(line.hasOption("a")){
                PSApacheRedirectConverter cvt = new PSApacheRedirectConverter();
                PSPercussionRedirectEntryList list = new PSPercussionRedirectEntryList(csv);

                int count = cvt.convertRedirects(list, Paths.get("").toAbsolutePath().toString());
                System.out.println("Wrote " +  count + " redirects to " + Paths.get("").toAbsolutePath() + File.separator + cvt.getFilename());

            }

        }
        catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
        }
    }

}
