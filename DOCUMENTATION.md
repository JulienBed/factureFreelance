# Documentation - Facture Freelance

## 📋 Table des matières

- [Vue d'ensemble](#vue-densemble)
- [Architecture](#architecture)
- [Fonctionnalités](#fonctionnalités)
- [Modèles de données](#modèles-de-données)
- [API REST](#api-rest)
- [Internationalisation](#internationalisation)
- [Configuration](#configuration)
- [Développement](#développement)

---

## Vue d'ensemble

**Facture Freelance** est une application SaaS de gestion de facturation pour freelances et petites entreprises. Elle permet de gérer les clients, créer et suivre des factures, et automatiser les rappels de paiement.

### Technologies utilisées

#### Backend
- **Quarkus** (Java) - Framework moderne pour applications cloud-native
- **PostgreSQL** - Base de données relationnelle
- **Hibernate ORM** - Mapping objet-relationnel
- **JWT** - Authentification par tokens
- **Mailer** - Envoi d'emails (OTP, rappels)
- **Scheduler** - Tâches planifiées (rappels automatiques)

#### Frontend
- **Vue.js 3** - Framework JavaScript progressif
- **TypeScript** - Typage statique
- **Pinia** - Gestion d'état
- **Vue Router** - Navigation
- **vue-i18n** - Internationalisation
- **Tailwind CSS** - Framework CSS utilitaire
- **Reform Design System** - Système de design appliqué

---

## Architecture

### Structure du projet

```
factureFreelance/
├── backend/                          # Application Quarkus
│   ├── src/main/java/com/facture/
│   │   ├── domain/                  # 🎯 Domain Layer (DDD)
│   │   │   ├── event/              # Événements métier
│   │   │   ├── repository/         # Interfaces repository
│   │   │   └── service/            # Services domaine
│   │   ├── application/             # 🎯 Application Layer (DDD)
│   │   │   └── service/            # Services applicatifs
│   │   ├── infrastructure/          # 🎯 Infrastructure Layer (DDD)
│   │   │   └── repository/         # Implémentations repository
│   │   ├── event/                   # Event listeners
│   │   │   └── listener/           # Listeners async
│   │   ├── scheduler/               # Jobs planifiés
│   │   ├── entity/                  # Entités JPA
│   │   ├── dto/                     # Data Transfer Objects
│   │   ├── resource/                # Endpoints REST
│   │   ├── service/                 # Services (legacy + PDF, etc.)
│   │   └── util/                    # Utilitaires
│   └── src/main/resources/
│       ├── application.properties
│       └── db/migration/            # Migrations Flyway
│
└── frontend/                         # Application Vue.js
    ├── src/
    │   ├── components/              # Composants réutilisables
    │   ├── views/                   # Pages de l'application
    │   ├── stores/                  # Stores Pinia
    │   ├── router/                  # Configuration des routes
    │   ├── i18n.ts                  # Configuration i18n
    │   └── locales/                 # Fichiers de traduction
    └── package.json
```

> 📖 **Architecture DDD complète**: Voir [ARCHITECTURE_DDD.md](./ARCHITECTURE_DDD.md) pour la documentation détaillée de l'architecture Domain-Driven Design.

### Architecture technique

```
┌────────────────────────────────────────────────────────┐
│                 Frontend (Vue.js)                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐            │
│  │  Views   │  │  Stores  │  │  Router  │            │
│  └──────────┘  └──────────┘  └──────────┘            │
└─────────────────────┬──────────────────────────────────┘
                      │ HTTP/REST + JWT
┌─────────────────────▼──────────────────────────────────┐
│              Backend (Quarkus + DDD)                   │
│                                                         │
│  ┌──────────────┐                                      │
│  │  Resources   │ (API Layer)                          │
│  └──────┬───────┘                                      │
│         │                                               │
│  ┌──────▼──────────────┐                               │
│  │ Application Service │ (Orchestration + Events)      │
│  └──────┬──────────────┘                               │
│         │                                               │
│  ┌──────▼──────────────┐                               │
│  │  Domain Service     │ (Business Logic)              │
│  └──────┬──────────────┘                               │
│         │                                               │
│  ┌──────▼──────────────┐         ┌──────────────┐     │
│  │    Repository       │────────▶│  PostgreSQL  │     │
│  └─────────────────────┘         │  + OpenSearch│     │
│                                   └──────────────┘     │
│                                                         │
│  ┌──────────────────────────────────────────┐         │
│  │         Event Listeners (Async)          │         │
│  │  • Emails  • Reminders  • Stats          │         │
│  └──────────────────────────────────────────┘         │
│                                                         │
│  ┌──────────────────────────────────────────┐         │
│  │      Scheduled Jobs (Quarkus Scheduler)  │         │
│  │  • Relances auto  • Cleanup OTP          │         │
│  │  • Stats nocturnes  • Factures en retard │         │
│  └──────────────────────────────────────────┘         │
│       │              ├──────▶ Email Service   │
│       │              │                         │
│       │              ├──────▶ OpenSearch      │
│       │              │        (PDF Indexing)   │
│       │              │                         │
│       └──────────────────────▶ Scheduler       │
└─────────────────────────────────────────────────┘
```

---

## Fonctionnalités

### 1. 🔐 Authentification avec OTP

#### Inscription
- Création de compte avec prénom, nom, email et mot de passe
- Envoi automatique d'un code OTP à 6 chiffres par email
- Vérification OTP avec expiration (5 minutes par défaut)
- Génération automatique de tokens JWT (access + refresh)

#### Connexion
- Authentification par email/mot de passe
- Gestion des erreurs (401 pour credentials invalides, 404 pour utilisateur non trouvé)
- Messages d'erreur localisés

#### Mode développement
- **OTP fixe en dev** : Le code OTP est toujours `123456` en mode développement
- Configuration via `otp.dev.enabled=true` (activé automatiquement avec le profil `%dev`)
- Facilite les tests locaux sans configuration SMTP

**Fichiers concernés :**
- Backend : `AuthResource.java`, `AuthService.java`, `OtpUtil.java`
- Frontend : `LoginView.vue`, `RegisterView.vue`, `VerifyOtpView.vue`
- Store : `auth.ts`

### 2. 👥 Gestion des clients

#### Liste des clients
- Affichage de tous les clients de l'utilisateur connecté
- Informations : nom d'entreprise, email, téléphone, adresse
- État vide avec message d'invitation à créer le premier client
- Navigation vers création/édition

#### Création/Édition de client
- Formulaire complet avec validation
- Champs :
  - Nom de l'entreprise (requis)
  - Email (requis)
  - Téléphone
  - Adresse (rue, code postal, ville)
  - SIRET
- Sauvegarde avec gestion d'erreurs

**Endpoints API :**
- `GET /api/clients` - Liste des clients
- `GET /api/clients/{id}` - Détails d'un client
- `POST /api/clients` - Créer un client
- `PUT /api/clients/{id}` - Modifier un client
- `DELETE /api/clients/{id}` - Supprimer un client

**Fichiers concernés :**
- Backend : `ClientResource.java`, `ClientService.java`, `Client.java`
- Frontend : `ClientsListView.vue`, `ClientFormView.vue`
- Store : `clients.ts`

### 3. 📄 Gestion des factures

#### Liste des factures
- Affichage de toutes les factures
- Informations visibles :
  - Numéro de facture
  - Client associé
  - Montant total (TTC)
  - Statut avec badge coloré
- Filtrage et tri
- État vide avec invitation

#### Création/Édition de facture
- Sélection du client (requis)
- Statut : Brouillon, Envoyée, Payée, En retard, Annulée
- Dates d'émission et d'échéance
- **Lignes de facture** :
  - Description du service/produit
  - Quantité
  - Prix unitaire HT
  - Taux de TVA (%)
  - Calcul automatique des totaux
  - Ajout/suppression de lignes dynamique
- Notes additionnelles
- Calcul automatique :
  - Total HT
  - Total TVA
  - Total TTC

#### Statuts de facture
- **DRAFT** (Brouillon) : Facture en cours de création
- **SENT** (Envoyée) : Envoyée au client
- **PAID** (Payée) : Paiement reçu
- **OVERDUE** (En retard) : Date d'échéance dépassée
- **CANCELLED** (Annulée) : Facture annulée

#### Génération PDF
- Service de génération de PDF pour les factures
- Format professionnel avec toutes les informations
- Téléchargement disponible

**Endpoints API :**
- `GET /api/invoices` - Liste des factures
- `GET /api/invoices/{id}` - Détails d'une facture
- `POST /api/invoices` - Créer une facture
- `PUT /api/invoices/{id}` - Modifier une facture
- `DELETE /api/invoices/{id}` - Supprimer une facture
- `GET /api/invoices/{id}/pdf` - Générer PDF

**Fichiers concernés :**
- Backend : `InvoiceResource.java`, `InvoiceService.java`, `Invoice.java`, `InvoiceItem.java`, `PdfService.java`
- Frontend : `InvoicesListView.vue`, `InvoiceFormView.vue`
- Store : `invoices.ts`

### 4. 🔔 Rappels automatiques

- Entité `Reminder` pour gérer les rappels de paiement
- Scheduler configuré pour exécuter des tâches planifiées
- Envoi automatique d'emails de relance pour factures impayées
- Configuration via `quarkus.scheduler.enabled=true`

**Fichiers concernés :**
- Backend : `Reminder.java`, `EmailService.java`

### 5. 🔍 Recherche plein texte avec OpenSearch

#### Indexation automatique des PDFs
- **Indexation automatique** : Chaque PDF généré est automatiquement indexé dans OpenSearch
- **Extraction de texte** : Utilisation d'Apache Tika pour extraire le contenu textuel des PDFs
- **Analyse française** : Analyseur français pour améliorer la pertinence de la recherche
- **Métadonnées indexées** :
  - Numéro de facture
  - Nom du client
  - Statut
  - Montants
  - Dates (émission, échéance)
  - Contenu complet du PDF

#### Recherche avancée
- **Multi-champs** : Recherche simultanée dans le numéro, client, et contenu PDF
- **Fuzzy search** : Correction automatique des fautes de frappe
- **Filtrage utilisateur** : Isolation des résultats par utilisateur
- **Limite de résultats** : 100 résultats maximum par requête

#### Infrastructure
- **OpenSearch 2.11.1** : Moteur de recherche distribué
- **OpenSearch Dashboards** : Interface de visualisation (port 5601)
- **Index** : `invoices` avec mapping optimisé pour les factures
- **Sécurité** : Désactivée en développement, à activer en production

**Endpoints API :**
- `GET /api/invoices/search?q={query}` - Rechercher dans les factures

**Configuration :**
```properties
opensearch.host=localhost
opensearch.port=9200
opensearch.scheme=http
opensearch.index.invoices=invoices
```

**Fichiers concernés :**
- Backend : `OpenSearchService.java`, `PdfService.java`, `InvoiceResource.java`
- Docker : `docker-compose.yml` (services opensearch et opensearch-dashboards)
- Dependencies : `opensearch-java`, `opensearch-rest-client`, `tika-core`, `tika-parsers-standard-package`

**Exemple de recherche :**
```bash
curl -H "Authorization: Bearer {token}" \
  "http://localhost:8080/api/invoices/search?q=ACME"
```

### 6. 📊 Tableau de bord

- Vue d'ensemble de l'activité
- Statistiques clés :
  - Total clients
  - Factures du mois
  - Factures payées
  - Factures en attente
- Liste des factures récentes
- Navigation rapide vers les sections

**Fichiers concernés :**
- Frontend : `DashboardView.vue`

### 7. ⚙️ Paramètres

- Page de configuration utilisateur
- Gestion du profil
- Préférences de l'application

**Fichiers concernés :**
- Frontend : `SettingsView.vue`

---

## Modèles de données

### User (Utilisateur)
```java
@Entity
public class User {
    @Id
    Long id;
    String email;           // Unique
    String password;        // Hash BCrypt
    String firstName;
    String lastName;
    String otp;            // Code OTP temporaire
    LocalDateTime otpExpiry;
    boolean emailVerified;
    LocalDateTime createdAt;
}
```

### Client
```java
@Entity
public class Client {
    @Id
    Long id;
    String companyName;
    String email;
    String phone;
    String addressStreet;
    String addressCity;
    String addressPostalCode;
    String siret;

    @ManyToOne
    User user;             // Propriétaire du client

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```

### Invoice (Facture)
```java
@Entity
public class Invoice {
    @Id
    Long id;
    String invoiceNumber;  // Auto-généré

    @ManyToOne
    Client client;

    @ManyToOne
    User user;

    @Enumerated
    InvoiceStatus status;  // DRAFT, SENT, PAID, OVERDUE, CANCELLED

    LocalDate issueDate;
    LocalDate dueDate;

    @OneToMany(cascade = ALL)
    List<InvoiceItem> items;

    BigDecimal subtotal;   // Total HT
    BigDecimal taxTotal;   // Total TVA
    BigDecimal total;      // Total TTC

    String notes;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```

### InvoiceItem (Ligne de facture)
```java
@Entity
public class InvoiceItem {
    @Id
    Long id;

    @ManyToOne
    Invoice invoice;

    String description;
    BigDecimal quantity;
    BigDecimal unitPrice;  // HT
    BigDecimal taxRate;    // Pourcentage (ex: 20.00 pour 20%)
    BigDecimal total;      // TTC calculé
}
```

### Reminder (Rappel)
```java
@Entity
public class Reminder {
    @Id
    Long id;

    @ManyToOne
    Invoice invoice;

    LocalDateTime sentAt;
    String reminderType;   // Type de rappel
}
```

---

## API REST

### Authentification

#### POST `/api/auth/register`
Inscription d'un nouvel utilisateur.

**Requête :**
```json
{
  "firstName": "Jean",
  "lastName": "Dupont",
  "email": "jean.dupont@example.com",
  "password": "motdepasse123"
}
```

**Réponse :** `204 No Content`

#### POST `/api/auth/verify-otp`
Vérification du code OTP et obtention des tokens.

**Requête :**
```json
{
  "email": "jean.dupont@example.com",
  "otp": "123456"
}
```

**Réponse :**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "user": {
    "id": 1,
    "email": "jean.dupont@example.com",
    "firstName": "Jean",
    "lastName": "Dupont"
  }
}
```

#### POST `/api/auth/login`
Connexion avec email/mot de passe.

**Requête :**
```json
{
  "email": "jean.dupont@example.com",
  "password": "motdepasse123"
}
```

**Réponse :**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "user": {
    "id": 1,
    "email": "jean.dupont@example.com",
    "firstName": "Jean",
    "lastName": "Dupont"
  }
}
```

### Clients

#### GET `/api/clients`
Liste tous les clients de l'utilisateur connecté.

**Headers :** `Authorization: Bearer {token}`

**Réponse :**
```json
[
  {
    "id": 1,
    "companyName": "ACME Corporation",
    "email": "contact@acme.com",
    "phone": "+33 6 12 34 56 78",
    "addressStreet": "123 Rue de la Paix",
    "addressCity": "Paris",
    "addressPostalCode": "75000",
    "siret": "123 456 789 00012"
  }
]
```

#### POST `/api/clients`
Crée un nouveau client.

**Headers :** `Authorization: Bearer {token}`

**Requête :**
```json
{
  "companyName": "ACME Corporation",
  "email": "contact@acme.com",
  "phone": "+33 6 12 34 56 78",
  "addressStreet": "123 Rue de la Paix",
  "addressCity": "Paris",
  "addressPostalCode": "75000",
  "siret": "123 456 789 00012"
}
```

**Réponse :** `201 Created` avec l'objet client créé

#### PUT `/api/clients/{id}`
Met à jour un client existant.

**Headers :** `Authorization: Bearer {token}`

**Requête :** Même structure que POST

**Réponse :** `200 OK` avec l'objet client mis à jour

### Factures

#### GET `/api/invoices`
Liste toutes les factures de l'utilisateur connecté.

**Headers :** `Authorization: Bearer {token}`

**Réponse :**
```json
[
  {
    "id": 1,
    "invoiceNumber": "INV-2024-001",
    "client": {
      "id": 1,
      "companyName": "ACME Corporation"
    },
    "status": "SENT",
    "issueDate": "2024-01-15",
    "dueDate": "2024-02-15",
    "subtotal": 1000.00,
    "taxTotal": 200.00,
    "total": 1200.00,
    "items": [
      {
        "description": "Développement application web",
        "quantity": 10,
        "unitPrice": 100.00,
        "taxRate": 20.00,
        "total": 1200.00
      }
    ],
    "notes": "Paiement par virement bancaire"
  }
]
```

#### POST `/api/invoices`
Crée une nouvelle facture.

**Headers :** `Authorization: Bearer {token}`

**Requête :**
```json
{
  "clientId": 1,
  "status": "DRAFT",
  "issueDate": "2024-01-15",
  "dueDate": "2024-02-15",
  "items": [
    {
      "description": "Développement application web",
      "quantity": 10,
      "unitPrice": 100.00,
      "taxRate": 20.00
    }
  ],
  "notes": "Paiement par virement bancaire"
}
```

**Réponse :** `201 Created` avec l'objet facture créé

#### GET `/api/invoices/{id}/pdf`
Génère et télécharge le PDF de la facture.

**Headers :** `Authorization: Bearer {token}`

**Réponse :** Fichier PDF

---

## Internationalisation

L'application supporte l'internationalisation (i18n) avec **vue-i18n**.

### Configuration

**Fichier de configuration** : `frontend/src/i18n.ts`
```typescript
import { createI18n } from 'vue-i18n'
import fr from './locales/fr.json'

export default createI18n({
  legacy: false,
  locale: 'fr',
  fallbackLocale: 'fr',
  messages: { fr }
})
```

### Fichiers de traduction

**Structure** : `frontend/src/locales/fr.json`

```json
{
  "common": {
    "appName": "Facture Freelance",
    "save": "Enregistrer",
    "cancel": "Annuler",
    "email": "Email",
    ...
  },
  "auth": {
    "login": { ... },
    "register": { ... },
    "verifyOtp": { ... }
  },
  "clients": {
    "list": { ... },
    "form": { ... }
  },
  "invoices": {
    "list": { ... },
    "form": { ... },
    "status": {
      "DRAFT": "Brouillon",
      "SENT": "Envoyée",
      "PAID": "Payée",
      "OVERDUE": "En retard",
      "CANCELLED": "Annulée"
    }
  }
}
```

### Utilisation dans les composants

```vue
<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
</script>

<template>
  <h1>{{ t('clients.list.title') }}</h1>
  <button>{{ t('common.save') }}</button>
</template>
```

### Vues internationalisées

Toutes les vues suivantes utilisent i18n :
- ✅ `LoginView.vue`
- ✅ `RegisterView.vue`
- ✅ `VerifyOtpView.vue`
- ✅ `ClientsListView.vue`
- ✅ `ClientFormView.vue`
- ✅ `InvoicesListView.vue`
- ✅ `InvoiceFormView.vue`

---

## Configuration

### Backend (application.properties)

#### Base de données
```properties
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=postgres
quarkus.datasource.password=postgres
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/facture_db
```

#### JWT
```properties
mp.jwt.verify.publickey.location=META-INF/publicKey.pem
mp.jwt.verify.issuer=https://facture-freelance.com
smallrye.jwt.sign.key.location=META-INF/privateKey.pem
jwt.access.token.expiration=900        # 15 minutes
jwt.refresh.token.expiration=604800    # 7 jours
```

#### OTP
```properties
otp.expiration.minutes=5
otp.length=6
otp.dev.enabled=false                  # false par défaut (sécurité)
```

#### Mailer
```properties
quarkus.mailer.from=noreply@facture-freelance.com
quarkus.mailer.mock=true               # Mock en dev
```

#### CORS
```properties
quarkus.http.cors=true
quarkus.http.cors.origins=http://localhost:5173,http://localhost:3000
quarkus.http.cors.methods=GET,POST,PUT,DELETE,OPTIONS
```

### Profils d'environnement

#### Développement (`%dev`)
```properties
%dev.quarkus.datasource.devservices.enabled=true
%dev.quarkus.mailer.mock=true
%dev.otp.dev.enabled=true              # OTP fixe: 123456
```

#### Test (`%test`)
```properties
%test.quarkus.datasource.db-kind=h2
%test.quarkus.datasource.jdbc.url=jdbc:h2:mem:test
%test.quarkus.mailer.mock=true
```

#### Production (`%prod`)
```properties
%prod.quarkus.hibernate-orm.log.sql=false
%prod.quarkus.log.level=WARN
%prod.quarkus.http.cors.origins=${FRONTEND_URL}
```

---

## Développement

### Prérequis

- Java 17+
- Node.js 18+
- PostgreSQL 15+
- Maven 3.8+

### Installation

#### Backend
```bash
cd backend
./mvnw clean install
```

#### Frontend
```bash
cd frontend
npm install
```

### Lancement en développement

#### Backend (mode dev Quarkus)
```bash
cd backend
./mvnw quarkus:dev
```
- Server: http://localhost:8080
- Dev UI: http://localhost:8080/q/dev
- OTP fixe activé: `123456`

#### Frontend (Vite dev server)
```bash
cd frontend
npm run dev
```
- Server: http://localhost:5173

### Build production

#### Backend
```bash
cd backend
./mvnw package -Dquarkus.package.type=uber-jar
java -jar target/facture-freelance-1.0.0-runner.jar
```

#### Frontend
```bash
cd frontend
npm run build
```
Fichiers générés dans `dist/`

### Tests

#### Backend
```bash
cd backend
./mvnw test
```

#### Frontend
```bash
cd frontend
npm run test
```

### Structure des stores Pinia

#### `auth.ts`
- Gestion de l'authentification
- Stockage des tokens
- État utilisateur connecté

#### `clients.ts`
- CRUD clients
- Liste des clients
- Client courant

#### `invoices.ts`
- CRUD factures
- Liste des factures
- Facture courante
- Calculs de totaux

---

## Design System

L'application utilise le **Reform Design System** avec Tailwind CSS.

### Composants principaux
- Formulaires avec validation
- Boutons avec états (loading, disabled)
- Cards avec effets backdrop-blur
- Badges de statut colorés
- Layout responsive

### Palette de couleurs
- **Primary** : Bleu (gradient)
- **Success** : Vert
- **Warning** : Orange
- **Error** : Rouge
- **Neutral** : Gris

### Typographie
- Font : System fonts
- Tailles : text-sm, text-base, text-lg, text-xl, text-3xl
- Weights : font-medium, font-semibold, font-bold

---

## Sécurité

### Authentification
- Hash des mots de passe avec BCrypt
- Tokens JWT signés avec clés RSA
- Expiration des tokens (15min access, 7j refresh)
- Expiration des OTP (5 minutes)

### Autorisation
- Endpoints protégés par `@RolesAllowed("User")`
- Vérification de propriété des ressources (clients, factures)
- CORS configuré pour origines autorisées

### Validation
- Validation des entrées côté backend
- Validation des formulaires côté frontend
- Protection contre les injections SQL (JPA/Hibernate)

---

## Fonctionnalités à venir

### En cours de développement
- [ ] Export des factures (Excel, CSV)
- [ ] Statistiques avancées
- [ ] Multi-devises
- [ ] Templates de factures personnalisables
- [ ] Signature électronique
- [ ] Paiement en ligne

### Planifié
- [ ] Application mobile (React Native)
- [ ] API publique pour intégrations
- [ ] Tableau de bord analytics
- [ ] Multi-entreprises
- [ ] Gestion des devis
- [ ] Gestion des dépenses

---

## Support et Contact

Pour toute question ou problème :
- 📧 Email : support@facture-freelance.com
- 🐛 Issues : GitHub Issues
- 📖 Documentation : https://docs.facture-freelance.com

---

**Version** : 1.0.0
**Dernière mise à jour** : 2025-01-20
