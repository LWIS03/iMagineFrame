<template>
  <div class="mx-auto text-center pt-2">
    <h1>Dashboard</h1>
  </div>

  <v-data-iterator
    class="mx-10 mt-5"
    :items="filtered_navigation_links"
    item-value="name"
    :items-per-page="12"
    >
        <template v-slot:default="{items}">
            <v-row>
                <v-col
                v-for="item in items"
                :key = "item.raw.name"
                cols="12" md="4">
                <v-btn
                    height="100"
                    :prepend-icon="item.raw.icon"
                    :to="item.raw.location"
                    class="d-flex px-auto w-100">
                        <h3>{{item.raw.name}}</h3>
                    </v-btn>
                </v-col>
            </v-row>
        </template>
    </v-data-iterator>
</template>

<script setup lang="ts">
import {config, PRIVILEGES} from "@/config";
import { computed } from "vue";
import { checkPrivileges } from "@/models/Group";
import { useAuthStore } from "@/stores/AuthStore";

const authStore = useAuthStore();
const navigation_links = config["DASHBOARD_TILES"]

const filtered_navigation_links = computed(() => {
        return navigation_links.filter((item) => {
            return hasRequiredPrivileges(PRIVILEGES[item.location])
        })
    }
) 
console.log(filtered_navigation_links.value)
function hasRequiredPrivileges(requiredPrivileges: string[] | undefined): boolean {
  return checkPrivileges(requiredPrivileges, authStore.userInfo?.privileges || []);
}



</script>