# Pricer

This is the Pricer application that determines the price for a product based on ordered quantity and configured discount.

All requirements are [here](REQUIREMENTS.pdf).
See also [Technical Overview](TECH_OVERVIEW.md)

## Building

This app is written in java 21, so to build it, make sure you have a proper [JDK](https://www.oracle.com/pl/java/technologies/downloads/) and `JAVA_HOME` property is set.

To build, run
```shell
./gradlew build
```

Gradle will run tests and build this app. The artifact should be [here](build/libs).

### Docker
There is also possibility to prepare a docker image & container. To do so, run:
```shell
docker build -t pricer .
```

## Running
To run this application, you need two things:
- JVM or Docker (when build using docker)
- running MongoDB on port 27017 (Pricer uses it as a database)

If you need to change the MongoDB host or port, please change its value in [properties file](src/main/resources/application.properties) and then rebuild the app. 

To start this app using JVM, run:
```shell
java -jar build/libs/pricer-0.0.1-SNAPSHOT.jar
```

It can be also run using Docker image:
```shell
docker run -p 8080:8080 --network host pricer
```

## Usage
Documentation of this App will be accessible under http://localhost:8080/swagger-ui/index.html

The simplest scenario:
- create a new price list using POST http://localhost:8080/prices endpoint with body:
```json
{
  "name": "example product",
  "basePrice": 200,
  "discountType": "AMOUNT_BASED",
  "discounts": {
    "2" : 10,
    "10" : 30
  }
}
```
where:
- "name" is simply the name of a given product
- "basePrice" is a product's base price (without discounts). 
- "discountType" is a type of discounts for this product. Possible values are AMOUNT_BASED and PERCENTAGE_BASED. 
- "discounts" is a map of ordered quantities of this product to its discounts. For the example above for:
  - 1 piece of the product the price will be the same as basePrice
  - 2 pieces - the price will be basePrice - 10 (200 - 10 = 190)
  - 5 pieces - the same as above
  - 10 pieces (or more) - the price will be basePrice - 30 (200 - 30 = 170)
- For "discountType": "PERCENTAGE_BASED" and the same "discounts":
  - for 1 piece of the product the price will be the same as basePrice
  - 2 pieces - the price will be basePrice - 10% (200 - 20 = 180)
  - 5 pieces - the same as above
  - 10 pieces (or more) - the price will be basePrice - 30% (200 - 60 = 140)


In response, you will get an ID of this price list.

- The price list can be updated (PUT http://localhost:8080/prices/$id) or deleted (DELETE http://localhost:8080/prices/$id)
- to list all price lists, send GET http://localhost:8080/prices
- most important endpoint: get the price for given product quantity using GET http://localhost:8080/prices/$id?quantity=$quantity
