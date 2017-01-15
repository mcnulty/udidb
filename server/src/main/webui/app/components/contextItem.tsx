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
    readonly active: boolean;
}

export class Component extends React.Component<Props, {}> {

    private handleThreadSelect(index: number, e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        this.props.process(new UdidbRequest(PUT_METHOD, "currentContext.activeThreadIndex",
            "" + index));
    }

    public render(): JSX.Element {
        let context = this.props.context;
        let process = this.props.process;
        let navProps = this.props.navProps;

        let title: string;
        let threadsContent: any;
        if (context.id !== "") {
            let threadItems = context.threads.map(function(currentValue, index, array) {
                let buttonStyle = (index === context.activeThreadIndex ? "info" : "default");
                return (
                    <Button onClick={this.handleThreadSelect.bind(this, index)}
                        key={index}
                        bsStyle={buttonStyle}>
                        {"Thread " + currentValue.id}
                    </Button>
                );
            }, this);
            title = "Process " + this.props.context.processId;
            threadsContent = (
                <Collapse in={this.props.active}>
                    <div>
                        <ButtonGroup vertical block>
                            {threadItems}
                        </ButtonGroup>
                    </div>
                </Collapse>
            );
        } else {
            title = "Global Context";
            threadsContent = "";
        }

        return (
            <NavItem {...navProps} active={this.props.active}>
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
