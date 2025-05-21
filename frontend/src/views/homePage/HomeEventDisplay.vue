<template>
  <v-card  :image="imageUrl" variant="text" class="h-100">
    <v-card-title class="d-flex justify-center align-center text-white text-h5 w-100 opaque-black-bg">
      {{ event.name }}
    </v-card-title>
    <div class="spacer"></div>
    <v-card-text class="opaque-black-bg"
      height="55">
      <v-row class="text-white">
        <v-col cols="5" class="ml-4">
          <v-icon icon="mdi-domain"/> {{ event.location }}
        </v-col>
        <v-spacer/>
        <v-col cols="6">
          <v-icon icon="mdi-calendar"/> {{ event.startdate?.split('T')[0].split('-').slice(1,3).reverse().join("/") }}
                        &emsp;
          <v-icon icon="mdi-clock-time-two"/> {{ event.startdate?.split('T')[1].split(':').slice(0,2).join("h") }}
        </v-col>
      </v-row>
      <v-row>
        <v-btn 
          @click="reveal = reveal? false: true" 
          block>
          More info
        </v-btn>
      </v-row>
    </v-card-text>
            
    <v-fade-transition>
      <v-card 
        v-if="reveal"
        class="position-absolute w-100 h-100 d-flex flex-column border"
        
        style="bottom: 0;">
        <v-card-title class="text-h5">
          {{ event.name }}
        </v-card-title>

        <v-card-subtitle>
          <v-row>
            <v-col>
              <span>
                <v-chip :color="getEventColor(event.label)" class="mr-2">{{ event.label }}</v-chip>
                <v-icon icon="mdi-map-marker" size="small"></v-icon> {{ event.location }}
              </span>
            </v-col>
            <v-col>
              <v-icon icon="mdi-calendar" size="small"></v-icon>
              {{ formatDateString(event.startdate) }}
              <span v-if="event.enddate">
                - {{ formatDateString(event.enddate) }}
              </span>

            </v-col>
          </v-row>
          
        </v-card-subtitle>

        <v-card-text>
          

          <p>{{ event.description }}</p>

          <v-divider class="my-4"></v-divider>

          <div class="d-flex justify-space-between align-center">
            <div v-if="checkPrivileges(['event_create'], authStore.userInfo?.privileges || [])">
              <p><strong>Attending:</strong>
                <v-chip
                  :color="attend?'success':'error'"
                  size="small"
                >
                  {{ attend?'yes':'no'}}
                </v-chip>
              </p>
            </div>

            <div>
              <p><strong>Participants:</strong> <v-chip color="primary" size="small">{{ participantCount }}</v-chip></p>
            </div>
          </div>
        </v-card-text>

        <v-card-actions class="px-4">
          <div class="d-flex justify-space-between align-center w-100">
            <div class="d-flex">
              <v-btn
                v-if="checkPrivileges(['logon'], authStore.userInfo?.privileges || [])"
                color="success"
                variant="outlined"
                :loading="loading"
                :disabled="attend"
                @click="confirmAttendance"
                style="margin-left: 5px;"
                class="mr-2"
              >
                ATTEND
              </v-btn>
              <v-btn
                v-if="checkPrivileges(['logon'], authStore.userInfo?.privileges || [])"
                color="error"
                variant="outlined"
                :loading="loading"
                :disabled="!attend"
                @click="denyAttendance"
              >
                CANCEL ATTENDANCE
              </v-btn>
            </div>
            <div>
              <v-btn
                variant="tonal"
                color="primary"
                @click="reveal = reveal? false: true">
                CLOSE
              </v-btn>
            </div>
          </div>
        </v-card-actions>
      </v-card>
    </v-fade-transition>
  </v-card>
</template>
<script lang="ts" setup>
import { ref, computed, watch } from 'vue';
import { useAuthStore } from '@/stores/AuthStore';
import fetcher from '@/exceptionHandler/exceptionHandler';
import { Event, getEventColor } from '@/models/Event';
import { checkPrivileges } from "@/models/Group";

