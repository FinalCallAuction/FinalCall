## How to run FinalCall (development)

1. clone FinalCall repo, (switch to branch if necessary)
2. to run the backend, first place `.finalcall/keys/` into your home directory, ie `/home/user/.finalcall/keys`
3. right click `FinalCall-AuthenticationService`, `FinalCall-AuctionService`, `FinalCall-CatalogueService` and `FinalCall-PaymentService` within Eclipse, `Run as...` then `Spring Boot App`
   - alternatively you can cd into `FinalCall-AuthenticationService`/`FinalCall-AuctionService`/`FinalCall-CatalogueService`/`FinalCall-PaymentService` folders and run `mvn spring-boot:run` in each
4. cd into `Finalcall-Frontend/finalcall-frontend`, run `npm install`, then `npm update` then `npm start`
5. cross fingers, the frontend should be up and running at `http://localhost:3000`

admin credentials:
`admin:admin`
