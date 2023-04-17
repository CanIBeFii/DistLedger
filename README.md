# DistLedger

Distributed Systems Project 2022/2023

## Authors

**Group T02**

### Code Identification

In all source files (namely in the *groupId*s of the POMs), replace __GXX__ with your group identifier. The group
identifier consists of either A or T followed by the group number - always two digits. This change is important for 
code dependency management, to ensure your code runs using the correct components and not someone else's.

### Team Members

| Number | Name              | User                                | Email                                                    |
|--------|-------------------|-------------------------------------|----------------------------------------------------------|
| 98934  | Filipe Mendes     | <https://github.com/CanIBeFii>      | <mailto:filipealexandrevidalmendes@tecnico.ulisboa.pt>   |
| 98936  | Gonçalo Goulão    | <https://github.com/goncalogoulao>  | <mailto:goncalogoulao@tecnico.ulisboa.pt>                |
| 98970  | Tiago Dinis       | <https://github.com/Tiago-Din>      | <mailto:tiago.manuel.dinis@tecnico.ulisboa.pt>           |

## Getting Started

The overall system is made up of several modules. The main server is the _DistLedgerServer_. The clients are the _User_ 
and the _Admin_. The definition of messages and services is in the _Contract_. The future naming server
is the _NamingServer_.

See the [Project Statement](https://github.com/tecnico-distsys/DistLedger) for a complete domain and system description.

### Prerequisites

The Project is configured with Java 17 (which is only compatible with Maven >= 3.8), but if you want to use Java 11 you
can too -- just downgrade the version in the POMs.

To confirm that you have them installed and which versions they are, run in the terminal:

```s
javac -version
mvn -version
```

### Installation

To compile and install all modules:

```s
mvn clean install
```

## Built With

* [Maven](https://maven.apache.org/) - Build and dependency management tool;
* [gRPC](https://grpc.io/) - RPC framework.
