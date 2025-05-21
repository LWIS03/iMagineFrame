<template>
  <v-sheet
    class="bg-star text-center">
    <v-img
      class="w-33 d-flex mx-auto"
      src="/src/assets/logos/imaginelab_logo.png"
      cover/>
    <p class="text-white font-trebuchet pb-3 align-center">Technology Club van de Faculteit Toegepaste Ingenieurswetenschappen - UAntwerpen</p>
  </v-sheet>
  
  <!-- First row -->
  <v-row class="mx-2 mt-2 d-flex align-center" no-gutters>
    <v-col cols="6">
      <v-card variant="text" min-height="400">
        <v-card-title class="text-center text-h4">
          <v-icon :icon="about_us_items[0].icon"/>
          {{ about_us_items[0].topic }}
        </v-card-title>
        <v-card-text class="text-body-1 text-center mx-7">
          {{ about_us_items[0].text }}
        </v-card-text>
      </v-card>
    </v-col>
    <v-col cols="6">
      <v-card>
        <v-card-title class="bg-black text-center">
          <h2>Our upcoming events!</h2>
        </v-card-title>
        <v-card-text
          class="mt-3">
          <v-carousel
            v-model="event_carousel"
            class="h-100"
            hide-delimiters
            show-arrows="hover">
            <v-carousel-item
              v-for="event in events"
              :key="`card-${event.name}`">
              <eventDisplay :event="event"/>
            </v-carousel-item>
          </v-carousel>
        </v-card-text>
      </v-card>
    </v-col>  
  </v-row>

  <v-sheet class="my-2 bg-grey-darken-3 w-100 d-flex align-center justify-center font-trebuchet" height="100">
    <div>
      <p class=" text-size-xx-large">
        "Imagination is more important than knowledge"
      </p>
      <p class="text-overline">
        - Albert Einstein
      </p>
    </div>
    
  </v-sheet>

  <!-- Second row -->
  <v-row class="mx-2 mt-2 d-flex align-center" no-gutters>
    <v-col cols="6">
      <v-card>
        <v-card-title class="bg-black text-center">
          <h2>Our projects!</h2>
        </v-card-title>
        <v-card-text
          class="mt-3">
          <v-carousel
            v-model="project_carousel"
            class="h-100"
            hide-delimiters
            show-arrows="hover">
            <v-carousel-item
              v-for="project in projects"
              :key="`card-${project.name}`">
              <projectDisplay :project="project"/>
            </v-carousel-item>
          </v-carousel>
        </v-card-text>
      </v-card>
    </v-col>
    <v-col cols="6">
      <v-card variant="text" min-height="400">
        <v-card-title class="text-center text-h4">
          <v-icon :icon="about_us_items[1].icon"/>
          {{ about_us_items[1].topic }}
        </v-card-title>
        <v-card-text class="text-body-1 text-center mx-7">
          {{ about_us_items[1].text }}
        </v-card-text>
      </v-card>
    </v-col>  
  </v-row>

  <v-sheet class="my-2 bg-grey-darken-3 w-100 d-flex align-center justify-center font-trebuchet" height="100">
    <div>
      <p class=" text-size-xx-large">
        "Imagination is the beginning of creation"
      </p>
      <p class="text-overline">
        - George Bernard Shaw
      </p>
    </div>
  </v-sheet>

  <!-- Third row -->
  <v-row class="mx-2 mt-2 d-flex align-center" no-gutters>
    <v-col cols="6">
      <v-card variant="text" min-height="400">
        <v-card-title class="text-center text-h4">
          <v-icon :icon="about_us_items[2].icon"/>
          {{ about_us_items[2].topic }}
        </v-card-title>
        <v-card-text class="text-body-1 text-center mx-7">
          {{ about_us_items[2].text }}
        </v-card-text>
      </v-card>
    </v-col>
    <v-col cols="6">
      <v-card>
        <v-card-title class="bg-black text-center">
          <h2>Join our amazing group</h2>
        </v-card-title>
        <v-card-text
          class="mt-3">
          <v-carousel
            height="400"
            show-arrows="hover"
            cycle
            hide-delimiters>
            <v-carousel-item
              v-for="link in image_links"
              v-bind:key="link"
            >
              <v-img :src="link" />
            </v-carousel-item>
          </v-carousel>
        </v-card-text>
      </v-card>
    </v-col>  
  </v-row>

  <!-- Registration section -->
  <v-sheet class="d-flex align-center justify-center bg-grey-darken-3 w-100 text-size-xx-large font-trebuchet" height="150">
    <p class="mx-5">
      Convinced of becoming an iMagineer?
    </p>
    <v-btn color="white" size="x-large" variant="outlined" width="250" :to="'/register'">
      Register here
    </v-btn>
  </v-sheet>

