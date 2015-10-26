import React from "react"
import { Button, ButtonGroup, NavItem, Collapse } from "react-bootstrap"

export default React.createClass({
    getInitialState: function() {
        return {}
    },

    render: function() {
        var { title, ...other } = this.props;
        return (
            <NavItem {...other}>
                <span>
                    { this.props.title }
                </span>
                <Collapse in={this.props.active}>
                    <div>
                        <ButtonGroup vertical block>
                            <Button bsStyle="info">
                                Thread 1 - src/main.c:1234
                            </Button>
                            <Button>
                                Thread 2 - src/events.c:120
                            </Button>
                        </ButtonGroup>
                    </div>
                </Collapse>
            </NavItem>
        );
    }
});
