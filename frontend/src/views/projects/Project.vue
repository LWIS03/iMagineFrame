<template>
  <div class="pa-5">
    <div class="d-flex justify-space-between">
      <h2>Projects</h2>
      <v-btn
        v-if="checkPrivileges(['project_write'], authStore.userInfo?.privileges || [])"
        color="primary"
        :to="'/projects/new'"
      >
        Add new project
      </v-btn>
    </div>

    <div class="mt-4 mb-4 d-flex align-center flex-wrap">
      <v-select
        v-model="statusFilter"
        :items="statusOptions"
        label="Status"
        clearable
        class="mr-2"
        style="max-width: 200px"
      ></v-select>

      <v-select
        v-model="userFilters"
        :items="availableUsers"
        item-title="fullName"
        item-value="id"
        label="Contributors"
        :multiple="true as any"
        chips
        clearable
        class="mr-2"
        style="max-width: 300px"
      ></v-select>

      <v-btn color="primary" @click="applyFilters" class="mr-2">
        Filter
      </v-btn>

      <v-btn variant="outlined" @click="resetFilters">
        Reset
      </v-btn>
    </div>

    <div class="text-caption mb-2">
      <v-icon icon="mdi-information" size="small"></v-icon>
      Click on a project card to see details
    </div>

    <div class="project-grid">
      <div v-for="project in projects" :key="project.id" class="project-card" @click="openProjectDetails(project)">
        <div class="project-image-container">
          <v-icon v-if="!project.mediaUrl" size="64" class="project-icon">mdi-clipboard-check-outline</v-icon>
          <img v-else-if="projectImages[project.id.toString()]" :src="projectImages[project.id.toString()]" alt="Project image" class="project-image" />
          <v-icon v-else size="32" class="loading-icon">mdi-loading mdi-spin</v-icon>
        </div>

        <div class="project-details">
          <h3 class="project-title">{{ project.name }}</h3>

          <v-chip
            :color="getStatusColor(project.status)"
            size="small"
            class="mt-2"
          >
            {{ project.status }}
          </v-chip>
        </div>

        <div class="project-actions">
          <v-btn
            v-if="checkPrivileges(['project_write'], authStore.userInfo?.privileges || [])"
            color="primary"
            icon="mdi-pencil"
            size="small"
            :to="'/projects/' + project.id"
            class="edit-btn"
            @click.stop
          ></v-btn>
          <v-btn
            v-if="checkPrivileges(['project_write'], authStore.userInfo?.privileges || [])"
            color="error"
            icon="mdi-delete"
            size="small"
            @click.stop="deleteDialog = true; deleteProjectId = project.id"
            class="delete-btn"
          ></v-btn>
        </div>
      </div>
    </div>

    <div v-if="projects.length === 0" class="text-center mt-8">
      <p>No projects found</p>
    </div>

    <v-dialog v-model="deleteDialog" max-width="300">
      <v-card>
        <v-card-text>Are you sure you want to delete this project?</v-card-text>
        <v-card-actions>
          <v-btn color="error" @click="deleteProject(deleteProjectId)">Confirm</v-btn>
          <v-btn color="secondary" @click="deleteDialog = false">Cancel</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <project-detail-dialog
      v-model="showProjectDetails"
      :project="selectedProject"
      @project-updated="refreshProjects"
    />
  </div>
</template>

<script setup lang="ts">
import fetcher from "@/exceptionHandler/exceptionHandler";
import { ref, onMounted, onUnmounted } from "vue";
import { Project } from "@/models/Project";
import { useAuthStore } from "@/stores/AuthStore";
import { checkPrivileges } from "@/models/Group";
import ProjectDetailDialog from './ProjectDetailDialog.vue';
import { fetchImageWithAuth, cleanupImageUrls } from '@/models/Image'
import { PRIVILEGES } from "@/config";

interface UserItem {
  id: number;
  firstName: string;
  lastName: string;
  fullName: string;
}

