import React from "react"
import { Panel, Grid, Row, Col } from "react-bootstrap";

import ContextSelector from "./contextSelector.js"
import SourceViewer from "./sourceViewer.js"
import CommandLine from "./commandLine.js"
import StatusLine from "./statusLine.js"

export default React.createClass({
    render: function() {
        return (
            <Grid fluid={true}>
                <Panel>
                    <Row className="contentRow">
                        <Col xs={3} className="contextSelector">
                            <ContextSelector/>
                        </Col>
                        <Col xs={8} className="contextPane">
                            <Row className="sourceViewerRow">
                                <SourceViewer/>
                            </Row>
                            <Row className="statusLine">
                                <StatusLine/>
                            </Row>
                            <Row className="commandLineRow">
                                <CommandLine/>
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
