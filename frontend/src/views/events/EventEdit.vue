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
    color="error"
    :title="errorMessage"
  ></v-alert>

  <div class="pa-5">
    <h2 v-if="action === 'edit'">Edit Event: {{ event.name }}</h2>
    <h2 v-if="action === 'new'">Create Event</h2>

    <v-row no-gutters>
      <v-col cols="12">
        <v-text-field
          v-model="editEvent.name"
          :counter="50"
          :error-messages="fieldErrors.get('name')"
          label="Event Name"
          required
        ></v-text-field>
      </v-col>
    </v-row>

    <v-row no-gutters>
      <v-col cols="12">
        <v-text-field
          v-model="editEvent.location"
          :counter="100"
          :error-messages="fieldErrors.get('location')"
          label="Location"
          required
        ></v-text-field>
      </v-col>
    </v-row>

    <v-row no-gutters>
      <v-col cols="12">
        <v-select
          v-model="editEvent.label"
          :items="eventLabels"
          label="Event Type"
          :error-messages="fieldErrors.get('label')"
          required
        ></v-select>
      </v-col>
    </v-row>

    <v-row no-gutters>
      <v-col cols="12">
        <v-textarea
          v-model="editEvent.description"
          :counter="500"
          :error-messages="fieldErrors.get('description')"
          label="Description"
          rows="4"
        ></v-textarea>
      </v-col>
    </v-row>

    <v-row no-gutters>
      <v-col cols="12" md="6">
        <v-text-field
          v-model="startDateInput"
          :error-messages="fieldErrors.get('startdate')"
          label="Start Date (YYYY-MM-DD)"
          type="date"
          :min="minDate"
          required
        ></v-text-field>
      </v-col>
      <v-col cols="12" md="6" class="pl-md-2">
        <v-text-field
          v-model="startTimeInput"
          :error-messages="fieldErrors.get('starttime')"
          label="Start Time (HH:MM)"
          placeholder="--:--"
          required
        ></v-text-field>
      </v-col>
    </v-row>

    <v-row no-gutters>
      <v-col cols="12" md="6">
        <v-text-field
          v-model="endDateInput"
          :error-messages="fieldErrors.get('enddate')"
          label="End Date (YYYY-MM-DD)"
          type="date"
          :min="startDateInput || minDate"
          required
        ></v-text-field>
      </v-col>
      <v-col cols="12" md="6" class="pl-md-2">
        <v-text-field
          v-model="endTimeInput"
          :error-messages="fieldErrors.get('endtime')"
          label="End Time (HH:MM)"
          placeholder="--:--"
          required
        ></v-text-field>
      </v-col>
    </v-row>

    <v-row no-gutters>
      <v-col cols="12">
        <label class="text-subtitle-2 mb-1 d-block">Event Image</label>
        <small class="text-caption d-block mb-2 text-grey">No file selected (only PNG & JPG accepted)</small>
        <div class="d-flex align-center">
          <v-icon class="mr-2">mdi-camera</v-icon>
          <input
            type="file"
            accept="image/jpeg,image/png,.jpg,.jpeg,.png"
            @change="onImageSelected"
            ref="fileInput"
            style="width: 100%"
          />
        </div>
        <div v-if="imagePreview" class="mt-2">
          <img
            :src="imagePreview"
            alt="Event image preview"
            style="max-width: 200px; max-height: 150px;"
          />
        </div>
      </v-col>
    </v-row>

    <v-row no-gutters class="mt-4">
      <v-col cols="12">
        <v-switch
          v-model="editEvent.public"
          label="Public event"
          hint="When enabled, all users can see this event"
          color="primary"
        ></v-switch>
      </v-col>
    </v-row>

    <v-row class="mt-4">
      <v-col>
        <v-btn
          v-if="isAdmin"
          color="success"
          @click="save"
        >
          Save
        </v-btn>
        <v-btn
          class="ml-2"
          color="secondary"
          to="/events"
        >
          Back
        </v-btn>
      </v-col>
    </v-row>
  </div>
</template>

<script lang="ts" setup>
import fetcher from "@/exceptionHandler/exceptionHandler";
import { useRoute, useRouter } from "vue-router";
import { computed, ref } from "vue";
import { useAuthStore } from "@/stores/AuthStore";
import { Event as CalendarEvent, EventEditDto } from "@/models/Event";
import { checkPrivileges } from "@/models/Group";
import { validateImageFile, createImagePreview } from '@/models/Image';
import { PRIVILEGES } from "@/config"

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const fileInput = ref(null);
const event = ref(new CalendarEvent());
const editEvent = ref(new EventEditDto());
const showSuccess = ref(false);
const showError = ref(false);
const errorMessage = ref("");
const fieldErrors = ref(new Map());
const action = ref("new");
const eventLabels = ref([
  'HACKATHON',
  'CODING',
  'EATING',
  'DRINKING',
  'MEETING',
  'LEARNING',
  'PARTY',
  'MOVIE',
  'OTHER'
]);

