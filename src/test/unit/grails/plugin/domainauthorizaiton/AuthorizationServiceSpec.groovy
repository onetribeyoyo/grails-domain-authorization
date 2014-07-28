package grails.plugin.domainauthorization

import com.onetribeyoyo.mtm.project.Project
import com.onetribeyoyo.mtm.project.Story

import grails.buildtestdata.mixin.Build

import grails.test.mixin.Mock
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(AuthorizationService)
@Build([Project, Story])
//@Mock([Authorization, Project, Story])
@Mock([Authorization])
class AuthorizationServiceSpec extends Specification {

    @Unroll("authorize class throws exception when args are (#uid, #clazz).")
    void "authorize class throws exception when args are null"() {
        when: "some of the required args are null"
            service.authorize(uid, clazz, null)
        then: "an illegal argument exception is thrown"
            thrown IllegalArgumentException
        where:
            uid   | clazz
            null  | null
            "uid" | null
            null  | (String)
    }

    @Unroll("authorize instance throws exception when args are (#uid, #instance).")
    void "authorize instance throws exception when args are null"() {
        when: "some of the required args are null"
            service.authorize(uid, instance, null)
        then: "an illegal argument exception is thrown"
            thrown IllegalArgumentException
        where:
            uid   | instance
            null  | null
            "uid" | null
            null  | new Project(name:"pname", code: "code")
    }

    void "authorize throws exception when instance.id is null"() {
        given:
            String uid = "uid"
            Object instance = new Project(name: "pname", code: "code")

        when:
            service.authorize(uid, instance, null)
        then: "an illegal argument exception is thrown"
            thrown IllegalArgumentException

        when:
            instance.save()
        then: "no exception is thrown"
            service.authorize(uid, instance)
    }


    @Unroll("deauthorizeAll for class throws exception when args are (#uid, #clazz).")
    void "deauthorizeAll for class throws exception when args are null"() {
        when: "some of the required args are null"
            service.deauthorizeAll(uid, clazz)
        then: "an illegal argument exception is thrown"
            thrown IllegalArgumentException
        where:
            uid   | clazz
            null  | null
            "uid" | null
            null  | (String)
    }

    @Unroll("deauthorize class with specific permissions throws exception when args are (#uid, #clazz).")
    void "deauthorize class with specific permissions throws exception when args are null"() {
        when: "some of the required args are null"
            service.deauthorize(uid, clazz, "OWNER")
        then: "an illegal argument exception is thrown"
            thrown IllegalArgumentException
        where:
            uid   | clazz
            null  | null
            "uid" | null
            null  | (String)
    }

    @Unroll("deauthorizeAll for instance throws exception when args are (#uid, #instance).")
    void "deauthorizeAll for instance throws exception when args are null"() {
        when: "some of the required args are null"
            service.deauthorizeAll(uid, instance)
        then: "an illegal argument exception is thrown"
            thrown IllegalArgumentException
        where:
            uid   | instance
            null  | null
            "uid" | null
            null  | new Project(name:"pname")
    }

    @Unroll("deauthorize instance with specific permissions throws exception when args are (#uid, #instance).")
    void "deauthorize instance with specific permissions throws exception when args are null"() {
        when: "some of the required args are null"
            service.deauthorize(uid, instance, "OWNER")
        then: "an illegal argument exception is thrown"
            thrown IllegalArgumentException
        where:
            uid   | instance
            null  | null
            "uid" | null
            null  | new Project(name:"pname", code: "code")
    }

    void "deauthorizeAll throws exception when instance.id is null"() {
        given:
            String uid = "uid"
            Object instance = new Project(name:"pname", code: "code")

        when:
            service.deauthorizeAll(uid, instance)
        then: "an illegal argument exception is thrown"
            thrown IllegalArgumentException

        when:
            instance.save()
        then: "no exception is thrown"
            service.deauthorizeAll(uid, instance)
    }

