# Use a lightweight official Node.js base image
FROM node:20-alpine

# Set the working directory inside the container
WORKDIR /usr/src/app

# Copy package.json and package-lock.json first for optimal layer caching
COPY package*.json ./

# Install only production dependencies for a smaller, faster build
RUN npm ci --only=production

# Copy the rest of the application source code
COPY . .

# Expose the port that the app will run on.
# Render automatically injects the PORT environment variable.
EXPOSE 3000

# Start the application using Node.js
CMD ["node", "server.js"]
