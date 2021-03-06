FROM openjdk:8

ENV SBT_VERSION 0.13.15

RUN \
  curl -L -o sbt-$SBT_VERSION.deb http://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install sbt && \
  sbt sbtVersion

WORKDIR /SlotbookTelegramBot

COPY . /SlotbookTelegramBot

CMD SBT_OPTS="-Xms512M -Xmx912M -Xss2M -XX:MaxMetaspaceSize=912M -XX:MaxPermSize=256M -Dconfig.file=src/main/resources/application.conf" sbt run
