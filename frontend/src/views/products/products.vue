<template>
  <div class="pa-5">
    <div class="d-flex justify-space-between align-center mb-4">
      <h2>Products</h2>
      <div class="d-flex">
        <v-btn color="primary" class="white--text mr-2" :to="'/products/edit'">
          Add new Product
        </v-btn>

        <v-btn color="primary" class="white--text mr-2" @click="tagDialog = true">
          Create Tag
        </v-btn>

        <v-btn color="primary" class="white--text mr-2" @click="goToPdf">
          Get product report
          <v-icon> mdi-open-in-new</v-icon>
        </v-btn>
      </div>
    </div>

    <div class = "main-content">
      <div class="product-list">
        <div v-for="product in productsList" :key="product.productId" class="product-card">
          <img :src="product.imageBlobUrl" alt="Product Image" class="product-image" />
          <div class="product-details">
            <h3 class="product-title">{{ product.name }}</h3>
            <div class="product-info">
              <div class="product-tags">
                <v-chip
                  v-for="tag in product.tagsName"
                  :key="tag"
                  class="ma-1 white--text"
                  color="primary"
                  variant="flat"
                  size="small"
                >
                  {{ tag }}
                </v-chip>
              </div>

              <p class="description-clamp">
                <strong>Description:</strong> {{ product.description }}
              </p>
            </div>
            <div class="d-flex">
              <div class="mr-2">
                <div class="buttons-container">
                  <div v-if="Object.keys(product.properties).length > 0" class="product-properties">
                    <div>
                      <v-btn @click="toggleDialogProperties(product)">View Properties</v-btn>
                      <v-dialog v-model="product.showProperties" max-width="300" max-height="400">
                        <v-card>
                          <v-card-text>
                            <v-table>
                              <thead>
                                <tr>
                                  <th class="text-left">Property</th>
                                  <th class="text-left">Value</th>
                                </tr>
                              </thead>
                              <tbody>
                                <tr v-for="(value, key) in product.properties" :key="key">
                                  <td>{{ key }}</td>
                                  <td>{{ value }}</td>
                                </tr>
                              </tbody>
                            </v-table>
                          </v-card-text>
                          <v-card-actions>
                            <v-btn @click="product.showProperties = false">Close</v-btn>
                          </v-card-actions>
                        </v-card>
                      </v-dialog>
                    </div>
                  </div>
                </div>
                <div class="product-batches">
                  <div>
                    <v-btn v-if="Object.keys(product.batchIds).length > 0" @click="fetchBatches(product)">View Batches</v-btn>
                    <v-dialog v-model="product.showBatches" max-width="600" max-height="400">
                      <v-card>
                        <v-card-text>
                          <v-table>
                            <thead>
                              <tr>
                                <th class="text-left">Id</th>
                                <th class="text-left">Ordered At</th>
                                <th class="text-left">Quantity</th>
                                <th class="text-left">Price</th>
                                <th class="text-left">Expires</th>
                              </tr>
                            </thead>
                            <tbody>
                              <tr v-for="batch in batchDetails" :key="batch.id">
                                <td>{{ batch.id }}</td>
                                <td>{{ formatDate(batch.addedDate).toString() }}</td>
                                <td>{{ batch.quantity }}</td>
                                <td>{{ batch.unitPrice }}€</td>
                                <td>{{ formatDate(batch.expirationDate).toString() }}</td>
                              </tr>
                            </tbody>
                          </v-table>
                        </v-card-text>
                        <v-card-actions>
                          <v-btn @click="product.showBatches = false">Close</v-btn>
                        </v-card-actions>
                      </v-card>
                    </v-dialog>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="bottom-button d-flex flex-column">
            <v-btn color="primary" class="white--text mb-2" @click="openBatchDialog(product)">
              New Batch
            </v-btn>
            <div class="d-flex justify-space-between align-center">
              <v-btn
                color="primary"
                class="white--text flex-grow-1 mr-2"
                @click="openEditDialog(product)"
              >
                Edit Product
              </v-btn>

              <v-btn
                icon
                color="error"
                class="white--text"
                style="font-size: 1.25rem; width: 40px; height: 40px; padding: 0; min-width: 0; border-radius: 10px;"
                @click="openDeleteDialog(product)"
              >
                <v-icon>mdi-delete</v-icon>
              </v-btn>

            </div>
          </div>
        </div>
      </div>
      <div :class="{ 'dark-mode': isDarkMode }" class="tags-panel sticky-sidebar" >
        <h3>Tags overview</h3>
        <v-chip
          v-for="tag in tags"
          :key="tag.id"
          class="ma-1"
          color="primary"
          text-color="white"
          label
        >
          {{ tag.name }}
        </v-chip>
      </div>
    </div>



    <v-dialog v-model="batchDialog" max-width="400">
      <v-card>
        <v-alert v-model="showSuccess" border="start" variant="tonal" closable color="success" title="Saved successfully" />
        <v-alert v-model="showError" border="start" variant="tonal" closable color="error" :title="errorMessage" />
        <v-card-title>Add New Batch</v-card-title>
        <v-card-text>
          <v-text-field label="Price (€) *" v-model="newBatch.unitPrice" type="number" />
          <v-text-field label="Quantity *" v-model="newBatch.quantity" type="number" />
          <v-text-field label="Expiration Date" v-model="newBatch.expirationDate" type="Date" />
        </v-card-text>
        <v-card-actions>
          <v-btn color="blue" @click="addBatch">Add</v-btn>
          <v-btn color="red" @click="batchDialog = false">Cancel</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>

  <v-dialog v-model="tagDialog" max-width="400">

    <v-card>
      <v-alert v-model="showSuccess" border="start" variant="tonal" closable color="success" title="Saved successfully" />
      <v-alert v-model="showError" border="start" variant="tonal" closable color="error" :title="errorMessage" />
      <v-card-title>Create Tag</v-card-title>
      <v-card-text>
        <v-text-field label="Tag name" v-model="newTag.name" />
      </v-card-text>
      <v-card-actions>
        <v-btn color="blue" @click="addTag">Save</v-btn>
        <v-btn color="red" @click="tagDialog = false">Cancel</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <v-dialog v-model="deleteDialog" max-width="400">
    <v-card>
      <v-card-title class="text-h6">Delete Product?</v-card-title>
      <v-card-text>
        Are you sure you want to delete <strong>{{ productToDelete?.name }}</strong>? This action cannot be undone.
      </v-card-text>
      <v-card-actions>
        <v-btn color="red" @click="deleteProduct">Yes, Delete</v-btn>
        <v-btn color="grey" @click="deleteDialog = false">Cancel</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <v-dialog v-model="editDialog" max-width="600px">
    <v-card>
      <v-card-title>Edit Product</v-card-title>
      <v-card-text>
        <v-textarea v-model="editableProduct.description" label="Description" />

        <div class="my-4">
          <label class="font-weight-medium">Tags:</label>
          <v-chip-group v-model="editableProduct.tagsId" multiple column>
            <v-chip
              v-for="tag in tags"
              :key="tag.id"
              :value="tag.id"
              color="primary"
              text-color="white"
              label
            >
              {{ tag.name }}
            </v-chip>
          </v-chip-group>
        </div>

        <div class="my-4">
          <label class="font-weight-medium">Properties:</label>
          <div
            v-for="(value, key) in editableProduct.properties"
            :key="key"
            class="d-flex align-center mb-2"
          >
            <v-text-field
              v-model="editableProduct.properties[key]"
              :label="key"
              class="flex-grow-1 mr-2"
            />
            <v-btn icon @click="removeProperty(key)">
              <v-icon color="red">mdi-delete</v-icon>
            </v-btn>
          </div>
          <div class="d-flex">
            <v-text-field v-model="newProperty.key" label="New Property Key" class="mr-2" />
            <v-text-field v-model="newProperty.value" label="Value" class="mr-2" />
            <v-btn @click="addProperty" color="primary">Add</v-btn>
          </div>
        </div>

        <div class="my-4">
          <label class="font-weight-medium">Image:</label>
          <v-file-input label="Upload Image" accept="image/*" @change="onImageSelected"></v-file-input>
          <v-img v-if="previewImage" :src="previewImage" max-height="200" contain></v-img>
        </div>
      </v-card-text>

      <v-card-actions>
        <v-btn color="blue" @click="saveEditedProduct">Save</v-btn>
        <v-btn color="red" @click="editDialog = false">Cancel</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>



