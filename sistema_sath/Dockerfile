# ──────────────────────────────────────────────────────────────────────────────
#  ServiceDesk — imagen de la aplicación (frontend Vue + backend Spring Boot)
#  Etapa 1: compila el frontend  →  Etapa 2: empaqueta el .jar  →  Etapa 3: runtime
# ──────────────────────────────────────────────────────────────────────────────

# ── 1. Frontend (Vue 3 + Vite) ────────────────────────────────────────────────
FROM node:22-alpine AS frontend
WORKDIR /build/Sath-frontend
COPY Sath-frontend/package.json Sath-frontend/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY Sath-frontend/ ./
# vite.config.js publica el build en ../src/main/resources/static
RUN mkdir -p ../src/main/resources/static && npm run build

# ── 2. Backend (Spring Boot + Maven) ──────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /build
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY src ./src
COPY --from=frontend /build/src/main/resources/static ./src/main/resources/static
RUN mvn -B -q -DskipTests package && cp target/*.jar /build/app.jar

# ── 3. Runtime ────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine
# JDK (no JRE): JasperReports compila los .jrxml en tiempo de ejecución y necesita javac
# ffmpeg: requerido por FileStorageService (evidencias multimedia)
# fontconfig + ttf-dejavu: requeridos por JasperReports / AWT para generar PDFs
RUN apk add --no-cache ffmpeg fontconfig ttf-dejavu tzdata
WORKDIR /app
COPY --from=backend /build/app.jar app.jar
RUN mkdir -p /app/uploads /app/logs
ENV TZ=America/La_Paz \
    FILE_STORAGE_DIR=/app/uploads \
    LOG_DIR=/app/logs \
    JAVA_OPTS="-Xms256m -Xmx768m -Djava.awt.headless=true"
EXPOSE 9006
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
