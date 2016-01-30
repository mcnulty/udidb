require('script!../third_party/autobahnjs/autobahn.min.js');

import React from "react";
import { Modal, Button } from "react-bootstrap";
import update from "react-addons-update";
import request from "superagent/lib/client";

import Udidb from "./udidb.js";
import { UdidbRequest, PUT_METHOD, POST_METHOD } from "./requests.js";
import initialState from "./initialState.js";

export default React.createClass({

    getInitialState: function() {
        return initialState;
    },

    componentDidMount: function() {
        this._getInitialAPIData(this._createUdidbEventListener);
    },

    _createUdidbEventListener: function() {
        this.state.eventConnection = new autobahn.Connection({
            url: 'ws://localhost:8888/events',
            realm: 'udidb'
        });

        this.state.eventConnection.onopen = function (session) {
            if (this.state.eventSession && this.state.eventSession.id !== session) {
                // Retry has been completed successfully
                this._closeModal();
                this._getInitialAPIData(function() { });
            }
            session.subscribe('com.udidb.events',
                              this._udidbEventHandler);
            this.setState(update(this.state, {
                eventSession: {
                    $set: session
                }
            }));
        }.bind(this);

        this.state.eventConnection.onclose = function (reason, details) {
            console.log("Events connection closed: (" + reason + ", " + details + ")");
            this._openModal("UDIDB events connection unavailable",
                            "Waiting for connection to be re-established...",
                            function () { },
                            null);
        }.bind(this);

        this.state.eventConnection.open();
    },

    _retryInitialData: function() {
        this._openModal("UDIDB server unavailable",
                        "Click Retry to connect again",
                        function() {
                            this._closeModal();
                            this._getInitialAPIData(this._createUdidbEventListener);
                        }.bind(this),
                        "Retry");
    },

    _getInitialAPIData: function(success) {
        // reset the state that will be retrieved
        this.setState(update(this.state, {
            globalContext: {
                history: { 
                    baseIndex: { $set: 0 },
                    operations: { $set: [] }
                },
                operationDescriptors: { $set: [] }
            },
            contexts: {
                $set: []
            },
            currentContextIndex: {
                $set: -1
            }
        }));

        request.get(this.props.baseApiUri + "/debuggeeContexts/operations")
        .end((function(err, resp) {
            if (err) {
                console.log("Failed to retrieve debuggee context operations: " + err);
                this._retryInitialData()
            }else{
                let newState = update(this.state, {
                    globalContext: {
                        operationDescriptors: { $set: resp.body.elements }
                    }
                });
                this.setState(newState);
            }
        }).bind(this));

        request.get(this.props.baseApiUri + "/debuggeeContexts")
        .end((function(err, resp) {

            if (err) {
                console.log("Failed to retrieve debuggee contexts: " + err);
                this._retryInitialData();
            }else{
                resp.body.elements.forEach((function(context, index, array) {
                    this._addContextFromApiModel(context,
                                                 function(resp) 
                                                 {
                                                     console.log("Failed retrieve data for context with id "
                                                                 + context.id + ": " + resp);

                                                                 if (resp instanceof Error) {
                                                                     console.log(resp.stack);
                                                                 }
                                                 });
                }).bind(this));
                success();
            }
        }).bind(this));
    },

    render: function() {
        let footerContent;
        if (this.state.modal.buttonLabel) {
            footerContent = <Button onClick={this.state.modal.clickHandler}>{this.state.modal.buttonLabel}</Button>;
        }else{
            footerContent = <div></div>;
        }
        return (
            <div>
                <Udidb {...this.state} process={this.process}/>
                <Modal show={this.state.modal.show} onHide={this.state.modal.clickHandler}>
                    <Modal.Header>
                        <h4>{this.state.modal.header}</h4>
                    </Modal.Header>
                    <Modal.Body>
                        {this.state.modal.body}
                    </Modal.Body>
                    <Modal.Footer>
                        {footerContent}
                    </Modal.Footer>
                </Modal>
            </div>
        );
    },

    _closeModal: function() {
        this.setState(update(this.state, {
            modal: {
                show: { $set: false },
                header: { $set: "" },
                body: { $set: "" },
                clickHandler: { $set: function() { } },
                buttonLabel: { $set: "" }
            }
        }));
    },

    _openModal: function(header, body, clickHandler, buttonLabel) {
        this.setState(update(this.state, {
            modal: {
                show: { $set: true },
                header: { $set: header },
                body: { $set: body },
                clickHandler: { $set: clickHandler },
                buttonLabel: { $set: buttonLabel }
            }
        }));
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
                this._submitOperation(request.getValue());
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

    _createOperation: function(value, operationDescriptors) {

        let foundOperation = false;
        let operation = null;
        if (operationDescriptors) {
            let fields = value.split(' ');
            for (let nameIndex = 0; nameIndex < fields.length; nameIndex++) {
                let name = fields.slice(0, nameIndex+1).join(' ');

                operationDescriptors.forEach(function(d, index, array) {
                    if (d.name === name) {
                        foundOperation = true;

                        operation = {};
                        operation["name"] = d.name;

                        let operands = [];
                        for (let i = nameIndex+1; i < fields.length; i++) {
                            let operandIndex = i - (nameIndex + 1);
                            if (operandIndex >= d.operandDescriptions.length) {
                                // The specified arguments are invalid
                                operation = null;
                                break;
                            }

                            let operandDescription = d.operandDescriptions[operandIndex];
                            if (operandDescription.type === 'list') {
                                operands.push({ 
                                    name: operandDescription.name,
                                    type: operandDescription.type,
                                    value: fields.splice(i)
                                });
                                break;
                            }else{
                                operands.push({
                                    name: operandDescription.name,
                                    type: operandDescription.type,
                                    value: fields[i]
                                });
                            }
                        }
                        operation["operands"] = operands;

                        if (operation.name === 'create') {
                            operation["result"] = 'Creating debuggee...';
                        }else{
                            operation["result"] = null;
                        }
                    }
                });
            }
        }

        if (operation === null) {
            let resultMsg;
            if (!foundOperation) {
                resultMsg = "Operation not found, cannot execute";
            }else{
                resultMsg = "Invalid arguments for operation '" + name + "'";
            }

            operation = {
                name: "<unknown>",
                operands: [],
                result: resultMsg
            };
        }

        return operation;
    },

    _createOperationApiModel: function(operation) {
        let apiModel = JSON.parse(JSON.stringify(operation));
        let newOperands = {};
        apiModel.operands.forEach(function(operand, index, array) {
            newOperands[operand.name] = operand.value;
        });
        apiModel.operands = newOperands;
        return apiModel;
    },

    _createDebuggeeConfigModel: function(operation) {
        let config = {};
        operation.operands.forEach(function(operand, index, array) {
            config[operand.name] = operand.value;
        });
        return config;
    },

    _submitOperation: function(value) {
        let currentContextIndex = this.state.currentContextIndex;

        let operationDescriptors;
        if (currentContextIndex >= 0) {
            operationDescriptors = this.state.contexts[currentContextIndex].operationDescriptors;
        }else{
            operationDescriptors = this.state.globalContext.operationDescriptors;
        }

        let operation = this._createOperation(value, operationDescriptors);

        let newState;
        if (currentContextIndex >= 0) {
            newState = update(this.state, {
                contexts: {
                    [currentContextIndex] : {
                        history: { 
                            operations: {
                                $push: [ operation ]
                            }
                        }
                    }
                }
            });
        }else{
            newState = update(this.state, {
                globalContext: {
                    history: {
                        operations: {
                            $push: [ operation ]
                        }
                    }
                }
            });

        }
        this.setState(newState);

        if (operation.result === null || operation.name === 'create') {
            this._sendOperation(currentContextIndex, operation);
        }
    },

    _sendOperation: function(contextIndex, operation) {

        switch (operation.name) {
            case "create":
                request.post(this.props.baseApiUri + "/debuggeeContexts")
                       .send(this._createDebuggeeConfigModel(operation))
                       .end(this._generateCreateResultHandler(operation));
                break;
            default:
                if (contextIndex >= 0) {
                    let contextId = this.state.contexts[contextIndex].id;
                    request.post(this.props.baseApiUri + "/debuggeeContexts/" + contextId + "/process/operation")
                           .send(this._createOperationApiModel(operation))
                           .end(this._generateOperationResultHandler(contextIndex));
                }else{
                    request.post(this.props.baseApiUri + "/debuggeeContexts/globalOperation")
                           .send(this._createOperationApiModel(operation))
                           .end(this._generateOperationResultHandler(contextIndex));
                }
            break;
        }
    },

    _addContextFromApiModel: function(context, errorCallback) {
        let newContext = {}
        newContext["id"] = context.id;
        newContext["history"] = {
            baseIndex: 0,
            operations: []
        };
        newContext["activeThreadIndex"] = 0;

        let processPromise = new Promise(function(resolve, reject) {
            request.get(this.props.baseApiUri + "/debuggeeContexts/" + context.id + "/process")
                   .end(function(err, resp) {
                       if (err) {
                           reject(resp);
                       }else{
                           resolve(resp);
                        }
                   });
        }.bind(this))
        .then(function(resp) {
            newContext["processId"] = resp.body.pid;
        });

        let threadsPromise = new Promise(function(resolve, reject) {
            request.get(this.props.baseApiUri + "/debuggeeContexts/" + context.id + "/process/threads")
            .end(function(err, resp) {
                if (err) {
                    reject(resp);
                }else{
                    resolve(resp);
                }
            });
        }.bind(this))
        .then(function(resp) {
            newContext["threads"] = resp.body.elements;
        });

        let descriptorsPromise = new Promise(function(resolve, reject) {
            request.get(this.props.baseApiUri + "/debuggeeContexts/" + context.id + "/process/operations")
                   .end(function(err, resp) {
                       if (err) {
                           reject(resp);
                       }else{
                           resolve(resp);
                       }
                   });
        }.bind(this))
        .then(function(resp) {
            newContext["operationDescriptors"] = resp.body.elements;
        });

        Promise.all([ processPromise, threadsPromise, descriptorsPromise ])
               .then(function (responses) {
                   let newState = update(this.state, {
                       contexts: {
                           $push: [ newContext ]
                       },
                       currentContextIndex: { $apply: function(x) {
                           if (x === -1) {
                               return 0;
                           }
                           return x + 1;
                       }}
                   });
                   this.setState(newState);
               }.bind(this))
               .catch(function(errorResponse) {
                   errorCallback(errorResponse);
               });
    },

    _removeContext: function(contextIndex) {
        let newState = update(this.state, {
            contexts: {
                $splice: [[contextIndex, 1]]
            },
            currentContextIndex: {
                $set: -1
            }
        });
        this.setState(newState);
    },

    _udidbEventHandler: function(args) {
        let udidbEvent = args[0];
        let contextIndex;
        if (udidbEvent.contextId) {
            contextIndex = this.state.contexts.findIndex(function (element, index, array) {
                return element.id === udidbEvent.contextId;
            });
            if (contextIndex < 0) {
                contextIndex = -1;
            }        
        }else{
            contextIndex = null;
        }

        switch (udidbEvent.eventType) {
            case "BREAKPOINT":
                this._handleBreakpointEvent(udidbEvent, contextIndex);
                break;
            case "PROCESS_EXIT":
                this._handleProcessExit(udidbEvent, contextIndex);
                break;
            case "PROCESS_CLEANUP":
                this._handleProcessCleanup(udidbEvent, contextIndex);
                break;
            default:
                console.log("Unhandled event: " + udidbEvent);
                break;
        }
    },

    _handleBreakpointEvent: function(udidbEvent, contextIndex) {
        if (contextIndex === null) {
            console.log("Could not determine context for breakpoint event");
            return;
        }
        this._updateLastResult(contextIndex, "Breakpoint hit at 0x" +
                               udidbEvent.eventData.address.toString(16));
    },

    _handleProcessExit: function(udidbEvent, contextIndex) {
        if (contextIndex === null) {
            console.log("Could not determine context for process exit event");
            return;
        }
        this._updateLastResult(contextIndex, "Process exiting with code = " +
                               udidbEvent.eventData.exitCode);
    },

    _handleProcessCleanup: function(udidbEvent, contextIndex) {
        if (contextIndex === null) {
            console.log("Could not determine context for process cleanup event");
            return;
        }
        this._removeContext(contextIndex);
    },

    _generateCreateResultHandler: function(operation) {
        return function(err, resp) {
            if (err) {
                this._openModal("Failed to create debuggee",
                                resp.body.exceptionName + ": " + resp.body.message,
                                this._closeModal.bind(this),
                                "Dismiss");
            }else{
                this._addContextFromApiModel(resp.body,
                                             function(resp) {
                                                 console.log("Failed to add context: " + resp);

                                                 if (resp instanceof Error) {
                                                     console.log(resp.stack);
                                                 }
                                             });
            }
        }.bind(this);
    },

    _generateOperationResultHandler: function(contextIndex) {
        return function(err, resp) {
            let result;
            if (err) {
                result = resp.body.exceptionName + ": " + resp.body.message;
            }else{
                let resultObj = resp.body.result;
                if (resultObj.description) {
                    result = resultObj.description;
                }else if (resultObj.value) {
                    result = resultObj.value;
                }else if (resultObj.eventPending) {
                    result = null;
                }else if (resultObj.rows) {
                    if (resultObj.columnHeaders && resultObj.columnHeaders.length > 0) {
                        result = resultObj.columnHeaders.join(' ') + "\n";
                    }else{
                        result = "";
                    }
                    resultObj.rows.forEach(function(row, index, array) {
                        result += row.columnValues.join(' ') + "\n";
                    });
                }else{
                    result = "No result";
                }
            }

            this._updateLastResult(contextIndex, result);
        }.bind(this);
    },

    _updateLastResult: function(contextIndex, result) {

        let newState;
        if (contextIndex < 0) {
            let globalContext = this.state.globalContext;
            let operationIndex = globalContext.history.operations.length-1;

            let newOperation = JSON.parse(JSON.stringify(globalContext.history.operations[operationIndex]));
            if (newOperation.result === null) {
                newOperation.result = result;

                newState = update(this.state, {
                    globalContext: {
                        history: {
                            operations: {
                                [operationIndex] : { $set: newOperation }
                            }
                        }
                    }
                });
                this.setState(newState);
            }
        }else{
            let currentContextIndex = this.state.currentContextIndex;
            if (this.state.contexts[currentContextIndex]) {
                let operationIndex = this.state.contexts[currentContextIndex].history.operations.length-1;

                let newOperation = JSON.parse(JSON.stringify(
                            this.state.contexts[currentContextIndex].history.operations[operationIndex]));

                if (newOperation.result === null) {
                    newOperation.result = result;

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
                    this.setState(newState);
                }
            }
        }
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
