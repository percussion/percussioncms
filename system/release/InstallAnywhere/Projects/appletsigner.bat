%1
cd "%1\%2\release\InstallShield\Projects"
jarsigner "%1\%2\ContentExplorer\lib\rxcx.jar" cxcert
jarsigner -verify -certs "%1\%2\ContentExplorer\lib\rxcx.jar"