import axios, {AxiosError, AxiosResponse} from "axios";
import TagData from "../vo/TagData";

export const GetTag = (baseUrl: string, onReceive: (data: Array<TagData>) => void ) => {
    axios
        .get(baseUrl+"/api/v1/tags")
        .then((result: AxiosResponse<Array<TagData>>) => {
            onReceive(result.data)
        })
        .catch((e: AxiosError<{error: string}>) => {
            console.error(e.message);
        })
}