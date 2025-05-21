<template>
  <!-- If there is a success/error message, use v-alert to display it -->
  <v-alert
    v-model="showSuccess"
    border="start"
    variant="tonal"
    closable
    close-label="Close Alert"
    color="success"
    :title="message"
  ></v-alert>
  <v-alert
    v-model="showError"
    border="start"
    variant="tonal"
    closable
    close-label="Close Alert"
    color="error"
    :title="message"
  >
  </v-alert>

    
    
  <div class="pa-5 text-center">
    <h2>Registrations</h2>
  </div>

  <div class="d-flex align-center ml-5 mr-9">
    <v-checkbox-btn v-model="filterUnproccessed" label="filter unprocessed"/>
    <v-date-input
      v-model="filterDate"
      label="Select range"
      max-width="350"
      multiple="range"
    />
         
  </div>

  <v-card class="px-5 pb-1 d-flex-column">
    <v-virtual-scroll
      class="viewport-65"
      :items = "filteredRegistrations">
      <template v-slot:default="{item}">
        <RegistrationView :registration="item" @on-error-occured="handleShowError" @on-success="handleSucces"/>
      </template>
    </v-virtual-scroll>
    <v-card-actions class="text-end">
      <v-btn class="flex-grow-1" variant="flat" color="success" @click="removeAccepted()">Remove all accepted</v-btn>
      <v-btn class="flex-grow-1" variant="flat" color="error" @click="removeDeclined()">Remove all declined</v-btn>
    </v-card-actions>
        
  </v-card>
</template>

<script lang="ts" setup>
import { getAllRegistrations, Registration } from "@/models/Registration";
import { ref, Ref, computed, onMounted, handleError } from "vue"
import RegistrationView from "@/views/registrations/Registration.vue"
import { VDateInput } from 'vuetify/labs/VDateInput'
import fetcher from "@/exceptionHandler/exceptionHandler";
import { useAuthStore } from "@/stores/AuthStore";


const authStore = useAuthStore();
const registrations: Ref<Registration[]> = ref([]);

const filterUnproccessed = ref<boolean>(false);
const filterDate = ref<Date[] | null>(null);

const showSuccess = ref<boolean>(false);
const showError = ref<boolean>(false);
const message = ref<string>("");

const filteredRegistrations = computed(() => {
    let returnedRegistrations = registrations.value

    if (filterUnproccessed.value) {
        returnedRegistrations = returnedRegistrations.filter(item => item.accepted == undefined)
    }

    if (filterDate.value!){
        const startDate = filterDate.value[0]
        const endDate = filterDate.value[filterDate.value.length-1]
        if (startDate && endDate) {

            returnedRegistrations = returnedRegistrations.filter(item => {
                const date = new Date(item.dateCreated) 
                return date >= startDate && date <= endDate
            })
        }
    }



    return returnedRegistrations
})

async function handleShowError(childMessage: Ref<string>) {
    showError.value = true;
    message.value = childMessage.value;
    await new Promise(resolve => setTimeout(resolve, 2000))
    location.reload()
}

async function handleSucces() {
    location.reload()
}

async function loadRequests() {
    registrations.value = await getAllRegistrations()
}

onMounted(() => {
    loadRequests()
})

async function removeDeclined() {
    await fetcher.delete("/register/declined", {headers: authStore.authHeader()})
    .then(async () => {
        handleSucces()

    })
    .catch((error) => {
        const msg: Ref<string> = ref<string>("")
        if(error.response.data) {
            msg.value = error.response.data;
        } else if (typeof error.response.data.message === "object") {
            msg.value = "Something went wrong.";
        }
        handleShowError(msg)
    })
}

async function removeAccepted() {
    await fetcher.delete("/register/accepted", {headers: authStore.authHeader()})
    .then(async () => {
        handleSucces()
    })
    .catch((error) => {
        const msg: Ref<string> = ref<string>("")
        if(error.response.data) {
            msg.value = error.response.data;
        } else if (typeof error.response.data.message === "object") {
            msg.value = "Something went wrong.";
        }
        handleShowError(msg)
    })
}

</script>

<style scoped>

.viewport-65 {
    height: 65vh;
}


</style>