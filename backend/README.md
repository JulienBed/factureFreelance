# Backend - Facture Freelance

API REST développée avec Quarkus pour la gestion de factures pour indépendants.

## Stack Technique

- **Framework**: Quarkus 3.20.2
- **Java**: 21 (LTS)
- **Build Tool**: Maven
- **Base de données**: PostgreSQL 15+
- **ORM**: Hibernate ORM avec Panache
- **Authentification**: JWT + OTP par email
- **PDF**: Apache PDFBox + Mustang Project (Factur-X/ZUGFeRD)

## Prérequis

- Java 21 ou supérieur (LTS)
- Maven 3.8+
- PostgreSQL 15+ (ou Docker)
- OpenSSL (pour générer les clés JWT)

## Installation

### 1. Cloner le repository

```bash
cd backend
```

### 2. Générer les clés JWT

```bash
./generate-keys.sh
```

### 3. Configurer la base de données

Option A: Utiliser Docker
```bash
docker run --name postgres-facture \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=facture_db \
  -p 5432:5432 \
  -d postgres:15
```

Option B: PostgreSQL local
- Créer une base de données nommée `facture_db`
- Mettre à jour `src/main/resources/application.properties` avec vos credentials

### 4. Configurer les variables d'environnement

Copier `.env.example` et créer `.env`:
```bash
cp .env.example .env
```

Modifier `.env` avec vos valeurs:
- Credentials PostgreSQL
- Google OAuth credentials (optionnel pour SSO)
- Configuration SMTP pour l'envoi d'emails

### 5. Lancer l'application en mode développement

```bash
./mvnw quarkus:dev
```

L'API sera accessible sur `http://localhost:8080`

### 6. Lancer en mode production

```bash
./mvnw clean package
java -jar target/quarkus-app/quarkus-run.jar
```

## Endpoints API

### Authentification

- `POST /api/auth/register` - Inscription
- `POST /api/auth/login` - Connexion (génère OTP)
- `POST /api/auth/verify-otp` - Vérification OTP
- `GET /api/auth/google` - Connexion Google (à implémenter)

### Clients

- `GET /api/clients` - Liste des clients (avec recherche optionnelle: `?search=nom`)
- `GET /api/clients/{id}` - Détails d'un client
- `POST /api/clients` - Créer un client
- `PUT /api/clients/{id}` - Modifier un client
- `DELETE /api/clients/{id}` - Supprimer un client

### Factures

- `GET /api/invoices` - Liste des factures (avec filtre optionnel: `?status=SENT`)
- `GET /api/invoices/{id}` - Détails d'une facture
- `POST /api/invoices` - Créer une facture
- `PUT /api/invoices/{id}` - Modifier une facture
- `PUT /api/invoices/{id}/status?status=PAID` - Changer le statut
- `DELETE /api/invoices/{id}` - Supprimer une facture
- **`GET /api/invoices/{id}/pdf`** - **Télécharger la facture au format Factur-X PDF**
- `POST /api/invoices/{id}/send` - Envoyer la facture par email (à implémenter)

## Authentification

Toutes les routes sauf `/api/auth/*` nécessitent un JWT valide dans le header:

```
Authorization: Bearer <token>
```

## Développement

### Hot reload

En mode dev, Quarkus recharge automatiquement les changements de code.

### Accéder à la console de développement

`http://localhost:8080/q/dev/`

### Tests

```bash
./mvnw test
```

## Structure du projet

```
backend/
├── src/main/java/com/facture/
│   ├── entity/         # Entités JPA
│   ├── dto/            # Data Transfer Objects
│   ├── service/        # Logique métier
│   ├── resource/       # REST endpoints
│   ├── util/           # Utilitaires
│   ├── exception/      # Gestion des exceptions
│   └── scheduler/      # Jobs planifiés
├── src/main/resources/
│   ├── application.properties
│   └── META-INF/       # Clés JWT
└── pom.xml
```

## Fonctionnalités

- ✅ Authentification par email/password + OTP
- ✅ Gestion des clients (CRUD)
- ✅ Gestion des factures (CRUD)
- ✅ Numérotation automatique des factures
- ✅ **Génération de PDF au format Factur-X (PDF/A-3 avec XML EN 16931 embarqué)**
- ⏳ Envoi de factures par email
- ⏳ Relances automatiques
- ⏳ Google OAuth SSO
- ⏳ Dashboard avec statistiques

## Format Factur-X

Les factures PDF générées sont conformes à la **norme Factur-X** (équivalent français de ZUGFeRD), qui est le standard européen pour la facturation électronique :

- **PDF/A-3** : Format PDF archivable à long terme
- **XML EN 16931** : Métadonnées structurées embarquées dans le PDF
- **Lisible par l'homme ET par les machines** : Le PDF est visuellement lisible, et le XML permet l'import automatique dans les logiciels comptables
- **Conforme aux obligations françaises** : Format obligatoire pour les factures aux organismes publics

### Avantages de Factur-X

1. **Archivage légal** : PDF/A-3 garantit la lisibilité à long terme
2. **Automatisation comptable** : Le XML embarqué permet l'import automatique des factures
3. **Interopérabilité** : Compatible avec tous les logiciels conformes EN 16931
4. **Obligation légale** : Obligatoire en France pour les marchés publics (depuis 2020)

### Vérifier le format Factur-X

Les PDF générés peuvent être vérifiés avec des outils comme :
- [Factur-X Viewer](https://www.mustangproject.org/)
- Adobe Acrobat (menu "Outils" > "Pièces jointes")
- Logiciels comptables compatibles Factur-X

## Configuration SMTP (Gmail)

Pour utiliser Gmail pour l'envoi d'emails:

1. Activer l'authentification à deux facteurs sur votre compte Google
2. Générer un "Mot de passe d'application" dans les paramètres de sécurité
3. Utiliser ce mot de passe dans votre `.env`:

```
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=votre-email@gmail.com
SMTP_PASSWORD=votre-mot-de-passe-application
```

## Troubleshooting

### Erreur de connexion à PostgreSQL

Vérifier que PostgreSQL est démarré et accessible:
```bash
psql -U postgres -h localhost
```

### Erreur de clés JWT manquantes

Exécuter le script de génération de clés:
```bash
./generate-keys.sh
```

## License

MIT
