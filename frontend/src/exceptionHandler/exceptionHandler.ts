import router from "@/router/index";
import axios from "axios";
import {config} from "@/config";

const fetcher = axios.create({
    baseURL: config["API_URL"],
    timeout: 1000,
    headers: { "Content-Type": "application/json" },
});

fetcher.interceptors.response.use(
    (config) => {

        return config;
    },
    (error) => {
        try {
            if (error.response.status == 404) {
                console.log("404 NOT FOUND")
                router.push("/error/404")
            } else if (error.response.status == 403) {
                console.log("403 FORBIDDEN")
                router.push("/error/403")
            } else if (error.response.status == 401) {
              console.log("401 UNAUTHORIZED")
              router.push("/error/401")
            }
        } catch {
            console.log(error)
        }
        return Promise.reject(error);
    }
);

export default fetcher;
