# Frontend - Facture Freelance

Interface utilisateur Vue.js 3 pour la gestion de factures pour indépendants.

## Stack Technique

- **Framework**: Vue.js 3 avec Composition API
- **Language**: TypeScript
- **Build Tool**: Vite
- **State Management**: Pinia
- **Routing**: Vue Router 4
- **Styling**: Tailwind CSS
- **HTTP Client**: Axios
- **Form Validation**: VeeValidate + Yup
- **Icons**: Heroicons
- **UI Components**: Headless UI

## Prérequis

- Node.js 18+ et npm/pnpm/yarn

## Installation

### 1. Installer les dépendances

```bash
cd frontend
npm install
```

### 2. Configuration

Créer un fichier `.env.local` :

```env
VITE_API_URL=http://localhost:8080/api
```

### 3. Lancer en mode développement

```bash
npm run dev
```

L'application sera accessible sur `http://localhost:5173`

### 4. Build pour la production

```bash
npm run build
```

Les fichiers de production seront dans le dossier `dist/`

## Structure du projet

```
frontend/
├── src/
│   ├── assets/          # CSS, images
│   ├── components/      # Composants Vue
│   │   ├── common/      # Boutons, inputs, etc.
│   │   ├── layout/      # Layout principal
│   │   └── features/    # Composants métier
│   ├── views/           # Pages (routes)
│   │   ├── Auth/        # Login, Register, OTP
│   │   ├── Dashboard/   # Dashboard
│   │   ├── Clients/     # Gestion clients
│   │   ├── Invoices/    # Gestion factures
│   │   └── Settings/    # Paramètres
│   ├── stores/          # Pinia stores
│   ├── services/        # Services API
│   ├── router/          # Configuration routing
│   ├── types/           # Types TypeScript
│   ├── composables/     # Composables Vue
│   ├── utils/           # Utilitaires
│   ├── App.vue
│   └── main.ts
├── public/
├── index.html
└── package.json
```

## Fonctionnalités

- ✅ Authentification (Login/Register/OTP)
- ✅ Gestion d'état avec Pinia
- ✅ Routing avec protection des routes
- ✅ Services API avec intercepteurs
- ✅ Layout responsive avec sidebar
- ⏳ Pages Clients (CRUD)
- ⏳ Pages Factures (CRUD)
- ⏳ Dashboard avec statistiques
- ⏳ Génération et téléchargement PDF
- ⏳ Google OAuth SSO

## Routes

### Publiques
- `/login` - Connexion
- `/register` - Inscription
- `/verify-otp` - Vérification OTP

### Protégées (authentification requise)
- `/` - Dashboard
- `/clients` - Liste des clients
- `/clients/new` - Créer un client
- `/clients/:id` - Éditer un client
- `/invoices` - Liste des factures
- `/invoices/new` - Créer une facture
- `/invoices/:id` - Éditer une facture
- `/settings` - Paramètres utilisateur

## Services API

Tous les appels API passent par le service `api.ts` qui :
- Ajoute automatiquement le JWT dans les headers
- Gère le refresh token automatiquement
- Redirige vers login en cas d'erreur 401

## Stores Pinia

### Auth Store
- Gestion de l'authentification
- Stockage du token et des infos utilisateur
- Methods: `register()`, `login()`, `verifyOtp()`, `logout()`

### Client Store
- Gestion des clients
- Methods: `fetchClients()`, `createClient()`, `updateClient()`, `deleteClient()`

### Invoice Store
- Gestion des factures
- Methods: `fetchInvoices()`, `createInvoice()`, `updateInvoice()`, `deleteInvoice()`

## Développement

### Hot Module Replacement

Vite fournit le HMR pour un rechargement instantané pendant le développement.

### Linting

```bash
npm run lint
```

### Type Checking

```bash
npm run type-check
```

## License

MIT
