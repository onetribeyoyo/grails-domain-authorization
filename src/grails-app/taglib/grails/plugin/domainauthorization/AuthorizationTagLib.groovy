package grails.plugin.domainauthorization

import org.springframework.security.access.AccessDeniedException

class AuthorizationTagLib {

    static namespace = "domainauth"

    def authorizationService
    def springSecurityService

    /**
     *  Checks authorization for either an instance or a class.
     */
    def authorized = { attrs, body ->
        def instance = attrs.instance
        def clazz = attrs.clazz
        def permission = attrs.permission
        if (checkPermissions(instance, clazz, permission)) {
            out << body()
        }
    }

    def notAuthorized = { attrs, body ->
        def permission = attrs.permission
        def instance = attrs.instance
        def clazz = attrs.clazz
        if (!checkPermissions(instance, clazz, permission)) {
            out << body()
        }
    }

    private boolean checkPermissions(instance, clazz, permission) {
        def user = springSecurityService.currentUser

        if (instance) {
            return authorizationService.isAuthorized(user, instance, permission)

        } else {
            if (clazz instanceof String) { // it might be the name of a domain class...
                clazz = grailsApplication.getDomainClass(clazz)?.clazz ?:
                grailsApplication.domainClasses.find { it.clazz.simpleName == clazz }?.clazz ?:
                clazz
            }
            return authorizationService.isAuthorized(user, clazz, permission)
        }
    }

}
