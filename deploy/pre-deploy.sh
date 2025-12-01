#!/bin/bash
# Pre-deploy script
echo "Starting pre-deploy steps..."

# Stop the service if it's running
if systemctl is-active --quiet myapp.service; then
    echo "Stopping myapp.service..."
    sudo systemctl stop myapp.service
else
    echo "myapp.service is not running."
fi

# Create backup directory if not exists
mkdir -p /home/deploy/backups

# Backup existing jar if exists
if [ -f /home/deploy/app/app.jar ]; then
    echo "Backing up existing jar..."
    cp /home/deploy/app/app.jar /home/deploy/backups/app-$(date +%Y%m%d%H%M%S).jar
fi

echo "Pre-deploy steps completed."
