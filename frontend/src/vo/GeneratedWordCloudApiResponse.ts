import WordCloudElement from "./WordCloudElement";

export default class GeneratedWordCloudApiResponse {
    time: string;
    hostname: string;
    width: number;
    height: number;
    data: Array<WordCloudElement>;

    constructor(time: string, hostname: string, width: number, height: number, data: Array<WordCloudElement>) {
        this.time = time;
        this.hostname = hostname;
        this.width = width;
        this.height = height;
        this.data = data;
    }
}