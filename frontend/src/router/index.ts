// Composables
import {createRouter, createWebHistory, RouteLocationNormalized } from 'vue-router'
import {useAuthStore} from "@/stores/AuthStore";

import {PRIVILEGES} from "@/config";
import {checkPrivileges} from "@/models/Group";

const routes = [
  {
    path: '/',
    redirect: () => {
      return determineHomeOrWelcomeRoute()
    }
  },
  {
    path: '/home',
    component: () => import('@/layouts/Default.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        // route level code-splitting
        // this generates a separate chunk (about.[hash].js) for this route
        // which is lazy-loaded when the route is visited.
        component: () => import(/* webpackChunkName: "home" */ '@/views/homePage/Home.vue'),
      },
    ],
  },
  {
    path: '/welcome',
    component: () => import('@/layouts/Default.vue'),
    children: [
      {
        path: '',
        name: 'welcome',
        // route level code-splitting
        // this generates a separate chunk (about.[hash].js) for this route
        // which is lazy-loaded when the route is visited.
        component: () => import(/* webpackChunkName: "home" */ '@/views/welcome/Welcome.vue'),
      },
    ],
  },
  {
    path: '/register',
    component: () => import('@/layouts/Default.vue'),
    children: [
      {
        path: '',
        name: 'Register',
        component: () => import(/* webpackChunkName: "users" */ '@/views/auth/Register.vue'),
      }
    ]
  },
  {
    path: '/registrations',
    component: () => import('@/layouts/Default.vue'),
    children: [
      {
        path: '',
        name: 'registrationDashboard',
        component: () => import('@/views/registrations/RegistrationDashboard.vue')
      }
    ]
  },
  {
    path: '/users',
    component: () => import('@/layouts/Default.vue'),
    children: [
      {
        path: '',
        name: 'Users',
        component: () => import(/* webpackChunkName: "users" */ '@/views/users/Users.vue'),
      },
      {
        path: ':id',
        beforeEnter: (to: RouteLocationNormalized ) => {
          return checkUserAccess(to, PRIVILEGES["/users/id"])
        },
        name: "User",
        component: () => import(/* webpackChunkName: "user" */ '@/views/users/UserEdit.vue'),
      }
    ]
  },
  {
    path: '/groups',
    component: () => import('@/layouts/Default.vue'),
    children: [
      {
        path: '',
        name: 'Groups',
        component: () => import(/* webpackChunkName: "users" */ '@/views/groups/Groups.vue'),
      },
      {
        path: ':id',
        name: 'Group',
        component: () => import(/* webpackChunkName: "user" */ '@/views/groups/GroupEdit.vue'),
      }
    ]
  },
  {
    path: '/profile',
    component: () => import('@/layouts/Default.vue'),
    children: [
      {
        path: '',
        name: 'profile',
        component: () => import(/* webpackChunkName: "user" */'@/views/profile/Profile.vue'),
      },
      {
        path: 'edit',
        name: 'ProfileEdit',
        component: () => import(/* webpackChunkName: "user" */'@/views/profile/ProfileEdit.vue')
      }
    ]
  },
  {
    path: '/events',
    component: () => import('@/layouts/Default.vue'),
    children: [
      {
        path: '',
        name: 'Events',
        component: () => import(/* webpackChunkName: "events" */ '@/views/events/Event.vue'),
      },
      {
        path: ':id',
        name: 'EventEdit',
        component: () => import(/* webpackChunkName: "event" */ '@/views/events/EventEdit.vue'),
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import(/* webpackChunkName: "login" */ '@/views/auth/Login.vue'),
  },
  {
    path: '/products',
    component: () => import('@/layouts/Default.vue'),
    children: [
      {
        path: '',
        name: 'Products',
        component: () => import(/* webpackChunkName: "products" */ '@/views/products/products.vue'),
      },
      {
        path: 'edit',
        name: 'ProductEdit',
        component: () => import(/* webpackChunkName: "user" */'@/views/products/ProductEdit.vue')
      }
    ]
  },
  {
    path: '/projects',
    component: () => import('@/layouts/Default.vue'),
    children: [
      {
        path: '',
        name: 'Projects',
        component: () => import(/* webpackChunkName: "projects" */ '@/views/projects/Project.vue'),
      },
      {
        path: ':id',
        name: 'ProjectEdit',
        component: () => import(/* webpackChunkName: "project" */ '@/views/projects/ProjectEdit.vue'),
      }
    ]
  },
  {
    path: '/dashboard',
    component: () => import('@/layouts/Default.vue'),
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import("@/views/dashboard/Dashboard.vue")
      }
    ]
  },
  {
    path: '/my-requests',
    component: () => import('@/layouts/Default.vue'),
    children: [
      {
        path: '',
        name: 'MyRequests',
        component: () => import('@/views/projects/MyRequests.vue'),
      }
    ]
  },
  {
    path: '/project-requests',
    component: () => import('@/layouts/Default.vue'),
    beforeEnter: (to: RouteLocationNormalized) => {
      return checkUserAccess(to, PRIVILEGES["/project-requests"])
    },
    children: [
      {
        path: '',
        name: 'ProjectRequests',
        component: () => import('@/views/projects/ProjectRequests.vue'),
      }
    ]
  },
  // Loads in the NotFound.vue page.
  {
    path: '/error',
    component: () => import('@/layouts/Default.vue'),
    redirect: "/error/404",
    children: [
      {
        path: '404',
        name: 'NotFound',
        component: () => import(/* webpackChunkName: "events" */ '@/views/errorPages/NotFound.vue'),
      },
      {
        path: '403',
        name: 'Forbidden',
        component: () => import('@/views/errorPages/Forbidden.vue'),
      },
      {
        path: '401',
        name: 'Unauthorized',
        component: () => import('@/views/errorPages/Unauthorized.vue'),
      }
    ]
  },
  // Every path (that does not exist) will be redirected to the 404 page.
  {
    path: '/:catchAll(.*)',
    redirect: '/error/404'
  },
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
})

