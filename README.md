# PC Price Tracker

Application Android native (Kotlin) qui surveille les prix des composants de ta
configuration PC cible chez LDLC, Materiel.net, Topachat, Fnac/Darty/Boulanger et
Amazon, applique tes remises (cashback, CSE, bon d'achat), calcule la config la
moins chere possible, et t'envoie une notification locale des qu'elle passe sous
ton seuil de "bonne affaire". Tout tourne en local (Room/SQLite embarque, aucun
backend, aucune base distante).

## Important - limite de cet environnement de build

Ce projet a ete ecrit dans un environnement sandbox dont l'acces reseau vers
`dl.google.com` (depot Maven de Google, indispensable a toute dependance
AndroidX/Compose/Room/AGP) est bloque. **Je n'ai donc pas pu compiler l'APK ni
lancer les tests depuis cet environnement.** Le code a ete relu attentivement a
la main (imports, coherence des signatures, equilibrage des blocs), mais il n'a
pas ete verifie par un vrai compilateur Kotlin/AGP. Attends-toi a devoir corriger
une ou deux erreurs mineures (import manquant, typo) au premier build reel sur
ta machine - ce sera rapide, Android Studio te les pointera directement.

## Build de l'APK

Prerequis : Android Studio (Koala ou plus recent) ou JDK 17 + Android SDK en
ligne de commande (`compileSdk 34`, `minSdk 26`, build-tools correspondants).

### Avec Android Studio (recommande)
1. Ouvrir le dossier du projet dans Android Studio.
2. Laisser Gradle synchroniser (telecharge automatiquement Gradle 8.7, l'AGP et
   toutes les dependances AndroidX/Compose la premiere fois - necessite une
   connexion internet normale, sans les restrictions du sandbox utilise ici).
3. `Build > Generate Signed Bundle / APK` pour un APK signe installable
   directement, ou `Build > Build APK(s)` pour un APK debug rapide.

### En ligne de commande
```bash
./gradlew assembleDebug
# APK genere dans app/build/outputs/apk/debug/app-debug.apk

# Pour une version signee/optimisee :
./gradlew assembleRelease
```
Pour une release signee il faut renseigner un keystore (via
`android.signingConfigs` dans `app/build.gradle.kts` ou passer les proprietes
`-Pandroid.injected.signing.store.file=...` etc. en ligne de commande).

Une fois l'APK genere, transfere-le sur ton telephone et installe-le (autoriser
les sources inconnues si necessaire) - aucun serveur, aucun compte requis.

## Choix techniques

- **Kotlin + Jetpack Compose** pour l'UI : un seul module, moins de boilerplate
  qu'XML/ViewBinding, theming clair (voir `ui/theme`).
- **Room (SQLite embarque)** pour tout le stockage : composants suivis, liens
  produits par fournisseur, historique des prix, remises, bon d'achat, seuil.
  Rien ne quitte l'appareil.
- **WorkManager** (`PeriodicWorkRequest`, contrainte reseau) pour le scraping en
  tache de fond toutes les X heures (3h par defaut, reglable 1-24h dans
  Reglages) - survit aux redemarrages et respecte Doze/App Standby.
- **Jsoup + OkHttp** pour le scraping : OkHttp recupere le HTML (User-Agent
  desktop, timeouts, un throttle global de 5-9s aleatoire entre deux requetes
  quel que soit le site pour rester discret), Jsoup parse le DOM.
- **Extraction "structured data first"** (`StructuredDataExtractor`) : avant
  tout selecteur CSS specifique a un site, l'app cherche les donnees
  schema.org (JSON-LD `Product`/`Offer`, microdata `itemprop=price`) que la
  plupart des sites e-commerce embarquent pour le SEO. C'est nettement plus
  resistant aux refontes visuelles qu'un selecteur CSS fige. Les selecteurs CSS
  par fournisseur (`scraper/*Scraper.kt`) ne servent que de repli si aucune
  donnee structuree n'est trouvee - **a verifier/ajuster en priorite si un
  fournisseur cesse de remonter un prix**, la structure precise des pages
  n'ayant pas pu etre revalidee en direct depuis ce sandbox.
- **Liens produits geres a la main** (ecran "Liens produits" par composant) :
  l'app ne devine pas quelle fiche produit correspond a ton SKU exact sur
  chaque site - tu colles toi-meme l'URL de la fiche qui correspond a ta config
  (ex: la fiche RTX 5070 precise chez LDLC). C'est plus fiable qu'une recherche
  automatique qui pourrait matcher le mauvais produit.
- **Logique de remise par fournisseur** (`domain/DiscountCalculator.kt`) :
  applique le pourcentage propre a chaque fournisseur (LDLC/Materiel.net/Amazon
  3%, Topachat 0%, Fnac/Darty/Boulanger CSE), puis reroute vers Fnac/Darty les
  composants ou l'ecart de prix est le plus faible en priorite, jusqu'a
  consommer entierement le bon d'achat fixe de 130€ - exactement la logique
  demandee (petits ecarts d'abord, pas de bascule couteuse sur CPU/GPU juste
  pour "utiliser" le bon).
- **Seuil automatique** (`domain/ThresholdCalculator.kt`) : moyenne des totaux
  observes sur les 7 premiers jours de suivi, moins 9% de marge. Le seuil reste
  modifiable manuellement a tout moment (bascule automatique/manuel dans
  Reglages).
- **Notification locale** (`NotificationHelper`, `NotificationManagerCompat`) :
  declenchee uniquement au moment ou le total franchit le seuil vers le bas
  (pas a chaque cycle tant qu'on reste en dessous, pour eviter le spam), avec le
  detail fournisseur + prix par composant et un lien direct d'ouverture de
  l'app.

## Structure du projet

```
app/src/main/java/com/pctracker/
  data/                 Provider (enum), Room (entities/dao/db), repository, seed
  scraper/              HttpFetcher, StructuredDataExtractor, scrapers par site
  domain/               DiscountCalculator, ThresholdCalculator (logique pure, testable)
  work/                 PriceScrapeWorker (CoroutineWorker) + WorkScheduler
  notification/         NotificationHelper
  ui/                   Compose : home / settings / links + navigation + theme
```

## Premiers pas apres installation

1. Ouvrir l'app : la configuration cible (CPU 7700X, RTX 5070, etc.) et les
   remises par defaut sont deja pre-remplies.
2. Pour chaque composant, ouvrir l'icone lien et coller l'URL de la fiche
   produit exacte chez chaque fournisseur que tu veux suivre (aucun prix ne
   remontera tant qu'aucun lien n'est renseigne).
3. Regler le bon d'achat (solde + date d'expiration reelle) et les taux de
   remise dans "Reglages" si besoin.
4. Le premier scraping se lance automatiquement (cycle WorkManager) ou via
   l'icone actualiser en haut de l'ecran principal.
