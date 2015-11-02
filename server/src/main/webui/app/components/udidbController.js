import React from "react"

import Udidb from "./udidb.js"

var staticProps = {};

export default React.createClass({

    render: function() {
        return (
            <Udidb {...staticProps} process={this.process}/>
        );
    },

    process: function(request) {

    }
});

staticProps = {
    contexts: [
        {
            id: "1234",
            processId: "5665",
            activeThreadIndex: 0,
            threads: [
                {
                    id: "1",
                    pc: "0xeff0f0",
                    source: {
                        line: 1234,
                        file: "src/main.c"
                    }
                },
                {
                    id: "2",
                    pc: "0xeff0f0",
                    source: {
                        line: 120,
                        file: "src/events.c"
                    }
                },
            ]
        },
        {
            id: "1235",
            processId: "60001",
            activeThreadIndex: 0,
            threads: [
                {
                    id: "1",
                    pc: "0xeff0f7",
                    source: {
                        line: 20,
                        file: "main.cxx"
                    }
                },
            ]
        }
    ],

    currentContextIndex: 0,

    history: {
        numDisplayedOps: 2,
        baseIndex: 20,
        operations: [
            {
                name: "eval",
                operands: [
                    { 
                        name: "value", 
                        type: "list",
                        value: [
                            "1",
                            "+",
                            "2"
                        ]
                    }
                ]
            },
            {
                name: "print",
                operands: [
                    {
                        name: "value",
                        type: "list",
                        value: [
                            "main"
                        ]
                    }
                ]
            }
        ]
    },

    sourceMap: {
        startLineNo: 30,
        lines: [
            "#include <stdlib.h>",
            "#define MACRO 1",
            "",
            "udi_process *create_process(const char *executable, char * const argv[],",
            "        char * const envp[], const udi_proc_config *config,",
            "        udi_error_e *error_code, char **errmsg)",
            "{",
            "    // Validate arguments",
            "    if (argv == NULL || executable == NULL) {",
            "        allocate_error(UDI_ERROR_REQUEST, \"invalid arguments\", error_code, errmsg);",
            "        return NULL;",
            "    }",
            "",
            "    udi_error_e local_error = UDI_ERROR_NONE;",
            "    char *local_errmsg = NULL;",
            "    udi_process *proc = (udi_process *)malloc(sizeof(udi_process));",
            "    do{",
            "        if ( proc == NULL ) {",
            "            local_errmsg = \"malloc failed\";",
            "            local_error = UDI_ERROR_LIBRARY;",
            "            break;",
            "        }",
            "",
            "        memset(proc, 0, sizeof(udi_process));",
            "        proc->error_code = UDI_ERROR_NONE;",
            "        proc->errmsg.size = ERRMSG_SIZE;",
            "        proc->errmsg.msg[ERRMSG_SIZE-1] = '\0';",
            "        proc->running = 0;",
            "        proc->terminated = 0;",
            "",
            "        if ( config->root_dir != NULL ) {"
        ]
    }
};
