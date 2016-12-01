import * as React from "react";
import {
    Panel,
    Grid,
    Row,
    Col
} from "react-bootstrap";

import * as ContextSelector from "./contextSelector";
import * as SourceViewer from "./sourceViewer";
import * as CommandLine from "./commandLine";
import * as StatusLine from "./statusLine";
import {
    UdidbRequest,
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
    readonly process: (request: UdidbRequest) => void;
}

export class Component extends React.Component<Props, {}> {

    render(): JSX.Element {
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

        return (
            <Grid fluid={true}>
                <Panel>
                    <Row className="contentRow">
                        <Col xs={3} className="contextSelector">
                            <ContextSelector.Component contexts={this.props.contexts}
                                globalContext={this.props.globalContext}
                                currentContextIndex={this.props.currentContextIndex}
                                process={this.props.process}/>
                        </Col>
                        <Col xs={8} className="contextPane">
                            <Row className="sourceViewerRow">
                                <SourceViewer.Component currentContext={currentContext}
                                    defaultSourceContent={this.props.defaultSourceContent}/>
                            </Row>
                            <Row className="statusLine">
                                <StatusLine.Component currentContext={currentContext}/>
                            </Row>
                            <Row className="commandLineRow">
                                <CommandLine.Component currentContext={currentContext}
                                    currentContextIndex={this.props.currentContextIndex}
                                    globalContext={this.props.globalContext}
                                    historyPrefs={this.props.prefs.history}
                                    process={this.props.process} />
                            </Row>
                        </Col>
                    </Row>
                    <Row className="footer">
                        <a target="_blank"
                            href="https://github.com/mcnulty/udidb">udidb</a>
                    </Row>
                </Panel>
            </Grid>
        );
    }
}
