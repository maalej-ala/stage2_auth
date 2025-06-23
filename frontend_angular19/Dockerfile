# Stage 1: Use Node.js to build and serve Angular locally
FROM node:22-alpine

# Set working directory
WORKDIR /app

# Copy package.json and package-lock.json
COPY package*.json ./

# Install dependencies
RUN npm install

# Install Angular CLI globally (optional but useful)
RUN npm install -g @angular/cli

# Copy the rest of the app
COPY . .

# Expose the Angular dev server port
EXPOSE 4200

# Run Angular app in development mode (can access at localhost:4200)
CMD ["ng", "serve", "--host", "0.0.0.0"]
