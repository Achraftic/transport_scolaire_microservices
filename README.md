#  gestion du transport scolaire (Microservice 7 et 8 )

Ce document fournit les instructions pour installer et exécuter l'infrastructure centrale du projet de Gestion du Transport Scolaire. Cela inclut deux microservices critiques :

1.  **Microservice 7 : `auth-service`**
2.  **Microservice 8 : `api-gateway`**

## Aperçu de l'Architecture

Ces deux services constituent le point d'entrée et la sécurité pour l'ensemble du système. L'architecture est la suivante :

  * La **Passerelle API (MS 8)** est le **seul** service avec lequel l'application cliente (application mobile) doit communiquer. Elle agit comme porte d'entrée et s'exécute sur le port `8080`.
  * Le **Service d'Authentification (MS 7)** gère les comptes utilisateurs et les jetons de sécurité. Il s'exécute sur le port `8081` et n'est **pas** exposé au monde extérieur.
  * La Passerelle reçoit tout le trafic entrant (par ex., `/auth/login`) et le route intelligemment vers le service interne approprié (par ex., `auth-service`).

-----

## 1\. Microservice 7 : Service d'Authentification (`auth-service`)

Ce service gère toute l'authentification et l'enregistrement des utilisateurs.

  * **Projet :** `auth-service`

  * **Port :** `8081`

  * **Objectif :**

      * Fournir les points de terminaison (endpoints) pour l'enregistrement (`/register`) et la connexion (`/login`) des utilisateurs.
      * Valider les identifiants des utilisateurs.
      * Générer des Jetons Web JSON (JWT) lors d'une connexion réussie.
      * Gérer les rôles et les permissions des utilisateurs.

  * **Technologie :** Spring Boot, Spring Security, Spring Data JPA, JWT, MySQL.

  * **Configuration :**
    La connexion à la base de données est configurée dans `auth-service/src/main/resources/application.yml`. Assurez-vous que votre serveur MySQL local est en cours d'exécution et que vous avez créé le schéma `auth_db`.

    ```yaml
    server:
        port: 8081

    APP_SECRET: ${APP_SECRET}

    spring:
    application:
        name: auth-service
    datasource:
        url: jdbc:mysql://localhost:3306/auth_db
        username: root
        password:
        driver-class-name: com.mysql.cj.jdbc.Driver
    jpa:
        hibernate:
        ddl-auto: update
        show-sql: true
        properties:
        hibernate:
            dialect: org.hibernate.dialect.MySQLDialect

    ```

-----

## 2\. Microservice 8 : Passerelle API (`api-gateway`)

Ce service est le point d'entrée unique pour toutes les requêtes du client.

  * **Projet :** `api-gateway`

  * **Port :** `8080` (Port exposé publiquement)

  * **Objectif :**

      * Fournir une API unique et unifiée pour le client.
      * Router les requêtes entrantes vers le bon microservice (par ex., `auth-service`, `bus-service`, etc.).
      * Agir comme un point de contrôle de sécurité (validera les JWT à l'avenir).

  * **Technologie :** Spring Cloud Gateway (Réactif, utilise Netty).

  * **Configuration :**
    Les règles de routage sont définies dans `api-gateway/src/main/resources/application.yml`. Ce fichier indique à la passerelle où envoyer le trafic.

    ```yaml
    server:
      port: 8080

    spring:
      application:
        name: api-gateway
      cloud:
        gateway:
          server:
            webflux:
              routes:
                
                # Route vers le Service d'Authentification (MS 7)
                - id: auth-service
                  uri: http://localhost:8081
                  predicates:
                    - Path=/auth/**
                
                # Route vers le Service des Bus (MS 1)
                - id: bus-service
                  uri: http://localhost:8082
                  predicates:
                    - Path=/buses/**
                
                # ... autres routes (élèves, localisation, etc.)
    ```

-----

## Prérequis

Avant de commencer, assurez-vous d'avoir installé les éléments suivants :

  * Java JDK 17 (ou 21)
  * Apache Maven
  * Un serveur MySQL en cours d'exécution
  * Un client API (comme Postman)

-----

## Comment Exécuter

Vous devez démarrer les services dans le bon ordre.

1.  **Démarrer MySQL :** Assurez-vous que votre serveur MySQL est lancé.
2.  **Créer la base de données :** Créez manuellement le schéma dans MySQL : `CREATE DATABASE auth_db;`
3.  **Lancer le Service d'Authentification (MS 7) :**
      * Ouvrez un terminal dans le dossier racine de `auth-service`.
      * Exécutez `mvn spring-boot:run`
      * Attendez de voir qu'il a démarré sur le port `8081`.
4.  **Lancer la Passerelle API (MS 8) :**
      * Ouvrez un *nouveau* terminal dans le dossier racine de `api-gateway`.
      * Exécutez `mvn spring-boot:run`
      * Attendez de voir qu'il a démarré sur le port `8080` (il mentionnera **Netty**).

Le système est maintenant en cours d'exécution. Toutes les requêtes doivent être envoyées à `http://localhost:8080`.

-----

## Comment Tester

Utilisez Postman pour envoyer des requêtes **uniquement à la Passerelle (port 8080)**.

### Test 1 : Enregistrer un nouvel utilisateur

  * **Méthode :** `POST`

  * **URL :** `http://localhost:8080/auth/register`

  * **Corps (Body) :** (raw, JSON)

    ```json
    {
        "email": "achraf@gmail.com",
        "password": "password123",
        "role": "ADMIN"
    }
    ```

### Test 2 : Se Connecter

  * **Méthode :** `POST`

  * **URL :** `http://localhost:8080/auth/login`

  * **Corps (Body) :** (raw, JSON)

    ```json
    {
        "email": "achraf@gmail.com",
        "password": "password123"
    }
    ```

  * **Résultat :** Vous recevrez une réponse JSON contenant le `access token`, `userId`, `email`, et `role`. Ce jeton doit être sauvegardé par le client et envoyé avec toutes les futures requêtes.
  ``` json
  {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJzdWIiOiJ0aWNhY2hyYWZAZ21haWwuY29tIiwiaWF0IjoxNzYyMjg5Mzc5LCJleHAiOjE3NjIzMjUzNzl9.rI8Xqwc5JDzTiS5BDvTR0XjB_aeZYxwMgm4oOKae1J8",
    "username": "achraf@gmail.com",
    "roles": "ADMIN",
     "id": "6149cde7-5d83-45c7-8bdb-b03c386803cc"
    
}  
  ```