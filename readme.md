# Project Setup

## Build Application
```sh
docker build -t nats-server .
```

## Run Application:
```sh
docker run nats-server
```

## Start Local Nats Server
```sh
docker run --name nats --rm -p 4222:4222 -p 8222:8222 nats --jetstream --server_name nats-server --http_port 8222
```

## Subscribe to Queue
```sh
./nats subscribe ">" -s 0.0.0.0:4222
```
