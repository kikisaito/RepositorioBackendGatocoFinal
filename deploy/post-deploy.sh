#!/bin/bash
# Post-deploy script
echo "Starting post-deploy steps..."

# Reload systemd daemon in case service file changed
sudo systemctl daemon-reload

# Start the service
echo "Starting myapp.service..."
sudo systemctl start myapp.service

# Check status
if systemctl is-active --quiet myapp.service; then
    echo "Deployment successful! Service is running."
else
    echo "Service failed to start. Check logs with 'journalctl -u myapp.service'."
    exit 1
fi

echo "Post-deploy steps completed."
