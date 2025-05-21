<template>
  <div class="pa-5">
    <div class="d-flex justify-space-between">
      <h2>Events</h2>
      <div class="d-flex align-center">
        <v-btn-toggle
          v-model="activeView"
          color="primary"
          mandatory
          class="mr-4"
        >
          <v-btn value="list">
            <v-icon>mdi-format-list-bulleted</v-icon>
            LIST
          </v-btn>
          <v-btn value="calendar">
            <v-icon>mdi-calendar-month</v-icon>
            CALENDAR
          </v-btn>
        </v-btn-toggle>

        <v-btn
          v-if="checkPrivileges(['event_create'], authStore.userInfo?.privileges || [])"
          color="primary"
          :to="'/events/new'"
        >
          Add new event
        </v-btn>
      </div>
    </div>

    <div class="mt-4 mb-4 d-flex justify-space-between align-center">
      <div class="d-flex align-center">
        <v-text-field
          v-model="startDate"
          label="Start Date"
          type="date"
          class="mr-2"
          style="max-width: 200px"
        ></v-text-field>

        <v-text-field
          v-model="endDate"
          label="End Date"
          type="date"
          class="mr-2"
          style="max-width: 200px"
        ></v-text-field>

        <v-btn color="primary" @click="applyFilters" class="mr-2">
          FILTER
        </v-btn>

        <v-btn variant="outlined" @click="resetFilters">
          RESET
        </v-btn>
      </div>

      <div v-if="hasParticipatingEvents">
        <v-menu>
          <template v-slot:activator="{ props }">
            <v-btn
              color="primary"
              v-bind="props"
            >
              <v-icon class="mr-2">mdi-calendar-export</v-icon>
              EXPORT
            </v-btn>
          </template>
          <v-list>
            <v-list-item @click="exportCalendar" prepend-icon="mdi-download">
              <v-list-item-title>Download Calendar File</v-list-item-title>
            </v-list-item>
            <v-list-item @click="showSubscriptionUrl" prepend-icon="mdi-link">
              <v-list-item-title>Get Subscription URL</v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>
      </div>
    </div>

    <div v-if="activeView === 'list'">
      <div class="text-caption mb-2">
        <v-icon icon="mdi-information" size="small"></v-icon>
        Click on an event row to see details and manage your attendance
      </div>

      <v-table hover>
        <thead>
          <tr>
            <th class="text-left">Name</th>
            <th class="text-left">Location</th>
            <th class="text-left">Type</th>
            <th class="text-left">Start Date</th>
            <th class="text-left">End Date</th>
            <th v-if="checkPrivileges(['event_manage'], authStore.userInfo?.privileges || [])" class="text-left">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="event in events"
            :key="event.id"
            @click="openEventDetails(event)"
            class="clickable-row"
          >
            <td>{{ event.name }}</td>
            <td>{{ event.location }}</td>
            <td>{{ event.label }}</td>
            <td>{{ formatDateString(event.startdate) }}</td>
            <td>{{ formatDateString(event.enddate) }}</td>
            <td>
              <v-btn
                color="primary"
                icon="mdi-pencil"
                size="x-small"
                :to="isEventOwner(event) || checkPrivileges(['event_manage'], authStore.userInfo?.privileges || []) ? '/events/' + event.id : ''"
                :disabled="!(isEventOwner(event) || checkPrivileges(['event_manage'], authStore.userInfo?.privileges || []))"
                class="mr-2"
                @click.stop
              ></v-btn>

              <v-btn
                color="error"
                icon="mdi-delete"
                size="x-small"
                @click.stop="(isEventOwner(event) || checkPrivileges(['event_manage'], authStore.userInfo?.privileges || [])) ? (deleteDialog = true, deleteEventId = event.id) : ''"
                :disabled="!(isEventOwner(event) || checkPrivileges(['event_manage'], authStore.userInfo?.privileges || []))"
              ></v-btn>
              <v-btn
                color="info"
                icon="mdi-eye"
                size="x-small"
                @click.stop="openEventDetails(event)"
                class="ml-2"
              ></v-btn>
            </td>
          </tr>
        </tbody>
      </v-table>
      <div v-if="events.length === 0" class="text-center mt-8">
        <p>No events found</p>
      </div>
    </div>

    <div v-else-if="activeView === 'calendar'" class="mt-4">
      <v-calendar
        :attributes="calendarAttributes"
        is-expanded
        :first-day-of-week="1"
        @dayclick="onDayClick"
        :columns="1"
        :rows="1"
        class="custom-calendar"
        :style="{ fontSize: '1.5em', width: '100%' }"
      />
    </div>

    <v-dialog v-model="deleteDialog" max-width="300">
      <v-card>
        <v-card-text>Are you sure you want to delete this event?</v-card-text>
        <v-card-actions>
          <v-btn color="error" @click="deleteEvent(deleteEventId)">Confirm</v-btn>
          <v-btn color="secondary" @click="deleteDialog = false">Cancel</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <event-detail-dialog
      v-model="showEventDetails"
      :event="selectedEvent"
      @attendanceupdated="checkUserParticipation"
    />

    <v-dialog v-model="urlDialog" max-width="600px">
      <v-card>
        <v-card-title>Calendar Subscription URL</v-card-title>
        <v-card-text>
          <p>Copy this URL and add it to your calendar application</p>
          <v-text-field
            v-model="urlical"
            readonly
            :loading="urlLoading"
            append-icon="mdi-content-copy"
            @click:append="copyToClipboard"
          ></v-text-field>
          <v-alert type="info" class="mt-4" dense>
            This URL will automatically update your calendar when events change
          </v-alert>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" @click="urlDialog = false">Close</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-snackbar v-model="snackbar" :timeout="2000">
      URL copied to clipboard!
    </v-snackbar>
  </div>
