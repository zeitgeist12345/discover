# Use official Cloudflared image
FROM cloudflare/cloudflared:2025.8.1

# Copy tunnel credentials and config
COPY 6a9960c7-1fe2-4565-a583-6a0a3cd16d31.json /etc/cloudflared/credentials.json
COPY config.yml /etc/cloudflared/config.yml

# Set the entrypoint to run the tunnel via the cloudflared binary
ENTRYPOINT ["/usr/local/bin/cloudflared", "tunnel", "--config", "/etc/cloudflared/config.yml", "run"]
