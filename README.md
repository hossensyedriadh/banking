ATM/POS Switch

<p align="center">
  <strong>A learning-oriented ATM/POS switch built with Java, Spring Boot, jPOS, and PostgreSQL.</strong>
</p><p align="center">
  <img src="https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white" alt="Java 25" />
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?logo=springboot&logoColor=green" alt="Spring Boot 4" />
  <img src="https://img.shields.io/badge/jPOS-ISO%208583-1F6FEB" alt="jPOS ISO 8583" />
  <img src="https://img.shields.io/badge/PostgreSQL%2018-Database-4169E1?logo=postgresql&logoColor=blue" alt="PostgreSQL" />
  <img src="https://img.shields.io/badge/Redis%208-Distributed Coordination-DC382D?logo=redis&logoColor=red" alt="Redis 8">
  <img src="https://img.shields.io/badge/Protocol-TCP%20%2B%20ISO%208583-0A7EA4" alt="TCP and ISO 8583" />
  <img src="https://img.shields.io/badge/Project-Learning%20Simulation-8A2BE2" alt="Learning simulation" />
  <img src="https://img.shields.io/badge/Status-Active%20Development-blue?style=flat-square" alt="Status">
  <img src="https://wakatime.com/badge/user/e2b59800-befa-4fbb-b409-570cc377f0eb/project/1659303f-2426-4eb3-a6a4-e7b298491bf3.svg" alt="WakaTime Stat"/>
</p>---

Overview

ATM/POS Switch is a learning project focused on understanding how payment terminals communicate with a transaction switch and how the switch routes financial messages to a core banking system (CBS).

The project uses ISO 8583:1987-based messaging, a custom jPOS packager, raw TCP sockets, and PostgreSQL-backed transaction tracking.

The long-term goal is to build a small banking ecosystem consisting of:

- A mini core banking system.
- An ATM/POS switch.
- An HTTP API gateway.
- An identity and access management (IAM) server.
- An ATM simulator.
- Bank administration and customer-facing interfaces.

The switch is being developed incrementally, starting with message framing, ISO 8583 parsing, terminal validation, transaction routing, duplicate detection, and reversal processing.

«Scope: This is an educational simulation. Its ISO 8583 implementation is a custom learning profile, not a complete implementation of the ISO 8583 standard or a certified Visa, Mastercard, or domestic payment-network specification.»

Objectives

- Understand ISO 8583 message structures, MTIs, bitmaps, and data elements.
- Implement a TCP server that receives and responds to framed ISO 8583 messages.
- Use jPOS to pack and unpack financial messages.
- Route transactions to handlers based on message type and processing code.
- Maintain a persistent transaction lifecycle in PostgreSQL.
- Handle duplicate requests and replay previously stored responses.
- Implement transaction reversals against an original transaction.
- Integrate the switch with a separately running CBS application over HTTP.
- Eventually simulate terminal operations, payment-network routing, and banking workflows.

Technology stack

Technology| Purpose
Java 25| Primary programming language
Spring Boot 4.x| Application framework and dependency injection
jPOS| ISO 8583 message construction, packing, and unpacking
PostgreSQL| Terminal registry and persistent transaction tracking
JDBC / "JdbcTemplate"| Explicit SQL access to PostgreSQL
Gradle Kotlin DSL| Build and dependency management
TCP sockets| Terminal-to-switch communication
HTTP / REST| Planned switch-to-CBS integration
Apache HttpClient 5| Planned HTTP client for the CBS adapter

