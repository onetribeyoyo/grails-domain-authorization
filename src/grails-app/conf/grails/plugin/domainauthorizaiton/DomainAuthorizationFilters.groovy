package grails.plugin.domainauthorization

import grails.web.Action

import org.springframework.security.access.AccessDeniedException

/**
 *  Uses the @Authorized annotation to restrict access to controller actions.
 */
class AuthorizationFilters {

    def grailsApplication
    def authorizationService
    def springSecurityService

    def filters = {
        domainSecurity(controller:"*", action:"*") {
            before = {

                def annotation = annotationFor(controllerName, actionName)
                if (annotation) {

                    def user = springSecurityService.currentUser
                    def uidProperty = annotation.uidProperty()
                    def uid = user?."${uidProperty}"

                    def clazz = annotation.clazz()

                    def idParam = annotation.idParam()
                    def idProperty = annotation.idProperty() ?: idParam
                    def id = params."${idParam}"

                    String permission = annotation.permission()

                    def authorized = false
                    if (idParam) {
                        def finderMethod = "findBy${idProperty.capitalize()}"
                        def instance = clazz."${finderMethod}"(id)
                        authorized = authorizationService.isAuthorized(uid, instance, permission)
                    } else {
                        authorized = authorizationService.isAuthorized(uid, clazz, permission)
                    }

                    if (authorized) {
                        if (log.debugEnabled) {
                            log.debug composemessage(controllerName, actionName, clazz, idParam, idProperty, id, permission, authorized)
                        }
                        return true
                    } else {
                        def message = composemessage(controllerName, actionName, clazz, idParam, idProperty, id, permission, authorized)
                        log.warn message
                        render status: 403, text: message
                        return false
                    }
                }
                return true
            }
        }
    }

    /**
     *  returns a message like...
     *    "foo/bar authorized."
     *  or
     *    "foo/bar not authorized."
     *
     *  or, when log.debugEnabled the message will have more detail...
     *    "foo/bar not authorized, permission:VIEWER on my.favorite.class[id(altId):xyzzy]
     */
    private String composeMessage(controllerName, actionName, clazz, idParam, idProperty, id, permission, authorized) {
        def message = "${controllerName}/${actionName}"
        message += (authorized ? "" : " not")
        message += " authorized"
        if (log.debugEnabled) {
            if (permission) message += ", permission:${permission}"
            message += " on ${clazz.simpleName}"
            if (idParam) {
                if (idParam == idProperty) {
                    message += (id ? "[${idParam}:${id}]" : "")
                } else {
                    message += (id ? "[${idParam}(idProperty):${id}]" : "")
                }
            }
        }
        message += "."
        message
    }

    /**
     *  The _annotationMap is a map of maps used to lookup controller/action annotation data...
     *
     *    [
     *      ctrlName1: [ actionName1: @...Authorized(...), actionName2: @...Authorized(...), ... ],
     *      ctrlName2: ...
     *    ]
     */
    private static def _annotationMap = [:]

    private def annotationFor(String controllerName, String actionName) {
        if (!_annotationMap[controllerName]) {
            cacheAnnotationsFor(controllerName)
        }
        _annotationMap[controllerName][actionName]
    }

    private void cacheAnnotationsFor(String controllerName) {
        def map = [:]
        def artefact = grailsApplication.getArtefactByLogicalPropertyName("Controller", controllerName)
        if (artefact) {
            def applicationContext = grailsApplication.mainContext
            def bean = applicationContext.getBean(artefact.clazz.name)
            bean?.class.methods.each { method ->
                if (method.getAnnotation(Action) && method.getAnnotation(Authorized)) {
                    map[method.name] = method.getAnnotation(Authorized)
                }
            }
        }
        _annotationMap[controllerName] = map
    }

}
