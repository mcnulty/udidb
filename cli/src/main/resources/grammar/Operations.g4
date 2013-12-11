grammar Operations;

@header {
package net.udidb.net.udidb.cli.parser;
}

// Rules

unaryExp : LITERAL
         | '-' LITERAL
         | '!' LITERAL
         ;

multDivExp : unaryExp
           (
               '*' unaryExp
             | '/' unaryExp
           )*
           ;

addSubExp : multDivExp
          (
              '+' multDivExp
            | '-' multDivExp
          )*
          ;

inequalityExp : addSubExp
              (
                  '<'  addSubExp
                | '>'  addSubExp
                | '<=' addSubExp
                | '>=' addSubExp
              )*
              ;

equalityExp : inequalityExp ( '==' inequalityExp )* ;

andExp : equalityExp ( '&&' equalityExp )* ;

orExp : andExp ( '||' andExp )* ;

expression : orExp
           | '(' expression ')' ;

// Literals

WHITESPACE : [ \t]+ -> skip;

LITERAL : STRING | INT | FLOAT | IDENT ;

IDENT : [a-zA-Z_$0-9]+ ;

STRING : '"' [^"\r\n]* '"';

INT : BASE2_INT | BASE8_INT | BASE16_INT | BASE10_INT ;

BASE2_INT : '-'? '0' 'b' [0-1]+ ;

BASE8_INT : '-'? '0' [0-7]+ ;

BASE16_INT : '-'? '0' 'x' [0-9a-fA-F]+ ;

BASE10_INT : ( '-'? [1-9] [0-9]* | '0' ) ;

FLOAT : BASIC_FLOAT | EXP_FLOAT ;

EXP_FLOAT : '-'? [0-9]+ 'e' '-'? [0-9]+ ;

BASIC_FLOAT : '-'? [0-9]+ '.' '-'? [0-9]+ ;