The ISO message format is defined through a custom XML configuration loaded by jPOS "GenericPackager". See the "jPOS ISO packager documentation" (https://jpos.org/docs/tutorial/iso-packager/generic-packager/).

Architecture

flowchart TD
    ATM["ATM / POS Terminal Simulator"]
    TCP["TCP Server"]
    FRAMER["Length-Prefixed Message Reader"]
    PARSER["ISO Message Parser"]
    VALIDATOR["ISO Request Validator"]
    REGISTRY["Terminal Registry"]
    TX["Transaction Service"]
    ROUTER["Transaction Router"]
    BALANCE["Balance Inquiry Handler"]
    REVERSAL["Reversal Handler"]
    CBS["Core Banking Service Adapter"]
    CORE["Core Banking System"]
    DB[("PostgreSQL")]

    ATM <-->|"TCP + ISO 8583"| TCP
    TCP --> FRAMER
    FRAMER --> PARSER
    PARSER --> VALIDATOR
    VALIDATOR --> REGISTRY
    REGISTRY --> TX
    TX <--> DB
    TX --> ROUTER
    ROUTER --> BALANCE
    ROUTER --> REVERSAL
    REVERSAL --> CBS
    CBS <-->|"HTTP / REST"| CORE
    BALANCE --> RESPONSE["ISO 8583 Response"]
    CBS --> REVERSAL
    REVERSAL --> RESPONSE
    RESPONSE --> TCP

The diagram shows the intended overall architecture. The CBS adapter and end-to-end reversal integration are part of the ongoing implementation.

Transaction flow

At a high level, a request follows this sequence:

1. The terminal sends a length-prefixed ISO 8583 message over TCP.
2. The switch reads the message frame and unpacks the ISO payload.
3. The request validator checks the supported MTI and required data elements.
4. The switch checks whether the terminal is registered and active.
5. The transaction service registers the request and detects duplicate transaction keys.
6. The router dispatches the request to the appropriate handler.
7. The handler creates the response or invokes the CBS adapter when implemented.
8. The switch packs the response and stores its payload and transaction outcome where applicable.
9. The response is returned to the terminal using the same length-prefixed TCP framing.

ISO 8583 profile

The project currently uses a custom ISO 8583:1987-based profile configured in "packager/learning-iso87.xml".

It is intentionally limited to the fields and transaction flows needed by the simulation.

Supported message types

MTI| Description| Purpose
"0200"| Financial transaction request| Request a supported financial operation
"0210"| Financial transaction response| Return the result of a "0200" request
"0400"| Reversal request| Request reversal of an original transaction
"0410"| Reversal response| Return the result of a reversal request

Currently configured data elements

Field| Name| Purpose
DE 3| Processing Code| Identifies the requested transaction type
DE 4| Amount, Transaction| Transaction amount
DE 7| Transmission Date and Time| Used in transaction identification
DE 11| Systems Trace Audit Number (STAN)| Request trace identifier
DE 37| Retrieval Reference Number (RRN)| Transaction reference
DE 39| Response Code| Indicates the transaction result
DE 41| Card Acceptor Terminal Identification| Identifies the terminal
DE 49| Currency Code, Transaction| Identifies the transaction currency
DE 54| Additional Amounts| Used by the learning balance-inquiry response
DE 90| Original Data Elements| Identifies the original transaction for reversal

The actual field encoding, lengths, and supported values are defined by the custom packager. A configured field does not imply that every standard-defined use of that field is implemented.

Example transaction conventions

- Processing code "310000" is the project's current convention for balance inquiry.
- The balance-inquiry response uses DE 54 with a project-specific representation of the simulated BDT balance.
- Reversal requests use DE 90 to identify the original transaction.
- Responses use the corresponding response MTI for the request type.

These are learning-project conventions, not universal network rules.

Implemented functionality

TCP communication

- Raw TCP socket communication.
- Two-byte, big-endian length prefix before the ISO payload.
- Maximum configured ISO payload length of 8,192 bytes.
- ISO payload parsing and response packing through jPOS.

ISO 8583 processing

- Custom "GenericPackager" configuration.
- Request validation for supported MTIs and required fields.
- Terminal identification through DE 41.
- Processing-code-based transaction routing for "0200" requests.
- MTI-based routing for "0400" reversal requests.
- Response creation for financial and reversal messages.

Terminal management

- PostgreSQL-backed terminal registry.
- Terminal identification and status validation.
- Active and disabled terminal states.

Transaction tracking

- Persistent switch transaction records.
- Transaction lifecycle statuses.
- Duplicate request detection using terminal ID, STAN, and transmission date/time.
- Stored response payloads for replaying previously completed requests.
- Processing-start timestamps.
- Transaction lookup by original transaction identifiers.
- Reversal linkage through a reference to the original switch transaction.

Reversal groundwork

- Original transaction identification using DE 90.
- Validation that the original transaction was approved.
- Prevention of multiple reversal registrations for the same original transaction through a database uniqueness constraint.
- Separate reversal handler and response MTI.
- Support for representing uncertain processing outcomes with an "UNKNOWN" transaction status.

Transaction lifecycle

The switch uses the following transaction statuses:

Status| Meaning
"RECEIVED"| Request registered by the switch
"PROCESSING"| Request processing has started
"APPROVED"| Transaction approved
"DECLINED"| Transaction declined
"FAILED"| Processing failed
"UNKNOWN"| Final outcome could not be established confidently

The lifecycle is persisted in PostgreSQL. The status and stored response payload allow the switch to distinguish completed requests from requests that are still processing or whose outcomes are uncertain.

A network timeout does not necessarily mean that the CBS operation failed. The CBS may have completed an operation even if the switch did not receive its response. The planned CBS integration therefore needs stable reversal references and idempotent handling.

Database schema

The switch currently uses two primary database tables.

"switch_terminals"

Stores the terminals known to the switch.

Column| Description
"terminal_id"| Terminal identifier and primary key
"terminal_type"| Terminal type
"status"| "ACTIVE" or "DISABLED"
"institution_code"| Associated institution code
"created_at"| Record creation timestamp

"switch_transactions"

Stores incoming requests and their processing outcomes.

Column| Description
"id"| Generated transaction identifier
"terminal_id"| Reference to the terminal
"stan"| Systems Trace Audit Number
"retrieval_reference"| Retrieval reference number
"processing_code"| Processing code from DE 3
"transaction_amount"| Transaction amount, when captured
"currency_code"| Currency code, when captured
"request_mti"| Original request MTI
"transmission_date_time"| Transmission date/time from DE 7
"response_code"| Final response code, when available
"status"| Transaction lifecycle status
"received_at"| Request registration timestamp
"processing_started_at"| Processing start timestamp
"completed_at"| Completion timestamp
"response_payload"| Packed ISO response bytes
"reversal_of_transaction_id"| Reference to the original transaction for a reversal

The database includes indexes for terminal transaction history and retrieval-reference lookups, a unique request key for terminal/STAN/transmission-date-time combinations, and a uniqueness constraint limiting each original transaction to one registered reversal.

The exact schema and migrations in the repository are the source of truth for the current implementation.

Project structure

The code follows a package-by-feature approach, with separate components for the TCP server, ISO message processing, terminal management, transaction tracking, routing, and transaction handlers.

src/
└── main/
    ├── java/
    │   └── io/github/hossensyedriadh/atm_pos_switch/
    │       ├── corebanking/
    │       │   └── CoreBankingService.java
    │       ├── terminal/
    │       │   ├── TerminalService.java
    │       │   ├── TerminalServiceImpl.java
    │       │   ├── TerminalRepository.java
    │       │   └── SwitchTerminal.java
    │       ├── transaction/
    │       │   ├── SwitchTransactionService.java
    │       │   ├── SwitchTransactionServiceImpl.java
    │       │   ├── TransactionRepository.java
    │       │   ├── TransactionRouter.java
    │       │   └── ...
    │       └── ...
    └── resources/
        ├── packager/
        │   └── learning-iso87.xml
        └── application.yml

This is a representative view of the main architectural areas, not an exhaustive listing of every source file.

Prerequisites

Before running the application, install or configure:

- JDK 25.
- PostgreSQL.
- The Gradle wrapper included with the project.
- The application configuration and database schema required by the current source.

The CBS application is a separate component. Its HTTP integration must be configured and implemented before end-to-end transactions that depend on it can work.

Configuration

Configure the PostgreSQL connection and server settings in the application's configuration file or through environment-specific overrides.

For example, the configuration will need values equivalent to:

spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

The exact property names and additional settings should match the application's existing "application.yml" and configuration classes.

Do not commit actual database passwords, credentials, or other secrets to the repository.

Running the project

1. Clone the repository

git clone <YOUR_REPOSITORY_URL>
cd atm-pos-switch

Replace the repository URL with your GitHub repository URL.

2. Configure PostgreSQL

Create a PostgreSQL database and apply the schema or migrations included in the project.

Configure the database connection using the application's supported properties or environment variables.

3. Configure the ISO packager

Ensure that "learning-iso87.xml" is available on the application's classpath under:

src/main/resources/packager/learning-iso87.xml

The packager defines how the switch encodes and decodes its supported ISO messages.

4. Run the application

On Linux or macOS:

./gradlew bootRun

On Windows:

.\gradlew.bat bootRun

The TCP listener port and other runtime settings depend on the application's current configuration.

5. Connect a terminal simulator

Send a length-prefixed ISO 8583 request to the configured TCP listener. The terminal identifier must exist in the switch database and be active.

Use a request containing the required fields for a supported MTI and transaction handler.

Example transaction: balance inquiry

The current learning profile uses processing code "310000" for balance inquiry.

A simulator can construct a "0200" request with the required fields, including:

- DE 3: "310000"
- DE 4: the amount representation required by the configured packager
- DE 7: current transmission date/time in the expected format
- DE 11: a unique six-digit STAN for the request
- DE 41: an active terminal ID, such as "ATM00001"
- DE 49: "050" for the project's BDT currency convention

The switch validates the request, registers it, routes it to the balance-inquiry handler, and constructs a "0210" response.

The example balance response includes DE 54 using the project's custom representation. It should not be interpreted as a universal ISO 8583 encoding for balances.

Planned development

The project is being developed in stages.

- [x] TCP server with length-prefixed ISO messages
- [x] Custom jPOS packager configuration
- [x] Terminal registry and status validation
- [x] Persistent transaction tracking
- [x] Duplicate request registration and response-payload storage
- [x] Balance-inquiry handler
- [x] Reversal request validation and original-transaction lookup
- [ ] Complete CBS reversal integration over HTTP
- [ ] Persist the final reversal response and status after the CBS result
- [ ] Implement ATM cash withdrawal through the CBS
- [ ] Improve handling of timeouts and ambiguous outcomes
- [ ] Extend transaction routing and supported processing codes
- [ ] Add network-management messages such as "0800" / "0810"
- [ ] Build a standalone ATM simulator
- [ ] Add an API gateway
- [ ] Build an IAM server
- [ ] Build bank administration and customer-facing interfaces

The checklist reflects the intended roadmap; it is not a claim that all planned functionality is already available.

Design principles

Keep the switch separate from the CBS

The switch manages terminal-facing messages, transaction routing, and switch-side tracking. The CBS owns the banking operations, account balances, and financial ledger postings.

Keep transaction handlers focused

The router dispatches "0200" requests by processing code and handles "0400" reversals separately by MTI. This keeps reversal-specific logic out of the ordinary processing-code handler interface.

Persist enough information to recover

The switch records transaction identifiers, lifecycle states, and packed responses. This provides a foundation for duplicate detection and response replay.

Use explicit SQL where appropriate

"JdbcTemplate" is used for direct database operations, making transaction registration, uniqueness constraints, and lifecycle updates visible in the implementation.

Learning resources

- "jPOS documentation" (https://jpos.org/docs/)
- "jPOS GenericPackager tutorial" (https://jpos.org/docs/tutorial/iso-packager/generic-packager/)
- "jPOS ISO message packing" (https://developer.jpos.org/docs/tutorial/iso-packager/)
- "ISO 8583 overview — Wikipedia" (https://en.wikipedia.org/wiki/ISO_8583)

Disclaimer

This repository is intended for learning and simulation. It does not implement every ISO 8583 data element, every transaction type, or any complete payment-network specification.

The project is not affiliated with or endorsed by Visa, Mastercard, or any bank or payment network. Brand names, where mentioned, refer to their respective owners.

---

<p align="center">
  <strong>Built to learn how banking transaction systems fit together, one message at a time.</strong>
</p>
