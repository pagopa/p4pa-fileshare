import org.gradle.kotlin.dsl.register
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import java.util.*

plugins {
  java
  id("org.springframework.boot") version "3.5.6"
  id("io.spring.dependency-management") version "1.1.7"
  jacoco
  id("org.sonarqube") version "6.3.1.5724"
  id("com.github.ben-manes.versions") version "0.52.0"
  id("org.openapi.generator") version "7.15.0"
  id("org.ajoberstar.grgit") version "5.3.2"
  id("com.gorylenko.gradle-git-properties") version "2.5.3"
}

group = "it.gov.pagopa.payhub"
version = "0.0.1"
description = "p4pa-fileshare"

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
}

configurations {
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
}

repositories {
  mavenCentral()
}

val springDocOpenApiVersion = "2.8.13"
val janinoVersion = "3.1.12"
val openApiToolsVersion = "0.2.7"
val micrometerVersion = "1.5.4"
val httpClientVersion = "5.5"

dependencies {
  implementation("org.springframework.boot:spring-boot-starter")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("io.micrometer:micrometer-tracing-bridge-otel:$micrometerVersion")
  implementation("io.micrometer:micrometer-registry-prometheus")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocOpenApiVersion")
  implementation("org.codehaus.janino:janino:$janinoVersion")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("org.openapitools:jackson-databind-nullable:$openApiToolsVersion")
  implementation("org.apache.httpcomponents.client5:httpclient5:$httpClientVersion")

  compileOnly("org.projectlombok:lombok")
  annotationProcessor("org.projectlombok:lombok")
  testAnnotationProcessor("org.projectlombok:lombok")

  //	Testing
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.mockito:mockito-core")
  testImplementation("org.projectlombok:lombok")
}

tasks.withType<Test> {
  useJUnitPlatform()
  finalizedBy(tasks.jacocoTestReport)
}

val mockitoAgent = configurations.create("mockitoAgent")
dependencies {
  mockitoAgent("org.mockito:mockito-core") { isTransitive = false }
}
tasks {
  test {
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
  }
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    xml.required = true
  }
}

val projectInfo = mapOf(
  "artifactId" to project.name,
  "version" to project.version
)

tasks {
  val processResources by getting(ProcessResources::class) {
    filesMatching("**/application.yml") {
      expand(projectInfo)
    }
  }
}

configurations {
  compileClasspath {
    resolutionStrategy.activateDependencyLocking()
  }
}

tasks.compileJava {
  dependsOn("dependenciesBuild")
}

tasks.register("dependenciesBuild") {
  group = "AutomaticallyGeneratedCode"
  description = "grouping all together automatically generate code tasks"

  dependsOn(
    "openApiGenerateFILESHARE",
    "openApiGenerateP4PAAUTH",
    "openApiGenerateORGANIZATION",
    "openApiGeneratePROCESSEXECUTION",
    "openApiGenerateP4PASENDNOTIFICATION",
    "openApiGeneratePAGOPAPAYMENTS",
    "openApiGenerateDEBTPOSITIONS"
  )
}

configure<SourceSetContainer> {
  named("main") {
    java.srcDir("$projectDir/build/generated/src/main/java")
  }
}

springBoot {
  buildInfo()
  mainClass.value("it.gov.pagopa.pu.fileshare.FileShareApplication")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerateFILESHARE") {
  group = "openapi"
  description = "description"

  generatorName.set("spring")
  inputSpec.set("$rootDir/openapi/p4pa-fileshare.openapi.yaml")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.pu.fileshare.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.fileshare.dto.generated")
  typeMappings.set(mapOf(
    "StartNotificationResponse" to "it.gov.pagopa.pu.sendnotification.dto.generated.StartNotificationResponse",
    "SignedUrlResultDTO" to "it.gov.pagopa.pu.pagopapayments.dto.generated.SignedUrlResultDTO"
  ))
  configOptions.set(mapOf(
    "dateLibrary" to "java8",
    "requestMappingMode" to "api_interface",
    "useSpringBoot3" to "true",
    "interfaceOnly" to "true",
    "useTags" to "true",
    "useBeanValidation" to "true",
    "generateConstructorWithAllArgs" to "true",
    "generatedConstructorWithRequiredArgs" to "true",
    "enumPropertyNaming" to "original",
    "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)",
    "serializationLibrary" to "jackson"
  ))
}


