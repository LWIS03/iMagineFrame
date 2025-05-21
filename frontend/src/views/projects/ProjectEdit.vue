<template>
  <v-alert
    v-model="showSuccess"
    border="start"
    variant="tonal"
    closable
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
    <h2 v-if="action === 'edit'">Edit Project: {{ project.name }}</h2>
    <h2 v-if="action === 'new'">Create Project</h2>

    <v-row no-gutters class="mt-4">
      <v-col cols="12">
        <v-switch
          v-model="editProject.public"
          label="Public project"
          hint="When enabled, all users can see this project"
          color="primary"
        ></v-switch>
      </v-col>
    </v-row>

    <v-row no-gutters>
      <v-col cols="12">
        <v-text-field
          v-model="editProject.name"
          :counter="50"
          :error-messages="fieldErrors.get('name')"
          label="Project Name"
          required
        ></v-text-field>
      </v-col>
    </v-row>

    <v-row no-gutters>
      <v-col cols="12">
        <v-textarea
          v-model="editProject.description"
          :counter="500"
          :error-messages="fieldErrors.get('description')"
          label="Description"
          rows="4"
          required
        ></v-textarea>
      </v-col>
    </v-row>

    <v-row no-gutters>
      <v-col cols="12">
        <v-select
          v-model="editProject.status"
          :items="statusOptions"
          label="Project Status"
          :error-messages="fieldErrors.get('status')"
          required
        ></v-select>
      </v-col>
    </v-row>

    <v-row no-gutters>
      <v-col cols="12">
        <v-select
          v-model="selectedUserIds"
          :items="availableUsers"
          item-title="fullName"
          item-value="id"
          label="Project Contributors"
          :multiple="true as any"
          chips
        ></v-select>
        <small v-if="projectOwnerId && action === 'edit'" class="text-caption text-grey ml-2">
          Project creator cannot be removed from contributors
        </small>
      </v-col>
    </v-row>

    <v-row no-gutters>
      <v-col cols="12">
        <label class="text-subtitle-2 mb-1 d-block">Project Media</label>
        <small class="text-caption d-block mb-2 text-grey">No file selected (only PNG & JPG accepted)</small>
        <div class="d-flex align-center">
          <v-icon class="mr-2">mdi-camera</v-icon>
          <input
            type="file"
            accept="image/jpeg,image/png,.jpg,.jpeg,.png"
            @change="onMediaSelected"
            ref="fileInput"
            style="width: 100%"
          />
        </div>

        <div v-if="fileTypeError" class="mt-2 text-red">
          {{ fileTypeError }}
        </div>

        <div v-if="imagePreview" class="mt-2">
          <img :src="imagePreview" alt="Project image preview" style="max-width: 200px; max-height: 150px;" />
        </div>
      </v-col>
    </v-row>

    <v-row class="mt-4">
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
          to="/projects"
        >
          Back
        </v-btn>
      </v-col>
    </v-row>
  </div>
</template>

<script setup lang="ts">
import fetcher from "@/exceptionHandler/exceptionHandler";
import { useRoute, useRouter } from "vue-router";
import { computed, ref, watch } from "vue";
import { useAuthStore } from "@/stores/AuthStore";
import { Project, ProjectEditDto } from "@/models/Project";
import { checkPrivileges } from "@/models/Group";
import { validateImageFile, createImagePreview } from '@/models/Image';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const fileInput = ref(null);
const project = ref(new Project());
const editProject = ref(new ProjectEditDto());
const showSuccess = ref(false);
const showError = ref(false);
const errorMessage = ref("");
const fieldErrors = ref(new Map());
const action = ref("new");
const statusOptions = ref([
  'PLANNING',
  'IN_PROGRESS',
  'COMPLETED',
  'ON_HOLD',
  'CANCELLED'
]);
const imagePreview = ref<string>("");
const mediaFile = ref<File | null>(null);
const hasEditPermission = computed(() => {
  return checkPrivileges(['project_write'], authStore.userInfo?.privileges || []);
});
const projectOwnerId = ref<number | null>(null);
const originalUserIds = ref<Array<number>>([]);

interface User {
  id: number;
  firstName: string;
  lastName: string;
  fullName: string;
}

const availableUsers = ref<Array<{ id: number, firstName: string, lastName: string, fullName: string }>>([]);
const selectedUserIds = ref<Array<number>>([]);
const fileTypeError = ref<string>("");

watch(selectedUserIds, (newValue) => {
  if (action.value === "edit" && projectOwnerId.value && !newValue.includes(projectOwnerId.value)) {
    selectedUserIds.value.push(projectOwnerId.value);
    showError.value = true;
    errorMessage.value = "project creator cannot be removed from contributors";
  }
});

async function onMediaSelected(event: Event) {
  const target = event.target as HTMLInputElement;
  if (!target.files || target.files.length === 0) {return;}

  const file = target.files[0];
  const result = validateImageFile(file);

  fileTypeError.value = result.errorMessage;
  mediaFile.value = result.file;

  if (result.isValid && result.file) {
    imagePreview.value = await createImagePreview(result.file);
  } else {
    imagePreview.value = "";
    target.value = '';
  }
}

