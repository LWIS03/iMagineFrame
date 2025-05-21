<template>
  <div class="pa-5 text-center">
    <h2>Page not found</h2>
    <p>Sorry, but the page you are looking for does not exist.</p>
  </div>
  <div class="pa-5 text-center">
    <v-btn v-if="authStore.isLoggedIn" @click=goToHome color="primary">Home page</v-btn>
    <v-btn v-else @click=goToWelcome color="primary">Welcome page</v-btn>
  </div>

</template>

<script lang="ts" setup>

import router from "@/router"
import {useAuthStore} from "@/stores/AuthStore";

const authStore = useAuthStore()

function goToHome() {
  router.push("/")
}

function goToWelcome() {
  router.push("/welcome")
}

</script>
