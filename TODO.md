A list of enhancements/bugfixes for the future
==============================================
- stack tracing support
- contextualize function/global var lookup based on a TranslationUnit
- allow use of Java8 lambdas
  - org.reflections in use doesn't currently process classes that use
    lambdas
- develop paradigm for optional arguments to Operations
- expose more functionality via builtin Operations
  - breakpoint management
  - function/variable/type query
- more complete ExpressionCompiler for C, including:
  - handling for global variables
  - more cases implemented in ExpressionSimplificationVisitor
  - code generation for x86/x86_64
