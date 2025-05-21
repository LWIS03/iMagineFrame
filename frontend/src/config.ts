const config = {
  API_URL : import.meta.env.VITE_API_URL,
  NAVIGATION: [ // Describes the links in the navigation bar
    {
      name: 'Home',
      location: '/',
      icon: 'mdi-home',
    },
    {
      name: 'My profile',
      location: '/profile',
      icon: 'mdi-account',
    },
    {
      name: 'My Requests',
      location: '/my-requests',
      icon: 'mdi-clipboard-account',
    },
    {
      name: "Dashboard",
      location: "/dashboard",
      icon: "mdi-clipboard-check"
    }
  ],
  DASHBOARD_TILES: [
    {
      name: "Event Management",
      location: "/events",
      icon: "mdi-calendar-text",
    },
    {
      name: "Project Management",
      location: "/projects",
      icon: "mdi-clipboard-check-outline"
    },
    {
      name: 'Project Requests',
      location: '/project-requests',
      icon: 'mdi-email-alert',
    },
    {
      name: "Product Management",
      location: "/products",
      icon: "mdi-package-variant"
    },
    {
      name: 'User management',
      location: '/users',
      icon: 'mdi-account-group',
    },
    {
      name: 'Group management',
      location: '/groups',
      icon: 'mdi-account-group',
    },
    {
      name: "Registration management",
      location: "/registrations",
      icon: 'mdi-account-group'
    }
  ]
}

const PRIVILEGES: Record<string, string[]> = { // Describes the links in the navigation bar
  "/users" : ['admin_read'],
  "/users/id" : ['admin_write'],
  "/groups" : ['groups_read'],
  "/events" : ["logon"],
  "/events/:id": ["event_create"],
  "/projects" : ["logon"],
  "/projects/:id": ["project_write"],
  "/products" : ["admin_read", "product_read"],
  "/profile" : ["logon"],
  "/dashboard" : ["logon"],
  "/productslist" : ["logon"],
  "/profile/edit" : ["logon"],
  "/project-requests": ["project_write"],
  "/home": ["logon"],
  "/my-requests": ["logon"],
  "/": ["logon"],
  "/registrations": ["registration_edit"]
}

export {config, PRIVILEGES};
