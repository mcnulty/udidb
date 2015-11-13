export class UdidbRequest {
    constructor(method, path, value) {
        this.method = method;
        this.path = path;
        this.value = value;
    }

    getMethod() {
        return this.method;
    }

    getPath() {
        return this.path;
    }

    getValue() {
        return this.value;
    }

    toString() {
        return this.method + " " + this.path + " '" + this.value + "'";
    }
};

export const PUT_METHOD = "PUT";
export const POST_METHOD = "POST";
