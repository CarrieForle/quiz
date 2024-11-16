javac -d out Server.java

if ($?) {
    java -cp out Server
}