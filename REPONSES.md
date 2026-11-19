# TP3 – Platform Channels : réponses

Projet IoT : **Gant gestuel** (accéléromètre / IMU + vibration).

---

## Questions de compréhension

### 1. Différence entre MethodChannel et EventChannel ? Exemple de chaque dans votre projet IoT.

Un **MethodChannel** fonctionne comme un appel téléphonique : Flutter pose **une question** et reçoit **une seule réponse** (`Future` côté Dart, `result.success/error` côté Kotlin). Il sert aux actions ponctuelles.

Un **EventChannel** fonctionne comme une radio : Kotlin **diffuse un flux de valeurs** (`Stream` côté Dart, `events.success(...)` côté Kotlin) tant que Flutter écoute. Il sert aux données continues.

Exemples dans le projet du gant :
- **MethodChannel** `gant/vibrer` : Flutter demande une vibration ponctuelle quand un geste est reconnu (réponse unique : « fait »).
- **EventChannel** `gant/imu` : Kotlin envoie en continu les valeurs de l'accéléromètre (x, y, z) via `SensorManager`.

### 2. Pourquoi le hot reload ne suffit-il pas après une modification du code Kotlin ?

Le hot reload ne recharge que le **code Dart** dans la machine virtuelle en cours d'exécution. Le code Kotlin est **compilé dans l'APK** Android, qui est installé sur le téléphone. Pour qu'une modification Kotlin soit prise en compte, il faut **arrêter l'application, la recompiler et la réinstaller** (`q` puis `flutter run`).

### 3. Que se passe-t-il côté Dart si une branche du `when` Kotlin oublie d'appeler `result.success` ? Et si elle l'appelle deux fois ?

- **Aucune réponse** : le `await` côté Dart **n'est jamais terminé**. Le `Future` reste bloqué indéfiniment et l'interface qui attend ce résultat ne se met jamais à jour.
- **Deux réponses** : la deuxième lève l'erreur `Reply already submitted` et **fait planter l'application**. Un appel ne peut recevoir qu'une seule réponse.

Chaque chemin du `when` doit donc finir par **exactement un** `success`, `error` ou `notImplemented`.

### 4. À l'étape 4, pourquoi lit-on le micro dans `Dispatchers.IO`, puis envoie-t-on la valeur dans `Dispatchers.Main` ?

- `micro.read(...)` est un appel **bloquant**. Dans le thread principal, il gèlerait l'interface (risque de message « Application ne répond pas »). On le lance donc dans `Dispatchers.IO`, un thread de travail adapté aux opérations bloquantes.
- Les messages vers Flutter via `events.success(...)` doivent être envoyés depuis le **thread principal** (`Dispatchers.Main`). C'est pourquoi on revient dessus avec `withContext(Dispatchers.Main)` pour envoyer chaque valeur.

### 5. Rôle de `onCancel` ? Que se passe-t-il si on n'y libère pas le capteur ou le micro ?

`onCancel` est appelé quand **Flutter arrête d'écouter** le flux (par exemple après `cancel()` ou dans `dispose()`). Il sert à **libérer les ressources** : désinscrire le listener du capteur (`unregisterListener`), ou arrêter et libérer le micro (`stop()` / `release()`).

Sans cela, le capteur ou le micro **continue de tourner en arrière-plan** : la batterie se vide, le micro reste occupé et peut ne plus être utilisable par une autre application, et le flux continue à produire des données que personne n'écoute (fuite de ressources).

### 6. Pourquoi `MainActivity` garde-t-elle `resultatPermission` au lieu de répondre tout de suite à Dart ?

La boîte de dialogue de permission appartient à l'**activité Android**, et la réponse de l'utilisateur arrive **plus tard**, de façon asynchrone, dans `onRequestPermissionsResult`. On ne connaît donc pas la réponse (accordée ou refusée) au moment de l'appel. On **garde le `result` en attente** pour répondre à Dart (`true` / `false`) seulement quand l'utilisateur a choisi : « je vous rappelle ».

### 7. Le plugin `battery_plus` donne la batterie « sans Kotlin ». Expliquez pourquoi c'est faux.

C'est faux pour le développeur de l'app, qui n'écrit pas de Kotlin, mais pas pour le plugin. Le plugin `battery_plus` **contient lui-même du code natif** (dossier `android/` du plugin) : un **MethodChannel** côté Kotlin/Java qui appelle l'API Android **`BatteryManager`**. Un plugin Flutter, c'est du **code natif écrit par quelqu'un d'autre** : exactement ce que l'on fait dans ce TP. Sans canal et sans API Android, Flutter (Dart) n'a aucun accès direct à la batterie.

---

## Fiche du module (exercice 4)

| Élément | Contenu |
|---|---|
| **Projet** | Gant gestuel |
| **Module natif proposé** | IMU (accéléromètre) du téléphone à haute fréquence + vibration au geste reconnu |
| **Canaux** | `gant/imu` (flux de mesures), `gant/vibrer` (vibration) |
| **Type** | `gant/imu` : **EventChannel** ; `gant/vibrer` : **MethodChannel** |
| **API Android** | `SensorManager` (capteur `TYPE_ACCELEROMETER`), `Vibrator` |
| **Permission** | `VIBRATE` (permission normale : aucune demande à l'utilisateur) |

**Remarque :** pour une fréquence d'échantillonnage élevée, il faut utiliser `SENSOR_DELAY_GAME` ou `SENSOR_DELAY_FASTEST`. Au-delà de 200 Hz sous Android 12 et plus, la permission `HIGH_SAMPLING_RATE_SENSORS` est nécessaire.
