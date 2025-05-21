<template>
    
  <!-- Dialog for selecting the group in which the new user will belong -->
  <v-dialog
    v-model="showDialog"
    max-width="600"
  >
    <v-card>
      <v-card-title>
        Assign groups to {{ props.registration.username }}
      </v-card-title>
      <v-card-text>
        <p class="font-weight-medium">Select groups</p> 
        <v-select
          clearable
          chips
          label="Select"
          :items="groups"
          v-model="selectedGroups"
          item-title="name"
          multiple
          return-object
        ></v-select>

      </v-card-text>
      <v-card-actions>
        <v-btn @click="acceptRequest()" color="success">
          Accept
        </v-btn>
        <v-btn @click="cancelRequestAcceptation()">
          Back
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
  <v-card variant="text">
    <v-card-title class="d-flex justify-space-between bg-primary align-center" >
      {{ props.registration.username }}
      <div>
        <v-btn
          variant="plain"
          :icon="showExtraInfo ? 'mdi-chevron-up' : 'mdi-chevron-down'"
          @click="showExtraInfo = !showExtraInfo"
        ></v-btn>
        <v-chip variant="elevated" :color="chipColor" size="x-small" class="text-overline">
          {{ chipText }}
        </v-chip>   
      </div>
    </v-card-title>
    <v-card-text class="d-flex justify-space-between align-center mt-2 mb-n2 mr-3">
      <v-row no-gutters>
        <v-col cols="6" class="text-body-1 font-weight-bold">
          Submitted at: {{ formatDate(props.registration.dateCreated) }}
        </v-col>
        <v-col v-if="showProcessed" cols="6" class="text-body-1 font-weight-bold text-right">
          Processed at: {{ formatDate(props.registration.dateUpdated) }}
        </v-col>
      </v-row>
    </v-card-text>
    <v-expand-transition>
      <div v-show="showExtraInfo" class="text-body-1">
        <v-divider></v-divider>
        <v-card-text class="mb-n3">
          <v-row no-gutters>
            <v-col>
              Username: {{ props.registration.username }}
            </v-col>
            <v-col>
              First name: {{ props.registration.firstName }}
            </v-col>
          </v-row>
          <v-row no-gutters>
            <v-col>
              Email: {{ props.registration.email }}
            </v-col>
            <v-col>
              Last name: {{ props.registration.lastName }}
            </v-col>
          </v-row>
        </v-card-text>
        <v-card-actions class="d-flex justify-space-between">
          <div>
            <v-btn class="mx-2"
              variant="tonal"
              color="success"
              @click="openAcceptDialog()">
              Accept
            </v-btn>
            <v-btn
              variant="tonal"
              color="error"
              @click="declineRequest()">
              Decline
            </v-btn>
          </div>
          <div>
            <v-btn
              variant="tonal"
              color="error"
              icon="mdi-delete"
              class="mx-2"
              @click="deleteRequest()"
            />
          </div>
        </v-card-actions>
      </div>
    </v-expand-transition>
  </v-card>
</template>

<script lang="ts" setup>
import fetcher from '@/exceptionHandler/exceptionHandler';
import { getGroups, Group } from '@/models/Group';
import { RegistrationResponse, Registration } from '@/models/Registration';
import { useAuthStore } from '@/stores/AuthStore';
import { computed, ref, Ref, shallowRef } from 'vue';

const props = defineProps<{registration: Registration}>()
const emits = defineEmits(["onErrorOccured", "onSuccess"])


const showExtraInfo = ref(false)
const groups: Ref<Group[]> = ref<Group[]>([])
const selectedGroups: Ref<Group[]> = ref<Group[]>([])

const showDialog = shallowRef(false)
const errorMessage = ref<string>("");

const chipColor = computed(() => {
    const status = props.registration.accepted
    if (status == undefined) {
        return "dark_grey"
    } else if (status) {
        return "green"
    } else {
        return "red"
    }
}) 

const chipText = computed(() => {
    const status = props.registration.accepted
    if (status == undefined) {
        return "unprocessed"
    } else if (status) {
        return "accepted"
    } else {
        return "declined"
    }
})

const showProcessed = computed(() => {
    return !(props.registration.accepted == undefined)
})

function formatDate(date: string) {
    const parsed_date = new Date(date);
    const hours = String(parsed_date.getHours()).padStart(2, '0');
    const minutes = String(parsed_date.getMinutes()).padStart(2, '0');
    return parsed_date.toDateString() + " " + hours + ":" + minutes;
}

async function acceptRequest() {
    console.log("Accepted request")
    showDialog.value = false
    const data: RegistrationResponse = new RegistrationResponse(props.registration.id, true, selectedGroups.value)
    sendRequest(data)
}

async function openAcceptDialog() {
    groups.value = await getGroups(useAuthStore().authHeader())
    showDialog.value = true
}

function cancelRequestAcceptation() {
    console.log("Cancel accept request")
    selectedGroups.value = []
    showDialog.value = false
}

async function declineRequest() {
    console.log("Decline request")
    const data: RegistrationResponse = new RegistrationResponse(props.registration.id, false, [])
    sendRequest(data)
}

async function deleteRequest() {
    await fetcher.delete('/register/' + props.registration.id, {headers: useAuthStore().authHeader()})
    location.reload()
}

async function sendRequest(data: RegistrationResponse) {
    console.log("Sending registration response")
    console.log(data)
    await fetcher.post("/register/process", data,{headers: useAuthStore().authHeader()})
    .then(async () => {
        emits('onSuccess')
    })
    .catch((error) => {
        if(error.response.data) {
            errorMessage.value = error.response.data;
        } else if (typeof error.response.data.message === "object") {
            errorMessage.value = "Something went wrong.";
        }
        emits('onErrorOccured', errorMessage.value)
    })
    
}

</script>