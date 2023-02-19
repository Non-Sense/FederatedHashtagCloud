import axios, {AxiosError, AxiosResponse} from "axios";
import GeneratedWordCloudApiResponse from "../vo/GeneratedWordCloudApiResponse";

export const GetWordCloud = (baseUrl: string, onReceive: (data: GeneratedWordCloudApiResponse) => void) => {
    axios
        .get(baseUrl + "/api/v1/generated")
        .then((result: AxiosResponse<GeneratedWordCloudApiResponse>) => {
            onReceive(result.data)
        })
        .catch((e: AxiosError<{ error: string }>) => {
            console.error(e.message);
        })
}