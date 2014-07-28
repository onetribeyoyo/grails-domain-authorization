package grails.plugin.domainauthorization

import java.lang.annotation.*

/**
 *  Annotation for controller actions, defining what autorization is required for the action.  Checking the
 *  authorization condition is delegated to the authorizationService by the authorizationFilters.
 *
 *  Examples:
 *
 *      @Authorized( clazz = Project )
 *            Suggested usage: for controlling access to create new instances.
 *
 *      @Authorized( clazz = Project, idParam = "id" )
 *            This will look for an instance of Project with a call to Project.findById(...)
 *            Suggested usage: for general access to an instance.  Note that this will only match
 *            authorizations with permission == null.
 *
 *      @Authorized( clazz = Project, idParam = "id", idProperty = "code" )
 *            This will look for an instance of Project with a call to Project.findByCode(...)
 *
 *      @Authorized( clazz = Project, idParam = "id", permission = "owner" )
 *        or
 *      @Authorized( clazz = Project, idParam = "id", permission = "viewer" )
 *            Suggested usage: for controlling access to an instance by requiring a specific permission
 *
 *  See AuthorizationFilters.groovy.
 *  See AuthorizationService.groovy.
 */
@Target([ElementType.FIELD, ElementType.METHOD])
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@interface Authorized {

    /** Class of the domain object we're checking */
    Class clazz() default Object

    /**
     *  Name of the request parameter that holds the value we're going to use to find up an instance of clazz.
     *  When idParam is defined the authorization will be checked with a call to
     *
     *      authorizationService.isAuthorized(user, instance, permissions)
     *
     *  Where instance is the result of calling clazz.read(params[idParam]).
     *
     *  When idParam is not defined the authorization will be checked with a call to
     *
     *      authorizationService.isAuthorized(user, clazz, permissions)
     */
    String idParam() default ""

    /**
     *  Name of the property used to find an instance of clazz.  If the idParam is specified but the
     *  idProperty is not, then the idParam will be used to as the idProperty.
     *
     *  NOTE: it is assumed that this property is unique across all instance of the domain class.
     */
    String idProperty() default ""

    /** Permission to check. */
    String permission() default ""

    /**
     *  Name of the property used to find identify a user.
     *
     *  Defaults to "id".
     */
    String uidProperty() default "id"

}
