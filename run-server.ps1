javac -d out quiz/Server.java

if ($?) {
    java -cp 'out;.' quiz/Server
}