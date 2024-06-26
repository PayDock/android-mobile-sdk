package utils

/**
 * Extension class for configuring publishing options for a Gradle project.
 */
open class PublishingExtension {

    /**
     * The group ID of the project, typically following the reverse domain name notation (e.g., "com.example").
     * This value will be used in the Maven POM file's groupId element.
     */
    var groupId: String? = null

    /**
     * The version of the project. This value will be used in the Maven POM file's version element.
     */
    var version: String? = null

    /**
     * The artifact ID of the project. This value will be used in the Maven POM file's artifactId element.
     */
    var artifactId: String? = null

    /**
     * The project ID, which can be used to uniquely identify the project within your organization.
     */
    var projectId: String? = null

    /**
     * The URL of the project's GitHub repository. This value will be used in the Maven POM file's url element.
     */
    var projectGithubUrl: String? = null

    /**
     * A brief description of the project. This value will be used in the Maven POM file's description element.
     */
    var projectDescription: String? = null

    /**
     * The packaging option for the project (e.g., "jar", "aar"). This value will be used in the Maven POM file's packaging element.
     */
    var packagingOption: String? = null

    /**
     * Whether to include the sources JAR in the publication. Defaults to true.
     */
    var includeSources: Boolean = true
}