</template>

<script lang="ts" setup>

import { getPublicEvents, Event } from "@/models/Event";
import { getPublicProjects, getProjects, Project } from "@/models/Project";
import { useAuthStore } from "@/stores/AuthStore";
import {Ref, ref} from "vue"
import eventDisplay from "@/views/welcome/WelcomeEventDisplay.vue"
import projectDisplay from "@/views/welcome/WelcomeProjectDisplay.vue"


const authStore = useAuthStore();
const events : Ref<Event[]> = ref<Event[]>([]);
const projects: Ref<Project[]> = ref<Project[]>([])
const event_carousel = ref<number>(0)
const project_carousel = ref<number>(0)


async function loadPublicEvents(){
  try {
    console.log("Trying to get Events")
    events.value = await getPublicEvents();
  } catch (error) {
    events.value = []
  }
  console.log("Events", events.value)
}
loadPublicEvents()


async function loadPublicProjects(){
  try {
    console.log("Trying to get Projects")
    projects.value = await getPublicProjects();
  } catch (error) {
    projects.value = []
  }
  console.log("Projects", projects.value)
}
loadPublicProjects()

const about_us_items = [
  {
    topic:"Community",
    icon:"mdi-account-group",
    text: "iMagineLab is a non-profit community club where students of the Faculty of Applied Engineering can collaborate on various projects with committed friend and fellow students in addition to their eduction. With this initiative, we want to bring students of different engineering disciplines and years together. Students are able to network with other members who are also passionated about technology. Developing these network skills are the key in building your further professional career.\nBesides the regular project nights, we are constantly expanding and improving the community spirit. For this, we organise various special events which members can participate, such as building challenges, competitions, Xperience On Demand and hackathons."
  },
  {
    topic:"Let your imagination run wild",
    icon:"mdi-brush",
    text:"In the community, students get the opportunity to work out their own ideas. Furthermore, you can find team members who want to participate with your project or get support from other community members. The iMagineLab community is strongly convinced that working in team is twice as fun!\nNo project to work on? No problem! iMagineLab is the place to be to find inspiration. We are happy to assist you with finding the right project in your domain of interest. Also, there are ongoing community projects that are always free to join."
  },
  {
    topic:"Learn by doing",
    icon:"mdi-tools",
    text:"iMagineLab is founded by a group of students from the Faculty of Applied Engineering at the University of Antwerp. With this initiative, we want to encourage students to create projects and experiment with technology. In doing so, students can apply their knowledge and experience in their own projects. The community is located on campus Groenen­borger. In addition, we are able to use the available infra­structure and tools on the campus.\nAs an engineer in training, you are prepared for lifelong learning and so, we try to stimulate this ability. In our projects, we work with state-of-the-art technology. Additionally, we offer our members the opportunity to learn and work on a self-chosen topic or skill on our 'Xperience On Demand' evenings."
  }
]

const image_links = [
  "src\\assets\\stock\\UA - iMagineLab-1.jpg",
  "src\\assets\\stock\\UA - iMagineLab-5.jpg",
  "src\\assets\\stock\\UA - iMagineLab-9.jpg",
  "src\\assets\\stock\\UA - iMagineLab-4.jpg",
  "src\\assets\\stock\\UA - iMagineLab-8.jpg",
  "src\\assets\\stock\\UA - iMagineLab-10.jpg",
  "src\\assets\\stock\\UA - iMagineLab-12.jpg",
]
</script>

<style scoped>
.bg-star {
  background: url('/src/assets/bakgrounds/bg_black_stars.jpg');
  background-size: cover;
}

.font-trebuchet {
  font-family: 'Trebuchet MS', 'Lucida Sans Unicode', 'Lucida Grande', 'Lucida Sans', Arial, sans-serif;
  font-size: 2ch
}

.v-calendar-month__day {
  height: 2px ;
}

.text-size-xx-large {
  font-size: xx-large;
}
</style>

<style>
.v-calendar-month__day {
  height: 100px !important;
  min-height: 40px !important;
  max-height: 100px !important;
}
</style>