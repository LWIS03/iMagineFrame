<template>
  <v-app>
    <v-container
      fluid
      class="fill-height d-flex flex-column align-center justify-top bg-star pt-12"
    >
      <v-div class="pt-12 pb-5">
        <v-img 
          src="/src/assets/logos/imaginelab_logo.png"
          min-width="200"
          max-width="800"
          width="700"
        />
      </v-div>
      
      <v-card class="elevation-12 w-50" min-width="300">
        <v-card-title class="font-weight-bold text-h5">Login</v-card-title>
        <v-card-subtitle class="font-weight-light text-h7">Please provide your login credentials.</v-card-subtitle>
        <v-card-text>
          <v-form @submit.prevent="login()">
            <v-text-field
              prepend-icon="mdi-account" name="email" label="Email"
              type="email"
              v-model="identifier"
            ></v-text-field>
            <v-text-field
              id="password"
              :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
              prepend-icon="mdi-lock"
              name="password"
              label="Password"
              :type="showPassword ? 'text' : 'password'"
              v-model="password"
              @click:append="showPassword = !showPassword"
            ></v-text-field>
            <!--
              add v-card-action with button here so it submitting the form works on enter!!
              As it should!
              -->
            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn color="primary" type="submit">Login</v-btn>
              <RouterLink to="/"><v-btn color="primary">Cancel</v-btn></RouterLink> 
            </v-card-actions>
          </v-form>
          <v-alert
            v-model="showError"
            variant="tonal"
            border="start"
            closable
            close-label="Close Alert"
            color="error"
            title="Something went wrong: wrong email or password"
          ></v-alert>
        </v-card-text>
      </v-card>
    </v-container>
  </v-app>
</template>

<script lang="ts" setup>
import {ref} from "vue";
import {useAuthStore} from "@/stores/AuthStore";
import {UserCredentials} from "@/models/User";
import {RouterLink, useRouter} from "vue-router";
import {useThemeStore} from "@/stores/ThemeStore";

const identifier = ref<string>('')
const password = ref<string>('')
const showPassword = ref<boolean>(false)
const showError = ref<boolean>(false)
const router = useRouter()
const authStore = useAuthStore()

useThemeStore() // this is needed to load the theme

function login(){
  authStore.login(new UserCredentials(identifier.value, password.value)).then(() => {
    console.log('login success')
    router.push('/')
  }).catch((error) => {
    console.log('login error')
    console.log(error)
    showError.value = true
  })
}

</script>

<style scoped>
.bg-star {
  background: url('/src/assets/bakgrounds/bg_black_stars.jpg');
  background-size: repeat;
}
</style>