router.beforeEach((to, from, next) => {
  console.log("Trying to go from ", from.fullPath, " to ", to.fullPath)

  // const publicPages = ['/login', '/', "/error/", "/welcome", "/register"]
  // const authRequired = !publicPages.includes(to.path)
  const publicPages = ['/login', '/', '/welcome', '/register'];
  const publicPathPrefixes = ['/error/']; // for wildcard-like matching

  const authRequired = !(
    publicPages.includes(to.path) ||
    publicPathPrefixes.some(prefix => to.path.startsWith(prefix))
  );
  const authStore = useAuthStore()

  // trying to access a restricted page + not logged in
  // redirect to login page
  if (authRequired && !authStore.isLoggedIn) {
    return next('/error/401')
  }

  // Check if user has the correct privilege, otherwise redirect to unauthorized/401
  if (to.path in PRIVILEGES && !checkPrivileges(PRIVILEGES[to.path], authStore.userInfo?.privileges || [])) {
    return next('/error/401');
  }

  next();
});


function checkUserAccess(to: RouteLocationNormalized, extra_privileges: string[] | undefined): boolean {
  const authStore = useAuthStore()
  let hasPermission: boolean = false;
  if (extra_privileges == undefined) {
    hasPermission = to.params["id"] == authStore.userInfo?.id
  } else {
    hasPermission = to.params["id"] == authStore.userInfo?.id || checkPrivileges(extra_privileges, authStore.userInfo?.privileges)
  }

  if (!hasPermission) {
    router.push("/error/401")
  }
  return hasPermission
}

function determineHomeOrWelcomeRoute(){
  const authStore = useAuthStore()

  if (!authStore.isLoggedIn) {
    return { path: "/welcome"}
  } else {
    return { path: "/home"}
  }
}

export default router
