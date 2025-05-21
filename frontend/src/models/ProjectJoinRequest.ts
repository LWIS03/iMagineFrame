
export class ProjectJoinRequest {
  id: number;
  userId: number;
  username: string;
  projectId: number;
  projectName: string;
  dateCreated: string;
  dateUpdated: string;
  accepted: boolean | null;

  constructor(
    id: number = 0,
    userId: number = 0,
    username: string = "",
    projectId: number = 0,
    projectName: string = "",
    dateCreated: string = "",
    dateUpdated: string = "",
    accepted: boolean | null = null
  ) {
    this.id = id;
    this.userId = userId;
    this.username = username;
    this.projectId = projectId;
    this.projectName = projectName;
    this.dateCreated = dateCreated;
    this.dateUpdated = dateUpdated;
    this.accepted = accepted;
  }
}

export class ProjectJoinRequestAddDto {
  projectId: number;

  constructor(projectId: number = 0) {
    this.projectId = projectId;
  }
}

export async function getUserRequests() {
  try {
    const authStore = useAuthStore();
    const response = await fetcher.get("/projects/requests/user", {
      headers: authStore.authHeader()
    });
    return response.data;
  } catch (error) {
    console.error("Error fetching user requests:", error);
    return [];
  }
}

export async function getAllRequests() {
  try {
    const authStore = useAuthStore();
    const response = await fetcher.get("/projects/requests", {
      headers: authStore.authHeader()
    });
    return response.data;
  } catch (error) {
    console.error("Error fetching all requests:", error);
    return [];
  }
}

import { useAuthStore } from "@/stores/AuthStore";
import fetcher from "@/exceptionHandler/exceptionHandler";
