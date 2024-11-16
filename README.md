## how i run it (updated)

1. using eclipse, clone FinalCall repo, (switch to branch if necessary)
2. right click FinalCall -> `Import Projects` -> check `FinalCall/FinalCall-AuthenticationService`, `FinalCall/FinalCall-CatalogueService` and `FinalCall/FinalCall-Frontend` -> Finish (the other folders are the old code, will clean up soon)
3. To run backend, right click `FinalCall/FinalCall-AuthenticationService` and `FinalCall/FinalCall-CatalogueService` -> `Run as...` -> `Spring Boot App` (`FinalCall/FinalCall-AuthenticationService` runs on port 8081, `FinalCall/FinalCall-CatalogueService` runs on port 8082)
4. To run frontend, cd into FinalCall-Frontend/finalcall-frontend, `npm install` then `npm start`, it runs at `localhost:3000` 
