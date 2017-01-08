require("babel-polyfill");
require("bootstrap/dist/css/bootstrap.css");
require("highlight.js/styles/solarized_dark.css");
require("./entry.css");

import * as React from "react";
import * as ReactDOM from "react-dom";

import * as Udidb from "./components/udidb";
import staticProps from "./components/staticData";
import {
    ContextBuilder,
    UdidbRequest,
    PUT_METHOD
} from "./components/types";
import update = require("react-addons-update");

class Props {
}

class Component extends React.Component<Props, Udidb.Props> {

    constructor(props: Props) {
        super(props);
        this.state = staticProps;
    }

    public render(): JSX.Element {
        return (
            <Udidb.Component {...this.state} process={this.process.bind(this)}/>
        );
    }

    private process(request: UdidbRequest): void {
        switch (request.method) {
            case PUT_METHOD:
                this.processPut(request);
                break;
            default:
                break;
        }
    }

    private processPut(request: UdidbRequest): void {
        switch (request.path) {
            case "currentContextIndex":
                this.setState(update(this.state, {
                    currentContextIndex: {
                        $set: parseInt(request.value, 10)
                    }
                }));
                break;
            case "currentContext.activeThreadIndex":
                let contexts = this.state.contexts.slice(0);
                let currentContextIndex = this.state.currentContextIndex;
                let context = contexts[currentContextIndex];

                contexts[currentContextIndex] = ContextBuilder.fromContext(context)
                                                              .setActiveThreadIndex(parseInt(request.value, 10))
                                                              .build();
                this.setState(update(this.state, {
                    contexts: {
                        $set: contexts
                    }
                }));
                break;
            default:
                console.log("PUT with unknown path: " + request);
                break;
        }
    }

}

ReactDOM.render(
    <Component/>,
    document.getElementById("udidb-app-container")
);
