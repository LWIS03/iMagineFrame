/**
 * main.ts
 *
 * Bootstraps Vuetify and other plugins then mounts the App`
 */

// Components
import App from './App.vue'

// Composables
import { createApp } from 'vue'
import { createPinia } from 'pinia'


// Plugins
import { registerPlugins } from '@/plugins'
import VCalendar from 'v-calendar';
import 'v-calendar/style.css';

const app = createApp(App)




app.use(VCalendar, {});

const pinia = createPinia()
app.use(pinia)

registerPlugins(app)


app.mount('#app')