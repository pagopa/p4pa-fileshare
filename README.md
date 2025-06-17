# p4pa-fileshare

This application belong to the **inbound/outbound** tier of the **Piattaforma Unitaria** product.

See [PU Microservice Architecture](https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1405845916/Architettura+microservizi) for more details.

## 🧱 Role

* To handle upload and download of files;
  * It authorizes requests retrieving user info through [p4pa-auth](https://github.com/pagopa/p4pa-auth);


## 🌐 APIs
See [OpenAPI](openapi/generated.openapi.json), exposed through the following path:
* `/swagger-ui/index.html`

### 📌 Relevant APIs
* `POST /organization/{organizationId}/ingestionflowfiles`: To upload an ingestion flow file and ask its processing;
* `GET /organization/{organizationId}/ingestionflowfiles/{ingestionFlowFileId}`: To download an ingestion flow file;
* `GET /organization/{organizationId}/ingestionflowfiles/{ingestionFlowFileId}/errors`: To download an error file related to an ingestion flow file processed;
* `GET /organization/{organizationId}/ingestionflowfiles/{ingestionFlowFileId}/notice`: TO download notices generated through the processing of a DP_INSTALLMENTS ingestion flow file;
* `GET /organization/{organizationId}/ingestionflowfiles/{ingestionFlowFileId}/iuv`: TO download IUVs file generated through the processing of a DP_INSTALLMENTS ingestion flow file;
* `GET /organization/{organizationId}/exportfiles/{exportFileId}`: To download a previous requested export file;
* `POST /organization/{organizationId}/send-files/{sendNotificationId}`: To upload SEND requested attachment.

## 🔎 Monitoring
See available actuator endpoints through the following path:
* `/actuator`

### 📌 Relevant endpoints
* Health (provide an accessToken to see details): `/actuator/health`
  * Liveness: `/actuator/health/liveness`
  * Readiness: `/actuator/health/readiness`
* Metrics: `/actuator/metrics`
  * Prometheus: `/actuator/prometheus`

Further endpoints are exposed through the JMX console.

## ✏️ Logging
See [log configured pattern](/src/main/resources/logback-spring.xml).

## 🔗 Dependencies

### 🗄️ Resources
* Shared folder

### 🧩 Microservices
* [p4pa-auth](https://github.com/pagopa/p4pa-auth):
  * To validate access token and retrieve user info;
* [p4pa-organization](https://github.com/pagopa/p4pa-organization):
  * 

## 🗃️ Entities handled
* `debt_position_type`
* `debt_position_type_org`
* `debt_position_type_org_operators`
* `iuv_sequence_number`
* `debt_position`
* `payment_option`
* `installment`
* `transfer`
* `receipt`

## 🔧 Configuration

See [application.yml](src/main/resources/application.yml) for each configurable property.

### 📌 Relevant configurations

#### 🌐 Application Server
| ENV         | DESCRIPTION                       | DEFAULT |
|-------------|-----------------------------------|---------|
| SERVER_PORT | Application server listening port | 8080    |

#### ✏️ Logging
| ENV                                   | DESCRIPTION                                                                                                                                                                     | DEFAULT |
|---------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|
| LOG_LEVEL_ROOT                        | Base level                                                                                                                                                                      | INFO    |
| LOG_LEVEL_PAGOPA                      | Base level of custom classes                                                                                                                                                    | INFO    |
| LOG_LEVEL_SPRING                      | Level applied to Spring framework                                                                                                                                               | INFO    |
| LOG_LEVEL_SPRING_BOOT_AVAILABILITY    | To print availability events                                                                                                                                                    | DEBUG   |
| LOGGING_LEVEL_API_REQUEST_EXCEPTION   | Level applied to APIs exception                                                                                                                                                 | INFO    |
| LOG_LEVEL_PERFORMANCE_LOG             | Level applied to [PerformanceLog](https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1540096383/Logging#2.2.-Log-di-performance)                                               | INFO    |
| LOG_LEVEL_PERFORMANCE_LOG_API_REQUEST | Level applied to [API Performance Log](https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1540096383/Logging#2.2.2.1.-Log-di-perfomance-per-le-API)                            | INFO    |
| LOG_LEVEL_PERFORMANCE_LOG_REST_INVOKE | Level applied to [REST invoke Performance Log](https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1540096383/Logging#2.2.2.2.-Log-di-performance-per-i-servizi-REST-integrati) | INFO    |

#### 🔁 Integrations

##### 🗄️ Resources
| ENV                        | DESCRIPTION                                                                           | DEFAULT                                                                                                                      |
|----------------------------|---------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------|
| REDIS_HOST                 | Redis server host                                                                     | localhost                                                                                                                    |
| REDIS_PORT                 | Redis server port                                                                     | 6380                                                                                                                         |
| REDIS_PASSWORD             | Redis password                                                                        |                                                                                                                              |
| SHOW_SQL                   | To print SQL statements                                                               | false                                                                                                                        |
| DEBT_POSITIONS_DB_URL      | PostgreSQL connection string (to use in order to customize the entire string)         | jdbc:postgresql://${CLASSIFICATION_DB_HOST}:${CLASSIFICATION_DB_PORT}/${CLASSIFICATION_DB_NAME}?currentSchema=debt_positions |
| DEBT_POSITIONS_DB_HOST     | PostgreSQL Host                                                                       | localhost                                                                                                                    |
| DEBT_POSITIONS_DB_PORT     | PostgreSQL port                                                                       | 5432                                                                                                                         |
| DEBT_POSITIONS_DB_NAME     | PostgreSQL Database name                                                              | payhub                                                                                                                       |
| DEBT_POSITIONS_DB_USER     | PostgreSQL username                                                                   |                                                                                                                              |
| DEBT_POSITIONS_DB_PASSWORD | PostgreSQL password                                                                   |                                                                                                                              |
| CITIZENDB_URL              | Citizen PostgreSQL connection string (to use in order to customize the entire string) | jdbc:postgresql://${CITIZENDB_HOST}:${CITIZENDB_PORT}/citizen                                                                |
| CITIZENDB_HOST             | Citizen PostgreSQL Host                                                               | localhost                                                                                                                    |
| CITIZENDB_PORT             | Citizen PostgreSQL port                                                               | 5432                                                                                                                         |
| CITIZENDB_NAME             | Citizen PostgreSQL Database name                                                      | payhub                                                                                                                       |
| CITIZENDB_USER             | Citizen PostgreSQL username                                                           |                                                                                                                              |
| CITIZENDB_PASSWORD         | Citizen PostgreSQL password                                                           |                                                                                                                              |

##### 📋 [Caching](https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1542128077/Caching)
| ENV                        | DESCRIPTION                                 | DEFAULT |
|----------------------------|---------------------------------------------|---------|
| CACHE_PII_SIZE             | PII cache size                              | 1000    |
| CACHE_PII_MINUTES          | PII cache retention (minutes)               | 60      |
| CACHE_ORGANIZATION_SIZE    | Organization data cache size                | 100     |
| CACHE_ORGANIZATION_MINUTES | Organization data cache retention (minutes) | 60      |
| CACHE_TAXONOMY_SIZE        | Taxonomy data cache size                    | 100     |
| CACHE_TAXONOMY_MINUTES     | Taxonomy data cache retention (minutes)     | 60      |

##### 🔗 REST
| ENV                                               | DESCRIPTION                               | DEFAULT |
|---------------------------------------------------|-------------------------------------------|---------|
| DEFAULT_REST_CONNECTION_POOL_SIZE                 | Default connection pool size              | 10      |
| DEFAULT_REST_CONNECTION_POOL_SIZE_PER_ROUTE       | Default connection pool size per route    | 5       |
| DEFAULT_REST_CONNECTION_POOL_TIME_TO_LIVE_MINUTES | Default connection pool TTL (minutes)     | 10      |
| DEFAULT_REST_TIMEOUT_CONNECT_MILLIS               | Default connection timeout (milliseconds) | 120000  |
| DEFAULT_REST_TIMEOUT_READ_MILLIS                  | Default read timeout (milliseconds)       | 120000  |

##### 🧩 Microservices
| ENV                                  | DESCRIPTION                                                                       | DEFAULT |
|--------------------------------------|-----------------------------------------------------------------------------------|---------|
| ORGANIZATION_BASE_URL                | Organization microservice URL                                                     |         |
| ORGANIZATION_MAX_ATTEMPTS            | Organization API max attempts                                                     | 3       |
| ORGANIZATION_WAIT_TIME_MILLIS        | Organization retry waiting time (milliseconds)                                    | 500     |
| ORGANIZATION_PRINT_BODY_WHEN_ERROR   | To print body when an error occurs                                                | true    |
| WORKFLOW_HUB_BASE_URL                | WorkflowHub microservice URL                                                      |         |
| WORKFLOW_HUB_MAX_ATTEMPTS            | WorkflowHub API max attempts                                                      | 3       |
| WORKFLOW_HUB_WAIT_TIME_MILLIS        | WorkflowHub retry waiting time (milliseconds)                                     | 500     |
| WORKFLOW_HUB_PRINT_BODY_WHEN_ERROR   | To print body when an error occurs                                                | true    |
| WORKFLOW_AWAIT_MAX_WAITING_MINUTES   | Max time to wait for synchronization workflow termination (minutes)               | 5       |
| WORKFLOW_AWAIT_RETRY_DELAYS_MS       | Time between checks to verify synchronization workflow termination (milliseconds) | 1000    |
| CLASSIFICATION_BASE_URL              | Classification microservice URL                                                   |         |
| CLASSIFICATION_MAX_ATTEMPTS          | Classification API max attempts                                                   | 3       |
| CLASSIFICATION_WAIT_TIME_MILLIS      | Classification retry waiting time (milliseconds)                                  | 500     |
| CLASSIFICATION_PRINT_BODY_WHEN_ERROR | To print body when an error occurs                                                | true    |

##### 🌀 KAFKA
| ENV                                              | DESCRIPTION                                                        | DEFAULT                    |
|--------------------------------------------------|--------------------------------------------------------------------|----------------------------|
| KAFKA_BINDER_BROKER                              | Comma separated list of brokers to which the Kafka binder connects |                            |
| KAFKA_CONFIG_HEARTBEAT_INTERVAL_MS               | Hearth beat interval (milliseconds)                                | 3000                       |
| KAFKA_CONFIG_SESSION_TIMEOUT_MS                  | Session timeout (milliseconds)                                     | 30000                      |
| KAFKA_CONFIG_REQUEST_TIMEOUT_MS                  | Request timeout (milliseconds)                                     | 60000                      |
| KAFKA_CONFIG_METADATA_MAX_AGE                    | Metadata max age (milliseconds)                                    | 180000                     |
| KAFKA_CONFIG_SASL_MECHANISM                      | SASL mechanism                                                     |                            |
| KAFKA_CONFIG_SECURITY_PROTOCOL                   | Security protocol                                                  |                            |
| KAFKA_CONFIG_MAX_REQUEST_SIZE                    | Max request size                                                   | 1000000                    |

###### 📤 KAFKA PRODUCERS
| ENV                                              | DESCRIPTION                                                        | DEFAULT                    |
|--------------------------------------------------|--------------------------------------------------------------------|----------------------------|
| KAFKA_TOPIC_PAYMENTS                             | Topic where to publish payment event                               | p4pa-payhub-payments-evh   |
| KAFKA_PAYMENTS_PRODUCER_SASL_JAAS_CONFIG         | JAAS Config string used to perform authentication                  |                            |
| KAFKA_PAYMENTS_PRODUCER_CONNECTION_MAX_IDLE_TIME | Max producer idle time (milliseconds)                              | 180000                     |
| KAFKA_PAYMENTS_PRODUCER_RETRY_MS                 | Producer retry waiting time (milliseconds)                         | \${KAFKA_RETRY_MS:10000}   |
| KAFKA_PAYMENTS_PRODUCER_LINGER_MS                | Producer linger time (milliseconds)                                | \${KAFKA_LINGER_MS:2}      |
| KAFKA_PAYMENTS_PRODUCER_BATCH_SIZE               | Producer batch size                                                | \${KAFKA_BATCH_SIZE:16384} |

#### 💼 Business logic
| ENV                                        | DESCRIPTION                                                          | DEFAULT                            |
|--------------------------------------------|----------------------------------------------------------------------|------------------------------------|
| DATA_EXPORT_MAX_TOTAL_ELEMENTS             | Maximum number of elements that could be exported                    | 100000                             |
| DATA_EXPORT_MAX_MONTHS_INTERVAL            | Maximum number of months that could be exported                      | 6                                  |                                          
| INSTALLMENT_PAID_VIEW_MAX_TOTAL_ELEMENTS   | Paid Installments: Maximum number of elements that could be exported | ${DATA_EXPORT_MAX_TOTAL_ELEMENTS}  |
| INSTALLMENT_PAID_VIEW_MAX_MONTHS_INTERVAL  | Paid Installments: Maximum number of months that could be exported   | ${DATA_EXPORT_MAX_MONTHS_INTERVAL} |
| RECEIPT_ARCHIVING_VIEW_MAX_TOTAL_ELEMENTS  | Receipt archiving: Maximum number of elements that could be exported | ${DATA_EXPORT_MAX_TOTAL_ELEMENTS}  |
| RECEIPT_ARCHIVING_VIEW_MAX_MONTHS_INTERVAL | Receipt archiving: Maximum number of months that could be exported   | ${DATA_EXPORT_MAX_MONTHS_INTERVAL} |
| FEATURE_ORGANIZATION_PIVA_CHECK            | To enable Organization tax code check                                | true                               |

#### 🔑 keys
| ENV                          | DESCRIPTION                                         | DEFAULT |
|------------------------------|-----------------------------------------------------|---------|
| JWT_TOKEN_PUBLIC_KEY         | p4pa-auth JWT public key                            |         |
| DATA_CIPHER_HASH_PEPPER      | Base64 encoded key (256 bit) used to calculate hash |         |
| DATA_CIPHER_ENCRYPT_PASSWORD | Base64 encoded key (256 bit) used to encrypt data   |         |

## 🛠️ Getting Started

### 📝 Prerequisites

Ensure the following tools are installed on your machine:

1. **Java 21+**
2. **Gradle** (or use the Gradle wrapper included in the repository)
3. **Docker** (to build and run on an isolated environment, optional)

### 🔐 Write Locks

```sh
./gradlew dependencies --write-locks
```

### ⚙️ Build

```sh
./gradlew clean build
```

### 🧪 Test

#### 📌 JUnit
```sh
./gradlew test
```

### 🚀 Run local

```sh
./gradlew bootRun
```

### 🐳 Build & run through Docker
```sh
docker build -t <APP_NAME> .
docker run --env-file <ENV_FILE> <APP_NAME>
```
