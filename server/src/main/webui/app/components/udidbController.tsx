import * as React from "react";
import {
    Modal,
    Button
} from "react-bootstrap";
import update = require("react-addons-update");
import autobahn = require("autobahn");
import client = require("superagent");
import * as Imm from "immutable";

import * as Udidb from "./udidb";
import {
    Thread,
    History,
    Operation,
    Operand,
    OperationDescription,
    OperandDescription,
    SourceLine,
    FileSourceLines,
    Context,
    ContextBuilder,
    ThreadModel,
    OperationModel,
    TableResultModel,
    TableRowModel,
    DebuggeeConfigModel,
    DebuggeeContextModel,
    OperationDescriptionModel,
    UdiEventModel,
    ResultModel,
    ValueResultModel,
    VoidResultModel,
    DeferredResultModel,
    ResultVisitor,
    UserPrefs,
    HistoryPrefs,
    UdidbRequest,
    PUT_METHOD,
    POST_METHOD
} from "./types";

class ModalState {
    readonly header: string;
    readonly show: boolean;
    readonly text: string;
    readonly body: string;
    readonly clickHandler: () => void;
    readonly buttonLabel: string;

    constructor(
        header: string,
        show: boolean,
        text: string,
        body: string,
        clickHandler: () => void,
        buttonLabel: string
    ) {
        this.header = header;
        this.show = show;
        this.text = text;
        this.body = body;
        this.clickHandler = clickHandler;
        this.buttonLabel = buttonLabel;
    }
}

export interface Props {
    readonly baseApiUri: string;
}

interface State extends Udidb.Props {
    readonly modal: ModalState;
    readonly eventConnection: autobahn.Connection;
    readonly eventSession: autobahn.Session;
}

let initialState: State;

export class Component extends React.Component<Props, State> {

    constructor(props: Props) {
        super(props);
        this.state = initialState;
    }

    public componentDidMount(): void {
        this.getInitialAPIData()
            .then(this.createUdidbEventListener)
            .catch(this.retryInitialData);
    }

    private createUdidbEventListener = (): void => {
        let eventConnection = new autobahn.Connection({
            url: "ws://localhost:8888/events",
            realm: "udidb"
        });

        this.setState(update(this.state, {
            eventConnection: {
                $set: eventConnection
            }
        }));

        eventConnection.onopen = (session: autobahn.Session, details: any) => {
            if (this.state.eventSession && this.state.eventSession.id !== session.id) {
                // Retry has been completed successfully
                this.closeModal();
                this.getInitialAPIData()
                    .catch(this.retryInitialData);
            }
            session.subscribe("com.udidb.events",
                this.udidbEventHandler.bind(this));
            this.setState(update(this.state, {
                eventSession: {
                    $set: session
                }
            }));
        };

        eventConnection.onclose = (reason: string, details: any): boolean => {
            console.log("Events connection closed: (" + reason + ", " + details + ")");
            this.openModal("UDIDB events connection unavailable",
                "Waiting for connection to be re-established...",
                () => {},
                null);
            return true;
        };

        eventConnection.open();
    }

    private retryInitialData = (error: any): void => {
        Component.defaultCatch("Failed to get initial API data")(error);
        this.openModal("UDIDB server unavailable",
            "Click Retry to connect again",
            () => {
                this.closeModal();
                this.getInitialAPIData()
                    .then(this.createUdidbEventListener)
                    .catch(this.retryInitialData);
            },
            "Retry");
    }