    void "deauthorize with specific permissions throws exception when instance.id is null"() {
        given:
            String uid = "uid"
            Object instance = new Project(name:"pname", code: "code")

        when:
            service.deauthorize(uid, instance, "OWNER")
        then: "an illegal argument exception is thrown"
            thrown IllegalArgumentException

        when:
            instance.save()
        then: "no exception is thrown"
            service.deauthorize(uid, instance, "OWNER")
    }

    void "isAuthorized doesn't throw exception when args are null"() {
        when:
            String uid = "uid"
            Class clazz = (String)
        then:
            !service.isAuthorized(null, null, null)
            !service.isAuthorized(null, clazz, null)
            !service.isAuthorized(uid, null, null)
    }

    void "class level authorization"() {
        given:
            String uid = "uid"
            Class clazz = (String)
        expect: "no authorization for new uids"
            !service.isAuthorized(uid, clazz, null)

        when: "an authorization record is added"
            service.authorize(uid, clazz)
        then: "authorization is permitted"
            service.isAuthorized(uid, clazz, null)

        when: "the uid is deauthorized"
            service.deauthorizeAll(uid, clazz)
        then: "authorization is no longer permitted"
            !service.isAuthorized(uid, clazz, null)
    }

    void "permissions consider empty string and white space to be same as null"() {
        given:
            String uid = "uid"
            Class clazz = (String)

        when:
            service.authorize(uid, clazz, "OWNER")
        then:
            service.isAuthorized(uid, clazz, "OWNER")
            !service.isAuthorized(uid, clazz, null)
    }

    void "class level authorization for two uids"() {
        given:
            String uid1 = "uid1"
            String uid2 = "uid2"
            Class clazz = (String)
        expect:
            !service.isAuthorized(uid1, clazz, null)
            !service.isAuthorized(uid2, clazz, null)

        when: "an authorization record is added for one uid"
            service.authorize(uid1, clazz)
        then: "the other doesn't have permission"
            service.isAuthorized(uid1, clazz, null)
            !service.isAuthorized(uid2, clazz, null)

        when: "an authorization record is added for one uid"
            service.authorize(uid2, clazz)
        then: "both uids are authorizes"
            service.isAuthorized(uid1, clazz, null)
            service.isAuthorized(uid2, clazz, null)

        when: "one uid is deauthorize"
            service.deauthorizeAll(uid2, clazz)
        then: "the other still has access"
            service.isAuthorized(uid1, clazz, null)
            !service.isAuthorized(uid2, clazz, null)
    }

    void "class level authorization with specific permissions"() {
        given:
            String uid = "uid"
            Class clazz = (String)
            def permissions = "OWNER"
            def otherPermissions = "VIEWER"
        expect:
            !service.isAuthorized(uid, clazz, null)
            !service.isAuthorized(uid, clazz, permissions)
            !service.isAuthorized(uid, clazz, otherPermissions)

        when: "an authorization record is added with no specific permissions"
            service.authorize(uid, clazz)
        then: "authorization is permitted"
            service.isAuthorized(uid, clazz, null)
        and: "not for the specific permissions"
            !service.isAuthorized(uid, clazz, permissions)
            !service.isAuthorized(uid, clazz, otherPermissions)

        when: "the uid is authorized for the specific permissions"
            service.authorize(uid, clazz, permissions)
        then: "authorization is allowed for permissions"
            service.isAuthorized(uid, clazz, null)
            service.isAuthorized(uid, clazz, permissions)
        and: "authorization is not allowed for other permissions"
            !service.isAuthorized(uid, clazz, otherPermissions)

        when: "the uid is deauthorized for the specific permissions"
            service.deauthorize(uid, clazz, permissions)
        then: "authorization is still allowed for the class"
            service.isAuthorized(uid, clazz, null)
            !service.isAuthorized(uid, clazz, permissions)

        when: "the uid is deauthorized with no specific permissions"
            service.deauthorizeAll(uid, clazz)
        then: "authorization is not allowed"
            !service.isAuthorized(uid, clazz, null)
            !service.isAuthorized(uid, clazz, permissions)
    }

