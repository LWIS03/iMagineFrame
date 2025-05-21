<template>
  <v-sheet
    elevation="5"
    class="h-100  ">
    <v-card class="h-100 d-flex flex-column">
      <v-row class="mt-5 d-flex justify-center">
        <v-avatar
          size="80"
          color="black">
        </v-avatar>
      </v-row>
      <v-row>
        <ul class="ml-12 my-2 text-justify-center">
          <li>
            <b>Email:</b>
          </li>
          <ul>
            {{props.displayedUser?.email}}
          </ul>
          <li>
            <b>Username:</b>
          </li>
          <ul>
            {{useAuthStore().userInfo?.username}}
          </ul>
          <li>
            <b>Name:</b> 
          </li>
          <ul>
            {{props.displayedUser?.firstName}} {{ props.displayedUser?.lastName }}
          </ul>
        </ul>
      </v-row>
      <v-card-actions>
        <v-btn
          class="text-none"
          color="info"
          variant="tonal"
          block
          @click="goToEditPage()"
        ><span>Edit profile</span></v-btn>
      </v-card-actions>
    </v-card>
  </v-sheet>
</template>

<script lang="ts" setup>
import { User } from '@/models/User';
import { useAuthStore } from '@/stores/AuthStore';
import router from '@/router';

// Define props
const props = defineProps<{ displayedUser: User }>();

function goToEditPage(){
  const authStore = useAuthStore()
  router.push("/users/"+parseInt(authStore.userInfo?.id as string))
}
</script>
