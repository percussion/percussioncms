package com.intsof.support.migrations.zohodesk;

/*
Copyright 2023 Intersoft Data Labs, Inc.

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/

import org.apache.commons.cli.*;

/**
 * The main entry point for the migration tool.
 */
public class App {


    private static Options initOptions(){
        Option help = new Option("help", "print this message");
        Option version = new Option("version", "print the version information and exit");
        Option debug = new Option("debug", "print debugging information");
        Option logfile   = Option.builder("logfile")
                .argName("file")
                .hasArg()
                .desc("use given file for log")
                .build();
        Option dropFirst = new Option("drop", "drop existing data instead of updating it");
        Option dryRun = new Option("dryrun", "will not import any data, just simulate it");
        Option rate = new Option("rate", "Set the number of operations allowed per second");
        Option refresh = new Option("refresh", "Only pull data that has changed since last run");

        Options options = new Options();
        options.addOption(help);
        options.addOption(version);
        options.addOption(debug);
        options.addOption(logfile);
        options.addOption(dropFirst);
        options.addOption(dryRun);
        options.addOption(rate);
        options.addOption(refresh);

        return options;
    }

    public static void main(String[] args){

        CommandLineParser parser = new DefaultParser();
        try {
            Options options = initOptions();

            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            if(args.length<2 || line.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("zohodesk-migration", options);
            }

        }
        catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
        }

    }

}