var targetEnv = when (Objects.requireNonNullElse(System.getProperty("targetBranch"), grgit.branch.current().name)) {
  "uat" -> "uat"
  "main" -> "main"
  else -> "develop"
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerateP4PAAUTH") {
  group = "openapi"
  description = "description"

  generatorName.set("java")
  remoteInputSpec.set("https://raw.githubusercontent.com/pagopa/p4pa-auth/refs/heads/$targetEnv/openapi/p4pa-auth.openapi.yaml")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.pu.p4paauth.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.p4paauth.dto.generated")
  configOptions.set(mapOf(
    "swaggerAnnotations" to "false",
    "openApiNullable" to "false",
    "dateLibrary" to "java8",
    "serializableModel" to "true",
    "useSpringBoot3" to "true",
    "useJakartaEe" to "true",
    "useOneOfInterfaces" to "true",
    "useBeanValidation" to "true",
    "serializationLibrary" to "jackson",
    "generateSupportingFiles" to "true",
    "generateConstructorWithAllArgs" to "true",
    "generatedConstructorWithRequiredArgs" to "true",
    "enumPropertyNaming" to "original",
    "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
  ))
  library.set("resttemplate")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerateORGANIZATION") {
  group = "openapi"
  description = "description"

  generatorName.set("java")
  remoteInputSpec.set("https://raw.githubusercontent.com/pagopa/p4pa-organization/refs/heads/$targetEnv/openapi/generated.openapi.json")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.pu.p4paorganization.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.p4paorganization.dto.generated")
  configOptions.set(mapOf(
    "swaggerAnnotations" to "false",
    "openApiNullable" to "false",
    "dateLibrary" to "java8",
    "serializableModel" to "true",
    "useSpringBoot3" to "true",
    "useJakartaEe" to "true",
    "useOneOfInterfaces" to "true",
    "useBeanValidation" to "true",
    "serializationLibrary" to "jackson",
    "generateSupportingFiles" to "true",
    "generateConstructorWithAllArgs" to "true",
    "generatedConstructorWithRequiredArgs" to "true",
    "enumPropertyNaming" to "original",
    "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
  ))
  library.set("resttemplate")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGeneratePROCESSEXECUTION") {
  group = "openapi"
  description = "description"

  generatorName.set("java")
  remoteInputSpec.set("https://raw.githubusercontent.com/pagopa/p4pa-process-executions/refs/heads/$targetEnv/openapi/generated.openapi.json")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.pu.p4paprocessexecutions.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.p4paprocessexecutions.dto.generated")
  typeMappings.set(mapOf(
    "LocalDateTime" to "java.time.LocalDateTime"
  ))
  configOptions.set(mapOf(
    "swaggerAnnotations" to "false",
    "openApiNullable" to "false",
    "dateLibrary" to "java8",
    "serializableModel" to "true",
    "useSpringBoot3" to "true",
    "useJakartaEe" to "true",
    "useBeanValidation" to "true",
    "serializationLibrary" to "jackson",
    "generateSupportingFiles" to "true",
    "generateConstructorWithAllArgs" to "true",
    "generatedConstructorWithRequiredArgs" to "true",
    "enumPropertyNaming" to "original",
    "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
  ))
  library.set("resttemplate")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerateP4PASENDNOTIFICATION") {
  group = "openapi"
  description = "description"

  generatorName.set("java")
  remoteInputSpec.set("https://raw.githubusercontent.com/pagopa/p4pa-send-notification/refs/heads/$targetEnv/openapi/generated.openapi.json")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.pu.sendnotification.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.sendnotification.dto.generated")
  configOptions.set(mapOf(
    "swaggerAnnotations" to "false",
    "openApiNullable" to "false",
    "dateLibrary" to "java8",
    "serializableModel" to "true",
    "useSpringBoot3" to "true",
    "useJakartaEe" to "true",
    "useOneOfInterfaces" to "true",
    "useBeanValidation" to "true",
    "serializationLibrary" to "jackson",
    "generateSupportingFiles" to "true",
    "generateConstructorWithAllArgs" to "true",
    "generatedConstructorWithRequiredArgs" to "true",
    "enumPropertyNaming" to "original",
    "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
  ))
  library.set("resttemplate")
}

tasks.register<GenerateTask>("openApiGeneratePAGOPAPAYMENTS") {
  group = "AutomaticallyGeneratedCode"
  description = "openapi"

  generatorName.set("java")
  remoteInputSpec.set("https://raw.githubusercontent.com/pagopa/p4pa-pagopa-payments/refs/heads/$targetEnv/openapi/p4pa-pagopa-payments.openapi.yaml")
  outputDir.set("$projectDir/build/generated")
  invokerPackage.set("it.gov.pagopa.pu.pagopapayments.generated")
  apiPackage.set("it.gov.pagopa.pu.pagopapayments.client.generated")
  modelPackage.set("it.gov.pagopa.pu.pagopapayments.dto.generated")
  typeMappings.set(mapOf(
    "DebtPositionDTO" to "String",
    "NoticeGenerationMassiveResourceDTO" to "String",
  ))
  configOptions.set(mapOf(
    "swaggerAnnotations" to "false",
    "openApiNullable" to "false",
    "dateLibrary" to "java8",
    "serializableModel" to "true",
    "useSpringBoot3" to "true",
    "useJakartaEe" to "true",
    "useOneOfInterfaces" to "true",
    "useBeanValidation" to "true",
    "serializationLibrary" to "jackson",
    "generateSupportingFiles" to "true",
    "generateConstructorWithAllArgs" to "true",
    "generatedConstructorWithRequiredArgs" to "true",
    "enumPropertyNaming" to "original",
    "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
  ))
  library.set("resttemplate")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerateDEBTPOSITIONS") {
  group = "openapi"
  description = "description"

  generatorName.set("java")
  remoteInputSpec.set("https://raw.githubusercontent.com/pagopa/p4pa-debt-positions/refs/heads/$targetEnv/openapi/generated.openapi.json")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.pu.debtpositions.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.debtpositions.dto.generated")
  typeMappings.set(mapOf(
    "LocalDateTime" to "java.time.LocalDateTime"
  ))
  configOptions.set(mapOf(
    "swaggerAnnotations" to "false",
    "openApiNullable" to "false",
    "dateLibrary" to "java8",
    "serializableModel" to "true",
    "useSpringBoot3" to "true",
    "useJakartaEe" to "true",
    "useOneOfInterfaces" to "true",
    "useBeanValidation" to "true",
    "serializationLibrary" to "jackson",
    "generateSupportingFiles" to "true",
    "generateConstructorWithAllArgs" to "true",
    "generatedConstructorWithRequiredArgs" to "true",
    "enumPropertyNaming" to "original",
    "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
  ))
  additionalProperties.set(mapOf(
    "removeEnumValuePrefix" to "false"
  ))
  library.set("resttemplate")
}
