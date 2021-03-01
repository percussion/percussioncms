del "%1\%2\uploader\src\com\percussion\uploader\xmlloader.properties"
cp "%1\%2\uploader\src\com\percussion\uploader\rxuploader.properties" "%1\%2\uploader\src\com\percussion\uploader\xmlloader.properties"

ismp %1\%2\release\installshield\projects\uploader\uploader.xml -build -is:log log.txt