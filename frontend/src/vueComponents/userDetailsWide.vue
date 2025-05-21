<template>
  <v-sheet
    elevation="5"
    class="text-high-emphasis ml-6"
    height="270"
  >
    <v-container fluid>
      <v-row class="d-flex justify-center align-center">
        <v-col cols="12" md="3" class="d-flex justify-center align-center">
          <v-avatar
            size="100"
            color="black">
          </v-avatar>
        </v-col>

        <v-col cols="12" md="9">
          <h2 class="pb-2">Personal details:</h2>

          <ul class="ml-4 mb-6">
            <li>
              <b>Email:</b> {{props.displayedUser?.email}}
            </li>
            <li>
              <b>Username:</b> {{useAuthStore().userInfo?.username}}
            </li>
            <li>
              <b>Name:</b> {{props.displayedUser?.firstName}} {{ props.displayedUser?.lastName }}
            </li>
            <li>
              <b>Groups:</b>
              <span
                class="mx-2">
                <span
                  v-for="group in props.displayedUser?.groups"
                  :key="group.name"
                  class="mx-1">
                  <v-chip
                    color="primary"
                    variant="tonal"
                    density="comfortable">
                    {{group.name}}
                  </v-chip>
                </span>
              </span>
            </li>
          </ul>

          <v-btn
            class="text-none"
            color="info"
            variant="tonal"
            block
            @click="goToEditPage()"
          ><span>Change profile</span></v-btn>
        </v-col>
      </v-row>
    </v-container>
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
  router.push("/profile/edit/")
}
</script>
