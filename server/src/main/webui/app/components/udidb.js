import React from "react"
import { Panel, Grid, Row, Col } from "react-bootstrap";

import ContextSelector from "./contextSelector.js"
import SourceViewer from "./sourceViewer.js"
import CommandLine from "./commandLine.js"
import StatusLine from "./statusLine.js"

export default React.createClass({

    getDefaultProps: function() {
        return {
            contexts: [],
            currentContextIndex: -1,
            prefs: {
                history: {
                    numDisplayedOps: 1
                }
            },
            process: function() {}
        }
    },

    render: function() {
        let currentContext;
        if (this.props.contexts.length > 0 && this.props.currentContextIndex >= 0) {
            if (this.props.currentContextIndex >= this.props.contexts.length)
            {
                console.log("Invalid context index: " + this.props.currentContextIndex);
                currentContext = this.props.contexts[0];
            }else{
                currentContext = this.props.contexts[this.props.currentContextIndex];
            }
        }else{
            currentContext = null;
        }

        return (
            <Grid fluid={true}>
                <Panel>
                    <Row className="contentRow">
                        <Col xs={3} className="contextSelector">
                            <ContextSelector contexts={this.props.contexts}
                                globalContext={this.globalContext} 
                                currentContextIndex={this.props.currentContextIndex}
                                process={this.props.process}/>
                        </Col>
                        <Col xs={8} className="contextPane">
                            <Row className="sourceViewerRow">
                                <SourceViewer currentContext={currentContext}
                                    globalContext={this.globalContext}
                                    defaultSourceContent={this.props.defaultSourceContent}
                                    process={this.props.process}/>
                            </Row>
                            <Row className="statusLine">
                                <StatusLine currentContext={currentContext}/>
                            </Row>
                            <Row className="commandLineRow">
                                <CommandLine currentContext={currentContext}
                                    globalContext={this.props.globalContext}
                                    historyPrefs={this.props.prefs.history}
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
    },

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

            /** The operation history for this context */
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

            /** the source map for the context */
            sourceMap: React.PropTypes.any
        })).isRequired,

        /** The global operation history */
        globalContext: React.PropTypes.shape({
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
            })
        }).isRequired,

        /** The selected context, as indicated by the user */
        currentContextIndex: React.PropTypes.number.isRequired,

        /** user preferences */
        prefs: React.PropTypes.shape({
            history: React.PropTypes.shape({
                numDisplayedOps: React.PropTypes.number
            }).isRequired
        }).isRequired,

        defaultSourceContent: React.PropTypes.arrayOf(React.PropTypes.string),

        /** The request processing function wired up to the event processor */
        process: React.PropTypes.func.isRequired,
    },

});
