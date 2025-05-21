<template>
  <v-carousel
    v-model="carousel"
    class="h-100"
    hide-delimiters
    show-arrows="hover"
  >
    <v-carousel-item
      v-for="event in props.events"
      :key="`card-${event.name}`">
      <v-card :image="event.imageUrl? event.imageUrl as string: 'src/assets/logos/imaginelab_logo.png'">
        <v-img :src="getImageUrl(event.imageUrl)" />
        <v-card-title class="d-flex justify-center align-center text-white text-h5 w-100 opaque-black-bg">
          {{ event.name }}
        </v-card-title>
        <div class="spacer"></div>
        <v-card-text class="opaque-black-bg"
          height="55">
          <v-row class="text-white">
            <v-col cols="5" class="ml-4">
              <v-icon icon="mdi-domain"/> {{ event.location }}
            </v-col>
            <v-spacer/>
            <v-col cols="6">
              <v-icon icon="mdi-calendar"/> {{ event.startdate?.split('T')[0].split('-').slice(1,3).reverse().join("/") }}
                            &emsp;
              <v-icon icon="mdi-clock-time-two"/> {{ event.startdate?.split('T')[1].split(':').slice(0,2).join("h") }}
            </v-col>
          </v-row>
          <v-row>
            <v-btn 
              @click="switchReveal()" 
              block>
              More info
            </v-btn>
          </v-row>
        </v-card-text>
                
        
            <v-fade-transition>
              <v-card
                v-if="reveal"
                class="position-absolute h-100 w-100 d-flex flex-column"
                color="primary"
                style="bottom: 0;">
                <v-card-title class="text-h5">
                  {{ event.name}}
                </v-card-title>
                <v-card-subtitle>
                  {{event.label}}
                </v-card-subtitle>
                <v-card-text 
                  class="pb-5 text-white">
                  {{event.description}}    
                </v-card-text>
                        
                <v-card-actions>
                  <v-row>
                    <v-col class="ml-3">
                      <v-btn
                        variant="outlined" 
                        @click="switchReveal()" 
                        block>
                        Back
                      </v-btn>
                    </v-col>
                    <v-col>
                      <v-btn 
                        variant="tonal"
                        @click="goToEventPage()"
                        color="success"
                        block>
                        Go to event page
                      </v-btn>
                    </v-col>
                    <v-col class="mr-3">
                      <v-btn @click="cancelEvent(event.id.toString())"/>
                    </v-col>           
                  </v-row>
                </v-card-actions>
              </v-card>
            </v-fade-transition>
          </v-card>
    </v-carousel-item>
  </v-carousel>
</template>
<script lang="ts" setup>
import { defineProps, ref } from 'vue';
import { Event } from '@/models/Event';
import router from '@/router';

const props = defineProps<{events: Event[]}>()
const carousel=ref(0)
const reveal = ref(false)

function getImageUrl(url: string | ArrayBuffer | null): string {
    if (url) {
        return url as string;
    } else {
        return "src/assets/logos/imaginelab_logo.png"
    }
}

function switchReveal(){
    reveal.value = reveal.value? false: true
}

function goToEventPage(){
    router.push("/events")
}

function cancelEvent(eventId:string) {
    console.log("Placeholder for cancelling the participation of an event: " + eventId)
}

</script>

<style scoped>
  .spacer {
    height:130px;
  }

  .opaque-black-bg{
    background-color: rgba(0,0,0,0.2)
  }
  </style>