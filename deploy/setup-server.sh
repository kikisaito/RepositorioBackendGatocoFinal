#!/bin/bash
# Setup script for Ubuntu/Debian EC2 instance
# Run this once as root or with sudo

set -e

echo "Updating system..."
sudo apt-get update -y

echo "Installing Java (Amazon Corretto 17)..."
wget -O- https://apt.corretto.aws/corretto.key | sudo apt-key add - 
sudo add-apt-repository 'deb https://apt.corretto.aws stable main'
sudo apt-get update -y
sudo apt-get install -y java-17-amazon-corretto-jdk

echo "Installing Authbind..."
sudo apt-get install -y authbind

echo "Creating deploy user..."
if id "deploy" &>/dev/null; then
    echo "User 'deploy' already exists."
else
    sudo useradd -m -s /bin/bash deploy
    echo "User 'deploy' created."
fi

echo "Configuring Authbind for port 80..."
sudo touch /etc/authbind/byport/80
sudo chown deploy /etc/authbind/byport/80
sudo chmod 500 /etc/authbind/byport/80

echo "Setting up application directory..."
sudo mkdir -p /home/deploy/app
sudo chown -R deploy:deploy /home/deploy/app

echo "Allowing deploy user to manage myapp.service..."
echo "deploy ALL=(ALL) NOPASSWD: /bin/systemctl start myapp.service, /bin/systemctl stop myapp.service, /bin/systemctl restart myapp.service, /bin/systemctl daemon-reload" | sudo tee /etc/sudoers.d/deploy-myapp

echo "Setup completed successfully!"
