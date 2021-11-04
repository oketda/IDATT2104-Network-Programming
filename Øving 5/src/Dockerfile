FROM gcc:9.3.0
RUN mkdir /src
WORKDIR /src
COPY main.cpp /src/
RUN g++ -o main main.cpp
CMD ["./main"]
