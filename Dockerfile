FROM maven:3-jdk-11 as builder

ENV OTP_ROOT="/opt/opentripplanner"
WORKDIR ${OTP_ROOT}/

RUN cd /opt

RUN git clone -b gtfs-flex-2 https://github.com/kyyticom/onebusaway-gtfs-modules \
      && cd onebusaway-gtfs-modules \
      && mvn install || :

run cd ${OTP_ROOT}

COPY pom.xml ${OTP_ROOT}/
RUN mvn dependency:go-offline

COPY . ${OTP_ROOT}/

ENV MAVEN_OPTS="-Xmx4000M"

# Build OTP
RUN mvn package

FROM openjdk:11

ENV OTP_ROOT="/opt/opentripplanner"
WORKDIR ${OTP_ROOT}/

COPY --from=builder /opt/opentripplanner/target/*-shaded.jar ./otp-shaded.jar

ENV JAVA_OPTS="-Xms8G -Xmx8G"
ENV PORT=8080
EXPOSE ${PORT}

CMD ["sh", "-c", "java ${JAVA_OPTS} -jar otp-shaded.jar --load ${OTP_ROOT}/graph"]