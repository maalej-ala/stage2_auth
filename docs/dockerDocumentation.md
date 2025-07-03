# Documentation Docker pour le Projet Stage

Ce document fournit des instructions pour la mise en place et l'exécution du projet Stage à l'aide de Docker. Le projet comprend un backend Spring Boot, un frontend Angular et une base de données PostgreSQL, orchestrés avec Docker Compose.

## Structure du Projet

Le projet est situé dans `C:\Users\Lenovo\Desktop\stge2\stage`. Les fichiers clés pour Docker sont :

```
C:\Users\Lenovo\Desktop\stge2\stage\
├── docker-compose.yaml
├── run_docker.bat
├── stop_docker.bat
├── backend_spring\
│   ├── Dockerfile
│   ├── pom.xml
│   ├── mvnw
│   ├── mvnw.cmd
│   ├── .mvn\
│   └── src\
├── frontend_angular19\
│   ├── Dockerfile
│   ├── package.json
│   └── src\
├── docs\
│   └── dockerDocumentation.md
```

*   `docker-compose.yaml`: Définit trois services : `frontend` (Angular), `backend` (Spring Boot) et `db` (PostgreSQL).
*   `backend_spring/Dockerfile`: Construit l'application Spring Boot à l'aide de Maven et Java 21.
*   `frontend_angular19/Dockerfile`: Configure le serveur de développement Angular avec Node.js 22.
*   `run_docker.bat`: Script batch pour démarrer les services Docker Compose.
*   `stop_docker.bat`: Script batch pour arrêter les services Docker Compose.

## Prérequis

Assurez-vous que les éléments suivants sont installés :

### Docker Desktop

