<template>
  <v-app id="inspire">
    <v-app-bar
      class="header-with-gradient-lines"
    >
      <v-app-bar-nav-icon
        class="hidden-sm-and-up"
        @click.stop="drawer = !drawer"
      ></v-app-bar-nav-icon>


      <v-container class="fill-height d-flex align-center">
        <router-link to="/welcome" class="mr-4 d-flex align-center justify-center">
          <img
            src="@/assets/logos/favicon.ico"
            height="33"
          />
        </router-link>
        <span
          v-for="link in navigation_links"
          :key="link.name"
        >
          <v-btn
            class="hidden-xs"
            v-if="hasRequiredPrivileges(PRIVILEGES[link.location])"
            :key="link.name"
            :prepend-icon="link.icon"
            :to="link.location"
          >
            {{ link.name }}
          </v-btn>
        </span>

      </v-container>
      <v-container class="text-right w-50" >
        <v-btn @click="themeStore.toggleTheme()" icon="mdi-theme-light-dark"></v-btn>
        <span v-if="!authStore.isLoggedIn">
          <router-link :to="{path : '/login'}"><v-btn>Login</v-btn></router-link>
          <router-link :to="{path : '/register'}"><v-btn color="secondary" variant="tonal">Register</v-btn></router-link>
        </span>
        <span v-else >
          <v-avatar
            size="32"
            color="black">
          </v-avatar>
          {{ authStore.userInfo?.username }}
          <v-btn @click="authStore.logout()" icon="mdi-logout"></v-btn>
        </span>
      </v-container>
    </v-app-bar>

    <v-navigation-drawer
      class="hidden-sm-and-up"
      v-model="drawer"
      temporary
    >
      <v-list
        :lines="false"
        density="compact"
        nav
      >
        <span
          v-for="link in navigation_links"
          :key="link.name"
        >
          <v-list-item
            v-if="hasRequiredPrivileges(PRIVILEGES[link.location])"
            :key="link.name"
            :title="link.name"
            :to="link.location"
            :prepend-icon="link.icon"
            color="primary"
          >
          </v-list-item>
        </span>
      </v-list>
    </v-navigation-drawer>

    <v-main :class="theme.global.current.value.dark ? 'black': 'bg-star'">
      <v-container>
        <v-sheet
          min-height="90vh"
          rounded="lg"
        >
          <RouterView/>
        </v-sheet>
      </v-container>
    </v-main>
  </v-app>
</template>

<script lang="ts" setup>
import {useTheme} from 'vuetify'
import {ref} from "vue";
import {useAuthStore} from "@/stores/AuthStore";
import {useThemeStore} from "@/stores/ThemeStore";
import {checkPrivileges} from "@/models/Group";
import {config, PRIVILEGES} from "@/config";

const theme = useTheme()
const themeStore = useThemeStore()
const authStore = useAuthStore()
const drawer = ref(false)
const navigation_links = config["NAVIGATION"]
function hasRequiredPrivileges(requiredPrivileges: string[] | undefined): boolean {
  return checkPrivileges(requiredPrivileges, authStore.userInfo?.privileges || []);
}
</script>

<style scoped>


</style>
<!-- .header-with-gradient-lines::before, can be added to get the gradient-->
<style scoped>
.header-with-gradient-lines {
  position: relative;
  background-color: #121212; /* or any solid color you want */
  color: white;
  z-index: 10;
}

.bg-star {
  background: url('/src/assets/bakgrounds/bg_black_stars.jpg');
  background-size: repeat;
}

.header-with-gradient-lines::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(
    90deg,
    #b00197 0%,
    #34c9e7 25%,
    #33fe99 50%,
    #34c9e7 75%,
    #b00197 100%
  );
}

.header-with-gradient-lines::before {
  top: 0;
}

.header-with-gradient-lines::after {
  bottom: 0;
}
</style>
