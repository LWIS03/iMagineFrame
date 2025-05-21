# iMagineFrame
# iMagineLab Management Platform

## Overview

This project is a web-based management platform built for **iMagineLab**, a student-driven technology club at the University of Antwerp. It aims to support and automate the daily operations of the club, replacing manual administrative tasks with an integrated, user-friendly application.

The platform centralizes member management, inventory control, payment processing, event planning, and data reporting into a single interface, accessible on both desktop and mobile devices.

## Tech Stack

- **Backend:** Java, Spring Boot  
- **Testing:** JUnit, Mockito  
- **Frontend:** Vue.js, Node.js  
- **API:** RESTful endpoints for integration with external services and mobile clients

## Key Features

### User Management
- Secure login with username and NFC membership card
- Role-based access control via groups and permissions
- Support for external authentication (LDAP/OAuth)
- GDPR-compliant personal data handling

### Stock Management
- Categorized inventory for drinks and electronic components
- Batch-based product tracking with support for different prices and expiration dates
- Tag system for efficient classification and filtering
- Low-stock and expiration notifications
- Barcode scanner integration

**I was responsible for the complete implementation of the Stock Management module, including both backend and frontend development.**

### Payment Platform
- Prepaid credit system using "iMagineCoins"
- Manual and online credit top-up (e.g., Payconiq, PayPal)
- Shopping list management for self-service purchases
- Administrator-only features for credit handling and product adjustments

### Events and Projects
- Project and event creation and management by administrators
- Team collaboration features for iMagineers
- Attendance tracking with privacy options
- REST API endpoints for integration with mobile apps and websites

### Reporting and Analytics
- Inventory, financial, and participation reports
- Analytics on user activity and product trends
- Export to PDF
- GDPR-compliant insights and behavior analysis

### Additional Features
- Centralized portal for quick access to related services
- Responsive design for mobile access
- Optional gamification and rewards system

## My Contributions

As a developer on this project, I implemented the following components:

### Backend (Java, Spring Boot)

- **Controllers:**
  - `BatchController`
  - `ProductController`
  - `TagController`
- **Models:**
  - `Batch`
  - `Product`
  - `Tag`
- **DTOs:**
  - Batch DTOs
  - Product DTOs
  - Tag DTOs
- **Services:**
  - `BatchService`
  - `ProductService`
- **Testing:**
  - Unit and integration tests using **Mockito** and **JUnit**

### Frontend (Vue.js, Node.js)

- `product.ts`
- `Product.vue`
- `ProductEdit.vue`

These files are responsible for the product listing UI, product editing interface, and the TypeScript logic for handling product data in the frontend.

## Deployment

The platform is designed to run on a three-stage deployment pipeline, hosted on-premise:

1. **Development (DVL):** Individual environments for each application
2. **Testing (TST):** Shared beta environment with limited access
3. **Production (PROD):** High-availability setup managed by internal IT

Docker is used for containerization and deployment across all stages.

## API

The backend exposes a RESTful API that allows integration with external applications and mobile clients. Documentation is included in the `/docs/api` folder.
