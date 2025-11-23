# Deploying SmartFashion with Docker Compose

This `deploy/` folder contains example artifacts to run the project on a single VM using Docker Compose and an Nginx reverse proxy.

Files included:
- `docker-compose.yml` - orchestrates MySQL, Django backend, Admin (Spring Boot), Frontend (built and served), and `proxy` (nginx)
- `docker/` - Dockerfiles for `backend`, `frontend` and `admin` build stages
- `nginx/` - example nginx site config (`smartfashion.conf`)
- `.env.example` - template for environment variables
- `setup-vm.sh` - bootstrap script for Ubuntu 22.04 to install Docker, clone the repo and start the stack

Quick start (on the VM):

1. Install Docker & Docker Compose (Ubuntu example):

```bash
sudo apt update
sudo apt install -y ca-certificates curl gnupg lsb-release
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin nginx
sudo usermod -aG docker $USER
```

2. Copy the `.env.example` to `.env` and fill secrets:

```bash
cp .env.example .env
# edit .env
```

3. From this `deploy/` directory run:

```bash
# build and start
sudo docker compose up -d --build
```

4. Configure DNS: point your domain `A` record to the VM public IP. 

5. Obtain TLS certs (after DNS resolves) with Certbot (on the host):

```bash
# replace with your domain
sudo certbot --nginx -d smarthfashion.shop -d www.smarthfashion.shop
```

Notes & adjustments:
- After first deploy, you'll likely want to run Django migrations inside the `backend` container and create a superuser:

```bash
sudo docker compose exec backend python manage.py migrate
sudo docker compose exec backend python manage.py createsuperuser
```

- If you prefer managed database, modify `docker-compose.yml` to remove the `db` service and point `backend`/`admin` to your managed DB host.
- The `admin` service builds the Spring Boot jar using Maven inside the container; if you prefer to build locally, change the `admin` service to use an image from a registry.

- I updated `deploy/nginx/smartfashion.conf` and added `deploy/nginx/smartfashion.ssl.conf` configured for `smarthfashion.shop`.
- Use `deploy/setup-vm.sh` on an Ubuntu 22.04 VM to bootstrap Docker, clone the repo, and start the compose stack. Run `sudo ./setup-vm.sh certs` to request Let's Encrypt certificates once DNS A record points to the VM.

Rollback / Restore:
- We keep runtime data in the `db_data` volume. Back up DB using `mysqldump` to object storage or to the host regularly.

If you want, I can now:
- generate the minimal commands to create the OCI VM, open ports, and configure Hostinger DNS records,
- or produce sample `ufw` rules and a basic monitoring checklist.
