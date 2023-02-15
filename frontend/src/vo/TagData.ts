export default class TagData {
    name: string;
    count: number;
    latest: string;

    constructor(name: string, count: number, latest: string = "") {
        this.name = name;
        this.count = count;
        this.latest = latest;
    }
}