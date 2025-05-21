# iMagineFrame Frontend

The fronted part of the project for iMagineLab called iMagineFrame.

## Features

The application has many pages, listed below. These pages are designed to be as easy to use possible.

- Public pages

    - Welcoming page
    - Registration page
    - Login page
- User home page
- Users can edit their profile
- Management pages

    - User management
    - Group management
    - Registrations overview to approve or decline
- Management and user pages

    - Products
    - Events
    - Projects


## Deployment

Before you can start you need to clone this git repository.

### 1. Configuring

In this repository there is a file called `.env.production` in this file you need to set the URL the backend will be running on.

### 2. Building

Next we need to build a docker image we can run later. To build an image a Dockerfile is provided.

To build an image run:

```bash
sudo docker build --no-cache -t imagineframe-frontend:latest . 
```

### 2. Running

To run the image use the following command or adjust some parameter. 
> [!caution]
> Only adjust the port for the outside, NEVER change the inside port.

```bash
sudo docker run -d -p 3002:3000 --restart unless-stopped --name imf-frontend imagineframe-frontend:latest
```

## Technologies

- Vue
- Vuetify
- NPM

## Getting Started - Development

First install the dependencies:

```bash
npm install
```
This will create a node_modules folder in the project root. Do not commit this folder to git!

Then start the development server:

```bash
npm run dev
```

This will start a development server on port 3000. You can access the application at http://localhost:3000.


