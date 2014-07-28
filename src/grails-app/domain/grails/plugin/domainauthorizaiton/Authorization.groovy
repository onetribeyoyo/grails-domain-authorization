package grails.plugin.domainauthorization

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode(includes=["id"])
@ToString(includePackage=false, includeNames=true, includeFields=true, ignoreNulls=true, excludes="dateCreated,lastUpdated")
class Authorization implements Comparable {

    String id

    String uid
    String classname
    String instanceId
    String permission

    Date dateCreated
    Date lastUpdated

    static mapping = {
        version false
    }

    static constraints = {
        uid blank: false, nullable: false
        classname blank: false, nullable: false
        instanceId blank: false, nullable: true
        permission blank: true, nullable: true
    }

    int compareTo(that) {
        (this.classname <=> that.classname) ?: (this.instanceId <=> that.instanceId) ?: (this.permission <=> that.permission)
    }

    def beforeValidate() {
        if (permission != null) {
            permission = permission.trim() // Note: alwauys trim white space when persisting permissions!
        }
    }

    def beforeInsert() {
        if (permission != null) {
            permission = permission.trim() // Note: alwauys trim white space when persisting permissions!
        }
    }

    def beforeUpdate() {
        if (isDirty("permission")) {
            if (permission != null) {
                permission = permission.trim() // Note: alwauys trim white space when persisting permissions!
            }
        }
    }

}
