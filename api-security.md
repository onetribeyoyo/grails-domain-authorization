## API Security

The REST API will be secured with a two part process where the
authentication part is handled with an OATH 2 style process, and the
authorization part is handled with @Authorized just like the user
interface of the web app.

Figure 1 from
http://self-issued.info/docs/draft-ietf-oauth-v2-bearer.html#anchor4
sums it up nicely...

    +--------+                               +---------------+
    |        |--(A)- Authorization Request ->|   Resource    |
    |        |                               |     Owner     |
    |        |<-(B)-- Authorization Grant ---|               |
    |        |                               +---------------+
    |        |
    |        |                               +---------------+
    |        |--(C)-- Authorization Grant -->| Authorization |
    | Client |                               |     Server    |
    |        |<-(D)----- Access Code --------|               |
    |        |                               +---------------+
    |        |
    |        |                               +---------------+
    |        |--(E)----- Access Code ------->|    Resource   |
    |        |                               |     Server    |
    |        |<-(F)--- Protected Resource ---|               |
    +--------+                               +---------------+


The web app will be acting as all three server-side roles (resource
owner, authorization server, and resource server.)


## Securing Actions

To secure an action requires both coding and data.  First the coding

Actions that need to be secured with API security must be annotated with
both

    @Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])

and

    @SecuredApi

The `@Secured` annotation with `IS_AUTHENTICATED_ANONYMOUSLY` indicates
that spring security should not be involved with controlling access.  No
user login will be required.

The `SecuredApi` annotation is a marker that is used by the
ApiConsumerFilters to indicate that an accessCode is expected to be
present in the request's `authorization` header variable.


## Granting Access To Resources

To obtain access to `@SecuredApi` actions you must first create and then
activate an ApiConsumer.

### Creating an API Consumer

Login to the project and use the `Admin / API Consumuers` menu.  This
will take you to the API Consumer web pages for your project where you
can create and edit API Consumers.

### Activating an API Consumer

There are two ways.

Once you've created a consumer, you can activate it from the web app by clicking on the `Activate`
link on the API Consumer Details page.

Or...

You can use the `/api/token` service to activate it from a remote
device.  To use the service you'll need to have the `activationName` and
`activationCode` for the API Consumer.  The combination of these make up
the activationKey that is passed on to the token service.  For example,
when

    activationName = abcdef
    activationCode = 123456

then

    activationKey = abcdef123456

and a call to

    /api/token?activationKey=abcdef123456

will return an accessCode similar to `6bf40402-dcc4-41b6-9e60-c9232a21ef54`.  The response is JSON data that also includes an info URI for retrieving additional data about the primary consumer.  For example:

    {
        "accessCode":"5e51312e-6087-4af4-a43f-3af0fe55adbb",
        "infoURI":"/api/prj5/participant/prj5-pid1"
    }


#### Providing Device Information When Activating an API Consumer

In addition to the required `activationKey` the `/api/token` service
also accepts `deviceInfo` parameters.  For example, the following call
passes three device info parameter values

    http://localhost:8080/api/token?activationKey=prj4-pid13KYkSp&deviceInfo=Device:%20Android%20Smartphone&deviceInfo=Model:%20TGF-938&deviceInfo=OS%20Version:%204.0

The results of this call (assuming the activationKey is valid) will
store the deviceInfo...

    Device: Android Smartphone
    Model: TGF-938
    OS Version: 4.0

on the participant device document corresponding to the activationKey.


### Using an API Consumer's accessCode

An API consumer will only be allowed access to `@SecuredApi` actions
when the consumer presents the accessCode in the request's
`authorization` header variable.  For example

    authorization: "bearer accessCode"


### API Consumer Scopes

The scope of an API consumer is a list of pattern strings that defines
the URIs to which the consumer has been granted access.  For example:

    [
      "/api/123456/groups",
      "/api/7890/**"
    ]

Scope patterns can be absolute:

    "/api/123456/groups"

in which case only the specified resource can be accessed.  Access is
denied to everything else.

Or the scope pattern can include a wildcard:

    "/api/7890/**"

in which case access is allowed to any resource starting with
"/api/123456/".
