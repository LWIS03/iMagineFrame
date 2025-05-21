<template>
  <div class="pa-5 text-center">
    <h2>Unauthorized</h2>
    <p>Sorry, if this page exists, you are not authorised to access it.</p>
  </div>
  <div class="pa-5 text-center">
    <v-btn v-if="authStore.isLoggedIn" @click=goToHome color="primary" class="mr-2">Home page</v-btn>
    <v-btn v-if="!authStore.isLoggedIn" @click=goToWelcome color="primary" class="mr-2">Welcome page</v-btn>
    <v-btn v-if="!authStore.isLoggedIn" @click=goToLogin color="primary">Login page</v-btn>
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

function goToLogin() {
  router.push("/login")
}
</script>
