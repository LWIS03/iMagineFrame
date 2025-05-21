<template>
  <v-alert
    v-model="showSuccess"
    border="start"
    variant="tonal"
    closable
    close-label="Close Alert"
    color="success"
    title="Saved successfully"
  ></v-alert>
  <v-alert
    v-model="showError"
    border="start"
    variant="tonal"
    closable
    close-label="Close Alert"
    color="error"
    :title="errorMessage"
  ></v-alert>

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


 <!-- Force Change password dialog -->
 <v-dialog v-model="showForceChangePasswordDialog" width="800">
    <v-card>
      <v-card-title class="d-flex justify-center">
        Change the password of {{ user.firstName }} {{ user.lastName }}
      </v-card-title>
      <v-card-text>
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
            <v-btn color="success" variant="tonal" @click="sendForceChangePasswordRequest">
              Save
            </v-btn>
            <v-btn class="ml-2" color="error" variant="tonal" @click="showForceChangePasswordDialog = false">
              Cancel
            </v-btn>
          </v-col>
        </v-row>
      </v-card-actions>
    </v-card>
  </v-dialog>


  <div class="pa-5">
    <h2 v-if="action === 'edit'">Edit User: {{ user.firstName }} {{ user.lastName }}</h2>
    <h2 v-if="action === 'new'">Create User</h2>
    <v-row no-gutters>
      <v-col
        cols="12"
        md="6"
      >
        <v-text-field
          v-model="user.firstName"
          :counter="30"
          :error-messages="fieldErrors.get('firstName')"
          label="First name"
          required
        ></v-text-field>
      </v-col>
      <v-col
        cols="12"
        md="6"
        class="pl-md-2"
      >
        <v-text-field
          v-model="user.lastName"
          :counter="30"
          :error-messages="fieldErrors.get('lastName')"
          label="Last name"
          required
        ></v-text-field>
      </v-col>
    </v-row>

    <v-row no-gutters>
      <v-col>
        <v-text-field
          v-model="user.username"
          :counter="50"
          :error-messages="fieldErrors.get('username')"
          label="Username*"
          required
        ></v-text-field>
      </v-col>
    </v-row>

    <v-row no-gutters>
      <v-col>
        <v-text-field
          v-model="user.email"
          :counter="50"
          :error-messages="fieldErrors.get('email')"
          label="Email*"
          required
        ></v-text-field>
      </v-col>
    </v-row>

    <v-row v-if="action == 'new'" no-gutters>
      <v-col
        cols="12"
        md="6"
      >
        <v-text-field
          v-model="user.password"
          :counter="30"
          :error-messages="fieldErrors.get('password')"
          :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
          :type="showPassword ? 'text' : 'password'"
          label="Password"
          required
        ></v-text-field>
      </v-col>
      <v-col
        cols="12"
        md="6"
        class="pl-md-2"
      >
        <v-text-field
          v-model="user.repeatPassword"
          :counter="30"
          :error-messages="fieldErrors.get('repeatPassword')"
          :append-icon="showRepeatPassword ? 'mdi-eye' : 'mdi-eye-off'"
          :type="showRepeatPassword ? 'text' : 'password'"
          label="Repeated Password"
          required
        ></v-text-field>
      </v-col>
    </v-row>

    <v-row no-gutters>
      <v-col
        cols="12"
        md="6"
        class="md-2"
      >
        <v-select
          v-model="user.groups"
          :items="groups"
          multiple
          item-title="name"
          item-value="id"
          label="Group"
          :error-messages="fieldErrors.get('groups')"
          v-bind="{'return-object':true}">
          <!--          return-object-->
          <!--          required-->
          <!--          chips-->
          <!--          multiple-->
        </v-select>
      </v-col>
    </v-row>

    <v-row no-gutters v-if="action != 'new'">
      <v-col>
        <v-btn v-if="checkPrivileges(['password_edit'], authStore.userInfo?.privileges)" @click="showForceChangePasswordDialog = true">Force change password</v-btn>
        <v-btn v-else @click="showChangePasswordDialog = true">Change password</v-btn>
      </v-col>
    </v-row>

    <v-row>
      <v-col>
        <v-btn
          color="success"
          @click="save"
        >
          Save
        </v-btn>
        <v-btn
          class="ml-2"
          color="secondary"
          @click="router.go(-1)"
        >
          Back
        </v-btn>
      </v-col>
    </v-row>
  </div>
