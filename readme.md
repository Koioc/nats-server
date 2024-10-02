Criar aplicação:
mvn clean install -DskipTests

Rodar o Java:
java -jar target/nats-api-0.0.1-SNAPSHOT.jar

Subindo o servidor:
docker run --name nats --rm -p 4222:4222 -p 8222:8222 nats --jetstream --server_name nats-server --http_port 8222

Se inscrever na fila, usar o nats
./nats subscribe ">" -s 0.0.0.0:4222