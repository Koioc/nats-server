# Etapa 1: Compilação do código usando Maven
FROM maven AS build

# Define o diretório de trabalho para a compilação
WORKDIR /app

# Copia o arquivo pom.xml e baixa as dependências
COPY pom.xml .

# Copia o código-fonte da aplicação para o container
COPY src ./src

# Compila o projeto e gera o JAR final
RUN mvn clean install -DskipTests

# Etapa 2: Criação da imagem final para rodar o JAR
FROM openjdk:21-jdk

# Define o diretório de trabalho
WORKDIR /app

# Copia o JAR gerado na fase anterior para a imagem final
COPY --from=build /app/target/nats-api-0.0.1-SNAPSHOT.jar /app/nats-api.jar

# Exponha a porta 300 da aplicação
EXPOSE 8080

# Variável de ambiente para o endereço do servidor NATS
ENV NATS_URL=nats://0.0.0.0:4222

# Comando para rodar a aplicação
CMD ["java", "-jar", "nats-api.jar"]
