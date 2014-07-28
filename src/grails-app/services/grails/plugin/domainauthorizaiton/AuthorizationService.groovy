package grails.plugin.domainauthorization

/**
 *  Used by the @Authorized/AuthorizationFilters and AuthorizationTagLib to check permissions/authorization on
 *  classes and instances.
 *
 *  Note: white space is alwauys trimmed when persisting and querying permissions.
 */
class AuthorizationService {

    static transactional = "mongo"


    //~ authorize/deauthorize methods --------------------------------------------------------------

    /** Establishes an authorization record for the uid/class/permissions combination. */
    void authorize(String uid, Class clazz, String permission = null) {
        if (log.debugEnabled) log.debug "authorize(${uid}, ${clazz}, ${permission})"
        if (!uid) throw new IllegalArgumentException("uid is required.")
        if (!clazz) throw new IllegalArgumentException("clazz is required.")
        if (!findAuthorizations(uid, clazz, permission)) {
            new Authorization(uid:uid, classname:clazz.name, instanceId:null, permission:permission).save(failOnError:true)
        }
    }

    /** Establishes an authorization record for the uid/instance/permission combination. */
    void authorize(String uid, Object instance, String permission = null) {
        if (log.debugEnabled) log.debug "authorize(${uid}, ${instance}, ${permission})"
        if (!uid) throw new IllegalArgumentException("uid is required.")
        if (!instance) throw new IllegalArgumentException("instance is required.")
        if (!instance.id) throw new IllegalArgumentException("instance.id is required.")
        if (!findAuthorizations(uid, instance, permission)) {
            new Authorization(uid:uid, classname:instance.class.name, instanceId:instance.id, permission:permission).save(failOnError:true)
        }
    }

    /** Removes the authorization record for the uid/class/permission combination. */
    void deauthorize(String uid, Class clazz, String permission) {
        if (log.debugEnabled) log.debug "deauthorize(${uid}, ${clazz}, ${permission})"
        if (!uid) throw new IllegalArgumentException("uid is required.")
        if (!clazz) throw new IllegalArgumentException("clazz is required.")
        findAuthorizations(uid, clazz, permission).each { authorization ->
            authorization.delete()
        }
    }

    /** Removes the authorization record for the uid/instance/permission combination. */
    void deauthorize(String uid, Object instance, String permission) {
        if (log.debugEnabled) log.debug "deauthorize(${uid}, ${instance}, ${permission})"
        if (!uid) throw new IllegalArgumentException("uid is required.")
        if (!instance) throw new IllegalArgumentException("instance is required.")
        if (!instance.id) throw new IllegalArgumentException("instance.id is required.")
        findAuthorizations(uid, instance, permission).each { authorization ->
            authorization.delete()
        }
    }

    /** Removes all persisted authorization records for the uid/class combination. */
    void deauthorizeAll(String uid, Class clazz) {
        if (log.debugEnabled) log.debug "deauthorize(${uid}, ${clazz})"
        if (!uid) throw new IllegalArgumentException("uid is required.")
        if (!clazz) throw new IllegalArgumentException("clazz is required.")
        findAuthorizations(uid, clazz).each { authorization ->
            authorization.delete()
        }
    }

    /** Removes all persisted authorization records for the uid/instance combination. */
    void deauthorizeAll(String uid, Object instance) {
        if (log.debugEnabled) log.debug "deauthorize(${uid}, ${instance})"
        if (!uid) throw new IllegalArgumentException("uid is required.")
        if (!instance) throw new IllegalArgumentException("instance is required.")
        if (!instance.id) throw new IllegalArgumentException("instance.id is required.")
        findAuthorizations(uid, instance).each { authorization ->
            authorization.delete()
        }
    }

    /* Removes all of the uid's persisted authorization records. */
    void deauthorizeAll(String uid) {
        if (log.debugEnabled) log.debug "deauthorizeAll(${uid})"
        if (!uid) throw new IllegalArgumentException("uid is required.")
        findAuthorizations(uid).each { authorization ->
            authorization.delete()
        }
    }


    //~ isAuthorized methods -----------------------------------------------------------------------

    /** Returns true if there exists an authorization record matching the uid/class/permission. */
    boolean isAuthorized(String uid, Class clazz, String permission) {
        if (log.debugEnabled) log.debug "isAuthorized(${uid}, ${clazz}, ${permission})"
        findAuthorizations(uid, clazz, permission)
    }

