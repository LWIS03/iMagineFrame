<template>
  <div class="pa-5 text-center">
    <h2>Forbidden</h2>
    <p>Sorry, if this page exists, it is forbidden.</p>
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
