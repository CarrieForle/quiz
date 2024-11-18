@echo off
javac -d out quiz/Server.java

IF %ERRORLEVEL% EQU 0 (
    java -cp out quiz/Server
)