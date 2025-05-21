<template>
  <v-dialog v-model="dialog" max-width="600px">
    <v-card>
      <v-img
        v-if="project.mediaUrl"
        :src="project.mediaUrl"
        height="200"
        cover
      ></v-img>
      <div v-else class="d-flex justify-center align-center grey lighten-3" style="height: 100px;">
        <v-icon size="large" color="grey">mdi-clipboard-check-outline</v-icon>
      </div>

      <v-card-title class="text-h5">
        {{ project.name }}
      </v-card-title>

      <v-card-subtitle>
        <v-chip
          :color="getStatusColor(project.status)"
          size="small"
          class="mt-2"
        >
          {{ project.status }}
        </v-chip>
      </v-card-subtitle>

      <v-card-text>
        <p class="mb-4">{{ project.description }}</p>

        <v-divider class="my-4"></v-divider>

        <div v-if="visibleUsers && visibleUsers.length > 0">
          <p class="font-weight-bold">Contributors:</p>
          <v-chip-group>
            <v-chip
              v-for="user in visibleUsers"
              :key="user.id"
              color="primary"
              variant="outlined"
              size="small"
              disabled
            >
              {{ user.firstName }} {{ user.lastName }}
            </v-chip>
          </v-chip-group>
        </div>
        <p v-else class="text-body-2">No contributors assigned</p>
      </v-card-text>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          v-if="!isProjectMember && checkPrivileges(['project_read'], authStore.userInfo?.privileges || [])"
          color="primary"
          class="mx-2"
          @click="requestJoinProject"
          :loading="loading"
        >
          Request to join project
        </v-btn>

        <v-btn
          v-if="isProjectMember && !isProjectOwner && checkPrivileges(['project_read'], authStore.userInfo?.privileges || [])"
          color="error"
          class="mx-2"
          @click="leaveProject"
          :loading="loading"
        >
          Leave project
        </v-btn>

        <v-btn
          v-if="checkPrivileges(['project_write'], authStore.userInfo?.privileges || [])"
          color="primary"
          :to="'/projects/' + project.id"
          class="mx-2"
        >
          Edit
        </v-btn>
        <v-btn
          color="primary"
          variant="text"
          @click="dialog = false"
          class="mx-2"
        >
          Close
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <v-snackbar v-model="snackbar" :color="snackbarColor" timeout="3000">
    {{ snackbarText }}
  </v-snackbar>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useAuthStore } from '@/stores/AuthStore';
import { Project } from '@/models/Project';
import { checkPrivileges } from "@/models/Group";
import { ProjectJoinRequestAddDto } from '@/models/ProjectJoinRequest';
import { User } from '@/models/User';
import fetcher from "@/exceptionHandler/exceptionHandler";

const props = defineProps<{
  modelValue: boolean;
  project: Project;
}>();

const emit = defineEmits(['update:modelValue', 'project-updated']);

const dialog = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

const isProjectMember = computed(() => {
  if (!props.project || !authStore.userInfo) {return false;}
  return props.project.users.some(user => user.id === parseInt(authStore.userInfo?.id || '0'));
});

const isProjectOwner = computed(() => {
  if (!props.project || !props.project.owner || !authStore.userInfo) {return false;}
  return props.project.owner.id === parseInt(authStore.userInfo?.id || '0');
});

const authStore = useAuthStore();
const loading = ref(false);
const snackbar = ref(false);
const snackbarText = ref('');
const snackbarColor = ref('success');
const visibleUsers = ref<User[]>([]);

function getStatusColor(status: string): string {
  switch (status) {
    case 'PLANNING':
      return 'blue';
    case 'IN_PROGRESS':
      return 'amber';
    case 'COMPLETED':
      return 'green';
    case 'ON_HOLD':
      return 'orange';
    case 'CANCELLED':
      return 'red';
    default:
      return 'grey';
  }
}

async function requestJoinProject() {
  try {
    loading.value = true;
    const requestDto = new ProjectJoinRequestAddDto(props.project.id);
    await fetcher.post('/projects/requests/new', requestDto, {
      headers: authStore.authHeader()
    });

    snackbarText.value = 'Join request sent successfully';
    snackbarColor.value = 'success';
    snackbar.value = true;

    setTimeout(() => {
      dialog.value = false;
    }, 1000);

  } catch (error : any) {
    snackbarText.value = error.response?.data || 'Failed to send join request';
    snackbarColor.value = 'error';
    snackbar.value = true;
  } finally {
    loading.value = false;
  }
}

async function leaveProject() {
  try {
    if (isProjectOwner.value) {
      snackbarText.value = 'Project creator cannot leave the project';
      snackbarColor.value = 'error';
      snackbar.value = true;
      return;
    }

    loading.value = true;

    const updatedUsers = props.project.users.filter(user => user.id !== parseInt(authStore.userInfo?.id || '0')).map(user => ({ id: user.id }));

    const projectData = {
      name: props.project.name,
      description: props.project.description,
      status: props.project.status,
      users: updatedUsers
    };

    const formData = new FormData();
    formData.append('project', JSON.stringify(projectData));

    const headers = authStore.authHeader();
    (headers as any)['Content-Type'] = 'multipart/form-data';

    const response = await fetcher.post(`/projects/requests/leave-project/${props.project.id}`, {}, {
      headers: authStore.authHeader()
    });

    snackbarText.value = 'Left project successfully';
    snackbarColor.value = 'success';
    snackbar.value = true;

    setTimeout(() => {
      dialog.value = false;
      emit('project-updated');
    }, 1000);
  } catch (error : any) {
    snackbarText.value = error.response?.data || 'Failed to leave project';
    snackbarColor.value = 'error';
    snackbar.value = true;
  } finally {
    loading.value = false;
  }
}

async function fetchVisibleUsers() {
  try {
    if (!props.project || !props.project.id) {return;}

    const response = await fetcher.get(`/projects/${props.project.id}/visible-users`, {
      headers: authStore.authHeader()
    });
    visibleUsers.value = response.data;
  } catch (error) {
    console.error("Error fetching visible users:", error);
    visibleUsers.value = [];
  }
}

watch(() => dialog.value, (newValue) => {
  if (newValue) {
    fetchVisibleUsers();
  }
});

watch(() => props.project, (newProject) => {
  if (newProject && dialog.value) {
    fetchVisibleUsers();
  }
});

watch(() => snackbar.value, (newValue) => {
  if (newValue) {
    setTimeout(() => {
      snackbar.value = false;
    }, 3000);
  }
});
</script>

<style scoped>
.v-card-text {
  max-height: 300px;
  overflow-y: auto;
}
</style>
