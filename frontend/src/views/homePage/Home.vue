<template>
  <div class="pa-5 text-center position-static">
    <h1>Welcome {{loggedInUser?.firstName}} {{loggedInUser?.lastName}}</h1>
  </div>

  <!-- For screen sizes bigger or equal large -->
  <v-container class="hidden-md-and-down" height="700">
    <v-row>
      <v-col class="d-flex justify-center">
        <v-sheet class="w-100" height="300">
          <v-card elevation="5">
            <v-card-title class="bg-black text-center">
              <b>Events</b>
            </v-card-title>
            <v-carousel
              v-model="event_carousel"
              class="h-100 w-100"
              hide-delimiters
              show-arrows="hover">
              <v-carousel-item
                v-for="event in events"
                :key="`card-${event.name}`">
                <eventDisplay :event="event"/>
              </v-carousel-item>
            </v-carousel>
          </v-card>
        </v-sheet>
      </v-col>
      <v-col xxl="2" lg="3" md="4" xl="2" >
        <userDetailsViewThin :displayed-user="loggedInUser" />
      </v-col>
    </v-row>

    <!-- Second row -->
    <v-row>
      <v-col class="d-flex align-center justify-center">
        <v-sheet class="w-100" height="300">
          <v-card elevation="5">
            <v-card-title class="bg-black text-center">
              <b>Projects</b>
            </v-card-title>
            <v-carousel
              v-model="project_carousel"
              class="h-100 w-100"
              hide-delimiters
              show-arrows="hover">
              <v-carousel-item
                v-for="project in projects"
                :key="`card-${project.name}`">
                <projectDisplay :project="project"/>
              </v-carousel-item>
            </v-carousel>
          </v-card>
        </v-sheet>
      </v-col>
      <v-col xxl="2" xl="2" lg="3" class="d-flex justify-center">
        <div>
          <calendar :events="events"/>
        </div>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts" setup>
import { User, getUser } from '@/models/User';
import { useAuthStore } from '@/stores/AuthStore';
import { ref, Ref, computed, ComputedRef } from 'vue';
import userDetailsViewWide from '@/vueComponents/userDetailsWide.vue';
import userDetailsViewThin from '@/views/homePage/userDetailsThin.vue'
import calendar from "@/vueComponents/calender.vue"
import eventDisplay from "@/views/homePage/HomeEventDisplay.vue"
import projectDisplay from "@/views/homePage/HomeProjectDisplay.vue"
import { getEvents, Event } from '@/models/Event';
import { Project, getProjects} from "@/models/Project";

const authStore = useAuthStore()
const userId: ComputedRef<string> = computed(() => {return authStore.userInfo? authStore.userInfo.id.toString() : ""})
const loggedInUser: Ref<User> = ref<User>({} as User);
const events: Ref<Event[]> = ref<Event[]>([])
const projects: Ref<Project[]> = ref<Project[]>([]);
const event_carousel=ref(0)
const project_carousel=ref(0)


async function initData(){
  // Get logged in user
  loggedInUser.value = await getUser(userId.value)
  events.value = await getEvents(authStore.authHeader());
  projects.value = await getProjects(authStore.authHeader());
}
initData()

</script>
