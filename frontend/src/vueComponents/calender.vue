<template>
  <v-calendar
    :attributes="calendarAttributes"
    :first-day-of-week="1"
  />
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import { Event } from "@/models/Event";


const props = defineProps<{ events: Array<Event> }>()

const parseDate = (date: string | null) => {
  const parsedDate = new Date(date as string);
  return isNaN(parsedDate.getTime()) ? null : parsedDate;
};

const calendarAttributes = computed(() => {
  const eventAttributes = props.events?.map(event => {
    const startDateObj = parseDate(event.startdate);
    const endDateObj = parseDate(event.enddate);
    const color = getEventColor(event.label);

    return {
      key: event.id,
      dates: startDateObj ? [startDateObj, endDateObj].filter(Boolean) : [],
      highlight: {
        color,
        fillMode: 'light',
      },
      dot: { color },
      popover: {
        label: event.name,
        description: event.location,
        visibility: 'hover',
      },
      customData: event,
    };
  });

    const todayAttribute = {
      key: 'today',
      dates: [new Date().toISOString().split('T')[0]], // Make sure it's in YYYY-MM-DD format
      highlight: {
        color: 'primary', // Change this color as needed
        fillMode: 'solid',
      },
      dot: { color: 'accent' },
      popover: {
        label: 'Today',
        description: 'Current Date',
        visibility: 'hover',
      },
    };

    return [...eventAttributes, todayAttribute];
});

function getEventColor(label: string): string {
  const colorMap: Record<string, string> = {
    'HACKATHON': 'blue',
    'CODING': 'purple',
    'EATING': 'green',
    'DRINKING': 'red',
    'MEETING': 'orange',
    'LEARNING': 'cyan',
    'PARTY': 'pink',
    'MOVIE': 'indigo',
    'OTHER': 'gray'
  };
  return colorMap[label] || 'gray'; //this is our fallback color
}


</script>