    private getInitialAPIData(): Promise<any> {
        // reset the state that will be retrieved
        this.setState(update(this.state, {
            globalContext: {
                $set: new ContextBuilder().build()
            },
            contexts: {
                $set: []
            },
            currentContextIndex: {
                $set: -1
            }
        }));

        return this.get("/debuggeeContexts/operations")
                   .then((resp: client.Response) => {
                let operationDescriptions = OperationDescriptionModel.fromJsonList(resp.body)
                        .map((value, index, array) => {
                            return OperationDescription.fromModel(value);
                        });

                let newState = update(this.state, {
                    globalContext: {
                        $set: ContextBuilder.fromContext(this.state.globalContext)
                                            .setOperationDescriptions(operationDescriptions)
                    }
                });
                this.setState(newState);

                return this.get("/debuggeeContexts");
            })
            .then((resp: client.Response): Promise<client.Response[]> => {
                return Promise.all(
                    DebuggeeContextModel.fromJsonList(resp.body)
                        .map((context: DebuggeeContextModel,
                                      index: number,
                                      array: DebuggeeContextModel[]): Promise<client.Response> => {

                            return this.addContextFromApiModel(context);
                        })
                );
            })
            .then((responses: client.Response[]): Promise<any> => {
                return Promise.resolve(null);
            })
            .catch(this.retryInitialData);
    }

