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
        request.get(this.props.baseApiUri + "/debuggeeContexts/operations")
               .end((function(err, resp) {

                   let operationDescriptors;
                   if (err) {
                       operationDescriptors = [];
                   }else{
                       operationDescriptors = resp.body.elements;
                   }

                   let newState = update(this.state, {
                       globalContext: {
                           operationDescriptors: { $set: operationDescriptors }
                       }
                   });
                   this.setState(newState);
               }).bind(this));

        request.get(this.props.baseApiUri + "/debuggeeContexts")
               .end((function(err, resp) {

                   // TODO better error handling for this case
                   if (err) {
                       console.log("Failed to retrieve debuggee contexts: " + err);
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
                   }
               }).bind(this));
    },

    render: function() {
        return (
            <div>
                <Udidb {...this.state} process={this.process}/>
                <Modal show={this.state.modal.show} onHide={this._closeModal}>
                    <Modal.Header>
                        <h4>{this.state.modal.header}</h4>
                    </Modal.Header>
                    <Modal.Body>
                        {this.state.modal.body}
                    </Modal.Body>
                    <Modal.Footer>
                        <Button onClick={this._closeModal}>Dismiss</Button>
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
                body: { $set: "" }
            }
        }));
    },

    _openModal: function(header, body) {
        this.setState(update(this.state, {
            modal: {
                show: { $set: true },
                header: { $set: header },
                body: { $set: body },
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
        let fields = value.split(' ');
        let name;
        if (fields.length > 0) {
            name = fields[0];
        }else{
            name = "<unspecified>";
        }

        let foundOperation = false;
        let operation = null;
        if (operationDescriptors) {
            operationDescriptors.forEach(function(d, index, array) {
                if (d.name === name) {
                    foundOperation = true;

                    operation = {};
                    operation["name"] = d.name;

                    let operands = [];
                    for (let i = 1; i < fields.length; i++) {
                        let operandIndex = i-1;
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

        if (operation === null) {
            let resultMsg;
            if (!foundOperation) {
                resultMsg = "Operation not found, cannot execute";
            }else{
                resultMsg = "Invalid arguments for operation '" + name + "'";
            }

            operation = {
                name: name,
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

    _generateCreateResultHandler: function(operation) {
        return function(err, resp) {
            if (err) {
                this._openModal("Failed to create debuggee",
                                resp.body.exceptionName + ": " + resp.body.message);
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
                if (resp.body.result.description) {
                    result = resp.body.result.description;
                }else if (resp.body.result.value) {
                    result = resp.body.result.value;
                }else if (resp.body.result.eventPending) {
                    result = null;
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
        }else{
            let currentContextIndex = this.state.currentContextIndex;
            let operationIndex = this.state.contexts[currentContextIndex].history.operations.length-1;

            let newOperation = JSON.parse(JSON.stringify(
                this.state.contexts[currentContextIndex].history.operations[operationIndex]));

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
        }
        this.setState(newState);
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
