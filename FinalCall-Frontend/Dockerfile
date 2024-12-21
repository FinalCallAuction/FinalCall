# Use a Node.js image to build the application
FROM node:18 AS build

# Set the working directory in the container
WORKDIR /app

# Copy the package.json and package-lock.json files
COPY package.json package-lock.json ./

# Install dependencies
RUN npm install

# Copy the rest of the application code
COPY . ./

# Build the application
RUN npm run build

# Use an Nginx image to serve the built application
FROM nginx:stable-alpine

# Copy the build output to the Nginx HTML directory
COPY --from=build /app/build /usr/share/nginx/html

# Expose the port the application runs on
EXPOSE 80

# Command to start Nginx
CMD ["nginx", "-g", "daemon off;"]
