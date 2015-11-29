import React from "react";
import update from "react-addons-update";
import request from "superagent/lib/client";

import Udidb from "./udidb.js";
import { UdidbRequest, PUT_METHOD, POST_METHOD } from "./requests.js";
import initialState from "./initialState.js";

const baseApiUri = "http://localhost:8888";

export default React.createClass({

    getInitialState: function() {
        return initialState;
    },

    render: function() {
        return (
            <Udidb {...this.state} process={this.process}/>
        );
    },

    process: function(request) {
        switch (request.getMethod()) {
            case POST_METHOD:
                this._processPost(request);
                break;
            case PUT_METHOD:
                this._processPut(request);
                break;
            default:
                console.log("Request with unknown method: " + request);
                break;
        }
    },

    _processPost: function(request) {
        switch (request.getPath()) {
            case "currentContext.operation":
                this._newOperation(request.getValue());
                break;
            default:
                console.log("POST with unknown path: " + request);
                break;
        }
    },

    _processPut: function(request) {
        switch (request.getPath()) {
            case "currentContextIndex":
                this._selectContext(request.getValue());
                break;
            case "currentContext.activeThreadIndex":
                this._selectActiveThread(request.getValue());
                break;
            default:
                console.log("PUT with unknown path: " + request);
                break;
        }
    },

    _newOperation: function(value) {
        let fields = value.split(" ");
        let currentContextIndex = this.state.currentContextIndex;

        let pendingResult;
        if (fields[0] === 'create') {
            pendingResult = 'Debuggee created';
        }else{
            pendingResult = null;
        }

        let newState;
        if (currentContextIndex >= 0) {
            newState = update(this.state, {
                contexts: {
                    [currentContextIndex] : {
                        history: { 
                            operations: {
                                $push: [{ 
                                    name: fields[0], 
                                    operands: [ {
                                        name: "value",
                                        type: "list",
                                        value: fields.slice(1),
                                    }],
                                    result: pendingResult
                                }]
                            }
                        }
                    }
                }
            });
        }else{
            newState = update(this.state, {
                globalHistory: {
                    operations: {
                        $push: [{
                            name: fields[0],
                            operands: [ {
                                name: "value",
                                type: "list",
                                value: fields.slice(1),
                            }],
                            result: pendingResult
                        }]
                    }
                }
            });

        }
        this.setState(newState);

        this._sendOperation(currentContextIndex, fields);
    },

    _sendOperation: function(contextIndex, fields) {
        if (contextIndex >= 0) {
            let contextId = this.state.contexts[contextIndex].id;
            request.post(baseApiUri + "/debuggeeContexts/" + contextId + "/process/operation")
                   .send(this._createOperationModel(fields))
                   .end(this._generateOperationResultHandler(contextIndex, fields));
        }else{
            switch (fields[0]) {
                case "create":
                    request.post(baseApiUri + "/debuggeeContexts")
                           .send(this._createDebuggeeConfigModel(fields))
                           .end(this._generateCreateResultHandler(fields));
                    break;
                default:
                    request.post(baseApiUri + "/debuggeeContexts/globalOperation")
                           .send(this._createOperationModel(fields))
                           .end(this._generateOperationResultHandler(contextIndex, fields));
                break;
            }
        }
    },

    _createDebuggeeConfigModel: function(fields) {
        let config = {};
        if (fields.length > 1) {
            config["execPath"] = fields[1];
        }else{
            config["execPath"] = null;
        }

        if (fields.length > 2) {
            config["args"] = fields.splice(2);
        }else{
            config["args"] = [];
        }

        return config;
    },

    _generateCreateResultHandler: function(fields) {
        return function(err, resp) {
            // TODO
        }
    },

    _createOperationModel: function(fields) {
        // TODO match name of the operation to its descriptor to constructor the operand model
        return {
            "name": fields[0],
            "operands": {}
        }
    },

    _generateOperationResultHandler: function(contextIndex, fields) {
        return function(err, resp) {
            let result;
            if (err) {
                result = JSON.stringify(err);
            }else{
                result = resp.body.result.value;
            }

            let newOperation = {
                name: fields[0],
                operands: [ {
                    name: "value",
                    type: "list",
                    value: fields.slice(1),
                } ],
                result: result
            };


            let newState;
            if (contextIndex < 0) {
                let operationIndex = this.state.globalHistory.operations.length-1;
                newState = update(this.state, {
                    globalHistory: {
                        operations: {
                            [operationIndex] : { $set: newOperation }
                        }
                    }
                });
            }else{
                let currentContextIndex = this.state.currentContextIndex;
                let operationIndex = this.state.contexts[currentContextIndex].history.operations.length-1;
                newState = update(this.state, {
                    contexts: {
                        [currentContextIndex] : {
                            history: {
                                operations: {
                                    [operationIndex] : { $set: newOperation }
                                }
                            }
                        }
                    }
                });
            }
            this.setState(newState);
        }.bind(this);
    },

    _selectContext: function(index) {
        let newState = update(this.state, {
            currentContextIndex: { $set: index }
        });
        this.setState(newState);
    },

    _selectActiveThread: function(index) {
        let currentContextIndex = this.state.currentContextIndex;
        let newState = update(this.state, {
            contexts: {
                [currentContextIndex]: {
                    activeThreadIndex: {
                        $set: index
                    }
                }
            }
        });
        this.setState(newState);
    },
});
