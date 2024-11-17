javac -d out Client.java

if ($?) {
    java -cp out Client
}