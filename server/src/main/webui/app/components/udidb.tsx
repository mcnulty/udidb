import * as React from "react";
import {
    Panel,
    Grid,
    Row,
    Col
} from "react-bootstrap";
import * as Measure from 'react-measure';

import * as ContextSelector from "./contextSelector";
import * as SourceViewer from "./sourceViewer";
import * as CommandLine from "./commandLine";
import * as StatusLine from "./statusLine";
import {
    UdidbRequest,
    PUT_METHOD,
    Context,
    History,
    UserPrefs
} from "./types";

export interface Props {
    readonly contexts: ReadonlyArray<Context>;
    readonly globalContext: Context;
    readonly currentContextIndex: number;
    readonly prefs: UserPrefs;
    readonly defaultSourceContent: ReadonlyArray<string>;
    readonly sourceViewerHeight: string;
    readonly process: (request: UdidbRequest) => void;
}

export class Component extends React.Component<Props, {}> {

    private onMeasureDimensionChange(dims: Measure.Dimensions): void {
        this.props.process(new UdidbRequest(PUT_METHOD, "commandLine.height", "" + dims.height));
    }

    public render(): JSX.Element {
        let currentContext: Context;
        if (this.props.contexts.length > 0 && this.props.currentContextIndex >= 0) {
            if (this.props.currentContextIndex >= this.props.contexts.length) {
                console.log("Invalid context index: " + this.props.currentContextIndex);
                currentContext = this.props.contexts[0];
            } else {
                currentContext = this.props.contexts[this.props.currentContextIndex];
            }
        } else {
            currentContext = null;
        }

        let mainPaneStyle = {
            paddingLeft: "256px",
            minHeight: "100vh"
        };

        let leftPaneStyle = {
            position: "fixed",
            minHeight: "100vh",
            top: 0,
            left: 0,
            maxWidth: "256px",
            minWidth: "256px"
        };

        return (
            <div>
                <div style={ mainPaneStyle }>
                    <Measure whitelist={ [ "height" ]} onMeasure={this.onMeasureDimensionChange.bind(this)}>
                        <div>
                            <CommandLine.Component currentContext={currentContext}
                                currentContextIndex={this.props.currentContextIndex}
                                globalContext={this.props.globalContext}
                                process={this.props.process} />
                            <StatusLine.Component currentContext={currentContext}/>
                        </div>
                    </Measure>
                    <SourceViewer.Component currentContext={currentContext}
                                            height={this.props.sourceViewerHeight}
                                            defaultSourceContent={this.props.defaultSourceContent}/>
                </div>
                <div style={ leftPaneStyle }>
                    <ContextSelector.Component contexts={this.props.contexts}
                                               globalContext={this.props.globalContext}
                                               currentContextIndex={this.props.currentContextIndex}
                                               process={this.props.process}/>
                </div>
            </div>
        );
    }
}
