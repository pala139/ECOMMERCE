# Oracle Cloud Always-Free Deployment Guide

## Prerequisites
- GitHub account ✅ (already have)
- Oracle Cloud account (create free: https://www.oracle.com/cloud/free/)
- SSH key pair (generate on your machine)

---

## Step 1: Generate SSH Key (Local Machine)

```bash
ssh-keygen -t rsa -b 4096 -f ~/.ssh/oracle_key
# Press Enter twice for no passphrase
cat ~/.ssh/oracle_key.pub  # Copy this
```

---

## Step 2: Create Oracle Cloud Compute Instance

1. **Sign in** to Oracle Cloud (console.oracle.com)
2. **Create Compute Instance:**
   - Compartment: Default
   - Name: `ecommerce-app`
   - Image: Ubuntu 22.04 (Always Free eligible)
   - Shape: Ampere (ARM) - **Always Free**
   - Add SSH public key from Step 1
   - Create

3. **Note the Public IP** (e.g., `141.xxx.xxx.xxx`)

---

## Step 3: Create MySQL Database (Always Free)

1. **In Oracle Cloud Console:**
   - Service: MySQL Database Service
   - Click "Create DB System"
   - **Configuration:**
     - Name: `ecommerce-db`
     - Description: E-commerce backend database
     - Admin Username: `admin`
     - Admin Password: (create strong password, save it!)
     - DB Name: `ecommerce`
     - Shape: MySQL.8 (Always Free eligible)
     - Data Storage Size: 50 GB (Free)
     - Backup: Unchecked (save space)
   - Create

2. **Wait for creation** (~15 minutes)

3. **Get connection info:**
   - Click on DB System
   - Note the **Private IP** (e.g., `10.0.1.x`)
   - Note the **hostname** (shown in connection string)

---

## Step 4: Connect to Compute Instance

```bash
ssh -i ~/.ssh/oracle_key ubuntu@141.xxx.xxx.xxx
```

---

## Step 5: Install Java & Maven on Compute

```bash
# Update system
sudo apt update
sudo apt upgrade -y

# Install Java 17
sudo apt install -y openjdk-17-jdk

# Install Maven
sudo apt install -y maven

# Verify
java -version
mvn -version
```

---

## Step 6: Clone Your Repository

```bash
# Create app directory
mkdir -p /opt/apps
cd /opt/apps

# Clone your repo
git clone https://github.com/YOUR_USERNAME/ecommerce.git
cd ecommerce
```

---

## Step 7: Configure Database Connection

Edit `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://10.0.1.x:3306/ecommerce?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=admin
spring.datasource.password=YOUR_DB_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Server
server.port=8080
server.servlet.context-path=/api

# JWT Configuration
jwt.secret=your-secret-key-min-32-chars-long-for-security!
jwt.expiration=86400000

# Logging
logging.level.root=INFO
```

---

## Step 8: Build & Deploy

```bash
# Build JAR
mvn clean package -DskipTests

# Run application (background)
nohup java -jar target/ecommerce-backend-1.0.0.jar > app.log 2>&1 &

# Check if running
curl http://localhost:8080/api/products

# View logs
tail -f app.log
```

---

## Step 9: Configure Firewall (Open Port 8080)

In Oracle Cloud Console:

1. **Compute → Instances → ecommerce-app**
2. **Attached VCNs → Go to VCN**
3. **Security Lists → Default Security List**
4. **Add Ingress Rule:**
   - Stateless: Unchecked
   - Protocol: TCP
   - Source: 0.0.0.0/0
   - Destination Port Range: 8080
   - Description: Spring Boot API

---

## Step 10: Test Your API

```bash
# From local machine
curl http://141.xxx.xxx.xxx:8080/api/products

# Or register user
curl -X POST http://141.xxx.xxx.xxx:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "Test123!@#"
  }'
```

---

## Step 11: Setup Auto-Start (Keep Running)

Create systemd service:

```bash
sudo nano /etc/systemd/system/ecommerce.service
```

Paste:

```ini
[Unit]
Description=E-commerce Spring Boot API
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/opt/apps/ecommerce
ExecStart=/usr/bin/java -jar target/ecommerce-backend-1.0.0.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Enable it:

```bash
sudo systemctl daemon-reload
sudo systemctl enable ecommerce
sudo systemctl start ecommerce
sudo systemctl status ecommerce
```

---

## Step 12: Setup Auto-Deploy (Optional)

Auto-update when you push to GitHub:

```bash
# Install webhook
nano /opt/scripts/deploy.sh
```

```bash
#!/bin/bash
cd /opt/apps/ecommerce
git pull origin main
mvn clean package -DskipTests
sudo systemctl restart ecommerce
```

Make executable:
```bash
chmod +x /opt/scripts/deploy.sh
```

---

## Troubleshooting

### Can't connect to database?
```bash
# Test from compute instance
mysql -h 10.0.1.x -u admin -p ecommerce
# Verify hostname/IP in Oracle Console
```

### Application won't start?
```bash
# View detailed logs
sudo journalctl -u ecommerce -f

# Or check nohup log
tail -100 app.log
```

### Port 8080 not accessible?
```bash
# Verify firewall rule exists
sudo ufw status

# If needed:
sudo ufw allow 8080
```

---

## LinkedIn Share Link

Your public API URL:
```
http://141.xxx.xxx.xxx:8080/api
```

Add to LinkedIn:
- **Project URL:** Your GitHub repo
- **Deployed at:** `http://141.xxx.xxx.xxx:8080/api`
- **Live endpoints:** View in TESTING_GUIDE.md

---

## Cost: $0 Always ✅

- **2 ARM Compute Instances** (4 OCPUs, 24GB RAM)
- **MySQL Database Service** (20GB storage)
- **Traffic:** Free
- **No auto-charging** - truly free forever

---

## Next Steps After Deployment

1. Test all endpoints: `http://141.xxx.xxx.xxx:8080/api/products`
2. Add to LinkedIn portfolio
3. Consider adding frontend deployment (React/Angular)
4. Monitor logs: `sudo journalctl -u ecommerce -f`

**Your app is now live! 🚀**