async function init() {
  if (!hasEditPermission.value) {
    router.push('/404');
    return;
  }

  await loadUsers();
  await loadProject();
}

async function loadUsers() {
  try {
    const response = await fetcher.get(
      "/projects/contributors",
      {headers: authStore.authHeader()}
    );

    let usersWithFullName = [];
    for (let i = 0; i < response.data.length; i++) {
      let user = response.data[i];
      usersWithFullName.push({
        id: user.id,
        firstName: user.firstName,
        lastName: user.lastName,
        fullName: user.firstName + " " + user.lastName
      });
    }

    availableUsers.value = usersWithFullName;
  } catch (error) {
    console.error("error loading users");
  }
}

async function loadProject() {
  if (route.params.id === "new") {
    action.value = "new";
    return;
  }
  action.value = "edit";

  try {
    const response = await fetcher.get(
      "/projects/" + route.params.id,
      {headers: authStore.authHeader()}
    );
    project.value = response.data;

    if (response.data.owner && response.data.owner.id) {
      projectOwnerId.value = response.data.owner.id;
    }

    editProject.value.name = response.data.name;
    editProject.value.description = response.data.description;
    editProject.value.status = response.data.status;
    editProject.value.public = response.data.public !== undefined ? response.data.public : true;

    if (response.data.users && response.data.users.length > 0) {
      let userIds = [];
      for (let i = 0; i < response.data.users.length; i++) {
        userIds.push(response.data.users[i].id);
      }
      selectedUserIds.value = userIds;
      originalUserIds.value = [...userIds];
    }

    if (response.data.mediaUrl) {
      try {
        const mediaResponse = await fetch(response.data.mediaUrl, {
          headers: authStore.authHeader() as HeadersInit
        });

        if (mediaResponse.ok) {
          const blob = await mediaResponse.blob();
          imagePreview.value = URL.createObjectURL(blob);
        } else {
          console.error("Failed to load media:", mediaResponse.status);
        }
      } catch (error) {
        console.error("Error loading media:", error);
      }
    }
  } catch (error) {
    showError.value = true;
    errorMessage.value = "error loading project";
  }
}

async function handleJoinRequests(projectId: number) {
  try {
    const newUsers = selectedUserIds.value.filter(id => !originalUserIds.value.includes(id));

    if (newUsers.length > 0) {
      const response = await fetcher.get(`/projects/requests/project/${projectId}`, {
        headers: authStore.authHeader()
      });
      const requests = response.data;
      for (let i = 0; i < requests.length; i++) {
        const request = requests[i];
        if (newUsers.includes(request.userId) && request.accepted === null) {
          await fetcher.post(`/projects/requests/${request.id}/accept`, {}, {
            headers: authStore.authHeader()
          });
        }
      }
    }
  } catch (error) {
    console.error("error handling join requests",error);
  }
}

async function save() {
  fieldErrors.value.clear();

  if (action.value === "edit" && projectOwnerId.value && !selectedUserIds.value.includes(projectOwnerId.value)) {
    selectedUserIds.value.push(projectOwnerId.value);
  }

  let hasErrors = false;

  if (!editProject.value.name || editProject.value.name.trim() === '') {
    fieldErrors.value.set('name', 'Name is required');
    hasErrors = true;
  }

  if (!editProject.value.description || editProject.value.description.trim() === '') {
    fieldErrors.value.set('description', 'Description is required');
    hasErrors = true;
  }

  if (!editProject.value.status) {
    fieldErrors.value.set('status', 'Status is required');
    hasErrors = true;
  }

  if (hasErrors) {
    showError.value = true;
    errorMessage.value = "Please fill all required fields";
    return;
  }

  try {
    let userList = [];
    for (let i = 0; i < selectedUserIds.value.length; i++) {
      userList.push({ id: selectedUserIds.value[i] });
    }

    const projectData = {
      name: editProject.value.name,
      description: editProject.value.description,
      status: editProject.value.status,
      users: userList,
      public: editProject.value.public
    };

    const formData = new FormData();
    formData.append('project', JSON.stringify(projectData));

    if (mediaFile.value) {
      formData.append('media', mediaFile.value);
    }

    const headers = authStore.authHeader();
    (headers as any)['Content-Type'] = 'multipart/form-data';

    if (action.value === "new") {
      const response = await fetcher.post(
        "/projects/new",
        formData,
        { headers: headers }
      );
      console.log("Project created successfully:", response.data);
    } else {
      await fetcher.put(
        "/projects/" + route.params.id,
        formData,
        { headers: headers }
      );

      await handleJoinRequests(parseInt(route.params.id as string));
    }

    showSuccess.value = true;
    setTimeout(function() {
      router.push("/projects");
    }, 1000);
  } catch (error) {
    console.error("Error details:", error);
    showError.value = true;
    errorMessage.value = "error saving project";
  }
}

init();
</script>
