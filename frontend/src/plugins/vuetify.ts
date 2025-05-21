/**
 * plugins/vuetify.ts
 *
 * Framework documentation: https://vuetifyjs.com`
 */

// Styles
import '@mdi/font/css/materialdesignicons.css'
import 'vuetify/styles'

// Composables
import { createVuetify } from 'vuetify'

// https://vuetifyjs.com/en/introduction/why-vuetify/#feature-guides
export default createVuetify({
  theme: {
    themes: {
      light: {
        colors: {
          primary: '#3399ff',   // Highlight blue for primary actions
          secondary: '#34c9e7', // Light blue, used for secondary UI
          accent: '#b00197',    // Accent purple/magenta for calls to action or highlights
          success: '#33fe99',   // Use the vibrant green for success messages
          info: '#34c9e7',      // Reuse sky blue for info messages
          warning: '#ffc107',   // Optional: warm contrast tone
          error: '#ff5252',     // Optional: for error messages
          //background: '#f5f7fa', // Light background
          //surface: '#ffffff'    // Cards, sheets, etc.
        }
      },
      dark: {
        colors: {
          primary: '#3399ff',   // Highlight blue for primary actions
          secondary: '#34c9e7', // Light blue, used for secondary UI
          accent: '#b00197',    // Accent purple/magenta for calls to action or highlights
          success: '#33fe99',   // Use the vibrant green for success messages
          info: '#34c9e7',      // Reuse sky blue for info messages
          warning: '#ffc107',   // Optional: warm contrast tone
          error: '#ff5252',     // Optional: for error messages
          //background: '#f5f7fa', // Light background
          //surface: '#ffffff'    // Cards, sheets, etc.
        }
      }
    },
  },
})
