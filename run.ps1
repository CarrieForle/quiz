javac -d out Program.java

if ($?) {
    java -cp out Program
}