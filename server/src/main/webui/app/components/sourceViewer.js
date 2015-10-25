
import React from "react"
import hljs from "highlight.js"

var topLevelStyle = {
    fontFamily: 'monospace',
    fontSize: '11',
    whiteSpace: 'pre',
    border: '1px solid rgb(204, 204, 204)',
    borderRadius: '4px',
    padding: '5px 5px 5px 5px'
};
var testSource = ""

export default React.createClass({

    render: function() {
        var highlightedSourceObj = hljs.highlightAuto(testSource, [ "C" ]);
        console.log("Source Language " + highlightedSourceObj["language"]);
        return (
            <div className="hljs" dangerouslySetInnerHTML={ { __html: highlightedSourceObj["value"] } } style={topLevelStyle}/>
        )
    }
});

testSource =
"#include <stdlib.h>\n" +
"#define MACRO 1\n" +
"\n" +
"udi_process *create_process(const char *executable, char * const argv[],\n" +
"        char * const envp[], const udi_proc_config *config,\n" +
"        udi_error_e *error_code, char **errmsg)\n" +
"{\n" +
"    // Validate arguments\n" +
"    if (argv == NULL || executable == NULL) {\n" +
"        allocate_error(UDI_ERROR_REQUEST, \"invalid arguments\", error_code, errmsg);\n" +
"        return NULL;\n" +
"    }\n" +
"\n" +
"    udi_error_e local_error = UDI_ERROR_NONE;\n" +
"    char *local_errmsg = NULL;\n" +
"    udi_process *proc = (udi_process *)malloc(sizeof(udi_process));\n" +
"    do{\n" +
"        if ( proc == NULL ) {\n" +
"            local_errmsg = \"malloc failed\";\n" +
"            local_error = UDI_ERROR_LIBRARY;\n" +
"            break;\n" +
"        }\n" +
"\n" +
"        memset(proc, 0, sizeof(udi_process));\n" +
"        proc->error_code = UDI_ERROR_NONE;\n" +
"        proc->errmsg.size = ERRMSG_SIZE;\n" +
"        proc->errmsg.msg[ERRMSG_SIZE-1] = '\0';\n" +
"        proc->running = 0;\n" +
"        proc->terminated = 0;\n" +
"\n" +
"        if ( config->root_dir != NULL ) {\n"

/*
testSource = "\
udi_process *create_process(const char *executable, char * const argv[],\n\
        char * const envp[], const udi_proc_config *config,\n\
        udi_error_e *error_code, char **errmsg)\n\
{\n\
    // Validate arguments\n\
    if (argv == NULL || executable == NULL) {\n\
        allocate_error(UDI_ERROR_REQUEST, \"invalid arguments\", error_code, errmsg);\n\
        return NULL;\n\
    }\n\
\n\
    udi_error_e local_error = UDI_ERROR_NONE;\n\
    char *local_errmsg = NULL;\n\
    udi_process *proc = (udi_process *)malloc(sizeof(udi_process));\n\
    do{\n\
        if ( proc == NULL ) {\n\
            local_errmsg = \"malloc failed\";\n\
            local_error = UDI_ERROR_LIBRARY;\n\
            break;\n\
        }\n\
\n\
        memset(proc, 0, sizeof(udi_process));\n\
        proc->error_code = UDI_ERROR_NONE;\n\
        proc->errmsg.size = ERRMSG_SIZE;\n\
        proc->errmsg.msg[ERRMSG_SIZE-1] = '\0';\n\
        proc->running = 0;\n\
        proc->terminated = 0;\n\
\n\
        if ( config->root_dir != NULL ) {\n\
            if ( set_root_dir(proc, config->root_dir) != 0 ) {\n\
                local_errmsg = \"failed to set root directory\";\n\
                local_error = UDI_ERROR_LIBRARY;\n\
                break;\n\
            }\n\
        }else{\n\
            proc->root_dir = DEFAULT_UDI_ROOT_DIR;\n\
        }\n\
\n\
        proc->pid = fork_process(proc, executable, argv, envp);\n\
        if ( proc->pid == INVALID_UDI_PID ) {\n\
            local_errmsg = \"failed to create process\";\n\
            local_error = UDI_ERROR_REQUEST;\n\
            break;\n\
        }\n\
\n\
        if ( initialize_process(proc) != 0 ) {\n\
            local_errmsg = \"failed to initialize process\";\n\
            local_error = UDI_ERROR_LIBRARY;\n\
            break;\n\
        }\n\
    }while(0);\n\
\n\
    *error_code = local_error;\n\
    if ( local_errmsg != NULL ) {\n\
        if ( proc ) {\n\
            free(proc);\n\
            proc = NULL;\n\
        }\n\
\n\
        allocate_error(local_error, local_errmsg, error_code, errmsg);\n\
    }\n\
\n\
    return proc;\n\
    }"
*/