    public render(): JSX.Element {
        let footerContent: JSX.Element;
        if (this.state.modal.buttonLabel) {
            footerContent = <Button onClick={this.state.modal.clickHandler}>{this.state.modal.buttonLabel}</Button>;
        } else {
            footerContent = <div></div>;
        }
        return (
            <div>
                <Udidb.Component {...this.state} process={this.process.bind(this)} />
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
    }

    private closeModal(): void {
        this.setState(update(this.state, {
            modal: {
                $set: new ModalState(
                    "",
                    false,
                    "",
                    "",
                    () => {},
                    ""
                )
            }
        }));
    }

    private openModal(header: string, body: string, clickHandler: () => void, buttonLabel: string): void {
        this.setState(update(this.state, {
            modal: {
                $set: new ModalState(
                    header,
                    true,
                    "",
                    body,
                    clickHandler,
                    buttonLabel)
            }
        }));
    }

    private process(request: UdidbRequest): void {
        switch (request.method) {
            case POST_METHOD:
                this.processPost(request);
                break;
            case PUT_METHOD:
                this.processPut(request);
                break;
            default:
                console.log("Request with unknown method: " + request);
                break;
        }
    }

    private processPost(request: UdidbRequest): void {
        switch (request.path) {
            case "currentContext.operation":
                this.submitOperation(request.value);
                break;
            default:
                console.log("POST with unknown path: " + request);
                break;
        }
    }

    private processPut(request: UdidbRequest): void {
        switch (request.path) {
            case "currentContextIndex":
                this.selectContext(parseInt(request.value, 10));
                break;
            case "currentContext.activeThreadIndex":
                this.selectActiveThread(parseInt(request.value, 10));
                break;
            default:
                console.log("PUT with unknown path: " + request);
                break;
        }
    }

    private createOperation(value: string, operationDescriptions: ReadonlyArray<OperationDescription>): Operation {

        let foundOperation = false;
        let operation: Operation = null;
        if (operationDescriptions) {
            let newOp: any = {};

            let fields = value.split(" ");
            for (let nameIndex = 0; nameIndex < fields.length; nameIndex++) {
                let name = fields.slice(0, nameIndex + 1).join(" ");

                operationDescriptions.forEach((d, index, arrary) => {
                    if (d.name === name) {
                        foundOperation = true;

                        newOp.name = d.name;

                        let operands: Operand[] = [];
                        for (let i = nameIndex + 1; i < fields.length; i++) {
                            let operandIndex = i - (nameIndex + 1);
                            if (operandIndex >= d.operandDescriptions.length) {
                                // The specified arguments are invalid
                                operation = null;
                                break;
                            }

                            let operandDescription = d.operandDescriptions[operandIndex];
                            if (operandDescription.type === "list") {
                                operands.push({
                                    name: operandDescription.name,
                                    type: operandDescription.type,
                                    value: fields.splice(i)
                                });
                                break;
                            } else {
                                operands.push({
                                    name: operandDescription.name,
                                    type: operandDescription.type,
                                    value: fields[i]
                                });
                            }
                        }
                        newOp.operands = operands;

                        if (newOp.name === "create") {
                            newOp.result = "Creating debuggee...";
                        } else {
                            newOp.result = null;
                        }
                    }
                });
            }

            operation = new Operation(
                newOp.name,
                newOp.operands,
                newOp.result);
        }

        if (operation === null) {
            let resultMsg: string;
            if (!foundOperation) {
                resultMsg = "Operation not found, cannot execute";
            } else {
                resultMsg = "Invalid arguments for operation '" + name + "'";
            }

            operation = {
                name: "<unknown>",
                operands: [],
                result: resultMsg
            };
        }

        return operation;
    }

    private createDebuggeeConfigModel(operation: Operation): DebuggeeConfigModel {
        let config = new DebuggeeConfigModel();
        operation.operands.forEach((operand, index, array) => {
            switch (operand.name) {
                case "execPath":
                    config.execPath = operand.value;
                    break;
                case "args":
                    config.args = operand.value;
                    break;
                case "env":
                    config.env = operand.value;
                    break;
                default:
                    break;
            }
        });
        return config;
    }

    private submitOperation(value: string): void {
        let currentContextIndex = this.state.currentContextIndex;

        let operationDescriptions: ReadonlyArray<OperationDescription>;
        if (currentContextIndex >= 0) {
            operationDescriptions = this.state.contexts[currentContextIndex].operationDescriptions;
        } else {
            operationDescriptions = this.state.globalContext.operationDescriptions;
        }

        let operation = this.createOperation(value, operationDescriptions);

        let newState: State;
        if (currentContextIndex >= 0) {
            let contexts = this.state.contexts.slice(0);

            let context = contexts[currentContextIndex];
            let operations = context.history.operations.slice(0);
            operations.push(operation);
            let history = new History(context.history.baseIndex,
                                      operations);

            contexts[currentContextIndex] = ContextBuilder.fromContext(context)
                                                          .setHistory(history)
                                                          .build();

            newState = update(this.state, {
                contexts: {
                    $set: contexts
                }
            });
        } else {

            let operations = this.state.globalContext.history.operations.slice(0);
            operations.push(operation);
            let history = new History(this.state.globalContext.history.baseIndex,
                                      operations);

            let globalContext = ContextBuilder.fromContext(this.state.globalContext)
                                              .setHistory(history)
                                              .build();

            newState = update(this.state, {
                globalContext: {
                    $set: globalContext
                }
            });
        }
        this.setState(newState);

        if (operation.result === null || operation.name === "create") {
            this.sendOperation(currentContextIndex, operation);
        }
    }

    private sendOperation(contextIndex: number, operation: Operation): void {

        switch (operation.name) {
            case "create":
                this.handleCreateResult(operation,
                    this.post("/debuggeeContexts", this.createDebuggeeConfigModel(operation)));
                break;
            default:
                if (contextIndex >= 0) {
                    let contextId = this.state.contexts[contextIndex].id;
                    this.handleOperationResult(contextIndex,
                        this.post("/debuggeeContexts/" + contextId + "/process/operation",
                            OperationModel.fromOperation(operation))
                    );
                } else {
                    this.handleOperationResult(contextIndex,
                        this.post("/debuggeeContexts/globalOperation",
                            OperationModel.fromOperation(operation))
                    );
                }
                break;
        }
    }

    private addContextFromApiModel(context: DebuggeeContextModel): Promise<client.Response> {
        let opDescs: OperationDescription[] = [];
        let newContext = new ContextBuilder();
        newContext.setId(context.id);
        newContext.setHistory(new History(0, []));
        newContext.setActiveThreadIndex(0);

        let operationUri = "/debuggeeContexts/" + context.id + "/process/operation";
        return this.get("/debuggeeContexts/" + context.id + "/process")
            .then( (resp: client.Response): Promise<client.Response> => {
                newContext.setProcessId(resp.body.pid as string);
                return this.get("/debuggeeContexts/" + context.id + "/process/threads");
            })
            .then( (resp: client.Response): Promise<client.Response> => {
                newContext.setThreads(ThreadModel.fromJsonList(resp.body)
                                                 .map( (value: ThreadModel,
                                                        index: number,
                                                        array: ThreadModel[]
                                                       ) => {
                                                           return Thread.fromModel(value);
                                                       }));
                return this.get("/debuggeeContexts/" + context.id + "/process/operations");
            })
            .then( (resp: client.Response): Promise<client.Response> => {
                opDescs = OperationDescriptionModel.fromJsonList(resp.body)
                    .map( 
                        (value: OperationDescriptionModel,
                         index: number,
                         array: OperationDescriptionModel[]): OperationDescription => {
                            return OperationDescription.fromModel(value);
                        });
                newContext.setOperationDescriptions(opDescs);
                return this.post(operationUri,
                    OperationModel.fromOperation(this.createOperation("source files", opDescs))
                );
            })
            .then( (resp: client.Response): Promise<Imm.Map<string, FileSourceLines>[]> => {
                // Create a list of promises to get all the source information for all the files
                let table = TableResultModel.fromResultModel(OperationModel.fromJson(resp.body).result);
                return Promise.all(
                    table.rows.map( (fileRow: TableRowModel, index: number, array: TableRowModel[]) => {
                        return this.post(operationUri,
                            OperationModel.fromOperation(this.createOperation(
                                "source lines 0:0 " + fileRow.columnValues[0], opDescs)
                            )
                        ).then( (resp: client.Response): Promise<Imm.Map<string, FileSourceLines>> => {
                            let table = TableResultModel.fromResultModel(OperationModel.fromJson(resp.body).result);

                            let sourceLines = table.rows.map(
                                (lineRow: TableRowModel, index: number, array: TableRowModel[]) => {
                                    return lineRow.columnValues[1];
                                }
                            );

                            let sourceMap = Imm.Map<string, FileSourceLines>();

                            return Promise.resolve(sourceMap.set(
                                fileRow.columnValues[0],
                                new FileSourceLines(1, sourceLines)
                            ));
                        });
                    })
                );
            })
            .then( (sourceMapEntries: Imm.Map<string, FileSourceLines>[]): Promise<Thread[]> => {
                let sourceMap = Imm.Map<string, FileSourceLines>();
                newContext.setSourceMap(sourceMap.merge(...sourceMapEntries));

                return Promise.all(
                    newContext.getThreads().map((thread: Thread, index: number, array: Thread[]): Promise<Thread> => {
                        if (thread.pc && thread.pc !== "0") {
                            return this.post(operationUri,
                                OperationModel.fromOperation(
                                    this.createOperation("source addr2line 0x" + thread.pc, opDescs)
                                )
                            )
                            .then( (resp: client.Response) => {
                                let table = TableResultModel.fromResultModel(OperationModel.fromJson(resp.body).result);

                                let newThread = new Thread(
                                    thread.id,
                                    thread.pc,
                                    SourceLine.fromAddr2LineTable(table)
                                );

                                return Promise.resolve(newThread);
                            });
                        }else{
                            return Promise.resolve(thread);
                        }
                    })
                );
            })
            .then((threads: Thread[]): Promise<any> => {
                newContext.setThreads(threads);

                let newContexts = this.state.contexts.slice(0);
                newContexts.push(newContext.build());

                let newState = update(this.state, {
                    contexts: {
                        $set: newContexts
                    },
                    currentContextIndex: {
                        $apply: function(x) {
                            if (x === -1) {
                                return 0;
                            }
                            return x + 1;
                        }
                    }
                });
                this.setState(newState);
                return Promise.resolve(null);
            });
    }

    private post(url: string, payload: any): Promise<client.Response> {
        return new Promise(function(
            resolve: (result: client.Response) => void,
            reject: (result: client.Response) => void
        ) {
            client.post(this.props.baseApiUri + url)
                .send(payload)
                .end(function(err: any, resp: client.Response) {
                    if (err) {
                        reject(resp);
                    } else {
                        resolve(resp);
                    }
                });
        }.bind(this));
    }

    private get(url: string): Promise<client.Response> {
        return new Promise(function(
            resolve: (result: client.Response) => void,
            reject: (result: client.Response) => void
        ) {
            client
                .get(this.props.baseApiUri + url)
                .end(function(err: any, resp: client.Response) {
                    if (err) {
                        reject(resp);
                    } else {
                        resolve(resp);
                    }
                });
        }.bind(this));
    }

    private removeContext(contextIndex: number): void {
        let newContexts: Context[];
        if (contextIndex === 0) {
            newContexts = [];
        }else{
            newContexts = this.state.contexts.slice(0, contextIndex - 1).concat(
                this.state.contexts.slice(contextIndex + 1));
        }
        let newState = update(this.state, {
            contexts: {
                $set: newContexts
            },
            currentContextIndex: {
                $set: -1
            }
        });
        this.setState(newState);
    }

    private updateThreadSourceInfo(contextIndex: number, tid: string, pc: string): void {
        let context = this.state.contexts[contextIndex];

        let threadIndex = -1;
        context.threads.forEach(function(value: Thread, index: number, array: ReadonlyArray<Thread>) {
            if (value.id === tid) {
                threadIndex = index;
            }
        });
        this.post("/debuggeeContexts/" + context.id + "/process/operation",
            OperationModel.fromOperation(this.createOperation(
                "source addr2line 0x" + pc, context.operationDescriptions)))
            .then( (resp: client.Response) => {
                let contexts = this.state.contexts.slice(0);
                let context = contexts[contextIndex];

                let threads = context.threads.slice(0);
                let thread = threads[threadIndex];
                let table = TableResultModel.fromResultModel(OperationModel.fromJson(resp.body).result);
                thread = new Thread(thread.id, pc, SourceLine.fromAddr2LineTable(table));

                contexts[contextIndex] = ContextBuilder.fromContext(context)
                                                       .setThreads(threads)
                                                       .build();

                let newState = update(this.state, {
                    contexts: {
                        $set: contexts
                    }
                });
                this.setState(newState);
            })
            .catch(Component.defaultCatch("Failed to retrieve source info for thread " + tid + " in context "
                   + contextIndex));
    }

    private udidbEventHandler(args: any[]): void {
        let udidbEvent = UdiEventModel.fromJson(args[0]);
        let contextIndex: number;
        if (udidbEvent.contextId) {
            contextIndex = -1;
            this.state.contexts.forEach(function(value: Context, index: number, array: ReadonlyArray<Context>){
                if (value.id === udidbEvent.contextId) {
                    contextIndex = index;
                }
            });
            if (contextIndex < 0) {
                contextIndex = -1;
            }
        } else {
            console.log("Could not determine context for event: " + JSON.stringify(args[0]));
            return;
        }

        if (udidbEvent.intermediateEvent) {
            // TODO issue a request for the latest operation data for this context
        }

        switch (udidbEvent.eventType) {
            case "BREAKPOINT":
                this.handleBreakpointEvent(udidbEvent, contextIndex);
                break;
            case "PROCESS_EXIT":
                this.handleProcessExit(udidbEvent, contextIndex);
                break;
            case "PROCESS_CLEANUP":
                this.handleProcessCleanup(udidbEvent, contextIndex);
                break;
            default:
                console.log("Unhandled event: " + udidbEvent);
                break;
        }
    }

    private handleBreakpointEvent(udidbEvent: UdiEventModel, contextIndex: number): void {
        let pc = udidbEvent.eventData.address.toString(16);
        this.updateLastResult(contextIndex, "Breakpoint hit at 0x" + pc);
        this.updateThreadSourceInfo(contextIndex, udidbEvent.tid, pc);
    }

    private handleProcessExit(udidbEvent: UdiEventModel, contextIndex: number): void {
        this.updateLastResult(contextIndex, "Process exiting with code = " +
            udidbEvent.eventData.exitCode);
    }

    private handleProcessCleanup(udidbEvent: UdiEventModel, contextIndex: number): void {
        this.removeContext(contextIndex);
    }

    private static defaultCatch(message: string): (err: any) => void {
        return (err: any) => {
            console.log(message + ": " + JSON.stringify(err));

            if (err instanceof Error) {
                console.log(err.stack);
            }
        }
    }

    private handleCreateResult(operation: Operation, result: Promise<client.Response>): void {
        result
        .then( (resp: client.Response): Promise<client.Response> => {
            return this.addContextFromApiModel(resp.body);
        })
        .catch(Component.defaultCatch("Failed to add context"));
    }

    private handleOperationResult(contextIndex: number, promise: Promise<client.Response>): void {

        promise
        .then( (resp: client.Response) => {
            let resultModel = resp.body.result as ResultModel;
            this.updateLastResult(contextIndex, ResultVisitor.accept<string>(new DefaultResultVisitor(), resultModel));
            return Promise.resolve(null);
        })
        .catch( (err: any) => {
            this.updateLastResult(contextIndex, err.body.exceptionName + ": " + err.body.message)
        });
    }

    private updateLastResult(contextIndex: number, result: string): void {

        if (contextIndex < 0) {
            let globalContext = this.state.globalContext;
            let operationIndex = globalContext.history.operations.length - 1;

            let operation = globalContext.history.operations[operationIndex];
            if (operation.result === null) {
                let operations = globalContext.history.operations.slice(0);
                operations[operationIndex] = new Operation(
                    operation.name,
                    operation.operands,
                    result
                );
                let history = new History(
                    globalContext.history.baseIndex,
                    operations
                );

                let newGlobalContext = ContextBuilder.fromContext(globalContext)
                                                     .setHistory(history)
                                                     .build();
                let newState = update(this.state, {
                    globalContext: {
                        $set: newGlobalContext
                    }
                });
                this.setState(newState);
            }
        } else {
            let currentContextIndex = this.state.currentContextIndex;
            let context = this.state.contexts[currentContextIndex];
            if (context) {
                let operationIndex = context.history.operations.length - 1;
                let operation = context.history.operations[context.history.operations.length - 1];

                if (operation.result === null) {
                    let operations = context.history.operations.slice(0);
                    operations[operationIndex] = new Operation(
                        operation.name,
                        operation.operands,
                        result
                    );

                    let history = new History(
                        context.history.baseIndex,
                        operations
                    );

                    let contexts = this.state.contexts.slice(0);
                    contexts[currentContextIndex] = ContextBuilder.fromContext(context)
                                                                  .setHistory(history)
                                                                  .build();

                    let newState = update(this.state, {
                        contexts: {
                            $set: contexts
                        }
                    });
                    this.setState(newState);
                }
            }
        }
    }

    private selectContext(index: number): void {
        let newState = update(this.state, {
            currentContextIndex: {
                $set: index
            }
        });
        this.setState(newState);
    }

    private selectActiveThread(index: number): void {
        let currentContextIndex = this.state.currentContextIndex;
        let contexts = this.state.contexts.slice(0);

        contexts[currentContextIndex] = ContextBuilder.fromContext(contexts[currentContextIndex])
                                                      .setActiveThreadIndex(index)
                                                      .build();

        let newState = update(this.state, {
            contexts: {
                $set: contexts
            }
        });
    }
}

class DefaultResultVisitor implements ResultVisitor<string> {
    visitDeferred(result: DeferredResultModel): string {
        return null;
    }

    visitTable(result: TableResultModel): string {
        return TableResultModel.format(result);
    }

    visitValue(result: ValueResultModel): string {
        if (result.description.length > 0) {
            return result.description;
        } else {
            return result.value;
        }
    }

    visitVoid(result: VoidResultModel): string {
        if (result.eventPending) {
            return null;
        } else {
            return "";
        }
    }
}


initialState = {
    contexts: [],

    currentContextIndex: -1,

    prefs: new UserPrefs(new HistoryPrefs(3)),

    defaultSourceContent: [
        " ",
        "udidb - UDI debugger",
        " ",
        " ",
        " ",
        " ",
        "Create processes with the 'create' operation.",
        " ",
        "Enter 'help' at the prompt for a list of available operations.",
        " ",
        " ",
        " ",
        " ",
        " ",
        " ",
        " ",
        " ",
        " ",
        " ",
    ],

    globalContext: new ContextBuilder().build(),

    modal: new ModalState(
        "",
        false,
        "",
        "",
        function() {},
        ""
    ),

    eventConnection: null,
    eventSession: null,
    process: function(request: UdidbRequest) {}
};
