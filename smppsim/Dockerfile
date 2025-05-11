FROM eclipse-temurin:23-jre
WORKDIR /opt/smppsim
COPY . .
EXPOSE 2775 88
CMD ["java", "-jar", "smppsim.jar", "conf/smppsim.props"]