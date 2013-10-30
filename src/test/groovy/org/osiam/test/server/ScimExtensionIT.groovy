package org.osiam.test.server

import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

import org.osiam.client.oauth.AccessToken
import org.osiam.resources.scim.Extension
import org.osiam.resources.scim.User
import org.osiam.test.AbstractIT

import spock.lang.Ignore

class ScimExtensionIT extends AbstractIT {

    def static URN = 'extension'
    
    def setupUser(user) {

        AccessToken validAccessToken = osiamConnector.retrieveAccessToken()

        def http = new HTTPBuilder(RESOURCE_ENDPOINT)

        def responseContent
        def responseStatusCode

        http.request(Method.POST, ContentType.JSON) { req ->
            uri.path = RESOURCE_ENDPOINT + "/Users"
            body = user
            headers."Authorization" = "Bearer " + validAccessToken.getToken()

            response.success = { json ->
                responseContent = json
            }

            response.failure = { resp ->
                responseStatusCode = resp.statusLine.statusCode
            }
        }

        return responseStatusCode == 200 ? responseContent.id : null
    }

    def "Acceptance-Test: HTTP-POST: Adding a scim user with extension schema data to the database"() {
        given:
        AccessToken validAccessToken = osiamConnector.retrieveAccessToken()
        def extension = new Extension(URN, [gender: 'male', size: '1334', birth: new Date().toString(), newsletter: 'false', married:'false'])
        def user = new User.Builder("userName")
                .setPassword("password")
                .addExtension(URN, extension)
                .build();

        when:
        User userCreated = osiamConnector.createUser(user, validAccessToken)

        then:
        userCreated.userName == user.userName
        userCreated.extensions.size() == 1
        userCreated.extensions[URN].fields.size() == 5
    }

    def "Acceptance-Test: HTTP-GET: Retrieving complete data with minimum 0f 5 additional attributes on a single user record"() {

        given:
        AccessToken validAccessToken = osiamConnector.retrieveAccessToken()
        def date = new Date()
        def user = '{"userName":"George Lucas","password":"topSecret!","extension":{"gender":"male","size":"1334","birth":' + date + ',"newsletter":false,"married":false}}'
        def userId = setupUser(user)

        when:
        def http = new HTTPBuilder(RESOURCE_ENDPOINT)

        def responseStatusCode
        def responseContent

        http.request(Method.GET, ContentType.JSON) { req ->
            uri.path = RESOURCE_ENDPOINT + "/Users/" + userId
            headers."Authorization" = "Bearer " + validAccessToken.getToken()

            response.success = { resp, json ->
                responseStatusCode = resp.statusLine.statusCode
                responseContent = json
            }

            response.failure = { resp ->
                responseStatusCode = resp.statusLine.statusCode
            }

        }

        then:
        assert responseStatusCode == 200
        assert responseContent.schemas.size() == 2
        assert responseContent.schemas[0] == "urn:scim:schemas:core:1.0"
        assert responseContent.schemas[1] == "extension"
        assert responseContent.id == userId
        assert responseContent.meta != null
        assert responseContent.extension.gender == "male"
        assert responseContent.extension.size == 1334
        assert responseContent.extension.birth == date
        assert responseContent.extension.newsletter == false
        assert responseContent.extension.married == false
    }

    def "Acceptance-Test: HTTP-GET: Retrieving at least 3 complete user records with the defined extensions"() {
        given:
        AccessToken validAccessToken = osiamConnector.retrieveAccessToken()
        def date = new Date()
        setupUser('{"userName":"George Michael","password":"topSecret!","extension":{"gender":"male","size":"1334","birth":' + date + ',"newsletter":false,"married":false}}')
        setupUser('{"userName":"George Brian","password":"topSecret!","extension":{"gender":"male","size":"1334","birth":' + date + ',"newsletter":false,"married":false}}')
        setupUser('{"userName":"George Adam","password":"topSecret!","extension":{"gender":"male","size":"1334","birth":' + date + ',"newsletter":false,"married":false}}')

        when:
        def http = new HTTPBuilder(RESOURCE_ENDPOINT)

        def responseStatusCode
        def responseContent

        http.request(Method.GET, ContentType.JSON) { req ->
            uri.path = RESOURCE_ENDPOINT + "/Users"
            headers."Authorization" = "Bearer " + validAccessToken.getToken()

            response.success = { resp, json ->
                responseStatusCode = resp.statusLine.statusCode
                responseContent = json
            }

            response.failure = { resp ->
                responseStatusCode = resp.statusLine.statusCode
            }

        }

        then:
        assert responseStatusCode == 200
        assert responseContent.schemas.size() == 2
        responseContent.Resources.each {user ->
            assert user.id != null
            assert user.meta != null
            assert user.extension.gender == "male"
            assert user.extension.size == 1334
            assert user.extension.birth == date
            assert user.extension.newsletter == false
            assert user.extension.married == false
        }
    }

