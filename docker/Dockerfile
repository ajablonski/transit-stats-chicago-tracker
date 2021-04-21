FROM adoptopenjdk:15-jre-hotspot

RUN mkdir /app \
    && apt-get update \
    && apt-get install unzip
COPY transit-stats-chicago.zip /app
COPY entrypoint.sh /app
WORKDIR /app
RUN unzip transit-stats-chicago.zip \
    && rm transit-stats-chicago.zip

ENTRYPOINT ["/app/entrypoint.sh"]