const props = defineProps<{
  event: Event;
}>();

const emit = defineEmits(['update:modelValue']);

const authStore = useAuthStore();
const attend = ref<boolean>(false);
const loading = ref<boolean>(false);
const imageUrl = ref<string>('');
const participants = ref<Array<any>>([]);
const count = ref<number>(0);
const showParticipants = ref<boolean>(false);
const sortByName = ref<boolean>(true);
const reveal = ref<boolean>(false);
const participantCount = computed(() => count.value);

function formatDateString(dateString: string | null): string {
  if (!dateString) {return '';}
  const date = new Date(dateString);
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const year = date.getFullYear();
  const time = date.toTimeString().substring(0, 5);
  return `${day}/${month}/${year} ${time}`;
}



async function getAttendance(): Promise<void> {
  try {
    loading.value = true;
    const response = await fetcher.get(`/events/${props.event.id}/attendance`, {headers: authStore.authHeader()});
    attend.value = response.data;
  } catch (error) {
    console.error('error checking attendance:',error);
  } finally {
    loading.value = false;
  }
}

async function confirmAttendance(): Promise<void> {
  try {
    loading.value = true;
    await fetcher.post(`/events/${props.event.id}/attend`, {}, {headers: authStore.authHeader()});
    attend.value = true;
    getParticipantCount();
    if (showParticipants.value) {
      getParticipants();
    }
  } catch (error) {
    console.error('error attend:', error);
  } finally {
    loading.value = false;
  }
}

async function denyAttendance(): Promise<void> {
  try {
    loading.value = true;
    await fetcher.delete(`/events/${props.event.id}/attend`, {headers: authStore.authHeader()});
    attend.value = false;
    getParticipantCount();
    if (showParticipants.value) {
      getParticipants();
    }
  } catch (error) {
    console.error('error cancelling: ', error);
  } finally {
    loading.value = false;
  }
}

async function loadImage(): Promise<void> {
  if (!props.event.imageUrl || typeof props.event.imageUrl !== 'string') {
    imageUrl.value = 'src/assets/logos/imaginelab_logo.png';
    return
  }
  try {
    const response = await fetch(props.event.imageUrl, {headers: authStore.authHeader() as HeadersInit});
    if (response.ok) {
      const blob = await response.blob();
      imageUrl.value = URL.createObjectURL(blob);
    }
  } catch (error) {
    imageUrl.value = 'src/assets/logos/imaginelab_logo.png';
    console.error('error loading image:', error);
  }
}

async function getParticipantCount(): Promise<void> {
  try {
    const response = await fetcher.get(`/events/${props.event.id}/participants/count`, {headers: authStore.authHeader()});
    count.value = response.data;
  } catch (error) {
    count.value = 0;
  }
}

async function getParticipants(): Promise<void> {
  try {
    const endpoint = sortByName.value ? `/events/${props.event.id}/participants/alphabetical` : `/events/${props.event.id}/participants/email`;
    const response = await fetcher.get(endpoint, {headers: authStore.authHeader()});
    participants.value = response.data;
  } catch (error) {
    participants.value = [];
  }
}

function toggleParticipantList(): void {
  showParticipants.value = !showParticipants.value;
  if (showParticipants.value) {
    getParticipants();
  }
}

function toggleSorting(): void {
  if (sortByName.value && !checkPrivileges(['event_create'], authStore.userInfo?.privileges || [])) {
    return;
  }
  sortByName.value = !sortByName.value;
  getParticipants();
}

watch(() => props.event, (newEvent, oldEvent) => {
  if (newEvent.id !== oldEvent?.id) {
    URL.revokeObjectURL(imageUrl.value);
    imageUrl.value = '';
    showParticipants.value = false;
  }

  if (props.event.id) {
    getAttendance();
    getParticipantCount();
    loadImage();
  }
}, { immediate: true });

</script>

<style scoped>
  .spacer {
    height:110px;
  }

  .opaque-black-bg{
    background-color: rgba(0,0,0,0.2)
  }
  </style>