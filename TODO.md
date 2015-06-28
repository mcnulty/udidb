A list of enhancements/bugfixes for the future
==============================================
- stack tracing support
- contextualize function/global var lookup based on a TranslationUnit
- develop paradigm for optional arguments to Operations
- create server module that provides HTTP REST API access to engine
  and Operations
- expose more functionality via builtin Operations
  - breakpoint management
  - function/variable/type query
- more complete ExpressionCompiler for C, including:
  - handling for global variables
  - more cases implemented in ExpressionSimplificationVisitor
  - code generation for x86/x86_64
