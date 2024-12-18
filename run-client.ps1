javac -d out quiz/Client.java

if ($?) {
    java -cp 'out;.' quiz/Client
}