const projects = ref<Array<Project>>([]);
const authStore = useAuthStore();
const deleteDialog = ref(false);
const deleteProjectId = ref<number | null>(null);
const projectImages = ref<Record<string, string>>({});
const showProjectDetails = ref(false);
const selectedProject = ref<Project>(new Project());
const statusFilter = ref<string | null>(null);
const userFilters = ref<number[]>([]);
const availableUsers = ref<UserItem[]>([]);
const statusOptions = ref([
  'PLANNING',
  'IN_PROGRESS',
  'COMPLETED',
  'ON_HOLD',
  'CANCELLED'
]);

onUnmounted(() => {
  cleanupImageUrls(projectImages.value);
});

function openProjectDetails(project: Project): void {
  selectedProject.value = project;
  showProjectDetails.value = true;
}

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

async function loadUsers() {
  try {
    const response = await fetcher.get(
      "/projects/contributors",
      {headers: authStore.authHeader()}
    );
    availableUsers.value = response.data.map((user: any) => ({
      id: user.id,
      firstName: user.firstName,
      lastName: user.lastName,
      fullName: user.firstName + " " + user.lastName
    }));
  } catch (error) {
    
  }
}

async function loadProjectImage(project: Project) {
  if (!project.mediaUrl) {return;}

  const imageUrl = await fetchImageWithAuth(project.mediaUrl, authStore.authHeader() as HeadersInit);
  if (imageUrl) {
    projectImages.value[project.id.toString()] = imageUrl;
  }
}

async function applyFilters() {
  try {
    let url = `/projects/filter`;
    let params: string[] = [];
    if (statusFilter.value) {
      params.push(`status=${statusFilter.value}`);
    }
    if (userFilters.value && userFilters.value.length > 0) {
      userFilters.value.forEach(userId => {
        params.push(`userId=${userId}`);
      });
    }
    let urlParams = '';
    if (params.length > 0) {
      urlParams = '?' + params.join('&');
    }
    const response = await fetcher.get(url + urlParams, {
      headers: authStore.authHeader()
    });
    if (response && response.data) {
      projects.value = response.data;
      for (const key in projectImages.value) {
        const url = projectImages.value[key];
        if (typeof url === 'string') {
          URL.revokeObjectURL(url);
        }
      }
      projectImages.value = {};
      projects.value.forEach(project => {
        if (project.mediaUrl) {
          loadProjectImage(project);
        }
      });
    } else {
      projects.value = [];
    }
  } catch (error) {
    
  }
}

function resetFilters() {
  statusFilter.value = null;
  userFilters.value = [];
  loadProjects();
}

async function loadProjects() {
  try {
    const response = await fetcher.get(
      "/projects",
      {headers: authStore.authHeader()}
    );

    if(response && response.data) {
      projects.value = response.data;

      projects.value.forEach(project => {
        if (project.mediaUrl) {
          loadProjectImage(project);
        }
      });
    } else {
      projects.value = [];
    }
  } catch (error) {
    projects.value = [];
  }
}

async function deleteProject(id: number | null) {
  if (!id) {return;}
  try {
    await fetcher.delete(`/projects/${id}`, {
      headers: authStore.authHeader()
    });
    deleteDialog.value = false;
    loadProjects();
  }
  catch (error) {
    
  }
}

function refreshProjects() {
  loadProjects();
}

onMounted(function() {
  loadProjects();
  loadUsers();
  refreshProjects();
});

</script>

<style scoped>
.project-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.project-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  height: 100%;
  position: relative;
  background-color: rgb(var(--v-theme-surface));
  padding: 20px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.project-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.project-image-container {
  height: 150px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 10px;
}

.project-icon {
  opacity: 0.6;
  color: rgb(var(--v-theme-secondary));
}

.loading-icon {
  color: rgb(var(--v-theme-primary));
}

.project-image {
  max-width: 100%;
  max-height: 150px;
  object-fit: contain;
}

.project-details {
  text-align: center;
}

.project-title {
  font-size: 1.2rem;
  font-weight: bold;
  margin-bottom: 5px;
}

.edit-btn {
  position: absolute;
  bottom: 15px;
  right: 75px;
}

.delete-btn {
  position: absolute;
  bottom: 15px;
  right: 15px;
}
</style>
