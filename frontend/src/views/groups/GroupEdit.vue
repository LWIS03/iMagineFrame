<template>
  <v-alert
    v-model="showSuccess"
    border="start"
    variant="tonal"
    closable
    close-label="Close Alert"
    color="success"
    title="Saved successfully"
  ></v-alert>
  <v-alert
    v-model="showError"
    border="start"
    variant="tonal"
    closable
    close-label="Close Alert"
    color="error"
    :title="errorMessage"
  ></v-alert>

  
  <div class="pa-5">
    <v-row no-gutters>
      <v-col cols="8">
        <h2 v-if="action === 'edit'">Edit Group: {{ groupname }}</h2>
        <h2 v-if="action === 'new'">Create Group</h2>
        <v-row no-gutters>
          <v-col
            cols="11"
          >
            <v-text-field
              v-model="group.name"
              :counter="30"
              :error-messages="fieldErrors.get('name')"
              label="Group name"
              required
            ></v-text-field>
          </v-col>
        </v-row>

        <v-row no-gutters>
          <v-col
            cols="11"
          >
            <v-select
              clearable
              label="Privileges"
              :items="privileges"
              v-model="group.privileges"
              item-title="name"
              multiple
              return-object
            >
            </v-select>
          </v-col>
        </v-row>

        <v-row no-gutters>
          <v-col
            cols="11"
          >
            <v-select
              clearable
              label="Users"
              :items="users"
              v-model="group.users"
              item-title="username"
              multiple
              return-object
            ></v-select>
          </v-col>
        </v-row>

        <v-row>
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
              to="/groups"
            >
              Back
            </v-btn>
          </v-col>
        </v-row>
      </v-col>
      <v-col cols="4">
        <v-expansion-panels>
          <v-expansion-panel>
            <v-expansion-panel-title>
              <b>Privilege Descriptions</b>
            </v-expansion-panel-title>
            <v-expansion-panel-text>
              <p v-for="privilege in privileges" :key="privilege.name">
                <b>{{ privilege.name }}</b> : {{ privilege.description }}
              </p>
            </v-expansion-panel-text>
          </v-expansion-panel>
        </v-expansion-panels>
      </v-col>
      
    </v-row>
    

  </div>
</template>

<script lang="ts" setup>
import {useRoute, useRouter} from "vue-router";
import {ref} from "vue";
import {Group, Privilege} from "@/models/Group";
import {useAuthStore} from "@/stores/AuthStore";
import fetcher from "@/exceptionHandler/exceptionHandler";
import { User } from "@/models/User";

const route = useRoute();
const router = useRouter();

const group = ref<Group>(new Group(0, "", [], []));

const groups = ref<object[]>([]);
const privileges = ref<Privilege[]>([]);
const users = ref<User[]>([]);
const groupname = ref<string>("");
const showPrivileges = ref<boolean>(false)

const showSuccess = ref<boolean>(false);
const showError = ref<boolean>(false);
const errorMessage = ref<string>("");
const fieldErrors = ref<Map<string, string>>(new Map());
const action = ref("edit")
const authStore = useAuthStore();

async function init() {
  if (route.params.id === "new") {
    action.value = "new";
    return;
  }
  await fetcher.get("/groups/" + route.params.id, {headers: authStore.authHeader()})
  .then((response) => {
    group.value = response.data;
    groupname.value = group.value.name;
  })
}
init(); // call init() on component load

async function initGroupOptions() {
  await fetcher.get("/groups", {headers: authStore.authHeader()})
    .then((response) => {
      groups.value = response.data;
    })
}
initGroupOptions(); // call initGroupOptions() on component load)


async function initPrivilegeOptions() {
  await fetcher.get("/privileges", {headers: authStore.authHeader()})
    .then((response) => {
      privileges.value = response.data;
    })
  .catch((err) => {})
}
initPrivilegeOptions(); // call initGroupOptions() on component load)


async function initUserOptions() {
  await fetcher.get("/users", {headers: authStore.authHeader()})
    .then((response) => {
      users.value = response.data;
    })
  .catch((err) => {})
}
initUserOptions(); // call initGroupOptions() on component load)



async function save() {
  if (action.value === "new") {
    await fetcher.put("/groups/new", group.value, {headers: authStore.authHeader()})
      .then(async () => {
        showSuccess.value = true;
        await new Promise(resolve => setTimeout(resolve, 1500));
        router.push("/groups")
      })
      .catch((error) => {
        showError.value = true;
        if(error.response.data) {
          errorMessage.value = error.response.data;
        } else if (typeof error.response.data === "object") {
          errorMessage.value = "Something went wrong.";
          for(let key in error.response.data) {
            fieldErrors.value.set(key as string, error.response.data[key])
          }
        }
      })
  } else {
    await fetcher.post("/groups/" + route.params.id, group.value, {headers: authStore.authHeader()})
      .then(async () => {
        showSuccess.value = true;
        await new Promise(resolve => setTimeout(resolve, 1500));
        router.push("/groups")
      })
      .catch((error) => {
        showError.value = true;
        if(error.response.data) {
          errorMessage.value = error.response.data;
        } else if (typeof error.response.data === "object") {
          errorMessage.value = "Something went wrong.";
          for(let key in error.response.data) {
            fieldErrors.value.set(key as string, error.response.data[key])
          }
        }
      })
  }
}


</script>

<style scoped>

</style>
