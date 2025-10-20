FROM node:18-alpine

WORKDIR /app

# Copy package files and install dependencies first (cache)
COPY package*.json ./
RUN npm install --production

# Copy source code
COPY . .

# Expose port
EXPOSE 3000

# Start the application
CMD ["npm", "start"]