</template>

<script setup lang="ts">
import fetcher from "@/exceptionHandler/exceptionHandler";
import {onMounted, ref, watch} from "vue";
import {useAuthStore} from "@/stores/AuthStore";
import {Product} from "@/models/Product";
import {Tag} from "@/models/Tag";
import {Batch, BatchAddDto} from "@/models/Batch";
import {config} from "@/config";

const authStore = useAuthStore();
const productsList = ref<Product[]>([]);
const isDarkMode = ref(localStorage.getItem("darkMode") === "true");

const batchDialog = ref(false);
const newBatch = ref<BatchAddDto>({ productId: 0, quantity: 0, unitPrice: 0, expirationDate: null});
const batchDetails = ref<Batch[]>([]);
const showSuccess = ref(false);
const showError = ref(false);
const errorMessage = ref("");
const tags = ref<Tag[]>([]);
const deleteDialog = ref(false);
const productToDelete = ref<Product | null>(null);
const editDialog = ref(false);
const editableProduct = ref<Product>({} as Product);
const previewImage = ref<string | null>(null);
const selectedImageFile = ref<File | null>(null);
const newProperty = ref({ key: "", value: "" });

const openEditDialog = (product: Product) => {
  editableProduct.value = JSON.parse(JSON.stringify(product)); // deep copy
  previewImage.value = product.imageBlobUrl ?? null;
  editDialog.value = true;
};