*   Inclut Docker et Docker Compose.
*   Téléchargez depuis le [site web de Docker](https://www.docker.com/products/docker-desktop/).

**Vérifier l'installation :**

```bash
docker --version
docker-compose --version
```

**Exemple de sortie :**

```
Docker version 28.1.1, build 4eba377
Docker Compose version v2.35.1-desktop.1
```

## Instructions de Configuration

### 1. Créer le Dockerfile du Backend

1.  Naviguez vers `C:\Users\Lenovo\Desktop\stge2\stage\backend_spring`.
2.  Créez un nouveau fichier nommé `Dockerfile` (clic droit > Nouveau Fichier > Renommer en Dockerfile, sans extension).
3.  Copiez et collez le contenu suivant :

```dockerfile
# Stage 1: Build with Maven
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace/app
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
COPY src src
RUN ./mvnw clean package -DskipTests
# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /workspace/app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "-Dserver.port=8080", "app.jar"]
```

**Vérifier :**

*   Utilise Java 21 (`eclipse-temurin:21-jdk-jammy` pour la construction, `21-jre-jammy` pour l'exécution).
*   Assurez-vous que `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn` et `src` existent dans `backend_spring`.

### 2. Créer le Dockerfile du Frontend

1.  Naviguez vers `C:\Users\Lenovo\Desktop\stge2\stage\frontend_angular19`.
2.  Créez un nouveau fichier nommé `Dockerfile` (sans extension).
3.  Copiez et collez le contenu suivant :

```dockerfile
FROM node:22-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
RUN npm install -g @angular/cli
COPY . .
EXPOSE 4200
CMD ["ng", "serve", "--host", "0.0.0.0"]
```

**Vérifier :**

*   Utilise Node.js 22 (`node:22-alpine`).
*   Assurez-vous que `package.json` et `src` existent dans `frontend_angular19`.

### 3. Créer le Fichier Docker Compose

1.  Naviguez vers `C:\Users\Lenovo\Desktop\stge2\stage`.
2.  Créez un nouveau fichier nommé `docker-compose.yaml` (notez l'extension `.yaml`).
3.  Copiez et collez le contenu suivant :

```yaml
services:
  frontend:
    build: ./frontend_angular19
    ports:
      - "4200:4200"
    volumes:
      - ./frontend_angular19:/app
      - /app/node_modules
    environment:
      - CHOKIDAR_USEPOLLING=true
      - NG_CLI_ANALYTICS=false
    stdin_open: true
    tty: true
    working_dir: /app
    networks:
      - app-network
  backend:
    build: ./backend_spring
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/mydb
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=ala112003
    depends_on:
      db:
        condition: service_healthy
    restart: unless-stopped
    networks:
      - app-network
  db:
    image: postgres:17
    environment:
      POSTGRES_DB: mydb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ala112003
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 5s
      retries: 5
    networks:
      - app-network
volumes:
  postgres_data:
networks:
  app-network:
    driver: bridge
```

**Vérifier :**

*   Les chemins de répertoire (`./frontend_angular19`, `./backend_spring`) sont relatifs à `C:\Users\Lenovo\Desktop\stge2\stage`.
*   Le mot de passe PostgreSQL (`ala112003`) est défini. Mettez-le à jour dans `docker-compose.yaml` et `backend_spring/src/main/resources/application-dev.yml` s'il est modifié pour des raisons de sécurité.

### 4. Démarrer Docker Desktop

*   Ouvrez Docker Desktop et assurez-vous qu'il est en cours d'exécution avant d'exécuter toute commande Docker.

## Exécution des Services

Pour démarrer le backend, le frontend et la base de données dans des conteneurs Docker :

1.  Ouvrez une invite de commande.
2.  Naviguez vers le répertoire du projet :

```bash
cd C:\Users\Lenovo\Desktop\stge2\stage
```

### Option 1: Utilisation du Script Batch

*   Exécutez le script batch :

```bash
run_docker.bat
```

### Option 2: Utilisation du Terminal

*   Exécutez la commande suivante dans le terminal pour démarrer les services sans le script batch :

```bash
docker-compose -f docker-compose.yaml up --build -d
```

*   `-f docker-compose.yaml`: Spécifie le fichier de configuration.
*   `--build`: Reconstruit les images si des modifications sont détectées.
*   `-d`: Exécute les conteneurs en mode détaché (en arrière-plan).

**Accéder aux services :**

*   **Frontend**: [http://localhost:4200](http://localhost:4200) (application Angular).
*   **Backend**: [http://localhost:8080](http://localhost:8080) (testez via Postman ou le navigateur).
*   **Base de données**: Connectez-vous à `localhost:5432` à l'aide d'un client PostgreSQL (par exemple, pgAdmin, psql) :
    *   **Base de données**: `mydb`
    *   **Utilisateur**: `postgres`
    *   **Mot de passe**: `ala112003`

## Arrêt des Services

Pour arrêter les services tout en préservant les données de la base de données :

1.  Naviguez vers le répertoire du projet :

```bash
cd C:\Users\Lenovo\Desktop\stge2\stage
```

2.  Exécutez le script d'arrêt :

```bash
stop_docker.bat
```

## Dépannage

### `docker-compose.yaml` Introuvable :

*   Assurez-vous que le fichier est nommé `docker-compose.yaml` dans `C:\Users\Lenovo\Desktop\stge2\stage`.
*   S'il est nommé `docker-compose.yml`, renommez-le ou mettez à jour les scripts pour utiliser `-f docker-compose.yml`.

### Échecs de Construction :

*   Vérifiez que `pom.xml` (backend) et `package.json` (frontend) ont des dépendances valides.
*   Assurez-vous d'avoir un accès Internet pour télécharger les images (`node:22-alpine`, `eclipse-temurin:21-jdk-jammy`, `postgres:17`).

### Conflits de Ports :

*   Si les ports `4200`, `8080` ou `5432` sont utilisés, arrêtez les processus en conflit ou modifiez la configuration des ports dans `docker-compose.yaml`.

### Problèmes de Connexion à la Base de Données :

*   Assurez-vous que `backend_spring/src/main/resources/application-dev.yml` correspond à :

```yaml
spring:
  datasource:
    url: jdbc:postgresql://db:5432/mydb
    username: postgres
    password: ala112003
```

*   Vérifiez la santé de la base de données : `docker logs stage-db-1`.

## Notes Supplémentaires

### Persistance de la Base de Données :

*   Le volume `postgres_data` persiste les données à moins d'être supprimé avec `docker-compose -f docker-compose.yaml down -v`.


