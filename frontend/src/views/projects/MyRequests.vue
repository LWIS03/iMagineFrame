<template>
  <div class="pa-5">
    <h2>My Project Join Requests</h2>

    <v-alert
      v-model="showSuccess"
      border="start"
      variant="tonal"
      closable
      color="success"
      :title="successMessage"
    ></v-alert>

    <v-alert
      v-model="showError"
      border="start"
      variant="tonal"
      closable
      color="error"
      :title="errorMessage"
    ></v-alert>

    <div v-if="loading" class="d-flex justify-center my-5">
      <v-progress-circular indeterminate color="primary"></v-progress-circular>
    </div>

    <v-table v-else-if="requests.length > 0">
      <thead>
        <tr>
          <th class="text-left">Project</th>
          <th class="text-left">Date Requested</th>
          <th class="text-left">Status</th>
          <th class="text-left">Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="request in requests" :key="request.id">
          <td>{{ request.projectName }}</td>
          <td>{{ formatDate(request.dateCreated) }}</td>
          <td>
            <v-chip
              :color="getStatusColor(request.accepted)"
              size="small"
            >
              {{ getStatusText(request.accepted) }}
            </v-chip>
          </td>
          <td>
            <v-btn
              v-if="request.accepted === null"
              color="error"
              size="small"
              @click="revokeRequest(request.id)"
              :loading="deletingId === request.id"
            >
              Revoke
            </v-btn>
          </td>
        </tr>
      </tbody>
    </v-table>

    <div v-else class="text-center my-5">
      <p>You don't have any project join requests.</p>
    </div>

    <v-btn class="mt-4" color="secondary" to="/projects">
      Back to Projects
    </v-btn>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ProjectJoinRequest, getUserRequests } from '@/models/ProjectJoinRequest';
import { useAuthStore } from '@/stores/AuthStore';
import fetcher from '@/exceptionHandler/exceptionHandler';

const authStore = useAuthStore();
const requests = ref<ProjectJoinRequest[]>([]);
const loading = ref(true);
const showSuccess = ref(false);
const successMessage = ref('');
const showError = ref(false);
const errorMessage = ref('');
const deletingId = ref<number | null>(null);

function formatDate(dateString: string): string {
  if (!dateString) {return '';}
  const date = new Date(dateString);
  return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
}

function getStatusColor(status: boolean | null): string {
  if (status === null) {return 'blue';}
  return status ? 'green' : 'red';
}

function getStatusText(status: boolean | null): string {
  if (status === null) {return 'Pending';}
  return status ? 'Approved' : 'Rejected';
}

async function loadRequests() {
  try {
    loading.value = true;
    requests.value = await getUserRequests();
  } catch (error: any) {
    showError.value = true;
    errorMessage.value = 'Failed to load requests';
    console.error(error);
  } finally {
    loading.value = false;
  }
}

async function revokeRequest(id: number) {
  try {
    deletingId.value = id;
    await fetcher.delete(`/projects/requests/${id}`, {
      headers: authStore.authHeader()
    });

    showSuccess.value = true;
    successMessage.value = 'Request revoked successfully';

    requests.value = requests.value.filter(r => r.id !== id);
  } catch (error: any) {
    showError.value = true;
    errorMessage.value = error.response?.data || 'Failed to revoke request';
  } finally {
    deletingId.value = null;
  }
}

onMounted(() => {
  loadRequests();
});
</script>