const addProperty = () => {
  if (newProperty.value.key && newProperty.value.value) {
    editableProduct.value.properties[newProperty.value.key] = newProperty.value.value;
    newProperty.value = { key: "", value: "" };
  }
};

const removeProperty = (key: string) => {
  delete editableProduct.value.properties[key];
};

const onImageSelected = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0];
  if (file) {
    selectedImageFile.value = file;
    previewImage.value = URL.createObjectURL(file);
  }
};

const saveEditedProduct = async () => {


  const payload = {
    description: editableProduct.value.description,
    properties: {},
    tagIds: editableProduct.value.tagsId,
  };

  Object.entries(editableProduct.value.properties).forEach(([key, value]) => {
    (payload.properties as Record<string, string>)[key] = value;
  });

  const formData = new FormData();
  formData.append("product", JSON.stringify(payload));

  if (selectedImageFile.value) {
    formData.append("image", selectedImageFile.value);
  }

  try {
    await fetcher.post(`products/${editableProduct.value.productId}`, formData, {
      headers: {
        "Content-Type": "multipart/form-data",
        ...authStore.authHeader(),
      },
    });

    editDialog.value = false;
    await InitData();
    selectedImageFile.value = null;
  } catch (error) {
    console.error("Error updating product:", error);
  }
};


async function goToPdf() {
  const response = await fetcher.get('/products/report', {
    headers: authStore.authHeader()
  });
  const url = config["API_URL"] + response.data;
  window.open(url, "_blank");
}

toggleBodyClass();

function toggleBodyClass() {
  if (isDarkMode.value) {
    document.body.classList.add("dark-mode");
  } else {
    document.body.classList.remove("dark-mode");
  }
}

async function InitData() {
  const res = await fetcher.get(`/products`, {
    headers: authStore.authHeader()
  });
  if (res && res.data) {
    productsList.value = await Promise.all(res.data.map(async (product: Product) => {
      let imageBlobUrl = null;
      if (product.imageUrl) {
        try {
          const imageRes = await fetcher.get(`/products/${product.productId}/image`, {
            headers: authStore.authHeader(),
            responseType: 'blob',
          });
          imageBlobUrl = URL.createObjectURL(imageRes.data);
        } catch (error) {
          console.error("Error loading image:", error);
        }
      }

      return {
        ...product,
        showProperties: false,
        showBatches: false,
        imageBlobUrl,
        batches: product.batchIds || []
      };
    }));
  } else {
    productsList.value = [];
    console.log("No data returned from API: " + res.status);
  }
}

onMounted(InitData);

const openBatchDialog = (product: Product) => {
  showSuccess.value = false;
  newBatch.value = new BatchAddDto(product.productId, 0, 0, null);
  batchDialog.value = true;
};

const addBatch = async () => {
  if (newBatch.value.expirationDate && newBatch.value.expirationDate.length === 10) {
    newBatch.value.expirationDate += "T00:00:00";
  }
  if (!newBatch.value) {
    showError.value = true;
    errorMessage.value = "Fill up all the information";
  } else if (newBatch.value.unitPrice === 0 && newBatch.value.quantity === 0) {
    showError.value = true;
    errorMessage.value = "Price and Quantity must be greater than 0";
  } else if (newBatch.value.unitPrice === 0) {
    showError.value = true;
    errorMessage.value = "Price must be greater than 0";
  } else if (newBatch.value.quantity === 0) {
    showError.value = true;
    errorMessage.value = "Quantity must be greater than 0";
  } else {
    try {
      await fetcher.post(`/batch`, newBatch.value, {
        headers: authStore.authHeader(),
      });
      showSuccess.value = true;
      showError.value = false;
      batchDialog.value = false;
      InitData();
    } catch (error) {
      showError.value = true;
      console.error("Error adding batch:", error);
    }
  }
};