    def "Acceptance-Test: HTTP-DELETE: Delete a scim user record including his extension data"() {
        given:
        AccessToken validAccessToken = osiamConnector.retrieveAccessToken()
        def date = new Date()
        def user = '{"userName":"George Hamilton","password":"topSecret!","extension":{"gender":"male","size":"1334","birth":' + date + ',"newsletter":false,"married":false}}'
        def userId = setupUser(user)

        when:
        def http = new HTTPBuilder(RESOURCE_ENDPOINT)

        def responseStatusCode

        http.request(Method.DELETE, ContentType.JSON) { req ->
            uri.path = RESOURCE_ENDPOINT + "/Users/" + userId
            headers."Authorization" = "Bearer " + validAccessToken.getToken()

            response.success = { resp ->
                responseStatusCode = resp.statusLine.statusCode
            }

            response.failure = { resp ->
                responseStatusCode = resp.statusLine.statusCode
            }

        }

        then:
        assert responseStatusCode == 200
    }

    def "Acceptance-Test: HTTP-PUT: Updating a scim user record including his extension fields"() {

        given:
        AccessToken validAccessToken = osiamConnector.retrieveAccessToken()
        def date = new Date()
        def user = '{"userName":"George Gregorian","password":"topSecret!","extension":{"gender":"male","size":"1334","birth":' + date + ',"newsletter":false,"married":false}}'
        def userId = setupUser(user)

        def userToUpdate = '{"userName":"George Gregorian","password":"topSecret!","extension":{"gender":"male","size":"1334","newsletter":true,"married":true}}'

        when:
        def http = new HTTPBuilder(RESOURCE_ENDPOINT)

        def responseStatusCode
        def responseContent

        http.request(Method.PUT, ContentType.JSON) { req ->
            uri.path = RESOURCE_ENDPOINT + "/Users/" + userId
            body = userToUpdate
            headers."Authorization" = "Bearer " + validAccessToken.getToken()

            response.success = { resp, json ->
                responseStatusCode = resp.statusLine.statusCode
                responseContent = json
            }

            response.failure = { resp ->
                responseStatusCode = resp.statusLine.statusCode
            }

        }

        then:
        assert responseStatusCode == 200
        assert responseContent.userName == "George Gregorian"
        assert responseContent.schemas.size() == 2
        assert responseContent.id != null
        assert responseContent.meta != null
        assert responseContent.extension.gender == "male"
        assert responseContent.extension.size == 1334
        assert responseContent.extension.birth == null
        assert responseContent.extension.newsletter == true
        assert responseContent.extension.married == true
    }

    @Ignore("Groovy HttpBuilder does not support the HTTP PATCH operation")
    def "Acceptance-Test: HTTP-PATCH: Updating a scim user record including his extension fields"() {
        given:
        AccessToken validAccessToken = osiamConnector.retrieveAccessToken()
        def date = new Date()
        def user = '{"userName":"George Stanley","password":"topSecret!","extension":{"gender":"male","size":"1334","birth":' + date + ',"newsletter":false,"married":false}}'
        def userId = setupUser(user)

        def userToUpdate = '{"userName":"George Stanley","password":"topSecret!","extension":{"newsletter":true,"married":true}}'

        when:
        def http = new HTTPBuilder(RESOURCE_ENDPOINT)

        def responseStatusCode
        def responseContent

        //TODO: Check options for not supported PATCH operation
        http.request("PATCH", ContentType.JSON) { req ->
            uri.path = RESOURCE_ENDPOINT + "/Users/" + userId
            body = userToUpdate
            headers."Authorization" = "Bearer " + validAccessToken.getToken()

            response.success = { resp, json ->
                responseStatusCode = resp.statusLine.statusCode
                responseContent = json
            }

            response.failure = { resp ->
                responseStatusCode = resp.statusLine.statusCode
            }

        }

        then:
        assert responseStatusCode == 200
        assert responseContent.userName == "George Stanley"
        assert responseContent.schemas.size() == 2
        assert responseContent.id != null
        assert responseContent.meta != null
        assert responseContent.extension.gender == "male"
        assert responseContent.extension.size == 1334
        assert responseContent.extension.birth == date
        assert responseContent.extension.newsletter == true
        assert responseContent.extension.married == true
    }
}