FROM eclipse-temurin:21-jdk
COPY . /quiz
WORKDIR /quiz
RUN javac -d out quiz/Server.java
ENTRYPOINT [ "java", "-cp", "out:.", "quiz/Server" ]
CMD 2 4
