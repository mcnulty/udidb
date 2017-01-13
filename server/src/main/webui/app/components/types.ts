import * as Imm from "immutable";

export class SourceLine {
    readonly line: number;
    readonly file: string;

    constructor(line: number, file: string) {
        this.line = line;
        this.file = file;
    }

    static fromAddr2LineTable(table: TableResultModel) {
        return new SourceLine(parseInt(table.rows[0].columnValues[1], 10),
                              table.rows[0].columnValues[0]);
    }
}

export class Thread {
    readonly id: string;
    readonly pc: string;
    readonly source: SourceLine;

    constructor(id: string, pc: string, source: SourceLine) {
        this.id = id;
        this.pc = pc;
        this.source = source;
    }

    static fromModel(model: ThreadModel): Thread {
        return new Thread(model.id, model.pc, null);
    }
}

export class Operand {
    readonly name: string;
    readonly type: string;
    readonly value: any;

    constructor(name: string, type: string, value: any) {
        this.name = name;
        this.type = type;
        this.value = value;
    }
}

export class Operation {
    readonly name: string;
    readonly operands: ReadonlyArray<Operand>;
    readonly result: string;

    constructor(name: string,
                operands: ReadonlyArray<Operand>,
                result: string) {
        this.name = name;
        this.operands = operands;
        this.result = result;
    }
}

export class OperandDescription {
    readonly name: string;
    readonly type: string;

    constructor(name: string, type: string) {
        this.name = name;
        this.type = type;
    }
}

export class OperationDescription {
    readonly name: string;
    readonly operandDescriptions: ReadonlyArray<OperandDescription>;

    constructor(name: string, operandDescriptions: ReadonlyArray<OperandDescription>) {
        this.name = name;
        this.operandDescriptions = operandDescriptions;
    }

    static fromModel(model: OperationDescriptionModel): OperationDescription {
        return new OperationDescription(model.name, model.operandDescriptions);
    }
}

export class History {
    readonly baseIndex: number;
    readonly operations: ReadonlyArray<Operation>;

    constructor(baseIndex: number, operations: Operation[]) {
        this.baseIndex = baseIndex;
        this.operations = operations.slice(0);
    }
}

export class FileSourceLines {
    readonly startLineNo: number;
    readonly lines: ReadonlyArray<string>;

    constructor(startLineNo: number, lines: ReadonlyArray<string>) {
        this.startLineNo = startLineNo;
        this.lines = lines;
    }
}

export class Context {
    readonly id: string;
    readonly processId: string;
    readonly activeThreadIndex: number;
    readonly threads: ReadonlyArray<Thread>;
    readonly history: History;
    readonly sourceMap: Imm.Map<string, FileSourceLines>;
    readonly operationDescriptions: ReadonlyArray<OperationDescription>;

    constructor(
        id: string,
        processId: string,
        activeThreadIndex: number,
        threads: ReadonlyArray<Thread>,
        history: History,
        sourceMap: Imm.Map<string, FileSourceLines>,
        operationDescriptions: ReadonlyArray<OperationDescription>
    ) {
        this.id = id;
        this.processId = processId;
        this.activeThreadIndex = activeThreadIndex;
        this.threads = threads;
        this.history = history;
        this.sourceMap = sourceMap;
        this.operationDescriptions = operationDescriptions;
    }
}

export class ContextBuilder {

    private id: string = "";
    private processId: string = "";
    private activeThreadIndex: number = -1;
    private threads: ReadonlyArray<Thread> = [];
    private history: History = new History(0, []);
    private sourceMap: Imm.Map<string, FileSourceLines> = Imm.Map<string, FileSourceLines>();
    private operationDescriptions: ReadonlyArray<OperationDescription> = [];

    constructor() {
    }

    static fromContext(context: Context): ContextBuilder {
        let builder = new ContextBuilder();

        builder.id = context.id;
        builder.processId = context.processId;
        builder.activeThreadIndex = context.activeThreadIndex;
        builder.threads = context.threads;
        builder.history = context.history;
        builder.sourceMap = context.sourceMap;
        builder.operationDescriptions = context.operationDescriptions;

        return builder;
    }

    setId(id: string): ContextBuilder {
        this.id = id;
        return this;
    }

    setProcessId(processId: string): ContextBuilder {
        this.processId = processId;
        return this;
    }

    setActiveThreadIndex(activeThreadIndex: number): ContextBuilder {
        this.activeThreadIndex = activeThreadIndex;
        return this;
    }

    setThreads(threads: Thread[]): ContextBuilder {
        this.threads = threads.slice(0);
        return this;
    }

    getThreads(): ReadonlyArray<Thread> {
        return this.threads;
    }

    setHistory(history: History): ContextBuilder {
        this.history = history;
        return this;
    }

    setSourceMap(sourceMap: Imm.Map<string, FileSourceLines>): ContextBuilder {
        this.sourceMap = sourceMap;
        return this;
    }

    setOperationDescriptions(operationDescriptions: OperationDescription[]): ContextBuilder {
        this.operationDescriptions = operationDescriptions.slice(0);
        return this;
    }

    build(): Context {
        return new Context(
            this.id,
            this.processId,
            this.activeThreadIndex,
            this.threads,
            this.history,
            this.sourceMap,
            this.operationDescriptions
        );
    }
}

export class UserPrefs {
}

// State Update Requests

export const PUT_METHOD = "PUT";
export const POST_METHOD = "POST";

export class UdidbRequest {
    readonly method: string;
    readonly path: string;
    readonly value: string;

    constructor(method: string, path: string, value: string) {
        this.method = method;
        this.path = path;
        this.value = value;
    }

    toString() {
        return this.method + " " + this.path + " '" + this.value + "'";
    }
}

