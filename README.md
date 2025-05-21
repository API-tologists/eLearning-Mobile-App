# 📱 Application Mobile d'Apprentissage de Compétences

Une application mobile Android permettant à des utilisateurs (étudiants/apprenants) de suivre des cours, progresser à leur rythme, obtenir des certificats, et aux instructeurs de publier et gérer des contenus pédagogiques.

---

## 🚀 Fonctionnalités principales

### 👩‍🎓 Pour les Étudiants :
- Création de compte et authentification
- Parcours de cours, inscription et suivi de progression
- Accès aux leçons multimédia (texte, vidéo, PDF, etc.)
- Réalisation de quiz
- Obtention de certificats
- Notation des cours

### 👨‍🏫 Pour les Instructeurs :
- Création de compte instructeur
- Création de cours, sections et leçons
- Ajout de quiz
- Suivi des inscriptions (versions futures)

---

## 🛠️ Technologies utilisées

- **Langage :** Kotlin
- **UI :** Jetpack Compose (Material 3, Navigation Compose)
- **Backend :** Firebase Firestore, Firebase Auth
- **IDE :** Android Studio
- **IA Générative :** Google Generative AI SDK (v0.6.0)

---

## ⚙️ Installation & Exécution

### 1. 📦 Prérequis

- [Android Studio](https://developer.android.com/studio) installé
- Compte Firebase avec projet configuré
- Clé API pour **Google Generative AI SDK** (optionnel si activé)
- Connexion internet

### 2. 🧱 Cloner le projet


```bash
git clone https://github.com/API-tologists/eLearning-Mobile-App.git
cd eLearning-Mobile-App
```


### 3. Configuration Firebase

1. Allez sur [Firebase Console](https://console.firebase.google.com/) et créez un nouveau projet.
2. Activez les services suivants :
   - **Authentication** : Email/Password
   - **Cloud Firestore** : pour les données utilisateurs, cours, quiz, etc.
   - **Storage** : pour stocker des images, vidéos, et fichiers PDF.
3. Téléchargez le fichier `google-services.json`.
4. Placez le fichier dans le dossier `app/` de votre projet Android

### 4. Configuration du SDK IA Générative (optionnel)

1. Créez un projet sur [Google Cloud Console](https://console.cloud.google.com/).
2. Activez l'API **Generative Language**.
3. Accéder à https://aistudio.google.com/app/apikey et créez une **clé API**.
4. Creé un fichier dans res/values, et ajoutez :```<name="gemini_api_key">Votre_Clé_API </string>```

## 👨‍💻 Auteurs

- Haitam Bidiouane  
- Wail Yacoubi  
- Yassine El Moudni  
- Mohammed Arafa Fengiro

**Encadré par :** Pr. Guermah Hatim
