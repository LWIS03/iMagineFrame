# Use Node.js as the base image for building the frontend
FROM node:18-alpine AS build
LABEL authors="s0192454 (MDB)"

ARG ENVIRONMENT_MODE=production

# Set the working directory inside the container
WORKDIR /app

# Copy package.json and package-lock.json before installing dependencies
COPY ./package.json ./package-lock.json ./

# Install dependencies
RUN npm ci
# Copy the rest of the application files
COPY . .

# Build the Vue.js app
RUN npm run build -- --mode $ENVIRONMENT_MODE

# Use Nginx as the base image for serving the frontend
FROM node:18-alpine AS runtime
LABEL authors="s0192454 (MDB)"

WORKDIR /app

RUN npm install -g serve

# Copy the built files from the build stage to Nginx's HTML directory
COPY --from=build /app/dist ./dist

# Expose port 80 for web traffic
EXPOSE 3000

# Start Nginx
CMD ["serve", "-s", "dist"]