    void "class level authorization with specific permissions for two uids"() {
        given:
            String uid1 = "uid1"
            String uid2 = "uid2"
            Class clazz = (String)
            def permissions = "OWNER"
        expect:
            !service.isAuthorized(uid1, clazz, permissions)
            !service.isAuthorized(uid2, clazz, permissions)

        when: "an authorization record is added for one uid"
            service.authorize(uid1, clazz, permissions)
        then: "the other doesn't have permission"
            service.isAuthorized(uid1, clazz, permissions)
            !service.isAuthorized(uid2, clazz, permissions)

        when: "an authorization record is added for one uid"
            service.authorize(uid2, clazz, permissions)
        then: "both uids are authorizes"
            service.isAuthorized(uid1, clazz, permissions)
            service.isAuthorized(uid2, clazz, permissions)

        when: "one uid is deauthorize"
            service.deauthorize(uid2, clazz, permissions)
        then: "the other still has access"
            service.isAuthorized(uid1, clazz, permissions)
            !service.isAuthorized(uid2, clazz, permissions)
    }

    void "instance level authorization"() {
        given:
            String uid = "uid"
            Object instance = Project.build()
            Object another = Project.build()
        expect: "no authorization for new uids"
            !service.isAuthorized(uid, instance, null)
            !service.isAuthorized(uid, another, null)

        when: "an authorization record is added"
            service.authorize(uid, instance)
        then: "authorization is permitted"
            service.isAuthorized(uid, instance, null)
        and: "not for other instances"
            !service.isAuthorized(uid, another, null)

        when: "the uid is deauthorized"
            service.deauthorizeAll(uid, instance)
        then: "authorization is no longer permitted"
            !service.isAuthorized(uid, instance, null)
            !service.isAuthorized(uid, another, null)
    }

    void "instance level authorization for two uids"() {
        given:
            String uid1 = "uid1"
            String uid2 = "uid2"
            Object instance = Project.build()
            Object another = Project.build()
        expect: "no authorization for new uids"
            !service.isAuthorized(uid1, instance, null)
            !service.isAuthorized(uid1, another, null)
            !service.isAuthorized(uid2, instance, null)
            !service.isAuthorized(uid2, another, null)

        when: "an authorization record is added for one uid"
            service.authorize(uid1, instance)
        then: "the other does not have access"
            service.isAuthorized(uid1, instance, null)
            !service.isAuthorized(uid1, another, null)
            !service.isAuthorized(uid2, instance, null)
            !service.isAuthorized(uid2, another, null)

        when: "the uid is deauthorized"
            service.deauthorizeAll(uid1, instance)
        then: "authorization is no longer permitted"
            !service.isAuthorized(uid1, instance, null)
            !service.isAuthorized(uid1, another, null)
            !service.isAuthorized(uid2, instance, null)
            !service.isAuthorized(uid2, another, null)
    }

    void "instance level authorization with specific permissions"() {
        given:
            String uid = "uid"
            Object instance = Project.build()
            Object another = Project.build()
            def permissions = "OWNER"
        expect:
            !service.isAuthorized(uid, instance, null)
            !service.isAuthorized(uid, instance, permissions)
            !service.isAuthorized(uid, another, null)
            !service.isAuthorized(uid, another, permissions)

        when: "an authorization record is added with no specific permissions"
            service.authorize(uid, instance)
        then: "authorization is permitted"
            service.isAuthorized(uid, instance, null)
        and: "not for other instances"
            !service.isAuthorized(uid, another, null)
        and: "not for the specific permissions"
            !service.isAuthorized(uid, instance, permissions)
        and: "nor for other instances"
            !service.isAuthorized(uid, another, permissions)

        when: "the uid is deauthorized for the specific permissions"
            service.deauthorize(uid, instance, permissions)
        then: "authorization is still allowed for the class"
            service.isAuthorized(uid, instance, null)
            !service.isAuthorized(uid, instance, permissions)
        and: "not for other instances"
            !service.isAuthorized(uid, another, null)
            !service.isAuthorized(uid, another, permissions)

        when: "the uid is deauthorized with no specific permissions"
            service.deauthorizeAll(uid, instance)
        then: "authorization is not allowed"
            !service.isAuthorized(uid, instance, null)
            !service.isAuthorized(uid, instance, permissions)
    }

