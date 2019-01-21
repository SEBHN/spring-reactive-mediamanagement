[![Build Status](https://travis-ci.org/SEBHN/spring-reactive-mediamanagement.svg?branch=develop)](https://travis-ci.org/SEBHN/spring-reactive-mediamanagement)
[![codecov](https://codecov.io/gh/SEBHN/spring-reactive-mediamanagement/branch/develop/graph/badge.svg)](https://codecov.io/gh/SEBHN/spring-reactive-mediamanagement)


# Tech demo for Spring 5 on Reactive Stack - A media management app

This Project shows an sample application on spring 5 reactive stack. 
It implements the backend for a media mangamement app like OneDrive or GoogleDrive. It focuses on a good usage of techonlogies rather than a wide functionality. The backend supports folders, tagging and metadata extraction such as a simple user managment. There are functional endpoints for each of these functions given in the Router classes. 

## Customize application.properties
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
