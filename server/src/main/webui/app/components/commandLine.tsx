import * as React from "react";

import {
    Context,
    History,
    HistoryPrefs,
    UdidbRequest,
    POST_METHOD
} from "./types";

let topLevelStyle = {
    fontFamily: "monospace",
    whiteSpace: "pre",
    border: "1px solid rgb(204, 204, 204)",
    borderRadius: "4px",
    padding: "5px 5px 5px 5px"
};

let formStyle = {
    display: "flex"
};

let inputStyle = {
    backgroundColor: "rgb(0, 43, 54)",
    border: "0px",
    outline: "0px",
    width: "auto",
    flex: 2,
    padding: "0px 0px 0px 0px"
};

const PROMPT = "(udidb)";

export interface Props {
    readonly currentContextIndex: number;
    readonly currentContext: Context;
    readonly globalContext: Context;
    readonly historyPrefs: HistoryPrefs;
    readonly process: (request: UdidbRequest) => void;
}

export class Component extends React.Component<Props, {}> {

    refs: {
        [key: string]: Element;
        operation: HTMLInputElement;
    };

    constructor(props: Props) {
        super(props);
        this.refs = {
            operation: null
        };
    }

    private handleNewOperation(e: React.FormEvent<HTMLFormElement>): void {
        e.preventDefault();
        let value = this.refs.operation.value.trim();
        this.refs.operation.value = "";
        this.props.process(new UdidbRequest(POST_METHOD, "currentContext.operation", value));
    }

    focus(e: React.MouseEvent<HTMLDivElement>): void {
        e.preventDefault();
        this.refs.operation.focus();
    }

    render(): JSX.Element {
        let history: History;
        if (this.props.currentContext === null) {
            history = this.props.globalContext.history;
        } else {
            history = this.props.currentContext.history;
        }
        let numDisplayedOps = this.props.historyPrefs.numDisplayedOps;

        let output: string[] = [];

        let startIndex = history.operations.length - numDisplayedOps;
        if (startIndex < 0) {
            startIndex = 0;
        }

        let pendingOperation = false;
        for (let i = startIndex; i < history.operations.length; i++) {
            let operation = history.operations[i];

            let operationResultValue: string;
            if (!operation.result) {
                operationResultValue = "";
                pendingOperation = true;
            } else {
                operationResultValue = operation.result;
            }

            let operationValue = operation.operands.reduce(function(p, c, i, a) {
                if (c.type === "list") {
                    return p + " " + c.value.join(" ");
                } else {
                    return p + " " + c.value;
                }
            }, operation.name);

            output.push(PROMPT + " " + operationValue);
            output.push(operationResultValue);
        }

        let inputElement: JSX.Element;
        if (pendingOperation) {
            inputElement = (
                <strong id="commandLineInput">
                    Result pending...
                </strong>
            );
        } else {
            inputElement = (
                <form style={formStyle} onSubmit={this.handleNewOperation.bind(this)}>
                    <label htmlFor="commandLineInput" style={{ flex: "2", display: "flex" }}>
                        {PROMPT + " "}
                        <input id="commandLineInput"
                            key={this.props.currentContextIndex}
                            style={inputStyle}
                            type="text"
                            autoFocus={true}
                            ref="operation" />
                    </label>
                </form>
            );
        }

        return (
            <div className="hljs" style={topLevelStyle}>
                <div onClick={this.focus.bind(this)}>
                    {output.join("\n")}
                </div>
                {inputElement}
            </div>
        );
    }
}
