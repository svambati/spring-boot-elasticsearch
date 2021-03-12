# spring-boot-elasticsearch


### Elastic Search Cluster setup:

##### Ths command cleans up previous docker container that were stopped already.
```
docker rm $(docker ps -a -q)

```

##### This is to run elasticsearch container version 7.3.2
```
docker run -p 9200:9200 --name elasticsearch -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:7.3.2
```

Executing this command will run one instances of elastic search. We can configure running n number instances with this command too


There is postman collection attached to this folder and this can used for initial sample set to load data to elastic search.
You can import this collection to postman and execute it. Make sure `Id` is different for every insert into index.


