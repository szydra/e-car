# e-car
## Overview
The repository contains a Spring Boot application designed for e-car pricing management.
It provides a REST API for defining prices per minute (possibly for a specific period of day)
as well as for calculating a cost of customer's charging process.

In-memory database H2 is used, however there is no problem with connecting the application to a real database.

## How to run
The application may be compiled and packaged via `mvn package` and then run as a single jar file:
```
java -jar target/e-car-0.1.jar
```
Please note that to launch the built jar file only JRE 8 is required.

## How to use
### Defining prices
In order to define a price per minute one should hit the `/prices` endpoint using POST method
with a request body containing number field `minuteRate`. For example
```json
{
  "minuteRate": 0.65
}
```
is used to set the price to *0.65*.

Moreover, it is possible to define a special price which applies to a specific time of the day.
It can be done by adding to the request body fields `startHour` and `endHour` with time in `HH:mm` format.
For example
```json
{
  "minuteRate": 0.48,
  "startHour": "20:00",
  "endHour": "04:30"
}
```
is used to set the special price *0.48* between 8 p.m. and 4:30 a.m.

Special prices take precedence over the usual price and cannot overlap.

### Editing prices
Defined prices can be deleted or modified. In order to delete a price one should hit the resource endpoint,
whose URL is returned in `Location` header in response to a price definition and usually is of the form `/prices/{id}`,
using DELETE method. A price can be modified by hitting its resource URL using PATCH method with a request body
containing fields to be modified. For example
```json
{
  "startHour": "21:45"
}
```
is used to change the starting hour to 9:45 p.m.

### Calculating prices
A total price for a single charging process can be received by hitting the `/prices` endpoint using GET method
with request parameters:
* `start` the start datetime of charging process,
* `end` the end datetime of charging process,
* `customer-id` id of an existing customer.

Datetime values should be in lower precision ISO 8601 format, i.e. `YYYYMMDDTHHmm`.
The calculated price is the value of `total` field in response body. For example a request to
```
/prices?start=20190329T2040&end=20190330T0630&customer-id=customer1
``` 
can return
```json
{
  "total": 125.61
}
```
which corresponds to the total price for customer with id *customer1* for charging a car between
29th March 2019 8:40 p.m. and 30th March 2019 6:30 a.m.

## Remarks
1. A list with sample customers in JSON format is loaded on application startup from location specified
as `customers` key in `application.properties` file.
2. A discount specified as `vip.discount` key in `application.properties` file is applied to vip customers,
by default it is equal to *10%*.
