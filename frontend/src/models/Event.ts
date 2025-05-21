export class Event {
  id: number;
  name: string;
  description: string;
  location: string;
  startdate: string | null;
  enddate: string | null;
  imageUrl: string | ArrayBuffer | null;
  label: string = 'OTHER';
  public: boolean = true;
  owner?: { id: number; username?: string; };

  constructor(
    id: number = 0,
    name: string = "default",
    description: string = "default",
    location: string = "default",
    startdate: string | null = null,
    enddate: string | null = null,
    imageUrl: string | ArrayBuffer | null = null,
    label: string = "OTHER"
  ) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.location = location;
    this.startdate = startdate;
    this.enddate = enddate;
    this.imageUrl = imageUrl;
    this.label = label;
  }
}

export class EventEditDto {
  name: string;
  description: string;
  location: string;
  startdate: string | null;
  enddate: string | null;
  imageUrl?: string | ArrayBuffer | null;
  label: string = 'OTHER';
  id?: number;
  public: boolean = true;

  constructor(
    name: string = "",
    description: string = "",
    location: string = "",
    startdate: string | null = null,
    enddate: string | null = null,
    imageUrl: string | ArrayBuffer | null = null,
    label: string = "OTHER",
    id?: number
  ) {
    this.name = name;
    this.description = description;
    this.location = location;
    this.startdate = startdate;
    this.enddate = enddate;
    this.imageUrl = imageUrl;
    this.label = label;
    this.id = id;
  }
}

import fetcher from "@/exceptionHandler/exceptionHandler";

export async function getEvents(authHeader = {}){
  try {
    const res = await fetcher.get("/events", {headers: authHeader});
    if(res && res.data) {
      return res.data;
    } else {
      console.log("No data returned from API: " + res.status);
      return [];
    }
  } catch (error) {
    return [];
  }
}

export async function getPublicEvents(){
  try {
    const res = await fetcher.get("/events/public", );
    if(res && res.data) {
      return res.data;
    } else {
      console.log("No data returned from API: " + res.status);
      return [];
    }
  } catch (error) {
    return [];
  }
}

export function getEventColor(label: string): string {
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

  return colorMap[label] || 'gray'; // default color
}
