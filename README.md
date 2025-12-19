# Facture Freelance - Application SaaS

Application SaaS complète pour la gestion de factures destinée aux indépendants et freelances.

## Vue d'ensemble

Facture Freelance est une application web moderne permettant aux travailleurs indépendants de :
- Gérer leurs clients
- Créer et suivre leurs factures
- Générer des PDF de factures professionnels
- Envoyer des factures par email
- Gérer les relances automatiques pour les paiements en retard
- Visualiser leurs statistiques de chiffre d'affaires

## Stack Technique

### Backend
- **Framework** : Quarkus 3.20.2 (Java 21 LTS)
- **Base de données** : PostgreSQL 15+
- **ORM** : Hibernate ORM avec Panache
- **Authentification** : JWT + OTP par email
- **PDF** : Apache PDFBox + Mustang Project (Factur-X)
- **Email** : Quarkus Mailer

### Frontend
- **Framework** : Vue.js 3 avec Composition API
- **Language** : TypeScript
- **Build Tool** : Vite
- **State Management** : Pinia
- **Styling** : Tailwind CSS
- **Routing** : Vue Router 4

### Infrastructure
- **Conteneurisation** : Docker + Docker Compose
- **Reverse Proxy** : Nginx (optionnel)

## Fonctionnalités

### ✅ Implémenté
- Authentification complète (Register/Login/OTP par email)
- Gestion des clients (CRUD complet)
- Gestion des factures (CRUD complet)
- Numérotation automatique des factures
- Calcul automatique TTC/HT
- Interface utilisateur responsive
- Multi-tenant (isolation par utilisateur)

### 🚧 En cours
- Dashboard avec statistiques
- Pages de gestion clients et factures

### 📋 À venir
- Génération de PDF pour les factures
- Envoi de factures par email
- Système de relances automatiques
- Google OAuth SSO
- Export comptable
- Multi-devise
- Devis en plus des factures

## Installation

### Prérequis

- Docker et Docker Compose **OU**
- Java 21+ (LTS) et Maven 3.9+ (pour le backend)
- Node.js 18+ (pour le frontend)
- PostgreSQL 15+ (si pas de Docker)

### Option 1 : Avec Docker (Recommandé)

1. Cloner le repository
```bash
git clone https://github.com/JulienBed/factureFreelance.git
cd factureFreelance
```

2. Copier et configurer les variables d'environnement
```bash
cp .env.example .env
# Éditer .env avec vos valeurs
```

3. Lancer avec Docker Compose
```bash
docker-compose up -d
```

4. Accéder à l'application
- Frontend : http://localhost:5173
- Backend API : http://localhost:8080
- Swagger UI : http://localhost:8080/q/swagger-ui

### Option 2 : Installation manuelle

#### Backend

```bash
cd backend

# Générer les clés JWT
./generate-keys.sh

# Configurer la base de données PostgreSQL
# Créer une base de données nommée 'facture_db'

# Copier et configurer les variables
cp .env.example .env

# Lancer en mode dev
./mvnw quarkus:dev
```

Le backend sera accessible sur http://localhost:8080

#### Frontend

```bash
cd frontend

# Installer les dépendances
npm install

# Créer le fichier .env.local
echo "VITE_API_URL=http://localhost:8080/api" > .env.local

# Lancer en mode dev
npm run dev
```

Le frontend sera accessible sur http://localhost:5173

## Documentation

- [Documentation Backend](./backend/README.md)
- [Documentation Frontend](./frontend/README.md)
- [Architecture](./ARCHITECTURE.md)

## Structure du projet

```
factureFreelance/
├── backend/                 # API REST Quarkus
│   ├── src/main/java/
│   │   └── com/facture/
│   │       ├── entity/      # Entités JPA
│   │       ├── dto/         # Data Transfer Objects
│   │       ├── service/     # Logique métier
│   │       ├── resource/    # REST endpoints
│   │       ├── security/    # Configuration sécurité
│   │       └── util/        # Utilitaires
│   ├── src/main/resources/
│   └── pom.xml
├── frontend/                # Application Vue.js
│   ├── src/
│   │   ├── components/      # Composants Vue
│   │   ├── views/           # Pages
│   │   ├── stores/          # Pinia stores
│   │   ├── services/        # Services API
│   │   ├── router/          # Configuration routing
│   │   └── types/           # Types TypeScript
│   └── package.json
├── docker-compose.yml       # Configuration Docker
├── ARCHITECTURE.md          # Documentation architecture
└── README.md               # Ce fichier
```

## API Endpoints

### Authentification
- `POST /api/auth/register` - Inscription
- `POST /api/auth/login` - Connexion (génère OTP)
- `POST /api/auth/verify-otp` - Vérification OTP

### Clients
- `GET /api/clients` - Liste des clients
- `GET /api/clients/:id` - Détails d'un client
- `POST /api/clients` - Créer un client
- `PUT /api/clients/:id` - Modifier un client
- `DELETE /api/clients/:id` - Supprimer un client

### Factures
- `GET /api/invoices` - Liste des factures
- `GET /api/invoices/:id` - Détails d'une facture
- `POST /api/invoices` - Créer une facture
- `PUT /api/invoices/:id` - Modifier une facture
- `PUT /api/invoices/:id/status` - Changer le statut
- `DELETE /api/invoices/:id` - Supprimer une facture

## Configuration SMTP

Pour l'envoi d'emails (OTP, factures), configurez un serveur SMTP dans `.env` :

### Gmail
1. Activer l'authentification à deux facteurs
2. Générer un mot de passe d'application
3. Utiliser ces credentials dans `.env`

```env
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=votre-email@gmail.com
SMTP_PASSWORD=votre-mot-de-passe-application
```

## Contribuer

Les contributions sont les bienvenues ! N'hésitez pas à ouvrir une issue ou une pull request.

## License

MIT

## Auteur

Développé avec ❤️ pour la communauté freelance