</template>

<script lang="ts" setup>
import fetcher from "@/exceptionHandler/exceptionHandler";
import { ref, computed, onMounted } from "vue";
import { useAuthStore } from "@/stores/AuthStore";
import { Event, getEventColor} from "@/models/Event";
import { checkPrivileges } from "@/models/Group";
import EventDetailDialog from "./EventDetailDialog.vue"; // Using relative path

const events = ref<Array<Event>>([]);
const authStore = useAuthStore();
const activeView = ref('list');
const startDate = ref('');
const endDate = ref('');
const deleteDialog = ref(false);
const deleteEventId = ref<number | null>(null);
const showEventDetails = ref(false);
const selectedEvent = ref<Event>(new Event());
const exportLoading = ref(false);
const userParticipatingEvents = ref<Event[]>([]);
const urlDialog = ref(false);
const urlical = ref('');
const urlLoading = ref(false);
const snackbar = ref(false);

const calendarAttributes = computed(() => {
  return events.value.map(event => {
    let startDateObj = null;
    if (event.startdate) {
      startDateObj = new Date(event.startdate);
    }
    let endDateObj = null;
    if (event.enddate) {
      endDateObj = new Date(event.enddate);
    }

    const color = getEventColor(event.label);

    let dateObj;
    if (startDateObj && endDateObj) {
      dateObj = { start: startDateObj, end: endDateObj };
    } else {
      dateObj = startDateObj;
    }

    return {
      key: event.id,
      dates: dateObj,
      highlight: {
        color: color,
        fillMode: 'light',
      },
      dot: { color: color },
      popover: {
        label: event.name,
        description: event.location,
        visibility: 'hover',
      },
      customData: event,
    };
  });
});

const hasParticipatingEvents = computed(() => {
  return userParticipatingEvents.value && userParticipatingEvents.value.length > 0;
});

function openEventDetails(event: Event): void {
  selectedEvent.value = event;
  showEventDetails.value = true;
}

function isEventOwner(event : Event) {
  return event.owner && authStore.userInfo && event.owner.id === parseInt(authStore.userInfo.id);
}

