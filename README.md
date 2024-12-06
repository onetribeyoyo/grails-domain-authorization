# Grails domain-authorization Plugin

# Securing A Grails Applicaiton

This is a multi dimensional problem

1. Users & Roles: anonymous, single user or multiple user
2. Tenants: single or multiple
3. Hierarchical organization of tenant data:  i.e. each tenant may have
   multipe projects
4. State of the tenant data: i.e.

Cool!  I like complex problems, but they're only fun to solve once. So
let's look at the dimensions and describe a few solutions.

## Users & Roles

### Anonymous Users

No security necessary.

### Single User

One step beyond "anonymous" access is the single user system.

Address this situation by using the @Secured annotation (provided by the
spring-security-core plugin) to secure controller actions.

    @Secured(["isFullyAuthenticated()"])

### Multipe Users

As soon as you've secured an application you'll discover you can do
things with the knowledge of who's loged in.  Things like audit logging
are possible (who did what/when.)

### Multipe Users / Multiple Roles

With multiple users you'll find that you want to allow some users to do
somethings and other users to do other things.  This too can be handles
with the @Secured annotation

    @Secured(["ROLE_SUPERUSER"])
    def rmMinusR(String pathname) {
        ...
    }

    @Secured(["ROLE_MOM"])
    def tellAuntAnnaWhatMySonDoesForALiving() {
        ...
    }

## Multiple Tenants

Eventually we're going to need to store data for multiple customers (aka
tenants) in the same database and that likely means we'll also have one
code base for an application for accessing that data.  Let's use these
access control requirements as a model...

* multiple users
* multiple projects
  * each project is owned by a single user
  * for each project, the project's owner can grant access to other users

How do we secure this?


??? Can we use SPel in @Secured ???





## When "Roles" Vary Across Multiple Tenants

Roles aren't enough for this situation.  Roles are great for implmenting
details like "Only the superuser can run with scissors." and "Never let
Mom touch this feature!"  But roles don't capture the variation across
projects that we really need.

One approach is to use the spring-security-acl plugin.  (link to doc)
this adds a few additional anotations to the mix and allows us to write
code something like this...

    @PreAuthorize('isAuthenticated() and principal?.username == #note.author.username')
    def showProject(Project project) {
       ...
    }

The spring-security-acl plugin provides more than just these
annotations.  It's reason for existence is to provide ACL suppoort for
fine grained control of access rights.


??? Can we use @PreAuthorize/etc on controller actions ???





SCRATCH
============================================================

http://www.javacodegeeks.com/2013/11/getting-started-with-method-security-in-grails-using-spring-security.html

http://www.mscharhag.com/2013/10/grails-calling-bean-methods-in-spring.html




Roles are no longer enough.  Sure I might still have a user or two with
the SUPERUSER role, but we'll also have users



Once you cross the line


And User

This is pretty simple and can be addressed by using the @Secured
annotation (provided by the spring-security-core plugin) to secure
controller actions.



If the user is anonymouse then no s
No security necessary.

### Multiple User / Single Tenant

This is pretty simple and can be addressed by using the @Secured
annotation (provided by the spring-security-core plugin) to secure
controller actions.




### Multiple User / Multiple Tenant


###




http://www.objectpartners.com/2011/04/07/fine-grained-security-simplified/










Craig,

Thanks for pointing me toward spring-security-acl.  It's pretty simple
to get services coded in a secure way and it's not too tough to ensure
that object creation creates the appropriate acl entries.  But I do have
a couple of questions for you.

I'd love to be able to use the acl security annotations on controller
actions.  I've seen some references (from Burt) describing why this
might not work but I also found a teaser that leads me to think it could
be possible.  Have you found a way to use the acl annotations on ctrl-er
actions?

What about handling access denies exceptions thrown from service calls?
Has experience led you to an elegant way to handle them without
repeating a lot of code across your actions?



If I didn't have a need for multi-tenancy all of my needs could be
addressed with user roles and the @Secured annotation.


Thanks,
Andy.








## Changes

### 0.4.3 ###

Fixes a bug with assigning default values for expected properties.

### 0.4.1 ###

Relaxing ConfigUtil method signatures: No longer requiring a
`ConfigObject`, now anything like a map is OK.

### 0.4 ###

Initial release.