const toggleDialogProperties = (product: Product) => {
  product.showProperties = !product.showProperties;
};

const fetchBatches = async (product: Product) => {
  const BatchInfo = await Promise.all(
    Array.from(product.batchIds).map(async (batchId: number) => {
      const res = await fetcher.get(`/batch/${batchId}`, {
        headers: authStore.authHeader(),
      });
      return res.data;
    })
  );

  batchDetails.value = BatchInfo;
  product.showBatches = true;
};

const formatDate = (date: any) => {
  try {
    if (!date) {return "does not expire";}
    const parsedDate = date instanceof Date ? date : new Date(date);
    return parsedDate.toLocaleDateString();
  } catch (error) {
    console.error("Error formatting date:", date, error);
    return "Invalid date";
  }
};

const tagDialog = ref(false);
const newTag = ref<{ name: string }>({ name: '' });

const addTag = async () => {
  if (!newTag.value.name.trim()) {
    showError.value = true;
    errorMessage.value = "Tag name is required";
    return;
  }

  try {
    await fetcher.post(`/tags/add`, newTag.value, {
      headers: authStore.authHeader(),
    });
    showSuccess.value = true;
    showError.value = false;
    tagDialog.value = false;
    newTag.value.name = ''; // reset
  } catch (error) {
    showError.value = true;
    errorMessage.value = "Error creating tag";
    console.error("Error creating tag:", error);
  }
  await fetchTags();
};

const fetchTags = async () => {
  try {
    const res = await fetcher.get("/tags", {
      headers: authStore.authHeader(),
    });
    tags.value = res.data;
  } catch (error) {
    console.error("Error fetching tags:", error);
  }
};

watch(tagDialog, (newVal) => {
  if (!newVal) {return;}
  showSuccess.value = false;
  showError.value = false;
});

const openDeleteDialog = (product: Product) => {
  productToDelete.value = product;
  deleteDialog.value = true;
};

const deleteProduct = async () => {
  if (!productToDelete.value) {return;}
  try {
    await fetcher.delete(`/products/id/${productToDelete.value.productId}`, {
      headers: authStore.authHeader(),
    });
    showSuccess.value = true;
    showError.value = false;
    deleteDialog.value = false;
    await InitData(); // Refresh product list
  } catch (error) {
    showError.value = true;
    errorMessage.value = "Error deleting product";
    console.error("Error deleting product:", error);
  }
};

onMounted(() => {
  InitData();
  fetchTags();
});

</script>

<style scoped>

.product-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  flex-grow: 1;
}

.product-card {
  display: flex;
  flex-direction: column;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  padding: 16px;
  background-color: #fff;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
  transition: transform 0.2s;
}

.product-card:hover {
  transform: translateY(-3px);
  box-shadow: 0px 8px 20px rgba(0, 0, 0, 0.1);
}

.product-card h3 {
  font-size: 1.5rem;
  font-weight: 600;
  text-align: center;
  margin-bottom: 8px;
  color: #333;
  min-height: 48px;
}



.product-card p {
  font-size: 1rem;
  color: #000000;
  font-weight: normal;
  margin-bottom: 12px;
}

.white--text{
  margin-top: 10px;
}

:root {
  background-color: white;
  --text-color: black;
  --border-color: #ddd;
}

.product-card .v-btn {
  text-transform: none;
  font-weight: 500;
}

.product-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-top-left-radius: 10px;
  border-top-right-radius: 10px;
  border-bottom-left-radius: 10px;
  border-bottom-right-radius: 10px;
}


.product-details {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
}

.product-details .d-flex {
  margin-top: auto;
}

.product-title {
  margin-bottom: 15px;
  font-size: 1.4em;
  font-weight: bold;
}

.product-info p {
  margin: 8px 0;
}

.buttons-container {
  min-height: 30px;
}

.product-batches {
  text-align: left;
  margin-top: 10px;
}

h2 {
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: bold;
}

.product-tags {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.main-content {
  display: flex;
  flex-direction: row;
  gap: 20px;
}

.tags-panel {
  width: 200px;
  padding: 16px;
  background-color: var(--bg-color, #f5f5f5);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  height: fit-content;
}

.tags-panel h3 {
  color: #000;
}

.dark-mode .tags-panel h3 {
  color: #fff;
}



</style>
