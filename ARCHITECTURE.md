# Architecture de l'Application SaaS de Gestion de Factures

## Vue d'ensemble

Application SaaS multi-tenant permettant aux indépendants de gérer leurs factures, clients et relances.

## Stack Technique

### Backend
- **Runtime** : Node.js 20+
- **Framework** : Express.js avec TypeScript
- **ORM** : Prisma
- **Base de données** : PostgreSQL 15+
- **Authentification** :
  - Passport.js pour Google OAuth2
  - JWT pour la gestion des sessions
  - OTP par email (Nodemailer)
- **Génération PDF** : PDFKit
- **Validation** : Zod
- **Logs** : Winston

### Frontend
- **Framework** : React 18+ avec TypeScript
- **Build Tool** : Vite
- **Styling** : Tailwind CSS
- **Routing** : React Router v6
- **State Management** : Context API + React Query
- **HTTP Client** : Axios
- **Form Management** : React Hook Form
- **UI Components** : Headless UI + Custom components

### Infrastructure
- **Conteneurisation** : Docker + Docker Compose
- **Reverse Proxy** : Nginx (optionnel)
- **Variables d'environnement** : dotenv

## Architecture des Données

### Modèles Principaux

1. **User (Utilisateur/Freelance)**
   - id, email, password (hash), firstName, lastName
   - googleId (pour SSO)
   - otpSecret, otpExpiry
   - companyName, siret, address, phone
   - bankDetails (IBAN, BIC)
   - createdAt, updatedAt

2. **Client**
   - id, userId (relation)
   - companyName, contactName, email, phone
   - address (rue, ville, code postal, pays)
   - siret, tvaNumber
   - notes
   - createdAt, updatedAt

3. **Invoice (Facture)**
   - id, userId, clientId (relations)
   - invoiceNumber (auto-généré)
   - status (DRAFT, SENT, PAID, OVERDUE, CANCELLED)
   - issueDate, dueDate, paidDate
   - subtotal, taxRate, taxAmount, total
   - currency (EUR par défaut)
   - notes, paymentTerms
   - createdAt, updatedAt

4. **InvoiceItem (Ligne de facture)**
   - id, invoiceId (relation)
   - description
   - quantity, unitPrice
   - taxRate, amount
   - order (pour le tri)

5. **Reminder (Relance)**
   - id, invoiceId (relation)
   - type (AUTO, MANUAL)
   - sentAt, sentBy
   - message
   - status (SENT, FAILED)

6. **PaymentMethod**
   - id, userId (relation)
   - type (BANK_TRANSFER, CHECK, CASH, OTHER)
   - isDefault
   - details (JSON)

## Architecture Backend

### Structure des dossiers
```
backend/
├── src/
│   ├── config/         # Configuration (database, auth, email)
│   ├── models/         # Types TypeScript et Prisma client
│   ├── routes/         # Routes Express
│   ├── controllers/    # Logique métier
│   ├── middleware/     # Auth, validation, error handling
│   ├── services/       # Services (email, pdf, auth)
│   ├── utils/          # Helpers
│   └── index.ts        # Point d'entrée
├── prisma/
│   └── schema.prisma   # Schéma de base de données
├── .env.example
├── tsconfig.json
└── package.json
```

### Routes API

#### Authentification
- POST `/api/auth/register` - Inscription
- POST `/api/auth/login` - Connexion (génère OTP)
- POST `/api/auth/verify-otp` - Vérification OTP
- GET `/api/auth/google` - Redirection Google OAuth
- GET `/api/auth/google/callback` - Callback Google
- POST `/api/auth/logout` - Déconnexion
- GET `/api/auth/me` - Profil utilisateur

#### Utilisateurs
- GET `/api/users/profile` - Récupérer profil
- PUT `/api/users/profile` - Mettre à jour profil
- PUT `/api/users/company` - Mettre à jour infos entreprise

#### Clients
- GET `/api/clients` - Liste des clients (pagination)
- GET `/api/clients/:id` - Détails client
- POST `/api/clients` - Créer client
- PUT `/api/clients/:id` - Modifier client
- DELETE `/api/clients/:id` - Supprimer client

