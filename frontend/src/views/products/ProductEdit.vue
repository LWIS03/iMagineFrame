<template>
  <v-alert
    v-model="showSuccess"
    border="start"
    variant="tonal"
    closable
    color="success"
    title="Saved successfully"
  ></v-alert>

  <v-alert
    v-model="showError"
    border="start"
    variant="tonal"
    closable
    color="error"
    :title="errorMessage"
  ></v-alert>

  <div class="pa-5">
    <h2 v-if="action === 'new'">Create Product</h2>

    <v-row no-gutters>
      <v-col cols="12">
        <v-text-field
          v-model="editProduct.name"
          :counter="50"
          :error-messages="fieldErrors.get('name')"
          label="Product Name *"
          required
        ></v-text-field>
      </v-col>
    </v-row>

    <v-row no-gutters>
      <v-col cols="12">
        <v-textarea
          v-model="editProduct.description"
          :counter="500"
          :error-messages="fieldErrors.get('description')"
          label="Description *"
          rows="4"
          required
        ></v-textarea>
      </v-col>
    </v-row>

    <v-row no-gutters>
      <h3 class="mb-2">Properties (e.g sweetness 8)</h3>
      <v-col cols="12">
        <v-row v-for="(value, key) in editProduct.properties" :key="key" class="d-flex align-center" >
          <v-col cols="5" class="mb-n8">
            <v-text-field v-model="editProduct.properties[key]" :label="key"></v-text-field>
          </v-col>
          <v-col cols="2">
            <v-btn color="error" @click="removeProperty(key)">Remove</v-btn>
          </v-col>
        </v-row>
        <v-row class="d-flex align-center">
          <v-col cols="5">
            <v-text-field v-model="newPropertyKey" label="Property Key"></v-text-field>
          </v-col>
          <v-col cols="5">
            <v-text-field v-model="newPropertyValue" label="Property Value"></v-text-field>
          </v-col>
          <v-col cols="2">
            <v-btn color="primary" @click="addProperty">Add</v-btn>
          </v-col>
        </v-row>
      </v-col>
    </v-row>

    <v-row no-gutters class="mt-4">
      <v-col cols="12">
        <v-autocomplete
          v-model="selectedTagIds"
          :items="availableTags"
          item-title="name"
          item-value="id"
          label="Select Tags"
          multiple
          chips
          clearable
        ></v-autocomplete>
      </v-col>
    </v-row>


    <v-row no-gutters>
      <v-col cols="12">
        <h3>Product Image</h3>
        <v-file-input label="Upload Image" accept="image/*" @change="handleImageUpload"></v-file-input>
        <v-img v-if="previewImage" :src="previewImage" max-height="200" contain></v-img>
      </v-col>
    </v-row>

    <v-row class="mt-4">
      <v-col>
        <v-btn color="success" @click="save">Save</v-btn>
        <v-btn class="ml-2" color="secondary" to="/products">Back</v-btn>
      </v-col>
    </v-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/AuthStore";
import { ProductEditDto } from "@/models/Product";
import { computed } from 'vue';


import fetcher from "@/exceptionHandler/exceptionHandler";

const router = useRouter();
const authStore = useAuthStore();
const editProduct = ref(new ProductEditDto());
const showSuccess = ref(false);
const showError = ref(false);
const errorMessage = ref("");
const fieldErrors = ref(new Map());
const action = ref("new");
const newPropertyKey = ref("");
const newPropertyValue = ref("");
const uploadedImageFile = ref<File | null>(null);
const previewImage = ref<string | null>(null);
import { onMounted } from 'vue';
const availableTags = ref<{ id: number, name: string }[]>([]);
const selectedTagIds = ref<number[]>([]);

onMounted(async () => {
  try {
    const response = await fetcher.get('/tags',{
      headers: authStore.authHeader()
    });
    availableTags.value = await response.data;
  } catch (error) {
    console.error("Error fetching tags", error);
  }
});


const addProperty = () => {
  if (newPropertyKey.value && newPropertyValue.value) {
    editProduct.value.properties[newPropertyKey.value] = newPropertyValue.value;
    newPropertyKey.value = "";
    newPropertyValue.value = "";
  }
};

const removeProperty = (key: string) => {
  delete editProduct.value.properties[key];
};

const handleImageUpload = (event: Event) => {
  const input = event.target as HTMLInputElement;
  if (input.files && input.files.length > 0) {
    const file = input.files[0];
    uploadedImageFile.value = file;
    editProduct.value.image = file;

    const reader = new FileReader();
    reader.onload = () => {
      if (typeof reader.result === "string") {
        previewImage.value = reader.result;
      }
    };
    reader.readAsDataURL(file);
  }

};


async function save() {
  fieldErrors.value.clear();
  if (!editProduct.value.name || !editProduct.value.description) {
    showError.value = true;
    errorMessage.value = "Both name and description are required.";
    return;
  }

  try {
    const existing = await fetcher.get(`/products/name/${editProduct.value.name}`);
    if (existing.status === 200) {
      showError.value = true;
      errorMessage.value = "A product with this name already exists.";
      return;
    }
  } catch (error) {
    
  }

  try {
    const formData = new FormData();
    formData.append("product", JSON.stringify(editProduct.value));
    if (uploadedImageFile.value) {
      formData.append("image", uploadedImageFile.value);
    }

    await fetcher.post(`/products/add`, formData, {
      headers: {
        ...authStore.authHeader(),
        "Content-Type": "multipart/form-data",
      },
    });
  } catch (error) {
    showError.value = true;
    errorMessage.value = "Error saving product. Please check all fields.";
  }

  try {
    const getResponse = await fetcher.get(`/products/name/${editProduct.value.name}`, {
      headers: authStore.authHeader(),
    });
    const productId = getResponse.data.productId;
    console.log("Fetched productId:", productId);
    console.log("Selected Tag IDs:", selectedTagIds.value);
    for (const tagId of selectedTagIds.value) {
      await fetcher.put(`/products/${productId}/addTag/${tagId}`, null, {
        headers: authStore.authHeader(),
      });
    }

    showSuccess.value = true;
    await new Promise(resolve => setTimeout(resolve, 1000));
    await router.push("/products");
  }
  catch (error){
    showError.value = true;
    errorMessage.value = "Error saving product. Please check all fields.";
  }
}




</script>

<style scoped>
</style>
