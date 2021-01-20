attrib \%2\release\installshield\projects\*.xml -r /s

java com.percussion.build.ConvertProject \%2\release\installshield\projects\server\server.xml %1 %2

java com.percussion.build.ConvertProject \%2\release\installshield\projects\repositorydb\repositorydb.xml %1 %2

java com.percussion.build.ConvertProject \%2\release\installshield\projects\devtools\devtools.xml %1 %2

java com.percussion.build.ConvertProject \%2\release\installshield\projects\split\xsplitInstaller.xml %1 %2

java com.percussion.build.ConvertProject \%2\release\installshield\projects\publisher\publisher.xml %1 %2

java com.percussion.build.ConvertProject \%2\release\installshield\projects\setup\setup.xml %1 %2

java com.percussion.build.ConvertProject \%2\release\installshield\projects\filetracker\filetracker.xml %1 %2

java com.percussion.build.ConvertProject \%2\release\installshield\projects\uploader\uploader.xml %1 %2

java com.percussion.build.ConvertProject \%2\release\installshield\projects\unixsetup\setup.xml %1 %2

java com.percussion.build.ConvertProject \%2\release\installshield\projects\evalsetup\setup.xml %1 %2

java com.percussion.build.ConvertProject \%2\release\installshield\projects\xroads\xroads.xml %1 %2

