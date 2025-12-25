# Architecture DDD & Event-Driven

## 📐 Vue d'ensemble

Cette application suit les principes du **Domain-Driven Design (DDD)** avec une architecture event-driven pour gérer les factures de manière scalable et maintenable.

## 🏗 Structure des couches

### 1. Domain Layer (`com.facture.domain`)

La couche domaine contient la logique métier pure, indépendante de toute infrastructure.

#### Domain Services
- **`InvoiceDomainService`**: Logique métier core
  - Génération de numéros de facture (format: `FACT-{TENANT_ID}-YYYY-MM-XXXXX`)
  - Règles métier pour les statuts (brouillon → envoyé → payé)
  - Détection des factures en retard
  - Validation des transitions d'état

#### Domain Repository Interfaces
- **`InvoiceRepository`**: Contrat pour la persistance des factures
  - Isolation par tenant
  - Requêtes métier (factures en retard, relances à envoyer)
  - Pas de dépendance à JPA/Hibernate

#### Domain Events
Les événements métier permettent de découpler les actions:

```java
// Événements de cycle de vie
- InvoiceCreatedEvent    // Facture créée
- InvoiceSentEvent       // Facture envoyée au client
- InvoicePaidEvent       // Facture marquée comme payée
```

Chaque événement contient:
- `invoiceId`: ID de la facture
- `userId`: ID de l'utilisateur
- `tenantId`: ID du tenant (multi-tenant)
- `occurredAt`: Timestamp de l'événement
- Données spécifiques à l'événement

### 2. Application Layer (`com.facture.application`)

Orchestration des cas d'utilisation et coordination entre domaine et infrastructure.

#### Application Services
- **`InvoiceApplicationService`**: Service applicatif principal
  - Gère les transactions (`@Transactional`)
  - Coordonne domaine + repository + événements
  - Orchestre les use cases métier
  - Fire les événements async (`Event<T>.fireAsync()`)

**Exemple de flow**:
```java
1. createInvoice(userId, request)
   ↓
2. Valide User + Client
   ↓
3. Crée l'entité Invoice
   ↓
4. Génère le numéro via InvoiceDomainService
   ↓
5. Persiste via InvoiceRepository
   ↓
6. Fire InvoiceCreatedEvent (async)
   ↓
7. Retourne InvoiceDto
```

### 3. Infrastructure Layer (`com.facture.infrastructure`)

Implémentation concrète des interfaces du domaine.

#### Repository Implementations
- **`InvoiceRepositoryImpl`**: Implémentation avec Panache
  - Requêtes SQL optimisées
  - Index pour performance multi-tenant
  - Isolation des données par tenant

### 4. Event Listeners (`com.facture.event.listener`)

Réaction asynchrone aux événements métier.

#### InvoiceEmailListener
- Envoie des emails de confirmation (création, envoi, paiement)
- Traitement async pour ne pas bloquer l'API
- TODO: Intégration Mailer

#### InvoiceReminderListener
- Création automatique de 3 relances lors de l'envoi:
  - J-7 avant échéance
  - J-3 avant échéance
  - Jour de l'échéance

#### InvoiceStatsListener
- Agrégation des statistiques en temps réel
- Compteurs par tenant
- Métriques de revenus et délais de paiement

## ⏰ Jobs planifiés (Quarkus Scheduler)

### InvoiceReminderJob
**Cron**: `0 0 * * * ?` (toutes les heures)
- Envoie les relances de paiement en attente
- Filtre les factures déjà payées
- Marque les relances comme envoyées

**Cron**: `0 0 2 * * ?` (tous les jours à 2h)
- Met à jour le statut des factures en retard
- `SENT` → `OVERDUE` si `dueDate < now`

### OtpCleanupJob
**Cron**: `0 0 3 * * ?` (tous les jours à 3h)
- Nettoie les codes OTP expirés
- Libère l'espace en base de données
- Sécurité: suppression des secrets obsolètes

### StatsAggregationJob
**Cron**: `0 0 1 * * ?` (tous les jours à 1h)
- Calcule les statistiques quotidiennes, mensuelles, annuelles
- Agrégation par tenant
- Métriques:
  - Nombre de factures par statut
  - Chiffre d'affaires (factures payées)
  - Montant en attente de paiement