    void "instance level authorization with specific permissions for two uids"() {
        given:
            String uid1 = "uid1"
            String uid2 = "uid2"
            Object instance = Project.build()
            Object another = Project.build()
            def permissions = "OWNER"
        expect: "no authorization for new uids"
            !service.isAuthorized(uid1, instance, permissions)
            !service.isAuthorized(uid1, another, permissions)
            !service.isAuthorized(uid2, instance, permissions)
            !service.isAuthorized(uid2, another, permissions)

        when: "an authorization record is added for one uid"
            service.authorize(uid1, instance, permissions)
        then: "the other does not have access"
            service.isAuthorized(uid1, instance, permissions)
            !service.isAuthorized(uid1, another, permissions)
            !service.isAuthorized(uid2, instance, permissions)
            !service.isAuthorized(uid2, another, permissions)

        when: "the uid is deauthorized"
            service.deauthorize(uid1, instance, permissions)
        then: "authorization is no longer permitted"
            !service.isAuthorized(uid1, instance, permissions)
            !service.isAuthorized(uid1, another, permissions)
            !service.isAuthorized(uid2, instance, permissions)
            !service.isAuthorized(uid2, another, permissions)
    }

    void "deauthorizeAll"() {
        given:
            String uid = "uid"
            Class clazz = (String)
            Object instance = Project.build()
            def permissions = "OWNER"

        when:
            service.authorize(uid, clazz)
            service.authorize(uid, clazz, permissions)
            service.authorize(uid, instance)
            service.authorize(uid, instance, permissions)
        then:
            service.isAuthorized(uid, clazz, null)
            service.isAuthorized(uid, clazz, permissions)
            service.isAuthorized(uid, instance, null)
            service.isAuthorized(uid, instance, permissions)

        when:
            service.deauthorizeAll(uid)
        then:
            !service.isAuthorized(uid, clazz, null)
            !service.isAuthorized(uid, clazz, permissions)
            !service.isAuthorized(uid, instance, null)
            !service.isAuthorized(uid, instance, permissions)
    }

    void "deauthorizeAll requries a uid"() {
        when:
            service.deauthorizeAll(null)
        then:
            thrown IllegalArgumentException
    }

    void "authorize is idempotent"() {
        given:
            String uid = "uid"
            Class clazz = (String)
            Object instance = Project.build()
            def permissions = "OWNER"
        expect:
            Authorization.count() == 0

        when:
            service.authorize(uid, clazz)
            service.authorize(uid, clazz, permissions)
            service.authorize(uid, instance)
            service.authorize(uid, instance, permissions)
        then:
            Authorization.count() == 4

        when: "make repetative calls"
            service.authorize(uid, clazz)
            service.authorize(uid, clazz, permissions)
            service.authorize(uid, instance)
            service.authorize(uid, instance, permissions)
        then: "still the same number of records!"
            Authorization.count() == 4
    }

