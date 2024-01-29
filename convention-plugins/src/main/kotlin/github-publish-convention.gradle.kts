/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 4:15 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Properties

plugins {
    id("core-publish-convention")
}

fun getExtraString(name: String) = ext[name]?.toString()

// Grabbing secrets from local.properties file or from environment variables, which could be used on CI
val secretPropsFile = project.rootProject.file("github.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply {
            load(it)
        }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["username"] = System.getenv("GITHUB_USERNAME")
    ext["token"] = System.getenv("GITHUB_PERSONAL_ACCESS_TOKEN")
}

val projectId: String by project

publishing {
    repositories {
        maven {
            // https://github.com/PayDock/android-mobile-sdk
            name = "GitHubPackages"
            // https://maven.pkg.github.com/{repository owner}/{repository}
            // url = uri("https://maven.pkg.github.com/GITHUB_USERID/REPOSITORY")
            url = uri("https://maven.pkg.github.com/Paydock/android-mobile-sdk")
            // Private Access Token - linked to a specific account (Paste token as-is, or define an environment variable to hold the token)
            credentials {
                username = getExtraString("username")
                password = getExtraString("token")
            }
        }
    }
}