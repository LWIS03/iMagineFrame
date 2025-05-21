<template>
  <div class="pa-5">
    <!-- Title and "Edit" button on the right -->
    <div class="d-flex justify-space-between">
      <h2>Summary</h2>
      <v-btn
        color="primary"
        class="white--text"
        :to="{ name: 'ProfileEdit' }"
      >
        Edit
      </v-btn>
    </div>

    <!-- Add a line of descriptive text -->
    <p>Here is your profile:</p>

    <!-- You can keep the error prompt, or remove it if not needed -->
    <v-alert
      v-model="showError"
      border="start"
      variant="tonal"
      closable
      close-label="Close Alert"
      color="error"
      :title="errorMessage"
    ></v-alert>

    <!-- If data is not loaded yet, display 'Loading user...'; otherwise display user data -->
    <div v-if="!user">
      Loading user...
    </div>
    <div v-else>
      <!-- The following demonstrates a single table/layout approach;
           you could switch to v-table or keep using v-row+v-col as needed -->
      <v-row class="mt-4">
        <v-col class="font-weight-bold" cols="3">First name:</v-col>
        <v-col cols="9">{{ user.firstName }}</v-col>
      </v-row>

      <v-row class="mt-2">
        <v-col class="font-weight-bold" cols="3">Last name:</v-col>
        <v-col cols="9">{{ user.lastName }}</v-col>
      </v-row>

      <v-row class="mt-2">
        <v-col class="font-weight-bold" cols="3">Username:</v-col>
        <v-col cols="9">{{ user.username }}</v-col>
      </v-row>

      <v-row class="mt-2">
        <v-col class="font-weight-bold" cols="3">Email:</v-col>
        <v-col cols="9">{{ user.email }}</v-col>
      </v-row>

      <v-row class="mt-2">
        <v-col class="font-weight-bold" cols="3">Groups:</v-col>
        <v-col cols="9">
          {{ user.groups?.map(g => g.name).join(", ") }}
        </v-col>
      </v-row>

      <v-row class="mt-2">
        <v-col class="font-weight-bold" cols="3">Privacy setting:</v-col>
        <v-col cols="9">
          {{ getPrivacyLevelLabel(user.privacyLevel) }}
        </v-col>
      </v-row>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { User } from "@/models/User";
import { useAuthStore } from "@/stores/AuthStore";
import fetcher from "@/exceptionHandler/exceptionHandler";

// User data
const user = ref<User | null>(null);

// Error message
const showError = ref(false);
const errorMessage = ref("");

// Get auth info
const authStore = useAuthStore();

async function init() {
  try {
    // Assume your JWT, once decoded, contains a "userInfo" object with an "id" field
    const userId = authStore.userInfo?.id;
    // Use the existing GET /users/{id} endpoint in the backend
    const response = await fetcher.get(`/users/${userId}`, {
      headers: authStore.authHeader()
    });
    user.value = response.data;
  } catch (error: any) {
    showError.value = true;
    errorMessage.value =
      error.response?.data?.message || "Failed to fetch user data";
  }
}

function getPrivacyLevelLabel(privacyLevel: string): string {
  const privacyOptions: {[key: string]: string} = {
    'PUBLIC': 'Public - Everyone can see my participation',
    'IMAGINEERS_ONLY': 'Imagineers Only - Only other Imagineers can see my participation',
    'PRIVATE': 'Private - Only I and administrators can see my participation'
  };

  return privacyOptions[privacyLevel] || privacyLevel;
}

onMounted(() => {
  init();
});
</script>

<style scoped>
/* Add custom styles here if necessary */
</style>