    void "authorizedIds"() {
        given: "a uid and a few projects"
            String uid = "uid"
            Project project1 = new Project(name:"pname1", code: "code1").save()
            Project project2 = new Project(name:"pname2", code: "code2").save()
            Project project3 = new Project(name:"pname3", code: "code3").save()
            Project project4 = new Project(name:"pname4", code: "code4").save()
            Project project5 = new Project(name:"pname5", code: "code5").save()
            Story story = Story.build(project: project1)
        expect:
            Project.count() == 5
            Story.count() == 1
            service.authorizedIds(uid, Project).size() == 0
            service.authorizedIds(uid, Project, null).size() == 0
            service.authorizedIds(uid, Project, "OWNER").size() == 0

        when: "the uid is authorized for a project"
            service.authorize(uid, project1)
        then:
            Project.count() == 5
            service.authorizedIds(uid, Project).size() == 1
            service.authorizedIds(uid, Project, null).size() == 1
            service.authorizedIds(uid, Project, "OWNER").size() == 0

        when: "the uid is authorized for more projects"
            service.authorize(uid, project2)
            service.authorize(uid, project3)
        then:
            Project.count() == 5
            service.authorizedIds(uid, Project).size() == 3
            service.authorizedIds(uid, Project, null).size() == 3
            service.authorizedIds(uid, Project, "OWNER").size() == 0

        when: "the uid is authorized for the project class"
            service.authorize(uid, Project)
        then: "class authorizations don't effect authorizedIds()"
            Project.count() == 5
            service.authorizedIds(uid, Project).size() == 3
            service.authorizedIds(uid, Project, null).size() == 3
            service.authorizedIds(uid, Project, "OWNER").size() == 0

        when: "the uid is authorized for things other than projects"
            service.authorize(uid, Story)
            service.authorize(uid, story)
        then:
            Project.count() == 5
            service.authorizedIds(uid, Project).size() == 3
            service.authorizedIds(uid, Story).size() == 1
            service.authorizedIds(uid, Project, null).size() == 3
            service.authorizedIds(uid, Project, "OWNER").size() == 0

        when: "the uid is authorized for specific permissions"
            service.authorize(uid, project5, "OWNER")
        then:
            Project.count() == 5
            service.authorizedIds(uid, Project).size() == 4
            service.authorizedIds(uid, Project, null).size() == 3
            service.authorizedIds(uid, Project, "OWNER").size() == 1

        when: "the uid is deauthorized for a project"
            service.deauthorizeAll(uid, project1)
        then:
            Project.count() == 5
            service.authorizedIds(uid, Project).size() == 3
            service.authorizedIds(uid, Project, null).size() == 2
            service.authorizedIds(uid, Project, "OWNER").size() == 1

        when: "the uid is deauthorized for the project class"
            service.deauthorizeAll(uid, Project)
        then: "class authorizations don't effect authorizedIds()"
            Project.count() == 5
            service.authorizedIds(uid, Project).size() == 3
            service.authorizedIds(uid, Project, null).size() == 2
            service.authorizedIds(uid, Project, "OWNER").size() == 1

        when:
            service.deauthorizeAll(uid)
        then:
            Project.count() == 5
            service.authorizedIds(uid, Project).size() == 0
            service.authorizedIds(uid, Project, null).size() == 0
            service.authorizedIds(uid, Project, "OWNER").size() == 0
            service.authorizedIds(uid, Story).size() == 0
    }

    void "authorizedInstances"() {
        given: "a uid and a few projects"
            String uid = "uid"
            Project project1 = new Project(name:"pname1", code: "code1").save()
            Project project2 = new Project(name:"pname2", code: "code2").save()
            Project project3 = new Project(name:"pname3", code: "code3").save()
            Project project4 = new Project(name:"pname4", code: "code4").save()
            Project project5 = new Project(name:"pname5", code: "code5").save()
        expect:
            Project.count() == 5
            !service.authorizedInstances(uid, Project)

        when: "the uid is authorized for a project"
            service.authorize(uid, project3)
        then:
            Project.count() == 5
            service.authorizedInstances(uid, Project).size() == 1
            service.authorizedInstances(uid, Project)[0].id == project3.id

        when: "the uid is authorized for more projects"
            service.authorize(uid, project2)
            service.authorize(uid, project4)
        then:
            Project.count() == 5
            service.authorizedInstances(uid, Project).size() == 3
    }