</template>

<script lang="ts" setup>
import fetcher from "@/exceptionHandler/exceptionHandler";
import {useRoute, useRouter} from "vue-router";
import {ref} from "vue";
import {User, getUser} from "@/models/User";
import {useAuthStore} from "@/stores/AuthStore";
import { checkPrivileges } from "@/models/Group";

const route = useRoute();
const router = useRouter();

const user = ref<User>(new User(0, "", "", "", "", null, null, []));
const groups = ref<object[]>([]);
const showSuccess = ref<boolean>(false);
const showError = ref<boolean>(false);
const errorMessage = ref<string>("");
const fieldErrors = ref<Map<string, string>>(new Map());
const action = ref("edit")
const authStore = useAuthStore();

const showChangePasswordDialog = ref<boolean>(false);
const showForceChangePasswordDialog = ref<boolean>(false);

const oldPassword = ref<string>("");
const newPassword = ref<string>("");
const newPasswordRepeated = ref<string>("");

const showOldPassword = ref<boolean>(false);
const showPassword = ref<boolean>(false);
const showRepeatPassword = ref<boolean>(false);


async function init() {
  if (route.params.id === "new") {
    action.value = "new";
    return;
  }
  user.value = await getUser(route.params.id as string) as User
  console.log(authStore.userInfo?.privileges)
}
init(); // call init() on component load

async function initGroupOptions() {
  await fetcher.get("/groups", {headers: authStore.authHeader()})
    .then((response) => {
      groups.value = response.data;
    }).catch((error) => {
      showError.value = true;
      errorMessage.value = error.response.data;
    })
}
initGroupOptions(); // call initGroupOptions() on component load

async function save() {
  if (action.value === "new") {
    console.log("new user")
    console.log(user.value)
    await fetcher.put("/users/new", user.value, {headers: authStore.authHeader()})
      .then(async () => {
        showSuccess.value = true;
        await new Promise(resolve => setTimeout(resolve, 1500));
        router.push("/users")
      })
      .catch((error) => {
        showError.value = true;
        if(error.response.data) {
          errorMessage.value = error.response.data;
        } else if (typeof error.response.data === "object") {
          errorMessage.value = "Something went wrong.";
          for(let key in error.response.data) {
            fieldErrors.value.set(key as string, error.response.data[key])
          }
        }
      })
  } else {
    await fetcher.post("/users/" + route.params.id, user.value, {headers: authStore.authHeader()})
      .then(async () => {
        showSuccess.value = true;
        await new Promise(resolve => setTimeout(resolve, 1500));
        router.push("/users")
      })
      .catch((error) => {
        showError.value = true;
        if(error.response.data) {
          errorMessage.value = error.response.data;
        } else if (typeof error.response.data === "object") {
          errorMessage.value = "Something went wrong.";
          for(let key in error.response.data) {
            fieldErrors.value.set(key as string, error.response.data[key])
          }
        }
      }
    )      
  }
}

async function sendForceChangePasswordRequest() {
  const changePasswordRequest: Record<string, string> = {
    "newPassword": newPassword.value,
    "newPasswordRepeated": newPasswordRepeated.value
  }

  fetcher.post("/users/"+route.params.id+"/forceChangePassword", changePasswordRequest, {headers: authStore.authHeader()})
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
      showForceChangePasswordDialog.value = false;
      newPassword.value = "";
      newPasswordRepeated.value="";
    }
    
  )
}

async function sendChangePasswordRequest() {
  const changePasswordRequest: Record<string, string> = {
    "oldPassword": oldPassword.value,
    "newPassword": newPassword.value,
    "newPasswordRepeated": newPasswordRepeated.value
  }

  fetcher.post("/users/"+route.params.id+"/changePassword", changePasswordRequest, {headers: authStore.authHeader()})
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
</script>

<style scoped>

</style>
