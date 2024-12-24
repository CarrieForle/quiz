@echo off
javac -d out quiz/Server.java

IF %ERRORLEVEL% EQU 0 (
    java -cp "out;." quiz/Server %1 %2 %3 %4 %5
)