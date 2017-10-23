# demos-blockchain

__tl;dr__: To run: `sbt run`

Creating a Scala version of a blockchain as described at
<https://hackernoon.com/learn-blockchains-by-building-one-117428612f46>

## API

GET <http://localhost:8080/v1/mine>
GET <http://localhost:8080/v1/chain>
POST <http://localhost:8080/v1/nodes/register>
GET <http://localhost:8080/v1/nodes/resolve>
POST <http://localhost:8080/v1/transactions/new>



## Examples

```
http GET http://localhost:8080/v1/chain
http GET http://localhost:8080/v1/mine
echo '{"nodes": ["http://localhost:8080/v1/chain"]}' | http -j POST 'http://localhost:8080/v1/nodes/register'
echo '{"sender": "my address", "recipient": "someone else\'s address","amount": 5}' | http -j POST 'http://localhost:8080/v1/transactions/new/'
```
