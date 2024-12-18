@echo off
javac -d out quiz/Client.java

IF %ERRORLEVEL% EQU 0 (
    java -cp "out;." quiz/Client
)