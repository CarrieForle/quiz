FROM eclipse-temurin:21-jdk
COPY . /quiz
WORKDIR /quiz
RUN javac -d out quiz/Server.java
CMD java -cp out quiz/Server