    /** Returns true if there exists an authorization record matching the uid/class for any permission. */
    boolean isAuthorized(String uid, Class clazz) {
        if (log.debugEnabled) log.debug "isAuthorized(${uid}, ${clazz})"
        findAuthorizations(uid, clazz)
    }

    /** Returns true if there exists an authorization record matching the uid/instance/permission. */
    boolean isAuthorized(String uid, Object instance, String permission) {
        if (log.debugEnabled) log.debug "isAuthorized(${uid}, ${instance}, ${permission})"
        findAuthorizations(uid, instance, permission)
    }

    /** Returns true if there exists an authorization record matching the uid/instance for any permission. */
    boolean isAuthorized(String uid, Object instance) {
        if (log.debugEnabled) log.debug "isAuthorized(${uid}, ${instance})"
        findAuthorizations(uid, instance)
    }


    //~ lists of authorized IDs, instances, permissions, ... ---------------------------------------

    /** Returns a list of all IDs the uid is authorized to access with any permission. */
    List<String> authorizedIds(String uid, Class clazz) {
        Authorization.withCriteria {
            eq("uid", uid)
            eq("classname", clazz.name)
            isNotNull("instanceId")
        }.collect { it.instanceId }.unique()
    }

    /** Returns a list of all IDs the uid is authorized to access with the specified permission. */
    List<String> authorizedIds(String uid, Class clazz, String permission) {
        Authorization.withCriteria {
            eq("uid", uid)
            eq("classname", clazz.name)
            isNotNull("instanceId")
            eq("permission", permission)
        }.collect { it.instanceId }.unique()
    }

    /** Returns a (paginated with params) list of all instances of clazz the uid is authorized to access with any permission. */
    List authorizedInstances(String uid, Class clazz, def params = [:]) {
        List<String> authorizedIds = authorizedIds(uid, clazz)
        clazz.withCriteria {
            'in'("id", authorizedIds)
            if (params.max) maxResults(params.max.toInteger())
            if (params.offset) firstResult(params.offset.toInteger())
            if (params.sort) order(params.sort, params.order ?: "asc")
        }
    }

    /** Returns a (paginated with params) list of all instances of clazz the uid is authorized to access with the specified permission. */
    List authorizedInstances(String uid, Class clazz, String permission, def params = [:]) {
        List<String> authorizedIds = authorizedIds(uid, clazz, permission)
        clazz.withCriteria {
            'in'("id", authorizedIds)
            if (params.max) maxResults(params.max.toInteger())
            if (params.offset) firstResult(params.offset.toInteger())
            if (params.sort) order(params.sort, params.order ?: "asc")
        }
    }

    /** Returns the permission for a uid on a project **/
    List findAllPermissions(String uid, Object instance) {
        List permissions = findAuthorizations(uid, instance)?.collect { auth -> auth.permission }
        permissions.unique()
    }


    //~ utility methods ----------------------------------------------------------------------------

    private def findAuthorizations(String uid) {
        Authorization.findAllByUid(uid)
    }

    private def findAuthorizations(String uid, Class clazz) {
        Authorization.withCriteria {
            eq("uid", uid)
            eq("classname", clazz?.name)
            eq("instanceId", null)
        }
    }

    private def findAuthorizations(String uid, Class clazz, String permission) {
        def trimmedPermission = permission?.trim()
        Authorization.withCriteria {
            eq("uid", uid)
            eq("classname", clazz?.name)
            eq("instanceId", null)
            if (trimmedPermission == null) {
                isNull("permission")
            } else {
                eq("permission", trimmedPermission)
            }
        }
    }

    private def findAuthorizations(String uid, Object instance) {
        Authorization.withCriteria {
            eq("uid", uid)
            eq("classname", instance?.class?.name)
            eq("instanceId", instance?.id)
        }
    }

    private def findAuthorizations(String uid, Object instance, String permission) {
        def trimmedPermission = permission?.trim()
        Authorization.withCriteria {
            eq("uid", uid)
            eq("classname", instance?.class?.name)
            eq("instanceId", instance?.id)
            if (trimmedPermission == null) {
                isNull("permission")
            } else {
                eq("permission", trimmedPermission)
            }
        }
    }

}
