## Application Security

To keep our application security implementation clean we are using
`@Secured` on controller actions.  This gives use the ability to assign
ROLES to users and allow some users to do somethings and other users to
do other things.

We also have a need to vary "roles" across multiple tenants.  Role based
security and `@Secured` aren't enough for this situation.

One possible solution is to use the spring-security-acl plugin.  This
adds a few additional annotations to the mix but unfortunately those
annotations are only available on service methods.  And the domain model
provided by the plugin is not compatible with mongo.

To solve the problem of implementing security on controller actions
we've added a new `@Authorized` annotation.  This annotation works
together with `AuthorizationFilters` and a `AuthorizationService`.
Checking the authorization condition is delegated to the service by the
filter.

With this approach we can still use the equivalent of ACLs to secure the
app and we also have an annotation that we can use alongside `@Secured`
on controller actions.

For API details look at `AuthorizationService`.

### Levels of Authorization

By convention we are using these levels of authorization

1. Class level authorization: used to grant users the ability to create
new instances of a domain class.  For example

        authorizationService.authorize(user, (Project))

2. Instance level authorization with the "ADMIN" permission: used to
allow users to do administrative functions for individual domain
objects.  For example

        Project project = Project.findBy...
        authorizationService.authorize(user, project, "ADMIN")

3. Instance level authorization: used to allow users to "team member"
operations on an individual domain object.  For example

        Project project = Project.findBy...
        authorizationService.authorize(user, project)


### Instance Level Authorization with @authorized

Authorization of specific instances will allow access only to those
users who have been granted access to a particular instance.  Access is
granted with

    Project project = Project.findBy...
    authorizationService.authorize(user, project)

and controller actions are annotated with

    @Secured(["isAuthenticated()"])
    @Authorized( clazz = (Project), idParam = "id" )
    def show(Project project) {
        ...
    }

The `@Secured` annotation forces the user to login before access is
allowed and the `@Authorized` annotation restricts access to users who
have been "authorized" to work with the Project instance identified by
the value of the "id" param.

### Class Level Authorization with @authorized

Class level authorization is used to grant users the ability to create
new instances.  For example

    @Secured(["isAuthenticated()"])
    @Authorized( clazz = (Project) )
    def create() {
        ...
    }

When the annotation doesn't specify an idParam, the authorization is
checked at the class level.

### TabLibs: &lt;auth:authorized ...&gt; and &lt;auth:notAuthorized ...&gt;

The auth taglib mirrors the behavior of the annotation.  Use the tags to
hide/show page content.  For example

    <auth:authorized clazz="Project">
      <g:link controller="project" action="create">New project...</g:link>
    </auth:authorized>

    <auth:authorized clazz="Project" idParam="xyzzy" permissions="ADMIN">
      <g:link controller="project" action="edit">edit...</g:link>
    </auth:authorized> <auth:notAuthorized>
      -- only the project admin can edit the project --
    </auth:notAuthorized>
