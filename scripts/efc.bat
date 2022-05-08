@echo off

where java 2> nul > nul
if not %errorlevel% == 0 ( 
    echo Error: JRE not found
    echo Java Runtime Environment is required to execute the MCFC.
    echo Please install the Java Runtime Environment of your platform.
) else (
    java -Xmx20M -jar %~dp0/efc.jar %*
)
