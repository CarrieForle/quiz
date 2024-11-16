@echo off
javac -d out Server.java

IF %ERRORLEVEL% EQU 0 (
    java -cp out Server
)