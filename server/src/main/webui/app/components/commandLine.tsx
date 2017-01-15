import * as React from "react";

import {
    Context,
    History,
    Operation,
    UdidbRequest,
    POST_METHOD,
    PUT_METHOD
} from "./types";

let topLevelStyle = {
    fontFamily: "monospace",
    whiteSpace: "pre",
    border: "1px solid rgb(204, 204, 204)",
    borderRadius: "4px",
    padding: "5px 5px 5px 5px",
    resize: "vertical",
    maxHeight: "40vh",
    minHeight: "10vh",
    overflow: "scroll"
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

    private handleKeyDown(e: React.KeyboardEvent<HTMLInputElement>): void {
        if (e.ctrlKey) {
            if (e.key === "u") {
                e.preventDefault();
                this.refs.operation.value = "";
            }
        }else if (e.key === "Tab") {
            e.preventDefault();
        }else if (e.key === "ArrowUp") {
            e.preventDefault();

            let history = this.getHistory();

            if (history.opIndex > 0 || history.opIndex === -1) {
                let newOpIndex: number;
                if (history.opIndex > 0) {
                    newOpIndex = history.opIndex - 1;
                } else {
                    newOpIndex = history.operations.length - 1;
                }

                if (newOpIndex >= 0) {
                    this.refs.operation.value = this.operationToString(history.operations[newOpIndex]);
                    this.props.process(new UdidbRequest(PUT_METHOD, "currentContext.history.setOpIndex", "" + newOpIndex));
                }
            }
        }else if (e.key === "ArrowDown") {
            e.preventDefault();

            let history = this.getHistory();

            if (history.opIndex <= history.operations.length - 1 && history.opIndex !== -1) {
                let newOpIndex: number;
                if (history.opIndex === history.operations.length - 1) {
                    newOpIndex = -1;
                } else {
                    newOpIndex = history.opIndex + 1;
                }

                if (newOpIndex >= 0) {
                    this.refs.operation.value = this.operationToString(history.operations[newOpIndex]);
                } else {
                    this.refs.operation.value = "";
                }
                this.props.process(new UdidbRequest(PUT_METHOD, "currentContext.history.setOpIndex", "" + newOpIndex));
            }
        }
    }

    private getHistory(): History {
        if (this.props.currentContext === null) {
            return this.props.globalContext.history;
        } else {
            return this.props.currentContext.history;
        }
    }

    private operationToString(operation: Operation): string {
        let operationValue = operation.operands.reduce(function(p, c, i, a) {
            if (c.type === "list") {
                return p + " " + c.value.join(" ");
            } else {
                return p + " " + c.value;
            }
        }, operation.name);
        return operationValue;
    }

    public render(): JSX.Element {
        let history = this.getHistory();
        let output: string[] = [];

        let pendingOperation = false;
        for (let i = 0; i < history.operations.length; i++) {
            let operation = history.operations[i];

            let operationResultValue: string;
            if (!operation.result) {
                operationResultValue = "";
                pendingOperation = true;
            } else {
                operationResultValue = operation.result;
            }

            let operationValue = this.operationToString(operation);

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
                            ref="operation"
                            onKeyDown={this.handleKeyDown.bind(this)}/>
                    </label>
                </form>
            );
        }

        return (
            <div className="hljs" style={topLevelStyle}>
                <div>
                    {output.join("\n")}
                </div>
                {inputElement}
            </div>
        );
    }
}
