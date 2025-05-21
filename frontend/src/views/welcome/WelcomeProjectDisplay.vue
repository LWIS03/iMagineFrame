<template>
  <v-card  :image="imageUrl" variant="text" class="h-100">
    <v-card-title class="d-flex justify-center align-center text-white text-h5 w-100 opaque-black-bg">
      {{ project.name }}
    </v-card-title>
    <div class="spacer"></div>
    <v-card-text class="opaque-black-bg"
      height="55">
      <v-row class="text-white">
        <v-col cols="5" class="ml-4">
          <v-icon icon="mdi-account"/> {{ project.owner.username }}
        </v-col>
        <v-spacer/>
        <v-col>
          <v-icon icon="mdi-progress-helper"/> {{ project.status }}
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
          <div class="d-flex justify-end align-center w-100">
            <v-btn
              variant="tonal"
              color="primary"
              @click="reveal = reveal? false: true">
              CLOSE
            </v-btn>
          </div>
        </v-card-actions>
      </v-card>

      <v-snackbar v-model="snackbar" :color="snackbarColor" timeout="3000">
        {{ snackbarText }}
      </v-snackbar>
    </v-fade-transition>
  </v-card>
</template>

<script lang="ts" setup>
import { ref, computed, watch } from 'vue';
import { useAuthStore } from '@/stores/AuthStore';
import { Project, getPublicProjects, getStatusColor } from '@/models/Project';
import { checkPrivileges } from "@/models/Group";
import { ProjectJoinRequestAddDto } from '@/models/ProjectJoinRequest';
import { User } from '@/models/User';
import fetcher from "@/exceptionHandler/exceptionHandler";

const props = defineProps<{
  project: Project;
}>();

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

const reveal = ref<boolean>(false);
const imageUrl = ref<string>('');



async function loadImage(): Promise<void> {
  if (!props.project.mediaUrl || typeof props.project.mediaUrl !== 'string') {
    imageUrl.value = 'src/assets/logos/imaginelab_logo.png';
    return
  }
  try {
    const response = await fetch(props.project.mediaUrl, {headers: authStore.authHeader() as HeadersInit});
    if (response.ok) {
      const blob = await response.blob();
      imageUrl.value = URL.createObjectURL(blob);
    }
  } catch (error) {
    imageUrl.value = 'src/assets/logos/imaginelab_logo.png';
    console.error('error loading image:', error);
  }
}
loadImage()

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
</script>

<style scoped>
.v-card-text {
  max-height: 300px;
  overflow-y: auto;
}

.spacer {
  height:160px;
}

.opaque-black-bg{
  background-color: rgba(0,0,0,0.2)
}
</style>