const startDateInput = ref("");
const startTimeInput = ref("");
const endDateInput = ref("");
const endTimeInput = ref("");
const imagePreview = ref<string | undefined>();
const fileTypeError = ref<string>("");
const mediaFile = ref<File | null>(null);

const minDate = computed(() => {
  const today = new Date();
  return today.toISOString().split('T')[0];
});

const onImageSelected = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (!target.files || target.files.length === 0) {return;}
  const file = target.files[0];
  const result = validateImageFile(file);
  fileTypeError.value = result.errorMessage;
  mediaFile.value = result.file;
  if (result.isValid && result.file) {
    imagePreview.value = await createImagePreview(result.file);
    editEvent.value.imageUrl = imagePreview.value;
  } else {
    imagePreview.value = "";
    target.value = '';
  }
};

const isAdmin = computed(() => {
  // if we create an event we only need event_create
  if (action.value === "new") {
    return checkPrivileges(["event_create"], authStore.userInfo?.privileges || []);
  }

  // Only the event owner or admin can update/edit the event
  const isOwner = event.value.owner && authStore.userInfo && event.value.owner.id === parseInt(authStore.userInfo.id);
  return isOwner || checkPrivileges(["event_manage"], authStore.userInfo?.privileges || []);
});

async function init() {
  if (!isAdmin.value) {
    router.push('/error/401')
    return
  }

  if (route.params.id === "new") {
    action.value = "new"
    return
  }
  try {
    action.value = "edit"
    const { data } = await fetcher.get(
      "/events/" + route.params.id,
      { headers: authStore.authHeader() }
    )

    if (data.owner && authStore.userInfo && data.owner.id !== parseInt(authStore.userInfo.id) && !checkPrivileges(["event_manage"], authStore.userInfo?.privileges || [])) {
      router.push('/404');
      return;
    }
    event.value = data;
    editEvent.value.name = data.name;
    editEvent.value.description = data.description;
    editEvent.value.location = data.location;
    editEvent.value.label = data.label;

    if (data.imageUrl) {
      try {
        const response = await fetch(data.imageUrl, {
          headers: authStore.authHeader() as HeadersInit
        });

        if (response.ok) {
          const blob = await response.blob();
          imagePreview.value = URL.createObjectURL(blob);
        } else {
          console.error("Failed to load image:", response.status);
        }
      } catch (error) {
        console.error("Error loading image:", error);
      }
    }

    if (data.startdate) {
      const startDateTime = new Date(data.startdate)
      startDateInput.value = startDateTime.toISOString().split('T')[0];
      startTimeInput.value = startDateTime.toTimeString().substring(0,5)
      editEvent.value.startdate = data.startdate
    }
    if (data.enddate) {
      const endDateTime = new Date(data.enddate)
      endDateInput.value = endDateTime.toISOString().split('T')[0];
      endTimeInput.value = endDateTime.toTimeString().substring(0,5)
      editEvent.value.enddate = data.enddate
    }
  } catch (error) {
    showError.value = true
    errorMessage.value = "Error loading event"
  }
}

async function save() {
  try {
    fieldErrors.value.clear();

    const now = new Date();
    const startDateTime = new Date(`${startDateInput.value}T${startTimeInput.value}:00`);
    const endDateTime = new Date(`${endDateInput.value}T${endTimeInput.value}:00`);

    if (startDateTime < now) {
      fieldErrors.value.set('starttime', 'Start date and time cannot be in the past');
      showError.value = true;
      errorMessage.value = "Event cannot start in the past";
      return;
    }

    if (endDateTime <= startDateTime) {
      fieldErrors.value.set('endtime', 'End time must be after start time');
      showError.value = true;
      errorMessage.value = "End time must be after start time";
      return;
    }

    if (startDateInput.value && startTimeInput.value) {
      editEvent.value.startdate = `${startDateInput.value}T${startTimeInput.value}:00`;
    }
    if (endDateInput.value && endTimeInput.value) {
      editEvent.value.enddate = `${endDateInput.value}T${endTimeInput.value}:00`;
    }

    if (action.value === "edit") {
      editEvent.value.id = parseInt(route.params.id as string);
    }

    const formData = new FormData();
    formData.append('event', JSON.stringify(editEvent.value));
    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    if (fileInput && fileInput.files && fileInput.files.length > 0) {
      formData.append('image', fileInput.files[0]);
    }
    const headers = authStore.authHeader();
    (headers as any)['Content-Type'] = 'multipart/form-data';

    if (action.value === "new") {
      await fetcher.post("/events/new", formData, { headers: headers });
    } else {
      await fetcher.put(`/events/${route.params.id}`, formData, { headers: headers });
    }

    showSuccess.value = true;
    await new Promise(resolve => setTimeout(resolve, 1000));
    await router.push("/events");
  } catch (error) {
    showError.value = true;
    errorMessage.value = "Error, please fill in all data. Also check start and end date";
  }
}

init();
</script>
