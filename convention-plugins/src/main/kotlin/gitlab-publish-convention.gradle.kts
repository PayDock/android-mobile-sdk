import java.util.Properties

plugins {
    id("core-publish-convention")
}

fun getExtraString(name: String) = ext[name]?.toString()

// Grabbing secrets from local.properties file or from environment variables, which could be used on CI
val secretPropsFile = project.rootProject.file("gitlab.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply {
            load(it)
        }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["projectId"] = System.getenv("PROJECT_ID")
    ext["privateToken"] = System.getenv("PRIVATE_TOKEN")
    ext["deployToken"] = System.getenv("DEPLOY_TOKEN")
}

publishing {
    repositories {
        maven {
            name = "GitLab"
            url = uri("https://gitlab.com/api/v4/projects/${getExtraString("gitlab.projectId")}/packages/maven")
            // Private Access Token - linked to a specific account (Paste token as-is, or define an environment variable to hold the token)
            credentials(HttpHeaderCredentials::class) {
                name = "Private-Token"
                value = getExtraString("gitlab.privateToken")
            }
            // Deploy Token - linked to repo (Paste token as-is, or define an environment variable to hold the token_
//            credentials(HttpHeaderCredentials::class) {
//                name = "Deploy-Token"
//                value = getExtraString("gitlab.deployToken")
//            }
            // 	Job-Token - (System.getenv("CI_JOB_TOKEN"))
//            credentials(HttpHeaderCredentials::class) {
//                name = "Job-Token"
//                value = getExtraString("gitlab.jobToken")
//            }
            authentication {
                create("header", HttpHeaderAuthentication::class)
            }
        }
    }
}