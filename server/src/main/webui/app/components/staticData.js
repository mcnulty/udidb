/** test data used by udidb */

var staticProps = {
    contexts: [
        {
            id: "1234",
            processId: "5665",
            activeThreadIndex: 0,
            threads: [
                {
                    id: "1",
                    pc: "eff0f0",
                    source: {
                        line: 1215,
                        file: "main.c"
                    }
                },
                {
                    id: "2",
                    pc: "eff0f0",
                    source: {
                        line: 110,
                        file: "events.c"
                    }
                },
            ],
            sourceMap: {
                "main.c" : {
                    startLineNo: 1200,
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
                },
                "events.c" : {
                    startLineNo: 90,
                    lines: [
                        "void free_event_list(udi_event *event_list) {",
                        "    if (event_list == NULL) return;",
                        "",
                        "    udi_event *current_event = event_list;",
                        "    while ( current_event != NULL ) {",
                        "        udi_event *next_event = current_event->next_event;",
                        "        free_event(current_event);",
                        "        current_event = next_event;",
                        "    }",
                        "}",
                        "",
                        "const char *get_event_type_str(udi_event_type event_type) {",
                        "    return event_type_str(event_type);",
                        "}",
                        "",
                        "/**",
                        " * Decodes a udi_event_internal event for the specified process",
                        " * into an exportable udi_event",
                        " *",
                        " * @param proc          process handle",
                        " * @param event         the internal event",
                        " *",
                        " * @return the new event",
                        " */",
                        "udi_event *decode_event(udi_process *proc, udi_event_internal *event) {",
                        "    udi_event *ret_event = (udi_event *)malloc(sizeof(udi_event));",
                    ]
                }
            },
            history: {
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
                        ],
                        result: "3"
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
                        ],
                        result: "0xdeadbeef"
                    }
                ]
            },
        },
        {
            id: "1235",
            processId: "60001",
            activeThreadIndex: 0,
            threads: [
                {
                    id: "1",
                    pc: "eff0f7",
                    source: {
                        line: 20,
                        file: "main.cxx"
                    }
                },
            ],
            sourceMap: {
                "main.cxx" : {
                    startLineNo: 10,
                    lines: [
                        "    char *argv[] = { NULL };",
                        "",
                        "    udi_proc_config config;",
                        "    config.root_dir = NULL;",
                        "",
                        "    udi_error_e error_code;",
                        "    char *errmsg = NULL;",
                        "    udi_process *proc = create_process(TEST_BINARY, argv, NULL, &config, &error_code, &errmsg);",
                        "    free(errmsg);",
                        "",
                        "    test_assert(proc != NULL);",
                        "",
                        "    udi_thread *thr = get_initial_thread(proc);",
                        "    test_assert(thr != NULL);",
                        "",
                        "    udi_error_e result = create_breakpoint(proc, TEST_FUNCTION);",
                        "    assert_no_error(proc, result);",
                        "",
                        "    result = install_breakpoint(proc, TEST_FUNCTION);",
                        "    assert_no_error(proc, result);",
                        "",
                        "    result = continue_process(proc);",
                        "    assert_no_error(proc, result);",
                        "",
                        "    wait_for_breakpoint(thr, TEST_FUNCTION);",
                    ]
                }
            },
            history: {
                baseIndex: 20,
                operations: [
                    {
                        name: "break",
                        operands: [
                            { 
                                name: "value", 
                                type: "list",
                                value: [
                                    "main"
                                ]
                            }
                        ],
                        result: "Set breakpoint at 0xdeadbeef"
                    },
                    {
                        name: "continue",
                        operands: [
                            {
                                name: "value",
                                type: "list",
                                value: [
                                    "main"
                                ]
                            }
                        ],
                        result: "Breakpoint at 0xdeadbeef hit"
                    },
                    {
                        name: "print",
                        operands: [
                            {
                                name: "value",
                                type: "list",
                                value: [
                                    "localVar"
                                ]
                            }
                        ],
                        result: "36"
                    }
                ]
            },
        }
    ],

    currentContextIndex: 0,

    prefs: {
        history: {
            numDisplayedOps: 3,
        },
    },
};

export default staticProps;