## 🏢 Multi-Tenancy

### Modèle de tenant

Chaque utilisateur appartient à un **tenant** (par défaut: son propre ID).

**Préparation future**: Support des cabinets/équipes
- Plusieurs utilisateurs partageant le même `tenantId`
- Partage des clients et factures au sein du cabinet
- Isolation totale entre tenants différents

### Isolation des données

```sql
-- Index de performance
CREATE INDEX idx_users_tenant_id ON users(tenant_id);
CREATE INDEX idx_invoices_user_tenant ON invoices(user_id);

-- Requêtes toujours filtrées par tenant
SELECT * FROM invoices
WHERE user_id IN (SELECT id FROM users WHERE tenant_id = ?)
```

### Migration Flyway

**V1**: Schema initial (baseline)
**V2**: Ajout du support multi-tenant
- Colonne `tenant_id` sur `users`
- Index de performance
- Rétrocompatibilité (tenant_id = user_id pour utilisateurs existants)

## 🔄 Flux de données complets

### Exemple: Création d'une facture

```
[Client HTTP POST /api/invoices]
         ↓
[InvoiceResource] ← JWT (userId)
         ↓
[InvoiceApplicationService.createInvoice()]
         ↓
[InvoiceDomainService.generateInvoiceNumber(tenantId)]
         ↓
[InvoiceRepositoryImpl.save(invoice)]
         ↓ (transaction committed)
[Event: InvoiceCreatedEvent] → fireAsync()
         ↓
    ┌────┴────┬──────────┐
    ↓         ↓          ↓
[EmailListener] [StatsListener] [AuditListener?]
```

### Exemple: Envoi d'une facture

```
[PUT /api/invoices/{id}/status?status=SENT]
         ↓
[InvoiceApplicationService.updateInvoiceStatus()]
         ↓
[InvoiceDomainService.markAsSent(invoice)] ← Validation métier
         ↓
[InvoiceRepositoryImpl.save(invoice)]
         ↓
[Event: InvoiceSentEvent] → fireAsync()
         ↓
    ┌────┴────┐
    ↓         ↓
[EmailListener] [ReminderListener]
    |              |
    |              ↓
    |         [Crée 3 reminders]
    ↓
[Envoie PDF au client]
```

## 📊 Avantages de cette architecture

### ✅ Séparation des préoccupations
- Domaine = logique métier pure
- Application = orchestration
- Infrastructure = détails techniques

### ✅ Testabilité
- Domain services testables sans base de données
- Mocks faciles des repositories
- Tests d'intégration isolés par couche

### ✅ Scalabilité
- Events async = non-bloquant
- Multi-tenant = prêt pour SaaS
- Jobs planifiés = tâches de fond efficaces

### ✅ Maintenabilité
- Chaque couche a une responsabilité claire
- Changements isolés (ex: changer de BDD)
- Documentation vivante via le code

### ✅ Extensibilité
- Ajout de nouveaux listeners sans modifier le core
- Nouveaux événements métier facilement
- Support futur des webhooks/intégrations

## 🚀 Prochaines étapes

1. **Email Service complet**
   - Implémentation des envois d'emails dans les listeners
   - Templates HTML pour factures
   - Intégration SendGrid/SES

2. **Stats persistées**
   - Table dédiée `invoice_stats`
   - Dashboard avec graphiques temps réel
   - Export CSV/Excel

3. **Webhooks**
   - API pour enregistrer des webhooks
   - Nouveau listener: `WebhookEventListener`
   - Retry mechanism pour les webhooks failés

4. **Audit Log**
   - Nouveau listener: `AuditLogListener`
   - Traçabilité complète des actions
   - Conformité RGPD

5. **Cabinets/Équipes**
   - UI pour créer et gérer un cabinet
   - Inviter des utilisateurs dans un tenant
   - Permissions granulaires (owner, admin, member)

## 📚 Ressources

- [Domain-Driven Design - Eric Evans](https://www.domainlanguage.com/ddd/)
- [Quarkus Events Guide](https://quarkus.io/guides/cdi-reference#events-and-observers)
- [Quarkus Scheduler Guide](https://quarkus.io/guides/scheduler)
- [Flyway Migrations](https://flywaydb.org/documentation/)
