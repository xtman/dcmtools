@echo off

pushd %~dp0..\..\
set DCMTOOLS_HOME=%cd%
popd

set LIB=%DCMTOOLS_HOME%\lib
set JRE=%DCMTOOLS_HOME%\jre

if exists %JRE% (
    set JAVA="%JRE%\bin\java"
) else (
    set JAVA=java
)

%JAVA% -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -Xmx200m -cp "%LIB%;%LIB%/*" dcmtools.siemens.cli.CSA2RDACommand %*
