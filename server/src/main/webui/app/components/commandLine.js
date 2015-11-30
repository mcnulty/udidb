import React from "react";
import { Input } from "react-bootstrap";

import {UdidbRequest, POST_METHOD} from "./requests.js"

let topLevelStyle = {
    fontFamily: 'monospace',
    fontSize: '11',
    whiteSpace: 'pre',
    border: '1px solid rgb(204, 204, 204)',
    borderRadius: '4px',
    padding: '5px 5px 5px 5px'
};

let formStyle = {
    display: "flex"
};

let inputStyle = {
    backgroundColor: 'rgb(0, 43, 54)',
    border: '0px',
    outline: '0px',
    width: 'auto',
    flex: 2,
    padding: '0px 0px 0px 0px'
};

const PROMPT = "(udidb)";

export default React.createClass({

    _handleNewOperation: function(e) {
        e.preventDefault();
        let value = this.refs.operation.value.trim();
        this.refs.operation.value = '';
        this.props.process(new UdidbRequest(POST_METHOD, "currentContext.operation", value));
    },

    focus: function(e) {
        e.preventDefault();
        this.refs.operation.focus();
    },

    render: function() {
        let history;
        if (this.props.currentContext.id === "-1") {
            history = this.props.globalContext.history;
        }else{
            history = this.props.currentContext.history;
        }
        let numDisplayedOps = this.props.historyPrefs.numDisplayedOps;

        let output = [];

        let startIndex = history.operations.length - numDisplayedOps;
        if (startIndex < 0) {
            startIndex = 0;
        }

        let pendingOperation = false;
        for (let i = startIndex; i < history.operations.length; i++)
        {
            let operation = history.operations[i];

            let operationResultValue;
            if (!operation.result) {
                operationResultValue = "";
                pendingOperation = true;
            }else{
                operationResultValue = operation.result;
            }

            let operationValue = operation.operands.reduce(function(p, c, i, a) {
                if (c.type === "list") {
                    return p + " " + c.value.join(" ");
                }else{
                    return p + " " + c.value;
                }
            }, operation.name);

            output.push(PROMPT + " " + operationValue);
            output.push(operationResultValue);
        }

        let inputElement;
        if (pendingOperation) {
            inputElement = 
            <strong id="commandLineInput">
                Result pending...
            </strong>
        }else{
            inputElement = 
                <form style={formStyle} onSubmit={this._handleNewOperation}>
                    <label htmlFor="commandLineInput" style={ { flex: '2', display: 'flex' } }>
                        {PROMPT + " "}
                        <input id="commandLineInput"
                            key={this.props.currentContext.id}
                            style={inputStyle}
                            type="text"
                            autoFocus="true"
                            ref="operation"/>
                    </label>
                </form>
        }

        return (
            <div className="hljs" style={topLevelStyle}>
                <div onClick={this.focus}>
                    {output.join("\n")}
                </div>
                {inputElement}
            </div>
        )
    }
});
