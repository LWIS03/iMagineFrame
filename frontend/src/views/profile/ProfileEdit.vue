<template>
  <!-- Success / Error messages -->
  <v-alert
    v-model="showSuccess"
    border="start"
    variant="tonal"
    closable
    close-label="Close Alert"
    color="success"
    title="Saved successfully"
  />
  <v-alert
    v-model="showError"
    border="start"
    variant="tonal"
    closable
    close-label="Close Alert"
    color="error"
    :title="errorMessage"
  />


  <!-- Change password dialog -->
  <v-dialog v-model="showChangePasswordDialog" width="800">
    <v-card>
      <v-card-title class="d-flex justify-center">
        Change your password
      </v-card-title>
      <v-card-text>
        <v-row no-gutters>
          <v-col>
            <v-text-field
              required
              label="Old Password"
              :append-icon="showOldPassword ? 'mdi-eye' : 'mdi-eye-off'"
              :type="showOldPassword ? 'text' : 'password'"
              v-model="oldPassword"
              @click:append="showOldPassword = !showOldPassword"
            />
          </v-col>
        </v-row>
        <v-row no-gutters>
          <v-col>
            <v-text-field
              required
              label="New Password"
              :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
              :type="showPassword ? 'text' : 'password'"
              v-model="newPassword"
              @click:append="showPassword = !showPassword"
            />
          </v-col>
        </v-row>
        <v-row no-gutters>
          <v-col>
            <v-text-field
              required
              label="Repeat New Password"
              :append-icon="showRepeatPassword ? 'mdi-eye' : 'mdi-eye-off'"
              :type="showRepeatPassword ? 'text' : 'password'"
              v-model="newPasswordRepeated"
              @click:append="showRepeatPassword = !showRepeatPassword"
            />
          </v-col>
        </v-row>
      </v-card-text>
      <v-card-actions class="mx-4 mb-2 mt-n8">
        <v-row>
          <v-col>
            <v-btn color="success" variant="tonal" @click="sendChangePasswordRequest">
              Save
            </v-btn>
            <v-btn class="ml-2" color="error" variant="tonal" @click="showChangePasswordDialog = false">
              Cancel
            </v-btn>
          </v-col>
        </v-row>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <div class="pa-5">
    <h2>Edit User: {{ user?.firstName }} {{ user?.lastName }}</h2>

    <!-- Render the form only after the user is loaded -->
    <template v-if="user">
      <v-row no-gutters>
        <v-col cols="12" md="6">
          <v-text-field
            v-model="user.firstName"
            :counter="30"
            :error-messages="fieldErrors.get('firstName')"
            label="First name"
            required
          />
        </v-col>
        <v-col cols="12" md="6" class="pl-md-2">
          <v-text-field
            v-model="user.lastName"
            :counter="30"
            :error-messages="fieldErrors.get('lastName')"
            label="Last name"
            required
          />
        </v-col>
      </v-row>

      <v-row no-gutters>
        <v-col cols="12" md="6">
          <v-text-field
            v-model="user.email"
            :counter="50"
            :error-messages="fieldErrors.get('email')"
            label="Email"
            required
          />
        </v-col>
        <v-col cols="12" md="6" class="pl-md-2">
          <v-text-field
            v-model="user.username"
            :value="user.username"
            label="Username"
          />
        </v-col>
      </v-row>

      
    <v-row no-gutters>
      <v-col cols="2">
        <v-btn block @click="showChangePasswordDialog = true">Change password</v-btn>
      </v-col>
    </v-row>

    <v-row no-gutters class="mt-4">
      <v-col cols="1" class="pr-4">
        <v-btn block color="success" @click="save">Save</v-btn>
      </v-col>  
      <v-col cols="1">
        <v-btn block color="secondary" @click="goBack">Back</v-btn>
      </v-col>
    </v-row>
    </template>

    <!-- Simple loading hint (replace with a spinner if you like) -->
    <div v-else class="text-center py-8">
      Loading user...
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Group, checkPrivileges} from '@/models/Group'
import { User } from '@/models/User'
import { useAuthStore } from '@/stores/AuthStore'
import fetcher from '@/exceptionHandler/exceptionHandler'

// The user currently being edited
const user = ref<User>()

// List of groups (only needed for administrators)
const groups = ref<Group[]>([])

const showSuccess        = ref(false)
const showError          = ref(false)
const errorMessage       = ref('')
const fieldErrors        = ref<Map<string, string>>(new Map())
const showChangePasswordDialog = ref<boolean>(false);
const oldPassword = ref<string>("");
const newPassword = ref<string>("");
const newPasswordRepeated = ref<string>("");

const showRepeatPassword = ref<boolean>(false);
const showPassword = ref<boolean>(false);
const showOldPassword = ref<boolean>(false);

const authStore = useAuthStore()

const privacyOptions = ref([
  { title: 'Public - Everyone can see my participation', value: 'PUBLIC' },
  { title: 'Imagineers Only - Only other Imagineers can see my participation', value: 'IMAGINEERS_ONLY' },
  { title: 'Private - Only I and administrators can see my participation', value: 'PRIVATE' }
]);

/** Load the current user's data */
async function init () {
  try {
    const userId   = authStore.userInfo?.id
    const response = await fetcher.get(`/users/${userId}`, {
      headers: authStore.authHeader()
    })
    user.value = response.data
  } catch (error: any) {
    showError.value   = true
    errorMessage.value =
      error.response?.data?.message || 'Failed to fetch user data'
  }
}
init()

/** Load group options if necessary */
async function initGroupOptions () {
  try {
    const response = await fetcher.get('/groups', {
      headers: authStore.authHeader()
    })
    groups.value = response.data
  } catch (error: any) {
    showError.value   = true
    errorMessage.value =
      error.response?.data?.message || 'Failed to fetch groups'
  }
}
if (checkPrivileges(['event_read'], authStore.userInfo?.privileges)) {
  initGroupOptions()
}

/** Save */
async function save () {
  fieldErrors.value.clear()
  try {
    if (user.value) {

      await fetcher.post(`/users/${user.value.id}`, user.value, {
        headers: authStore.authHeader()
      })
    }
    showSuccess.value = true
    setTimeout(() => history.go(-1), 1200)
  } catch (error: any) {
    showError.value = true
    if (error.response.data) {
      errorMessage.value = error.response.data
    } else if (typeof error.response?.data === 'object') {
      errorMessage.value = 'Something went wrong.'
      for (const key in error.response.data) {
        fieldErrors.value.set(key, error.response.data[key])
      }
    }
  }
}

async function sendChangePasswordRequest() {
  const changePasswordRequest: Record<string, string> = {
    "oldPassword": oldPassword.value,
    "newPassword": newPassword.value,
    "newPasswordRepeated": newPasswordRepeated.value
  }

  if (user.value){
    fetcher.post("/users/"+user.value.id+"/changePassword", changePasswordRequest, {headers: authStore.authHeader()})
    .then(
      () => showSuccess.value = true
    )
    .catch((error) => {
        showError.value = true;
        if(error.response.data) {
          errorMessage.value = error.response.data;
        } else if (typeof error.response.data === "object") {
          errorMessage.value = "Something went wrong.";
        }
      }
    ).finally(
      () => {
        showChangePasswordDialog.value = false;
        oldPassword.value = "";
        newPassword.value = "";
        newPasswordRepeated.value="";
      }
    )
  }
}

/** Go back */
function goBack () {
  history.go(-1)
}
</script>
