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
import * as Ctrl from "./components/udidbController";
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
                this.setState(Ctrl.Component.selectContext(this.state, parseInt(request.value, 10)));
                break;
            case "currentContext.activeThreadIndex":
                this.setState(Ctrl.Component.selectActiveThread(this.state, parseInt(request.value, 10)));
                break;
            case "commandLine.height":
                this.setState(Ctrl.Component.handleCommandLineHeightChange(this.state, parseInt(request.value, 10)));
                break;
            case "currentContext.history.setOpIndex":
                this.setState(Ctrl.Component.setOpIndex(this.state, parseInt(request.value, 10)));
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
