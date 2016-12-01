import * as React from "react";
import {
    Button,
    ButtonGroup,
    NavItem,
    NavItemProps,
    Collapse
} from "react-bootstrap";

import {
    Context,
    UdidbRequest,
    PUT_METHOD
} from "./types";

export interface Props {
    readonly context: Context;
    readonly process: (request: UdidbRequest) => void;
    readonly navProps: NavItemProps;
}

export class Component extends React.Component<Props, {}> {

    _handleThreadSelect(index: number, e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        this.props.process(new UdidbRequest(PUT_METHOD, "currentContext.activeThreadIndex",
            "" + index));
    }

    render() {
        let context = this.props.context;
        let process = this.props.process;
        let navProps = this.props.navProps;

        let title: string;
        let threadsContent: any;
        if (process !== null) {
            let threadItems = context.threads.map(function(currentValue, index, array) {
                let buttonStyle = (index === context.activeThreadIndex ? "info" : "default");
                return (
                    <Button onClick={this._handleThreadSelect.bind(this, index)}
                        key={currentValue.id}
                        bsStyle={buttonStyle}>
                        {"Thread " + currentValue.id}
                    </Button>
                );
            }, this);
            title = "Process " + this.props.context.processId;
            threadsContent = (
                <Collapse in={this.props.navProps.active}>
                    <div>
                        <ButtonGroup vertical block>
                            {threadItems}
                        </ButtonGroup>
                    </div>
                </Collapse>);
        } else {
            title = "Global Context";
            threadsContent = "";
        }

        return (
            <NavItem {...navProps}>
                <span>
                    {title}
                </span>
                <div>
                    {threadsContent}
                </div>
            </NavItem>
        );
    }
}
