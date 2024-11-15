@echo off
javac -d out Program.java

IF %ERRORLEVEL% EQU 0 (
    java -cp out Program
)