function onDayClick(day: any): void {
  const clickedEvents = events.value.filter(event => {
    if (!event.startdate) {return false;}
    const startDate = new Date(event.startdate);
    const endDate = event.enddate ? new Date(event.enddate) : startDate;
    const dayDate = new Date(day.id);
    return dayDate >= startDate && dayDate <= endDate;
  });

  if (clickedEvents.length > 0) {
    openEventDetails(clickedEvents[0]);
  }
}

async function initData(): Promise<void> {
  try {
    const res = await fetcher.get("/events", {headers: authStore.authHeader()});
    if(res && res.data) {
      events.value = res.data;
    } else {
      events.value = [];
      console.log("No data returned from API: " + res.status);
    }
  } catch (error) {
    events.value = [];
  }
}

async function applyFilters(): Promise<void> {
  try {
    let url = `/events/filter`;
    let params: string[] = [];
    if (startDate.value) {
      params.push(`startDate=${startDate.value}T00:00:00`);
    }
    if (endDate.value) {
      params.push(`endDate=${endDate.value}T23:59:59`);
    }

    let queryString = '';
    if (params.length > 0) {
      queryString = '?' + params.join('&');
    }

    const response = await fetcher.get(url + queryString, {
      headers: authStore.authHeader()
    });

    if (response && response.data) {
      events.value = response.data;
    } else {
      events.value = [];
    }
  } catch (error) {
    console.error("error filtering events:", error);
  }
}

async function deleteEvent(id: number | null): Promise<void> {
  if (!id) {return;}

  try {
    await fetcher.delete(`/events/${id}`, {
      headers: authStore.authHeader()
    });
    deleteDialog.value = false;
    initData();
  } catch (error) {
    console.error("Error deleting event:", error);
  }
}

function resetFilters(): void {
  startDate.value = '';
  endDate.value = '';
  initData();
}

function formatDateString(dateString: string | null): string {
  if (dateString == null) {
    return '';
  }

  const date = new Date(dateString);
  const day = String(date.getDate()).padStart(2,'0'); //make sure that dd and mm consist of 2 numbers each
  const month = String(date.getMonth()+1).padStart(2,'0');
  const year = date.getFullYear();
  const time = date.toTimeString().substring(0,5);

  return `${day}/${month}/${year} ${time}`;
}

async function checkUserParticipation(): Promise<void> {
  try {
    const response = await fetcher.get("/events/my-events", {headers: authStore.authHeader()});
    userParticipatingEvents.value = response.data;
  } catch (error) {
    console.error("error fetching events:", error);
    userParticipatingEvents.value = [];
  }
}

async function exportCalendar(): Promise<void> {
  try {
    exportLoading.value = true;
    const response = await fetcher.get("/events/my-calendar", {headers: authStore.authHeader(), responseType: 'blob'});
    const blob = new Blob([response.data], { type: 'text/calendar' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'my-events.ics';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error("error exporting: ", error);
  } finally {
    exportLoading.value = false;
  }
}

async function showSubscriptionUrl(): Promise<void> {
  try {
    urlLoading.value = true;
    urlDialog.value = true;
    const response = await fetcher.post("/events/my-calendar-url", {}, {headers: authStore.authHeader()});
    urlical.value = response.data.url;
  } catch (error) {
    console.error("Error generating calendar URL:", error);
  } finally {
    urlLoading.value = false;
  }
}

function copyToClipboard(): void {
  navigator.clipboard.writeText(urlical.value).then(() => { snackbar.value = true;}).catch(err => { console.error('Failed to copy text: ', err);});
}

onMounted(() => {
  initData();
  checkUserParticipation();
});
</script>

<style scoped>
.clickable-row {
  cursor: pointer;
}
.clickable-row:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

.custom-calendar {
  max-width: 1200px;
  margin: 0 auto;
}
.custom-calendar .vc-day {
  min-height: 100px;
}
.custom-calendar .vc-day-content {
  font-size: 1.3em;
}
</style>
