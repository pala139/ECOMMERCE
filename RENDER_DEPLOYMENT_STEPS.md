# Render Deployment Test & Next Steps

## Step 1: Test Database Connection (Local)

Run this command to verify TiDB connection:

```bash
mysql -h gateway01.ap-southeast-1.prod.alicloud.tidbcloud.com \
  -P 4000 \
  -u root \
  -p0Jvlt96TTIart05B \
  -e "SELECT 1;"
```

**Expected output:**
```
+---+
| 1 |
+---+
| 1 |
+---+
```

If successful ✅ → Move to Step 2
If failed ❌ → Check password, TiDB IP whitelist

---

## Step 2: Create Database in TiDB

```bash
mysql -h gateway01.ap-southeast-1.prod.alicloud.tidbcloud.com \
  -P 4000 \
  -u root \
  -p0Jvlt96TTIart05B \
  -e "CREATE DATABASE IF NOT EXISTS ecommerce;"
```

Verify:
```bash
mysql -h gateway01.ap-southeast-1.prod.alicloud.tidbcloud.com \
  -P 4000 \
  -u root \
  -p0Jvlt96TTIart05B \
  -e "SHOW DATABASES;" | grep ecommerce
```

---

## Step 3: Update application.properties (Local Test)

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://gateway01.ap-southeast-1.prod.alicloud.tidbcloud.com:4000/ecommerce?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=0Jvlt96TTIart05B
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

server.port=8080
server.servlet.context-path=/api

jwt.secret=nitishpala32CharacterSecretKeyForJWT!
jwt.expiration=86400000

logging.level.root=INFO
```

---

## Step 4: Test Locally

```bash
# Clean build
mvn clean package -DskipTests

# Run locally
java -jar target/ecommerce-backend-1.0.0.jar

# In another terminal, test endpoint
curl http://localhost:8080/api/products
```

**Expected:** `[]` (empty array - database connected!)

If successful ✅ → Tables created automatically
If failed ❌ → Check logs: `tail -20 nohup.out`

---

## Step 5: Revert application.properties (Important!)

**DO NOT COMMIT** password to GitHub!

Change back to use environment variables:

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.datasource.driver-class-name=${SPRING_DATASOURCE_DRIVER_CLASS_NAME}

spring.jpa.hibernate.ddl-auto=${SPRING_JPA_HIBERNATE_DDL_AUTO}
spring.jpa.show-sql=${SPRING_JPA_SHOW_SQL}
spring.jpa.properties.hibernate.dialect=${SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT}

server.port=${SERVER_PORT:8080}
server.servlet.context-path=${SERVER_SERVLET_CONTEXT_PATH:/api}

jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION}

logging.level.root=${LOGGING_LEVEL_ROOT}
```

---

## Step 6: Commit & Push

```bash
git add .
git commit -m "Add environment variable support for cloud deployment"
git push origin main
```

---

## Step 7: Deploy to Render

### 7A: Create Web Service on Render

1. **Go to render.com**
2. **Dashboard → Create → Web Service**
3. **Connect** your GitHub repo
4. **Configure:**
   - Name: `ecommerce-api`
   - Environment: `Docker` or `Node` → Change to `Web Service`
   - Build Command: `mvn clean package -DskipTests`
   - Start Command: `java -jar target/ecommerce-backend-*.jar`
   - Plan: Free (with 15min sleep)

### 7B: Add Environment Variables

1. **Go to Service → Environment**
2. **Copy-paste these variables:**

```
SPRING_DATASOURCE_URL=jdbc:mysql://FEStpQ5aKjWkWdK.root:0Jvlt96TTIart05B@gateway01.ap-southeast-1.prod.alicloud.tidbcloud.com:4000/ecommerce?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=0Jvlt96TTIart05B
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver

SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false
SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=org.hibernate.dialect.MySQL8Dialect

SERVER_PORT=8080
SERVER_SERVLET_CONTEXT_PATH=/api

JWT_SECRET=nitishpala32CharacterSecretKeyForJWT!
JWT_EXPIRATION=86400000

LOGGING_LEVEL_ROOT=INFO
```

3. **Mark as Secret 🔒:**
   - `SPRING_DATASOURCE_URL`
   - `SPRING_DATASOURCE_PASSWORD`
   - `JWT_SECRET`

4. **Save & Deploy** → Render auto-builds from GitHub

---

## Step 8: Test Live Deployment

Wait 3-5 minutes for build to complete.

```bash
# Get your Render URL from dashboard
# Format: https://your-app-name.onrender.com

curl https://your-app-name.onrender.com/api/products

# Should return: []
```

---

## Step 9: Test Full API Flow

### Register User:
```bash
curl -X POST https://your-app-name.onrender.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "Test123!@#"
  }'
```

**Expected response:**
```json
{
  "message": "Registration successful",
  "token": "eyJhbGc..."
}
```

### Login:
```bash
curl -X POST https://your-app-name.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!@#"
  }'
```

---

## Step 10: Add to LinkedIn

Once verified working ✅:

**LinkedIn Project Details:**
- **Project Name:** Spring Boot E-Commerce REST API Backend
- **URL:** https://github.com/your-username/ecommerce
- **Live Endpoint:** https://your-app-name.onrender.com/api
- **Description:** Full-stack Spring Boot REST API with JWT auth, TiDB MySQL database, input validation, and comprehensive testing.
- **Skills:** Spring Boot 3.x, REST API, JWT Authentication, MySQL, TiDB, Render

---

## Troubleshooting

### Build fails?
```bash
# Check logs in Render Dashboard
# Common: Java not found - use correct Maven build command
```

### Database connection fails?
```bash
# Check TiDB IP whitelist allows Render IPs
# In TiDB Cloud: Project Settings → Project Whitelist
# Add: 0.0.0.0/0 (allow all - for demo)
```

### 15-minute sleep issue?
```bash
# Free Render instances sleep after 15 min inactivity
# First request wakes it (30 sec delay)
# Upgrade to paid if needed for demos
```

---

## Summary

✅ **Test DB connection locally**
✅ **Build & run locally** 
✅ **Commit code to GitHub**
✅ **Add to Render with env vars**
✅ **Wait for build (3-5 min)**
✅ **Test live endpoints**
✅ **Add to LinkedIn**

**Your live API will be at:** `https://your-app-name.onrender.com/api` 🚀
