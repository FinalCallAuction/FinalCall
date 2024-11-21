## How to run FinalCall (development)

1. clone FinalCall repo, (switch to branch if necessary)
2. to run the backend, right click `FinalCall-AuthenticationService`, `FinalCall-AuctionService`, `FinalCall-CatalogueService` and `FinalCall-PaymentService` within Eclipse, `Run as...` then `Spring Boot App`
   - alternatively you can cd into `FinalCall-AuthenticationService`/`FinalCall-AuctionService`/`FinalCall-CatalogueService`/`FinalCall-PaymentService` folders and run `mvn spring-boot:run` in each
3. cd into `Finalcall-Frontend/finalcall-frontend`, run `npm install`, then `npm update` then `npm start`
4. cross fingers