// API Models

export interface ResultModel {
    typeName: string;
}

export class DeferredResultModel implements ResultModel {
    static readonly TYPE_NAME = "DeferredResult";

    typeName: string;
    eventPending: boolean;
}

export interface TableRowModel {
    columnValues: string[];
}

export class TableResultModel implements ResultModel {
    static readonly TYPE_NAME = "TableResult";

    typeName: string;
    eventPending: boolean;
    columnHeaders: string[];
    rows: TableRowModel[];

    static fromResultModel(result: ResultModel): TableResultModel {
        if (result.typeName !== TableResultModel.TYPE_NAME) {
            throw new TypeError("Unexpected result type: " + result.typeName);
        } else {
            return result as TableResultModel;
        }
    }

    static format(table: TableResultModel): string {
        let result = "";

        if (table.columnHeaders.length > 0) {
            for (let i = 0; i < table.columnHeaders.length; i++) {
                result += table.columnHeaders[i];
                if (i < table.columnHeaders.length - 1) {
                    result += " "
                }
            }
            result += "\n";
        }

        if (table.rows.length > 0) {
            for (let i = 0; i < table.rows.length; i++) {
                for (let j = 0; j < table.rows[i].columnValues.length; j++) {
                    result += table.rows[i].columnValues[j];
                    if (j < table.rows[i].columnValues.length - 1) {
                        result += " ";
                    }
                }
                result += "\n";
            }
        }

        return result;
    }
}

export class ValueResultModel implements ResultModel {
    static readonly TYPE_NAME = "ValueResult";

    typeName: string;
    eventPending: boolean;
    description: string;
    value: any;
}

export class VoidResultModel implements ResultModel {
    static readonly TYPE_NAME = "VoidResult";

    typeName: string;
    eventPending: boolean;
    noExpectedResult: boolean;
}

export class ResultVisitor<T> {

    visitDeferred(result: DeferredResultModel): T {
        return null;
    }

    visitTable(result: TableResultModel): T {
        return null;
    }

    visitValue(result: ValueResultModel): T {
        return null;
    }

    visitVoid(result: VoidResultModel): T {
        return null;
    }

    static accept<T>(visitor: ResultVisitor<T>, result: ResultModel): T {
        switch (result.typeName) {
            case DeferredResultModel.TYPE_NAME:
                return visitor.visitDeferred(result as DeferredResultModel);
            case TableResultModel.TYPE_NAME:
                return visitor.visitTable(result as TableResultModel);
            case ValueResultModel.TYPE_NAME:
                return visitor.visitValue(result as ValueResultModel);
            case VoidResultModel.TYPE_NAME:
                return visitor.visitVoid(result as VoidResultModel);
            default:
                throw new TypeError("Unexpected result type: " + result.typeName);
        }
    }
}

export class OperationModel {
    name: string;
    operands: any;
    pending: boolean;
    result: ResultModel;

    static fromJson(input: any): OperationModel {
        return input as OperationModel;
    }

    static fromJsonList(input: any): OperationModel[] {
        return input.elements as OperationModel[];
    }

    static fromOperation(operation: Operation): OperationModel {
        let model = new OperationModel();
        model.result = null;
        model.name = operation.name;
        model.operands = {};
        model.pending = true;
        operation.operands.forEach(function(operand: Operand, index: number, array: Operand[]) {
            model.operands[operand.name] = operand.value;
        });
        return model;
    }
}

export class DebuggeeConfigModel {
    execPath: string;
    args: string[];
    env: Map<string, string>;

    static fromJson(input: any): DebuggeeConfigModel {
        return input as DebuggeeConfigModel;
    }

    static fromJsonList(input: any): DebuggeeConfigModel[] {
        return input.elements as DebuggeeConfigModel[];
    }
}

export class DebuggeeContextModel {
    id: string;
    execPath: string;
    args: string[];

    static fromJson(input: any): DebuggeeContextModel {
        return input as DebuggeeContextModel;
    }

    static fromJsonList(input: any): DebuggeeContextModel[] {
        return input.elements as DebuggeeContextModel[];
    }
}

export class ErrorModel {
    message: string;
    exceptionName: string;

    static fromJson(input: any): ErrorModel {
        return input as ErrorModel;
    }
}

export class OperandDescriptionModel {
    name: string;
    type: string

    static fromJson(input: any): OperandDescriptionModel {
        return input as OperandDescriptionModel;
    }

    static fromJsonList(input: any): OperandDescriptionModel[] {
        return input.elements as OperandDescriptionModel[];
    }
}

export class OperationDescriptionModel {
    name: string;
    operandDescriptions: OperandDescriptionModel[];

    static fromJson(input: any): OperationDescriptionModel {
        return input as OperationDescriptionModel;
    }

    static fromJsonList(input: any): OperationDescriptionModel[] {
        return input.elements as OperationDescriptionModel[];
    }
}

export class ProcessModel {
    pid: string;
    running: boolean;

    static fromJson(input: any): ProcessModel {
        return input as ProcessModel;
    }
}

export class ThreadModel {
    id: string;
    pc: string;

    static fromJson(input: any): ThreadModel {
        return input as ThreadModel;
    }

    static fromJsonList(input: any): ThreadModel[] {
        return input.elements as ThreadModel[];
    }
}

export class UdiEventModel {
    contextId: string;
    pid: string;
    tid: string;
    eventType: string;
    eventData: any;
    intermediateEvent: boolean;

    static fromJson(input: any): UdiEventModel {
        let result = input as UdiEventModel;
        if ("intermediateEvent" in input) {
            result.intermediateEvent = input.intermediateEvent;
        }else{
            result.intermediateEvent = false;
        }
        return result;
    }
}
