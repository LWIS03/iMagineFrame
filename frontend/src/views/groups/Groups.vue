<template>
  <div class="pa-5">
    <div class="d-flex justify-space-between">
      <h2>Groups</h2>
      <v-btn
        color="primary"
        :to="'/groups/new'"
      > Add new group
      </v-btn>
    </div>

    <p>Here is a list of all groups:</p>
    <v-table>
      <thead>
        <tr>
          <th class="text-left">
            Id
          </th>
          <th class="text-left">
            Name
          </th>
          <th class="text-left">
            Privileges
          </th>
          <th class="text-left">
            Users
          </th>
          <th class="text-left">
            Actions
          </th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="group in groups"
          :key="group.id"
        >
          <td>{{ group.id }}</td>
          <td>{{ group.name }}</td>
          <td>
            <v-data-iterator
              :items="group.privileges"
              item-value="name"
              items-per-page="100"
            >
              <template v-slot:default="{items}">
                <v-chip v-for="item in items"  :key="item.raw.id" class="ma-1" color="primary">
                  <b>{{ capitalizeFirstLetter(item.raw.name.split("_").join(" "))}}&ensp;:</b>&ensp;{{ item.raw.description }}
                </v-chip>
              </template>     
            </v-data-iterator>
          </td>
          <td>
            <v-btn
              class="ml-1 ml-xs-0"
              color="primary"
              icon="mdi-view-list"
              size="x-small"
              @click="showUsersDialog = true; users = group.users || []"
            >
            </v-btn>
          </td>
          <td>
            <v-row>
              <v-btn
                color="primary"
                icon="mdi-pencil"
                size="x-small"
                :to="'/groups/' + group.id"
              >
              </v-btn>
              <v-btn
                class="ml-1 ml-xs-0"
                color="error"
                icon="mdi-delete"
                size="x-small"
                @click="deleteDialog = true; deleteGroupId = group.id"
              >
              </v-btn>
            </v-row>
          </td>
        </tr>
      </tbody>
    </v-table>

    <v-dialog
      v-model="deleteDialog"
      max-width="300"
    >
      <v-card>
        <v-card-text>
          Are you sure you want to delete this group?
        </v-card-text>
        <v-card-actions>
          <v-btn color="error" @click="deleteGroup(deleteGroupId)">Confirm</v-btn>
          <v-btn color="secondary" @click="deleteDialog = false">Cancel</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog
      v-model="showUsersDialog"
      max-width="300"
    >
      <v-card>
        <v-card-actions>
          <v-btn @click="showUsersDialog = false">Close dialog</v-btn>
        </v-card-actions>
        <v-card-text>
          <v-table>
            <thead>
              <tr>
                <th class="text-left">
                  Id
                </th>
                <th class="text-left">
                  Name
                </th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="user in users.sort(compare)"
                :key="user.id"
              >
                <td>{{ user.id }}</td>
                <td>{{ user.firstName }} {{ user.lastName}}</td>
              </tr>
            </tbody>
          </v-table>
        </v-card-text>
      </v-card>
    </v-dialog>
  </div>
</template>

<script lang="ts" setup>
import {ref} from "vue";
import {Group} from "@/models/Group";
import {useAuthStore} from "@/stores/AuthStore";
import { User } from "@/models/User";
import fetcher from "@/exceptionHandler/exceptionHandler";

const users = ref<Array<User>>([])
const groups = ref<Array<Group>>([]);
const deleteDialog = ref<boolean>(false);
const showUsersDialog = ref<boolean>(false);
const deleteGroupId = ref<number>();
const authStore = useAuthStore();

function compare(a: any, b: any) {
  if (a.id < b.id)
    {return -1;}
  if (a.id > b.id)
    {return 1;}
  return 0;
}

async function initData() {
  const res = await fetcher.get("/groups", {headers: authStore.authHeader()});
  if(res && res.data) {
    groups.value = res.data;
  } else {
    groups.value = [];
    console.log("No data returned from API: " + res.status)
  }
}
initData();

function deleteGroup(id: number | undefined) {
  if(!id) {
    return;
  }
  fetcher.delete("/groups/" + id, {headers: authStore.authHeader()})
    .then(() => {
      initData();
      deleteDialog.value = false;
    })
}

function capitalizeFirstLetter(sentence: string) {
    return String(sentence).charAt(0).toUpperCase() + String(sentence).slice(1);
}

</script>

<style scoped>

</style>
