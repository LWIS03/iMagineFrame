<template>
  <v-dialog v-model="dialog" max-width="600px">
    <v-card>
      <v-img
        v-if="imageUrl"
        :src="imageUrl"
        height="200"
        cover
      ></v-img>
      <div v-else class="d-flex justify-center align-center grey lighten-3" style="height: 100px;">
        <v-icon size="large" color="grey">mdi-image</v-icon>
      </div>

      <v-card-title class="text-h5">
        {{ event.name }}
      </v-card-title>

      <v-card-subtitle>
        <v-chip :color="getEventColor(event.label)" class="mr-2">{{ event.label }}</v-chip>
        <span><v-icon icon="mdi-map-marker" size="small"></v-icon> {{ event.location }}</span>
      </v-card-subtitle>

      <v-card-text>
        <div class="mb-4">
          <v-icon icon="mdi-calendar" size="small"></v-icon>
          {{ formatDateString(event.startdate) }}
          <span v-if="event.enddate">
            - {{ formatDateString(event.enddate) }}
          </span>
        </div>

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
              color="primary"
              variant="text"
              @click="dialog = false"
            >
              CLOSE
            </v-btn>
          </div>
        </div>
      </v-card-actions>

      <v-divider class="my-4"></v-divider>

      <div class="pl-3">
        <div class="d-flex justify-space-between align-center">
          <v-btn
            v-if="participantCount > 0"
            variant="text"
            size="small"
            @click="toggleParticipantList"
          >
            {{ showParticipants?'hide':'show'}} participants
          </v-btn>
        </div>

        <v-expand-transition>
          <div v-if="showParticipants && participantCount > 0">
            <div class="d-flex justify-space-between align-center my-2">
              <v-btn
                v-if="checkPrivileges(['event_create'], authStore.userInfo?.privileges || [])"
                variant="text"
                size="x-small"
                @click="toggleSorting"
              >
                <v-icon size="small">mdi-sort-alphabetical-ascending</v-icon>
                {{ sortByName ? 'Sort by Name' : 'Sort by Email' }}
              </v-btn>
            </div>

            <v-list density="compact" class="bg-grey-lighten-4 rounded">
              <v-list-item
                v-for="participant in participants"
                :key="participant.id"
                :title="`${participant.firstName} ${participant.lastName}`"
                :subtitle="participant.email"
              >
              </v-list-item>
            </v-list>
          </div>
        </v-expand-transition>
      </div>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useAuthStore } from '@/stores/AuthStore';
import fetcher from '@/exceptionHandler/exceptionHandler';
import { Event, getEventColor } from '@/models/Event';
import {checkPrivileges} from '@/models/Group' 

const props = defineProps<{
  modelValue: boolean;
  event: Event;
}>();

const emit = defineEmits(['update:modelValue','attendanceupdated']);

const dialog = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

const authStore = useAuthStore();
const attend = ref<boolean>(false);
const loading = ref<boolean>(false);
const imageUrl = ref<string>('');
const participants = ref<Array<any>>([]);
const count = ref<number>(0);
const showParticipants = ref<boolean>(false);
const sortByName = ref<boolean>(true);

const participantCount = computed(() => count.value);

const isEventOwner = computed(() => {
  return props.event?.owner?.id &&
    authStore.userInfo?.id &&
    props.event.owner.id === parseInt(authStore.userInfo.id);
});

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
    emit('attendanceupdated');
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
  if (!props.event.imageUrl || typeof props.event.imageUrl !== 'string') {return;}
  try {
    const response = await fetch(props.event.imageUrl, {headers: authStore.authHeader() as HeadersInit});
    if (response.ok) {
      const blob = await response.blob();
      imageUrl.value = URL.createObjectURL(blob);
    }
  } catch (error) {
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

watch(() => dialog.value, (newValue) => {
  if (newValue && props.event.id) {
    getAttendance();
    getParticipantCount();
  }
});
</script>
