#!/usr/bin/env bash
#set -euo pipefail

# Minimal bootstrap for Ubuntu 22.04 LTS
# Usage: sudo ./setup-vm.sh [certs]

echo "==> Update packages"
apt update
apt upgrade -y

echo "==> Install prerequisites"
apt install -y ca-certificates curl gnupg lsb-release git nginx

echo "==> Install Docker"
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
mkdir -p /etc/apt/keyrings || true
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
apt update
apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

echo "==> Prepare directories"
mkdir -p /var/www/certbot
chown $(whoami):$(whoami) /var/www/certbot || true

echo "==> Clone repo"
cd /home/$(whoami) || exit 1
if [ ! -d smartfashion ]; then
  git clone https://github.com/Rodopqsi/SmartFashion.git smartfashion
fi
cd smartfashion/deploy || exit 1

if [ ! -f .env ]; then
  cp .env.example .env
  echo "Created .env from .env.example — edit .env and set secrets, then re-run this script with 'certs' to issue certificates."
fi

echo "==> Build and start containers"
docker compose up -d --build

if [ "${1:-}" = "certs" ]; then
  echo "==> Installing certbot (snap)"
  snap install core; snap refresh core
  snap install --classic certbot
  ln -s /snap/bin/certbot /usr/bin/certbot || true

  echo "==> Stop host nginx to free port 80"
  systemctl stop nginx || true

  echo "==> Request certificates for smarthfashion.shop and www.smarthfashion.shop"
  certbot certonly --webroot -w /var/www/certbot -d smarthfashion.shop -d www.smarthfashion.shop --agree-tos --no-eff-email -m your-email@example.com

  echo "==> Deploy nginx configs and reload"
  cp ./nginx/smartfashion.ssl.conf /etc/nginx/sites-available/smartfashion.ssl.conf
  ln -sf /etc/nginx/sites-available/smartfashion.ssl.conf /etc/nginx/sites-enabled/smartfashion.ssl.conf
  cp ./nginx/smartfashion.conf /etc/nginx/sites-available/smartfashion.conf
  ln -sf /etc/nginx/sites-available/smartfashion.conf /etc/nginx/sites-enabled/smartfashion.conf
  systemctl start nginx
  nginx -t
  systemctl reload nginx
  echo "Done. Certificates should be in /etc/letsencrypt/live/smarthfashion.shop"
fi

echo "All done. If you edited .env after first run, re-run 'sudo docker compose up -d --build' to apply changes."
