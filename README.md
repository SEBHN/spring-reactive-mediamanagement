[![Build Status](https://travis-ci.org/SEBHN/spring-reactive-mediamanagement.svg?branch=develop)](https://travis-ci.org/SEBHN/spring-reactive-mediamanagement)
[![codecov](https://codecov.io/gh/SEBHN/spring-reactive-mediamanagement/branch/develop/graph/badge.svg)](https://codecov.io/gh/SEBHN/spring-reactive-mediamanagement)


# Tech demo for Spring 5 on Reactive Stack - A media management app

This Project shows an sample application on spring 5 reactive stack. 
It implements the backend for a media mangamement app like OneDrive or GoogleDrive. It focuses on a good usage of technologies rather than a wide functionality. The backend supports folders, tagging and metadata extraction such as a simple user managment. There are functional endpoints for each of these functions given in the Router classes. 

## Setup
### Accounts you'll need
* Security is done with OIDC provider [Okta](https://www.okta.com/) which is free for projects with small traffic. There you will need an application where you must whitelist the routes for this project.
* Persistence is done with MongoDB. For us we used a free [MongoDb Atlas Cluster](https://www.mongodb.com/cloud/atlas). You can create an account and use it up to a specific size for free. You can specify your collection in the connection string you'll get. The collections will be created automatically.  

### Customize application.properties
To setup this project you need to add an application.properties file to src/main/resources and src/test/resources. There you must specify your okta and MongoDb Atlas properties. You need the following properties:
``` yaml
# MongoDB connection string
spring.data.mongodb.uri= TODO your_mongo_db_connection_string
# Okta-Config
okta.baseUrl= TODO your_okta_base_url (ending with .oktapreview.com)
okta.oauth2.issuer=${okta.baseUrl}/oauth2/default
okta.oauth2.clientId= TODO your_client_id
okta.apiKey= TODO your_api_key
okta.apiUrl=${okta.baseUrl}/api/v1
# users group
okta.userGroupIds= TODO your_group_ids (if you have groups)
```
Don't forget to use different DB collections for test and productive usage. 


## Frontend
We have implemented a frontend which uses this api too. It's implemented with Angular 7 and you'll find the code [here](https://github.com/SEBHN/mvs).
