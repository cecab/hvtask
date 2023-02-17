FROM eclipse-temurin:11.0.12_7-jdk
COPY ./build/libs/hvtask-0.1-all.jar /app/
ENTRYPOINT ["java"]
CMD ["-jar", "/app/hvtask-0.1-all.jar"]
