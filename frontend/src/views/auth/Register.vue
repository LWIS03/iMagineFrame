<template>
  <!-- If there is a success/error message, use v-alert to display it -->
  <v-alert
    v-model="showSuccess"
    border="start"
    variant="tonal"
    closable
    close-label="Close Alert"
    color="success"
    title="Your registration request has been send succesfully!"
  ></v-alert>
  <v-alert
    v-model="showError"
    border="start"
    variant="tonal"
    closable
    close-label="Close Alert"
    color="error"
    :title="errorMessage"
  >
  </v-alert>

  <div class="pa-5">
    <h2>Register as an iMagineer</h2>

    <v-row no-gutters>
      <v-col cols="12" md="12">
        <v-text-field
          v-model="registrationProposal.email"
          :counter="50"
          :error-messages="fieldErrors.get('email')"
          label="Email*"
          required
        ></v-text-field>
      </v-col>
    </v-row>

    <v-row no-gutters>
      <v-col cols="12" md="12">
        <v-text-field
          v-model="registrationProposal.username"
          :counter="50"
          :error-messages="fieldErrors.get('username')"
          label="Username*"
          required
        ></v-text-field>
      </v-col>
    </v-row>


    <v-row no-gutters>
      <v-col cols="12" md="6">
        <v-text-field
          :counter="30"
          :error-messages="fieldErrors.get('firstName')"
          v-model="registrationProposal.firstName"
          label="First name"
          required
        ></v-text-field>
      </v-col>
      <v-col cols="12" md="6" class="pl-md-2">
        <v-text-field
          :counter="30"
          :error-messages="fieldErrors.get('lastName')"
          v-model="registrationProposal.lastName"
          label="Last name"
          required
        ></v-text-field>
      </v-col>
    </v-row>

    
    <v-row no-gutters>
      <v-col>
        <v-text-field
          v-model="registrationProposal.password"
          :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
          :type="showPassword ? 'text' : 'password'"
          :error-messages="fieldErrors.get('password')"
          label="Password*"
          counter
          @click:append="showPassword = !showPassword"
        ></v-text-field>
        <v-text-field
          v-model="registrationProposal.repeatPassword"
          :append-icon="showRepeatPassword ? 'mdi-eye' : 'mdi-eye-off'"
          :type="showRepeatPassword ? 'text' : 'password'"
          :error-messages="fieldErrors.get('repeatPassword')"
          label="Repeat Password*"
          counter
          @click:append="showRepeatPassword = !showRepeatPassword"
        ></v-text-field>
      </v-col>
    </v-row>

    <v-row>
      <v-col>
        <v-btn color="success" @click="save">
          Send
        </v-btn>
        <v-btn class="ml-2" color="secondary" @click="goBack">
          Back
        </v-btn>
      </v-col>
    </v-row>
  </div>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import { useAuthStore } from "@/stores/AuthStore";
import { useRouter } from "vue-router";
import fetcher from "@/exceptionHandler/exceptionHandler";
import { RegistrationProposal } from "@/models/Registration";

const showPassword = ref<boolean>(false);
const showRepeatPassword = ref<boolean>(false);
const showSuccess = ref<boolean>(false);
const showError = ref<boolean>(false);
const errorMessage = ref<string>("");
const fieldErrors = ref<Map<string, string>>(new Map());
const registrationProposal = ref<RegistrationProposal>(new RegistrationProposal("","","","","",""))

// Auth
const authStore = useAuthStore();
const router = useRouter();

/** On "Save" click: call POST /users/{user.value.id} */
async function save() {
  fieldErrors.value.clear();
  
  if(registrationProposal.value){
    await fetcher.put("/register/new", registrationProposal.value)
    .then(async () => {
      showSuccess.value = true;
      await new Promise(resolve => setTimeout(resolve, 1500));
      router.push("/welcome")
    })
    .catch((error) => {
      console.info(error)
      showError.value = true;
      if(error.response.data) {
        errorMessage.value = error.response.data;
      } else if (typeof error.response.data.message === "object") {
        errorMessage.value = "Something went wrong.";
        for(let key in error.response.data) {
          fieldErrors.value.set(key as string, error.response.data[key])
        }
      }
    })
  }
}
/** pushed in 11-user-see-personal-details-frontend */
/** On "Back" click */
function goBack() {
  router.push({ name: "welcome" });
}
</script>
