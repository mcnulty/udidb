/** 
 * The styling is currently hard-coded. It would be nice to let the user
 * select from some hljs themes eventually.
 */

import * as React from "react";
import hljs = require("highlight.js");

import {
    Context,
} from "./types";

let topLevelStyle = {
    fontFamily: "monospace",
    whiteSpace: "pre",
    border: "1px solid rgb(204, 204, 204)",
    borderRadius: "4px",
    padding: "5px 0px 5px 0px",
};

let placeholderStyle = Object.assign({
    textAlign: "center"
}, topLevelStyle);

let noSourceContent: string[];

export interface Props {
    readonly currentContext: Context;
    readonly defaultSourceContent: ReadonlyArray<string>;
}

export class Component extends React.Component<Props, {}> {

    public render(): JSX.Element {
        if (this.props.currentContext === null) {
            return this.renderPlaceholderContent(this.props.defaultSourceContent);
        } else {
            return this._renderContent();
        }
    }

    private renderPlaceholderContent(content: ReadonlyArray<string>) {
        let rows = content.map(function(line, index, array) {
            return <tr key={"defaultSourceContent-" + index}><td>{line}</td></tr>;
        });

        return (
            <div className="hljs" style={placeholderStyle}>
                <table style={{ width: "100%" }}>
                    <tbody>{rows}</tbody>
                </table>
            </div>
        );
    }

    _renderContent() {
        let currentContext = this.props.currentContext;
        let sourceContext = currentContext.threads[currentContext.activeThreadIndex].source;

        if (!sourceContext || !currentContext.sourceMap || !(currentContext.sourceMap.has(sourceContext.file))) {
            return this.renderPlaceholderContent(noSourceContent);
        }

        let source = currentContext.sourceMap.get(sourceContext.file).lines.join("\n");

        let lineStart = currentContext.sourceMap.get(sourceContext.file).startLineNo;
        let activeLineNo = sourceContext.line;

        let highlightedSourceObj = hljs.highlightAuto(source, ["C"]);

        let numberedSourceRows = highlightedSourceObj.value.split("\n").map(
            function(line: string, index: number, array: string[]) {
                let currentLineNo = index + lineStart;

                let markedUpLine: string;
                if (activeLineNo === currentLineNo) {
                    markedUpLine = "<td style=\"background-color: rgb(10,81,99); width: 100%;\">" +
                        line +
                        "</td>";
                } else {
                    markedUpLine = "<td style=\"width: 100%;\">" + line + "</td>";
                }

                let lineNoCell = "<td style=\"" +
                    "background-color: rgb(7,54,66);" +
                    "border:1px solid rgb(7,54,66);" +
                    "color: rgb(131, 148, 150);" +
                    "padding-left: 5px;" +
                    "padding-right: 5px;" +
                    "\">"
                    + currentLineNo + "</td>";

                return "<tr>" + lineNoCell + markedUpLine + "</tr>";
            }).join("");

        return (
            <div className="hljs" style={topLevelStyle}>
                <table style={{ width: "100%" }}>
                    <tbody dangerouslySetInnerHTML={{ __html: numberedSourceRows }} />
                </table>
            </div>
        );
    }
}

noSourceContent = [
    " ",
    "udidb - UDI debugger",
    " ",
    " ",
    " ",
    " ",
    "No source information available",
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
    " ",
    " ",
];