#### Factures
- GET `/api/invoices` - Liste des factures (filtres, pagination)
- GET `/api/invoices/:id` - Détails facture
- POST `/api/invoices` - Créer facture
- PUT `/api/invoices/:id` - Modifier facture
- DELETE `/api/invoices/:id` - Supprimer facture
- POST `/api/invoices/:id/send` - Envoyer facture par email
- GET `/api/invoices/:id/pdf` - Télécharger PDF
- PUT `/api/invoices/:id/status` - Changer statut

#### Relances
- GET `/api/invoices/:id/reminders` - Liste des relances
- POST `/api/invoices/:id/reminders` - Créer relance manuelle
- GET `/api/reminders/pending` - Relances en attente

#### Statistiques
- GET `/api/stats/dashboard` - Stats dashboard (CA, factures en attente, etc.)

## Architecture Frontend

### Structure des dossiers
```
frontend/
├── src/
│   ├── components/     # Composants réutilisables
│   │   ├── common/     # Buttons, Inputs, etc.
│   │   ├── layout/     # Header, Sidebar, Footer
│   │   └── features/   # Composants métier
│   ├── pages/          # Pages de l'application
│   │   ├── Auth/       # Login, Register
│   │   ├── Dashboard/  # Dashboard principal
│   │   ├── Clients/    # Gestion clients
│   │   ├── Invoices/   # Gestion factures
│   │   └── Settings/   # Paramètres
│   ├── contexts/       # Context API (Auth, Theme)
│   ├── hooks/          # Custom hooks
│   ├── services/       # API calls
│   ├── types/          # Types TypeScript
│   ├── utils/          # Helpers
│   ├── App.tsx
│   └── main.tsx
├── public/
├── index.html
├── tailwind.config.js
├── tsconfig.json
└── package.json
```

### Pages principales

1. **Authentification**
   - Login (email/password + Google button)
   - OTP verification
   - Register

2. **Dashboard**
   - Vue d'ensemble : CA mensuel, factures en attente, factures en retard
   - Graphiques : évolution CA, répartition par statut
   - Dernières factures

3. **Clients**
   - Liste avec recherche et filtres
   - Formulaire création/édition
   - Vue détail avec historique factures

4. **Factures**
   - Liste avec filtres (statut, client, date)
   - Formulaire création/édition
   - Prévisualisation PDF
   - Envoi par email
   - Gestion des relances

5. **Paramètres**
   - Profil utilisateur
   - Informations entreprise
   - Coordonnées bancaires
   - Préférences (devise, délais paiement, etc.)

## Sécurité

- Mots de passe hashés avec bcrypt
- JWT avec expiration courte (15min) + refresh token
- OTP avec expiration (5min)
- Rate limiting sur les endpoints sensibles
- CORS configuré
- Helmet.js pour les headers de sécurité
- Validation stricte des inputs (Zod)
- Multi-tenant : isolation stricte des données par userId

## Fonctionnalités Clés

### Authentification
- Double méthode : Google SSO ou Email/Password
- OTP par email pour la connexion par email
- Tokens JWT avec refresh

### Gestion Clients
- CRUD complet
- Recherche et filtrage
- Historique des factures par client

### Gestion Factures
- Numérotation automatique
- Statuts multiples (brouillon, envoyée, payée, en retard)
- Lignes de prestation multiples
- Calcul automatique TTC/HT
- Génération PDF personnalisée
- Envoi par email

### Relances
- Relances automatiques configurables
- Relances manuelles
- Historique des relances

### Dashboard
- KPIs : CA, factures en attente, taux de paiement
- Graphiques et visualisations
- Alertes (factures en retard)

## Déploiement

### Docker Compose
- Container PostgreSQL
- Container Backend
- Container Frontend (build statique servi par Nginx)

### Variables d'environnement
- DATABASE_URL
- JWT_SECRET
- GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET
- SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASS
- FRONTEND_URL, BACKEND_URL

## Évolutions futures possibles

- Multi-devise
- Export comptable
- Devis (en plus des factures)
- Paiement en ligne (Stripe)
- Multi-langue
- Application mobile
- Tableau de bord analytique avancé
- Intégrations (banque, comptabilité)
