@echo off
javac -d out Client.java

IF %ERRORLEVEL% EQU 0 (
    java -cp out Client
)