import { defineStore } from 'pinia'
import {JWTToken, JWTUser, UserCredentials} from "@/models/User";
import {computed, Ref, ref, UnwrapRef} from "vue";
import fetcher from "@/exceptionHandler/exceptionHandler";
import router from "@/router";

export const useAuthStore = defineStore('auth', () => {
  const userToken: Ref<UnwrapRef<JWTToken | undefined>> = ref<JWTToken | undefined>(undefined)
  const userInfo: Ref<UnwrapRef<JWTUser | undefined>> = ref<JWTUser | undefined>(undefined)

  const isLoggedIn = computed(() => {
    return !!userToken.value
  })

  async function login(user_c: UserCredentials) {
    return await fetcher.post('/auth/login', user_c)
      .then((res) => {
        userToken.value = new JWTToken(res.data["jwt-token"])
        userInfo.value = userToken.value.decode()
        // Store token in cookie
        document.cookie = 'jwt-token=' + res.data["jwt-token"] + '; path=/' + '; samesite=strict' + '; max-age=86400'
        return Promise.resolve(true)
      }).catch((err) => {
        return Promise.reject(err)
      })
  }

  async function logout() {
    userToken.value = undefined
    // Remove token from cookie
    document.cookie = 'jwt-token=; path=/' + '; samesite=strict' + '; max-age=0'
    await router.push('/')
    router.go(0)
  }

  function checkToken() {
    // Check if the token is still valid
    if (userInfo.value && userInfo.value.exp < Math.floor(Date.now() / 1000)) {
      logout()
    }
  }

  function authHeader() {
    checkToken()
    if (userToken.value) {
      return { Authorization: 'Bearer ' + userToken.value.token }
    } else {
      return {}
    }
  }

  async function load_user() {
    if (document.cookie.includes('jwt-token')) {
      const token = document.cookie.split('; ').find(row => row.startsWith('jwt-token='))?.split('=')[1]
      if (token) {
        userToken.value = new JWTToken(token)
        userInfo.value = userToken.value.decode()
        checkToken()
      }
    }
  }
  load_user()

  // ★ (A) Add: A small function to update the Token
  function updateTokenIfPresent(newToken: string | undefined) {
    if (newToken) {
      userToken.value = new JWTToken(newToken)
      userInfo.value = userToken.value.decode()
      document.cookie = 'jwt-token=' + newToken + '; path=/; samesite=strict; max-age=86400'
    }
  }

  // ★ (B) Install a global Axios interceptor
  // It only needs to be executed once in this file; it intercepts all successful responses
  fetcher.interceptors.response.use(
    (response) => {
      const data = response.data;
      if (data && data.jwtToken) {
        // If the backend returns jwtToken, update it immediately
        updateTokenIfPresent(data.jwtToken);
      }
      return response;
    },
    (error) => {
      // Throw it directly in case of an error
      return Promise.reject(error);
    }
  );

  return {
    userToken,
    userInfo,
    isLoggedIn,
    login,
    logout,
    authHeader,
    load_user
  }
})
