import React from "react"
import { Panel, Grid, Row, Col } from "react-bootstrap";

import ContextSelector from "./contextSelector.js"
import SourceViewer from "./sourceViewer.js"
import CommandLine from "./commandLine.js"
import StatusLine from "./statusLine.js"

export default React.createClass({

    propTypes: {

        /** Representation of all the available contexts */
        contexts: React.PropTypes.arrayOf(React.PropTypes.shape({
            id: React.PropTypes.string,
            processId: React.PropTypes.string,
            activeThreadIndex: React.PropTypes.number,
            threads: React.PropTypes.arrayOf(React.PropTypes.shape({
                id: React.PropTypes.string,
                pc: React.PropTypes.string,
                source: React.PropTypes.shape({
                    line: React.PropTypes.number,
                    file: React.PropTypes.string
                })
            })),
        })).isRequired,

        /** The selected context, as indicated by the user */
        currentContextIndex: React.PropTypes.number.isRequired,

        /** The operation history */
        history: React.PropTypes.shape({
            baseIndex: React.PropTypes.number,
            operations: React.PropTypes.arrayOf(React.PropTypes.shape({
                name: React.PropTypes.string,
                operands: React.PropTypes.arrayOf(React.PropTypes.shape({
                    name: React.PropTypes.string,
                    type: React.PropTypes.string,
                    value: React.PropTypes.any
                }))
            }))
        }),

        sourceMap: React.PropTypes.shape({
            startLineNo: React.PropTypes.number,
            lines: React.PropTypes.arrayOf(React.PropTypes.string)
        }).isRequired,

        /** The request processing function wired up to the event processor */
        process: React.PropTypes.func.isRequired,
    },

    getDefaultProps: function() {
        return {
            contexts: [],
            currentContextIndex: -1,
            history: {
                numDisplayedOps: 0,
                baseIndex: -1,
                operations: []
            },
            sourceMap: {
                startLineNo: 0,
                lines: []
            },
            process: function() {}
        }
    },

    render: function() {
        let currentContext;
        if (this.props.contexts.length > 0) {
            if (this.props.currentContextIndex >= this.props.contexts.length ||
                this.props.currentContextIndex < 0) 
            {
                console.log("Invalid context index: " + this.props.currentContextIndex);
                currentContext = this.props.contexts[0];
            }else{
                currentContext = this.props.contexts[this.props.currentContextIndex];
            }
        }else{
            currentContext = {
                id: "-1",
                processId: "-1",
                activeThreadIndex: -1,
                threads: []
            };
        }

        return (
            <Grid fluid={true}>
                <Panel>
                    <Row className="contentRow">
                        <Col xs={3} className="contextSelector">
                            <ContextSelector contexts={this.props.contexts} 
                                currentContextIndex={this.props.currentContextIndex}
                                process={this.props.process}/>
                        </Col>
                        <Col xs={8} className="contextPane">
                            <Row className="sourceViewerRow">
                                <SourceViewer currentContext={currentContext}
                                    sourceMap={this.props.sourceMap}
                                    process={this.props.process}/>
                            </Row>
                            <Row className="statusLine">
                                <StatusLine currentContext={currentContext}/>
                            </Row>
                            <Row className="commandLineRow">
                                <CommandLine history={this.props.history}
                                    process={this.props.process}/>
                            </Row>
                        </Col>
                    </Row>
                    <Row className="footer">
                        <a target="_blank" 
                           href="https://github.com/mcnulty/udidb">udidb</a>
                    </Row>
                </Panel>
            </Grid>
        )
    }
});