    void "authorizedInstances with pagination params"() {
        given: "a uid and a few projects"
            String uid = "uid"
            20.times {
                Project project = new Project(name:"pname${it}", code: "code${it}").save()
                service.authorize(uid, project)
            }
        expect: "max and offset work as expected"
            service.authorizedInstances(uid, Project).size() == 20
            service.authorizedInstances(uid, Project, [max:10]).size() == 10
            service.authorizedInstances(uid, Project, [max:10, offset:15]).size() == 5
        and: "sort and order work as expected"
            service.authorizedInstances(uid, Project, [sort:"name"])[0].name == "pname0"
            service.authorizedInstances(uid, Project, [sort:"name", order:"desc"])[0].name == "pname9"
            service.authorizedInstances(uid, Project, [sort:"name", order:"asc"])[0].name == "pname0"
    }

    void "authorizedInstances for specific permissions"() {
        given: "a uid and a few projects"
            String uid = "uid"
            Project project1 = new Project(name:"pname1", code: "code1").save()
            Project project2 = new Project(name:"pname2", code: "code2").save()
            Project project3 = new Project(name:"pname3", code: "code3").save()
            Project project4 = new Project(name:"pname4", code: "code4").save()
            Project project5 = new Project(name:"pname5", code: "code5").save()
        expect:
            Project.count() == 5
            !service.authorizedInstances(uid, Project, "OWNER")

        when: "the uid is authorized for a project"
            service.authorize(uid, project3, "OWNER")
        then:
            Project.count() == 5
            service.authorizedInstances(uid, Project, "OWNER").size() == 1
            service.authorizedInstances(uid, Project, "OWNER")[0].id == project3.id

        when: "the uid is authorized for more projects"
            service.authorize(uid, project2, "OWNER")
            service.authorize(uid, project4, "OWNER")
        then:
            Project.count() == 5
            service.authorizedInstances(uid, Project, "OWNER").size() == 3
    }

    void "authorizedInstances for specific permissions with pagination params"() {
        given: "a uid and a few projects"
            String uid = "uid"
            20.times {
                Project project = new Project(name:"pname${it}", code: "code${it}").save()
                service.authorize(uid, project, "OWNER")
            }
        expect: "max and offset work as expected"
            service.authorizedInstances(uid, Project, "OWNER").size() == 20
            service.authorizedInstances(uid, Project, "OWNER", [max:10]).size() == 10
            service.authorizedInstances(uid, Project, "OWNER", [max:10, offset:15]).size() == 5
        and: "sort and order work as expected"
            service.authorizedInstances(uid, Project, "OWNER", [sort:"name"])[0].name == "pname0"
            service.authorizedInstances(uid, Project, "OWNER", [sort:"name", order:"desc"])[0].name == "pname9"
            service.authorizedInstances(uid, Project, "OWNER", [sort:"name", order:"asc"])[0].name == "pname0"
    }

    void "isAuthorized regardless of permissions"() {
        given: "a uid and a few projects"
            String uid = "uid"
            Project project = Project.build()
        expect:
            !service.isAuthorized(uid, Project)
            !service.isAuthorized(uid, project)

        when:
            service.authorize(uid, Project, "OWNER")
        then:
            service.isAuthorized(uid, Project)
            !service.isAuthorized(uid, project)

        when:
            service.authorize(uid, project, "OWNER")
        then:
            service.isAuthorized(uid, project)

        when:
            service.deauthorize(uid, project, "OWNER")
            service.authorize(uid, project, "VIEWER")
        then:
            service.isAuthorized(uid, project)

        when:
            service.deauthorize(uid, project, "VIEWER")
            service.authorize(uid, project)
        then:
            service.isAuthorized(uid, project)
    }

}
