<template>
  <div class="pa-5">
    <h2>Project Join Requests</h2>

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
          <th class="text-left">
            <v-checkbox
              v-model="selectAll"
              @change="toggleSelectAll"
              hide-details
              label="Select All"
            ></v-checkbox>
          </th>
          <th class="text-left">User</th>
          <th class="text-left">Project</th>
          <th class="text-left">Date Requested</th>
          <th class="text-left">Status</th>
          <th class="text-left">Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="request in requests" :key="request.id">
          <td>
            <v-checkbox
              v-model="selectedRequests"
              :value="request.id"
              hide-details
            ></v-checkbox>
          </td>
          <td>{{ request.username }}</td>
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
            <div v-if="request.accepted === null" class="d-flex">
              <v-btn
                color="success"
                size="small"
                class="mr-2"
                @click="acceptRequest(request.id)"
                :loading="processingId === request.id && action === 'accept'"
              >
                Accept
              </v-btn>
              <v-btn
                color="error"
                size="small"
                @click="declineRequest(request.id)"
                :loading="processingId === request.id && action === 'decline'"
              >
                Decline
              </v-btn>
            </div>
          </td>
        </tr>
      </tbody>
    </v-table>

    <div v-else class="text-center my-5">
      <p>There are no pending project join requests.</p>
    </div>

    <div class="d-flex mt-4">
      <v-btn
        color="error"
        @click="clearSelectedRequests"
        class="mr-2"
        :disabled="selectedRequests.length === 0"
      >
        CLEAR SELECTED REQUESTS
      </v-btn>
      <v-btn color="secondary" to="/projects">
        BACK TO PROJECTS
      </v-btn>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ProjectJoinRequest, getAllRequests } from '@/models/ProjectJoinRequest';
import { useAuthStore } from '@/stores/AuthStore';
import fetcher from '@/exceptionHandler/exceptionHandler';

const authStore = useAuthStore();
const requests = ref<ProjectJoinRequest[]>([]);
const loading = ref(true);
const showSuccess = ref(false);
const successMessage = ref('');
const showError = ref(false);
const errorMessage = ref('');
const processingId = ref<number | null>(null);
const action = ref('');
const selectedRequests = ref<number[]>([]);
const selectAll = ref(false);

function toggleSelectAll() {
  if (selectAll.value) {
    selectedRequests.value = requests.value.map(request => request.id);
  } else {
    selectedRequests.value = [];
  }
}

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
    requests.value = await getAllRequests();
    selectedRequests.value = [];
    selectAll.value = false;
  } catch (error: any) {
    showError.value = true;
    errorMessage.value = 'Failed to load requests';
    console.error(error);
  } finally {
    loading.value = false;
  }
}

async function acceptRequest(id: number) {
  try {
    processingId.value = id;
    action.value = 'accept';

    await fetcher.post(`/projects/requests/${id}/accept`, {}, {
      headers: authStore.authHeader()
    });

    showSuccess.value = true;
    successMessage.value = 'Request accepted successfully';

    const request = requests.value.find(r => r.id === id);
    if (request) {
      request.accepted = true;
    }
  } catch (error: any) {
    showError.value = true;
    errorMessage.value = error.response?.data || 'Failed to accept request';
  } finally {
    processingId.value = null;
    action.value = '';
  }
}

async function declineRequest(id: number) {
  try {
    processingId.value = id;
    action.value = 'decline';

    await fetcher.post(`/projects/requests/${id}/decline`, {}, {
      headers: authStore.authHeader()
    });

    showSuccess.value = true;
    successMessage.value = 'Request declined successfully';

    const request = requests.value.find(r => r.id === id);
    if (request) {
      request.accepted = false;
    }
  } catch (error: any) {
    showError.value = true;
    errorMessage.value = error.response?.data || 'Failed to decline request';
  } finally {
    processingId.value = null;
    action.value = '';
  }
}

async function clearSelectedRequests() {
  if (selectedRequests.value.length === 0) {return;}

  try {
    loading.value = true;

    const deletePromises = selectedRequests.value.map(id =>
      fetcher.delete(`/projects/requests/admin/${id}`, {
        headers: authStore.authHeader()
      })
    );

    await Promise.all(deletePromises);

    showSuccess.value = true;
    successMessage.value = `Successfully cleared ${selectedRequests.value.length} request(s)`;

    await loadRequests();
  } catch (error: any) {
    showError.value = true;
    errorMessage.value = error.response?.data || 'Failed to clear selected requests';
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadRequests();
});
</script>
