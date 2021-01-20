# Contributing

:clap: :metal: :heart: :clap: First, thanks for your interest in contributing to our project! :clap: :metal: :heart: :clap:

The following is a set of guidelines for contributing to Percussion CMS and its packages, which are hosted in the Percussion Software organization on GitHub. These are mostly guidelines, not rules. Use your best judgment, and feel free to propose changes to this document in a pull request.

## Code of Conduct

This project, and everyone participating in it, is governed by the [Percussion CMS - Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.

## I just have a quick question...

Questions can be posted on the [Community](https://community.percussion.com) discussion board.

## Getting Started

The Percussion CMS is a large open source project made up of a number of modules and third party components.  When you first start exploring contributing to the project, it can be difficult to identify wich module or component contains the functionality that you want to change or file a bug for.

### Key Modules
The table below outlines the most commonly used modules.

| Module        | Path | Description |
| ------------- | ------------ | --------|
| CMLite-Main | system | This is the CMS core module.|
| CMLite-WebUI | WebUI | This contains the primary user interface. |
| sitemanage | projects/sitemanage | This contains the backend for the primary user interface |
| rest | rest | This contains the public REST API implementation |
| cui | cui | This contains the Home screen UI implementation |
| perc-common-ui | delivery | This contains the JavaScript code for dynamic widgets used with the DTS services. |
| delivery-tier-suite (DTS) | deliverytiersuite/delivery-tier-suite | This contains the dynamic DTS service modules. |
| perc-distribution-tree | modules/perc-distribution-tree | This project contains the final installable CMS distribution |
| delivery-tier-distribution | deliverytiersuite/delivery-tier-suite/delivery-tier-distribution | This project contains the installable DTS distribution |

There are many more modules, but the ones listed above are the big ones.

## How Can I Contribute?

### Reporting Bugs

This section guides you through submitting a bug report for Percussion CMS. Following these guidelines helps maintainers and the community understand your report :pencil:, reproduce the behavior :computer: :computer:, and find related reports :mag_right:.

Before creating bug reports, please check [this list](#before-submitting-a-bug-report) as you might find out that you don't need to create one. When you are creating a bug report, please [include as many details as possible](#how-do-i-submit-a-good-bug-report). Fill out [the required template](https://github.com/percussion/percussioncms/.github/blob/master/.github/ISSUE_TEMPLATE/bug_report.md), the information it asks for helps us resolve issues faster.

> **Note:** If you find a **Closed** issue that seems like it is the same thing that you're experiencing, open a new issue and include a link to the original issue in the body of your new one.
>
#### Before Submitting A Bug Report
* **Perform a [cursory search](https://github.com/search?q=+is%3Aissue+user%3Apercussion)** to see if the problem has already been reported. If it has **and the issue is still open**, add a comment to the existing issue instead of opening a new one.

#### How Do I Submit A (Good) Bug Report?

Bugs are tracked as [GitHub issues](https://guides.github.com/features/issues/). Create an issue in the project repository and provide the following information by filling in [the template](https://github.com/percussion/percussion/.github/blob/master/.github/ISSUE_TEMPLATE/bug_report.md).

Explain the problem and include additional details to help maintainers reproduce the problem:

* **Use a clear and descriptive title** for the issue to identify the problem.
* **Describe the exact steps which reproduce the problem** in as many details as possible. For example, you logged in to the Spanish locale, clicked the Dashboard, clicked the Bulk Upload Gadget, selected a folder (/Assets/uploads/myfolder) etc. When listing steps, **don't just say what you did, but explain how you did it**. For example, if you moved the cursor to the end of a line, explain if you used the mouse, or a keyboard shortcut, and if so which one?
* **Provide specific examples to demonstrate the steps**. Include links to files or GitHub projects, or copy/pasteable snippets, which you use in those examples. If you're providing snippets in the issue, use [Markdown code blocks](https://help.github.com/articles/markdown-basics/#multiple-lines).
* **Describe the behavior you observed after following the steps** and point out what exactly is the problem with that behavior.
* **Explain which behavior you expected to see instead and why.**
* **Include screenshots and animated GIFs** which show you following the described steps and clearly demonstrate the problem.  You can use [this tool](https://www.cockos.com/licecap/) to record GIFs on macOS and Windows, and [this tool](https://github.com/colinkeenan/silentcast).
* **If the problem wasn't triggered by a specific action**, describe what you were doing before the problem happened and share more information using the guidelines below.
* **If the problem involved a server error**, attach the ```<InstallDir>/jetty/base/logs/server.log``` to the issue.
* **If there was an error in the Browser JavaScript console** attach screenshots of the console and any failing requests.

Provide more context by answering these questions:

* **Did the problem start happening recently** (e.g. after updating to a new version of Percussion CMS) or was this always a problem?
* If the problem started happening recently, **can you reproduce the problem in an older version of Percussion CMS ?** What's the most recent version in which the problem doesn't happen? You can download older versions of Percussion CMS from [the releases page](https://github.com/percussion/percussioncms/releases).
* **Can you reliably reproduce the issue?** If not, provide details about how often the problem happens and under which conditions it normally happens.
* If the problem is related to working with Pages / Items / Assets (e.g. opening and editing Pages), **does the problem happen for all Pages or only some?**

Include details about your configuration and environment:

* **Which version of Percussion CMS are you using?** You can get the exact version by viewing the About box in the UI or from view ```<InstallDir>/Version.properties``` in a terminal.
* **What's the name and version of the OS you're using for the server and for the client**?
* **Are you running Percussion CMS in a virtual machine?** If so, which VM software are you using and which operating systems and versions are used for the host and the guest?
* **Which browser are you using and which version of the browser?** Are you able to reproduce the problem in other web browsers (Edge, Chrome, Firefox, Safari, etc)

### Suggesting Enhancements

This section guides you through submitting an enhancement suggestion for Percussion CMS, including completely new features and minor improvements to existing functionality. Following these guidelines helps maintainers and the community understand your suggestion :pencil: and find related suggestions :mag_right:.

Before creating enhancement suggestions, please check [this list](#before-submitting-an-enhancement-suggestion) as you might find out that you don't need to create one. When you are creating an enhancement suggestion, please [include as many details as possible](#how-do-i-submit-a-good-enhancement-suggestion). Fill in [the template](https://github.com/percussion/percussioncms/.github/blob/master/.github/ISSUE_TEMPLATE/feature_request.md), including the steps that you imagine you would take if the feature you're requesting existed.

#### Before Submitting An Enhancement Suggestion

* **Perform a [cursory search](https://github.com/search?q=+is%3Aissue+user%3Apercussion)** to see if the enhancement has already been suggested. If it has, add a comment to the existing issue instead of opening a new one.

#### How Do I Submit A (Good) Enhancement Suggestion?

Enhancement suggestions are tracked as [GitHub issues](https://guides.github.com/features/issues/). Create an issue on that repository and provide the following information:

* **Use a clear and descriptive title** for the issue to identify the suggestion.
* **Provide a step-by-step description of the suggested enhancement** in as many details as possible.
* **Provide specific examples to demonstrate the steps**. Include copy/pasteable snippets which you use in those examples, as [Markdown code blocks](https://help.github.com/articles/markdown-basics/#multiple-lines).
* **Describe the current behavior** and **explain which behavior you expected to see instead** and why.
* **Include screenshots and animated GIFs** which help you demonstrate the steps or point out the part of Percussion CMS which the suggestion is related to. You can use [this tool](https://www.cockos.com/licecap/) to record GIFs on macOS and Windows, and [this tool](https://github.com/colinkeenan/silentcast).
* **Explain why this enhancement would be useful** to most Percussion CMS users and isn't something that can or should be implemented as a custom extension / widget.
* **List some other content management applications where this enhancement exists.**
* **Specify which version of Percussion CMS you're using.**  You can get the exact version by viewing the About box in the UI or from view ```<InstallDir>/Version.properties``` in a terminal.
* **Specify the name and version of the OS you're using.**

### Reporting Security Issues / Vulnerabilities

If you feel that you have discovered a security issue or vulnerability in any of the project modules, **please do not log a normal GitHub issue**.  Follow the process defined in the project [Security Policy](https://github.com/percussion/percussioncms/blob/development/SECURITY.md)


### Great, how do I get started working on Percussion CMS code?

## Developer Setup

```
git clone https://github.com/percussion/percussioncms.git
cd percussioncms
git fetch origin
git pull
```

## Pre-Requisites for Building
- Apache Maven > 3.6
- Java 1.8 OpenJDK - Amazon Corretto is the current default https://aws.amazon.com/corretto/
- [home directory]/.m2/toolchains.xml file configured to point at the Corretto JDK.

````
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>8</version>
      <vendor>amazon</vendor>
    </provides>
    <configuration>
      <jdkHome>/Library/Java/JavaVirtualMachines/amazon-corretto-8.jdk/Contents/Home</jdkHome>
    </configuration>
  </toolchain>
````

## Building

```
mvn clean install
```
## Working on the Code

Before you start working on a bug or feature, discuss the issue in GitHub Issues or on the percussion community.  This is just to make sure you don't duplicate efforts or waste your valuable time on something that is already underway by someone else.

When starting work, create a new feature branch.

IntelliJ makes some of this a lot easier, especially when searching for specific error messages across modules in the code base.

The examples below show the typical command line for create a feature branch and pull request.

For example:

```
git checkout -b CMS-7209
```

As you make changes for the bug commit them to your local using:

```
git commit  -m 'Validate entry on x form'
```

If you add new files, track them with:

```
git add <filename>
```

When you are done with the work, and feeling good about the tests passing and code is working and committed to your local branch.
Push the branch to the origin.

```
git push --set-upstream origin CMS-7209
```
From there, goto http://github.com/percussion/percussioncms/

Select the branch you just pushed and request a pull request adding any comments and requesting a review from at least 1 other maintainer.  Once the pull request has been merged, you can then delete the branch on your local.

```
git branch -d CMS-7209
```
After this, re-sync your local development branch to get your changes and changes posted by others:

```
git checkout development
git pull
```
Pick the next issue to work on and repeat the process.

## Performing a Development Install

From the project directory, a local development install can be performed using the following commands after a full build has been completed.

### Percussion CMS
```
 java -jar modules/perc-distribution-tree/target/perc-distribution-tree-8.0.0-SNAPSHOT.jar <Install Directory>
```
### Percussion DTS
```
java -jar deliverytiersuite/delivery-tier-suite/delivery-tier-distribution/target/delivery-tier-distribution-8.0.0-SNAPSHOT.jar <Install Directory>
```
### Starting Percussion CMS

After running the installation, either a symbolic link, or a copy of a 1.8 JRE needs placed in the ```<Install Directory>/JRE``` folder.

For example, on OSX:

```
cd <Install Directory>
ln -s /Library/Java/JavaVirtualMachines/amazon-corretto-8.jdk/Contents/Home/jre JRE
```

Once the JRE is symlinked - you can then start a local instance of the CMS by running:

```
cd <Install Directory>/jetty
chmod +x *.sh
./StartJetty.sh
```

The developer instance can be terminated by using CTRL-C from the terminal.

### Debugging Percussion CMS

Debugging can be enabled for the CMS by editing:
```
<InstallDir>/jetty/base/start.d/jvm.ini
```
file and adding the Java debug flags:
```
# ---------------------------------------
# Module: jvm
# A noop module that creates an ini template useful for
# setting JVM arguments (eg -Xmx )
# ---------------------------------------
--module=jvm

## JVM Configuration
## If JVM args are include in an ini file then --exec is needed
## to start a new JVM from start.jar with the extra args.
##
## If you wish to avoid an extra JVM running, place JVM args
## on the normal command line and do not use --exec
--exec
-XX:+DisableAttachMechanism
-server
-Xms512m
-Xmx4096m
-XX:+HeapDumpOnOutOfMemoryError
-noverify
-XX:+UseStringDeduplication
-Dorg.eclipse.jetty.annotations.AnnotationParser.LEVEL=OFF
-Dfile.encoding=UTF8
-Dsun.jnu.encoding=UTF8
-Dhttps.protocols=TLSv1.2
-Djava.net.preferIPv4Stack=true
-Djava.net.preferIPv4Addresses=true
-Drxdeploydir=/Users/natechadwick/cm1540618
-Djetty_perc_defaults=/Users/natechadwick/cm1540618/jetty/defaults
-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8050

-XX:+DisableExplicitGC
-XX:+UseConcMarkSweepGC
-XX:NewSize=500m
-XX:SurvivorRatio=16
```

The above config enables the Java debug port of 8050.  Any Java IDE or debug tool should be able to connect to the server and remote debug on port 8050.  This port number can be changed based on your configuration.

The line that adds the debug support is:

-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8050

Note that suspend can be set to y or n.  When it is set to y, the CMS will pause until a debugger is attached when started, wich is useful for debugging startup issues.  If you are not debugging a startup issue you would set that to n.

Also note that the heap size (memory allocated at runtime), is also controlled by this file. We are allocating as much as 4096m of RAM by default for the CMS. The memory setting can be changed based on your system configuration to allow more or less.


### Logging

Most activity should be logged to the server log and to the running CMS console in the terminal.  You can tweak the logging level and what gets logged by modifying the ```<InstallDir>/jetty/base/resources/log4j2.xml``` file.

For example:

 ```
 <?xml version="1.0" encoding="UTF-8"?>
<Configuration status="warn" monitorInterval="30">
    <Appenders>

        <RollingFile name="FILE" fileName="${log4j:configParentLocation}/../logs/server.log"
                     filePattern="${log4j:configParentLocation}/../logs/server-%d{yyyy-MM-dd}-%i.log">
            <PatternLayout header="${env:rxDir} ${env:deployerdir} ${java:runtime} - ${java:vm} - ${java:os}" pattern="%d %-5p [%c] %m%n"/>
            <Policies>
                <SizeBasedTriggeringPolicy size="10 MB" />
            </Policies>
            <DefaultRolloverStrategy max="10"/>
        </RollingFile>

        <RollingFile name="RXGLOBALTEMPLATES" fileName="${log4j:configParentLocation}/../logs/globaltemplate.log"
                     filePattern="${log4j:configParentLocation}/../logs/globaltemplate-%d{yyyy-MM-dd}-%i.log">
            <PatternLayout>
                <Pattern>%d %p %c{1.} [%t] %m%n</Pattern>
            </PatternLayout>
            <Policies>
                <SizeBasedTriggeringPolicy size="10 MB" />
            </Policies>
            <DefaultRolloverStrategy max="10"/>
        </RollingFile>

        <RollingFile name="VELOCITY" fileName="${log4j:configParentLocation}/../logs/velocity.log"
                     filePattern="${log4j:configParentLocation}/../logs/velocity-%d{yyyy-MM-dd}-%i.log">
            <PatternLayout>
                <Pattern>%d %p %c{1.} [%t] %m%n</Pattern>
            </PatternLayout>
            <Policies>
                <SizeBasedTriggeringPolicy size="10 MB" />
            </Policies>
            <DefaultRolloverStrategy max="10"/>
        </RollingFile>

        <RollingFile name="RevisionPurgeApp" fileName="${log4j:configParentLocation}/../logs/revisionPurge.log"
                     filePattern="${log4j:configParentLocation}/../logs/revisionPurge-%d{yyyy-MM-dd}-%i.log">
            <PatternLayout>
                <Pattern>%d %p %c{1.} [%t] %m%n</Pattern>
            </PatternLayout>
            <Policies>
                <SizeBasedTriggeringPolicy size="10 MB" />
            </Policies>
            <DefaultRolloverStrategy max="10"/>
        </RollingFile>

        <Console name="CONSOLE" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{ABSOLUTE} %-5p [%c{1}] %m%n"/>
        </Console>

    </Appenders>

    <Loggers>
        <AsyncRoot level="info" includeLocation="true" additivity="false">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="FILE"/>
        </AsyncRoot>

        <!-- =========================== -->
        <!-- Setup the Percussion loggers -->
        <!-- =========================== -->

        <!-- Percussion turn off excessive logger from betwixt -->
        <AsyncLogger name="org.apache.commons.betwixt.io.BeanReader" level="error" includeLocation="true" additivity="false">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="FILE"/>
        </AsyncLogger>
        <AsyncLogger name="org.apache.commons.betwixt.digester.ElementRule" level="error" includeLocation="true" additivity="false">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="FILE"/>
        </AsyncLogger>

        <!-- Turn off excessive logging from PDF Box -->
        <AsyncLogger name="org.apache.pdfbox" level="fatal" includeLocation="true" additivity="false">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="FILE"/>
        </AsyncLogger>

        <AsyncLogger name="org.apache.pdfbox.util.PDFStreamEngine" level="off" includeLocation="true" additivity="false">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="FILE"/>
        </AsyncLogger>

        <AsyncLogger name="org.apache.pdfbox.pdmodel.font.PDSimpleFont" level="off" includeLocation="true" additivity="false">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="FILE"/>
        </AsyncLogger>

        <AsyncLogger name="org.apache.pdfbox.pdmodel.font.PDFont" level="off" includeLocation="true" additivity="false">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="FILE"/>
        </AsyncLogger>

        <AsyncLogger name="org.apache.pdfbox.pdmodel.font.FontManager" level="off" includeLocation="true" additivity="false">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="FILE"/>
        </AsyncLogger>

        <AsyncLogger name="org.apache.pdfbox.pdfparser.PDFObjectStreamParser" level="off" includeLocation="true" additivity="false">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="FILE"/>
        </AsyncLogger>

        <AsyncLogger name="org.apache.pdfbox.pdmodel.graphics.xobject.PDPixelMap" level="off" includeLocation="true" additivity="false">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="FILE"/>
        </AsyncLogger>

        <AsyncLogger name="org.hibernate" level="error" includeLocation="true" additivity="false">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="FILE"/>
        </AsyncLogger>

        <AsyncLogger name="org.springframework" level="warn" includeLocation="true" additivity="false">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="FILE"/>
        </AsyncLogger>

        <AsyncLogger name="XmlUtil" level="error" includeLocation="true" additivity="false">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="FILE"/>
        </AsyncLogger>

        <AsyncLogger name="net.htmlparser.jericho" level="off" includeLocation="true" additivity="false">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="FILE"/>
        </AsyncLogger>

        <AsyncLogger name="org.apache.cxf" level="error" includeLocation="true" additivity="false">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="FILE"/>
        </AsyncLogger>

        <AsyncLogger name="org.eclipse.jetty.annotations.AnnotationParser" level="OFF" includeLocation="true" additivity="false">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="FILE"/>
        </AsyncLogger>

        <AsyncLogger name="com.percussion" level="info" includeLocation="true" additivity="false">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="FILE"/>
        </AsyncLogger>

        <!-- AsyncLogger config to be used by global template creation process -->
        <AsyncLogger name="com.percussion.globaltemplates" level="info" includeLocation="true" additivity="false">
            <AppenderRef ref="RXGLOBALTEMPLATES"/>
        </AsyncLogger>

        <!-- Remove info about basic auth -->
        <AsyncLogger name="org.apache.commons.httpclient.auth.AuthChallengeProcessor" level="error" includeLocation="true" additivity="false">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="FILE"/>
        </AsyncLogger>

        <!-- Change to INFO to see Metadata extraction errors -->
        <AsyncLogger name="org.deri.any23.extractor.SingleDocumentExtraction" level="error" includeLocation="true" additivity="false">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="FILE"/>
        </AsyncLogger>

        <AsyncLogger name="org.apache.velocity" level="info" includeLocation="true" additivity="false">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="VELOCITY"/>
        </AsyncLogger>

        <AsyncLogger name="com.percussion.services.assembly.impl.plugin.PSVelocityAssembler" level="DEBUG" includeLocation="true" additivity="false">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="VELOCITY"/>
        </AsyncLogger>

        <AsyncLogger name="RevisionPurge" level="info" includeLocation="true" additivity="false">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="RevisionPurgeApp"/>
        </AsyncLogger>

       <AsyncLogger name="org.apache.shindig" level="DEBUG" includeLocation="true">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="FILE"/>
        </AsyncLogger>
       <AsyncLogger name="com.percussion.delivery" level="DEBUG" includeLocation="true">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="FILE"/>
	</AsyncLogger>
  	<AsyncLogger name="org.apache.any23" level="DEBUG" includeLocation="true">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="FILE"/>
        </AsyncLogger>

    </Loggers>
</Configuration>
 
  ```
The log4j2 snippet above enables debug logging for a couple of Third Party components as well as for the PSVelocityAssebler class wich is responsible for rendering Velocity templates.

The code base currently uses multiple logging frameworks - most of wich are funneled through Log4J2.  There are cases where a developer may have not used a framework and instead do something like:

```
<snip>
catch(Exception e){
    e.printStackTrace();
}
</snip>
```

Those types of errors will get written to SDTERR or STDOUT and will either only show up on the console/terminal when run interactively or in the jetty/base/logs/<year>-<day>-<month>-jetty.log files via the jetty capture module.

If you see code like above, it should be refactored to use a Log4j2 logger.

### Pull Request Review / Approval

All pull requests are subject to a number of automated checks, as well as manual review by at least one core maintainer. The check / review process is intended to ensure quality of changes and consistency of the system.

Things that will help with Pull Request approvals:

* Create / update unit tests for the code that you are changing / adding
* Comment the code
* Use i18n for messages.
> **Note:** TODO:  Add link to i18n info!

### Contributor Agreement

Before your pull requests can be processed or reviewed, you first must sign a Contributor Agreement linked tour GitHub account.

We are using [CLA Assistant] (https://cla-assistant.io/) to track these agreements.

> **Note:** TODO: Add link to agreement

The Contributor Agreement covers assignment of copyright for your contribution to Percussion Software, the author of Percussion CMS, with terms to license the
change back to you under a Free Software Foundation or OSI approved open source license.  Percussion CMS is currently licensed using the AGPL v3 license.

Once you have executed the Contributor Agreement and the agreement has been approved by a project Admin, the CLA check will clear and your pull request will be
ready for review.

### Git Hints and Tricks

* git config --global core.autocrlf true


### Acknowledgements
* The [Atom project](https://www.github.com/atom) provided a good guideline for this document.  Thanks!

:v:
