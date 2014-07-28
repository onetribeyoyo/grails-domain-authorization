grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.work.dir = "target/work"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.fork = [
    // configure settings for compilation JVM, note that if you alter the Groovy version forked compilation is required
    //  compile: [maxMemory: 256, minMemory: 64, debug: false, maxPerm: 256, daemon:true],

    test: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
    run: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
    war: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
    console: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256]
]

// TODO: can we set grails.reload.enabled per environment in BuildConfig?

grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
    }
    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve
    legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

    repositories {
        inherits true // Whether to inherit repository definitions from plugins

        grailsPlugins()
        grailsHome()
        mavenLocal()
        grailsCentral()
        mavenCentral()
        // uncomment these (or add new ones) to enable remote dependency resolution from public Maven repositories
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }

    dependencies {
        runtime 'mysql:mysql-connector-java:5.1.28'
    }

    plugins {
        //~ Core Plugins -------------------------------------------------------------
        // plugins for the build system only
        build ":tomcat:7.0.47"

        // plugins for the compile step
        compile ":scaffolding:2.0.1"
        compile ':cache:1.1.1'

        //~ Resources ----------------------------------------------------------------
        runtime ":resources:1.2.1"
        //runtime ":gsp-resources:0.4.4" // NOTE: gsp-resources plugin requires groovy be available on the path at server startup time!
        //runtime ":zipped-resources:1.0.1"
        //runtime ":cached-resources:1.1"
        //runtime ":yui-minify-resources:0.1.5"

        //~ Persistence --------------------------------------------------------------
        runtime ":hibernate:3.6.10.6" // or ":hibernate4:4.1.11.6"
        runtime ":database-migration:1.3.8"

        //~ Security ----------------------------------------------------------------
        compile ":spring-security-core:1.2.7.3"

        //~ UI -----------------------------------------------------------------------
        runtime ":jquery:1.10.2.2"
        compile ":twitter-bootstrap:3.0.3"
        compile ":lesscss-resources:1.3.3"

        //~ Misc Plugins -------------------------------------------------------------
        //compile ":csv:0.3.1"
        //compile ":export:1.5"
        //compile ":joda-time:1.4"
        //compile ":remote-pagination:0.4.6"

        //~ Testing ------------------------------------------------------------------
        //compile ":build-test-data:2.0.9"

        //~ Code Quality -------------------------------------------------------------
        compile ":codenarc:0.20"     // generate report with `grails codenarc`
        compile ":gmetrics:0.3.1"    // generate report with `grails gmetrics`
        test ":code-coverage:1.2.7"  // generate report with `grails test-app -coverage`
    }

}


//~ cobertura config ---------------------------------------------------------
coverage = {
    enabledByDefault = false
    exclusions = [
    ]
}

//~ codenarc config ----------------------------------------------------------
codenarc {
    reports = {
        xmlReport("xml") {
            outputFile = "target/test-reports/CodeNarc-Report.xml"
        }
        htmlReport("html") {
            outputFile = "target/test-reports/CodeNarc-Report.html"
        }
    }
    ruleSetFiles = [
        //"all",

        // grails plugin default rulesets...
        "basic",
        "exceptions",
        "grails",
        "imports",
        "unused",

        // additional rulesets...
        //"braces",
        //"concurrency",
        //"convention",
        //"design",
        //"dry",
        //"formatting",
        //"generic",
        //"groovyism",
        //"jdbc",
        //"junit",
        //"logging",
        //"naming",
        //"security",
        //"serialization",
        //"size",
        //"unnecessary",
    ].collect { "file:codenarc/${it}.groovy" }
}
